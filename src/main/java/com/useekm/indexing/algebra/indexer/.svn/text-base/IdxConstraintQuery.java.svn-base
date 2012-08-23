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

import org.apache.commons.lang.Validate;
import org.openrdf.model.URI;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.StatementPattern;

/**
 * Query for objects that are related to one of the variables of another {@link AbstractIdxQuery} in the same (potentially nested) {@link Join}.
 */
public class IdxConstraintQuery extends AbstractIdxQuery {
    /**
     * @param constraintPattern The joined statement pattern. At least one of the variables should be equal to another {@link IdxQuery} in the same {@link IdxJoin}.
     */
    public IdxConstraintQuery(StatementPattern constraintPattern) {
        super(constraintPattern.getSubjectVar(), constraintPattern.getPredicateVar(), constraintPattern.getObjectVar());
        Validate.isTrue(constraintPattern.getPredicateVar().getValue() instanceof URI);
        Validate.notEmpty(getSubjectVar().getName());
    }
}