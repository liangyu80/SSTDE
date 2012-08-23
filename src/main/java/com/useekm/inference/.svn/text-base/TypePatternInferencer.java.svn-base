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
package com.useekm.inference;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * @see SimpleTypeInferencingSail
 */
class TypePatternInferencer implements QueryOptimizer {
    @Override public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
        PatternVisitor visitor = new PatternVisitor(bindings);
        tupleExpr.visit(visitor);
        int n = 1;
        for (StatementPattern dtPattern: visitor.directTypePatterns) {
            Var pred = dtPattern.getPredicateVar();
            pred.setName("dpi__priv__" + (n++) + "__");
            pred.setValue(RDF.TYPE);
        }
    }

    private static final class PatternVisitor extends QueryModelVisitorBase<RuntimeException> {
        private BindingSet bindings;
        private List<StatementPattern> directTypePatterns = new ArrayList<StatementPattern>(1);
        private int n = 1;

        private PatternVisitor(BindingSet bindings) {
            this.bindings = bindings;
        }

        @Override
        public void meet(StatementPattern pattern) {
            boolean rewrite = RDF.TYPE.equals(pattern.getPredicateVar().getValue()) ||
                RDF.TYPE.equals(bindings.getValue(pattern.getPredicateVar().getName()));
            if (rewrite)
                rewriteToJoin(pattern);
            else {
                if (SimpleTypeInferencingSail.DIRECT_TYPE.equals(pattern.getPredicateVar().getValue())
                    || SimpleTypeInferencingSail.DIRECT_TYPE.equals(bindings.getValue(pattern.getPredicateVar().getName())))
                    directTypePatterns.add(pattern);
            }
        }

        private void rewriteToJoin(StatementPattern pattern) {
            //?x rdf:type ?y  ->  ?x rdf:type ?type . ?type rdfs:subClassOf ?y
            Var pred = new Var("ppi__priv__" + n + "__", RDFS.SUBCLASSOF);
            Var type = new Var("tpi__priv__" + n + "__");
            ++n;
            StatementPattern dummy = new StatementPattern();
            pattern.replaceWith(dummy);
            Join join = new Join(pattern, new StatementPattern(type, pred, pattern.getObjectVar()));
            pattern.setObjectVar(new Var(type.getName()));
            dummy.replaceWith(join);
        }
    }
}
