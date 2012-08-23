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

import java.util.HashSet;
import java.util.Set;

import org.openrdf.query.algebra.TupleExpr;

import edu.ncsa.sstde.indexing.algebra.IndexerExpr;

/**
 * A {@link QueryEvaluator} that makes no assumptions about supported features for query evaluation. It will do a custom query evaluation by using only standard
 * SparQL/SerQL query elements.
 * This may be slow for some types of queries, since it is not possible to do native query planning for queries that use our specific {@link TupleExpr} extensions.
 * Note that queries that do not use such extensions are always evaluated completely by the underlying Sail and therefore will not be affected.
 */
public class DefaultQueryEvaluator extends AbstractQueryEvaluator {
    private static final Set<Class<? extends TupleExpr>> EXPRESSIONS_NEEDING_CUSTOM_EVAL = new HashSet<Class<? extends TupleExpr>>();
    static {
        EXPRESSIONS_NEEDING_CUSTOM_EVAL.add(IndexerExpr.class);
        try {
            @SuppressWarnings("unchecked") Class<? extends TupleExpr> em = (Class<? extends TupleExpr>)Class.forName("org.openrdf.sail.optimistic.helpers.ExternalModel");
            EXPRESSIONS_NEEDING_CUSTOM_EVAL.add(em);
        } catch (ClassNotFoundException e) {
            //just ignore, if not an classpath we don't care it can't be evaluated
        }
    }
    public static final DefaultQueryEvaluator INSTANCE = new DefaultQueryEvaluator();

    protected DefaultQueryEvaluator() {
        super(EXPRESSIONS_NEEDING_CUSTOM_EVAL);
    }
}
