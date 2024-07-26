/*
 * GICI Library -
 * Copyright (C) 2011  Group on Interactive Coding of Images (GICI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Group on Interactive Coding of Images (GICI)
 * Department of Information and Communication Engineering
 * Autonomous University of Barcelona
 * 08193 - Bellaterra - Cerdanyola del Valles (Barcelona)
 * Spain
 *
 * http://gici.uab.es
 * gici-info@deic.uab.es
 */
package GiciFile.RawImage;

import java.nio.*;
import GiciException.LackOfPrecisionError;

/**
 * This class cast an array of elements represented in byte[] to int[], long[] or float[] and vice versa.
 * @see Geometry
 */
public class TypeConverter <T> {

	//Type

	/**
	 * Indicates that the conversion is to or from int[]
	 */
	public final static int INT_ARRAY = 0;

	/**
	 * Indicates that the conversion is to or from long[]
	 */
	public final static int LONG_ARRAY = 1;

	/**
	 * Indicates that the conversion is to or from float[]
	 */
	public final static int FLOAT_ARRAY = 2;
	
	/**
	 * Represents the geometry of a image in BSQ mode.
	 * @see Geometry
	 */
	private int[] geo;

	/**
	 * Indicates if the conversion of original to desired sample type has to be lossless.
	 */
	private boolean lossless;

	/**
	 *Indicates that the conversion is to or from this type (see "Type" above)
	 */
	private int type;

	/**
	 * The size of the original type of pixels.
	 */
	private int size;

	/**
	 * Constructor that obtain the necessary parameters.
	 * @param t represents the type of array that the conversion is to or from, for example, new int[0].
	 * @param geo is the geometry of the image in BSQ mode.
	 * @param lossless indicates if the conversion of original to desired sample type has to be lossless. If it is true and occurs
	 * a problem of type conversion, a LackOfPrecisionError will be thrown.
	 * @exception ClassCastException if the Type of T is not supported.
	 * @see Geometry
	 */
	public TypeConverter (T t, int[] geo, boolean lossless) throws ClassCastException {
		if(t instanceof int[]) {
			type = INT_ARRAY;
		}else if(t instanceof long[]) {
			type = LONG_ARRAY;
		}else if(t instanceof float[]) {
			type = FLOAT_ARRAY;
		}else {
			throw new ClassCastException("The only types accepted are int[], long[] and float[]");
		}
		this.geo = geo;
		this.lossless = lossless;
		int[] sizeTable = {1/* boolean */, 1/* byte */, 2/* char */, 2/* short */, 4/* int */, 8/* long */, 4/* float */, 8/* double */};
		size = sizeTable[geo[Geometry.SAMPLE_TYPE]];
	}

	/**
	 * Test if d is integer.
	 * @param d a real number.
	 * @return if d is integer.
	 */
	private static boolean isInteger(double d) {
		long l = (long) d;
		return l == d;
	}

	/**
	 * Test if d fits in float.
	 * @param d a real number.
	 * @return if d is f fits in float.
	 */
	private static boolean fitsInFloat(double d) {
		float f = (float) d;
		return f == d;
	}

	/**
	 * Test if l fits in float.
	 * @param l an integer number.
	 * @return if l is f fits in float.
	 */
	private static boolean fitsInFloat(long l) {
		float f = l;
		return (long) f == l;
	}

	/**
	 * Test if l fits in double.
	 * @param l an integer number.
	 * @return if l is f fits in double.
	 */
	private static boolean fitsInDouble(long l) {
		double d = l;
		return (long) d == l;
	}

	/**
	 * This class wrap an array into a ByteBuffer.
	 */
	private abstract static class BufferWriter {
		/**
		 * Wrap an array into a ByteBuffer
		 * @param buffer the bytebuffer in which the values are put.
		 * @param sampleType the real sample type of elements of array.
		 * @see Geometry
		 */
		public abstract void writeBuffer(ByteBuffer buffer, final int sampleType);
	}

	/**
	 * This class wrap a int[] into a ByteBuffer.
	 */
	private static class IntWriter extends BufferWriter {
		/**
		 * The array that will be put in ByteBuffer
		 */
		private int[] imageSamples;

		/**
		 * Indicates if the conversion of original to desired sample type has to be lossless.
		 */
		private boolean lossless;

		/**
		 * Constructor that obtain the necessary parameters.
		 * @param imageSamples is the array that will be put in ByteBuffer.
		 * @param lossless indicates if the conversion of original to desired sample type has to be lossless. If it is true and occurs
		 * a problem of type conversion, a LackOfPrecisionError will be thrown.
		 */
		public IntWriter(int[] imageSamples, boolean lossless) {
			this.imageSamples = imageSamples;
			this.lossless = lossless;
		}

		/**
		 * Wrap a int[] into a ByteBuffer
		 * @param buffer the bytebuffer in which the values are put.
		 * @param sampleType the real sample type of elements of array.
		 * @see Geometry
		 */
		public void writeBuffer(ByteBuffer buffer, final int sampleType) {
			switch(sampleType){
				case Geometry.BOOLEAN:
					for(int i=0;i<imageSamples.length;i++) {
						buffer.put((byte) (imageSamples[i] == 0 ? 0 : 1));
					}
					break;
				case Geometry.U_BYTE:
					for(int i=0;i<imageSamples.length;i++) {
						if(imageSamples[i] < 0 || imageSamples[i] > 255) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						buffer.put((byte)imageSamples[i]);
					}
					break;
				case Geometry.U_SHORT:
					for(int i=0;i<imageSamples.length;i++) {
						if(imageSamples[i] < Character.MIN_VALUE  || imageSamples[i] > Character.MAX_VALUE ) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						buffer.putChar((char)imageSamples[i]);
					}
					break;
				case Geometry.SHORT:
					for(int i=0;i<imageSamples.length;i++) {
						if(imageSamples[i] < Short.MIN_VALUE  || imageSamples[i] > Short.MAX_VALUE ) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						buffer.putShort((short) imageSamples[i]);
					}
					break;
				case Geometry.INT:
					for(int i=0;i<imageSamples.length;i++) {
						buffer.putInt(imageSamples[i]);
					}
					break;
				case Geometry.LONG:
					for(int i=0;i<imageSamples.length;i++) {
						buffer.putLong(imageSamples[i]);
					}
					break;
				case Geometry.FLOAT:
					for(int i=0;i<imageSamples.length;i++) {
						if(!fitsInFloat(imageSamples[i]) && lossless) {
							throw new LackOfPrecisionError("Lack of precision with one pixel");
						}
						buffer.putFloat(imageSamples[i]);
					}
					break;
				case Geometry.DOUBLE:
					for(int i=0;i<imageSamples.length;i++) {
						buffer.putDouble(imageSamples[i]);
					}
					break;
			}
		}
	};

	/**
	 * This class wrap a long[] into a ByteBuffer.
	 */
	private static class LongWriter extends BufferWriter {
		/**
		 * The array that will be put in ByteBuffer
		 */
		private long[] imageSamples;

		/**
		 * Indicates if the conversion of original to desired sample type has to be lossless.
		 */
		private boolean lossless;

		/**
		 * Constructor that obtain the necessary parameters.
		 * @param imageSamples is the array that will be put in ByteBuffer.
		 * @param lossless indicates if the conversion of original to desired sample type has to be lossless. If it is true and occurs
		 * a problem of type conversion, a LackOfPrecisionError will be thrown.
		 */
		public LongWriter(long[] imageSamples, boolean lossless) {
			this.imageSamples = imageSamples;
			this.lossless = lossless;
		}

		/**
		 * Wrap a long[] into a ByteBuffer
		 * @param buffer the bytebuffer in which the values are put.
		 * @param sampleType the real sample type of elements of array.
		 * @see Geometry
		 */
		public void writeBuffer(ByteBuffer buffer, final int sampleType) {
			switch(sampleType){
				case Geometry.BOOLEAN:
					for(int i=0;i<imageSamples.length;i++) {
						buffer.put((byte) (imageSamples[i] == 0 ? 0 : 1));
					}
					break;
				case Geometry.U_BYTE:
					for(int i=0;i<imageSamples.length;i++) {
						if(imageSamples[i] < 0 || imageSamples[i] > 255) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						buffer.put((byte)imageSamples[i]);
					}
					break;
				case Geometry.U_SHORT:
					for(int i=0;i<imageSamples.length;i++) {
						if(imageSamples[i] < Character.MIN_VALUE  || imageSamples[i] > Character.MAX_VALUE ) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						buffer.putChar((char)imageSamples[i]);
					}
					break;
				case Geometry.SHORT:
					for(int i=0;i<imageSamples.length;i++) {
						if(imageSamples[i] < Short.MIN_VALUE  || imageSamples[i] > Short.MAX_VALUE ) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						buffer.putShort((short) imageSamples[i]);
					}
					break;
				case Geometry.INT:
					for(int i=0;i<imageSamples.length;i++) {
						if(imageSamples[i] < Integer.MIN_VALUE  || imageSamples[i] > Integer.MAX_VALUE ) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						buffer.putInt((int)imageSamples[i]);
					}
					break;
				case Geometry.LONG:
					for(int i=0;i<imageSamples.length;i++) {
						buffer.putLong(imageSamples[i]);
					}
					break;
				case Geometry.FLOAT:
					for(int i=0;i<imageSamples.length;i++) {
						if(!fitsInFloat(imageSamples[i]) && lossless) {
							throw new LackOfPrecisionError("Lack of precision with one pixel");
						}
						buffer.putFloat(imageSamples[i]);
					}
					break;
				case Geometry.DOUBLE:
					for(int i=0;i<imageSamples.length;i++) {
						if(!fitsInDouble(imageSamples[i]) && lossless) {
							throw new LackOfPrecisionError("Lack of precision with one pixel");
						}
						buffer.putDouble(imageSamples[i]);
					}
					break;
			}
		}
	};

	/**
	 * This class wrap a float[] into a ByteBuffer.
	 */
	private static class FloatWriter extends BufferWriter {
		/**
		 * The array that will be put in ByteBuffer
		 */
		private float[] imageSamples;

		/**
		 * Indicates if the conversion of original to desired sample type has to be lossless.
		 */
		private boolean lossless;

		/**
		 * Constructor that obtain the necessary parameters.
		 * @param imageSamples is the array that will be put in ByteBuffer.
		 * @param lossless indicates if the conversion of original to desired sample type has to be lossless. If it is true and occurs
		 * a problem of type conversion, a LackOfPrecisionError will be thrown.
		 */
		public FloatWriter(float[] imageSamples, boolean lossless) {
			this.imageSamples = imageSamples;
			this.lossless = lossless;
		}

		/**
		 * Wrap a float[] into a ByteBuffer
		 * @param buffer the bytebuffer in which the values are put.
		 * @param sampleType the real sample type of elements of array.
		 * @see Geometry
		 */
		public void writeBuffer(ByteBuffer buffer, final int sampleType) {
			switch(sampleType){
				case Geometry.BOOLEAN:
					for(int i=0;i<imageSamples.length;i++) {
						buffer.put((byte) (imageSamples[i] == 0 ? 0 : 1));
					}
					break;
				case Geometry.U_BYTE:
					for(int i=0;i<imageSamples.length;i++) {
						if(imageSamples[i] < 0 || imageSamples[i] > 255) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						if(!isInteger(imageSamples[i]) && lossless) {
							throw new LackOfPrecisionError("Lack of precision with one pixel");
						}
						buffer.put((byte)imageSamples[i]);
					}
					break;
				case Geometry.U_SHORT:
					for(int i=0;i<imageSamples.length;i++) {
						if(imageSamples[i] < Character.MIN_VALUE  || imageSamples[i] > Character.MAX_VALUE ) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						if(!isInteger(imageSamples[i]) && lossless) {
							throw new LackOfPrecisionError("Lack of precision with one pixel");
						}
						buffer.putChar((char)imageSamples[i]);
					}
					break;
				case Geometry.SHORT:
					for(int i=0;i<imageSamples.length;i++) {
						if(imageSamples[i] < Short.MIN_VALUE  || imageSamples[i] > Short.MAX_VALUE ) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						if(!isInteger(imageSamples[i]) && lossless) {
							throw new LackOfPrecisionError("Lack of precision with one pixel");
						}
						buffer.putShort((short) imageSamples[i]);
					}
					break;
				case Geometry.INT:
					for(int i=0;i<imageSamples.length;i++) {
						if(imageSamples[i] < Integer.MIN_VALUE  || imageSamples[i] > Integer.MAX_VALUE ) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						if(!isInteger(imageSamples[i]) && lossless) {
							throw new LackOfPrecisionError("Lack of precision with one pixel");
						}
						buffer.putInt((int) imageSamples[i]);
					}
					break;
				case Geometry.LONG:
					for(int i=0;i<imageSamples.length;i++) {
						if(imageSamples[i] < Long.MIN_VALUE  || imageSamples[i] > Long.MAX_VALUE ) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						if(!isInteger(imageSamples[i]) && lossless) {
							throw new LackOfPrecisionError("Lack of precision with one pixel");
						}
						buffer.putLong((long) imageSamples[i]);
					}
					break;
				case Geometry.FLOAT:
					for(int i=0;i<imageSamples.length;i++) {
						buffer.putFloat(imageSamples[i]);
					}
					break;
				case Geometry.DOUBLE:
					for(int i=0;i<imageSamples.length;i++) {
						buffer.putDouble(imageSamples[i]);
					}
					break;
			}
		}
	};

	/**
	 * Cast a int[], long[] or float[] to an array of elements represented in byte[].
	 * @param t is the array that will be cast 
	 * @return a byte[] with the elements.
	 */
	public byte[] TtoByte(T t) {
		ByteBuffer buffer = null;
		BufferWriter writer = null;
		switch(type) {
			case INT_ARRAY:
				int[] intTmp = (int[]) t;
				writer = new IntWriter(intTmp, lossless);
				buffer = ByteBuffer.allocate(size*intTmp.length);
				break;
			case LONG_ARRAY:
				long[] longTmp = (long[]) t;
				writer = new LongWriter(longTmp, lossless);
				buffer = ByteBuffer.allocate(size*longTmp.length);
				break;
			case FLOAT_ARRAY:
				float[] floatTmp = (float[]) t;
				writer = new FloatWriter(floatTmp, lossless);
				buffer = ByteBuffer.allocate(size*floatTmp.length);
				break;
		}

		switch(geo[Geometry.BYTE_ORDER]){
			case Geometry.BIG_ENDIAN:
				buffer = buffer.order(ByteOrder.BIG_ENDIAN);
				break;
			case Geometry.LITTLE_ENDIAN:
				buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);
				break;
		}
		writer.writeBuffer(buffer, geo[Geometry.SAMPLE_TYPE]);
		return buffer.array();
	}

	/**
	 * This class extract the elements of ByteBuffer and put it in array.
	 */
	private abstract static class BufferReader {
		/**
		 * Extract the elements of ByteBuffer and put it in array.
		 * @param buffer the bytebuffer with the elements.
		 * @sampleType the real sample type of elements of array.
		 * @see Geometry
		 */
		public abstract void readBuffer(ByteBuffer buffer, final int sampleType);
	}

	/**
	 * This class extract the elements of ByteBuffer and put it in int[]
	 */
	private static class IntReader extends BufferReader {

		/**
		 * The array that will contain the elements of ByteBuffer
		 */
		final private int[] imageSamples;

		/**
		 * Indicates if the conversion of original to desired sample type has to be lossless.
		 */
		private boolean lossless;

		/**
		 * Constructor that obtain the necessary parameters.
		 * @param imageSamples is the array that willcontain the elements of ByteBuffer.
		 * @param lossless indicates if the conversion of original to desired sample type has to be lossless. If it is true and occurs
		 * a problem of type conversion, a LackOfPrecisionError will be thrown.
		 */
		public IntReader(int[] imageSamples, boolean lossless) {
			this.imageSamples = imageSamples;
			this.lossless = lossless;
		}

		/**
		 * Put the elements of buffer into int[]
		 * @param buffer the ByteBuffer with the elements.
		 * @param sampleType the real sample type of elements of the buffer.
		 * @see Geometry
		 */
		public void readBuffer(ByteBuffer buffer, final int sampleType) {
			switch(sampleType){
				case Geometry.BOOLEAN:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.get() == 0 ? 0 : 1;
					}
					break;
				case Geometry.U_BYTE:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.get();
					}
					break;
				case Geometry.U_SHORT:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.getChar();
					}
					break;
				case Geometry.SHORT:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.getShort();
					}
					break;
				case Geometry.INT:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.getInt();
					}
					break;
				case Geometry.LONG:
					for(int i=0;i<imageSamples.length;i++) {
						long l = buffer.getLong();
						if(l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						imageSamples[i] = (int) l;
					}
					break;
				case Geometry.FLOAT:
					for(int i=0;i<imageSamples.length;i++) {
						float f = buffer.getFloat();
						if(f < Integer.MIN_VALUE || f > Integer.MAX_VALUE) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						if(!isInteger(f) && lossless) {
							throw new LackOfPrecisionError("Lack of precision with one pixel");
						}
						imageSamples[i] = (int) f;
					}
					break;
				case Geometry.DOUBLE:
					for(int i=0;i<imageSamples.length;i++) {
						double d = buffer.getDouble();
						if(d < Integer.MIN_VALUE || d > Integer.MAX_VALUE) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						if(!isInteger(d) && lossless) {
							throw new LackOfPrecisionError("Lack of precision with one pixel");
						}
						imageSamples[i] = (int) d;
					}
					break;
			}
		}
	};

	/**
	 * This class extract the elements of ByteBuffer and put it in long[]
	 */
	private static class LongReader extends BufferReader { 
		/**
		 * The array that will contain the elements of ByteBuffer
		 */
		final private long[] imageSamples;

		/**
		 * Indicates if the conversion of original to desired sample type has to be lossless.
		 */
		private boolean lossless;

		/**
		 * Constructor that obtain the necessary parameters.
		 * @param imageSamples is the array that willcontain the elements of ByteBuffer.
		 * @param lossless indicates if the conversion of original to desired sample type has to be lossless. If it is true and occurs
		 * a problem of type conversion, a LackOfPrecisionError will be thrown.
		 */
		public LongReader(long[] imageSamples, boolean lossless) {
			this.imageSamples = imageSamples;
			this.lossless = lossless;
		}

		/**
		 * Put the elements of buffer into long[]
		 * @param buffer the ByteBuffer with the elements.
		 * @param sampleType the real sample type of elements of the buffer.
		 * @see Geometry
		 */
		public void readBuffer(ByteBuffer buffer, final int sampleType) {
			switch(sampleType){
				case Geometry.BOOLEAN:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.get() == 0 ? 0 : 1;
					}
					break;
				case Geometry.U_BYTE:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.get();
					}
					break;
				case Geometry.U_SHORT:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.getChar();
					}
					break;
				case Geometry.SHORT:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.getShort();
					}
					break;
				case Geometry.INT:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.getInt();
					}
					break;
				case Geometry.LONG:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.getLong();
					}
					break;
				case Geometry.FLOAT:
					for(int i=0;i<imageSamples.length;i++) {
						float f = buffer.getFloat();
						if(f < Long.MIN_VALUE || f > Long.MAX_VALUE) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						if(!isInteger(f) && lossless) {
							throw new LackOfPrecisionError("Lack of precision with one pixel");
						}
						imageSamples[i] = (long) f;
					}
					break;
				case Geometry.DOUBLE:
					for(int i=0;i<imageSamples.length;i++) {
						double d = buffer.getDouble();
						if(d < Long.MIN_VALUE || d > Long.MAX_VALUE) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						if(!isInteger(d) && lossless) {
							throw new LackOfPrecisionError("Lack of precision with one pixel");
						}
						imageSamples[i] = (long) d;
					}
					break;
			}
		}
	};

	/**
	 * This class extract the elements of ByteBuffer and put it in float[]
	 */
	private static class FloatReader extends BufferReader { 

		/**
		 * The array that will contain the elements of ByteBuffer
		 */
		final private float[] imageSamples;

		/**
		 * Indicates if the conversion of original to desired sample type has to be lossless.
		 */
		private boolean lossless;

		/**
		 * Constructor that obtain the necessary parameters.
		 * @param imageSamples is the array that willcontain the elements of ByteBuffer.
		 * @param lossless indicates if the conversion of original to desired sample type has to be lossless. If it is true and occurs
		 * a problem of type conversion, a LackOfPrecisionError will be thrown.
		 */
		public FloatReader(float[] imageSamples, boolean lossless) {
			this.imageSamples = imageSamples;
			this.lossless = lossless;
		}

		/**
		 * Put the elements of buffer into float[]
		 * @param buffer the ByteBuffer with the elements.
		 * @param sampleType the real sample type of elements of the buffer.
		 * @see Geometry
		 */
		public void readBuffer(ByteBuffer buffer, final int sampleType) {
			switch(sampleType){
				case Geometry.BOOLEAN:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.get() == 0 ? 0.0F : 1.0F;
					}
					break;
				case Geometry.U_BYTE:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.get();
					}
					break;
				case Geometry.U_SHORT:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.getChar();
					}
					break;
				case Geometry.SHORT:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.getShort();
					}
					break;
				case Geometry.INT:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.getInt();
					}
					break;
				case Geometry.LONG:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.getLong();
					}
					break;
				case Geometry.FLOAT:
					for(int i=0;i<imageSamples.length;i++) {
						imageSamples[i] = buffer.getFloat();
					}
					break;
				case Geometry.DOUBLE:
					for(int i=0;i<imageSamples.length;i++) {
						double d = buffer.getDouble();
						if(d < -Float.MAX_VALUE || d > Float.MAX_VALUE) {
							throw new LackOfPrecisionError("Pixel out of range");
						}
						if(!fitsInFloat(d) && lossless) {
							throw new LackOfPrecisionError("Lack of precision with one pixel");
						}
						imageSamples[i] = (float) d;
					}
					break;
			}
		}
	};

	/**
	 * Cast an array of elements represented in byte[] to int[], long[] or float[].
	 * @param b is the array that will be cast 
	 * @return a T[] with the elements.
	 */
	@SuppressWarnings("unchecked")
	public T bytetoT(byte b[]) {
		ByteBuffer buffer = ByteBuffer.wrap(b);

		switch(geo[Geometry.BYTE_ORDER]){
			case Geometry.BIG_ENDIAN:
				buffer = buffer.order(ByteOrder.BIG_ENDIAN);
				break;
			case Geometry.LITTLE_ENDIAN:
				buffer = buffer.order(ByteOrder.LITTLE_ENDIAN);
				break;
		}

		BufferReader reader = null;
		T line = null;
		switch(type) {
			case INT_ARRAY:
				line = (T) new int[b.length/size];
				reader = new IntReader((int[])line, lossless);
				break;
			case LONG_ARRAY:
				line = (T) new long[b.length/size];
				reader = new LongReader((long[])line, lossless);
				break;
			case FLOAT_ARRAY:
				line = (T) new float[b.length/size];
				reader = new FloatReader((float[])line, lossless);
				break;
		}
		reader.readBuffer(buffer, geo[Geometry.SAMPLE_TYPE]);
		return line;
	}

}
