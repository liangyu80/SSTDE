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

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

/**
 * This iterator will for each iteration result join combine the bindings of the result with the bindings of the provided
 * singleton. If the singleton is empty or null, it will behave exactly the same as the wrapped iterator would.
 */
public class JoinWithSingletonIteration implements CloseableIteration<BindingSet, QueryEvaluationException> {
    private CloseableIteration<BindingSet, QueryEvaluationException> iter;
    private BindingSet singleton;

    public JoinWithSingletonIteration(CloseableIteration<BindingSet, QueryEvaluationException> iter, BindingSet singleton) {
        this.iter = iter;
        this.singleton = singleton == null || singleton.size() == 0 ? null : singleton;
    }

    @Override public boolean hasNext() throws QueryEvaluationException {
        return iter.hasNext();
    }

    @Override public BindingSet next() throws QueryEvaluationException {
        if (singleton == null)
            return iter.next();
        else {
            QueryBindingSet result = new QueryBindingSet(singleton);
            result.addAll(iter.next());
            return result;
        }
    }

    @Override public void remove() throws QueryEvaluationException {
        iter.remove();
    }

    @Override public void close() throws QueryEvaluationException {
        iter.close();
    }
}
