
package edu.uthscsa.ric.visualization.surface.io.formats.gifti;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
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
	private int level;
	private final boolean lineBreaks;

	public static final int BUFFER_SIZE = 8192;
	private final byte[] buffer = new byte[BUFFER_SIZE];
	public static final String INDENT = "   ";



	/**
	 * Constructor.
	 *
	 * @param gifti the GIFTI object to write
	 * @param file the file to write to
	 */
	public GiftiWriter(final GIFTI gifti, final File file) {
		this(gifti, file, false);
	}



	/**
	 * Constructor.
	 *
	 * @param gifti the GIFTI object to write
	 * @param file the file to write to
	 * @param lineBreaks true to allow line breaks when writing encoded binary data, false otherwise
	 */
	public GiftiWriter(final GIFTI gifti, final File file, final boolean lineBreaks) {
		this.gifti = gifti;
		this.file = file;
		this.lineBreaks = lineBreaks;
	}



	/**
	 * Write the file.
	 *
	 * @throws GiftiFormatException
	 */
	public void writeGiftiXML() throws GiftiFormatException {
		FileOutputStream os = null;
		try {

			os = new FileOutputStream(file);

			final XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(new OutputStreamWriter(os, "UTF-8"));
			writeStartDocument(out);
			writeDTD(out, GIFTI.DOC_TYPE);

			writeStartElement(out, GiftiReader.TAG_GIFTI, gifti.getAttributes(), false);

			writeMetadata(out, gifti.getMetadata());

			final Map<Integer, Label> labelTable = gifti.getLabelTable();
			if (labelTable != null) {
				writeStartElement(out, GiftiReader.TAG_LABELTABLE, null, false);

				for (final Map.Entry<Integer, Label> entry : labelTable.entrySet()) {
					final Integer key = entry.getKey();
					final Label label = entry.getValue();
					final Map<String, String> labelAtts = label.getAttributes();
					labelAtts.put(Label.ATT_KEY, String.valueOf(key));
					writeStartElement(out, GiftiReader.TAG_LABEL, labelAtts, Label.ORDER, true);
					writeCData(out, label.getLabel());
					writeEndElement(out, true); // GiftiReader.TAG_LABEL
				}

				writeEndElement(out, false); // GiftiReader.TAG_LABELTABLE
			} else {
				writeEmptyElement(out, GiftiReader.TAG_LABELTABLE);
			}

			if (gifti.getNumDataArrays() > 0) {
				final Iterator<DataArray> it = gifti.getDataArrays().iterator();
				while (it.hasNext()) {
					final DataArray da = it.next();
					writeStartElement(out, GiftiReader.TAG_DATAARRAY, da.getAttributes(), false);
					writeMetadata(out, da.getMetadata());

					final Vector<GiftiTransform> xforms = da.getTransforms();

					if ((xforms != null) && (xforms.size() > 0)) {
						final Iterator<GiftiTransform> itx = xforms.iterator();
						while (itx.hasNext()) {
							final GiftiTransform xform = itx.next();
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
		} catch (final FileNotFoundException ex) {
			throw new GiftiFormatException(ex);
		} catch (final UnsupportedEncodingException ex) {
			throw new GiftiFormatException(ex);
		} catch (final XMLStreamException ex) {
			throw new GiftiFormatException(ex);
		} catch (final FactoryConfigurationError ex) {
			throw new GiftiFormatException(ex);
		} finally {
			try {
				os.close();
			} catch (final Exception ex) {}
		}
	}



	private void writeStartElement(final XMLStreamWriter out, final String tag, final boolean containsData) throws XMLStreamException {
		writeStartElement(out, tag, null, null, containsData);
	}



	private void writeStartElement(final XMLStreamWriter out, final String tag, final Map<String, String> atts, final boolean containsData)
			throws XMLStreamException {
		writeStartElement(out, tag, atts, null, containsData);
	}



	private void writeStartElement(final XMLStreamWriter out, final String tag, final Map<String, String> atts, final List<String> attOrder,
			final boolean containsData) throws XMLStreamException {
		for (int x = 0; x < level; x++) {
			out.writeCharacters(INDENT);
		}

		out.writeStartElement(tag);

		if ((atts != null) && (atts.size() > 0)) {
			if (attOrder != null) {
				for (final String name : attOrder) {
					final String value = atts.get(name);
					out.writeAttribute(name, value);
				}
			} else {
				final Iterator<String> it = atts.keySet().iterator();
				while (it.hasNext()) {
					final String name = it.next();
					final String value = atts.get(name);

					out.writeAttribute(name, value);
				}
			}
		}

		if (!containsData) {
			out.writeCharacters("\n");
		}

		level++;
	}



	private void writeEndElement(final XMLStreamWriter out, final boolean containsData) throws XMLStreamException {
		level--;

		if (!containsData) {
			for (int x = 0; x < level; x++) {
				out.writeCharacters(INDENT);
			}
		}

		out.writeEndElement();
		out.writeCharacters("\n");
	}



	private void writeStartDocument(final XMLStreamWriter out) throws XMLStreamException {
		out.writeStartDocument();
		out.writeCharacters("\n");
	}



	private void writeCData(final XMLStreamWriter out, final String str) throws XMLStreamException {
		out.writeCData(str);
	}



	private void writeCharacters(final XMLStreamWriter out, final String str) throws XMLStreamException {
		out.writeCharacters(str);
	}



	private void writeDTD(final XMLStreamWriter out, final String dtd) throws XMLStreamException {
		out.writeDTD(dtd);
		out.writeCharacters("\n");
	}



	private void writeMetadata(final XMLStreamWriter out, final Map<String, String> metadata) throws XMLStreamException {
		if ((metadata != null) && (metadata.size() > 0)) {
			writeStartElement(out, GiftiReader.TAG_METADATA, false);

			final Iterator<String> it = metadata.keySet().iterator();
			while (it.hasNext()) {
				final String name = it.next();
				final String value = metadata.get(name);

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



	private void writeEmptyElement(final XMLStreamWriter out, final String str) throws XMLStreamException {
		for (int x = 0; x < level; x++) {
			out.writeCharacters(INDENT);
		}

		out.writeEmptyElement(str);
		out.writeCharacters("\n");
	}



	private void writeData(final XMLStreamWriter out, final DataArray dataArray) throws XMLStreamException, UnsupportedEncodingException {
		final GiftiWriterDataHandler it = new GiftiWriterDataHandler(dataArray);
		final Deflater deflater = new Deflater();
		int leftover = 0;
		int bufferMark = 0;
		String currentString = "";

		final int numValues = dataArray.getNumValues();
		int valueCt = 0;

		final byte[] deflatedBuffer = new byte[BUFFER_SIZE];

		while (it.hasNext()) {
			final int dataValue = it.next();

			valueCt++;
			final boolean lastValue = (valueCt == numValues);

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

			if ((bufferMark == BUFFER_SIZE) || lastValue) {
				deflater.setInput(buffer, 0, bufferMark);

				if (lastValue) {
					deflater.finish();
				}

				while ((!lastValue && !deflater.needsInput()) || (lastValue && !deflater.finished())) {
					final int numBytesDeflated = deflater.deflate(deflatedBuffer, leftover, deflatedBuffer.length - leftover) + leftover;

					if (numBytesDeflated > 0) {
						final int numValid = (numBytesDeflated / 3) * 3;
						leftover = numBytesDeflated % 3;

						final byte[] encoded = GiftiUtils.encode(deflatedBuffer, 0, numValid);

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

		if (currentString.length() > 0) {
			out.writeCharacters(currentString);
		}

		if (lineBreaks) {
			out.writeCharacters("\r\n");
		}

		deflater.end();
	}
}
