package io.github.l4rue.cime.internal.sax;

import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import io.github.l4rue.cime.internal.sax.EdomXMLReader;

/**
 * SAX parser wrapper that exposes the custom {@link EdomXMLReader}.
 *
 * @author dingyh
 */
public class DdomSAXParsers extends javax.xml.parsers.SAXParser{

	@Override
	/**
	 * Returns the legacy SAX {@link Parser} implementation.
	 *
	 * @return always {@code null} because only {@link XMLReader} is supported
	 * @throws SAXException never thrown by this implementation
	 */
	public Parser getParser() throws SAXException {
		return null;
	}

	@Override
	/**
	 * Returns a parser property value.
	 *
	 * @param name property name
	 * @return always {@code null} because custom properties are not supported
	 * @throws SAXNotRecognizedException never thrown by this implementation
	 * @throws SAXNotSupportedException never thrown by this implementation
	 */
	public Object getProperty(String name) throws SAXNotRecognizedException,
			SAXNotSupportedException {
		return null;
	}

	@Override
	/**
	 * Returns the custom XML reader used by this parser.
	 *
	 * @return new {@link EdomXMLReader} instance
	 * @throws SAXException never thrown by this implementation
	 */
	public XMLReader getXMLReader() throws SAXException {
		return new EdomXMLReader();
	}

	@Override
	/**
	 * Returns whether namespace processing is enabled.
	 *
	 * @return {@code false}
	 */
	public boolean isNamespaceAware() {
		return false;
	}

	@Override
	/**
	 * Returns whether validation is enabled.
	 *
	 * @return {@code false}
	 */
	public boolean isValidating() {
		return false;
	}

	@Override
	/**
	 * Ignores parser property assignments because the lightweight reader exposes no custom properties.
	 *
	 * @param name property name
	 * @param value property value
	 * @throws SAXNotRecognizedException never thrown by this implementation
	 * @throws SAXNotSupportedException never thrown by this implementation
	 */
	public void setProperty(String name, Object value)
			throws SAXNotRecognizedException, SAXNotSupportedException {
	}

}
