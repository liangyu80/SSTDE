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
package com.useekm.geosparql;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractGeomDoubleFunction extends AbstractFunction {
    protected abstract Value evaluate(ValueFactory valueFactory, Geometry geom, double value);

    @Override public Value evaluate(ValueFactory valueFactory, Value... args) throws ValueExprEvaluationException {
        if (args.length != 2)
            throw new ValueExprEvaluationException(getName() + " function expects 2 arguments, found " + args.length);
        else {
            Geometry geom = asGeometry(args[0]);
            double value = asDouble(args[1]);
            return evaluate(valueFactory, geom, value);
        }
    }
}