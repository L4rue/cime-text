package io.github.l4rue.cime.internal.dom;

import java.util.List;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Simple {@link NamedNodeMap} adapter backed by an attribute list.
 * Unsupported namespace-specific operations fall back to local-name handling.
 *
 * @author dingyh
 */
public class StandardNamedNodeMap implements NamedNodeMap{

	private final List<Attr> attrs;
	
	/**
	 * Creates a map view backed by the supplied attribute list.
	 *
	 * @param attrs mutable attribute list backing this map
	 */
	public   StandardNamedNodeMap(List<Attr> attrs){
		this.attrs=attrs;
	}

	/**
	 * Returns the number of stored attributes.
	 *
	 * @return attribute count
	 */
	public int getLength() {
		return attrs.size();
	}

	/**
	 * Returns the attribute with the supplied qualified name.
	 *
	 * @param name attribute name to resolve
	 * @return matching attribute node, or {@code null} when absent
	 */
	public Node getNamedItem(String name) {
		if(attrs!=null && attrs.size()>0){
			for(Attr attr:attrs){
				if(attr.getName().equals(name)){
					return attr;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the attribute that matches the supplied local name.
	 *
	 * @param namespaceURI ignored namespace value for this lightweight implementation
	 * @param localName local attribute name to resolve
	 * @return matching attribute node, or {@code null} when absent
	 */
	public Node getNamedItemNS(String namespaceURI, String localName)
			throws DOMException {
		return getNamedItem(localName);
	}

	/**
	 * Returns the attribute at the requested index.
	 *
	 * @param index zero-based attribute index
	 * @return attribute node at the requested index, or {@code null} when out of bounds
	 */
	public Node item(int index) {
		if (index < 0 || index >= attrs.size()) {
			return null;
		}
		return attrs.get(index);
	}

	/**
	 * Removes the attribute with the supplied qualified name.
	 *
	 * @param name attribute name to remove
	 * @return removed attribute node, or {@code null} when absent
	 * @throws DOMException never thrown by this implementation
	 */
	public Node removeNamedItem(String name) throws DOMException {
		for (int i = 0; i < attrs.size(); i++) {
			Attr attr = attrs.get(i);
			if (attr.getName().equals(name)) {
				return attrs.remove(i);
			}
		}
		return null;
	}

	/**
	 * Removes the attribute with the supplied local name.
	 *
	 * @param namespaceURI ignored namespace value for this lightweight implementation
	 * @param localName local attribute name to remove
	 * @return removed attribute node, or {@code null} when absent
	 * @throws DOMException never thrown by this implementation
	 */
	public Node removeNamedItemNS(String namespaceURI, String localName)
			throws DOMException {
		return removeNamedItem(localName);
	}

	/**
	 * Adds or replaces an attribute node.
	 *
	 * @param arg attribute node to store
	 * @return previous attribute with the same name, or the supplied node when newly added
	 * @throws DOMException when the node is not an {@link Attr}
	 */
	public Node setNamedItem(Node arg) throws DOMException {
		if (!(arg instanceof Attr)) {
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "Only Attr nodes are supported");
		}
		Attr newAttr = (Attr) arg;
		Node existing = removeNamedItem(newAttr.getName());
		attrs.add(newAttr);
		return existing == null ? newAttr : existing;
	}

	/**
	 * Adds or replaces an attribute node using local-name lookup.
	 *
	 * @param arg attribute node to store
	 * @return previous attribute with the same name, or the supplied node when newly added
	 * @throws DOMException when the node is not an {@link Attr}
	 */
	public Node setNamedItemNS(Node arg) throws DOMException {
		return setNamedItem(arg);
	}

}
