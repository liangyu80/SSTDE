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
package com.useekm.types;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.Validate;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;

import com.useekm.types.exception.InvalidGeometryException;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;

import edu.ncsa.sstde.indexing.GeoConstants;
import edu.ncsa.sstde.indexing.postgis.PostgisIndexerSettings;

public abstract class AbstractGeo {
    private String value;

    protected AbstractGeo(String value) {
        Validate.notEmpty(value);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public abstract URI getType();

    public abstract Geometry getGeo();

    @Override public int hashCode() {
        return value.hashCode();
    }

    @Override public boolean equals(Object other) {
        if (other instanceof AbstractGeo)
            return value.equals(((AbstractGeo)other).value) && getType().equals(((AbstractGeo)other).getType());
        return false;
    }

    @Override public String toString() {
        return getGeo().toText();
    }

    public static byte[] gunzip(byte[] bytes) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            BufferedInputStream bufis = new BufferedInputStream(new GZIPInputStream(bis));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = bufis.read(buf)) > 0)
                bos.write(buf, 0, len);
            byte[] result = bos.toByteArray();
            bufis.close();
            bos.close();
            return result;
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected IOException on inmemory gunzip", e);
        }
    }

    public static byte[] gzip(byte[] bytes) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            BufferedOutputStream bufos = new BufferedOutputStream(new GZIPOutputStream(bos));
            bufos.write(bytes);
            bufos.close();
            byte[] retval = bos.toByteArray();
            bos.close();
            return retval;
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected IOException on inmemory gzip", e);
        }
    }

    private static final String HEXES = "0123456789ABCDEF";

    public static String asHex(byte[] bytes) {
        final StringBuilder hex = new StringBuilder(2 * bytes.length);
        for (final byte b: bytes)
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        return hex.toString();
    }

    public static Geometry binaryToGeometry(byte[] bytes) {
        WKBReader reader = new WKBReader(PostgisIndexerSettings.DEFAULT_GEOM_FACTORY);
        try {
            return reader.read(bytes);
        } catch (ParseException e) {
            throw new InvalidGeometryException("Invalid geo WKB: " + asHex(bytes), e);
        }
    }

    public static Geometry wktToGeometry(String value) {
        WKTReader reader = new WKTReader(PostgisIndexerSettings.DEFAULT_GEOM_FACTORY);
        try {
            return reader.read(value);
        } catch (ParseException e) {
            throw new InvalidGeometryException("Invalid geo WKT: " + value, e);
        }
    }

    public static Geometry asGeometry(Literal literal, boolean acceptNoType) throws ValueExprEvaluationException {
        URI type = literal.getDatatype();
        try {
            if (GeoConstants.XMLSCHEMA_SPATIAL_BIN.equals(type))
                return binaryToGeometry(Base64.decodeBase64(literal.stringValue().getBytes()));
            else if (GeoConstants.XMLSCHEMA_SPATIAL_BINGZ.equals(type))
                return binaryToGeometry(AbstractGeo.gunzip(Base64.decodeBase64(literal.stringValue().getBytes())));
            else if (GeoConstants.XMLSCHEMA_SPATIAL_TEXT.equals(type) || (type == null && acceptNoType))
                return wktToGeometry(literal.stringValue());
            else if (GeoConstants.XMLSCHEMA_SPATIAL_TEXTGZ.equals(type))
                return wktToGeometry(new String(AbstractGeo.gunzip(Base64.decodeBase64(literal.stringValue().getBytes()))));
        } catch (InvalidGeometryException e) {
            throw new ValueExprEvaluationException(e.getMessage(), e);
        }
        throw new ValueExprEvaluationException("Not a valid geometry: " + literal.toString());
    }
}
