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
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.openrdf.query.algebra.OrderElem;
import org.openrdf.query.algebra.Var;

/**
 * Adds ORDER BY semantics to a wrapped {@link IdxQuery}.
 */
public class IdxOrder implements IdxQuery {
    private final IdxQuery query;
    private final List<OrderInfo> orderElems;

    public IdxOrder(IdxQuery query, List<OrderElem> orderElems) {
        this.query = query;
        this.orderElems = new ArrayList<OrderInfo>(orderElems.size());
        for (OrderElem elem: orderElems) {
            Validate.isTrue(elem.getExpr() instanceof Var);
            String var = ((Var)elem.getExpr()).getName();
            Validate.isTrue(query.getAllResultBindings().contains(var));
            this.orderElems.add(new OrderInfo(new IdxVar(var), !elem.isAscending()));
        }
    }

    public IdxQuery getQuery() {
        return query;
    }

    public List<OrderInfo> getOrderElems() {
        return orderElems;
    }

    @Override
    public Set<String> getAllResultBindings() {
        return query.getAllResultBindings();
    }

    public static final class OrderInfo {
        private final IdxExpr expr;
        private final boolean desc;

        private OrderInfo(IdxExpr expr, boolean desc) {
            this.expr = expr;
            this.desc = desc;
        }

        public IdxExpr getExpr() {
            return expr;
        }

        public boolean isDesc() {
            return desc;
        }
    }
}
