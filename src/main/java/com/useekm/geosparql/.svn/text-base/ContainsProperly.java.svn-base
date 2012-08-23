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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;

/**
 * Tests whether the first geometry properly contains the second geometry. A geometry geom1 contains properly geometry geom2 if geom1 contains (see {@link Contains}) geom2, and the
 * boundaries of both geometries do not intersect. The "contains properly" relation can be computed more efficiently than the "contains" relation.
 * 
 * @see PreparedGeometry#containsProperly(Geometry)
 */
public class ContainsProperly extends AbstractBinaryFunction {
    public static final String NAME = "containsProperly";

    @Override protected String getName() {
        return NAME;
    }

    /**
     * @return a boolean Literal that is true if geom1 contains properly geom2.
     */
    @Override protected Literal evaluate(ValueFactory valueFactory, Geometry geom1, Geometry geom2, Value... allArgs) {
        boolean result;
        if (!geom1.getEnvelopeInternal().contains(geom2.getEnvelopeInternal()))
            result = false;
        else
            result = geom1.relate(geom2, "T**FF*FF*");
        return valueFactory.createLiteral(result);
    }
}
