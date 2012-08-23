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
import java.util.List;

public class IdxExprAnd extends AbstractIdxExpr {
    private final IdxExpr lhExpr;
    private final IdxExpr rhExpr;

    public IdxExprAnd(IdxExpr left, IdxExpr right) {
        this.lhExpr = left;
        this.rhExpr = right;
    }

    public IdxExpr getLeft() {
        return lhExpr;
    }

    public IdxExpr getRight() {
        return rhExpr;
    }

    @Override
    public List<IdxExpr> getChildren() {
        ArrayList<IdxExpr> result = new ArrayList<IdxExpr>(2);
        result.add(lhExpr);
        result.add(rhExpr);
        return result;
    }
}
