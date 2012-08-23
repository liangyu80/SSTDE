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

import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

import com.useekm.pipeline.PipelineSail;

import edu.ncsa.sstde.indexing.IndexingSail;

/**
 * Strategy for query evaluation for Open Sahara Sesame extensions {@link IndexingSail} and {@link PipelineSail}.
 * The evaluator optimizes the query (a combination of standard Sesame {@link TupleExpr} elements with Open Sahara specific extensions of {@link TupleExpr}).
 * for the underlying store.
 */
public interface QueryEvaluator {
    /**
     * Evaluates the provided query.
     * @param conn The connection to be used for the evaluation.
     * @param vf The ValueFactory of the Sail or SailConnection
     * @param dataset The dataset to use for evaluating the query, null to use the Sail's default dataset.
     * @param includeInferred Wether inferred statements should be included in the results or not.
     * @param expr The {@link TupleExpr} representing the parsed query.
     * @param bindings The input bindings for the query.
     * 
     * @throws SailException Upon failure.
     */
    CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(SailConnection conn, ValueFactory vf, Dataset dataset, boolean includeInferred, QueryRoot expr,
        BindingSet bindings) throws SailException;
}