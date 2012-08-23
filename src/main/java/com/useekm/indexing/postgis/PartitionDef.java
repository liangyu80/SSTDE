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
package com.useekm.indexing.postgis;

import org.apache.commons.lang.Validate;

/**
 * Partitions can be used to partition the indexed data in different tables.
 */
public class PartitionDef {
    private String name;
    private String[] predicates;
    private String[] languagePartitions;

    /**
     * @see #setName(String)
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name should match the regexp [A-Za-z]*
     */
    public void setName(String name) {
        Validate.isTrue(name != null && name.matches("[A-Za-z][A-Za-z0-9_]*"));
        this.name = name;
    }

    /**
     * @see #setPredicates(String)
     */
    public String[] getPredicates() {
        return predicates;
    }

    /**
     * @param predicates The predicates for which this partition should be used.
     *        All of the predicates given here will be put in the same partition,
     *        unless the partition is further partitioned by languages.
     */
    public void setPredicates(String[] predicates) {
        Validate.notNull(predicates);
        Validate.notEmpty(predicates);
        this.predicates = predicates;
    }

    /**
     * @see #setLanguages(String[])
     */
    public String[] getLanguagePartitions() {
        return languagePartitions;
    }

    /**
     * @param languages For each of the given languages a sub-partition will be created.
     *        Thus when languages 'en' and 'nl' are given, three partitions will be created
     *        for this PartitionDef: one for the language 'en', one for 'nl', and one for all other
     *        languages. Only statements with matching predicates will be used for any of these
     *        three partitions.
     */
    public void setLanguagePartitions(String[] languages) {
        this.languagePartitions = languages;
    }
}
