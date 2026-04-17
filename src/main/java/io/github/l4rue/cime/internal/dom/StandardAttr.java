package io.github.l4rue.cime.internal.dom;

import org.w3c.dom.*;

/**
 * Lightweight {@link Attr} implementation used by the custom DOM model.
 * Methods not required by the parser intentionally return default values.
 *
 * @author dingyh
 */
public class StandardAttr implements Attr {

    String name;
    String value;

    /**
     * Creates an empty attribute.
     */
    public StandardAttr() {}

    /**
     * Creates an attribute with the supplied name and value.
     *
     * @param name  attribute name
     * @param value attribute value
     */
    public StandardAttr(String name, String value) {
        this.name = name;
        this.value = value;
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

    public boolean getSpecified() {
        // TODO Auto-generated method stub
        return false;
    }

    public String getValue() {
        // TODO Auto-generated method stub
        return value;
    }

    public void setValue(String value) throws DOMException {
        this.value = value;
    }

    public Element getOwnerElement() {
        // Not implemented by this lightweight DOM adapter.
        return null;
    }

    public TypeInfo getSchemaTypeInfo() {
        // Not implemented by this lightweight DOM adapter.
        return null;
    }

    public boolean isId() {
        // TODO Auto-generated method stub
        return false;
    }

    public String getNodeName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getNodeValue() throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setNodeValue(String nodeValue) throws DOMException {
        // TODO Auto-generated method stub

    }

    public short getNodeType() {
        // TODO Auto-generated method stub
        return 0;
    }

    public Node getParentNode() {
        // TODO Auto-generated method stub
        return null;
    }

    public NodeList getChildNodes() {
        // TODO Auto-generated method stub
        return null;
    }

    public Node getFirstChild() {
        // TODO Auto-generated method stub
        return null;
    }

    public Node getLastChild() {
        // TODO Auto-generated method stub
        return null;
    }

    public Node getPreviousSibling() {
        // TODO Auto-generated method stub
        return null;
    }

    public Node getNextSibling() {
        // TODO Auto-generated method stub
        return null;
    }

    public NamedNodeMap getAttributes() {
        // TODO Auto-generated method stub
        return null;
    }

    public Document getOwnerDocument() {
        // TODO Auto-generated method stub
        return null;
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public Node removeChild(Node oldChild) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public Node appendChild(Node newChild) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean hasChildNodes() {
        // TODO Auto-generated method stub
        return false;
    }

    public Node cloneNode(boolean deep) {
        // TODO Auto-generated method stub
        return null;
    }

    public void normalize() {
        // TODO Auto-generated method stub

    }

    public boolean isSupported(String feature, String version) {
        // TODO Auto-generated method stub
        return false;
    }

    public String getNamespaceURI() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getPrefix() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setPrefix(String prefix) throws DOMException {
        // TODO Auto-generated method stub

    }

    public String getLocalName() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean hasAttributes() {
        // TODO Auto-generated method stub
        return false;
    }

    public String getBaseURI() {
        // TODO Auto-generated method stub
        return null;
    }

    public short compareDocumentPosition(Node other) throws DOMException {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getTextContent() throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setTextContent(String textContent) throws DOMException {
        // TODO Auto-generated method stub

    }

    public boolean isSameNode(Node other) {
        // TODO Auto-generated method stub
        return false;
    }

    public String lookupPrefix(String namespaceURI) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isDefaultNamespace(String namespaceURI) {
        // TODO Auto-generated method stub
        return false;
    }

    public String lookupNamespaceURI(String prefix) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isEqualNode(Node arg) {
        // TODO Auto-generated method stub
        return false;
    }

    public Object getFeature(String feature, String version) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object setUserData(String key, Object data, UserDataHandler handler) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getUserData(String key) {
        // TODO Auto-generated method stub
        return null;
    }
}
