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

import com.vividsolutions.jts.algorithm.match.HausdorffSimilarityMeasure;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Measures the degree of similarity between two geometries using the Hausdorff distance metric. The measure is normalized to lie in the range [0, 1]. Higher measures indicate a
 * great degree of similarity.
 * <p>
 * The measure is computed by computing the Hausdorff distance between the input geometries, and then normalizing this by dividing it by the diagonal distance across the envelope
 * of the combined geometries.
 * 
 * @see HausdorffSimilarityMeasure
 */
public class HausdorffDistance extends AbstractBinaryFunction {
    static private HausdorffSimilarityMeasure hdsm = new HausdorffSimilarityMeasure();

    @Override protected String getName() {
        return "hausdorffDistance";
    }

    @Override protected Value evaluate(ValueFactory valueFactory, Geometry geom1, Geometry geom2, Value... allArgs) {
        return valueFactory.createLiteral(hdsm.measure(geom1, geom2));
    }
}