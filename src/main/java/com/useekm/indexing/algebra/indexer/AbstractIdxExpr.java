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

import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for expressions used in {@link IdxQuery} implementations.
 */
public abstract class AbstractIdxExpr implements IdxExpr {
    @SuppressWarnings("unchecked")
    @Override
    public List<IdxExpr> getChildren() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public String getSignature() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return IdxExprTreePrinter.printTree(this);
    }
}
