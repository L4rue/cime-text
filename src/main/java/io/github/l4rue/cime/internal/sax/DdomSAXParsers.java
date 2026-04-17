package io.github.l4rue.cime.internal.sax;

import org.xml.sax.*;

/**
 * SAX parser wrapper that exposes the custom {@link EdomXMLReader}.
 *
 * @author dingyh
 */
public class DdomSAXParsers extends javax.xml.parsers.SAXParser {

    /**
     * Returns the legacy SAX {@link Parser} implementation.
     *
     * @return always {@code null} because only {@link XMLReader} is supported
     * @throws SAXException never thrown by this implementation
     */
    @Override
    public Parser getParser() throws SAXException {
        return null;
    }

    /**
     * Returns the custom XML reader used by this parser.
     *
     * @return new {@link EdomXMLReader} instance
     * @throws SAXException never thrown by this implementation
     */
    @Override
    public XMLReader getXMLReader() throws SAXException {
        return new EdomXMLReader();
    }

    /**
     * Returns whether namespace processing is enabled.
     *
     * @return {@code false}
     */
    @Override
    public boolean isNamespaceAware() {
        return false;
    }

    /**
     * Returns whether validation is enabled.
     *
     * @return {@code false}
     */
    @Override
    public boolean isValidating() {
        return false;
    }

    /**
     * Ignores parser property assignments because the lightweight reader exposes no custom properties.
     *
     * @param name  property name
     * @param value property value
     * @throws SAXNotRecognizedException never thrown by this implementation
     * @throws SAXNotSupportedException  never thrown by this implementation
     */
    @Override
    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    /**
     * Returns a parser property value.
     *
     * @param name property name
     * @return always {@code null} because custom properties are not supported
     * @throws SAXNotRecognizedException never thrown by this implementation
     * @throws SAXNotSupportedException  never thrown by this implementation
     */
    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return null;
    }
}
