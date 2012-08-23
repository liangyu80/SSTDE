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
import info.aduna.iteration.ExceptionConvertingIteration;

import org.apache.commons.lang.Validate;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * A {@link TripleSource} for custom query evaluation that makes no assumptions (this can't be used for native query optimizations/planning) about the underlying store.
 */
public class GenericTripleSource implements TripleSource {
    private final SailConnection conn;
    private final ValueFactory valueFactory;
    private final boolean includeInferred;

    public GenericTripleSource(SailConnection conn, ValueFactory vf, boolean includeInferred) {
        this.conn = conn;
        this.valueFactory = vf;
        this.includeInferred = includeInferred;
    }

    @Override public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(Resource subj, URI pred, Value obj, Resource... contexts)
        throws QueryEvaluationException {
        try {
            return new ExceptionConvertingIteration<Statement, QueryEvaluationException>(conn.getStatements(subj, pred, obj, includeInferred, contexts)) {
                @Override protected QueryEvaluationException convert(Exception e) {
                    if (e instanceof SailException) {
                        return new QueryEvaluationException(e);
                    } else {
                        Validate.isTrue(e instanceof RuntimeException);
                        throw (RuntimeException)e;
                    }
                }
            };
        } catch (SailException e) {
            throw new QueryEvaluationException(e);
        }
    }

    @Override public ValueFactory getValueFactory() {
        return valueFactory;
    }
}