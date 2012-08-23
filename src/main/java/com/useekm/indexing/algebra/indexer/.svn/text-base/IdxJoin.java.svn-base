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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.useekm.indexing.internal.Indexer;

/**
 * A join of several {@link AbstractIdxQuery} instances, that will result in one query on the {@link Indexer}.
 */
public class IdxJoin implements IdxQuery {
    private final List<AbstractIdxQuery> queries;

    public IdxJoin(AbstractIdxQuery query) {
        this.queries = new ArrayList<AbstractIdxQuery>(1);
        this.queries.add(query);
    }

    public void joinWith(IdxQuery query) {
        if (query instanceof IdxJoin)
            this.queries.addAll(((IdxJoin)query).queries);
        else
            this.queries.add((AbstractIdxQuery)query);
    }

    public List<AbstractIdxQuery> getQueries() {
        return queries;
    }

    /**
     * The list of all binding names that are resolved by the search queries in this IndexerExpr.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAllResultBindings() {
        Set<String> result = new HashSet<String>(queries.size());
        for (IdxQuery join: queries)
            result.addAll(join.getAllResultBindings());
        return result;
    }
}
