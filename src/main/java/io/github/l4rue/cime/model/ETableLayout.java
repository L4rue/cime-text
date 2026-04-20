package io.github.l4rue.cime.model;

/**
 * Source table body layout detected while parsing an E-language table.
 *
 * @author dingyh
 */
public enum ETableLayout {

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
