/*
 * Copyright 2010 by TalkingTrends (Amsterdam, The Netherlands)
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.Validate;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.object.managers.Marshall;

import edu.ncsa.sstde.indexing.GeoConstants;

public final class GeometryMarshall<T extends AbstractGeo> implements Marshall<T> {
    private ValueFactory vf;
    private URI datatype;
    private Constructor<T> constructor;

    public GeometryMarshall(Class<T> clazz, ValueFactory vf) {
        if (clazz.equals(GeoWkt.class))
            this.datatype = GeoConstants.XMLSCHEMA_SPATIAL_TEXT;
        else if (clazz.equals(GeoWkb.class))
            this.datatype = GeoConstants.XMLSCHEMA_SPATIAL_BIN;
        else if (clazz.equals(GeoWktGz.class))
            this.datatype = GeoConstants.XMLSCHEMA_SPATIAL_TEXTGZ;
        else {
            Validate.isTrue(clazz.equals(GeoWkbGz.class));
            this.datatype = GeoConstants.XMLSCHEMA_SPATIAL_BINGZ;
        }
        this.vf = vf;
        try {
            this.constructor = clazz.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override public String getJavaClassName() {
        return constructor.getDeclaringClass().getName();
    }

    @Override public URI getDatatype() {
        return datatype;
    }

    @Override public void setDatatype(URI datatype) {
        Validate.isTrue(this.datatype.equals(datatype));
    }

    @Override public T deserialize(Literal literal) {
        try {
            return constructor.newInstance(literal.stringValue());
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override public Literal serialize(AbstractGeo geo) {
        return vf.createLiteral(geo.getValue(), geo.getType());
    }
}
