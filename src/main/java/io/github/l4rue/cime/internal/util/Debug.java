package io.github.l4rue.cime.internal.util;

import java.io.PrintStream;
import java.util.List;

/**
 * Simple debug helpers for printing parser structures.
 *
 * @author dingyh
 */
public class Debug {

	/**
	 * Prints a debug representation to {@link System#out}.
	 *
	 * @param value value to print
	 */
	public static void debug(Object value) {
		debug(value, System.out);
	}

	/**
	 * Prints a debug representation to the supplied output stream.
	 *
	 * @param value value to print
	 * @param out target output stream
	 */
	public static void debug(Object value, PrintStream out) {
		if (value == null) {
			out.println("null");
			return;
		}
		if (value instanceof String) {
			out.print(value);
			return;
		}
		if (value instanceof Object[]) {
			Object[] array = (Object[]) value;
			for (int i = 0; i < array.length; i++) {
				if (i > 0) {
					out.print(',');
				}
				debug(array[i], out);
			}
			out.println();
			return;
		}
		if (value instanceof List<?>) {
			List<?> list = (List<?>) value;
			for (Object item : list) {
				debug(item, out);
			}
			return;
		}
		out.println(value);
	}
}
