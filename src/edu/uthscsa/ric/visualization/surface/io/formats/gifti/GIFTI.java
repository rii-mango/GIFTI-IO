
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
		} catch (NumberFormatException ex) {}
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
		Iterator<DataArray> it = dataArrays.iterator();
		while (it.hasNext()) {
			DataArray dataArray = it.next();
			if (dataArray.isPoints()) {
				return dataArray.getAsPointsBuffer();
			}
		}

		return null;
	}



	/**
	 * @return
	 */
	public FloatBuffer getNormals() {
		Iterator<DataArray> it = dataArrays.iterator();
		while (it.hasNext()) {
			DataArray dataArray = it.next();
			if (dataArray.isNormals()) {
				return dataArray.getAsNormalsBuffer();
			}
		}

		return null;
	}



	/**
	 * @return
	 */
	public IntBuffer getIndices() {
		Iterator<DataArray> it = dataArrays.iterator();
		while (it.hasNext()) {
			DataArray dataArray = it.next();
			if (dataArray.isTriangles()) {
				return dataArray.getAsIndicesBuffer();
			}
		}

		return null;
	}



	/**
	 * @return
	 */
	public String getDescription() {
		StringBuffer sb = new StringBuffer();

		Set<String> keys = metadata.keySet();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			String key = it.next();
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
}
