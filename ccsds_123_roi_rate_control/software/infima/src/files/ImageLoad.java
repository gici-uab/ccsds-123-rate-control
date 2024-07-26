/**
 * Copyright (C) 2013 - Francesc Auli-Llinas
 *
 * This program is distributed under the BOI License.
 * This program is distributed in the hope that it will be useful, but without any warranty; without even the implied warranty of merchantability or fitness for a particular purpose.
 * You should have received a copy of the BOI License along with this program. If not, see <http://www.deic.uab.cat/~francesc/software/license/>.
 */
package files;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;
import java.util.StringTokenizer;


/**
 * This class opens, reads, and returns (segments of) an image that is stored in a file. The format of the file must be either P5 (grayscale image) / P6 (color image) of the PNM family, or raw data. The class also allows the subtraction of a level offset, or a color transform, when reading the image samples from the file.<br>
 *
 * Usage: once the object is created, the header of the file is read and the function to <code>getImageChunk</code> is called to get image data. Also, the <code>get</code> functions can be called at any time to get information from the image file. Each object of this class manages one file.<br>
 *
 * Multithreading support: the object is created by a thread and then it can be shared by multiple threads. There can be many objects of this class running simultaneously (each for a different file).<br>
 *
 * @author Francesc Auli-Llinas
 * @version 2.2
 */
public final class ImageLoad{

	/**
	 * Coefficients of the irreversible color transform. Forward transform.
	 * <p>
	 * These coefficients transforms the RGB channels of a color image to YCbCr.
	 */
	 private final static float[][] IRREVERSIBLE_COLOR_TRANSFORM =
		{{0.299f, 0.587f, 0.114f},
		{-0.168736f, -0.331264f, 0.5f},
		{0.5f, -0.418688f, -0.081312f}};

	/**
	 * Input stream of the image file read.
	 * <p>
	 * It is initialized by the constructor of the class and is open at each call of <code>getImageChunk</code>.
	 */
	private String fileName = null;

	/**
	 * Offset of the file format header.
	 * <p>
	 * It is determined when opening the file.
	 */
	private int headerOffset = -1;

	/**
	 * Components of the image.
	 * <p>
	 * Values greater than zero only.
	 */
	private int zSize = -1;

	/**
	 * Image height.
	 * <p>
	 * Values greater than zero only.
	 */
	private int ySize = -1;

	/**
	 * Image width.
	 * <p>
	 * Values greater than zero only.
	 */
	private int xSize = -1;

	/**
	 * Data types allowed for the samples. This is the data type read from the file.
	 * <p>
	 * Data types allowed:
	 * <ul>
	 * <li>0 for boolean (1 byte, java primitive type boolean) --any value different from 0 is considered true, otherwise false--</li>
	 * <li>1 for unsigned integer (1 byte, java primitive type byte)</li>
	 * <li>2 for unsigned integer (2 bytes, java primitive type char)</li>
	 * <li>3 for signed integer (2 bytes, java primitive type short)</li>
	 * <li>4 for signed integer (4 bytes, java primitive type int)</li>
	 * <li>5 for signed integer (8 bytes, java primitive type long)</li>
	 * <li>6 for float (4 bytes, java primitive type float)</li>
	 * <li>7 for double (8 bytes, java primitive type double)</li>
	 * </ul>
	 */
	private int sampleType = -1;

	/**
	 * Indicates whether the data is signed or not. Note that this may not be in accordance with the sampleType (e.g., data stored in the file as float may be unsigned).
	 * <p>
	 * 0 indicates UNsigned data, 1 signed data.
	 */
	private int signedType = -1;

	/**
	 * Indicates the bit-depth of the data type. Note that this may not be in accordance with the sampleType (e.g., data stored in the file as float may have a bit-depth of 12 bps).
	 * <p>
	 * Values greater than zero only.
	 */
	private int bitDepth = -1;

	/**
	 * Byte order of the data.
	 * <p>
	 * 0 indicates big endian, 1 little endian
	 */
	private int byteOrder = -1;

	/**
	 * Order of the data.
	 * <p>
	 * 0 indicates that the data are organized first as rows, then columns and then components (i.e., the outer loop reads the components), 1 indicates that the data are organized as components, rows and columns (i.e., the inner loop reads the components)
	 */
	private int dataOrder = -1;

	/**
	 * Indicate whether the first three components of the image correspond to the red, green, and blue (RGB) colors (when relevant).
	 * <p>
	 * 1 indicates that they do correspond to RGB, 0 otherwise
	 */
	private int componentsRGB = -1;


	/**
	 * Reads the header of the image file when reading a PNM-like format or extracts the geometry of a raw file that is specified in the file name, and then initializes the structures of this class. This function uses the extension of the file to determine its format. For the raw files, its geometry is determined from its file name, which must include a subsequence like *.zSize_ySzie_xSize_sampleType_signedType_bitDepth_byteOrder_dataOrder_componentsRGB.*.
	 *
	 * @param fileName image file (supported formats are P5 and P6 from the PNM family and raw files with its geometry specified in the file name)
	 * @throws Exception when the file does not have a supported format or can not be read
	 */
	public ImageLoad(String fileName) throws Exception{
		//Checks the extension of the file and decides whether is is a PNM-like format, a raw file, or something not supported
		this.fileName = fileName;
		String extension = "";
		if(fileName.lastIndexOf(".") != -1){
			extension = fileName.substring(fileName.lastIndexOf("."), fileName.length());
		}

		if(extension.equalsIgnoreCase(".ppm") || extension.equalsIgnoreCase(".pgm")){
			//Open file to parse the header
			FileInputStream fis = new FileInputStream(fileName);
			BufferedInputStream bis = new BufferedInputStream(fis);
			BufferedReader br = new BufferedReader(new InputStreamReader(bis));

			//Header parsing
			String line;
			do{
				line = br.readLine();
			}while(line.charAt(0) == '#');

			StringTokenizer tokenizer = new StringTokenizer(line);
			String magicNumber = tokenizer.nextToken();

			if(magicNumber.equalsIgnoreCase("P5") || magicNumber.equalsIgnoreCase("P6")){
				if(magicNumber.equalsIgnoreCase("P5")){
					zSize = 1;
					dataOrder = 0;
					componentsRGB = 0;
				}else{
					zSize = 3;
					dataOrder = 1;
					componentsRGB = 1;
				}

				int reads = 0;
				do{
					while(tokenizer.hasMoreElements()){
						int readInteger = Integer.valueOf(tokenizer.nextToken());
						switch(reads){
						case 0:{
							xSize = readInteger;
						}break;
						case 1:{
							ySize = readInteger;
						}break;
						case 2:{
							if(readInteger == 255){ //only unsigned integer (1 byte) allowed
								sampleType = 1;
								signedType = 0;
								bitDepth = 8;
							}else{
								throw new Exception("File \"" + fileName + "\" does not have a valid data type (only unsigned integer of 1 byte are allowed).");
							}
						}}
						reads++;
					}
					if(reads <= 2){
						do{
							line = br.readLine();
						}while(line.charAt(0) == '#');
						tokenizer = new StringTokenizer(line);
					}
				}while(reads <= 2);

				br.close();
				bis.close();
				fis.close();

				//Sets the header offset
				FileChannel file = FileChannel.open(FileSystems.getDefault().getPath(fileName), StandardOpenOption.READ);
				headerOffset = (int) (file.size() - ((long) zSize * (long) ySize * (long) xSize));
				file.close();
			}else{
				throw new Exception("File \"" + fileName + "\" does not have a supported format. Supported formats are P5 (grayscale images) and P6 (color images) from the PNM family.");
			}

		}else if(extension.equalsIgnoreCase(".raw") || extension.equalsIgnoreCase(".img")){
			String[] subsequences = fileName.split("\\.");
			int i = 0;
			boolean foundGeometry = false;
			do{
				String[] geometry = subsequences[i].split("_");
				if(geometry.length == 9){
					headerOffset = 0;
					zSize = Integer.parseInt(geometry[0]);
					ySize = Integer.parseInt(geometry[1]);
					xSize = Integer.parseInt(geometry[2]);
					sampleType = Integer.parseInt(geometry[3]);
					signedType = Integer.parseInt(geometry[4]);
					bitDepth = Integer.parseInt(geometry[5]);
					byteOrder = Integer.parseInt(geometry[6]);
					dataOrder = Integer.parseInt(geometry[7]);
					componentsRGB = Integer.parseInt(geometry[8]);
					foundGeometry = true;
				}
				i++;
			}while((!foundGeometry) && (i < subsequences.length));
			if(!foundGeometry){
				throw new Exception("The name of the file \"" + fileName + "\" does not include a substring like *.zSize_ySzie_xSize_sampleType_signedType_bitDepth_byteOrder_dataOrder_componentsRGB.* from which to read the image geometry.");
			}

		}else{
			throw new Exception("File \"" + fileName + "\" does not have a supported format. Supported formats are P5 (grayscale images), P6 (color images) from the PNM family, and raw files with extension raw or img.");
		}
	}

	/**
	 * Initializes the structures of this class when reading a raw format with its geometry specified as the parameters of this function.
	 *
	 * @param fileName image file with extension .raw or .img
	 * @param geometry and integer array of 9 positions corresponding to the parameters of the <code>ImageLoad</code> constructor that accepts the geometry of the image as individual parameters
	 * @throws Exception when the file can not be open or read
	 */
	public ImageLoad(String fileName, int[] geometry) throws Exception{
		assert(geometry.length == 9);
		this.fileName = fileName;
		headerOffset = 0;
		zSize = geometry[0];
		ySize = geometry[1];
		xSize = geometry[2];
		sampleType = geometry[3];
		signedType = geometry[4];
		bitDepth = geometry[5];
		byteOrder = geometry[6];
		dataOrder = geometry[7];
		componentsRGB = geometry[8];
	}

	/**
	 * Initializes the structures of this class when reading a raw format with its geometry specified as the parameters of this function.
	 *
	 * @param fileName image file with extension .raw or .img
	 * @param zSize see {@link #zSize}
	 * @param ySize see {@link #ySize}
	 * @param xSize see {@link #xSize}
	 * @param sampleType see {@link #sampleType}
	 * @param signedType see {@link #signedType}
	 * @param bitDepth see {@link #bitDepth}
	 * @param byteOrder see {@link #byteOrder}
	 * @param dataOrder see {@link #dataOrder}
	 * @param componentsRGB see {@link #componentsRGB}
	 * @throws Exception when the file can not be open or read
	 */
	public ImageLoad(String fileName, int zSize, int ySize, int xSize, int sampleType, int signedType, int bitDepth, int byteOrder, int dataOrder, int componentsRGB) throws Exception{
		headerOffset = 0;
		this.fileName = fileName;
		this.zSize = zSize;
		this.ySize = ySize;
		this.xSize = xSize;
		this.sampleType = sampleType;
		this.signedType = signedType;
		this.bitDepth = bitDepth;
		this.byteOrder = byteOrder;
		this.dataOrder = dataOrder;
		this.componentsRGB = componentsRGB;
	}

	/**
	 * Reads a chunk of the image corresponding to the volume specified with the method's parameters. At the same time that data are read, a level offset or a color transform can be applied.
	 *
	 * @param zFirst front coordinate of the volume
	 * @param yFirst top coordinate of the volume
	 * @param xFirst right coordinate of the volume
	 * @param zLength depth of the volume
	 * @param yLength height of the volume
	 * @param xLength width of the volume
	 * @param levelOffset applies a level offset subtracting the value specified in this parameter (0 does nothing)
	 * @param colorTransform 0 does nothing, 1 and 2 applies an irreversible and reversible color transform, respectively, when the call to this function reads, at least, the three first components of the image and these components correspond to the red, green, and blue channels
	 * @return a 3D float array containing the image samples. The array is organized as [z][y][x]
	 * @throws Exception when the image chunk can not be read
	 */
	public float[][][] getImageChunk(int zFirst, int yFirst, int xFirst, int zLength, int yLength, int xLength, float levelOffset, int colorTransform) throws Exception{
		//Checks sizes
		if((zFirst < 0) || (zFirst >= zSize) || (yFirst < 0) || (yFirst >= ySize) || (xFirst < 0) || (xFirst >= xSize)){
			throw new Exception("The front-top-right coordinate of the volume is incorrect.");
		}
		if((zLength <= 0) || (zFirst + zLength > zSize) || (yLength <= 0) || (yFirst + yLength > ySize) || (xLength <= 0) || (xFirst + xLength > xSize)){
			throw new Exception("The size of the volume is incorrect.");
		}

		//Opens the file
		FileChannel file = FileChannel.open(FileSystems.getDefault().getPath(fileName), StandardOpenOption.READ);

		//Allocates memory
		float[][][] samples = new float[zLength][yLength][xLength];

		//Prepares the parameters for the buffer that performs the data conversion
		int multBytesType = ImageLoad.getTypeNumBytes(sampleType);
		ByteOrder byteOrderBuffer = ByteOrder.BIG_ENDIAN;
		if(byteOrder == 1){
			byteOrderBuffer = ByteOrder.LITTLE_ENDIAN;
		}

		//Reads data minimizing the number of readings from the file
		switch(dataOrder){
		case 0:{ //Image data ordered as ZYX

			if(xLength < xSize){ //ZYX data order, reading xLength samples at each iteration
				int bytesBuffer = xLength * multBytesType;
				ByteBuffer buffer = ByteBuffer.allocate(bytesBuffer);
				buffer = buffer.order(byteOrderBuffer);

				for(int z = zFirst; z < zFirst + zLength; z++){
				for(int y = yFirst; y < yFirst + yLength; y++){

					//Seeks and reads
					buffer.clear();
					file.position((long) headerOffset + ((long) z * (long) ySize * (long) xSize * (long) multBytesType) + ((long) y * (long) xSize * (long) multBytesType) + ((long) xFirst * (long) multBytesType));
					int readBytes = file.read(buffer);
					if(readBytes != bytesBuffer){
						throw new Exception("Volume reading error.");
					}
					buffer.position(0);

					//Converts data types
					switch(sampleType){
					case 0:{ //boolean (1 byte)
						for(int x = 0; x < xLength; x++){
							samples[z - zFirst][y - yFirst][x] = (buffer.get(x) == 0 ? 0.0F : 1.0F) - levelOffset;
						}
					}break;
					case 1:{ //unsigned int (1 byte)
						for(int x = 0; x < xLength; x++){
							samples[z - zFirst][y - yFirst][x] = (buffer.get(x) & 0xff) - levelOffset;
						}
					}break;
					case 2:{ //unsigned int (2 bytes)
						CharBuffer cb = buffer.asCharBuffer();
						for(int x = 0; x < xLength; x++){
							samples[z - zFirst][y - yFirst][x] = (cb.get(x) & 0xffff) - levelOffset;
						}
					}break;
					case 3:{ //signed short (2 bytes)
						ShortBuffer sb = buffer.asShortBuffer();
						for(int x = 0; x < xLength; x++){
							samples[z - zFirst][y - yFirst][x] = (sb.get(x)) - levelOffset;
						}
					}break;
					case 4:{ //signed int (4 bytes)
						IntBuffer ib = buffer.asIntBuffer();
						for(int x = 0; x < xLength; x++){
							samples[z - zFirst][y - yFirst][x] = (ib.get(x)) - levelOffset;
						}
					}break;
					case 5:{ //signed long (8 bytes)
						LongBuffer lb = buffer.asLongBuffer();
						for(int x = 0; x < xLength; x++){
							samples[z - zFirst][y - yFirst][x] = (lb.get(x)) - levelOffset;
						}
					}break;
					case 6:{ //float (4 bytes)
						FloatBuffer fb = buffer.asFloatBuffer();
						for(int x = 0; x < xLength; x++){
							samples[z - zFirst][y - yFirst][x] = (fb.get(x)) - levelOffset;
						}
					}break;
					case 7:{ //double (8 bytes) - lost of precision
						DoubleBuffer db = buffer.asDoubleBuffer();
						for(int x = 0; x < xLength; x++){
							samples[z - zFirst][y - yFirst][x] = ((float) db.get(x)) - levelOffset;
						}
					}}
				}}

			}else{

				if(yLength < ySize){ //ZYX data order, reading yLength * xLength samples at each iteration
					int bytesBuffer = xLength * yLength * multBytesType;
					ByteBuffer buffer = ByteBuffer.allocate(bytesBuffer);
					buffer = buffer.order(byteOrderBuffer);

					for(int z = zFirst; z < zFirst + zLength; z++){

						//Seeks and reads
						buffer.clear();
						file.position((long) headerOffset + ((long) z * (long) ySize * (long) xSize * (long) multBytesType) + ((long) yFirst * (long) xSize * (long) multBytesType));
						int readBytes = file.read(buffer);
						if(readBytes != bytesBuffer){
							throw new Exception("Volume reading error.");
						}
						buffer.position(0);

						//Converts data types
						switch(sampleType){
						case 0:{ //boolean (1 byte)
							int i = 0;
							for(int y = 0; y < yLength; y++){
							for(int x = 0; x < xLength; x++){
								samples[z - zFirst][y][x] = (buffer.get(i) == 0 ? 0.0F : 1.0F) - levelOffset;
								i++;
							}}
						}break;
						case 1:{ //unsigned int (1 byte)
							int i = 0;
							for(int y = 0; y < yLength; y++){
							for(int x = 0; x < xLength; x++){
								samples[z - zFirst][y][x] = (buffer.get(i) & 0xff) - levelOffset;
								i++;
							}}
						}break;
						case 2:{ //unsigned int (2 bytes)
							int i = 0;
							CharBuffer cb = buffer.asCharBuffer();
							for(int y = 0; y < yLength; y++){
							for(int x = 0; x < xLength; x++){
								samples[z - zFirst][y][x] = (cb.get(i) & 0xffff) - levelOffset;
								i++;
							}}
						}break;
						case 3:{ //signed short (2 bytes)
							int i = 0;
							ShortBuffer sb = buffer.asShortBuffer();
							for(int y = 0; y < yLength; y++){
							for(int x = 0; x < xLength; x++){
								samples[z - zFirst][y][x] = (sb.get(i)) - levelOffset;
								i++;
							}}
						}break;
						case 4:{ //signed int (4 bytes)
							int i = 0;
							IntBuffer ib = buffer.asIntBuffer();
							for(int y = 0; y < yLength; y++){
							for(int x = 0; x < xLength; x++){
								samples[z - zFirst][y][x] = (ib.get(i)) - levelOffset;
								i++;
							}}
						}break;
						case 5:{ //signed long (8 bytes)
							int i = 0;
							LongBuffer lb = buffer.asLongBuffer();
							for(int y = 0; y < yLength; y++){
							for(int x = 0; x < xLength; x++){
								samples[z - zFirst][y][x] = (lb.get(i)) - levelOffset;
								i++;
							}}
						}break;
						case 6:{ //float (4 bytes)
							int i = 0;
							FloatBuffer fb = buffer.asFloatBuffer();
							for(int y = 0; y < yLength; y++){
							for(int x = 0; x < xLength; x++){
								samples[z - zFirst][y][x] = (fb.get(i)) - levelOffset;
								i++;
							}}
						}break;
						case 7:{ //double (8 bytes) - lost of precision
							int i = 0;
							DoubleBuffer db = buffer.asDoubleBuffer();
							for(int y = 0; y < yLength; y++){
							for(int x = 0; x < xLength; x++){
								samples[z - zFirst][y][x] = ((float) db.get(i)) - levelOffset;
								i++;
							}}
						}}
					}

				}else{ //ZYX data order, reading the whole volume at once
					int bytesBuffer = xLength * yLength * zLength * multBytesType;
					ByteBuffer buffer = ByteBuffer.allocate(bytesBuffer);
					buffer = buffer.order(byteOrderBuffer);

					//Seeks and reads
					buffer.clear();
					file.position((long) headerOffset + ((long) zFirst * (long) ySize * (long) xSize * (long) multBytesType));
					int readBytes = file.read(buffer);
					if(readBytes != bytesBuffer){
						throw new Exception("Volume reading error.");
					}
					buffer.position(0);

					//Converts data types
					switch(sampleType){
					case 0:{ //boolean (1 byte)
						int i = 0;
						for(int z = 0; z < zLength; z++){
						for(int y = 0; y < yLength; y++){
						for(int x = 0; x < xLength; x++){
							samples[z][y][x] = (buffer.get(i) == 0 ? 0.0F : 1.0F) - levelOffset;
							i++;
						}}}
					}break;
					case 1:{ //unsigned int (1 byte)
						int i = 0;
						for(int z = 0; z < zLength; z++){
						for(int y = 0; y < yLength; y++){
						for(int x = 0; x < xLength; x++){
							samples[z][y][x] = (buffer.get(i) & 0xff) - levelOffset;
							i++;
						}}}
					}break;
					case 2:{ //unsigned int (2 bytes)
						int i = 0;
						CharBuffer cb = buffer.asCharBuffer();
						for(int z = 0; z < zLength; z++){
						for(int y = 0; y < yLength; y++){
						for(int x = 0; x < xLength; x++){
							samples[z][y][x] = (cb.get(i) & 0xffff) - levelOffset;
							i++;
						}}}
					}break;
					case 3:{ //signed short (2 bytes)
						int i = 0;
						ShortBuffer sb = buffer.asShortBuffer();
						for(int z = 0; z < zLength; z++){
						for(int y = 0; y < yLength; y++){
						for(int x = 0; x < xLength; x++){
							samples[z][y][x] = (sb.get(i)) - levelOffset;
							i++;
						}}}
					}break;
					case 4:{ //signed int (4 bytes)
						int i = 0;
						IntBuffer ib = buffer.asIntBuffer();
						for(int z = 0; z < zLength; z++){
						for(int y = 0; y < yLength; y++){
						for(int x = 0; x < xLength; x++){
							samples[z][y][x] = (ib.get(i)) - levelOffset;
							i++;
						}}}
					}break;
					case 5:{ //signed long (8 bytes)
						int i = 0;
						LongBuffer lb = buffer.asLongBuffer();
						for(int z = 0; z < zLength; z++){
						for(int y = 0; y < yLength; y++){
						for(int x = 0; x < xLength; x++){
							samples[z][y][x] = (lb.get(i)) - levelOffset;
							i++;
						}}}
					}break;
					case 6:{ //float (4 bytes)
						int i = 0;
						FloatBuffer fb = buffer.asFloatBuffer();
						for(int z = 0; z < zLength; z++){
						for(int y = 0; y < yLength; y++){
						for(int x = 0; x < xLength; x++){
							samples[z][y][x] = (fb.get(i)) - levelOffset;
							i++;
						}}}
					}break;
					case 7:{ //double (8 bytes) - lost of precision
						int i = 0;
						DoubleBuffer db = buffer.asDoubleBuffer();
						for(int z = 0; z < zLength; z++){
						for(int y = 0; y < yLength; y++){
						for(int x = 0; x < xLength; x++){
							samples[z][y][x] = ((float) db.get(i)) - levelOffset;
							i++;
						}}}
					}}
				}
			}
		}break;

		case 1:{ //Image data ordered as YXZ

			if(zLength < zSize){ //YXZ data order, reading zLength samples at each iteration
				int bytesBuffer = zLength * multBytesType;
				ByteBuffer buffer = ByteBuffer.allocate(bytesBuffer);
				buffer = buffer.order(byteOrderBuffer);

				for(int y = yFirst; y < yFirst + yLength; y++){
				for(int x = xFirst; x < xFirst + xLength; x++){

					//Seeks and reads
					buffer.clear();
					file.position((long) headerOffset + ((long) y * (long) zSize * (long) xSize * (long) multBytesType) + ((long) x * (long) zSize * (long) multBytesType) + ((long) zFirst * (long) multBytesType));
					int readBytes = file.read(buffer);
					if(readBytes != bytesBuffer){
						throw new Exception("Volume reading error.");
					}
					buffer.position(0);

					//Converts data types
					switch(sampleType){
					case 0:{ //boolean (1 byte)
						for(int z = 0; z < zLength; z++){
							samples[z][y - yFirst][x - xFirst] = (buffer.get(z) == 0 ? 0.0F : 1.0F) - levelOffset;
						}
					}break;
					case 1:{ //unsigned int (1 byte)
						for(int z = 0; z < zLength; z++){
							samples[z][y - yFirst][x - xFirst] = (buffer.get(z) & 0xff) - levelOffset;
						}
					}break;
					case 2:{ //unsigned int (2 bytes)
						CharBuffer cb = buffer.asCharBuffer();
						for(int z = 0; z < zLength; z++){
							samples[z][y - yFirst][x - xFirst] = (cb.get(z) & 0xffff) - levelOffset;
						}
					}break;
					case 3:{ //signed short (2 bytes)
						ShortBuffer sb = buffer.asShortBuffer();
						for(int z = 0; z < zLength; z++){
							samples[z][y - yFirst][x - xFirst] = (sb.get(z)) - levelOffset;
						}
					}break;
					case 4:{ //signed int (4 bytes)
						IntBuffer ib = buffer.asIntBuffer();
						for(int z = 0; z < zLength; z++){
							samples[z][y - yFirst][x - xFirst] = (ib.get(z)) - levelOffset;
						}
					}break;
					case 5:{ //signed long (8 bytes)
						LongBuffer lb = buffer.asLongBuffer();
						for(int z = 0; z < zLength; z++){
							samples[z][y - yFirst][x - xFirst] = (lb.get(z)) - levelOffset;
						}
					}break;
					case 6:{ //float (4 bytes)
						FloatBuffer fb = buffer.asFloatBuffer();
						for(int z = 0; z < zLength; z++){
							samples[z][y - yFirst][x - xFirst] = (fb.get(z)) - levelOffset;
						}
					}break;
					case 7:{ //double (8 bytes) - lost of precision
						DoubleBuffer db = buffer.asDoubleBuffer();
						for(int z = 0; z < zLength; z++){
							samples[z][y - yFirst][x - xFirst] = ((float) db.get(z)) - levelOffset;
						}
					}}
				}}

			}else{

				if(xLength < xSize){ //YXZ data order, reading zLength * xLength samples at each iteration
					int bytesBuffer = zLength * xLength * multBytesType;
					ByteBuffer buffer = ByteBuffer.allocate(bytesBuffer);
					buffer = buffer.order(byteOrderBuffer);

					for(int y = yFirst; y < yFirst + yLength; y++){

						//Seeks and reads
						buffer.clear();
						file.position((long) headerOffset + ((long) y * (long) zSize * (long) xSize * (long) multBytesType) + ((long) xFirst * (long) zSize * (long) multBytesType));
						int readBytes = file.read(buffer);
						if(readBytes != bytesBuffer){
							throw new Exception("Volume reading error.");
						}
						buffer.position(0);

						//Converts data types
						switch(sampleType){
						case 0:{ //boolean (1 byte)
							int i = 0;
							for(int x = 0; x < xLength; x++){
							for(int z = 0; z < zLength; z++){
								samples[z][y - yFirst][x] = (buffer.get(i) == 0 ? 0.0F : 1.0F) - levelOffset;
								i++;
							}}
						}break;
						case 1:{ //unsigned int (1 byte)
							int i = 0;
							for(int x = 0; x < xLength; x++){
							for(int z = 0; z < zLength; z++){
								samples[z][y - yFirst][x] = (buffer.get(i) & 0xff) - levelOffset;
								i++;
							}}
						}break;
						case 2:{ //unsigned int (2 bytes)
							int i = 0;
							CharBuffer cb = buffer.asCharBuffer();
							for(int x = 0; x < xLength; x++){
							for(int z = 0; z < zLength; z++){
								samples[z][y - yFirst][x] = (cb.get(i) & 0xffff) - levelOffset;
								i++;
							}}
						}break;
						case 3:{ //signed short (2 bytes)
							int i = 0;
							ShortBuffer sb = buffer.asShortBuffer();
							for(int x = 0; x < xLength; x++){
							for(int z = 0; z < zLength; z++){
								samples[z][y - yFirst][x] = (sb.get(i)) - levelOffset;
								i++;
							}}
						}break;
						case 4:{ //signed int (4 bytes)
							int i = 0;
							IntBuffer ib = buffer.asIntBuffer();
							for(int x = 0; x < xLength; x++){
							for(int z = 0; z < zLength; z++){
								samples[z][y - yFirst][x] = (ib.get(i)) - levelOffset;
								i++;
							}}
						}break;
						case 5:{ //signed long (8 bytes)
							int i = 0;
							LongBuffer lb = buffer.asLongBuffer();
							for(int x = 0; x < xLength; x++){
							for(int z = 0; z < zLength; z++){
								samples[z][y - yFirst][x] = (lb.get(i)) - levelOffset;
								i++;
							}}
						}break;
						case 6:{ //float (4 bytes)
							int i = 0;
							FloatBuffer fb = buffer.asFloatBuffer();
							for(int x = 0; x < xLength; x++){
							for(int z = 0; z < zLength; z++){
								samples[z][y - yFirst][x] = (fb.get(i)) - levelOffset;
								i++;
							}}
						}break;
						case 7:{ //double (8 bytes) - lost of precision
							int i = 0;
							DoubleBuffer db = buffer.asDoubleBuffer();
							for(int x = 0; x < xLength; x++){
							for(int z = 0; z < zLength; z++){
								samples[z][y - yFirst][x] = ((float) db.get(i)) - levelOffset;
								i++;
							}}
						}}
					}

				}else{ //YXZ data order, reading the whole volume at once
					int bytesBuffer = zLength * xLength * yLength * multBytesType;
					ByteBuffer buffer = ByteBuffer.allocate(bytesBuffer);
					buffer = buffer.order(byteOrderBuffer);

					//Seeks and reads
					buffer.clear();
					file.position((long) headerOffset + ((long) yFirst * (long) zSize * (long) xSize * (long) multBytesType));
					int readBytes = file.read(buffer);
					if(readBytes != bytesBuffer){
						throw new Exception("Volume reading error.");
					}
					buffer.position(0);

					//Converts data types
					switch(sampleType){
					case 0:{ //boolean (1 byte)
						int i = 0;
						for(int y = 0; y < yLength; y++){
						for(int x = 0; x < xLength; x++){
						for(int z = 0; z < zLength; z++){
							samples[z][y][x] = (buffer.get(i) == 0 ? 0.0F : 1.0F) - levelOffset;
						}}}
					}break;
					case 1:{ //unsigned int (1 byte)
						int i = 0;
						for(int y = 0; y < yLength; y++){
						for(int x = 0; x < xLength; x++){
						for(int z = 0; z < zLength; z++){
							samples[z][y][x] = (buffer.get(i) & 0xff) - levelOffset;
							i++;
						}}}
					}break;
					case 2:{ //unsigned int (2 bytes)
						int i = 0;
						CharBuffer cb = buffer.asCharBuffer();
						for(int y = 0; y < yLength; y++){
						for(int x = 0; x < xLength; x++){
						for(int z = 0; z < zLength; z++){
							samples[z][y][x] = (cb.get(i) & 0xffff) - levelOffset;
							i++;
						}}}
					}break;
					case 3:{ //signed short (2 bytes)
						int i = 0;
						ShortBuffer sb = buffer.asShortBuffer();
						for(int y = 0; y < yLength; y++){
						for(int x = 0; x < xLength; x++){
						for(int z = 0; z < zLength; z++){
							samples[z][y][x] = (sb.get(i)) - levelOffset;
							i++;
						}}}
					}break;
					case 4:{ //signed int (4 bytes)
						int i = 0;
						IntBuffer ib = buffer.asIntBuffer();
						for(int y = 0; y < yLength; y++){
						for(int x = 0; x < xLength; x++){
						for(int z = 0; z < zLength; z++){
							samples[z][y][x] = (ib.get(i)) - levelOffset;
							i++;
						}}}
					}break;
					case 5:{ //signed long (8 bytes)
						int i = 0;
						LongBuffer lb = buffer.asLongBuffer();
						for(int y = 0; y < yLength; y++){
						for(int x = 0; x < xLength; x++){
						for(int z = 0; z < zLength; z++){
							samples[z][y][x] = (lb.get(i)) - levelOffset;
							i++;
						}}}
					}break;
					case 6:{ //float (4 bytes)
						int i = 0;
						FloatBuffer fb = buffer.asFloatBuffer();
						for(int y = 0; y < yLength; y++){
						for(int x = 0; x < xLength; x++){
						for(int z = 0; z < zLength; z++){
							samples[z][y][x] = (fb.get(i)) - levelOffset;
							i++;
						}}}
					}break;
					case 7:{ //double (8 bytes) - lost of precision
						int i = 0;
						DoubleBuffer db = buffer.asDoubleBuffer();
						for(int y = 0; y < yLength; y++){
						for(int x = 0; x < xLength; x++){
						for(int z = 0; z < zLength; z++){
							samples[z][y][x] = ((float) db.get(i)) - levelOffset;
							i++;
						}}}
					}}
				}
			}
		}break;
		default:{
			throw new Exception("Unrecognized data order.");
		}}

		file.close();

		//Applies the color transform
		if(colorTransform != 0){
			if(componentsRGB == 0){
				throw new Exception("The color transform can only be applied when the first three components correspond to the RGB channels.");
			}
			if((zFirst != 0) || (zLength < 3)){
				throw new Exception("The color transform can only be applied when reading, at least, the first three components of the image.");
			}
			if(colorTransform == 1){ //irreversible
				for(int y = 0; y < yLength; y++){
				for(int x = 0; x < xLength; x++){
					float c1 = samples[0][y][x];
					float c2 = samples[1][y][x];
					float c3 = samples[2][y][x];
					for(int z = 0; z < 3; z++){
						samples[z][y][x] = c1 * IRREVERSIBLE_COLOR_TRANSFORM[z][0] + c2 * IRREVERSIBLE_COLOR_TRANSFORM[z][1] + c3 * IRREVERSIBLE_COLOR_TRANSFORM[z][2];
					}
				}}
			}else if(colorTransform == 2){ //reversible
				for(int y = 0; y < yLength; y++){
				for(int x = 0; x < xLength; x++){
					float c1 = samples[0][y][x];
					float c2 = samples[1][y][x];
					float c3 = samples[2][y][x];
					samples[0][y][x] = (float) Math.floor((c1 + c2 * 2f + c3) / 4f);
					samples[1][y][x] = c3 - c2;
					samples[2][y][x] = c1 - c2;
				}}
			}else{
				throw new Exception("Unrecognized color transform.");
			}
		}

		return(samples);
	}

	/**
	 * @return {@link #zSize}
	 */
	public int getZSize(){
		return(zSize);
	}

	/**
	 * @return {@link #ySize}
	 */
	public int getYSize(){
		return(ySize);
	}

	/**
	 * @return {@link #xSize}
	 */
	public int getXSize(){
		return(xSize);
	}

	/**
	 * @return {@link #sampleType}
	 */
	public int getSampleType(){
		return(sampleType);
	}

	/**
	 * @return {@link #signedType}
	 */
	public int getSignedType(){
		return(signedType);
	}

	/**
	 * @return {@link #bitDepth}
	 */
	public int getBitDepth(){
		return(bitDepth);
	}

	/**
	 * @return {@link #byteOrder}
	 */
	public int getByteOrder(){
		return(byteOrder);
	}

	/**
	 * @return {@link #dataOrder}
	 */
	public int getDataOrder(){
		return(dataOrder);
	}

	/**
	 * @return {@link #componentsRGB}
	 */
	public int getComponentsRGB(){
		return(componentsRGB);
	}

	/**
	 * Returns the number of bytes required by a sample type.
	 *
	 * @param sampleType the sample type
	 * @return the number of bytes
	 * @throws Exception when the data type is not supported
	 */
	public static int getTypeNumBytes(int sampleType) throws Exception{
		int numBytes = 1;
		switch(sampleType){
		case 0: //boolean (1 byte, java primitive type boolean)
		case 1:{ //unsigned integer (1 byte, java primitive type byte)
		}break;
		case 2:{ //unsigned integer (2 bytes, java primitive type char)
			numBytes *= 2;
		}break;
		case 3:{ //signed integer (2 bytes, java primitive type short)
			numBytes *= 2;
		}break;
		case 4:{ //signed integer (4 bytes, java primitive type int)
			numBytes *= 4;
		}break;
		case 5:{ //signed integer (8 bytes, java primitive type long)
			numBytes *= 8;
		}break;
		case 6:{ //float (4 bytes, java primitive type float)
			numBytes *= 4;
		}break;
		case 7:{ //double (8 bytes, java primitive type double)
			numBytes *= 8;
		}break;
		default:{
			throw new Exception("Data type not supported.");
		}}
		return(numBytes);
	}
}