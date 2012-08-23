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

/**
 * Exception thrown by implementations of {@link Fulltext} when a query is invalid.
 */
public class FulltextParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String query;

    public FulltextParseException() {
        query = null;
    }

    public FulltextParseException(Throwable cause, String query) {
        super(cause);
        this.query = query;
    }

    @Override public String getMessage() {
        return "Invalid query: " + query;
    }
}