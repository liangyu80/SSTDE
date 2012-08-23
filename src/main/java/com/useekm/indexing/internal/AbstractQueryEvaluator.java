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

import java.util.Collection;
import java.util.Collections;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.openrdf.query.algebra.evaluation.impl.ConstantOptimizer;
import org.openrdf.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.openrdf.query.algebra.evaluation.impl.FilterOptimizer;
import org.openrdf.query.algebra.evaluation.impl.IterativeEvaluationOptimizer;
import org.openrdf.query.algebra.evaluation.impl.OrderLimitOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryJoinOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryModelNormalizer;
import org.openrdf.query.algebra.evaluation.impl.SameTermFilterOptimizer;
import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import edu.ncsa.sstde.indexing.IndexedJoinOptimizer;


/**
 * {@link QueryEvaluator} implementation that will use the {@link CustomEvaluationStrategy} to split a query ({@link TupleExpr}) into parts that can be evaluated natively by the
 * underlying store and parts that need a custom evaluation strategy. By default it will use {@link CustomEvaluationStrategy} to do the splitting and custom evaluation of queries.
 */
public abstract class AbstractQueryEvaluator implements QueryEvaluator {
    //private static final Logger LOG = LoggerFactory.getLogger(AbstractQueryEvaluator.class);
    private Collection<Class<? extends TupleExpr>> expressionsNeedingCustomEval;

    protected AbstractQueryEvaluator() {
        this.expressionsNeedingCustomEval = Collections.emptyList();
    }

    protected AbstractQueryEvaluator(Collection<Class<? extends TupleExpr>> expressionsNeedingCustomEval) {
        this.expressionsNeedingCustomEval = expressionsNeedingCustomEval;
    }

    @Override public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(SailConnection conn, ValueFactory vf, Dataset dataset, boolean includeInferred,
        QueryRoot tupleExpr, BindingSet bindings) throws SailException {
        EvaluationStrategy evaluationStrategy = getEvaluationStrategy(conn, vf, dataset, includeInferred);
        try {
            new BindingAssigner().optimize(tupleExpr, dataset, bindings);
            new ConstantOptimizer(evaluationStrategy).optimize(tupleExpr, dataset, bindings);
            new CompareOptimizer().optimize(tupleExpr, dataset, bindings);
            new ConjunctiveConstraintSplitter().optimize(tupleExpr, dataset, bindings);
            new DisjunctiveConstraintOptimizer().optimize(tupleExpr, dataset, bindings);
            new SameTermFilterOptimizer().optimize(tupleExpr, dataset, bindings);
            new QueryModelNormalizer().optimize(tupleExpr, dataset, bindings);
            new IndexedJoinOptimizer().optimize(tupleExpr, dataset, bindings);
            new IterativeEvaluationOptimizer().optimize(tupleExpr, dataset, bindings);
            new FilterOptimizer().optimize(tupleExpr, dataset, bindings);
            new OrderLimitOptimizer().optimize(tupleExpr, dataset, bindings);
            return evaluationStrategy.evaluate(tupleExpr, bindings);
        } catch (QueryEvaluationException e) {
            if (e.getCause() instanceof SailException)
                throw (SailException)e.getCause();
            throw new SailException(e);
        }
    }

    /**
     * @return The {@link EvaluationStrategy} that should be used for queries.
     * 
     * @see CustomEvaluationStrategy
     */
    protected EvaluationStrategy getEvaluationStrategy(SailConnection conn, ValueFactory vf, Dataset dataset, boolean includeInferred) {
        return new CustomEvaluationStrategy(this, conn, vf, dataset, includeInferred);
    }

    /**
     * @return false if the entire {@link TupleExpr} can be evaluated by the underlying store, false if custom evaluation is needed.
     */
    protected boolean needsCustomEvaluation(TupleExpr expr) {
        return hasElement(expr, expressionsNeedingCustomEval);
    }

    public static boolean hasElement(TupleExpr expr, Collection<Class<? extends TupleExpr>> elements) {
        if (elements.contains(expr.getClass()))
            return true;
        else if (expr instanceof UnaryTupleOperator)
            return hasElement(((UnaryTupleOperator)expr).getArg(), elements);
        else if (expr instanceof BinaryTupleOperator) {
            BinaryTupleOperator bto = (BinaryTupleOperator)expr;
            return hasElement(bto.getLeftArg(), elements) || hasElement(bto.getRightArg(), elements);
        }
        return false;
    }

    /**
     * Optimizes a query {@link TupleExpr} in a way that works well enough for most {@link Sail} implementations.
     */
    protected void customOptimize(EvaluationStrategy strategy, TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
        new BindingAssigner().optimize(tupleExpr, dataset, bindings);
        new ConstantOptimizer(strategy).optimize(tupleExpr, dataset, bindings);
        new CompareOptimizer().optimize(tupleExpr, dataset, bindings);
        new ConjunctiveConstraintSplitter().optimize(tupleExpr, dataset, bindings);
        new DisjunctiveConstraintOptimizer().optimize(tupleExpr, dataset, bindings);
        new SameTermFilterOptimizer().optimize(tupleExpr, dataset, bindings);
        new QueryModelNormalizer().optimize(tupleExpr, dataset, bindings);
        new QueryJoinOptimizer().optimize(tupleExpr, dataset, bindings);
        new IterativeEvaluationOptimizer().optimize(tupleExpr, dataset, bindings);
        new FilterOptimizer().optimize(tupleExpr, dataset, bindings);
        new OrderLimitOptimizer().optimize(tupleExpr, dataset, bindings);
        //TODO Filter (true) { } -> cleanup?
    }
}