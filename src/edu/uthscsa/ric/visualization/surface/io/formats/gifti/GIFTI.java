
package edu.uthscsa.ric.visualization.surface.io.formats.gifti;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


public class GIFTI implements MetadataHolder {

	private final Map<String, String> metadata;
	private final Map<String, String> attributes;
	private final Vector<DataArray> dataArrays;
	private Map<Integer, Label> labelTable;

	public static final String ATT_VERSION = "Version";
	public static final String ATT_NUMBEROFDATAARRAYS = "NumberOfDataArrays";
	public static final String DEFAULT_VERSION = "1.0";
	public static final String DOC_TYPE = "<!DOCTYPE GIFTI SYSTEM \"http://gifti.projects.nitrc.org/gifti.dtd\">";



	/**
	 * @param attributes
	 */
	public GIFTI(Map<String, String> attributes) {
		this.attributes = attributes;
		this.metadata = new HashMap<String, String>();
		this.dataArrays = new Vector<DataArray>();
	}



	/* (non-Javadoc)
	 * @see edu.uthscsa.ric.visualization.surface.io.formats.gifti.MetadataHolder#addMetadata(java.util.Map)
	 */
	@Override
	public void addMetadata(Map<String, String> metadata) {
		this.metadata.putAll(metadata);
	}



	/**
	 * @param dataArray
	 */
	public void addDataArray(DataArray dataArray) {
		dataArrays.add(dataArray);
	}



	/**
	 * @return
	 */
	public String getVersion() {
		return attributes.get(ATT_VERSION);
	}



	/**
	 * @return
	 */
	public int getNumDataArrays() {
		int num = 0;
		try {
			num = Integer.parseInt(attributes.get(ATT_NUMBEROFDATAARRAYS));
		} catch (final NumberFormatException ex) {}
		return num;
	}



	/**
	 * @return
	 */
	public Map<String, String> getMetadata() {
		return metadata;
	}



	/**
	 * @return
	 */
	public FloatBuffer getPoints() {
		final Iterator<DataArray> it = dataArrays.iterator();
		while (it.hasNext()) {
			final DataArray dataArray = it.next();
			if (dataArray.isPoints()) {
				return dataArray.getAsFloatBuffer();
			}
		}

		return null;
	}



	/**
	 * @return
	 */
	public FloatBuffer getRGBA() {
		final Iterator<DataArray> it = dataArrays.iterator();
		while (it.hasNext()) {
			final DataArray dataArray = it.next();
			if (dataArray.isRGBA()) {
				return dataArray.getAsFloatBuffer();
			}
		}

		return null;
	}



	/**
	 *
	 * @return
	 */
	public int getNumPoints() {
		final Iterator<DataArray> it = dataArrays.iterator();
		while (it.hasNext()) {
			final DataArray dataArray = it.next();
			if (dataArray.isPoints()) {
				return dataArray.getNumElements();
			}
		}

		return 0;
	}



	/**
	 * @return
	 */
	public FloatBuffer getNormals() {
		final Iterator<DataArray> it = dataArrays.iterator();
		while (it.hasNext()) {
			final DataArray dataArray = it.next();
			if (dataArray.isNormals()) {
				return dataArray.getAsFloatBuffer();
			}
		}

		return null;
	}



	/**
	 * @return
	 */
	public IntBuffer getIndices() {
		final Iterator<DataArray> it = dataArrays.iterator();
		while (it.hasNext()) {
			final DataArray dataArray = it.next();
			if (dataArray.isIndices()) {
				return dataArray.getAsIntBuffer();
			}
		}

		return null;
	}



	/**
	 *
	 * @return
	 */
	public int getNumTriangles() {
		final Iterator<DataArray> it = dataArrays.iterator();
		while (it.hasNext()) {
			final DataArray dataArray = it.next();
			if (dataArray.isIndices()) {
				return dataArray.getNumElements() / 3;
			}
		}

		return 0;
	}



	/**
	 * @return
	 */
	public String getDescription() {
		final StringBuffer sb = new StringBuffer();

		final Set<String> keys = metadata.keySet();
		final Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			final String key = it.next();
			sb.append("  " + key + " = " + metadata.get(key) + "\n");
		}

		return sb.toString();
	}



	/**
	 * @return
	 */
	public Map<String, String> getAttributes() {
		return attributes;
	}



	/**
	 * @return
	 */
	public Vector<DataArray> getDataArrays() {
		return dataArrays;
	}



	/**
	 *
	 * @return
	 */
	public Map<Integer, Label> getLabelTable() {
		return labelTable;
	}



	/**
	 *
	 * @param labelTable
	 */
	public void setLabelTable(Map<Integer, Label> labelTable) {
		this.labelTable = labelTable;
	}
}
