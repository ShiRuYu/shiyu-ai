package com.shiyu.ai.composition.database;

import java.util.ArrayList;
import java.util.List;

/** 拆分 PostgreSQL 脚本，同时保留引号包围的过程体内部语句。 */
final class PostgresqlScriptExecutor {

    private PostgresqlScriptExecutor() {}

    static List<String> splitStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder statement = new StringBuilder();
        int index = 0;
        int blockCommentDepth = 0;
        String dollarQuote = null;
        boolean singleQuoted = false;
        boolean escapeString = false;
        boolean doubleQuoted = false;
        boolean lineComment = false;

        while (index < script.length()) {
            char current = script.charAt(index);
            char next = index + 1 < script.length() ? script.charAt(index + 1) : '\0';

            if (lineComment) {
                statement.append(current);
                if (current == '\n') {
                    lineComment = false;
                }
                index++;
                continue;
            }
            if (blockCommentDepth > 0) {
                statement.append(current);
                if (current == '/' && next == '*') {
                    statement.append(next);
                    blockCommentDepth++;
                    index += 2;
                } else if (current == '*' && next == '/') {
                    statement.append(next);
                    blockCommentDepth--;
                    index += 2;
                } else {
                    index++;
                }
                continue;
            }
            if (dollarQuote != null) {
                if (script.startsWith(dollarQuote, index)) {
                    statement.append(dollarQuote);
                    index += dollarQuote.length();
                    dollarQuote = null;
                } else {
                    statement.append(current);
                    index++;
                }
                continue;
            }
            if (singleQuoted) {
                statement.append(current);
                if (escapeString && current == '\\' && next != '\0') {
                    statement.append(next);
                    index += 2;
                } else if (current == '\'' && next == '\'') {
                    statement.append(next);
                    index += 2;
                } else {
                    if (current == '\'') {
                        singleQuoted = false;
                        escapeString = false;
                    }
                    index++;
                }
                continue;
            }
            if (doubleQuoted) {
                statement.append(current);
                if (current == '"' && next == '"') {
                    statement.append(next);
                    index += 2;
                } else {
                    if (current == '"') {
                        doubleQuoted = false;
                    }
                    index++;
                }
                continue;
            }

            if (current == '-' && next == '-') {
                statement.append(current).append(next);
                lineComment = true;
                index += 2;
            } else if (current == '/' && next == '*') {
                statement.append(current).append(next);
                blockCommentDepth = 1;
                index += 2;
            } else if (current == '\'') {
                statement.append(current);
                singleQuoted = true;
                escapeString = index > 0
                        && (script.charAt(index - 1) == 'E' || script.charAt(index - 1) == 'e')
                        && (index < 2 || !isIdentifierPart(script.codePointBefore(index - 1)));
                index++;
            } else if (current == '"') {
                statement.append(current);
                doubleQuoted = true;
                index++;
            } else if (current == '$'
                    && (index == 0 || !isIdentifierPart(script.codePointBefore(index)))) {
                String tag = dollarQuoteTag(script, index);
                if (tag == null) {
                    statement.append(current);
                    index++;
                } else {
                    statement.append(tag);
                    dollarQuote = tag;
                    index += tag.length();
                }
            } else if (current == ';') {
                addStatement(statements, statement);
                index++;
            } else {
                statement.append(current);
                index++;
            }
        }

        if (singleQuoted || doubleQuoted || blockCommentDepth != 0 || dollarQuote != null) {
            throw new IllegalArgumentException("Unterminated quoted value or comment in PostgreSQL script");
        }
        addStatement(statements, statement);
        return List.copyOf(statements);
    }

    private static String dollarQuoteTag(String script, int start) {
        int end = script.indexOf('$', start + 1);
        if (end < 0) {
            return null;
        }
        String tag = script.substring(start + 1, end);
        if (!tag.isEmpty()
                && (!isIdentifierStart(tag.charAt(0))
                        || tag.chars().skip(1).anyMatch(character -> !isIdentifierPart(character)))) {
            return null;
        }
        return script.substring(start, end + 1);
    }

    private static boolean isIdentifierStart(char character) {
        return character == '_' || Character.isLetter(character) || character >= 0x80;
    }

    private static boolean isIdentifierPart(int character) {
        return character == '_' || character == '$' || Character.isLetterOrDigit(character) || character >= 0x80;
    }

    private static void addStatement(List<String> statements, StringBuilder statement) {
        String sql = statement.toString().trim();
        if (!sql.isEmpty()) {
            statements.add(sql);
        }
        statement.setLength(0);
    }
}
