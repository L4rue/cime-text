package com.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for tokenizing and inspecting parser input strings.
 *
 * @author dingyh
 */
public class StringUtils {

    private static final char[] WRAPPERS = new char[]{'\'', '"'};

    /**
     * Splits a line by whitespace while preserving quoted tokens.
     *
     * @param line source line
     * @return token array, or an empty array when the input is {@code null}
     */
    public static String[] splitLineWithSpace(String line) {
        if (line == null) {
            return new String[0];
        }
        List<String> list = new ArrayList<String>();
        int length = line.length();
        int pos = 0;
        while (pos < length) {
            while (pos < length && isSpace(line.charAt(pos))) {
                pos++;
            }
            if (pos >= length) {
                break;
            }
            char current = line.charAt(pos);
            if (isWrapper(current)) {
                char wrapper = current;
                pos++;
                int sectionStart = pos;
                while (pos < length && line.charAt(pos) != wrapper) {
                    pos++;
                }
                if (pos >= length) {
                    throw new IllegalArgumentException("Unclosed quote detected");
                }
                list.add(line.substring(sectionStart, pos));
                pos++;
                continue;
            }
            int sectionStart = pos;
            while (pos < length && !isSpace(line.charAt(pos))) {
                pos++;
            }
            list.add(line.substring(sectionStart, pos));
        }

        return list.toArray(new String[list.size()]);
    }

    /**
     * Returns whether the character is a single or double quote.
     *
     * @param chr character to inspect
     * @return {@code true} when the character is a quote wrapper
     */
    private static boolean isWrapper(char chr) {
        for (char c : WRAPPERS) {
            if (c == chr) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the character is treated as whitespace.
     *
     * @param chr character to inspect
     * @return {@code true} when the character is whitespace
     */
    public static boolean isSpace(char chr) {
        return chr == ' ' || chr == '\n' || chr == '\t' || chr == '\r' || chr == '\f';
    }

    /**
     * Returns whether the input contains digits only.
     *
     * @param str input string
     * @return {@code true} when the input is an integer string
     */
    public static boolean isInteger(String str) {
        String regx = "\\d+";
        return str.matches(regx);
    }
}
