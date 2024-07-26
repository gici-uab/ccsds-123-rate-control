/*
 * EMPORDA Software - More than an implementation of MHDC Recommendation for Image Data Compression
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
 * http://sourceforge.net/projects/emporda
 * gici-info@deic.uab.es
 */

package GiciEntropyCoder.ArithmeticCoder;

import GiciStream.*;
import GiciMath.*;

import java.io.*;

import emporda.CONS;
import emporda.Parameters;

public class DecoderHeaderAC {
//	TODO: Modificació feta per tal de no haver de crear dos objectes un d'ells inutil sempre.
	private InputStream bis = null;
//	private ByteArrayInputStream is = null;
	private boolean debugMode = false;
	private Parameters params = null;
	private int bitsRead = 0;
	
	private static final int BITS_PER_BYTE = 8;

	private static final int bmask[] = {
		0x00, 0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, 0xff,
		0x1ff,0x3ff,0x7ff,0xfff,0x1fff,0x3fff,0x7fff,0xffff,
		0x1ffff,0x3ffff,0x7ffff,0xfffff,0x1fffff,0x3fffff,
		0x7fffff,0xffffff,0x1ffffff,0x3ffffff,0x7ffffff,
		0xfffffff,0x1fffffff,0x3fffffff,0x7fffffff,0xffffffff
	};

	private int buffer = 0;
	private int bitCount = 0;

	/**
	 * Constructor of DecoderHeader. It receives the BitIntputStream, that
	 * will read all the information to the input file.
	 *
	 * @param bis the bit input stream
	 */
	public DecoderHeaderAC(final InputStream bis, Parameters params, boolean debugMode) {

		this.bis = bis;
//		TODO: AQUEST CAST NO SERVEIX PER RES PER QUE EL MÈTODE READ QUE S'UTILITZA DESPRÉS ÉS NOMÉS FUNCIONAL PER LA CLASSE BITINPUTSTREAM
//		this.bis = (BitInputStream) bis;
		this.params = params;
		this.debugMode = debugMode;
	}
	
//	/**
//	 * Constructor of DecoderHeader. It receives the ByteArrayIntputStream, that
//	 * will read all the information to the input file.
//	 *
//	 * @param is the byte Array input stream
//	 */
//	public DecoderHeader(final ByteArrayInputStream is, Parameters params, boolean debugMode) {
//
//		this.bis = is;
//		this.bis = (ByteArrayInputStream) bis;
//		this.params = params;
//		this.debugMode = debugMode;
//	}
	/**
	 * Reads all the header of the compressed image, and saves all the parameters to an instance
	 * of the class Parameters
	 * @throws IOException
	 */
	public void readImageHeader() throws IOException {
		System.out.println("2.1");
		imageHeader();
		System.out.println("2.2");
		predictorMetadata();
		System.out.println("2.3");
		entropyCoderMetadata();
		System.out.println("2.4");
	}
	
	/**
	 * Returns the number of bits requested as the rightmost bits in the
	 * returned value.
	 *
	 * @param howManyBits is the number of bits to read and return (0-32).
	 *
	 * @return the value read. Only rightmost <code>howManyBits</code>
	 * are valid.
	 * 
	 * @throws IOException if there are not enough bits left.
	 */
	public int read(int howManyBits) throws IOException {

		if (howManyBits > 32 || howManyBits < 0) {
			throw new RuntimeException("BitInputStream can only return from 0 to 32 bits.");
		}

		int retval = 0;

		while (howManyBits > bitCount) {
			retval |= buffer << (howManyBits - bitCount);
			howManyBits -= bitCount;

			buffer = bis.read();
			if (buffer == -1) {
				throw new EOFException();
			}

			bitCount = BITS_PER_BYTE;
		}

		if (howManyBits > 0) {
			retval |= buffer >>> (bitCount - howManyBits);
			buffer &= bmask[bitCount - howManyBits];
			bitCount -= howManyBits;
		}

		return retval;
	}
	/**
	 * Reads the header relative to image information.
	 *
	 * @throws IOException when something goes wrong and writing must be stopped
	 */
	public void imageHeader() throws IOException {

		int bitsRead = 0;
		int[] imageGeometry = params.getImageGeometry();
		
//		TODO: ES POSSIBLE QUE AL NO PODER LLEGIR RES D'AQUI POSI ELS VALORS A 0. ES LLEGEIX LA MIDA DES DE LA CAPÇALERA DE LA IMATGE
		int value = this.read(CONS.BYTE); /*User defined data*/
		System.out.println("Bytes requested to noseque: "+value);
		imageGeometry[CONS.WIDTH] = this.read(CONS.SHORT);
		System.out.println("imageGeometry[CONS.WIDTH]: "+imageGeometry[CONS.WIDTH]);
		imageGeometry[CONS.WIDTH] = (imageGeometry[CONS.WIDTH] == 0)
									? (1 << 16) : imageGeometry[CONS.WIDTH]; /*X Value*/
		imageGeometry[CONS.HEIGHT] = this.read(CONS.SHORT);
		imageGeometry[CONS.HEIGHT] = (imageGeometry[CONS.HEIGHT] == 0)
									? (1 << 16) : imageGeometry[CONS.HEIGHT]; /*Y Value*/
		System.out.println("imageGeometry[CONS.HEIGHT]: "+imageGeometry[CONS.HEIGHT]);
		imageGeometry[CONS.BANDS] = this.read(CONS.SHORT);
		imageGeometry[CONS.BANDS] = (imageGeometry[CONS.BANDS] == 0)
									? (1 << 16) : imageGeometry[CONS.BANDS]; /*Z Value*/
		System.out.println("imageGeometry[CONS.BANDS]: "+imageGeometry[CONS.BANDS]);							
		bitsRead += CONS.BYTE + CONS.SHORT*3;
		
		/* SampleType|00|Dynamic Range|Sample Encoding Order */
		imageGeometry[CONS.TYPE] = this.read(1);
		imageGeometry[CONS.TYPE] = (imageGeometry[CONS.TYPE] == 0) ? 2 : 3;
		this.read(2);
		params.setImageGeometry(imageGeometry);
		params.dynamicRange = this.read(4);
		params.dynamicRange = (params.dynamicRange == 0) ? (1 << 4) : params.dynamicRange;
		
		// Allow bytes to be written if the sample type is unsigned and the dynamic range is up to 8.
		if (imageGeometry[CONS.TYPE] == 2 && params.dynamicRange <= 8) {
			imageGeometry[CONS.TYPE] = 1 ;
		}

		/* Sample order */
		params.sampleEncodingOrder = this.read(1);
		bitsRead += 1 + 2 + 4 + 1;
		/* Sub-frame interleaving depth */
		params.subframeInterleavingDepth = this.read(CONS.SHORT);
		if(params.sampleEncodingOrder == 0) {
			params.subframeInterleavingDepth = (params.subframeInterleavingDepth == 0) ? (1 << CONS.SHORT) : params.subframeInterleavingDepth;
		}
		bitsRead += CONS.SHORT;
		
		/* 00|Output word Size| ECT | PMF | ECMF */
		this.read(2);
		params.outputWordSize = this.read(3);
		params.outputWordSize = (params.outputWordSize == 0) ? (1 << 3) : params.outputWordSize;        
		params.entropyCoderType = this.read(2);
		this.read(1);  // this reads the deprecated predictor metadata flag
		this.read(1);  // this reads the deprecated entropy coder metadata flag 
		bitsRead += 2 + 3 + 2 + 1 + 1;
		/*Reserved*/
		this.read(CONS.BYTE);
		bitsRead += CONS.BYTE;

		if(debugMode) {
			System.out.println("Image Header");
			System.out.println("\t user defined data: 0 (8 bits)");
			System.out.println("\t image width: " + imageGeometry[CONS.WIDTH] % (1 << 16) 
					+ "(16 bits)");
			System.out.println("\t image height: " + imageGeometry[CONS.HEIGHT] % (1 << 16) 
					+ "(16 bits)");
			System.out.println("\t image bands: " + imageGeometry[CONS.BANDS] % (1 << 16) 
					+ "(16 bits)");
			System.out.println("\t sample type: " + imageGeometry[CONS.TYPE]
					+ "(1 bit)");
			System.out.println("\t free space: " + 0
					+ "(2 bits)");
			System.out.println("\t dynamic range: " + params.dynamicRange % (1 << 4)
					+ "(4 bits)");
			System.out.println("\t sample encoding order: " + params.sampleEncodingOrder
					+ "(1 bit)");
			System.out.println("\t subframe interleaving depth: " + params.subframeInterleavingDepth
					+ "(16 bits)");
			System.out.println("\t free space: " + 0 + "(2 bits)");
			System.out.println("\t output word size: "
					+ params.outputWordSize + "(3 bit)");
			System.out.println("\t entropy coder type: " + params.entropyCoderType
					+ "(2 bit)");
			System.out.println("\t free space (previously predictor metadata flag): " + 0
			                                      					+ "(1 bit)");
			System.out.println("\t free space (previously entropy metadata flag): " + 0
  					+ "(1 bit)");
			System.out.println("\t free space: " + 0
  					+ "(8 bits)");
			System.out.println("\tread " + (bitsRead/8) + " bytes " + " and " + (bitsRead % 8) + " bits");
		}
		this.bitsRead += bitsRead;
	}



	/**
	 * Reads the header relative to the predictor.
	 *
	 * @throws IOException when something goes wrong and writing must be stopped
	 */
	public void predictorMetadata()  throws IOException {
		int bitsRead = 0;
		
		/* 00 | numBands | predictionMode | 0 */
		this.read(2);
		params.numberPredictionBands = this.read(4);
		params.predictionMode = this.read(1);
		this.read(1);

		/* localSumMode | 0 | RegisterSize */
		params.localSumMode = this.read(1);
		this.read(1);
		params.registerSize = this.read(6);
		params.registerSize = (params.registerSize == 0) ? (1 << 6) : params.registerSize;
		bitsRead += 2 + 4 + 1 + 1 + 1 + 1 + 6;
		
		/* weightComponentResolution | expChangeInterval */

		params.weightComponentResolution = this.read(4) + 4;
		params.tinc = this.read(4) + 4;

		/* weightUpdateScalExp | weightUpdateScalExpFinalParameter */
		params.vmin = this.read(4) - 6;
		params.vmax = this.read(4) - 6;
		/* 0 | weightInit | weightInitTableFlag | weightInitresol */
		this.read(1);
		bitsRead += 4 + 4 + 4 + 4 + 1;
		
		params.weightInitMethod = this.read(1);
		params.weightInitTableFlag = this.read(1);
		params.weightInitResolution = this.read(5);
		bitsRead += 1 + 1 + 5;
		
		if(debugMode) {
			System.out.println("Predictor metadata");
			System.out.println("\t free space: " + 0  
					+ "(2 bits)");
			System.out.println("\t number prediction bands: " +  params.numberPredictionBands
					+ "(4 bits)");
			System.out.println("\t prediction mode: " + params.predictionMode
					+ "(1 bit)");
			System.out.println("\t free space: " + 0
					+ "(1 bit)");
			System.out.println("\t local sum mode: " + params.localSumMode
					+ "(1 bit)");
			System.out.println("\t free space: " + 0
					+ "(1 bit)");
			System.out.println("\t register size: " + params.registerSize % (1 << 6)
					+ "(6 bits)");
			System.out.println("\t weight component resolution: " + (params.weightComponentResolution - 4)
					+ "(4 bit)");
			System.out.println("\t exponent change interval: " + (params.tinc - 4)
			        + "(4 bits)");
			System.out.println("\t weight update scaling exponent: " + (params.vmin + 6)
					+ "(4 bit)");
			System.out.println("\t weight update scaling exponent final parameter: " + (params.vmax + 6)
			        + "(4 bits)");
			System.out.println("\t free space: " + 0
					+ "(1 bit)");
			System.out.println("\t weight initialization method: " + params.weightInitMethod
			        + "(1 bits)");
			System.out.println("\t weight initialization table flag: " + params.weightInitTableFlag
					+ "(1 bit)");
			System.out.println("\t weight initialization resolution: " + params.weightInitResolution
			        + "(5 bits)");
			System.out.println("\tread " + (bitsRead/8) + " bytes " + " and " + (bitsRead % 8) + " bits");
		}
		this.bitsRead += bitsRead;
		if (params.weightInitTableFlag == 1) {
			params.setWeightInitTable(weightInitializationTable());
		}
	}


	/**
	 * Reads the header relative to initialization table.
	 *
	 * @return the initialization table of the compression algorithm
	 * @throws IOException when something goes wrong and writing must be stopped
	 */
	public int[][] weightInitializationTable() throws IOException {
		
		int numBands = params.getImageGeometry()[CONS.BANDS];
		int[][] initializationTable = new int[numBands][];
		int sumBands = 3 * (1 - params.predictionMode);
		int cont = 0;
		int bitsRead = 0;
		if (debugMode) {
			System.out.println("decoding weight initialization table ");
		}
		for (int z = 0; z < numBands; z++) {

			int numIt = Math.min(params.numberPredictionBands, z) + sumBands;
			bitsRead += numIt*params.weightInitResolution;
			initializationTable[z] = new int[numIt];

			for (int i = 0; i < numIt; i++) {

				initializationTable[z][i] = this.read(params.weightInitResolution);
				initializationTable[z][i] = IntegerMath.complement2ToInt(initializationTable[z][i], 
																		params.weightInitResolution);
				cont++;
			}
		}
		if (cont*params.weightInitResolution % 8 != 0) {
			this.read(8 - (cont * params.weightInitResolution % 8));
			bitsRead += 8 - (cont * params.weightInitResolution % 8);
		}
		this.bitsRead += bitsRead;
		if (debugMode) {
			System.out.println("\tread " + (bitsRead/8) + " bytes " + " and " + (bitsRead % 8) + " bits");
		}
		return initializationTable;
	}

	/**
	 * Reads the header relative to the entropy coder.
	 *
	 * @return the initialization table for the statistics of the entropy coder
	 * @throws IOException when something goes wrong and writing must be stopped
	 */
	public int[] entropyCoderMetadata() throws IOException {
		int[] accumulatorTable = null;
//		TODO: Create switch as we now have 3 options (so far)
		System.out.println("params.entropyCoderType: "+params.entropyCoderType);
		if (params.entropyCoderType == CONS.SAMPLE_ADAPTIVE_ENCODER){
			System.out.println("getting in sampleEntropyCoderMetadata()");
			sampleEntropyCoderMetadata() ;
		}
		else {
			blockEntropyCoderMetadata();
		}
		System.out.println("accumulatorTable: "+accumulatorTable);
		return accumulatorTable;
	}

	/**
	 * Reads the header relative to the entropy coder.
	 *
	 * @throws IOException when something goes wrong and writing must be stopped
	 */
	public void sampleEntropyCoderMetadata() throws IOException {
		int bitsRead = 0;
		
		
		/* UMAX | RCS */
		params.unaryLengthLimit = this.read(5);
		params.unaryLengthLimit = (params.unaryLengthLimit == 0) ? 32 : params.unaryLengthLimit;
		params.rescalingCounterSize = this.read(3) + 4;
		bitsRead += 5 + 3;
		
		/* ICE | AITF | AIC */
		params.initialCountExponent = this.read(3);
		params.initialCountExponent = (params.initialCountExponent == 0) ? 8 : params.initialCountExponent;
		params.accInitConstant = this.read(4);
		params.accInitTableFlag = this.read(1);
		bitsRead += 3 + 4 + 1;
		
		this.bitsRead += bitsRead;
		if (debugMode) {
			System.out.println("Sample entropy coder metadata");
			System.out.println("\t unary length limit: " + (params.unaryLengthLimit % 32) 
					+ "(5 bits)");
			System.out.println("\t rescaling counter size: " +  (params.rescalingCounterSize - 4)
					+ "(3 bits)");
			System.out.println("\t initial count exponent: " + (params.initialCountExponent % 8)
					+ "(3 bits)");
			System.out.println("\t accumulator initialization constant: " + (params.accInitConstant)
					+ "(4 bits)");
			System.out.println("\t accumulator initialization table flag: " + (params.accInitTableFlag)
			        + "(1 bit)");
			System.out.println("\tread " + (bitsRead/8) + " bytes " + " and " + (bitsRead % 8) + " bits");
		}
		
		if (params.accInitTableFlag == 1) {
			System.out.println("Inside the if");
			System.out.println("params.getImageGeometry()[CONS.BANDS]"+params.getImageGeometry()[CONS.BANDS]);
			System.out.println("getAccInitTable(params.getImageGeometry()[CONS.BANDS]): "+getAccInitTable(params.getImageGeometry()[CONS.BANDS]));
			for (int i = 0; i < getAccInitTable(params.getImageGeometry()[CONS.BANDS]).length; i++) System.out.println(getAccInitTable(params.getImageGeometry()[CONS.BANDS])[i]);
			params.setAccInitTable(getAccInitTable(params.getImageGeometry()[CONS.BANDS]));
			System.out.println("Outside the if");
		}
	}

	/**
	 * Reads the header relative to the accumulator table.
	 *
	 * @param nbands the number of bands of the image
	 * @return the initialization table for the statistics of the entropy coder
	 * @throws IOException when something goes wrong and writing must be stopped
	 */
	public int[] getAccInitTable(int nBands) throws IOException {
		System.out.println("nBands: "+nBands);
		int[] accumulatorTable = new int[nBands];
		int bitsRead = 0;
		
		for (int i = 0; i < nBands; i++) {
			accumulatorTable[i] = this.read(4);
			bitsRead += 4;
		}
		if (nBands % 2 == 1) {
			System.out.println("DINS IF CONFLICTE");
			if(bis == null) System.out.println("ES NULL");
			this.read(4);
			bitsRead += 4;
		}
		this.bitsRead += bitsRead;
		
		if (debugMode) {
			System.out.println("coding accumulator initialization table ");
			System.out.println("\tread " + (bitsRead/8) + " bytes " + " and " + (bitsRead % 8) + " bits");
		}
		System.out.println(accumulatorTable[0]);
		return accumulatorTable;
	}

	/**
	 * Reads the header relative to the block entropy coder.
	 *
	 * @throws IOException when something goes wrong and writing must be stopped
	 */
	public void blockEntropyCoderMetadata() throws IOException
	{
		int bitsRead = 0;
		
		this.read(1);
		params.blockSize = this.read(2);
		params.blockSize = (params.blockSize == 0) ? 8 : 16;
		this.read(1);
		bitsRead += 1 + 2 + 1;
		
		params.referenceSampleInterval = this.read(12);
		params.referenceSampleInterval = (params.referenceSampleInterval == 0) ? 4096 : params.referenceSampleInterval;
		bitsRead += 12;
		
		this.bitsRead += bitsRead;
		
		if (debugMode) {
			System.out.println("Block entropy coder metadata");
			System.out.println("\t free space: " + 0 
					+ "(1 bit)");
			System.out.println("\t block size: " +  ((params.blockSize == 8) ? 0: 1)
					+ "(2 bits)");
			System.out.println("\t free space: " + 0
					+ "(1 bit");
			System.out.println("\t reference sample interval: " + (params.referenceSampleInterval % (1 << 12))
					+ "(12 bits)");
			System.out.println("\tread " + (bitsRead/8) + " bytes " + " and " + (bitsRead % 8) + " bits");
		}
	}

	/**
	 * Finishes the header reading process and shows how many bits have been read if necessary
	 */
	public void finish() {
		if(debugMode) {
			System.out.println("debug_info: total bits read: " + bitsRead);
		}
	}
}
