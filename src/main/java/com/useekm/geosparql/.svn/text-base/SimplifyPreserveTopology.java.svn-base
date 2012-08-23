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

import com.useekm.types.GeoWkt;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.TopologyPreservingSimplifier;

/**
 * Computes a simplified version of the given geometry using the Douglas-Peuker algorithm.
 * Will avoid creating derived geometries (polygons in particular) that are invalid.
 * 
 * @see TopologyPreservingSimplifier
 */
public class SimplifyPreserveTopology extends AbstractGeomDoubleFunction {
    @Override protected String getName() {
        return "simplifyPreserveTopology";
    }

    @Override protected Value evaluate(ValueFactory valueFactory, Geometry geom, double distanceTolerance) {
        GeoWkt wkt = new GeoWkt(TopologyPreservingSimplifier.simplify(geom, distanceTolerance));
        return valueFactory.createLiteral(wkt.getValue(), wkt.getType());
    }
}