package io.github.l4rue.cime.internal.dom;

import org.w3c.dom.*;

/**
 * Minimal {@link Document} implementation used by the parser runtime.
 * Methods not required by the parser intentionally return default values.
 *
 * @author dingyh
 */
public class StandardDocument implements Document {

    Element root;

    StandardNodeList childNodes = new StandardNodeList();

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

        return childNodes;
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
        childNodes.add(newChild);
        return newChild;
    }

    public boolean hasChildNodes() {
        // TODO Auto-generated method stub
        return false;
    }

    public Node cloneNode(boolean deep) {
        // Not implemented by this lightweight DOM adapter.
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
        // Not implemented by this lightweight DOM adapter.
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

    public DocumentType getDoctype() {
        // TODO Auto-generated method stub
        return null;
    }

    public DOMImplementation getImplementation() {
        // TODO Auto-generated method stub
        return null;
    }

    public Element getDocumentElement() {
        // TODO Auto-generated method stub
        return root;
    }

    public Element createElement(String tagName) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public DocumentFragment createDocumentFragment() {
        // TODO Auto-generated method stub
        return null;
    }

    public Text createTextNode(String data) {
        // TODO Auto-generated method stub
        return null;
    }

    public Comment createComment(String data) {
        // TODO Auto-generated method stub
        return null;
    }

    public CDATASection createCDATASection(String data) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public ProcessingInstruction createProcessingInstruction(String target,
                                                             String data) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public Attr createAttribute(String name) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public EntityReference createEntityReference(String name)
            throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public NodeList getElementsByTagName(String tagname) {
        // TODO Auto-generated method stub
        return null;
    }

    public Node importNode(Node importedNode, boolean deep) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public Element createElementNS(String namespaceURI, String qualifiedName)
            throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public Attr createAttributeNS(String namespaceURI, String qualifiedName)
            throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        // TODO Auto-generated method stub
        return null;
    }

    public Element getElementById(String elementId) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getInputEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getXmlEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean getXmlStandalone() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setXmlStandalone(boolean xmlStandalone) throws DOMException {
        // TODO Auto-generated method stub

    }

    public String getXmlVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setXmlVersion(String xmlVersion) throws DOMException {
        // TODO Auto-generated method stub

    }

    public boolean getStrictErrorChecking() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setStrictErrorChecking(boolean strictErrorChecking) {
        // TODO Auto-generated method stub

    }

    public String getDocumentURI() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setDocumentURI(String documentURI) {
        // TODO Auto-generated method stub

    }

    public Node adoptNode(Node source) throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public DOMConfiguration getDomConfig() {
        // TODO Auto-generated method stub
        return null;
    }

    public void normalizeDocument() {
        // TODO Auto-generated method stub

    }

    public Node renameNode(Node n, String namespaceURI, String qualifiedName)
            throws DOMException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setDocumentElement(Element root) {
        this.root = root;
    }
}
