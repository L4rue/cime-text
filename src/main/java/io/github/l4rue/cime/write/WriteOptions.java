package io.github.l4rue.cime.write;

/**
 * Options that control E-language file writing.
 *
 * @author dingyh
 */
public final class WriteOptions {

    private final TableLayout layout;
    private final boolean preserveSourceLayout;

    private WriteOptions(TableLayout layout) {
        this(layout, false);
    }

    private WriteOptions(TableLayout layout, boolean preserveSourceLayout) {
        this.layout = layout == null ? TableLayout.HORIZONTAL : layout;
        this.preserveSourceLayout = preserveSourceLayout;
    }

    /**
     * Returns the default write options.
     *
     * @return options using the horizontal table layout
     */
    public static WriteOptions defaults() {
        return horizontal();
    }

    /**
     * Returns options for the horizontal table layout.
     *
     * @return horizontal-layout options
     */
    public static WriteOptions horizontal() {
        return new WriteOptions(TableLayout.HORIZONTAL);
    }

    /**
     * Returns options for the single-column table layout.
     *
     * @return single-column-layout options
     */
    public static WriteOptions singleColumn() {
        return new WriteOptions(TableLayout.SINGLE_COLUMN);
    }

    /**
     * Returns options for the multi-column table layout.
     *
     * @return multi-column-layout options
     */
    public static WriteOptions multiColumn() {
        return new WriteOptions(TableLayout.MULTI_COLUMN);
    }

    /**
     * Returns options that preserve the source table body layout when available.
     *
     * @return options preserving parsed source layout
     */
    public static WriteOptions preserveSourceLayout() {
        return new WriteOptions(TableLayout.HORIZONTAL, true);
    }

    /**
     * Creates options with an explicit table layout.
     *
     * @param layout table layout to write
     * @return write options
     */
    public static WriteOptions of(TableLayout layout) {
        return new WriteOptions(layout);
    }

    /**
     * Returns the configured table layout.
     *
     * @return target table body layout
     */
    public TableLayout getLayout() {
        return layout;
    }

    /**
     * Indicates whether writer should use each table's parsed source layout when available.
     *
     * @return {@code true} when source layout should be preserved
     */
    public boolean isPreserveSourceLayout() {
        return preserveSourceLayout;
    }
}
