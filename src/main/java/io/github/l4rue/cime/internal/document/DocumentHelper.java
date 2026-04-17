package io.github.l4rue.cime.internal.document;

import io.github.l4rue.cime.internal.sax.EdomSAXReader;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.StringTokenizer;

/**
 * Helper methods for building parser DOM documents from in-memory text.
 *
 * @author dingyh
 */
public class DocumentHelper {

	/**
	 * Parses a raw E-language string into a DOM document.
	 *
	 * @param text raw E-language text
	 * @return parsed document tree
	 * @throws DocumentException when the input cannot be parsed
	 */
	public static Document parseText(String text) throws DocumentException {
		Document result = null;
		EdomSAXReader reader = new EdomSAXReader();

		InputSource source = new InputSource(new StringReader(text));

		result = reader.read(source);

		return result;
	}

	private static String getEncoding(String text) {
		String result;
		label0: {
			result = null;
			String xml = text.trim();
			if (!xml.startsWith("<?xml"))
				break label0;
			int end = xml.indexOf("?>");
			String sub = xml.substring(0, end);
			StringTokenizer tokens = new StringTokenizer(sub, " =\"'");
			String token;
			do {
				if (!tokens.hasMoreTokens())
					break label0;
				token = tokens.nextToken();
			} while (!"encoding".equals(token));
			if (tokens.hasMoreTokens())
				result = tokens.nextToken();
		}
		return result;
	}
	
	/**
	 * Splits a token string using the historic delimiter expression used by the parser.
	 *
	 * @param str source text to split
	 * @return token array produced by the legacy split rule
	 */
	public static String[] split(String str){
		return str.split(" =\"'");
	}
}
