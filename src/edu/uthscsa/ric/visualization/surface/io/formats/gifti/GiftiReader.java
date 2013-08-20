
package edu.uthscsa.ric.visualization.surface.io.formats.gifti;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.zip.DataFormatException;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.codec.binary.Base64;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class GiftiReader extends DefaultHandler {

	private final File file;
	private GIFTI gifti;
	private MetadataHolder currentMetadataHolder;
	private DataArray currentDataArray;
	private GiftiTransform currentTransform;
	private Map<String, String> metadata;
	private MD currentMD;
	private StringBuffer currentString;
	private GiftiReaderDataHandler dataHandler;
	private int leftOverBytes;
	private int componentIndex;
	private final float[] offset;
	private final float[] scale;
	private final Base64 base64;
	private final Tuple3f tempPoint;
	private boolean isReadingName;
	private boolean isReadingValue;
	private boolean isReadingData;
	private boolean isReadingXform;
	private boolean isReadingTransformedSpace;
	private boolean isReadingDataSpace;

	public static final String TAG_COORDINATESYSTEMTRANSFORMMATRIX = "CoordinateSystemTransformMatrix";
	public static final String TAG_DATA = "Data";
	public static final String TAG_DATAARRAY = "DataArray";
	public static final String TAG_DATASPACE = "DataSpace";
	public static final String TAG_GIFTI = "GIFTI";
	public static final String TAG_LABEL = "Label";
	public static final String TAG_LABELTABLE = "LabelTable";
	public static final String TAG_MATRIXDATA = "MatrixData";
	public static final String TAG_METADATA = "MetaData";
	public static final String TAG_MD = "MD";
	public static final String TAG_NAME = "Name";
	public static final String TAG_TRANSFORMEDSPACE = "TransformedSpace";
	public static final String TAG_VALUE = "Value";

	public static final int BUFFER_SIZE = 8192;
	private final byte[] buffer = new byte[BUFFER_SIZE];


	
	
	/**
	 * Constructor.
	 * @param file the file to read
	 */
	public GiftiReader(File file) {
		this(file, new float[] {0, 0, 0}, new float[] {1, 1, 1});
	}
	
	

	/**
	 * Constructor.
	 * @param file the file to read
	 * @param offset offset to be applied to points (float[3])
	 * @param scale scale to be applied to points (float[3])
	 */
	public GiftiReader(File file, float[] offset, float[] scale) {
		this.file = file;
		this.base64 = new Base64();
		this.offset = offset;
		this.scale = scale;
		this.tempPoint = new Point3f();
	}



	/**
	 * Read the file.
	 * @return the GIFTI object
	 * @throws GiftiFormatException
	 */
	public GIFTI parseGiftiXML() throws GiftiFormatException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);

		SAXParser saxParser;
		try {
			saxParser = factory.newSAXParser();
			InputStream inputStream = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(inputStream, BUFFER_SIZE);
			Reader reader = new InputStreamReader(bis, "UTF-8");
			InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			saxParser.parse(is, this);
		} catch (ParserConfigurationException ex) {
			throw new GiftiFormatException(ex);
		} catch (SAXException ex) {
			throw new GiftiFormatException(ex);
		} catch (FileNotFoundException ex) {
			throw new GiftiFormatException(ex);
		} catch (UnsupportedEncodingException ex) {
			throw new GiftiFormatException(ex);
		} catch (IOException ex) {
			throw new GiftiFormatException(ex);
		}

		return gifti;
	}



	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (qName.equalsIgnoreCase(TAG_GIFTI)) {
			currentMetadataHolder = gifti = new GIFTI(GiftiUtils.attributesToMap(attributes));
		} else if (qName.equalsIgnoreCase(TAG_DATAARRAY)) {
			currentMetadataHolder = currentDataArray = new DataArray(GiftiUtils.attributesToMap(attributes));
			gifti.addDataArray(currentDataArray);
		} else if (qName.equalsIgnoreCase(TAG_METADATA)) {
			metadata = new HashMap<String, String>();
		} else if (qName.equalsIgnoreCase(TAG_MD)) {
			currentMD = new MD();
		} else if (qName.equalsIgnoreCase(TAG_NAME)) {
			isReadingName = true;
			currentString = new StringBuffer();
		} else if (qName.equalsIgnoreCase(TAG_VALUE)) {
			isReadingValue = true;
			currentString = new StringBuffer();
		} else if (qName.equalsIgnoreCase(TAG_DATA)) {
			isReadingData = true;
			currentString = new StringBuffer();
			componentIndex = 0;
			leftOverBytes = 0;
			dataHandler = new GiftiReaderDataHandler(currentDataArray.isGzipBase64Binary());
		} else if (qName.equalsIgnoreCase(TAG_COORDINATESYSTEMTRANSFORMMATRIX)) {
			currentTransform = new GiftiTransform();
		} else if (qName.equalsIgnoreCase(TAG_TRANSFORMEDSPACE)) {
			isReadingTransformedSpace = true;
			currentString = new StringBuffer();
		} else if (qName.equalsIgnoreCase(TAG_DATASPACE)) {
			isReadingDataSpace = true;
			currentString = new StringBuffer();
		} else if (qName.equalsIgnoreCase(TAG_MATRIXDATA)) {
			isReadingXform = true;
			currentString = new StringBuffer();
		}
	}



	/**
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		if (isReadingName) {
			isReadingName = false;
			currentString.append(ch, start, length);
		} else if (isReadingValue) {
			isReadingValue = false;
			currentString.append(ch, start, length);
		} else if (isReadingXform) {
			currentString.append(ch, start, length);
		} else if (isReadingTransformedSpace) {
			currentString.append(ch, start, length);
		} else if (isReadingDataSpace) {
			currentString.append(ch, start, length);
		} else if (isReadingData) {
			if (currentDataArray.isAscii()) {
				currentString.append(ch, start, length);

				int spaceIndex = currentString.lastIndexOf(" ");
				int tabIndex = currentString.lastIndexOf("\t");
				int newlineIndex = currentString.lastIndexOf("\n");

				int index = spaceIndex;

				if (tabIndex > index) {
					index = tabIndex;
				}

				if (newlineIndex > index) {
					index = newlineIndex;
				}

				String string = currentString.substring(0, index);
				currentString.delete(0, index);

				if (currentDataArray.isPoints() || currentDataArray.isNormals()) {
					handleAsciiPointData(string);
				} else if (currentDataArray.isTriangles()) {
					handleAsciiIndicesData(string);
				}
			} else {
				String str = new String(ch, start, length);
				str = str.replaceAll("\\s", "");

				currentString.append(str);

				int actualLength = currentString.length();
				int validLength = (actualLength / 4) * 4;

				String string = null;

				if (actualLength != validLength) { // base64 encoded string must be multiple of 4
					string = currentString.substring(0, validLength);
					currentString.delete(0, validLength);
				} else {
					string = currentString.toString();
					currentString.delete(0, actualLength);
				}

				try {
					if (currentDataArray.isPoints() || currentDataArray.isNormals()) {
						handleBinaryPointData(string.getBytes("UTF-8"));
					} else if (currentDataArray.isTriangles()) {
						handleBinaryIndicesData(string.getBytes("UTF-8"));
					}
				} catch (UnsupportedEncodingException ex) {
					throw new SAXException(ex);
				} catch (DataFormatException ex) {
					throw new SAXException(ex);
				} catch (GiftiFormatException ex) {
					throw new SAXException(ex);
				}
			}
		}
	}



	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase(TAG_GIFTI)) {} else if (qName.equalsIgnoreCase(TAG_DATAARRAY)) {} else if (qName.equalsIgnoreCase(TAG_METADATA)) {
			currentMetadataHolder.addMetadata(metadata);
		} else if (qName.equalsIgnoreCase(TAG_MD)) {
			metadata.put(currentMD.name, currentMD.value);
		} else if (qName.equalsIgnoreCase(TAG_NAME)) {
			isReadingName = false;
			currentMD.name = currentString.toString().trim();
		} else if (qName.equalsIgnoreCase(TAG_VALUE)) {
			isReadingValue = false;
			currentMD.value = currentString.toString().trim();
		} else if (qName.equalsIgnoreCase(TAG_DATA)) {
			isReadingData = false;
		} else if (qName.equalsIgnoreCase(TAG_TRANSFORMEDSPACE)) {
			isReadingTransformedSpace = false;
			currentTransform.xformSpace = currentString.toString().trim();
		} else if (qName.equalsIgnoreCase(TAG_DATASPACE)) {
			isReadingDataSpace = false;
			currentTransform.dataSpace = currentString.toString().trim();
		} else if (qName.equalsIgnoreCase(TAG_MATRIXDATA)) {
			isReadingXform = false;
			try {
				handleTransform();
			} catch (GiftiFormatException ex) {
				throw new SAXException(ex);
			}
		}
	}



	private void handleTransform() throws GiftiFormatException {
		Scanner scanner = new Scanner(currentString.toString());
		float[][] xform = new float[4][4];

		for (int ctrOut = 0; ctrOut < 4; ctrOut++) {
			for (int ctrIn = 0; ctrIn < 4; ctrIn++) {
				if (scanner.hasNextFloat()) {
					xform[ctrOut][ctrIn] = scanner.nextFloat();
				} else {
					throw new GiftiFormatException("Could not read the coordinate transform matrix!");
				}
			}
		}

		currentTransform.xform = xform;

		if (!GiftiUtils.isIdentity(xform, false)) {
			currentDataArray.addTransform(currentTransform);
		}
	}



	private void handleBinaryPointData(byte[] data) throws DataFormatException {
		ByteBuffer outputBuffer = currentDataArray.getBuffer();
		boolean isNormals = currentDataArray.isNormals();
		boolean isByte = currentDataArray.isUnsignedInt8();
		boolean isFloat = currentDataArray.isFloat32();
		boolean isInt = currentDataArray.isInt32();
		boolean swap = !isByte && currentDataArray.isLittleEndian();
		int numBytes = isByte ? 1 : 4;

		dataHandler.setData(base64.decode(data));

		while (dataHandler.hasMoreData()) {
			int bytesRead = dataHandler.readData(buffer, leftOverBytes, buffer.length - leftOverBytes) + leftOverBytes;
			int validBytes = (bytesRead / numBytes) * numBytes;

			for (int ctr = 0; ctr < validBytes; ctr += numBytes) {
				float value;

				if (swap) {
					if (isFloat) {
						value = GiftiUtils.swapFloat(buffer, ctr);
					} else if (isInt) {
						value = GiftiUtils.swapInt(buffer, ctr);
					} else {
						value = buffer[ctr];
					}
				} else {
					if (isFloat) {
						value = GiftiUtils.getFloat(buffer, ctr);
					} else if (isInt) {
						value = GiftiUtils.getInt(buffer, ctr);
					} else {
						value = buffer[ctr];
					}
				}

				if (componentIndex == 0) {
					tempPoint.x = value;
				} else if (componentIndex == 1) {
					tempPoint.y = value;
				} else if (componentIndex == 2) {
					tempPoint.z = value;
					float xVal, yVal, zVal;

					if (isNormals) {
						xVal = tempPoint.x * scale[0];
						yVal = tempPoint.y * scale[1];
						zVal = tempPoint.z * scale[2];
					} else {
						xVal = (scale[0] * tempPoint.x) - offset[0];
						yVal = (scale[1] * tempPoint.y) - offset[1];
						zVal = (scale[2] * tempPoint.z) - offset[2];
					}

					outputBuffer.putFloat(xVal);
					outputBuffer.putFloat(yVal);
					outputBuffer.putFloat(zVal);
				}

				componentIndex++;
				componentIndex %= 3;
			}

			for (int ctr = validBytes; ctr < bytesRead; ctr++) {
				buffer[ctr - validBytes] = buffer[ctr];
			}

			leftOverBytes = bytesRead - validBytes;
		}
	}



	private void handleBinaryIndicesData(byte[] data) throws DataFormatException, GiftiFormatException {
		ByteBuffer outputBuffer = currentDataArray.getBuffer();

		boolean isByte = currentDataArray.isUnsignedInt8();
		boolean isFloat = currentDataArray.isFloat32();
		boolean swap = !isByte && currentDataArray.isLittleEndian();

		if (isFloat) {
			throw new GiftiFormatException("Indices cannot be float data!");
		}

		dataHandler.setData(base64.decode(data));

		while (dataHandler.hasMoreData()) {
			int bytesRead = dataHandler.readData(buffer, leftOverBytes, buffer.length - leftOverBytes) + leftOverBytes;
			int validBytes = (bytesRead / 4) * 4;

			for (int ctr = 0; ctr < validBytes; ctr += 4) {
				int value;
				if (swap) {
					value = GiftiUtils.swapInt(buffer, ctr);
				} else {
					value = GiftiUtils.getInt(buffer, ctr);
				}

				outputBuffer.putInt(value);
			}

			for (int ctr = validBytes; ctr < bytesRead; ctr++) {
				buffer[ctr - validBytes] = buffer[ctr];
			}

			leftOverBytes = bytesRead - validBytes;
		}
	}



	private void handleAsciiPointData(String str) {
		ByteBuffer outputBuffer = currentDataArray.getBuffer();
		StringTokenizer scanner = new StringTokenizer(str);
		boolean isNormals = currentDataArray.isNormals();

		while (scanner.hasMoreTokens()) {
			float value = Float.valueOf(scanner.nextToken());

			if (componentIndex == 0) {
				tempPoint.x = value;
			} else if (componentIndex == 1) {
				tempPoint.y = value;
			} else if (componentIndex == 2) {
				tempPoint.z = value;

				float xVal, yVal, zVal;

				if (isNormals) {
					xVal = tempPoint.x * scale[0];
					yVal = tempPoint.y * scale[1];
					zVal = tempPoint.z * scale[2];
				} else {
					xVal = (scale[0] * tempPoint.x) - offset[0];
					yVal = (scale[1] * tempPoint.y) - offset[1];
					zVal = (scale[2] * tempPoint.z) - offset[2];
				}

				outputBuffer.putFloat(xVal);
				outputBuffer.putFloat(yVal);
				outputBuffer.putFloat(zVal);
			}

			componentIndex++;
			componentIndex %= 3;
		}
	}



	private void handleAsciiIndicesData(String str) {
		ByteBuffer outputBuffer = currentDataArray.getBuffer();
		StringTokenizer scanner = new StringTokenizer(str);

		while (scanner.hasMoreTokens()) {
			outputBuffer.putInt(Integer.valueOf(scanner.nextToken()));
		}
	}
}
