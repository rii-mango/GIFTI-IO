GIFTI-IO
========

A GIFTI reader/writer implementation in Java.  Current list of features:
- Supports most features common to .surf.gii format, including the intent types: 
  * NIFTI_INTENT_POINTSET
  * NIFTI_INTENT_TRIANGLE
  * NIFTI_INTENT_VECTOR
- Supports reading ASCII, Base64Binary, GZipBase64Binary data (all datatypes, byte orders).
- Supports writing GZipBase64Binary data (all datatypes, byte orders).

TODO:
- External data file support.
- LabelTabel/Label element support.
- Support for complete list of intent types.


Installation
------
1. Requires [Apache Commons Codec] (http://commons.apache.org/proper/commons-codec/): [(download)](http://mirror.sdunix.com/apache//commons/codec/binaries/commons-codec-1.8-bin.zip).  Copy to `lib` directory.
2. Run `build.xml` to produce `build/gifti-io.jar`.


Usage (Reader)
------
```Java
GiftiReader reader = new GiftiReader(file);
GIFTI gifti = null;

try {
    gifti = reader.parseGiftiXML();
			
    FloatBuffer points = gifti.getPoints();
    FloatBuffer normals = gifti.getNormals();
    IntBuffer indices = gifti.getIndices();
    
    // do something with data...
} catch (GiftiFormatException ex) {
    // do something with error...
}
```

Usage (Writer)
------
```Java
// gifti
Map<String, String> giftiAtts = new HashMap<String, String>();
giftiAtts.put(GIFTI.ATT_NUMBEROFDATAARRAYS, "2");
giftiAtts.put(GIFTI.ATT_VERSION, GIFTI.DEFAULT_VERSION);
GIFTI gifti = new GIFTI(giftiAtts);

Map<String, String> metadata = new HashMap<String, String>();
gifti.addMetadata(metadata);


// points
FloatBuffer points = //...
int numPoints = //...
		
Map<String, String> pointsAtts = new HashMap<String, String>();
pointsAtts.put(DataArray.ATT_INTENT, DataArray.NIFTI_INTENT_POINTSET);
pointsAtts.put(DataArray.ATT_ARRAYINDEXINGORDER, DataArray.DIM_ORDER_ROWMAJORORDER);
pointsAtts.put(DataArray.ATT_DATATYPE, DataArray.TYPE_NIFTI_TYPE_FLOAT32);
pointsAtts.put(DataArray.ATT_DIMENSIONALITY, "2");
pointsAtts.put(DataArray.ATT_DIMN + "0", "" + numPoints);
pointsAtts.put(DataArray.ATT_DIMN + "1", "3");
pointsAtts.put(DataArray.ATT_ENCODING, DataArray.ENCODING_GZIPBASE64BINARY);
pointsAtts.put(DataArray.ATT_ENDIAN, (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) ? DataArray.DATA_ORDER_LITTLEENDIAN
				: DataArray.DATA_ORDER_BIGENDIAN);
pointsAtts.put(DataArray.ATT_ENCODING, DataArray.ENCODING_GZIPBASE64BINARY);

DataArray pointsData = new DataArray(pointsAtts, points);
gifti.addDataArray(pointsData);
pointsData.addTransform(GiftiTransform.buildDefaultTransform());


// normals
FloatBuffer normals = //...

Map<String, String> normalsAtts = new HashMap<String, String>();
normalsAtts.put(DataArray.ATT_INTENT, DataArray.NIFTI_INTENT_VECTOR);
normalsAtts.put(DataArray.ATT_ARRAYINDEXINGORDER, DataArray.DIM_ORDER_ROWMAJORORDER);
normalsAtts.put(DataArray.ATT_DATATYPE, DataArray.TYPE_NIFTI_TYPE_FLOAT32);
normalsAtts.put(DataArray.ATT_DIMENSIONALITY, "2");
normalsAtts.put(DataArray.ATT_DIMN + "0", "" + numPoints);
normalsAtts.put(DataArray.ATT_DIMN + "1", "3");
normalsAtts.put(DataArray.ATT_ENCODING, DataArray.ENCODING_GZIPBASE64BINARY);
normalsAtts.put(DataArray.ATT_ENDIAN, (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) ? DataArray.DATA_ORDER_LITTLEENDIAN
					: DataArray.DATA_ORDER_BIGENDIAN);
normalsAtts.put(DataArray.ATT_ENCODING, DataArray.ENCODING_GZIPBASE64BINARY);

DataArray normalsData = new DataArray(normalsAtts, normals);
gifti.addDataArray(normalsData);


// triangles
IntBuffer triangles = //...
int numTriangles = //...

Map<String, String> indicesAtts = new HashMap<String, String>();
indicesAtts.put(DataArray.ATT_INTENT, DataArray.NIFTI_INTENT_TRIANGLE);
indicesAtts.put(DataArray.ATT_ARRAYINDEXINGORDER, DataArray.DIM_ORDER_ROWMAJORORDER);
indicesAtts.put(DataArray.ATT_DATATYPE, DataArray.TYPE_NIFTI_TYPE_INT32);
indicesAtts.put(DataArray.ATT_DIMENSIONALITY, "2");
indicesAtts.put(DataArray.ATT_DIMN + "0", "" + numTriangles);
indicesAtts.put(DataArray.ATT_DIMN + "1", "3");
indicesAtts.put(DataArray.ATT_ENCODING, DataArray.ENCODING_GZIPBASE64BINARY);
indicesAtts.put(DataArray.ATT_ENDIAN, (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) ? DataArray.DATA_ORDER_LITTLEENDIAN
				: DataArray.DATA_ORDER_BIGENDIAN);
indicesAtts.put(DataArray.ATT_ENCODING, DataArray.ENCODING_GZIPBASE64BINARY);

DataArray trianglesData = new DataArray(indicesAtts, triangles);
gifti.addDataArray(trianglesData);


// write it out
GiftiWriter writer = new GiftiWriter(gifti, file);

try {
    writer.writeGiftiXML();
} catch (GiftiFormatException gfe) {
    // do something with error...
}
```
