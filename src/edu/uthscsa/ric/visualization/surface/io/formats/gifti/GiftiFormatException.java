
package edu.uthscsa.ric.visualization.surface.io.formats.gifti;

public class GiftiFormatException extends Exception {

	private static final long serialVersionUID = 1L;



	/**
	 * @param string
	 */
	public GiftiFormatException(String string) {
		super(string);
	}



	/**
	 * @param ex
	 */
	public GiftiFormatException(Throwable ex) {
		super(ex);
	}
}
