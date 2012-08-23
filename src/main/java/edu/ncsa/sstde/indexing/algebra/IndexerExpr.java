/*
 * Copyright 2010 by TalkingTrends (Amsterdam, The Netherlands)
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
package edu.ncsa.sstde.indexing.algebra;

import info.aduna.iteration.CloseableIteration;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.impl.ExternalSet;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.sail.SailConnection;

import com.useekm.indexing.algebra.indexer.AbstractIdxQuery;
import com.useekm.indexing.algebra.indexer.IdxJoin;
import com.useekm.indexing.algebra.indexer.IdxOrder;
import com.useekm.indexing.algebra.indexer.IdxQuery;
import com.useekm.indexing.internal.Indexer;

import edu.ncsa.sstde.indexing.IndexingSailConnection;
import edu.ncsa.sstde.indexing.GraphAnalyzer.MatchedIndexedGraph;

/**
 * A {@link TupleExpr} to iterate over a query result set of the {@link Indexer}.
 * Instances of this class are created by an {@link IndexingSailConnection} when
 * a query is evaluated.
 *
 * @see IdxQuery
 * @see QueryExtractor
 */
public class IndexerExpr extends ExternalSet implements Cloneable {
    private IdxQuery query;
//    private final Indexer indexer;
    private final ValueFactory valueFactory;
    private final MatchedIndexedGraph graph;
    
    public IndexerExpr(MatchedIndexedGraph graph, ValueFactory valueFactory) {
    	this.graph = graph;
    	this.valueFactory = valueFactory;
		// TODO Auto-generated constructor stub
	}

	/**
     * The searches that need to be done via the {@link Indexer}.
     */
    public IdxQuery getQuery() {
        return query;
    }

    /**
     * The indexer for which this {@link IndexerExpr} provides a search.
     */
    public Indexer getIndexer() {
        return graph.getIndexer();
    }

    
    public MatchedIndexedGraph getGraph() {
		return graph;
	}

	/**
     * The valueFactory of the connection.
     *
     * @see RepositoryConnection#getValueFactory()
     */
    public ValueFactory getValueFactory() {
        return valueFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAssuredBindingNames() {
        return query.getAllResultBindings();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getBindingNames() {
    	return  new HashSet<String>(this.graph.getNameMappings().values());
//        return query.getAllResultBindings();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Only for the common use where {@link SailConnection}s clone a {@link TupleExpr} before doing optimizations. This method <strong>does not</strong> provide a deep copy of the
     * contained {@link IndexQuery} queries.
     */
    @Override
    public IndexerExpr clone() {
        IndexerExpr result = (IndexerExpr)super.clone();
        //indexer and valueFactory can just point to the same instance
        //queries are not copied deep, which is not an issue since Sail implementation that clone this IndexExpr
        //  won't tamper with the queries object anymore.
        return result;
    }

    /**
     * See <a href="https://sourceforge.net/apps/trac/opensahara/ticket/7">#7</a>
     */
    @Override
    public double cardinality() {
        return 0.0; //See #14
    }

    /**
     * Returns an iterator over the result set of the contained {@link IdxQuery}.
     */
    @Override
    public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(BindingSet bindings) throws QueryEvaluationException {
        return getIndexer().iterator(valueFactory, this, bindings);
    }

    /**
     * Joins all the queries of the given {@link IndexerExpr} to this.
     */
    public void joinWith(IndexerExpr other) {
        Validate.isTrue(getIndexer() == other.getIndexer());
        IdxJoin join = query instanceof IdxJoin ? (IdxJoin)query : new IdxJoin((AbstractIdxQuery)query);
        join.joinWith(other.query);
        query = join;
    }

    /**
     * Checks if the {@link IdxQuery} can be used to take over the ordering provided by the {@link Order} expression.
     * If so, the {@link Order} is replaced with its argument {@link TupleExpr}, and the {@link IdxQuery} is wrapped
     * in an {@link IdxOrder} that provides the ordering via the {@link Indexer}.
     */
    public void orderBy(Order order) {
        boolean isCompatible = true;
        for (Iterator<OrderElem> orderElemI = order.getElements().iterator(); orderElemI.hasNext() && isCompatible;) {
            OrderElem orderElem = orderElemI.next();
            if (!(orderElem.getExpr() instanceof Var)) {
                isCompatible = false;
                break;
            }
            Var var = (Var)orderElem.getExpr();
            if (!getBindingNames().contains(var.getName())) {
                isCompatible = false;
                break;
            }
        }
        if (isCompatible) {
            query = new IdxOrder(query, order.getElements());
            order.replaceWith(order.getArg());
        }
    }

    public boolean isOrdered() {
        return query instanceof IdxOrder;
    }
}
