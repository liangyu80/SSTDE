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

import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

import edu.ncsa.sstde.indexing.algebra.IndexerExpr;

/**
 * If {@link IndexerExpr} elements are part of a {@link Join} this class can be used to find the topmost {@link Join} in the join-tree.
 * <p>
 * Override {@link #action(Join, IndexerExpr)} to do something with that information.
 */
abstract class AbstractTopJoinFinder extends QueryModelVisitorBase<RuntimeException> {
    @Override
    public void meetNode(QueryModelNode node) {
        if (node instanceof IndexerExpr) {
            IndexerExpr expr = (IndexerExpr)node;
            Join parentJoin = node.getParentNode() instanceof Join ? (Join)node.getParentNode() : null;
            if (parentJoin != null) {
                while (parentJoin.getParentNode() instanceof Join)
                    parentJoin = (Join)parentJoin.getParentNode();
                action(parentJoin, expr);
            }
        } else
            super.meetNode(node);
    }

    /**
     * Will be called for all {@link IndexerExpr} elements that are used directly in a {@link Join} The method is called for the topmost {@link Join} in the join-tree.
     */
    protected abstract void action(Join topJoin, IndexerExpr expr);
}