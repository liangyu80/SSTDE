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
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import edu.ncsa.sstde.indexing.postgis.PostgisIndexerSettings;

/**
 * Computes the shortest line (LineString) between two geometries.
 */
public class ShortestLine extends AbstractBinaryFunction {
    @Override protected String getName() {
        return "shortestLine";
    }

    @Override protected Value evaluate(ValueFactory valueFactory, Geometry geom1, Geometry geom2, Value... originals) {
        DistanceOp distOp = new DistanceOp(geom1, geom2);
        LineString result = PostgisIndexerSettings.DEFAULT_GEOM_FACTORY.createLineString(distOp.nearestPoints());
        GeoWkt wkt = new GeoWkt(result);
        return valueFactory.createLiteral(wkt.getValue(), wkt.getType());
    }
}