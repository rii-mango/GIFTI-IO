
package edu.uthscsa.ric.visualization.surface.io.formats.gifti;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;


public class GiftiWriterDataHandler {

	private FloatBuffer floatBuffer;
	private IntBuffer intBuffer;
	private int index;
	private int capacity;



	/**
	 * @param dataArray
	 */
	public GiftiWriterDataHandler(final DataArray dataArray) {
		if (dataArray.isFloat32()) {
			floatBuffer = dataArray.getAsFloatBuffer();
			capacity = floatBuffer.capacity();
		} else if (dataArray.isInt32()) {
			intBuffer = dataArray.getAsIntBuffer();
			capacity = intBuffer.capacity();
		}
	}



	/**
	 * @return
	 */
	public boolean hasNext() {
		return index < capacity;
	}



	/**
	 * @return
	 */
	public int next() {
		if (floatBuffer != null) {
			return Float.floatToIntBits(floatBuffer.get(index++));
		} else if (intBuffer != null) {
			return intBuffer.get(index++);
		}

		return 0;
	}
}
