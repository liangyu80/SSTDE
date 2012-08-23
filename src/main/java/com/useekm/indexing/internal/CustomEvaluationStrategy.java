/*
 * Copyright 2011 by TalkingTrends (Amsterdam, The Netherlands)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://opensahara.com/licenses/apache-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.useekm.indexing.internal;

import info.aduna.iteration.CloseableIteration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.ProjectionElemList;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.impl.ExternalSet;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * An EvaluationStrategy for {@link QueryEvaluator}s that uses a top down approach were a query is evaluated by this stretegy top-down until
 * a {@link TupleExpr} subtree is reached that does not contain non-standard {@link TupleExpr} elements and hence can be evaluated natively by the underlying {@link Sail}.
 * <p>
 * Depending on the support for custom {@link TupleExpr} expressions such as {@link ExternalSet}, this EvaluationStrategy will either use Sesame query algebra, or call
 * {@link SailConnection#evaluate(TupleExpr, Dataset, BindingSet, boolean)} for the provided query or subexpressions of the query.
 * <p>
 * When all query algebra in the passed {@link TupleExpr} is supported, the query is passed to the SailConnection unaltered.
 * <p>
 * When {@link TupleExpr} elements are not supported, the strategy will split the query in various parts and use Sesame's {@link EvaluationStrategyImpl} for parts of the query that
 * contain {@link ExternalSet} expressions, and the native {@link SailConnection#evaluate(TupleExpr, Dataset, BindingSet, boolean)} for parts that can be safely processed by the
 * SailConnection.
 */
public class CustomEvaluationStrategy extends EvaluationStrategyImpl {
    //private static final Logger LOG = LoggerFactory.getLogger(CustomEvaluationStrategy.class);
    private SailConnection conn;
    private boolean includeInferred;
    private AbstractQueryEvaluator queryEvaluator;

    public CustomEvaluationStrategy(AbstractQueryEvaluator queryEvaluator, SailConnection conn, ValueFactory vf, Dataset dataset, boolean includeInferred) {
        super(new GenericTripleSource(conn, vf, includeInferred), dataset);
        this.conn = conn;
        this.includeInferred = includeInferred;
        this.queryEvaluator = queryEvaluator;
    }

    protected SailConnection getConn() {
        return conn;
    }

    protected boolean isIncludeInferred() {
        return includeInferred;
    }

    protected AbstractQueryEvaluator getQueryEvaluator() {
        return queryEvaluator;
    }

    @Override public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(TupleExpr expr, BindingSet bindings)
        throws QueryEvaluationException {
        if (queryEvaluator.needsCustomEvaluation(expr)) {
            if (expr instanceof QueryRoot)
                queryEvaluator.customOptimize(this, expr, dataset, bindings);
            return evaluateCustom(expr, bindings);
        } else
            return evaluateNative(expr, bindings);
    }

    /**
     * Evaluates this (sub)query with this evaluation strategy. Will first call
     * {@link AbstractQueryEvaluator#customOptimize(org.openrdf.query.algebra.evaluation.EvaluationStrategy, TupleExpr, Dataset, BindingSet)} if
     * the expr is a {@link QueryRoot}.
     */
    protected CloseableIteration<BindingSet, QueryEvaluationException> evaluateCustom(TupleExpr expr, BindingSet bindings) throws QueryEvaluationException {
        return super.evaluate(expr, bindings);
    }

    /**
     * Evaluates this (sub)query against the connection. Will wrap the expr in a proper {@link Projection} when necessary.
     */
    @SuppressWarnings("unchecked") protected CloseableIteration<BindingSet, QueryEvaluationException> evaluateNative(TupleExpr expr, BindingSet bindings)
        throws QueryEvaluationException {
        try {
            QueryBindingSet constBindings = new QueryBindingSet();
            if (!(expr instanceof QueryRoot))
                expr = addProjection(expr, constBindings);
            CloseableIteration<BindingSet, QueryEvaluationException> result =
                (CloseableIteration<BindingSet, QueryEvaluationException>)conn.evaluate(expr, dataset, bindings, includeInferred);
            if (constBindings.size() != 0)
                result = new JoinWithSingletonIteration(result, constBindings);
            return result;
        } catch (SailException e) {
            throw new QueryEvaluationException(e);
        }
    }

    protected TupleExpr addProjection(TupleExpr expr, QueryBindingSet constBindings) {
        Set<String> names = expr.getBindingNames();
        List<ProjectionElem> resultBindings = new ArrayList<ProjectionElem>(names.size());
        boolean searchedConstBindings = false;
        for (String name: names) {
            if (name.charAt(0) == '-') { // that's how the sesame parsers name anonymous/const variables
                if (!searchedConstBindings) {
                    expr.visit(new ConstFinder(constBindings));
                    searchedConstBindings = true;
                }
            } else
                resultBindings.add(new ProjectionElem(name));
        }
        ProjectionElemList peList = new ProjectionElemList();
        peList.setElements(resultBindings); //will set by referencing resultBindings instead of iterating&copying if we had put it in constructor call.
        expr = new Projection(expr.clone(), new ProjectionElemList(resultBindings));
        return expr;
    }

    private static class ConstFinder extends QueryModelVisitorBase<RuntimeException> {
        private QueryBindingSet constBindings;

        public ConstFinder(QueryBindingSet constBindings) {
            this.constBindings = constBindings;
        }

        @Override public void meet(Var node) {
            Value value = node.getValue();
            String name = node.getName();
            if (name != null && value != null && name.charAt(0) == '-' && !constBindings.hasBinding(name))
                constBindings.addBinding(name, value);
        }
    }
}
