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

/**
 * Prints a readable tree representation of an {@link IdxExpr} to a {@link StringBuffer}.
 * Used for {@link IdxExpr#toString()} implementations.
 */
final class IdxExprTreePrinter {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String INDENT_STRING = "  ";
    private final StringBuffer buffer = new StringBuffer();
    private int indentLevel;

    public static String printTree(IdxExpr expr) {
        IdxExprTreePrinter printer = new IdxExprTreePrinter();
        printer.print(expr);
        return printer.getString();
    }

    private IdxExprTreePrinter() {
    }

    private String getString() {
        return buffer.toString();
    }

    private void print(IdxExpr expr) {
        for (int i = 0; i < indentLevel; i++)
            buffer.append(INDENT_STRING);
        buffer.append(expr.getSignature());
        buffer.append(LINE_SEPARATOR);
        indentLevel++;
        for (IdxExpr child: expr.getChildren())
            print(child);
        indentLevel--;
    }
}
