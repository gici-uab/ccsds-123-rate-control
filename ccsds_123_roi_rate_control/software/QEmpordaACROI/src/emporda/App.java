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
package emporda;

import java.io.FileNotFoundException;

import GiciException.*;

/**
 * Main class of EMPORDA application of the Recommended Standard MHDC-123 White Book.
 * <p>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public class App {

	/**
	 * Main function.
	 *
	 * @params args input command line
	 */
	public static void main(final String[] args) {
		Parser parser = null;
		try {
			parser = new Parser(args);

		} catch (ErrorException e) {
			System.err.println("RUN ERROR:");
			System.err.println("Please report this error (specifying "
					+ "image type and parameters) to: gici-dev@abra.uab.es");
			System.exit(-1);

		} catch (ParameterException e) {
			System.err.println("ARGUMENTS ERROR: " + e.getMessage());
			System.exit(-1);
		}
		if (parser.getAction() == 0) {
			compress(parser);
		} else {
			decompress(parser);
		}
	}

	/**
	 * Runs all the compression and coding process.
	 *
	 * @params parser the command line options of the user
	 */
	public static void compress(final Parser parser) {
		int sampleOrder = parser.getSampleOrder();
		String inputFile = parser.getInputFile();
		String outputFile = parser.getOutputFile();
		String maskFile = parser.getInputMaskFile();
		boolean verbose = parser.getVerbose();
		boolean debugMode = parser.getDebugMode();
		int[] geo = null;
		int quantizationStep = parser.getQuantizationStep();
		int [] quantizationSteps = parser.getQuantizationSteps();
		int quantizer = parser.getQuantizer();
		int quantizationMode = parser.getQuantizationMode();
		float targetRate = parser.getTargetRate();
		int segmentSize = parser.getSegmentSize();
		int encoderType = parser.getEncoderType();
		int contextModel = parser.getContextModel();
		int AC_option = parser.getAC_option();
		int probabilityModel = parser.getProbabilityModel();
		int quantizerProbabilityLUT = parser.getQuantizerProbabilityLUT();
		int encoderWP = parser.getEncoderWP();
		int encoderUP = parser.getEncoderUP();
		int RCStrategy = parser.getRCStrategy();
		int windowsize = parser.getWindowSize();
		int samplePrediction = parser.getSamplePrediction();
		int bufferSize = parser.getBufferSize();
			
		Parameters parameters = null;
		Coder encoder = null;
		Quantizer uq = null;
		
		geo = parser.getImageGeometry();
		geo[CONS.ENDIANESS] = parser.getEndianess(); 

		try {	
			if(debugMode) {
				System.out.println("debug info: loading parameters");
			}
//			New parameter added in case there is not option file to control the encoder type
			parameters = new Parameters(parser.getOptionFile(), geo, true, debugMode, encoderType, AC_option);
			if(debugMode) {
				System.out.println("debug info: starting coder");
			}
			switch (quantizer){
			case 0:
				uq = new UTQ_URQ(quantizationStep);
				break;
			case 1:
				uq = new UTQDZ_NURQ(quantizationStep);
				break;
			case 2:
				uq = new UTQDZ_URQ(quantizationStep);
				break;
			case 3:
				uq = new URURQ(quantizationStep);
				break;
			case 4:
				uq = new OptimalUTQDZ(quantizationStep);
				break;
			case 5:
				uq = new UTQDZ_SingleOffset(quantizationStep);
				break;
			case 6:
				uq = new URQMAX(quantizationStep);
				break;
			case 7:
				uq = new URQ(quantizationStep);
				break;	
			case 8:
				uq = new TWOSDQ(quantizationStep);
				break;	
			case 9:
				uq = new SDQ(quantizationStep);
				break;	
			}
						
			// argument to control the type of encoder added ---------------------------------------------------------------------------------------
			encoder = new Coder(outputFile, inputFile, sampleOrder, parameters, debugMode, uq, quantizer, quantizationMode, windowsize, targetRate, segmentSize, contextModel, probabilityModel, quantizerProbabilityLUT, encoderWP, encoderUP, RCStrategy, samplePrediction, bufferSize, maskFile, quantizationSteps);	
			// -------------------------------------------------------------------------------------------------------------------------------------
			if(debugMode) {
				System.out.println("debug info: writting image header");
			}
			encoder.writeHeader(parameters);
			
			if(debugMode) {
				System.out.println("debug info: coding image");
			}
			encoder.code(verbose);
			
			// -----------------------------------------------------------------------------------
			encoder = null;
			parameters = null;
			uq = null;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		if (verbose || debugMode) {
			System.out.println("Compression process ended successfully");
		}
	}

	/**
	 * Runs the decoding and decompression process.
	 *
	 * @params parser the command line options of the user
	 */
	public static void decompress(final Parser parser) {

		int sampleOrder = parser.getSampleOrder();
		String inputFile = parser.getInputFile();
		String outputFile = parser.getOutputFile();
		String optionFile = parser.getOptionFile();
		String maskFile = parser.getInputMaskFile();
		boolean verbose = parser.getVerbose();
		boolean debugMode = parser.getDebugMode();
		int[] geo = null;
		int quantizationStep = parser.getQuantizationStep();
		int quantizer = parser.getQuantizer();
		int quantizationMode = parser.getQuantizationMode();
		float targetRate = parser.getTargetRate();
		int segmentSize = parser.getSegmentSize();
		Parameters parameters = null;
		Decoder decoder = null;
		Quantizer uq = null;
		int contextModel = parser.getContextModel();
		int probabilityModel = parser.getProbabilityModel();
		int quantizerProbabilityLUT = parser.getQuantizerProbabilityLUT();
		int entropyCoderType = parser.getEncoderType();
		int encoderWP = parser.getEncoderWP();
		int encoderUP = parser.getEncoderUP();
		int windowsize = parser.getWindowSize();
		int samplePrediction = parser.getSamplePrediction();
		int sampleType = parser.getImageGeometry()[CONS.TYPE];
		int RCStrategy = parser.getRCStrategy();
		
		try {
			
			if(debugMode) {
				System.out.println("debug info: starting decoder");
			}
			switch (quantizer){
			case 0:
				uq = new UTQ_URQ(quantizationStep);
				break;
			case 1:
				uq = new UTQDZ_NURQ(quantizationStep);
				break;
			case 2:
				uq = new UTQDZ_URQ(quantizationStep);
				break;
			case 3:
				uq = new URURQ(quantizationStep);
				break;
			case 4:
				uq = new OptimalUTQDZ(quantizationStep);
				break;
			case 5:
				uq = new UTQDZ_SingleOffset(quantizationStep);
				break;
			case 6:
				uq = new URQMAX(quantizationStep);
				break;
			case 7:
				uq = new URQ(quantizationStep);
				break;
			case 8:
				uq = new TWOSDQ(quantizationStep);
				break;	
			case 9:
				uq = new SDQ(quantizationStep);
				break;	
			}
			decoder = new Decoder(inputFile, outputFile, sampleType, debugMode, sampleOrder, uq, quantizer, quantizationMode, windowsize, segmentSize, targetRate, contextModel, probabilityModel, quantizerProbabilityLUT, entropyCoderType, encoderWP, encoderUP, samplePrediction, RCStrategy, maskFile);
			if (debugMode) {
				System.out.println("debug info: reading image header and loading parameters");
			}
			
			parameters = decoder.readHeader(optionFile);
			geo = parameters.getImageGeometry();
			geo[CONS.ENDIANESS] = parser.getEndianess();
			
			
			if(debugMode) {
				System.out.println("debug info: decoding image");
			}
			decoder.decode(verbose);
			decoder = null;
			parameters = null;
			uq = null;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		if (verbose || debugMode) {
			System.out.println("Decompression process ended succesfully");
		}
	}
	
}
