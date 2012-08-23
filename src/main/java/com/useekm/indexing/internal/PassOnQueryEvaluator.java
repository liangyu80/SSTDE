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
 * A {@link QueryEvaluator} that simply defers query evaluation to a wrapped {@link SailConnection} that is known to understand the special elements that are put into the
 * {@link TupleExpr} by on of our Sail Wrappers. This is used when e.g. an {@link IndexingSail} is wrapped inside a {@link PipelineSail}. Or when Sesame native stores are wrapped.
 */
public class PassOnQueryEvaluator extends AbstractQueryEvaluator {
    public static final PassOnQueryEvaluator INSTANCE = new PassOnQueryEvaluator();

    protected PassOnQueryEvaluator() {}

    @Override public CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluate(SailConnection conn, ValueFactory vf, Dataset dataset, boolean includeInferred,
        QueryRoot expr, BindingSet bindings) throws SailException {
        return conn.evaluate(expr, dataset, bindings, includeInferred);
    }

    @Override protected boolean needsCustomEvaluation(TupleExpr expr) {
        return false;
    }
}
