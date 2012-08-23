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
package com.useekm.indexing.algebra;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

import com.useekm.indexing.algebra.indexer.IdxOrder;
import com.useekm.indexing.algebra.indexer.IdxQuery;

import edu.ncsa.sstde.indexing.algebra.IndexerExpr;

/**
 * Moves {@link OrderElem} elements from the query on the Triple Store to the query on the index by wrapping a {@link IdxQuery} in a {@link IdxOrder},
 * and replacing the {@link OrderElem} with its {@link OrderElem#getExpr()}.
 */
public class OrderByOptimizer implements QueryOptimizer {
    /**
     * Moves {@link OrderElem} elements from the query on the Triple Store to the {@link IdxQuery}.
     * <p>
     * <strong>Note: that this optimizer may change semantics of the ordering. See <a href="https://dev.opensahara.com/issues/13">#13</a></strong>
     */
    public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
        IndexerExprVisitor visitor = new IndexerExprVisitor();
        tupleExpr.visit(visitor);
    }

    private static class IndexerExprVisitor extends QueryModelVisitorBase<RuntimeException> {
        @Override
        public void meetNode(QueryModelNode node) {
            if (node instanceof IndexerExpr) {
                IndexerExpr expr = (IndexerExpr)node;
                QueryModelNode parent = node.getParentNode();
                while (parent != null) {
                    if (parent instanceof Order) {
                        expr.orderBy((Order)parent);
                        break;
                    }
                    parent = parent.getParentNode();
                }
            } else
                super.meetNode(node);
        }
    }
}
