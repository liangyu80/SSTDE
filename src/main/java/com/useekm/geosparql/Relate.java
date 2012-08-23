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

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

import com.vividsolutions.jts.geom.Geometry;

/**
 * <p>
 * Tests whether the DE-9IM IntersectionMatrix for two geometries matches a given pattern.
 * <p>
 * This method doesn't use any indexes, because some relationships are anti e.g. disjoint (see <a
 * href="http://postgis.refractions.net/docs/ST_Relate.html">http://postgis.refractions.net/docs/ST_Relate.html</a>).
 * 
 * @see Geometry#relate(Geometry, String)
 */
public class Relate extends AbstractBinaryFunction {
    public static final String NAME = "relate";

    @Override protected String getName() {
        return NAME;
    }

    /**
     * @return a boolean Literal that is true if geom1 contains properly geom2.
     * @throws ValueExprEvaluationException
     */
    @Override protected Literal evaluate(ValueFactory valueFactory, Geometry geom1, Geometry geom2, Value... originals) throws ValueExprEvaluationException {
        if (originals.length >= 3) {
            String pattern = originals[2].stringValue();
            try {
                return valueFactory.createLiteral(geom1.relate(geom2, pattern));
            } catch (IllegalArgumentException e) {
                throw new ValueExprEvaluationException(e);
            }
        } else
            throw new ValueExprEvaluationException(getName() + " function expects 3+ arguments, found " + originals.length);
    }
}