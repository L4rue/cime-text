package io.github.l4rue.cime.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents file-level E-language metadata from the {@code <! ... !>} header.
 *
 * @author dingyh
 */
public class EHeader {

    private String rawText;
    private LinkedHashMap<String, EAttribute> attributes;

    /**
     * Creates an empty file header.
     */
    public EHeader() {
        attributes = new LinkedHashMap<String, EAttribute>();
    }

    /**
     * Returns the original header text when it was parsed from a source file.
     *
     * @return raw header text, or {@code null} when unavailable
     */
    public String getRawText() {
        return rawText;
    }

    /**
     * Stores the original header text.
     *
     * @param rawText raw header text
     */
    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    /**
     * Returns file-level attributes in source order.
     *
     * @return mutable attribute map
     */
    public LinkedHashMap<String, EAttribute> getAttributes() {
        return attributes;
    }

    /**
     * Replaces file-level attributes.
     *
     * @param attributes attributes to store
     */
    public void setAttributes(Map<String, EAttribute> attributes) {
        this.attributes = new LinkedHashMap<String, EAttribute>();
        if (attributes != null) {
            for (EAttribute attribute : attributes.values()) {
                putAttribute(attribute);
            }
        }
    }

    /**
     * Adds or replaces a file-level attribute.
     *
     * @param name  attribute name
     * @param value attribute value
     */
    public void putAttribute(String name, String value) {
        putAttribute(new EAttribute(name, value));
    }

    /**
     * Adds or replaces a file-level attribute.
     *
     * @param attribute attribute to store
     */
    public void putAttribute(EAttribute attribute) {
        if (attribute == null || attribute.getName() == null) {
            return;
        }
        attributes.put(attribute.getName(), attribute);
    }

    /**
     * Returns a file-level attribute value.
     *
     * @param name attribute name
     * @return attribute value, or {@code null} when absent
     */
    public String getAttribute(String name) {
        EAttribute attribute = getAttributeNode(name);
        return attribute == null ? null : attribute.getValue();
    }

    /**
     * Returns a file-level attribute object.
     *
     * @param name attribute name
     * @return attribute object, or {@code null} when absent
     */
    public EAttribute getAttributeNode(String name) {
        return attributes.get(name);
    }

    /**
     * Indicates whether this header carries metadata.
     *
     * @return {@code true} when no attributes are present
     */
    public boolean isEmpty() {
        return attributes.isEmpty();
    }
}
