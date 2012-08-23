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

import org.openrdf.model.URI;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

import com.useekm.indexing.algebra.indexer.IdxConstraintQuery;
import com.useekm.indexing.algebra.indexer.IdxQuery;
import com.useekm.indexing.internal.Indexer;

import edu.ncsa.sstde.indexing.algebra.IndexerExpr;

/**
 * Replaces {@link StatementPattern} elements with {@link IdxConstraintQuery} elements when the {@link Indexer}
 * is assumed to speed up the search, and the Indexer has support to restrict the search based on
 * the {@link StatementPattern}, thus:
 * <ul>
 * <li>The indexer indexes statements that match the pattern</li>
 * <li>The indexer supports joins</li>
 * <li>The subject of the statement pattern is also the subject of a search on the index</li>.
 * </ul>
 */
public class ConstraintOptimizer implements QueryOptimizer {
    /**
     * Replaces {@link StatementPattern} elements with {@link IdxConstraintQuery} when they can be used as extra constraints on existing
     * {@link IdxQuery} elements.
     */
    public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
        AbstractTopJoinFinder topJoins = new AbstractTopJoinFinder() {
            @Override
            protected void action(Join parentJoin, final IndexerExpr expr) {
                JoinedPatternVisitor patternVisitor = new JoinedPatternVisitor(expr);
                parentJoin.visit(patternVisitor);
            }
        };
        tupleExpr.visit(topJoins);
    }

    private static class JoinedPatternVisitor extends QueryModelVisitorBase<RuntimeException> {
        private final IndexerExpr expr;

        public JoinedPatternVisitor(IndexerExpr expr) {
            this.expr = expr;
        }

        @Override
        public void meet(Join join) {
            addConstraint(join.getLeftArg());
            addConstraint(join.getRightArg());
            super.meet(join);
        }

        private void addConstraint(TupleExpr tupleExpr) {
            if (tupleExpr instanceof StatementPattern) {
                StatementPattern pattern = (StatementPattern)tupleExpr;
                //TODO we only want to match when the subject is a matchVar (i.e. the subject of another IdxMatchQueries)
                //needs more conditions (these are copied from the old QueryExctractor, may not apply anymore):
                // - subjectvar should not equal objectvar
                // - multiple constraints bind to the same variable, because this will make joins to complex.
                // - Constraints bind to variables that are the subject of another {@link IndexQuery} in this {@link TupleExpr}
                // - Constraints bind to variables in other constraints for an {@link IndexQuery} found in this {@link TupleExpr}. TODO
                // - Constraints bind to variables that are bound as the object part of options in any of the extracted {@link IndexQuery}s. TODO
                if (pattern.getSubjectVar().getName() != null
                    && pattern.getPredicateVar().getValue() instanceof URI
                    && expr.getIndexer().getSettings().isPredicateIndexed((URI)pattern.getPredicateVar().getValue())
                    && expr.getBindingNames().contains(pattern.getSubjectVar().getName()))
                	System.out.println();
//                    pattern.replaceWith(new IndexerExpr(new IdxConstraintQuery(pattern), expr.getIndexer(), expr.getValueFactory()));
            }
        }
    }
}
