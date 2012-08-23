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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.Validate;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.sail.SailConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.useekm.indexing.algebra.indexer.AbstractIdxQuery;
import com.useekm.indexing.algebra.indexer.IdxConstraintQuery;
import com.useekm.indexing.algebra.indexer.IdxJoin;
import com.useekm.indexing.algebra.indexer.IdxMatchQuery;
import com.useekm.indexing.algebra.indexer.IdxOrder;
import com.useekm.indexing.algebra.indexer.IdxQuery;
import com.useekm.util.SesameUtil;

import edu.ncsa.sstde.indexing.algebra.IndexerExpr;

/**
 * An EvaluationStrategy for Bigdata. It uses a special override (which is specific to a BigadataSail) of the
 * {@link SailConnection#evaluate(TupleExpr, Dataset, BindingSet, boolean)} method that instead of a {@link BindingSet} accepts a stream of {@link BindingSet}s. Effectively in most
 * cases the entire query (or otherwise larger parts of the query) can be evaluated by Bigdata natively, instead of requiring custom evaluation by the {@link EvaluationStrategy},
 * thus having better performance.
 */
public class BigdataEvaluationStrategy extends CustomEvaluationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(BigdataEvaluationStrategy.class);
    //We invoke BigdataSailConnection.evaluate via reflection because we currently do not compile against the Bigdata specific API's.
    //TODO We should just compile against Bigdata (have Bigdata as a compile-time dependency). This is allowed with the GPL license of Bigdata (since that basically only affects
    // distribution) however, it means that we should have a Bigdata maven artifact somewhere, and if we distribute that ourselves probably also a source-code artifact....
    private static Method streamingEvaluate = getStreamingEvaluate();

    public BigdataEvaluationStrategy(AbstractQueryEvaluator queryEvaluator, SailConnection conn, ValueFactory vf, Dataset dataset, boolean includeInferred) {
        super(queryEvaluator, conn, vf, dataset, includeInferred);
    }

    @Override protected CloseableIteration<BindingSet, QueryEvaluationException> evaluateCustom(TupleExpr expr, BindingSet bindings)
        throws QueryEvaluationException {
        List<IndexerExpr> indexerExprs = new ArrayList<IndexerExpr>();
        findIndexerExpr(expr, indexerExprs);
        IndexerExpr indexerExpr = indexerExprs.size() == 1 ? indexerExprs.get(0) : null; // if there are more than one we need the super.evaluateCustom
        if (expr != indexerExpr && indexerExpr != null && !indexerExpr.isOrdered() && !AbstractQueryEvaluator.hasElement(expr, BigdataQueryEvaluator.NON_NATIVE_NON_INLINE)
            && !isInRhsOfLeftJoin(expr, indexerExpr) //If it is in the RHS of a LeftJoin we can't just stream the results in, as that would mean skipping potentially valid
                                                     //bindings from the LHS. TODO there are cases in which we can do the streaming evaluate, i.e. when the variables that the
                                                     // IndexerExpr binds don't overlap with variables on the LHS of the join.
        ) {
            //Re-introduce all removed statement patterns and joins so Bigdata will check streamed solutions against the original patterns/joins:
            indexerExpr.replaceWith(asTupleExpr(indexerExpr.getQuery()));//No cloning necessary for Bigdata

            QueryBindingSet constBindings = new QueryBindingSet(bindings);
            if (!(expr instanceof QueryRoot))
                expr = addProjection(expr, constBindings);
            JoinWithSingletonIteration bindingSets = new JoinWithSingletonIteration(indexerExpr.evaluate(bindings), constBindings);
            CloseableIteration<BindingSet, QueryEvaluationException> result = null;
            try {
                result = evaluateNativeStreaming(expr, bindingSets);
                return result;
            } finally {
                //Close bindingSets if evaluateNativeStreaming fails (since in that case result is not created so won't be closed):
                if (result == null)
                    SesameUtil.closeQuietly(bindingSets);
            }
        } else
            return super.evaluateCustom(expr, bindings);
    }

    private boolean isInRhsOfLeftJoin(TupleExpr expr, IndexerExpr leaf) {
        QueryModelNode current = leaf;
        QueryModelNode parent = current.getParentNode();
        while (current != expr) {
            if (parent instanceof LeftJoin && ((LeftJoin)parent).getRightArg() == current)
                return true;
            current = parent;
            parent = current.getParentNode();
        }
        return false;
    }

    @SuppressWarnings("unchecked") private CloseableIteration<BindingSet, QueryEvaluationException> evaluateNativeStreaming(TupleExpr expr, JoinWithSingletonIteration bindingSets)
        throws QueryEvaluationException {
        try {
            //See remarks for streamingEvaluate member for why we have and should get rid of this reflection thing... 
            return (CloseableIteration<BindingSet, QueryEvaluationException>)streamingEvaluate.invoke(getConn(), expr, dataset, new QueryBindingSet(), bindingSets,
                isIncludeInferred(), null);
        } catch (InvocationTargetException e) {
            QueryEvaluationException newE;
            if (e.getCause() instanceof QueryEvaluationException)
                throw (QueryEvaluationException)e.getCause();
            else {
                //By writing it like this checkstyle doesn't warn about missing reference to original exception....
                newE = new QueryEvaluationException(e.getCause());
                throw newE;
            }
        } catch (IllegalArgumentException e) {
            throw new QueryEvaluationException("BigdataEvaluationStrategy is not properly initialized", e);
        } catch (IllegalAccessException e) {
            throw new QueryEvaluationException("BigdataEvaluationStrategy is not properly initialized", e);
        }
    }

    private TupleExpr asTupleExpr(IdxQuery idxQ) {
        if (idxQ instanceof IdxOrder)
            return asTupleExpr(((IdxOrder)idxQ).getQuery());
        else if (idxQ instanceof IdxJoin) {
            List<AbstractIdxQuery> queries = ((IdxJoin)idxQ).getQueries();
            Validate.isTrue(queries.size() > 1);
            Join result = new Join();
            int i = queries.size() - 1;
            result.setRightArg(asTupleExpr(queries.get(i--)));
            result.setLeftArg(asTupleExpr(queries.get(i--)));
            while (i > 0)
                result = new Join(asTupleExpr(queries.get(i--)), result);
            return result;
        } else if (idxQ instanceof IdxMatchQuery) {
            IdxMatchQuery mQ = (IdxMatchQuery)idxQ;
            return new StatementPattern(mQ.getSubjectVar(), mQ.getPredicateVar(), mQ.getObjectVar());
        } else {
            Validate.isTrue(idxQ instanceof IdxConstraintQuery);
            IdxConstraintQuery mQ = (IdxConstraintQuery)idxQ;
            return new StatementPattern(mQ.getSubjectVar(), mQ.getPredicateVar(), mQ.getObjectVar());
        }
    }

    /**
     * @return Searches for an {@link IndexerExpr} element in this query subtree. If the subtree has (or is) only one {@link IndexerExpr}, that is returned as the result. If there
     *         are more than one IndexerExpr elements in the subtree, or there are none, the method returns null.
     */
    protected void findIndexerExpr(TupleExpr expr, List<IndexerExpr> result) {
        if (expr instanceof IndexerExpr)
            result.add((IndexerExpr)expr);
        else if (expr instanceof UnaryTupleOperator)
            findIndexerExpr(((UnaryTupleOperator)expr).getArg(), result);
        else if (expr instanceof BinaryTupleOperator) {
            BinaryTupleOperator bto = (BinaryTupleOperator)expr;
            findIndexerExpr(bto.getLeftArg(), result);
            findIndexerExpr(bto.getRightArg(), result);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"}) private static Method getStreamingEvaluate() {
        try {
            Class connectionClass = Class.forName("com.bigdata.rdf.sail.BigdataSail$BigdataSailConnection");
            return connectionClass.getMethod("evaluate", TupleExpr.class, Dataset.class, BindingSet.class, CloseableIteration.class, Boolean.TYPE, Properties.class);
        } catch (ClassNotFoundException e) {
            LOG.warn("Could not initialize BigdataEvaluationStrategy properly. Bigdata is not on the classpath.", e);
        } catch (SecurityException e) {
            LOG.warn("Could not initialize BigdataEvaluationStrategy properly. Bigdata.evaluate is secured", e);
        } catch (NoSuchMethodException e) {
            LOG.warn("Could not initialize BigdataEvaluationStrategy properly. Bigdata.evaluate does not exist", e);
        }
        return null;
    }
}
