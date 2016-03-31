
package edu.uthscsa.ric.visualization.surface.io.formats.gifti;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Label {

	private double alpha;
	private double blue;
	private double green;
	private double red;
	private String label;

	public static final List<String> ORDER = new ArrayList<String>();
	public static final String ATT_ALPHA = "Alpha";
	public static final String ATT_BLUE = "Blue";
	public static final String ATT_GREEN = "Green";
	public static final String ATT_KEY = "Key";
	public static final String ATT_RED = "Red";

	static {
		ORDER.add(ATT_KEY);
		ORDER.add(ATT_RED);
		ORDER.add(ATT_GREEN);
		ORDER.add(ATT_BLUE);
		ORDER.add(ATT_ALPHA);
	}



	public static int computeHash(final double red, final double green, final double blue, final double alpha) {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(alpha);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(blue);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(green);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(red);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		return result;
	}



	public Label(final double red, final double green, final double blue, final double alpha) {
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}



	public Label(final double red, final double green, final double blue, final double alpha, final String label) {
		this(red, green, blue, alpha);
		this.label = label;
	}



	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Label other = (Label) obj;
		if (Double.doubleToLongBits(alpha) != Double.doubleToLongBits(other.alpha)) {
			return false;
		}
		if (Double.doubleToLongBits(blue) != Double.doubleToLongBits(other.blue)) {
			return false;
		}
		if (Double.doubleToLongBits(green) != Double.doubleToLongBits(other.green)) {
			return false;
		}
		if (Double.doubleToLongBits(red) != Double.doubleToLongBits(other.red)) {
			return false;
		}
		return true;
	}



	public double getAlpha() {
		return alpha;
	}



	public Map<String, String> getAttributes() {
		final Map<String, String> map = new HashMap<String, String>();
		map.put(ATT_RED, String.valueOf(red));
		map.put(ATT_GREEN, String.valueOf(green));
		map.put(ATT_BLUE, String.valueOf(blue));
		map.put(ATT_ALPHA, String.valueOf(alpha));

		return map;
	}



	public double getBlue() {
		return blue;
	}



	public double getGreen() {
		return green;
	}



	public String getLabel() {
		return label;
	}



	public double getRed() {
		return red;
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(alpha);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(blue);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(green);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(red);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		return result;
	}



	public void setAlpha(final double alpha) {
		this.alpha = alpha;
	}



	public void setBlue(final double blue) {
		this.blue = blue;
	}



	public void setGreen(final double green) {
		this.green = green;
	}



	public void setLabel(final String label) {
		this.label = label;
	}



	public void setRed(final double red) {
		this.red = red;
	}
}
