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

import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;

/**
 * 
 */
public class Delta {
    private Model added;
    private Model removed;

    public Delta(Model added, Model removed) {
        this.added = added;
        this.removed = removed;
    }

    /**
     * @return A model of the statements that have been added to the pipeline.
     */
    public Model getAdditions() {
        return added == null ? new LinkedHashModel() : added;
    }

    /**
     * @return A model of the statements that were removed from the pipeline.
     */
    public Model getRemovals() {
        return removed == null ? new LinkedHashModel() : removed;
    }

    public boolean isEmpty() {
        boolean hasRemovals = removed != null && !removed.isEmpty();
        boolean hasAdditions = added != null && !added.isEmpty();
        return !hasRemovals && !hasAdditions;
    }
}
