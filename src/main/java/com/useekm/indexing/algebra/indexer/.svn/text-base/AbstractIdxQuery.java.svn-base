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
package com.useekm.indexing.algebra.indexer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.query.algebra.Var;

/**
 * Abstract base class for singular {@link IdxQuery} implementations (i.e. unjoined, unprojected).
 */
public abstract class AbstractIdxQuery implements IdxQuery {
    private final Var subjectVar;
    private final Var predicateVar;
    private final Var objectVar;

    private final Collection<Var> vars;
    private final HashMap<String, String> nameMaps;

    protected AbstractIdxQuery(Var subjectVar, Var predicateVar, Var objectVar) {
        this.subjectVar = subjectVar;
        this.predicateVar = predicateVar;
        this.objectVar = objectVar;
        this.vars = null;
        this.nameMaps = null;
    }


    protected AbstractIdxQuery(Collection<Var> vars, HashMap<String, String> namemaps) {
        this.subjectVar = null;
        this.predicateVar = null;
        this.objectVar = null;
        this.nameMaps = namemaps;
        this.vars = vars;
    }

    public Collection<Var> getVars() {
        return vars;
    }

    public HashMap<String, String> getNameMaps() {
        return nameMaps;
    }

    /**
     * The subject variable used for or resolved by this {@link IdxQuery}.
     * If it has a value it is an input restrictions. If it has no value yet, it is an output/result.
     */
    public Var getSubjectVar() {
        return subjectVar;
    }

    /**
     * The predicate variable used for or resolved by this {@link IdxQuery}.
     * If it has a value it is an input restrictions. If it has no value yet, it is an output/result.
     */
    public Var getPredicateVar() {
        return predicateVar;
    }

    /**
     * The object variable used for or resolved by this {@link IdxQuery}.
     * If it has a value it is an input restrictions. If it has no value yet, it is an output/result.
     */
    public Var getObjectVar() {
        return objectVar;
    }

    /**
     * @return a limiting list of uri's that define the set of allowed predicates for this {@link IdxQuery}.
     *         If there are no restrictions, an empty list will be returned. Thus empty list indicates match-all.
     */
    @SuppressWarnings("unchecked")//EMPTY_LIST
    public Collection<URI> getPredicateRestrictions() {
        if (getPredicateVar().hasValue())
            return Collections.singleton((URI)getPredicateVar().getValue());
        return Collections.EMPTY_LIST;
    }

    /**
     * {@inheritDoc}
     */
    @Override public Set<String> getAllResultBindings() {
        Set<String> result = new HashSet<String>();

        for (Var var: vars) {
            result.add(var.getName());
        }
        //        if (!subjectVar.hasValue())
        //            result.add(subjectVar.getName());
        //        if (!predicateVar.hasValue())
        //            result.add(predicateVar.getName());
        //        if (!objectVar.hasValue())
        //            result.add(objectVar.getName());
        return result;
    }
}
