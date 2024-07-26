
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
import GiciException.ParameterException;
import GiciMath.*;

import java.io.*;

import emporda.CONS;
import emporda.Parameters;

/**
* CoderHeader class of EMPORDA application. CoderHeader writes the headers as
* it is told on the Recommended Standard MHDC-123 White Book.
* <p>
* 
* @author Group on Interactive Coding of Images (GICI)
* @version 1.0
*/

public class CoderHeaderAC {
	private static final int BITS_PER_BYTE = 8;

	private static final int bmask[] = {
		0x00, 0x01, 0x03, 0x07, 0x0f, 0x1f, 0x3f, 0x7f, 0xff,
		0x1ff,0x3ff,0x7ff,0xfff,0x1fff,0x3fff,0x7fff,0xffff,
		0x1ffff,0x3ffff,0x7ffff,0xfffff,0x1fffff,0x3fffff,
		0x7fffff,0xffffff,0x1ffffff,0x3ffffff,0x7ffffff,
		0xfffffff,0x1fffffff,0x3fffffff,0x7fffffff,0xffffffff
	};

	private int buffer = 0;
	private int bitsToGo = BITS_PER_BYTE;
	private OutputStream bos;
	private boolean debugMode = false;
	private long bitsWritten = 0;
	private int[] geo;
	private Parameters parameters;

	/**
	 * Constructor of CoderHeader. It receives the BitOutputStream, that will
	 * write all the information to the output file.
	 * 
	 * @param bos
	 *            the bit output stream
	 * @param debugMode
	 *            indicates whether to display debug information
	 * @param parameters
	 *            all the information about the coding process
	 */
	public CoderHeaderAC(final OutputStream bos, boolean debugMode,
			Parameters parameters) {
		this.bos = bos;
		this.debugMode = debugMode;
		this.parameters = parameters;
		this.geo = parameters.getImageGeometry();
	}

	/**
	 * Writes the image information to the header of the compressed file
	 * 
	 * @throws IOException
	 *             if can not write information to the file
	 */
	public void imageHeader() throws IOException {
		int sampleType = (geo[CONS.TYPE] == 3) ? 1 : 0;
		int bitsWritten = 0;

		if (debugMode) {
			System.out.println();
			System.out.println("Image Header");
			System.out.println("\t user defined data: 0 " +
					"(8 bits)");
			System.out.println("\t image width: " + geo[CONS.WIDTH] % (1 << 16) + 
					"(16 bits)");
			System.out.println("\t image height: " + geo[CONS.HEIGHT] % (1 << 16) + 
					"(16 bits)");
			System.out.println("\t image bands: " + geo[CONS.BANDS] % (1 << 16) + 
					"(16 bits)");
			System.out.println("\t sample type: " + sampleType + 
					"(1 bit)");
			System.out.println("\t free space: " + 0 + 
					"(2 bits)");
			System.out.println("\t dynamic range: " + parameters.dynamicRange % (1 << 4) + 
					"(4 bits)");
			System.out.println("\t sample encoding order: " + parameters.sampleEncodingOrder + 
					"(1 bit)");
			System.out.println("\t subframe interleaving depth: " + parameters.subframeInterleavingDepth + 
					"(16 bits)");
			System.out.println("\t free space: " + 0 + 
					"(2 bits)");
			System.out.println("\t output word size: " + parameters.outputWordSize % (1 << 3) + 
					"(3 bit)");
			System.out.println("\t entropy coder type: " + parameters.entropyCoderType + 
					"(2 bit)");
			System.out.println("\t free space (previously predictor metadata flag): " + 0 + 
					"(1 bit)");
			System.out.println("\t free space (previously entropy metadata flag): " + 0 + 
					"(1 bit)");
			System.out.println("\t free space: " + 0 + 
					"(8 bits)");

		}
		this.write(CONS.BYTE, 0); /* User defined data */
		this.write(CONS.SHORT, geo[CONS.WIDTH] % (1 << 16)); /* width */
		this.write(CONS.SHORT, geo[CONS.HEIGHT] % (1 << 16)); /* height */
		this.write(CONS.SHORT, geo[CONS.BANDS] % (1 << 16)); /* bands */
		bitsWritten += CONS.BYTE + CONS.SHORT * 3;

		/* SampleType|00|Dynamic Range|Sample Encoding Order */
		this.write(1, sampleType);
		this.write(2, 0);
		this.write(4, parameters.dynamicRange % (1 << 4));
		this.write(1, parameters.sampleEncodingOrder);
		bitsWritten += 1 + 2 + 4 + 1;

		/* Sub-frame interleaving depth */
		this.write(CONS.SHORT, parameters.subframeInterleavingDepth);
		bitsWritten += CONS.SHORT;

		/* 00|Output word Size| ECT | PMF | ECMF */
		this.write(2, 0);
		this.write(3, parameters.outputWordSize % (1 << 3));
		this.write(2, parameters.entropyCoderType);
		this.write(2, 0);
		this.write(CONS.BYTE, 0);
		bitsWritten += 2 + 3 + 1 + 2 + 1 + CONS.BYTE;

		if (debugMode) {
			System.out.println("\twritten " + (bitsWritten / 8) + " bytes "
					+ " and " + (bitsWritten % 8) + " bits");
		}
		this.bitsWritten += bitsWritten;
		this.flush();
	}

	/**
	 * Writes information about the prediction process to the header of the
	 * compressed file
	 * 
	 * @throws IOException
	 *             if can not write information to the file
	 * @throws ParameterException
	 *             when an invalid parameter is detected
	 */
	public void predictorMetadata() throws IOException, ParameterException {
		int bitsWritten = 0;
		if (debugMode) {
			System.out.println("Predictor metadata");
			System.out.println("\t free space: " + 0 + 
					"(2 bits)");
			System.out.println("\t number prediction bands: " + parameters.numberPredictionBands + 
					"(4 bits)");
			System.out.println("\t prediction mode: " + parameters.predictionMode + 
					"(1 bit)");
			System.out.println("\t free space: " + 0 + 
					"(1 bit)");
			System.out.println("\t local sum mode: " + parameters.localSumMode + 
					"(1 bit)");
			System.out.println("\t free space: " + 0 + 
					"(1 bit)");
			System.out.println("\t register size: " + parameters.registerSize % (1 << 6) + 
					"(6 bits)");
			System.out.println("\t weight component resolution: " + (parameters.weightComponentResolution - 4) + 
					"(4 bit)");
			System.out.println("\t exponent change interval: " + (parameters.tinc - 4) + 
					"(4 bits)");
			System.out.println("\t weight update scaling exponent: " + (parameters.vmin + 6) + 
					"(4 bit)");
			System.out.println("\t weight update scaling exponent final parameter: " + (parameters.vmax + 6) + 
					"(4 bits)");
			System.out.println("\t free space: " + 0 + 
					"(1 bit)");
			System.out.println("\t weight initialization method: " + parameters.weightInitMethod + 
					"(1 bits)");
			System.out.println("\t weight initialization table flag: " + parameters.weightInitTableFlag + 
					"(1 bit)");
			System.out.println("\t weight initialization resolution: " + parameters.weightInitResolution + 
					"(5 bits)");

		}
		/* 00 | numBands | predictionMode | 0 */
		this.write(2, 0);
		this.write(4, parameters.numberPredictionBands);
		this.write(1, parameters.predictionMode);
		this.write(1, 0);
		bitsWritten += 2 + 4 + 1 + 1;

		/* localSumMode | 0 | RegisterSize */
		this.write(1, parameters.localSumMode);
		this.write(1, 0);
		this.write(6, parameters.registerSize % (1 << 6));
		bitsWritten += 1 + 1 + 6;

		/* weightComponentResolution | expChangeInterval */

		this.write(4, parameters.weightComponentResolution - 4);
		this.write(4, parameters.tinc - 4);
		bitsWritten += 4 + 4;

		/* weightUpdateScalExp | weightUpdateScalExpFinalParameter */

		this.write(4, parameters.vmin + 6);
		this.write(4, parameters.vmax + 6);
		bitsWritten += 4 + 4;

		/* 0 | weightInit | weightInitTableFlag | weightInitresol */
		this.write(1, 0);
		this.write(1, parameters.weightInitMethod);
		this.write(1, parameters.weightInitTableFlag);
		this.write(5, parameters.weightInitResolution);
		this.flush();
		bitsWritten += 1 + 1 + 1 + 5;

		if (debugMode) {
			System.out.println("\twritten " + (bitsWritten / 8) + " bytes "
					+ " and " + (bitsWritten % 8) + " bits");
		}
		this.bitsWritten += bitsWritten;
		if (parameters.weightInitTableFlag == 1) {
			weightInitializationTable();
		}

	}

	/**
	 * Writes the weight initialization table to the header of the 
	 * compressed file
	 * 
	 * @throws IOException
	 *             if can not write information to the file
	 * @throws ParameterException
	 *             when an invalid parameter is detected
	 */
	public void weightInitializationTable() throws IOException,
			ParameterException {
		int zlength = geo[CONS.BANDS];
		int cont = 0;
		int sumBands = 3 * (1 - parameters.predictionMode);
		int bitsWritten = 0;
		int[][] weightInitTable = parameters.getWeightInitTable();

		if (debugMode) {
			System.out.println("coding weight initialization table ");
		}
		for (int z = 0; z < zlength; z++) {
			int nBands = Math.min(parameters.numberPredictionBands, z)
					+ sumBands;
			bitsWritten += parameters.weightInitResolution * nBands;
			for (int i = 0; i < nBands; i++) {
				this.write(parameters.weightInitResolution, IntegerMath
						.intToComplement2(weightInitTable[z][i],
								parameters.weightInitResolution));
				cont++;
			}
		}

		if (cont * parameters.weightInitResolution % 8 != 0) {
			this.write(8 - (cont * parameters.weightInitResolution % 8), 0);
			bitsWritten += 8 - (cont * parameters.weightInitResolution % 8);
		}
		this.bitsWritten += bitsWritten;

		if (debugMode) {
			System.out.println("\twritten " + (bitsWritten / 8) + " bytes "
					+ " and " + (bitsWritten % 8) + " bits");
		}
		this.flush();
	}

	/**
	 * Writes information about the entropy coder to the header of the
	 * compressed file
	 * 
	 * @throws IOException
	 *             if can not write information to the file
	 * @throws ParameterException
	 *             when an invalid parameter is detected
	 */
	public void entropyCoderMetadata() throws IOException, ParameterException {

		if (parameters.entropyCoderType == CONS.SAMPLE_ADAPTIVE_ENCODER) {
			sampleEntropyCoderMetadata();
		} else {
			blockEntropyCoderMetadata();
		}
	}

	/**
	 * Writes information about the sample entropy code to the header of
	 * the image file
	 * 
	 * @throws IOException
	 *             if can not write information to the file
	 * @throws ParameterException
	 *             when an invalid parameter is detected
	 */
	public void sampleEntropyCoderMetadata() throws IOException,
			ParameterException {
		int bitsWritten = 0;
		if (debugMode) {
			System.out.println("Sample entropy coder metadata");
			System.out.println("\t unary length limit: "
					+ (parameters.unaryLengthLimit % 32) + 
					"(5 bits)");
			System.out.println("\t rescaling counter size: " + (parameters.rescalingCounterSize - 4) + 
					"(3 bits)");
			System.out.println("\t initial count exponent: "
					+ (parameters.initialCountExponent % 8) + 
					"(3 bits)");
			System.out.println("\t accumulator initialization constant: " + (parameters.accInitConstant) + 
					"(4 bits)");
			System.out.println("\t accumulator initialization table flag: " + (parameters.accInitTableFlag) + 
					"(1 bit)");
		}
		/* UMAX | RCS */
		this.write(5, parameters.unaryLengthLimit % 32);
		this.write(3, parameters.rescalingCounterSize - 4);
		bitsWritten += 5 + 3;

		/*
		 * Initial Count Exponent | Accumulator initialization Constant |
		 * Accumulator Initialization Table Flag
		 */

		this.write(3, parameters.initialCountExponent % 8);
		this.write(4, parameters.accInitConstant);
		this.write(1, parameters.accInitTableFlag);
		bitsWritten += 3 + 4 + 1;
		this.flush();

		this.bitsWritten += bitsWritten;
		if (debugMode) {
			System.out.println("\twritten " + (bitsWritten / 8) + " bytes "
					+ " and " + (bitsWritten % 8) + " bits");
		}
		if (parameters.accInitTableFlag == 1) {
			accumulatorInitializationTable();
		}
	}

	/**
	 * Writes the accumulator initialization table to the header of the
	 * compressed file
	 * 
	 * @param accumulatorTable
	 *            the initialization table for the statistics of the entropy
	 *            coder
	 * @throws IOException
	 *             if can not write information to the file
	 * @throws ParameterException
	 *             when an invalid parameter is detected
	 */
	public void accumulatorInitializationTable() throws IOException,
			ParameterException {
		int[] accumulatorTable = parameters.getAccInitTable();
		int length = accumulatorTable.length;
		int bitsWritten = 0;

		if (debugMode) {
			System.out.println("coding accumulator initialization table ");
		}
		for (int i = 0; i < length; i++) {
			this.write(4, accumulatorTable[i]);
			bitsWritten += 4;
		}
		/* we make sure an integer number of bytes is written */
		if (length % 2 == 1) {
			this.write(4, 0);
			bitsWritten += 4;
		}
		this.bitsWritten += bitsWritten;
		if (debugMode) {
			System.out.println("\twritten " + (bitsWritten / 8) + " bytes "
					+ " and " + (bitsWritten % 8) + " bits");
		}
		this.flush();

	}

	/**
	 * Writes information about the block entropy coder to the header of the
	 * image file
	 * 
	 * @param entropyOption
	 *            information about the entropy coder algorithm
	 * @throws IOException
	 *             if can not write information to the file
	 * @throws ParameterException
	 *             when an invalid parameter is detected
	 */
	public void blockEntropyCoderMetadata() throws IOException,
			ParameterException {

		int bitsWritten = 0;

		if (debugMode) {
			System.out.println("Block entropy coder metadata");
			System.out.println("\t free space: " + 0 + 
					"(1 bit)");
			System.out.println("\t block size: " + ((parameters.blockSize == 8) ? 0 : 1) + 
					"(2 bits)");
			System.out.println("\t free space: " + 0 + 
					"(1 bit");
			System.out.println("\t reference sample interval: " + (parameters.referenceSampleInterval % (1 << 12)) + 
					"(12 bits)");
		}

		this.write(1, 0);
		if (parameters.blockSize == 8) {
			this.write(2, 0);
		} else {
			this.write(2, 1);
		}
		bitsWritten += 1 + 2;
		this.write(1, 0);
		this.write(12, parameters.referenceSampleInterval % (1 << 12));
		bitsWritten += 1 + 12;
		if (debugMode) {
			System.out.println("\twritten " + (bitsWritten / 8) + " bytes "
					+ " and " + (bitsWritten % 8) + " bits");
		}
		this.bitsWritten += bitsWritten;
		this.flush();
	}

	public long getBitsWritten() {
		return bitsWritten;
	}
	
	/**
	 * Write specified number of bits from value to the stream.
	 *
	 * @param howManyBits is number of bits to write (0-32).
	 * @param value is the source of bits. Rightmost bits are written.
	 *
	 * @throws IOException if there's an I/O problem writing bits
	 */
	public void write(int howManyBits, int value) throws IOException {

		if (howManyBits > 32 || howManyBits < 0) {
			throw new RuntimeException("BitInputStream can only write from 0 to 32 bits.");
		}

		value &= bmask[howManyBits]; // only right most bits valid

		while (howManyBits >= bitsToGo) {
			buffer = (buffer << bitsToGo) | (value >>> (howManyBits - bitsToGo));
			bos.write(buffer);

			value &= bmask[howManyBits - bitsToGo];
			howManyBits -= bitsToGo;
			bitsToGo = BITS_PER_BYTE;
			buffer = 0;
		}

		if (howManyBits > 0) {
			buffer = (buffer << howManyBits) | value;
			bitsToGo -= howManyBits;
		}
	}
	
	/**
	 * Flushes bits not yet written. Either this function or
	 * <code>close</code> must be called to ensure that all bits will be
	 * written.
	 *
	 * @throws IOException if there's a problem writing bits.
	 */
	public void flush() throws IOException {

		if (bitsToGo != BITS_PER_BYTE) {
			bos.write(buffer << bitsToGo);
			buffer = 0;
			bitsToGo = BITS_PER_BYTE;
		}

		bos.flush();
	}
}
