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
package com.useekm.indexing.postgis;

import java.util.Locale;

import org.apache.commons.lang.Validate;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * Settings for which statements/triples are indexed by a {@link PostgisIndexMatcher}, and
 * which type of index will be used.
 */
public class PostgisIndexMatcher {
    public static final String NULL = "null";

    private String predicate;
    private String type;
    private String language;
    private String searchConfig;

    /**
     * Indexing for statements will be done if the predicate of the statement literally matches
     * the returned value.
     * If the value is null, it will match any statement predicate.
     */
    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    String getPredicate() {
        return predicate;
    }

    /**
     * Indexing for statements where the object is a {@link Literal} will be done when the type matches
     * the returned value.
     * If the value is null it will match any literal. If the value is the string "null" it will
     * match only when {@link Literal#getDatatype()} is null.
     * <p>
     * Note that when the type is set, this matcher will only be used for indexing Literal objects (not for URI's).
     */
    public void setType(String type) {
        this.type = type;
        validateTypeLanguage();
    }

    String getType() {
        return type;
    }

    /**
     * Indexing for statements where the object is a {@link Literal} will be done when the language matches
     * the returned value.
     * If the language is null it will match any literal. If the value is the string "NULL" it will
     * match only when {@link Literal#getLanguage()} is null.
     * <p>
     * Note that when the language is set, this matcher will only be used for indexing Literal objects (not for URI's).
     */
    public void setLanguage(String language) {
        this.language = language == null ? null : language.toLowerCase(Locale.ROOT);
        validateTypeLanguage();
    }

    String getLanguage() {
        return language;
    }

    /**
     * @see #setSearchConfig(String)
     */
    public String getSearchConfig() {
        return searchConfig;
    }

    /**
     * @param searchConfig The configuration to use when creating ts_vector and ts_query objects for storing and
     *        searching based on full text indexes.
     *        See <a href="http://www.postgresql.org/docs/8.3/static/textsearch.html">Postgres Text Search documentation</a>.
     *        If null, no search-index will be build, and full text search extensions will not be available for statements
     *        matching this {@link PostgisIndexMatcher}.
     */
    public void setSearchConfig(String searchConfig) {
        this.searchConfig = searchConfig;
    }

    private void validateTypeLanguage() {
        if (language != null && !NULL.equals(language) && type != null && !NULL.equals(type))
            throw new IllegalArgumentException("Combination of language=" + language + " and type=" + type + " is invalid");
    }

    /**
     * @return true if an add with these parameters needs an update of the index for this PostgisIndexMatcher.
     *         The parameters subj, pred and obj may not be null (as that does not make a valid statement).
     */
    public boolean matchesAdd(Resource subj, URI pred, Value obj, Resource... ctx) {
        //        boolean subjMatches = subj instanceof URI;
        boolean subjMatches = true;
        boolean predMatches = predicate == null || predicate.equals(pred.stringValue());
        return subjMatches && predMatches && matchesObject(obj);
    }

    /**
     * @return false if a delete with these parameters is guaranteed to not affect the index for this PostgisIndexMatcher.
     *         (I.e. none of the indexed statements will match this statement pattern). The parameters subj, pred, and obj may be null
     *         indicating that statements will be removed for any such subj, pred, or obj.
     */
    public boolean matchesDelete(Resource subj, URI pred, Value obj, Resource... ctx) {
        boolean subjMatches = subj == null || subj instanceof URI;
        boolean predMatches = predicate == null || pred == null || predicate.equals(pred.stringValue());
        return subjMatches && predMatches && (obj == null || matchesObject(obj));
    }

    public boolean includes(PostgisIndexMatcher other) {
        boolean includesPred = this.predicate == null || this.predicate.equals(other.predicate);
        boolean includesType = this.type == null || this.type.equals(other.type);
        boolean includesLang = this.language == null || this.language.equalsIgnoreCase(other.language);
        return includesPred && includesType && includesLang;
    }

    private boolean matchesObject(Value obj) {
        boolean typeMatches = type == null || (obj instanceof Literal && matches(type, ((Literal)obj).getDatatype()));
        boolean langMatches = language == null || (obj instanceof Literal && matches(language, ((Literal)obj).getLanguage(), true));
        return typeMatches && langMatches;
    }

    private static boolean matches(String toMatch, URI datatype) {
        return matches(toMatch, datatype == null ? null : datatype.stringValue(), false);
    }

    private static boolean matches(String toMatch, String matched, boolean normalize) {
        Validate.notNull(toMatch);
        if (NULL.equals(toMatch)) // only matches null
            return matched == null;
        else if (normalize)
            return matched != null && matched.toLowerCase(Locale.ROOT).equals(toMatch);
        return matched != null && matched.equals(toMatch);
    }
}
