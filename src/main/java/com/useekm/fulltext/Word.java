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
 * Full text search word to search for.
 */
public class Word implements FulltextSearch {
    private String word;

    public Word(String word) {
        this(word, false);
    }

    /**
     * This constructor is called for prefix-search-words such as 'xyz*' (for lookup of words starting with xyz).
     * The grammar makes am operator out of the star, hence it can be removed from the lexical token.
     */
    public Word(String word, boolean removeOperator) {
        Validate.notEmpty(word);
        Validate.isTrue(!removeOperator || (word.length() >= 2 &&word.endsWith("*")));
        if (removeOperator)
            this.word = word.substring(0, word.length() - 1);
        else
            this.word = word;
    }

    public String getWord() {
        return word;
    }
}
