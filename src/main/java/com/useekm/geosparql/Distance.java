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

import com.vividsolutions.jts.geom.Geometry;

/**
 * Computes the minimum distance between two geometries, the distance is 0 if either of the input geometries is empty.
 */
public class Distance extends AbstractBinaryFunction {
    @Override protected String getName() {
        return "distance";
    }

    @Override protected Value evaluate(ValueFactory valueFactory, Geometry geom1, Geometry geom2, Value... allArgs) {
        return valueFactory.createLiteral(geom1.distance(geom2));
    }
}