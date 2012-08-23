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

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.Var;

/**
 * Function call expression.
 */
public class IdxExprFunction extends AbstractIdxExpr {
    private final URI function;
    private final Value[] arguments;
    private final int resultPosition;
    private Var var;

    public Var getVar() {
        return var;
    }

    public void setVar(Var var) {
        this.var = var;
    }

    public IdxExprFunction(Var var, URI function, int resultPosition, Value... arguments) {
        this.var = var;
        this.function = function;
        this.arguments = arguments;
        this.resultPosition = resultPosition;
    }

    public URI getFunction() {
        return function;
    }

    public Value[] getArguments() {
        return arguments;
    }

    public int getResultArgPosition() {
        return resultPosition;
    }

    @Override
    public String getSignature() {
        StringBuffer result = new StringBuffer(super.getSignature());
        result.append(" ").append(function.stringValue()).append("(").append(resultPosition);
        for (Value arg: arguments) {
            result.append(", ");
            result.append(arg);
        }
        result.append(")");
        return result.toString();
    }
}
