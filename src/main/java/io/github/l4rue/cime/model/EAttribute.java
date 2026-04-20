package io.github.l4rue.cime.model;

/**
 * Represents an E-language metadata attribute.
 *
 * @author dingyh
 */
public class EAttribute {

    private String name;
    private String value;
    private Character quote;

    /**
     * Creates an empty metadata attribute.
     */
    public EAttribute() {
    }

    /**
     * Creates a metadata attribute without preserving a quote character.
     *
     * @param name  attribute name
     * @param value attribute value
     */
    public EAttribute(String name, String value) {
        this(name, value, null);
    }

    /**
     * Creates a metadata attribute.
     *
     * @param name  attribute name
     * @param value attribute value
     * @param quote quote character used in the source, or {@code null} for unquoted values
     */
    public EAttribute(String name, String value, Character quote) {
        this.name = name;
        this.value = value;
        this.quote = quote;
    }

    /**
     * Returns the attribute name.
     *
     * @return attribute name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the attribute name.
     *
     * @param name attribute name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the attribute value.
     *
     * @return attribute value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the attribute value.
     *
     * @param value attribute value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the quote character used by the source.
     *
     * @return quote character, or {@code null} when the source value was unquoted
     */
    public Character getQuote() {
        return quote;
    }

    /**
     * Sets the quote character used by the source.
     *
     * @param quote quote character, or {@code null} for unquoted values
     */
    public void setQuote(Character quote) {
        this.quote = quote;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EAttribute)) {
            return false;
        }
        EAttribute that = (EAttribute) other;
        return equalsNullable(name, that.name)
                && equalsNullable(value, that.value)
                && equalsNullable(quote, that.quote);
    }

    @Override
    public int hashCode() {
        int result = name == null ? 0 : name.hashCode();
        result = 31 * result + (value == null ? 0 : value.hashCode());
        result = 31 * result + (quote == null ? 0 : quote.hashCode());
        return result;
    }

    private boolean equalsNullable(Object left, Object right) {
        return left == null ? right == null : left.equals(right);
    }
}
