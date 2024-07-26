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

import java.io.*;
import java.util.*;

/**
 * Is a iterator to write and read three-dimensional images in files using low memory
 * and abstracting pixel order and sample type.
 * @see ListIterator<T>
 */
public class RawImageIterator <T> implements ListIterator <T> {

	/**
	 * Is the instance of class RawImage that built this object, it is used to avoid that garbage collector
	 * erasure this reference before all instances to RawImageIterator are erasured.
	 */
	private RawImage image;

	/**
	 * Is the file that contain of will contain the image.
	 */
	private RandomAccessFile file;

	/**
	 * Indicates the operations that can be done with file (see "Mode" above).
	 */
	private int mode;

	/**
	 * An object used to convert positions of pixels in desired order to original order.
	 * @see OrderConverter
	 */
	private OrderConverter oc;

	/**
	 * Is the distance (in pixels) in the original byte order between consecutive pixels in the same line of 
	 * desired pixel order.
	 * @see OrderConverter#getColumnOffset()
	 */
	private int offset;

	/**
	 * An object used to convert the original to the desired type of pixels.
	 * @see TypeConverter
	 */
	private TypeConverter<T> ty;

	/**
	 * The number of pixels per line in the desired pixel order.
	 */
	private int x_length;

	/**
	 * The size of the original type of pixels.
	 */
	private int size;

	/**
	 * The next line to be read.
	 */
	private int index;

	/**
	 * The last line read.
	 */
	private int lastIndex;

	/**
	 * The lowest possible index.
	 */
	private int min;

	/**
	 * The largest possible index.
	 */
	private int max;
	
	/**
	 * Constructor that built a iterator over all lines of image.
	 * @param image is the instance of class RawImage that built this object, it is used to avoid that garbage collector
	 * erasure this reference before all instances to RawImageIterator are erasured.
	 * @param t represents the type of array that will contain the pixels, for example, new int[0].
	 * @param file represents the file that contains or will contain the image.
	 * @param geo is the geometry of the image in BSQ mode.
	 * @param originalPixelOrder represents the original pixel order of the image by the transformation of BSQ to
	 * the original pixel order.
	 * @param pixelOrderTransfomation represents the pixel order transformation between original and desired pixel order of
	 * the image.
	 * @param mode indicates the operations (read, write, ...) that can be done.
	 * @param lossless indicates if the conversion of original to desired sample type has to be lossless. If it is true and occurs
	 * a problem of type conversion, a LackOfPrecisionError will be thrown.
	 * @exception IOException if an error occurs in read or write operations.
	 * @exception ClassCastException if the Type of T is not supported.
	 * @see Geometry
	 * @see TypeConverter
	 */
	public RawImageIterator(RawImage image, T t, File f, int geo[], int[] originalPixelOrder, int[] pixelOrderTransformation, int mode, boolean lossless) throws IOException, ClassCastException {
		try {
			init(image, t, f, geo, originalPixelOrder, pixelOrderTransformation, mode, lossless, 0, geo[originalPixelOrder[pixelOrderTransformation[Geometry.Z_SIZE]]]-1);
		}catch(IndexOutOfBoundsException e) {
			new Error("An unexpected error was ocurred" + e.getMessage());
		}
	}

	/**
	 * Constructor that built a iterator over all lines of the specified band of image.
	 * @param image is the instance of class RawImage that built this object, it is used to avoid that garbage collector
	 * erasure this reference before all instances to RawImageIterator are erasured.
	 * @param t represents the type of array that will contain the pixels, for example, new int[0].
	 * @param file represents the file that contains or will contain the image.
	 * @param geo is the geometry of the image in BSQ mode.
	 * @param originalPixelOrder represents the original pixel order of the image by the transformation of BSQ to
	 * the original pixel order.
	 * @param pixelOrderTransfomation represents the pixel order transformation between original and desired pixel order of
	 * the image.
	 * @param mode indicates the operations (read, write, ...) that can be done.
	 * @param lossless indicates if the conversion of original to desired sample type has to be lossless. If it is true and occurs
	 * a problem of type conversion, a LackOfPrecisionError will be thrown.
	 * @param band is the index of the band.
	 * @exception IOException if an error occurs in read or write operations.
	 * @exception IndexOutOfBoundsException is the band is out of range of the bands that compose the image.
	 * @exception ClassCastException if the type of T is not supported.
	 * @see Geometry
	 * @see TypeConverter
	 */
	public RawImageIterator(RawImage image, T t, File f, int geo[], int[] originalPixelOrder, int[] pixelOrderTransformation, int mode, boolean lossless, int band) throws IOException, IndexOutOfBoundsException, ClassCastException {
		init(image, t, f, geo, originalPixelOrder, pixelOrderTransformation, mode, lossless, band, band);
	}

	/**
	 * Constructor that built a iterator over all lines between specified bands of image.
	 * @param image is the instance of class RawImage that built this object, it is used to avoid that garbage collector
	 * erasure this reference before all instances to RawImageIterator are erasured.
	 * @param t represents the type of array that will contain the pixels, for example, new int[0].
	 * @param file represents the file that contains or will contain the image.
	 * @param geo is the geometry of the image in BSQ mode.
	 * @param originalPixelOrder represents the original pixel order of the image by the transformation of BSQ to
	 * the original pixel order.
	 * @param pixelOrderTransfomation represents the pixel order transformation between original and desired pixel order of
	 * the image.
	 * @param mode indicates the operations (read, write, ...) that can be done.
	 * @param lossless indicates if the conversion of original to desired sample type has to be lossless. If it is true and occurs
	 * a problem of type conversion, a LackOfPrecisionError will be thrown.
	 * @param band is the index of the band.
	 * @exception IOException if an error occurs in read or write operations.
	 * @exception IndexOutOfBoundsException is the band is out of range of the bands that compose the image.
	 * @exception ClassCastException if the type of T is not supported.
	 * @see Geometry
	 * @see TypeConverter
	 */
	public RawImageIterator(RawImage image, T t, File f, int geo[], int[] originalPixelOrder, int[] pixelOrderTransformation, int mode, boolean lossless, int initBand, int finalBand) throws IOException, IndexOutOfBoundsException, ClassCastException {
		init(image, t, f, geo, originalPixelOrder, pixelOrderTransformation, mode, lossless, initBand, finalBand);
	}

	/**
	 * Init the iterator over all lines between specified bands of image. This method is used, instead of put the same code
	 * in the constructor between bands, because is necessary to caught the IndexOutOfBoundsException in the iterator
	 * over all image.
	 * @param image is the instance of class RawImage that built this object, it is used to avoid that garbage collector
	 * erasure this reference before all instances to RawImageIterator are erasured.
	 * @param t represents the type of array that will contain the pixels, for example, new int[0].
	 * @param file represents the file that contains or will contain the image.
	 * @param geo is the geometry of the image in BSQ mode.
	 * @param originalPixelOrder represents the original pixel order of the image by the transformation of BSQ to
	 * the original pixel order.
	 * @param pixelOrderTransfomation represents the pixel order transformation between original and desired pixel order of
	 * the image.
	 * @param mode indicates the operations (read, write, ...) that can be done.
	 * @param lossless indicates if the conversion of original to desired sample type has to be lossless. If it is true and occurs
	 * a problem of type conversion, a LackOfPrecisionError will be thrown.
	 * @param band is the index of the band.
	 * @exception IOException if an error occurs in read or write operations.
	 * @exception IndexOutOfBoundsException is the band is out of range of the bands that compose the image.
	 * @exception ClassCastException if the type of T is not supported.
	 * @see Geometry
	 * @see TypeConverter
	 */
	private void init(RawImage image, T t, File f, int geo[], int[] originalPixelOrder, int[] pixelOrderTransformation, int mode, boolean lossless, int initBand, int finalBand) throws IOException, IndexOutOfBoundsException, ClassCastException {
		this.image = image;
		file = new RandomAccessFile(f, "rw");

		int[] sizeTable = {1/* boolean - 1 byte */, 1/* byte */, 2/* char */, 2/* short */, 4/* int */, 8/* long */, 4/* float */, 8/* double */};
		size = sizeTable[geo[Geometry.SAMPLE_TYPE]];
		int length = geo[Geometry.Z_SIZE]*geo[Geometry.Y_SIZE]*geo[Geometry.X_SIZE];
		if(((mode & RawImage.WRITE) == RawImage.WRITE) && (!f.exists() || f.length() < length*size)) {
			file.setLength(length*size);
		}

		this.mode = mode;

		oc = new OrderConverter(geo, originalPixelOrder, pixelOrderTransformation);
		offset = oc.getColumnOffset();

		ty = new TypeConverter<T>(t, geo, lossless);

		x_length = geo[originalPixelOrder[pixelOrderTransformation[Geometry.X_SIZE]]];

		int pixelsPerLine = geo[originalPixelOrder[pixelOrderTransformation[Geometry.X_SIZE]]];
		int linesPerBand = geo[originalPixelOrder[pixelOrderTransformation[Geometry.Y_SIZE]]];
		int pixelsPerBand = linesPerBand * pixelsPerLine;
		if(initBand > finalBand || initBand < 0 || finalBand >= length/pixelsPerBand) {
			throw new IndexOutOfBoundsException("Init or final band are not in range [0, "+length/pixelsPerBand+")");
		}
		min = initBand*linesPerBand;
		max = (finalBand+1)*linesPerBand;
		index = min;
		lastIndex = -1;
	}

	/**
	 * Throw an UnsupportedOperationException
	 * @exception UnsupportedOperationException is thrown when this method is called.
	 * @see ListIterator#add(T)
	 */
	public void add(T t) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Method add is not supported");
	}

	/**
	 * Say if the list has a next element.
	 * @return if the list has a next element.
	 * @see ListIterator#next()
	 */
	public boolean hasNext() {
		return index < max; 
	}

	/**
	 * Say if the list has a previous element.
	 * @return if the list has a previous element.
	 * @see ListIterator#previous()
	 */
	public boolean hasPrevious() {
		return index > min;
	}

	/**
	 * Return the element at the current index position
	 * @return a byte[] that represents the element at the current index position or null 
	 * if read is not allowed.
	 * @exception NoSuchElementException is the index is not between min and max.
	 */
//	TODO: change again to private
	public byte[] getElement() throws NoSuchElementException {
		if((mode & RawImage.READ) == 0) {
			return null;
		}
		int address;
		byte b[] = new byte[x_length*size];
		address = oc.getAddress(index*x_length);
		try {
			file.seek(address*size);
			if(offset < 1) {
				file.read(b);
			}else {
				file.read(b, 0, size);
				for(int i=1;i<x_length;i++) {
					file.skipBytes((offset-1)*size);
					file.read(b, size*i, size);
				}
			}
		}catch(IOException e) {
			throw new NoSuchElementException(e.getMessage());
		}
		return b;
	}

	/**
	 * Return the next element.
	 * @return the next element or null if read is not allowed.
	 * @exception NoSuchElementException is the index is greatest than max index.
	 * @see ListIterator#next()
	 */
	public T next() throws NoSuchElementException {
		if(!hasNext()) {
			throw new NoSuchElementException("Image has no next element");
		}
		byte[] b = getElement();
		lastIndex = index;
		index++;
		return b == null ? null : ty.bytetoT(b);
	}

	/**
	 * Return the next index.
	 * @return the next index or null if read is not allowed.
	 * @see ListIterator#nextIndex()
	 */
	public int nextIndex() {
		return index;
	}

	/**
	 * Return the previous element.
	 * @return the previous element.
	 * @exception NoSuchElementException is the index is lowest than max index.
	 * @see ListIterator#previous()
	 */
	public T previous() throws NoSuchElementException {
		if(!hasPrevious()) {
			throw new NoSuchElementException("Image has no previous element");
		}
		lastIndex = index;
		index--;
		byte[] b = getElement();
		return b == null ? null : ty.bytetoT(getElement());
	}

	/**
	 * Return the previous index.
	 * @return the previous index.
	 * @see ListIterator#previousIndex()
	 */
	public int previousIndex() {
		return index-1;
	}

	/**
	 * Throw an UnsupportedOperationException
	 * @exception UnsupportedOperationException is thrown when this method is called.
	 * @see ListIterator#remove()
	 */
	public void remove() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Method add is not supported");
	}

	/**
	 * Set the last element returned by previous() or next().
	 * @exception UnsupportedOperationException is write is not available.
	 * @exception IllegalStateException if next() or previous() have not been called before.
	 * @exception NoSuchElementException is the index is greatest than max index.
	 * @see ListIterator#set(T)
	 */
	public void set(T t) throws UnsupportedOperationException, IllegalStateException, NoSuchElementException {
		if((mode & RawImage.WRITE) == 0) {
			throw new UnsupportedOperationException("Write operations are not available in this mode");
		}
		if(lastIndex == -1) {
			throw new IllegalStateException("Method next hasn't been called");
		}
		int address;
		byte b[] = ty.TtoByte(t);
		address = oc.getAddress(lastIndex*x_length);
		try {
			file.seek(address*size);
			if(offset < 1) {
				file.write(b);
			}else {
				file.write(b, 0, size);
				for(int i=1;i<x_length;i++) {
					file.skipBytes((offset-1)*size);
					file.write(b, size*i, size);
				}
			}
		}catch(IOException e) {
			throw new NoSuchElementException(e.getMessage());
		}
	}

	/**
	 * Close the file associated at this iterator.
	 * @exception IOException if there are any problems closing the file associated.
	 */
	public void close() throws IOException {
		file.close();
		image = null;
	}
}
