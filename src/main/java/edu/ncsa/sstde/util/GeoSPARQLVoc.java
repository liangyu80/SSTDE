package edu.ncsa.sstde.util;

/**
 * @author liangyu
 * 
 *         It is expected to be a vocabulary which can be used to store the
 *         standard URIs defined in GeoSPARQL, and to look up if a URI or
 *         statement meets the standard of GeoSPARQL. Now it is very simple but
 *         can be extended later.
 */
public class GeoSPARQLVoc {

	public static boolean isGeoSPARQLPred(String uri) {
		return uri.startsWith("http://rdf.opensahara.com/search#");
	}
}
