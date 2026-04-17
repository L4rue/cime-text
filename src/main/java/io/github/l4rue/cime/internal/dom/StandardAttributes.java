package io.github.l4rue.cime.internal.dom;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mutable attribute collection used while parsing start tags.
 *
 * @author dingyh
 */
public class StandardAttributes implements org.xml.sax.Attributes{

	
	private final Map<String, String> attributes = new LinkedHashMap<String, String>();
	
	/**
	 * Creates an empty mutable attribute collection.
	 */
	public StandardAttributes(){}
	
	
	
	public void put(String name,String value){
		attributes.put(name, value);
	}
	/**
	 * Returns the index of the named attribute.
	 *
	 * @param name attribute name
	 * @return zero-based attribute index, or {@code -1} when absent
	 */
	public int getIndex(String name) {
		int i=0;
		for(String key:attributes.keySet()){
			if(key.equals(name)){
				return i;
			}
			i++;
		}
		return -1;
	}

	/**
	 * Returns the index of the named attribute using namespace-aware lookup.
	 *
	 * @param uri ignored namespace value for this lightweight implementation
	 * @param localName local attribute name
	 * @return zero-based attribute index, or {@code -1} when absent
	 */
	public int getIndex(String uri, String localName) {
		return getIndex(localName);
	}

	public int getLength() {
		return attributes.size();
	}

	public String getLocalName(int index) {
		
		int i=0;
		for(String key:attributes.keySet()){
			if(i==index)
				return key;
			i++;
		}
		return null;
	}

	public String getQName(int index) {
		return getLocalName(index);
	}

	/**
	 * Returns the declared attribute type for the supplied index.
	 *
	 * @param index zero-based attribute index
	 * @return {@code "CDATA"} when the index is valid, otherwise {@code null}
	 */
	public String getType(int index) {
		return index >= 0 && index < attributes.size() ? "CDATA" : null;
	}

	/**
	 * Returns the declared attribute type for the supplied name.
	 *
	 * @param name attribute name
	 * @return {@code "CDATA"} when the attribute exists, otherwise {@code null}
	 */
	public String getType(String name) {
		return attributes.containsKey(name) ? "CDATA" : null;
	}

	/**
	 * Returns the declared attribute type for the supplied local name.
	 *
	 * @param uri ignored namespace value for this lightweight implementation
	 * @param localName local attribute name
	 * @return {@code "CDATA"} when the attribute exists, otherwise {@code null}
	 */
	public String getType(String uri, String localName) {
		return getType(localName);
	}

	/**
	 * Returns the namespace URI for the attribute at the supplied index.
	 *
	 * @param index zero-based attribute index
	 * @return always {@code null} because namespaces are not tracked by this implementation
	 */
	public String getURI(int index) {
		return null;
	}

	public String getValue(int index) {
		int i=0;
		for(String value:attributes.values()){
			if(i==index){
				return value;
			}
			i++;
		}
		return null;
	}

	public String getValue(String name) {
		return attributes.get(name);
	}

	public String getValue(String uri, String localName) {
		return getValue(localName);
	}
	 

}
