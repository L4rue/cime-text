package io.github.l4rue.cime.write;

/**
 * Supported E-language table body layouts for writing {@code ETable} objects.
 *
 * @author dingyh
 */
public enum TableLayout {

    /**
     * Row-based layout, using {@code @} for headers and {@code #} for rows.
     */
    HORIZONTAL,

    /**
     * Single-column layout, using {@code @@}.
     */
    SINGLE_COLUMN,

    /**
     * Multi-column layout, using {@code @#}.
     */
    MULTI_COLUMN
}
