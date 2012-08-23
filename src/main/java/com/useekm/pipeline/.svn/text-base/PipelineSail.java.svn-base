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
package com.useekm.pipeline;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailWrapper;

import com.useekm.indexing.internal.QueryEvaluator;
import com.useekm.indexing.internal.QueryEvaluatorUtil;

/**
 * The PipelineSail writes changes to a delta-model instead of to the underlying (wrapped) sail.
 * Thus added statements are not actually added to the underlying sail, but {@link PipelineSailConnection}s behave in all aspects as though
 * they are. The same is true for statement removals.
 * Queries ({@link SailConnection#evaluate(org.openrdf.query.algebra.TupleExpr, org.openrdf.query.Dataset, org.openrdf.query.BindingSet, boolean)}),
 * and methods like {@link SailConnection#size(org.openrdf.model.Resource...)} all behave as for a normal read-write Sail.
 * 
 * @see PipelineSailConnection
 */
public class PipelineSail extends SailWrapper {
    private QueryEvaluator queryEvaluator;

    public PipelineSail(Sail sail) {
        super(sail);
        this.queryEvaluator = QueryEvaluatorUtil.getEvaluator(sail);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Updates are not written to the underlying sail, but are reflected by reads and queries on the {@link PipelineSailConnection}. They can be retrieved by calling
     * {@link PipelineSailConnection#getAdditions()} and {@link PipelineSailConnection#getRemovals()}
     */
    @Override
    public PipelineSailConnection getConnection() throws SailException {
        return new PipelineSailConnection(super.getConnection(), getValueFactory(), queryEvaluator);
    }
}