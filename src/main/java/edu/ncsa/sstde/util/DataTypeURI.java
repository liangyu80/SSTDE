
package edu.ncsa.sstde.util;

import java.sql.Types;
import java.util.HashSet;

/**
 * @author liangyu
 *
 *To provide the constants for each RDF data types, as well as method to decide if a type uri falls into a type category. 
 */
public class DataTypeURI {
    public static final String DATETIME= "http://www.w3.org/2001/XMLSchema#dateTime";
    public static final String FLOAT = "http://www.w3.org/2001/XMLSchema#float";
    public static final String DOUBLE = "http://www.w3.org/2001/XMLSchema#double";
    public static final String TIME = "http://www.w3.org/2001/XMLSchema#time";
    public static final String STRING = "http://www.w3.org/2001/XMLSchema#string";
    public static final String INTEGER = "http://www.w3.org/2001/XMLSchema#int";
    public static final String GEOMETRY = "http://rdf.opensahara.com/type/geo/wkt";
    
    private static final HashSet<String> WktLiterals = new HashSet<String>();
    static {
    	WktLiterals.add("http://rdf.opensahara.com/type/geo/wkt");
    	WktLiterals.add("http://www.opengis.net/def/sf/wktLiteral");
    }
    //    public static final String datetime = "http://www.w3.org/2001/XMLSchema#dateTime";

    public static boolean isGeometry(String typeuri) {
        return WktLiterals.contains(typeuri);
    }

    public static boolean isNumeric(String typeuri) {
        return DATETIME.equals(typeuri) || FLOAT.equals(typeuri) || DOUBLE.equals(typeuri) || INTEGER.equals(typeuri) || TIME.equals(typeuri);
    }

    public static boolean isText(String typeuri) {
        return STRING.equals(typeuri);
    }
    
    public static int toSQLType(String uri){
    	if (DATETIME.equals(uri)) {
			return Types.TIMESTAMP;
		}else if (FLOAT.equals(uri)) {
			return Types.FLOAT;
		}else if (DOUBLE.equals(uri)) {
			return Types.DOUBLE;
		}else if (TIME.equals(uri)) {
			return Types.TIME;
		}else if (STRING.equals(uri)) {
			return Types.VARCHAR;
		}else if (INTEGER.equals(uri)) {
			return Types.INTEGER;
		}else if (isGeometry(uri)) {
			return Types.OTHER;
		}
    	
    	return Types.OTHER;
    	
    }

}
