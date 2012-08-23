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

import org.openrdf.model.ValueFactory;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.sail.SailConnection;

import edu.ncsa.sstde.indexing.algebra.IndexerExpr;

/**
 * {@link QueryEvaluator} implementation for Bigdata.
 * It will use a {@link BigdataEvaluationStrategy} that takes advantage of some Bigdata specific query capabilities.
 * 
 * @see BigdataEvaluationStrategy
 */
public class BigdataQueryEvaluator extends AbstractQueryEvaluator {
    //Expressions that need to have a custom evaluation (query subtrees without these will be passed on to the SailConnection.evaluate directly):
    private static final Set<Class<? extends TupleExpr>> EXPRESSIONS_NEEDING_CUSTOM_EVAL = new HashSet<Class<? extends TupleExpr>>();
    //Expressions that prevent the use of the streaming BigdataSailConnection.evaluate (query subtrees that need a custom evaluate but contain any of these
    // will be evaluated with a non-native EvaluationStrategy):
    static final Set<Class<? extends TupleExpr>> NON_NATIVE_NON_INLINE = new HashSet<Class<? extends TupleExpr>>();
    static {
        try {
            @SuppressWarnings("unchecked") Class<? extends TupleExpr> em = (Class<? extends TupleExpr>)Class.forName("org.openrdf.sail.optimistic.helpers.ExternalModel");
            EXPRESSIONS_NEEDING_CUSTOM_EVAL.add(em);
            NON_NATIVE_NON_INLINE.add(em); // due to missing locality and statement patterns, ExternalModel evaluation will in many cases not work with streaming-evaluate
        } catch (ClassNotFoundException e) {
            //just ignore, if not an classpath we don't care it can't be evaluated
        }
        NON_NATIVE_NON_INLINE.add(Union.class); //See IndexingSailConnectionTest.queryUnion; bigdata streaming evaluation doesn't work properly for Union's at least in some cases
        NON_NATIVE_NON_INLINE.add(Slice.class); //TODO: figure out why sliced evaluations don't work properly with streaming evaluate. may need help from Bigdata guys
        EXPRESSIONS_NEEDING_CUSTOM_EVAL.add(IndexerExpr.class);
    }
    public static final BigdataQueryEvaluator INSTANCE = new BigdataQueryEvaluator();

    protected BigdataQueryEvaluator() {
        super(EXPRESSIONS_NEEDING_CUSTOM_EVAL);
    }

    @Override protected EvaluationStrategy getEvaluationStrategy(SailConnection conn, ValueFactory vf, Dataset dataset, boolean includeInferred) {
        return new BigdataEvaluationStrategy(this, conn, vf, dataset, includeInferred);
    }
}