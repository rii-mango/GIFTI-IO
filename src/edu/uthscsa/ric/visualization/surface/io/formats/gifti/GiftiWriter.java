
package edu.uthscsa.ric.visualization.surface.io.formats.gifti;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.zip.Deflater;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


public class GiftiWriter {

	private final GIFTI gifti;
	private final File file;
	private final float[] offset;
	private final float[] scale;
	private int level;
	private final boolean lineBreaks;

	public static final int BUFFER_SIZE = 8192;
	private final byte[] buffer = new byte[BUFFER_SIZE];
	public static final String INDENT = "   ";



	/**
	 * Constructor.
	 * @param gifti the GIFTI object to write
	 * @param file the file to write to
	 * @param offset offset to be applied to points (float[3])
	 * @param scale scale to be applied to points (float[3])
	 * @param lineBreaks true to allow line breaks when writing encoded binary data, false otherwise
	 */
	public GiftiWriter(GIFTI gifti, File file, float[] offset, float[] scale, boolean lineBreaks) {
		this.gifti = gifti;
		this.file = file;
		this.offset = offset;
		this.scale = scale;
		this.lineBreaks = lineBreaks;
	}



	/**
	 * Write the file.
	 * @throws GiftiFormatException
	 */
	public void writeGiftiXML() throws GiftiFormatException {
		FileOutputStream os = null;
		try {

			os = new FileOutputStream(file);

			XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(new OutputStreamWriter(os, "UTF-8"));
			writeStartDocument(out);
			writeDTD(out, GIFTI.DOC_TYPE);

			writeStartElement(out, GiftiReader.TAG_GIFTI, gifti.getAttributes(), false);

			writeMetadata(out, gifti.getMetadata());

			writeEmptyElement(out, GiftiReader.TAG_LABELTABLE);

			if (gifti.getNumDataArrays() > 0) {
				Iterator<DataArray> it = gifti.getDataArrays().iterator();
				while (it.hasNext()) {
					DataArray da = it.next();
					writeStartElement(out, GiftiReader.TAG_DATAARRAY, da.getAttributes(), false);
					writeMetadata(out, da.getMetadata());

					Vector<GiftiTransform> xforms = da.getTransforms();

					if ((xforms != null) && (xforms.size() > 0)) {
						Iterator<GiftiTransform> itx = xforms.iterator();
						while (itx.hasNext()) {
							GiftiTransform xform = itx.next();
							writeStartElement(out, GiftiReader.TAG_COORDINATESYSTEMTRANSFORMMATRIX, null, false);

							writeStartElement(out, GiftiReader.TAG_DATASPACE, null, true);
							writeCData(out, xform.dataSpace);
							writeEndElement(out, true); // GiftiReader.TAG_DATASPACE

							writeStartElement(out, GiftiReader.TAG_TRANSFORMEDSPACE, null, true);
							writeCData(out, xform.xformSpace);
							writeEndElement(out, true); // GiftiReader.TAG_TRANSFORMEDSPACE

							writeStartElement(out, GiftiReader.TAG_MATRIXDATA, null, true);
							writeCharacters(out, xform.getXformAsString());
							writeEndElement(out, true); // GiftiReader.TAG_MATRIXDATA

							writeEndElement(out, false);// GiftiReader.TAG_COORDINATESYSTEMTRANSFORMMATRIX
						}
					}

					writeStartElement(out, GiftiReader.TAG_DATA, null, false);
					writeData(out, da);
					writeEndElement(out, false); // GiftiReader.TAG_DATA

					writeEndElement(out, true); // GiftiReader.TAG_DATAARRAY
				}
			}

			writeEndElement(out, false); // GiftiReader.TAG_GIFTI

			out.writeEndDocument();
			out.close();
		} catch (FileNotFoundException ex) {
			throw new GiftiFormatException(ex);
		} catch (UnsupportedEncodingException ex) {
			throw new GiftiFormatException(ex);
		} catch (XMLStreamException ex) {
			throw new GiftiFormatException(ex);
		} catch (FactoryConfigurationError ex) {
			throw new GiftiFormatException(ex);
		} finally {
			try {
				os.close();
			} catch (Exception ex) {}
		}
	}



	private void writeStartElement(XMLStreamWriter out, String tag, boolean containsData) throws XMLStreamException {
		writeStartElement(out, tag, null, containsData);
	}



	private void writeStartElement(XMLStreamWriter out, String tag, Map<String, String> atts, boolean containsData) throws XMLStreamException {
		for (int x = 0; x < level; x++) {
			out.writeCharacters(INDENT);
		}

		out.writeStartElement(tag);

		if ((atts != null) && (atts.size() > 0)) {
			Iterator<String> it = atts.keySet().iterator();
			while (it.hasNext()) {
				String name = it.next();
				String value = atts.get(name);

				out.writeAttribute(name, value);
			}
		}

		if (!containsData) {
			out.writeCharacters("\n");
		}

		level++;
	}



	private void writeEndElement(XMLStreamWriter out, boolean containsData) throws XMLStreamException {
		level--;

		if (!containsData) {
			for (int x = 0; x < level; x++) {
				out.writeCharacters(INDENT);
			}
		}

		out.writeEndElement();
		out.writeCharacters("\n");
	}



	private void writeStartDocument(XMLStreamWriter out) throws XMLStreamException {
		out.writeStartDocument();
		out.writeCharacters("\n");
	}



	private void writeCData(XMLStreamWriter out, String str) throws XMLStreamException {
		out.writeCData(str);
	}



	private void writeCharacters(XMLStreamWriter out, String str) throws XMLStreamException {
		out.writeCharacters(str);
	}



	private void writeDTD(XMLStreamWriter out, String dtd) throws XMLStreamException {
		out.writeDTD(dtd);
		out.writeCharacters("\n");
	}



	private void writeMetadata(XMLStreamWriter out, Map<String, String> metadata) throws XMLStreamException {
		if ((metadata != null) && (metadata.size() > 0)) {
			writeStartElement(out, GiftiReader.TAG_METADATA, false);

			Iterator<String> it = metadata.keySet().iterator();
			while (it.hasNext()) {
				String name = it.next();
				String value = metadata.get(name);

				writeStartElement(out, GiftiReader.TAG_MD, false);

				writeStartElement(out, GiftiReader.TAG_NAME, true);
				writeCData(out, name);
				writeEndElement(out, true); // GiftiReader.TAG_NAME

				writeStartElement(out, GiftiReader.TAG_VALUE, true);
				writeCData(out, value);
				writeEndElement(out, true); // GiftiReader.TAG_VALUE

				writeEndElement(out, false); // GiftiReader.TAG_MD
			}

			writeEndElement(out, false); // GiftiReader.TAG_METADATA
		}
	}



	private void writeEmptyElement(XMLStreamWriter out, String str) throws XMLStreamException {
		for (int x = 0; x < level; x++) {
			out.writeCharacters(INDENT);
		}

		out.writeEmptyElement(str);
		out.writeCharacters("\n");
	}



	private void writeData(XMLStreamWriter out, DataArray dataArray) throws XMLStreamException, UnsupportedEncodingException {
		GiftiWriterDataHandler it = new GiftiWriterDataHandler(dataArray, offset, scale);
		Deflater deflater = new Deflater();
		int leftover = 0;
		int bufferMark = 0;
		String currentString = "";

		int numValues = dataArray.getNumValues();
		int valueCt = 0;

		byte[] deflatedBuffer = new byte[BUFFER_SIZE];

		while (it.hasNext()) {
			int dataValue = it.next();
			valueCt++;
			boolean lastValue = (valueCt == numValues);

			if (dataArray.isLittleEndian()) {
				buffer[bufferMark++] = (byte) ((dataValue >> 0) & 0xFF);
				buffer[bufferMark++] = (byte) ((dataValue >> 8) & 0xFF);
				buffer[bufferMark++] = (byte) ((dataValue >> 16) & 0xFF);
				buffer[bufferMark++] = (byte) ((dataValue >> 24) & 0xFF);
			} else {
				buffer[bufferMark++] = (byte) ((dataValue >> 24) & 0xFF);
				buffer[bufferMark++] = (byte) ((dataValue >> 16) & 0xFF);
				buffer[bufferMark++] = (byte) ((dataValue >> 8) & 0xFF);
				buffer[bufferMark++] = (byte) ((dataValue >> 0) & 0xFF);
			}

			if (bufferMark == BUFFER_SIZE) {
				deflater.setInput(buffer);

				if (lastValue) {
					deflater.finish();
				}

				while ((!lastValue && !deflater.needsInput()) || (lastValue && !deflater.finished())) {
					int numBytesDeflated = deflater.deflate(deflatedBuffer, leftover, deflatedBuffer.length - leftover) + leftover;

					if (numBytesDeflated > 0) {
						int numValid = (numBytesDeflated / 3) * 3;
						leftover = numBytesDeflated % 3;

						byte[] encoded = GiftiUtils.encode(deflatedBuffer, 0, numValid);

						if (lineBreaks) {
							currentString = (currentString + new String(encoded, "UTF-8"));

							while (currentString.length() > 76) {
								out.writeCharacters(currentString.substring(0, 76) + "\r\n");
								currentString = currentString.substring(76);
							}
						} else {
							out.writeCharacters(new String(encoded, "UTF-8"));
						}

						if (leftover > 0) {
							System.arraycopy(deflatedBuffer, numValid, deflatedBuffer, 0, leftover);
						}
					}
				}

				bufferMark = 0;
			}
		}

		if (bufferMark > 0) {
			deflater.setInput(buffer, 0, bufferMark);
			deflater.finish();

			while (!deflater.finished()) {
				int numBytesDeflated = deflater.deflate(deflatedBuffer, leftover, deflatedBuffer.length - leftover) + leftover;

				if (numBytesDeflated > 0) {
					int numValid = (numBytesDeflated / 3) * 3;
					leftover = numBytesDeflated % 3;

					byte[] encoded = GiftiUtils.encode(deflatedBuffer, 0, numValid);

					if (lineBreaks) {
						currentString = (currentString + new String(encoded, "UTF-8"));

						while (currentString.length() > 76) {
							out.writeCharacters(currentString.substring(0, 76) + "\r\n");
							currentString = currentString.substring(76);
						}
					} else {
						out.writeCharacters(new String(encoded, "UTF-8"));
					}

					if (leftover > 0) {
						System.arraycopy(deflatedBuffer, numValid, deflatedBuffer, 0, leftover);
					}
				}
			}

			if (leftover > 0) {
				byte[] encoded = GiftiUtils.encode(deflatedBuffer, 0, leftover);

				if (lineBreaks) {
					currentString = (currentString + new String(encoded, "UTF-8"));

					while (currentString.length() > 76) {
						out.writeCharacters(currentString.substring(0, 76) + "\r\n");
						currentString = currentString.substring(76);
					}
				} else {
					out.writeCharacters(new String(encoded, "UTF-8"));
				}
			}
		}

		if (currentString.length() > 0) {
			out.writeCharacters(currentString);
		}

		if (lineBreaks) {
			out.writeCharacters("\r\n");
		}

		deflater.end();
	}
}
