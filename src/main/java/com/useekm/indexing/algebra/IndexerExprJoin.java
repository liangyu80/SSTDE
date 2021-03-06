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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;

import edu.ncsa.sstde.indexing.algebra.IndexerExpr;

/**
 * Combines multiple {@link IndexerExpr} instances into one {@link TupleExpr} if they are part of the same {@link Join} (taking nested joins into account).
 */
public class IndexerExprJoin implements QueryOptimizer {
    /**
     * Combines multiple {@link IndexerExpr} instances into one {@link TupleExpr} if they are part of the same {@link Join} (taking nested joins into account). {@link IndexerExpr}
     * instances that are joined into another {@link IndexerExpr} are replaced by a {@link SingletonSet}.
     */
    public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
        final Map<Join, Collection<IndexerExpr>> joinedExpressions = findJoinedExpressions(tupleExpr);

        for (Collection<IndexerExpr> expressionsCol: joinedExpressions.values()) {
            Iterator<IndexerExpr> expressions = expressionsCol.iterator();
            IndexerExpr first = expressions.next();
            while (expressions.hasNext()) {
                IndexerExpr next = expressions.next();
                first.joinWith(next);
                next.replaceWith(new SingletonSet());
            }
        }
    }

    private Map<Join, Collection<IndexerExpr>> findJoinedExpressions(TupleExpr tupleExpr) {
        final Map<Join, Collection<IndexerExpr>> joinedExpressions = new HashMap<Join, Collection<IndexerExpr>>();
        AbstractTopJoinFinder visitor = new AbstractTopJoinFinder() {
            @Override
            protected void action(Join parentJoin, IndexerExpr expr) {
                Collection<IndexerExpr> expressions = joinedExpressions.get(parentJoin);
                if (expressions == null) {
                    expressions = new ArrayList<IndexerExpr>();
                    joinedExpressions.put(parentJoin, expressions);
                }
                expressions.add(expr);
            }
        };
        tupleExpr.visit(visitor);
        return joinedExpressions;
    }
}
