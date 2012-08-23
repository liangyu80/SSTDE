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
package com.useekm.indexing.algebra.indexer;

import java.util.Collection;

import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;


import com.useekm.indexing.internal.Indexer;

import edu.ncsa.sstde.indexing.IndexingSailConnection;
import edu.ncsa.sstde.indexing.algebra.IndexerExpr;

/**
 * A query against the {@link Indexer} index, extracted from a query on the Triple Store (a {@link TupleExpr}).
 * Instances of this class are created by an {@link IndexingSailConnection} when a query is evaluated.
 * 
 * @see QueryExtractor
 * @see IndexerExpr
 */
public class IdxMatchQuery extends AbstractIdxQuery {
    private final IdxExpr expression;

    /**
     * @param subjVar The variable (the subject side of an extension query pattern) to which results will be bound.
     *        May have a value already, in which case the query should filter that value based on availability in the {@link Indexer}.
     * @param predVar The variable (the predicate of an extension query pattern) to which results will be bound.
     *        May have a value already, in which case the query should filter that value based on availability in the {@link Indexer}.
     * @param objVar The variable (the object side of an extension query pattern) to which results will be bound.
     *        May have a value already, in which case the query should filter that value based on availability in the {@link Indexer} and validity with resppect to the provided
     *        expression.
     * @param expression The expression used for the search in the index.
     */
    public IdxMatchQuery(Var subjVar, Var predVar, Var objVar, IdxExpr expression) {
        super(subjVar, predVar, objVar);
        //        Validate.isTrue(subjVar.getName() == null || subjVar.getName().length() > 0);
        //        Validate.isTrue(predVar.getName() == null || predVar.getName().length() > 0);
        //        Validate.isTrue(objVar.getName() == null || objVar.getName().length() > 0);
        //        Validate.isTrue(!predVar.hasValue() || predVar.getValue() instanceof URI);
        //        Validate.isTrue(!objVar.hasValue() || objVar.getValue() instanceof Literal);
        this.expression = expression;
    }

    public IdxMatchQuery(Collection<Var> vars, IdxExpr expression) {
        super(vars, null);
        this.expression = expression;
    }
    /**
     * @return The search expression for this query on the index.
     */
    public IdxExpr getExpression() {
        return expression;
    }
}
