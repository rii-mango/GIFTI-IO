
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
import java.util.TreeMap;
import java.util.zip.DataFormatException;

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
	private final Base64 base64;
	private boolean isReadingName;
	private boolean isReadingValue;
	private boolean isReadingData;
	private boolean isReadingXform;
	private boolean isReadingTransformedSpace;
	private boolean isReadingDataSpace;
	private boolean isReadingLabel;
	private boolean headerOnly;
	private Map<Integer, Label> labelTable;
	private Label currentLabel;
	private ByteBuffer currentBuffer;

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
	 *
	 * @param file the file to read
	 */
	public GiftiReader(final File file) {
		this.file = file;
		base64 = new Base64();
	}



	/**
	 * Read the file.
	 *
	 * @return the GIFTI object
	 * @throws GiftiFormatException
	 */
	public GIFTI parseGiftiXML() throws GiftiFormatException {
		return parseGiftiXML(false);
	}



	/**
	 *
	 * @param headerOnly
	 * @return
	 * @throws GiftiFormatException
	 */
	public GIFTI parseGiftiXML(final boolean headerOnly) throws GiftiFormatException {
		this.headerOnly = headerOnly;
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);

		SAXParser saxParser;
		try {
			saxParser = factory.newSAXParser();
			final InputStream inputStream = new FileInputStream(file);
			final BufferedInputStream bis = new BufferedInputStream(inputStream, BUFFER_SIZE);
			final Reader reader = new InputStreamReader(bis, "UTF-8");
			final InputSource is = new InputSource(reader);
			is.setEncoding("UTF-8");
			saxParser.parse(is, this);
		} catch (final ParserConfigurationException ex) {
			throw new GiftiFormatException(ex);
		} catch (final SAXException ex) {
			throw new GiftiFormatException(ex);
		} catch (final FileNotFoundException ex) {
			throw new GiftiFormatException(ex);
		} catch (final UnsupportedEncodingException ex) {
			throw new GiftiFormatException(ex);
		} catch (final IOException ex) {
			throw new GiftiFormatException(ex);
		}

		return gifti;
	}



	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
		if (qName.equalsIgnoreCase(TAG_GIFTI)) {
			currentMetadataHolder = gifti = new GIFTI(GiftiUtils.attributesToMap(attributes));
		} else if (qName.equalsIgnoreCase(TAG_LABELTABLE)) {
			labelTable = new TreeMap<Integer, Label>();
			gifti.setLabelTable(labelTable);
		} else if (qName.equalsIgnoreCase(TAG_LABEL)) {
			final Map<String, String> atts = GiftiUtils.attributesToMap(attributes);
			currentLabel = new Label(Double.parseDouble(atts.get(Label.ATT_RED)), Double.parseDouble(atts.get(Label.ATT_GREEN)),
					Double.parseDouble(atts.get(Label.ATT_BLUE)), Double.parseDouble(atts.get(Label.ATT_ALPHA)));
			labelTable.put(Integer.parseInt(atts.get(Label.ATT_KEY)), currentLabel);
			isReadingLabel = true;
			currentString = new StringBuffer();
		} else if (qName.equalsIgnoreCase(TAG_DATAARRAY)) {
			currentMetadataHolder = currentDataArray = new DataArray(GiftiUtils.attributesToMap(attributes), headerOnly);
			currentBuffer = currentDataArray.getAsByteBuffer();
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
	public void characters(final char ch[], final int start, final int length) throws SAXException {
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
		} else if (isReadingLabel) {
			currentString.append(ch, start, length);
		} else if (isReadingDataSpace) {
			currentString.append(ch, start, length);
		} else if (isReadingData) {
			if (!headerOnly) {
				if (currentDataArray.isAscii()) {
					currentString.append(ch, start, length);

					final int spaceIndex = currentString.lastIndexOf(" ");
					final int tabIndex = currentString.lastIndexOf("\t");
					final int newlineIndex = currentString.lastIndexOf("\n");

					int index = spaceIndex;

					if (tabIndex > index) {
						index = tabIndex;
					}

					if (newlineIndex > index) {
						index = newlineIndex;
					}

					final String string = currentString.substring(0, index);
					currentString.delete(0, index);
					handleAsciiData(string);
				} else {
					String str = new String(ch, start, length);
					str = str.replaceAll("\\s", "");

					currentString.append(str);

					final int actualLength = currentString.length();
					final int validLength = (actualLength / 4) * 4;

					String string = null;

					if (actualLength != validLength) { // base64 encoded string must be multiple of 4
						string = currentString.substring(0, validLength);
						currentString.delete(0, validLength);
					} else {
						string = currentString.toString();
						currentString.delete(0, actualLength);
					}

					try {
						handleBinaryData(string.getBytes("UTF-8"));
					} catch (final UnsupportedEncodingException ex) {
						throw new SAXException(ex);
					} catch (final DataFormatException ex) {
						throw new SAXException(ex);
					}
				}
			}
		}
	}



	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		if (qName.equalsIgnoreCase(TAG_GIFTI)) {

		} else if (qName.equalsIgnoreCase(TAG_LABEL)) {
			isReadingLabel = false;
			currentLabel.setLabel(currentString.toString().trim());
		} else if (qName.equalsIgnoreCase(TAG_DATAARRAY)) {

		} else if (qName.equalsIgnoreCase(TAG_METADATA)) {
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
			} catch (final GiftiFormatException ex) {
				throw new SAXException(ex);
			}
		}
	}



	private void handleTransform() throws GiftiFormatException {
		try (Scanner scanner = new Scanner(currentString.toString())) {
			final float[][] xform = new float[4][4];

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
	}



	private void handleBinaryData(final byte[] data) throws DataFormatException {
		final boolean isByte = currentDataArray.isUnsignedInt8();
		final boolean isFloat = currentDataArray.isFloat32();
		final boolean isInt = currentDataArray.isInt32();
		final boolean swap = !isByte && currentDataArray.isLittleEndian();
		final int numBytes = isByte ? 1 : 4;

		dataHandler.setData(base64.decode(data));

		while (dataHandler.hasMoreData()) {
			final int bytesRead = dataHandler.readData(buffer, leftOverBytes, buffer.length - leftOverBytes) + leftOverBytes;
			final int validBytes = (bytesRead / numBytes) * numBytes;

			for (int ctr = 0; ctr < validBytes; ctr += numBytes) {
				if (swap) {
					if (isFloat) {
						currentBuffer.putFloat(GiftiUtils.swapFloat(buffer, ctr));
					} else if (isInt) {
						currentBuffer.putInt(GiftiUtils.swapInt(buffer, ctr));
					} else {
						currentBuffer.put(buffer[ctr]);
					}
				} else {
					if (isFloat) {
						currentBuffer.putFloat(GiftiUtils.getFloat(buffer, ctr));
					} else if (isInt) {
						currentBuffer.putInt(GiftiUtils.getInt(buffer, ctr));
					} else {
						currentBuffer.put(buffer[ctr]);
					}
				}
			}

			for (int ctr = validBytes; ctr < bytesRead; ctr++) {
				buffer[ctr - validBytes] = buffer[ctr];
			}

			leftOverBytes = bytesRead - validBytes;
		}
	}



	private void handleAsciiData(final String str) {
		final StringTokenizer scanner = new StringTokenizer(str);

		while (scanner.hasMoreTokens()) {
			currentBuffer.putFloat(Float.valueOf(scanner.nextToken()));
		}
	}
}
