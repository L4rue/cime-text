/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.

package io.github.l4rue.cime.internal.sax;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import io.github.l4rue.cime.internal.dom.StandardDocument;
import io.github.l4rue.cime.internal.dom.StandardElement;
import io.github.l4rue.cime.internal.util.ArrayStack;

/**
 * SAX content handler that builds the lightweight DOM tree used by the parser.
 *
 * @author dingyh
 */
public class EdomSAXContentHandler extends DefaultHandler {
	
	private final ArrayStack<Node> elementStack;

	private Document document;
	
	/**
	 * Creates a content handler with a new internal node stack.
	 */
	public EdomSAXContentHandler(){
		this(new ArrayStack<Node>());
	}

	/**
	 * Creates a content handler backed by the supplied node stack.
	 *
	 * @param elementStack stack used to maintain the current parse path
	 */
	public EdomSAXContentHandler(ArrayStack<Node> elementStack){
		this.elementStack = elementStack;
	}
	
	@Override
	/**
	 * Writes text content into the current element.
	 *
	 * @param ch source character buffer
	 * @param start start offset in the buffer
	 * @param length number of characters to consume
	 * @throws SAXException when SAX processing fails
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		
		Element ele=(Element) elementStack.peek();
		ele.setTextContent(new String(ch,start,length));
	}

	@Override
	/**
	 * Finalizes document parsing by removing the root document node from the stack.
	 *
	 * @throws SAXException when SAX processing fails
	 */
	public void endDocument() throws SAXException {
		elementStack.pop();
	}

	@Override
	/**
	 * Finishes the current element scope.
	 *
	 * @param uri namespace URI, ignored by this implementation
	 * @param localName local element name
	 * @param name qualified element name
	 * @throws SAXException when SAX processing fails
	 */
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		elementStack.pop();
	}

	@Override
	/**
	 * Initializes the lightweight DOM document before parsing starts.
	 *
	 * @throws SAXException when SAX processing fails
	 */
	public void startDocument() throws SAXException {
		document=new StandardDocument();
		elementStack.push(document);
	}

	@Override
	/**
	 * Creates a new lightweight element and attaches it to the current parent node.
	 *
	 * @param uri namespace URI, ignored by this implementation
	 * @param localName local element name
	 * @param name qualified element name
	 * @param attributes SAX attributes for the element
	 * @throws SAXException when SAX processing fails
	 */
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		Element ele=new StandardElement(localName);
		for(int i=0;i<attributes.getLength();i++){
			ele.setAttribute(attributes.getLocalName(i), attributes.getValue(i));
		}
		elementStack.peek().appendChild(ele);
		this.elementStack.push(ele);
	}

	
	/**
	 * Returns the document built from the SAX events processed so far.
	 *
	 * @return parsed document
	 */
	public  Document getDocument(){
		return document;
	}
	 
}
