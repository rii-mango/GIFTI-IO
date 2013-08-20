
package edu.uthscsa.ric.visualization.surface.io.formats.gifti;

import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


public class GiftiReaderDataHandler {

	private byte[] data;
	private int position;
	private Inflater inflater;



	/**
	 * @param isCompressed
	 */
	public GiftiReaderDataHandler(boolean isCompressed) {
		if (isCompressed) {
			this.inflater = new Inflater();
		}
	}



	/**
	 * @param data
	 */
	public void setData(byte[] data) {
		if (inflater != null) {
			inflater.setInput(data);
		} else {
			position = 0;
			this.data = data;
		}
	}



	/**
	 * @return
	 */
	public boolean hasMoreData() {
		if (inflater != null) {
			return !inflater.needsInput();
		} else {
			return (position < data.length);
		}
	}



	/**
	 * @param buffer
	 * @param offset
	 * @param length
	 * @return
	 * @throws DataFormatException
	 */
	public int readData(byte[] buffer, int offset, int length) throws DataFormatException {
		if (inflater != null) {
			return inflater.inflate(buffer, offset, length);
		} else {
			length = Math.min(length, data.length - position);
			System.arraycopy(data, position, buffer, offset, length);
			position += length;
			return length;
		}
	}
}
