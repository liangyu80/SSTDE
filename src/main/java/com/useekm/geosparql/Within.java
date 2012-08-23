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

/**
 * Tests whether the first geometry is within the second geometry. A geometry geom1 is within geometry geom2 if every point of geom1 is a point of geom2, and the interiors of the
 * two geometries have at least one point in common. Effectively, the boundary of a geometry is not within that geometry.
 * 
 * @see Geometry#within(Geometry)
 */
public class Within extends AbstractBinaryFunction {
    public static final String NAME = "within";

    @Override protected String getName() {
        return NAME;
    }

    /**
     * @return a boolean Literal that is true if geom1 is within geom2.
     */
    @Override protected Literal evaluate(ValueFactory valueFactory, Geometry geom1, Geometry geom2, Value... originals) {
        return valueFactory.createLiteral(geom1.within(geom2));
    }
}
