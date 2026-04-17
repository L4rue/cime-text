package com.efile;

/**
 * Malformed row handling strategy during table body parsing.
 *
 * @author dingyh
 */
public enum MalformedRowPolicy {
    /**
     * Fail fast when malformed rows are found.
     */
    THROW,
    /**
     * Skip malformed data rows and continue parsing.
     */
    SKIP_ROW
}
