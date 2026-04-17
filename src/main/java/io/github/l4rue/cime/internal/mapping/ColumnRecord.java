package io.github.l4rue.cime.internal.mapping;

import java.lang.reflect.Field;

/**
 * Stores the mapping between a bean field and an E-table column index.
 *
 * @author dingyh
 */
public class ColumnRecord {
    private Field field;

    private int index;

    /**
     * Returns the mapped bean field.
     *
     * @return bean field associated with the column
     */
    public Field getField() {
        return field;
    }

    /**
     * Sets the mapped bean field.
     *
     * @param field bean field associated with the column
     */
    public void setField(Field field) {
        this.field = field;
    }

    /**
     * Returns the zero-based column index.
     *
     * @return mapped column index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the zero-based column index.
     *
     * @param index mapped column index
     */
    public void setIndex(int index) {
        this.index = index;
    }
}
