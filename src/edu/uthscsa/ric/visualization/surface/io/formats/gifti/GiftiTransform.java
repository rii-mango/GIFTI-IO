
package edu.uthscsa.ric.visualization.surface.io.formats.gifti;



public class GiftiTransform {

	public float[][] xform;
	public String dataSpace;
	public String xformSpace;
	public static final float[][] IDENTITY = { { 1, 0, 0, 0 }, { 0, 1, 0, 0 }, { 0, 0, 1, 0 }, { 0, 0, 0, 1 } };
	public static final String NIFTI_XFORM_UNKNOWN = "NIFTI_XFORM_UNKNOWN";
	public static final String NIFTI_XFORM_SCANNER_ANAT = "NIFTI_XFORM_SCANNER_ANAT";
	public static final String NIFTI_XFORM_ALIGNED_ANAT = "NIFTI_XFORM_ALIGNED_ANAT";
	public static final String NIFTI_XFORM_TALAIRACH = "NIFTI_XFORM_TALAIRACH";
	public static final String NIFTI_XFORM_MNI_152 = "NIFTI_XFORM_MNI_152";



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return dataSpace + " -> " + xformSpace;
	}



	/**
	 * @return
	 */
	public String getXformAsString() {
		StringBuffer sb = new StringBuffer();
		for (int ctrOut = 0; ctrOut < 4; ctrOut++) {
			for (int ctrIn = 0; ctrIn < 4; ctrIn++) {
				sb.append(xform[ctrOut][ctrIn] + " ");
			}
		}

		String str = sb.toString();
		return str.trim();
	}



	/**
	 * @return
	 */
	public static GiftiTransform buildDefaultTransform() {
		GiftiTransform gt = new GiftiTransform();
		gt.dataSpace = NIFTI_XFORM_UNKNOWN;
		gt.xformSpace = NIFTI_XFORM_UNKNOWN;
		gt.xform = IDENTITY;

		return gt;
	}
}
