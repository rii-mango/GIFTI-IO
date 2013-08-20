
package edu.uthscsa.ric.visualization.surface.io.formats.gifti;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;


public class GiftiUtils {

	// The Apache codec Base64 class doesn't allow to specify offset and length for some reason...
	// http://oodt.apache.org/components/maven/xref/org/apache/oodt/commons/util/Base64.html
	/**
	 * @param data
	 * @param offset
	 * @param length
	 * @return
	 */
	public static byte[] encode(final byte[] data, int offset, int length) {
		if (data == null)
			return null;
		if (offset < 0 || offset > data.length)
			throw new IndexOutOfBoundsException("Can't encode at index " + offset + " which is beyond array bounds 0.." + data.length);
		if (length < 0)
			throw new IllegalArgumentException("Can't encode a negative amount of data");
		if (offset + length > data.length)
			throw new IndexOutOfBoundsException("Can't encode beyond right edge of array");

		int i, j;
		byte dest[] = new byte[((length + 2) / 3) * 4];

		// Convert groups of 3 bytes into 4.
		for (i = 0 + offset, j = 0; i < offset + length - 2; i += 3) {
			dest[j++] = (byte) ((data[i] >>> 2) & 077);
			dest[j++] = (byte) ((data[i + 1] >>> 4) & 017 | (data[i] << 4) & 077);
			dest[j++] = (byte) ((data[i + 2] >>> 6) & 003 | (data[i + 1] << 2) & 077);
			dest[j++] = (byte) (data[i + 2] & 077);
		}

		// Convert any leftover bytes.
		if (i < offset + length) {
			dest[j++] = (byte) ((data[i] >>> 2) & 077);
			if (i < offset + length - 1) {
				dest[j++] = (byte) ((data[i + 1] >>> 4) & 017 | (data[i] << 4) & 077);
				dest[j++] = (byte) ((data[i + 1] << 2) & 077);
			} else
				dest[j++] = (byte) ((data[i] << 4) & 077);
		}

		// Now, map those onto base 64 printable ASCII.
		for (i = 0; i < j; i++) {
			if (dest[i] < 26)
				dest[i] = (byte) (dest[i] + 'A');
			else if (dest[i] < 52)
				dest[i] = (byte) (dest[i] + 'a' - 26);
			else if (dest[i] < 62)
				dest[i] = (byte) (dest[i] + '0' - 52);
			else if (dest[i] < 63)
				dest[i] = (byte) '+';
			else
				dest[i] = (byte) '/';
		}

		// Pad the result with and we're done.
		for (; i < dest.length; i++)
			dest[i] = (byte) '=';
		return dest;
	}



	/**
	 * @param atts
	 * @return
	 */
	public static Map<String, String> attributesToMap(Attributes atts) {
		Map<String, String> map = null;

		if (atts != null) {
			map = new HashMap<String, String>();
			int numAtts = atts.getLength();
			for (int ctr = 0; ctr < numAtts; ctr++) {
				String name = atts.getQName(ctr);
				String value = atts.getValue(name);
				map.put(name, value);
			}
		}

		return map;
	}



	/**
	 * @param b
	 * @param i
	 * @return
	 */
	public static int swapInt(byte[] b, int i) {
		return Integer.reverseBytes(bytes_to_int(b, i));
	}



	private static int bytes_to_int(byte[] b, int i) {
		return ((((b[i + 3]) & 0xff) << 0) | (((b[i + 2]) & 0xff) << 8) | (((b[i + 1]) & 0xff) << 16) | (((b[i + 0]) & 0xff) << 24));
	}



	/**
	 * @param b
	 * @param i
	 * @return
	 */
	public static int getInt(byte[] b, int i) {
		return bytes_to_int(b, i);
	}



	/**
	 * @param b
	 * @param i
	 * @return
	 */
	public static float swapFloat(byte[] b, int i) {
		return Float.intBitsToFloat(swapInt(b, i));
	}



	/**
	 * @param b
	 * @param i
	 * @return
	 */
	public static float getFloat(byte[] b, int i) {
		return bytes_to_float(b, i);
	}



	private static float bytes_to_float(byte b[], int i) {
		return Float.intBitsToFloat(bytes_to_int(b, i));
	}



	/**
	 * @param aMatParams
	 * @param abs
	 * @return
	 */
	public static boolean isIdentity(float[][] aMatParams, boolean abs) {
		for (int ctrOut = 0; ctrOut < 4; ctrOut++) {
			for (int ctrIn = 0; ctrIn < 4; ctrIn++) {
				if ((ctrOut == ctrIn) && ((abs && (Math.abs(aMatParams[ctrOut][ctrIn]) != 1)) || (!abs && (aMatParams[ctrOut][ctrIn]) != 1))) {
					return false;
				} else if ((ctrOut != ctrIn) && (aMatParams[ctrOut][ctrIn] != 0)) {
					return false;
				}
			}
		}

		return true;
	}
}
