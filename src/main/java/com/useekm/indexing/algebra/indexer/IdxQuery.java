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

import java.util.Set;

import com.useekm.indexing.internal.Indexer;

import edu.ncsa.sstde.indexing.algebra.IndexerExpr;

/**
 * Interface for an {@link Indexer} based search/query.
 * 
 * @see IndexerExpr
 */
public interface IdxQuery {
    /**
     * Returns the names of all variables that are bound by the result set of this query, and not already bound in the set of input bindings.
     */
    Set<String> getAllResultBindings();
}
