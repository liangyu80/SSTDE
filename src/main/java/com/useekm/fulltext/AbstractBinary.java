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
package com.useekm.fulltext;

import org.apache.commons.lang.Validate;

/**
 * Binary full text search operator (& for AND, | for Or).
 * 
 * @see And
 * @see Or
 */
public abstract class AbstractBinary implements Binary {
    private FulltextSearch lhs;
    private FulltextSearch rhs;

    protected AbstractBinary(FulltextSearch lhs, FulltextSearch rhs) {
        Validate.notNull(lhs);
        Validate.notNull(rhs);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override public FulltextSearch getLhs() {
        return lhs;
    }

    @Override public FulltextSearch getRhs() {
        return rhs;
    }
}