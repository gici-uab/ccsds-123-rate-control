/*
 * EMPORDA Software - More than an implementation of MHDC Recommendation for  Image Data Compression
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
 * GNU General Public License for  more details.
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

import GiciStream.FileBitOutputStream;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;


import GiciContextModel.ContextModelling;
import GiciContextModel.ContextProbability;
import GiciContextModel.ContextSymbolModelling;
import GiciContextModel.IntegerContextProbability;
import GiciContextModel.IntegerContextModelling;
import GiciEntropyCoder.EntropyBlockCoder.EntropyBlockCoder;
import GiciEntropyCoder.EntropyIntegerCoder.EntropyIntegerCoder;
import GiciEntropyCoder.ArithmeticCoder.ArithmeticCoderFLW;
import GiciEntropyCoder.ArithmeticCoder.DumbMQCoder;
import GiciEntropyCoder.InterleavedEntropycoder.*;
import GiciEntropyCoder.Interface.EntropyCoder;
import GiciException.ParameterException;
import GiciException.WarningException;
import GiciFile.*;
import GiciFile.RawImage.LoadFile;
import GiciFile.RawImage.OrderConverter;
import GiciFile.RawImage.RawImage;
import GiciFile.RawImage.RawImageIterator;
import GiciStream.BitOutputStream;
import GiciStream.MemBitOutputStream;
import GiciStream.ByteStream;
import GiciPredictor.*;

/**
 * Coder class of EMPORDA application. This is class uses the predictor and the type of coder chosen 
 * in order to encode the image
 * <p>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public class Coder {

	private final int MAXQSTEP = 1024;
	private final File file;
	private final FileOutputStream fileStream;
	private final BitOutputStream fbos;
	private IntegerContextModelling icm;
	private ContextSymbolModelling cmsymbol;
	private IntegerContextProbability icp;
	private InterleavedEntropyEncoder iec;
	private DumbMQCoder dmq;
	private List<EntropyIntegerCoder> ecList;
	private List<ArithmeticCoderFLW> acList;
	private List<ContextProbability> cpList;
	private List<int[][]> predictedSamplesListBSQ;
	private List<int[][][]> predictedSamplesListBI;
	private List<ByteStream> ByteStreamList;
	private List<BitOutputStream> mbos;
	private List<Predictor> predictorList;
	
	private double entropy = 0;
	
	private Parameters parameters;
	private int[] geo;
	private Predictor predictorRC;
	private ContextModelling cmRC;
	private ContextProbability cpRC;
	
	
	private Predictor predictor;
	private EntropyCoder ec;
	private ContextModelling cm;
	private ContextProbability cp;
	private ContextProbability cps;
	
	private int[] originalPixelOrder;
	private int[] pixelOrderTransformation;
	private String inputFile;
	private String maskFile;
	private Quantizer uq;
	private int quantizationMode;
	private float targetRate = 0;
	private float T = 0;
	private float numbitsWritten = 0;
	private float numbitsWrittenBefore = 0;
	private float numbitsWrittenCurrentLine = 0;
	private float adaptiveTargetRate;
	//private float extraRate;
	private List<Integer> quantizationStepList = null;
	private int RCStrategy = 0;
	private int N = 0;
	private int contextModel = 0;
	private int probabilityModel = 0;
	private int quantizerProbabilityLUT = 0;
	private int numBitsPrecision = 0;
	private int IntegerContexts = 0;
	private int UPDATE_PROB0 = 0;
	private int WINDOW_PROB = 0;

	private float eta = 0;
	private float deviationRate = 0;
	private float mu[] = null;
	private float C[] = null;
	private float W[] = null;
	private int quantizer = 0;
	private FileWriter fw = null;
	private PrintWriter pw = null;
	private int samplePrediction = 0;
	//private SamplePrediction samplePredictor = null;
	private int MAXBITS = 32;
	int numOfContexts = 0;
	int coderWordLength = 0;
	int bufferSize = 1;
	int [][][] linePreviousBuffer = null;
	int [][][] predictedLinePreviousBuffer = null;
	double [] finalBinaryEntropies = null;
	double [] MSEs = null;
	double [] VARs = null;
	double [][] VARs2 = null;
	double [][] finalBinaryEntropies2 = null;
	double [][] finalBinaryEntropies2Real = null;
	double [][] MSEs2 = null;
	double [][] eumas2 = null;
	
	int [] quantizationSteps = null;
	int predictedLinesRef[][][] = null;
	int buffer[][][] = null;
	int predictedLines[][][] = null;
	int [] Qsteps = null;
	int [][] Qsteps2 = null;
	double minDiffRate = Double.MAX_VALUE;
	double euma = 0;
	float previousOutputRate = 0;
	int ySize = 0;
	int xSize = 0;
	int zSize = 0;
	/**
	 * Bit masks (employed when coding integers).
	 * <p>
	 * The position in the array indicates the bit for which the mask is computed.
	 */
	protected static final int[] BIT_MASKS2 = {1, 1 << 1, 1 << 2, 1 << 3, 1 << 4, 1 << 5, 1 << 6,
	1 << 7, 1 << 8, 1 << 9, 1 << 10, 1 << 11, 1 << 12, 1 << 13, 1 << 14, 1 << 15, 1 << 16, 1 << 17,
	1 << 18, 1 << 19, 1 << 20, 1 << 21, 1 << 22, 1 << 23, 1 << 24, 1 << 25, 1 << 26, 1 << 27,
	1 << 28, 1 << 29, 1 << 30, 1 << 31, 1 << 32};
	
	protected static final int[] BIT_MASKS = {1, 1 << 1, 1 << 2, 1 << 3, 1 << 4, 1 << 5, 1 << 6, 1 << 7, 1 << 8, 1 << 9};
	/**
	 * Qsteps.
	 * <p>
	 * The position in the array indicates the Qstep
	 */
	//protected static final int[] QSTEPS = {1, 2, 4, 6, 8, 10, 12, 16, 20, 24, 28, 32, 40, 48, 56, 64, 80, 96, 112, 128, 192, 256, 384, 512, 1024, 2048, 4096, 8192};
	protected static final int[] QSTEPS = {1};
	
	ByteStream stream;
	
	/**
	 * This variable indicates if debug information must be shown or not
	 */
	private boolean debugMode = false;
	
	/**
	 * Constructor of Coder. It receives the name of the output file and
	 * the parameters needed for the headers.
	 *
	 * @param outputFile the file where the result of the compressing will be saved
	 * @param inputFile the file that contain the image
	 * @param sampleOrder is the sample order of the image in the input file.
	 * @param parameters all the information about the compression process
	 * @param debugMode indicates if debug information must be shown
	 * @param numberOfBitplanesToDecode 
	 * @throws IOException when something goes wrong and writing must be stopped
	 * @throws ParameterException when an invalid parameter is detected
	 */
	public Coder(String outputFile, String inputFile, int sampleOrder, final Parameters parameters, 
			boolean debugMode, Quantizer uq, int quantizer, int quantizationMode, int windowsize, float targetRate, int segmentSize, int contextModel, int probabilityModel, int quantizerProbabilityLUT, int encoderWP, int encoderUP, int RCStrategy, int samplePrediction, int bufferSize, String maskFile, int [] quantizationSteps)
			throws IOException, ParameterException {
		
		switch(RCStrategy){
			case 0:
				//quantizationStepList = new ArrayList<Integer>(Arrays.asList(1,3,6,12,15,17,20,23,23,30,33,49,65,81,97,113,129,145,161,177,193,209,225,241,257,273,289,305,321,337,353,369,385,401,417,433,449,465,481,497,513,529,545));//
				quantizationStepList = new ArrayList<Integer>(Arrays.asList(3,6,12,18,30));//3,6,12,18,24,30//3,6,12,16,20,28,36,44,60,76,100
				break;
			case 1:
				quantizationStepList = new ArrayList<Integer>(Arrays.asList(3,6,12,18,30,42));//3,6,12,18,24,30//3,6,12,16,20,28,36,44,60,76,100
				break;
		}
			
		geo = parameters.getImageGeometry();
		linePreviousBuffer = new int[geo[CONS.BANDS]][2][geo[CONS.WIDTH]];
		predictedLinePreviousBuffer = new int[geo[CONS.BANDS]][2][geo[CONS.WIDTH]];
		finalBinaryEntropies = new double [geo[CONS.HEIGHT]];
		file = new File(outputFile);
		file.delete();
		
		fileStream = new FileOutputStream(file);
		fbos = new FileBitOutputStream( new BufferedOutputStream(fileStream));
		this.inputFile = inputFile;
		this.maskFile = maskFile;
		this.uq = uq;
		this.quantizationMode = quantizationMode;
		this.targetRate = targetRate;
		this.eta = this.targetRate;
		this.contextModel = contextModel;
		this.probabilityModel =  probabilityModel;
		this.quantizerProbabilityLUT = quantizerProbabilityLUT;
		this.WINDOW_PROB = encoderWP;
		this.UPDATE_PROB0 = encoderUP;
		this.RCStrategy = RCStrategy;
		this.quantizer = quantizer;
		this.samplePrediction = samplePrediction;
		this.bufferSize = bufferSize;
		
		if(probabilityModel == 1 && UPDATE_PROB0 > WINDOW_PROB){
			throw new Error("For this Probability Model option (-pm) UPDATE_PROB0 must be minor than WINDOW_PROB.");		
		}
		if(probabilityModel == 2 && UPDATE_PROB0 != WINDOW_PROB){
			throw new Error("For this Probability Model option (-pm) UPDATE_PROB0 equal than WINDOW_PROB.");		
		}
		
		if (quantizationMode == 1){ //Fixed Rate
			adaptiveTargetRate = targetRate;
			
			mbos = new ArrayList<BitOutputStream>();		
			for(int i = 0; i < N; i++){
				mbos.add(new MemBitOutputStream());
			}
		}
		
		this.quantizationSteps = new int[quantizationSteps.length];
		for(int i = 0; i < quantizationSteps.length; i++) {
				this.quantizationSteps[i] = quantizationSteps[i];
		}

		switch(sampleOrder) {
			case 0: //BSQ
				originalPixelOrder = OrderConverter.DIM_TRANSP_IDENTITY;
				if (parameters.sampleEncodingOrder == CONS.BAND_SEQUENTIAL) {
					pixelOrderTransformation = OrderConverter.DIM_TRANSP_IDENTITY;
				}else {
					pixelOrderTransformation = OrderConverter.DIM_TRANSP_BSQ_TO_BIL;
				}
				break;
			case 1: //BIL
				originalPixelOrder = OrderConverter.DIM_TRANSP_BSQ_TO_BIL;
				if (parameters.sampleEncodingOrder == CONS.BAND_SEQUENTIAL) {
					pixelOrderTransformation = OrderConverter.DIM_TRANSP_BIL_TO_BSQ;
				}else {
					pixelOrderTransformation = OrderConverter.DIM_TRANSP_IDENTITY;
				}
				break;
			case 2: //BIP
				originalPixelOrder = OrderConverter.DIM_TRANSP_BSQ_TO_BIP;
				if (parameters.sampleEncodingOrder == CONS.BAND_SEQUENTIAL) {
					pixelOrderTransformation = OrderConverter.DIM_TRANSP_BIP_TO_BSQ;
				}else {
					pixelOrderTransformation = OrderConverter.DIM_TRANSP_BIP_TO_BIL;
				}
				break;
		}
		this.parameters = parameters;
		
		zSize = geo[CONS.BANDS];
		ySize = geo[CONS.HEIGHT];
		xSize = geo[CONS.WIDTH];
		
		
		this.debugMode = debugMode;
		
	
		if (quantizationMode == 0){ //Fixed Quantization Step
			this.predictor = new Predictor(parameters);
			if(quantizer == 10){
				fw = new FileWriter("quantizer.data",false);
				pw = new PrintWriter(fw);
			}
		}else if (quantizationMode == 1){ //Fixed Rate
			if(this.RCStrategy == 0 || this.RCStrategy == 1 || this.RCStrategy == 2 || this.RCStrategy == 3){
				this.predictor = new Predictor(parameters);
				this.predictorRC = new Predictor(parameters);
			}else{
				this.predictorList = new ArrayList<Predictor>();
				for (int i = 0; i < N; i++){
					predictorList.add(new Predictor(parameters));
				}
			}
		}
		
	}
	

	/**
	 * Writes the header with all the needed information for the decompression process.
	 *
	 * @param parameters all information about the compression process
	 * @throws IOException when something goes wrong and writing must be stopped
	 * @throws ParameterException when an invalid parameter is detected
	 */
	public void writeHeader(final Parameters parameters) throws IOException, ParameterException {
		
		CoderHeader ch = new CoderHeader(fbos, debugMode, parameters);
		ch.imageHeader();
		ch.predictorMetadata();
		ch.entropyCoderMetadata();
		ch = null;
	}

	/**
	 * Compiles all the information needed to create the entropy coder,
	 * and creates it.
	 * @param verbose indicates whether to display information
	 */
	// --------- New argument added to control the entropyCoder
	private void startCoder(boolean verbose) {
		
		
		
		switch(parameters.entropyCoderType)
		{
			case CONS.SAMPLE_ADAPTIVE_ENCODER:
				try {
					if (quantizationMode == 0 || (quantizationMode == 1 && RCStrategy == 0)){ //Fixed Quantization Step
						ec = new EntropyIntegerCoder(
								fbos,
								parameters.initialCountExponent,
								parameters.accInitConstant,
								parameters.rescalingCounterSize,
								parameters.dynamicRange,
								parameters.unaryLengthLimit,
								parameters.getAccInitTable(),
								parameters.getImageGeometry()[CONS.BANDS],
								parameters.getImageGeometry()[CONS.HEIGHT],
								parameters.getImageGeometry()[CONS.WIDTH]);
					} else if (quantizationMode == 1 ){ //Fixed Rate
						ecList = new ArrayList<EntropyIntegerCoder>();
						for(int i = 0; i < N; i++){
							ecList.add(new EntropyIntegerCoder(mbos.get(i),
									parameters.initialCountExponent,
									parameters.accInitConstant,
									parameters.rescalingCounterSize,
									parameters.dynamicRange,
									parameters.unaryLengthLimit,
									parameters.getAccInitTable(),
									parameters.getImageGeometry()[CONS.BANDS],
									parameters.getImageGeometry()[CONS.HEIGHT],
									parameters.getImageGeometry()[CONS.WIDTH]));
						}
					}
				} catch (ParameterException e) {
					e.printStackTrace();
					System.exit(-1);
				}
				if(verbose || debugMode) {
					System.out.println("debug Info: Starting sample adaptive coder");
				}
				
				break;
			case CONS.BLOCK_ADAPTIVE_ENCODER:
				ec =  new EntropyBlockCoder(
						fbos,
						parameters.blockSize,
						parameters.dynamicRange,
						parameters.referenceSampleInterval,
						verbose);
				if(verbose || debugMode) {
					System.out.println("debug Info: Starting block adaptive coder");
				}
				
				break;
			
			case CONS.SYMBOL_ARITHMETIC_ENCODER_FLW:
				 cmsymbol = new ContextSymbolModelling();
				
			case CONS.ARITHMETIC_ENCODER_FLW:
				if (quantizationMode == 0){ //Fixed Quantization Step
					cm = new ContextModelling(contextModel);
					numOfContexts = cm.getNumberOfContexts(MAXBITS);
					numBitsPrecision = 15;
					coderWordLength = 48;
					cp = new ContextProbability(probabilityModel, numOfContexts, numBitsPrecision, quantizerProbabilityLUT, parameters.entropyCoderType, WINDOW_PROB, UPDATE_PROB0);
					ec = new ArithmeticCoderFLW(coderWordLength, numBitsPrecision, numOfContexts);
				
				}else if (quantizationMode == 1 ){ //Fixed Rate
					
					if(this.RCStrategy == 0 || this.RCStrategy == 1 || this.RCStrategy == 2 || this.RCStrategy == 3){
						cm = new ContextModelling(contextModel);
						numOfContexts = cm.getNumberOfContexts(MAXBITS);
						numBitsPrecision = 15;
						coderWordLength = 48;
						cp = new ContextProbability(probabilityModel, numOfContexts, numBitsPrecision, quantizerProbabilityLUT, parameters.entropyCoderType, WINDOW_PROB, UPDATE_PROB0);
						cps = new ContextProbability(probabilityModel, numOfContexts, numBitsPrecision, quantizerProbabilityLUT, parameters.entropyCoderType, WINDOW_PROB, UPDATE_PROB0);
						ec = new ArithmeticCoderFLW(coderWordLength, numBitsPrecision, numOfContexts);
						
					}else{
						cm = new ContextModelling(contextModel);
						cpList = new ArrayList<ContextProbability>();
						acList = new ArrayList<ArithmeticCoderFLW>();
						
						
						predictedSamplesListBSQ = new ArrayList<int[][]>();
						predictedSamplesListBI = new ArrayList<int[][][]>();
						
						ByteStreamList = new ArrayList<ByteStream>();
						
						for(int i = 0; i < N; i++){
							numOfContexts = cm.getNumberOfContexts(MAXBITS);
							numBitsPrecision = 15;
							coderWordLength = 48;
							cpList.add(new ContextProbability(probabilityModel, numOfContexts, numBitsPrecision, quantizerProbabilityLUT, parameters.entropyCoderType, WINDOW_PROB, UPDATE_PROB0));
							acList.add(new ArithmeticCoderFLW(coderWordLength, numBitsPrecision, numOfContexts));
							
							ByteStreamList.add(new ByteStream());
							
							if(parameters.sampleEncodingOrder == CONS.BAND_SEQUENTIAL){
								predictedSamplesListBSQ.add(new int[parameters.getImageGeometry()[CONS.HEIGHT]][parameters.getImageGeometry()[CONS.WIDTH]]);
							}
							if(parameters.sampleEncodingOrder == CONS.BAND_INTERLEAVE){
								predictedSamplesListBI.add(new int[parameters.getImageGeometry()[CONS.BANDS]][2][parameters.getImageGeometry()[CONS.WIDTH]]);
							}
						}
					}
					
					
				}
				break;
				
			case CONS.INTERLEAVE_ENTROPY_CODER:
				cm = new ContextModelling(contextModel);
				numOfContexts = cm.getNumberOfContexts(MAXBITS);
				numBitsPrecision = 15;
				coderWordLength = 48;
				cp = new ContextProbability(probabilityModel, numOfContexts, numBitsPrecision, quantizerProbabilityLUT, parameters.entropyCoderType, WINDOW_PROB, UPDATE_PROB0);
				iec = new InterleavedEntropyEncoder();
				break;
			case CONS.DUMB_MQ_ENCODER:
				cm = new ContextModelling(contextModel);
				numOfContexts = cm.getNumberOfContexts(MAXBITS);
				numBitsPrecision = 15;
				coderWordLength = 48;
				cp = new ContextProbability(probabilityModel, numOfContexts, numBitsPrecision, quantizerProbabilityLUT, parameters.entropyCoderType, WINDOW_PROB, UPDATE_PROB0);
				dmq = new DumbMQCoder((OutputStream) fbos);
				break;
			case CONS.ENTROPY:
				cm = new ContextModelling(contextModel);
				numOfContexts = cm.getNumberOfContexts(MAXBITS);
				numBitsPrecision = 15;
				coderWordLength = 48;
				cp = new ContextProbability(probabilityModel, numOfContexts, numBitsPrecision, quantizerProbabilityLUT, parameters.entropyCoderType, WINDOW_PROB, UPDATE_PROB0);
				break;
				
			case CONS.AIC:
				IntegerContexts = 100;
				icm = new IntegerContextModelling(contextModel, IntegerContexts);
				numBitsPrecision = 15;
				coderWordLength = 48;
				icp = new IntegerContextProbability(probabilityModel, numBitsPrecision, quantizerProbabilityLUT, IntegerContexts);
				//ec = new ArithmeticCoderFLW(coderWordLength, numBitsPrecision, numOfContexts);
				break;
			
		}
		
	}

	/**
	 * Runs the EMPORDA coder algorithm to compress the image.
	 * 
	 * @param verbose indicates whether to display information
	 * @throws Exception 
	 */
	// New argument added to control the entropy coder ---------------------
	public void code(boolean verbose) throws Exception {
		startCoder(verbose/*entropyCoder*/);
		
		if (parameters.sampleEncodingOrder == CONS.BAND_SEQUENTIAL) {
			if (quantizationMode == 0 && targetRate == 0){
				codeBSQ(verbose); //Fixed quantization step mode
			}
			if (quantizationMode == 1 && targetRate != 0){
				codeBSQRateControl(verbose);
			}
		} else {
			if (quantizationMode == 0){
				if(parameters.entropyCoderType == 0 || parameters.entropyCoderType == 1) codeBI(verbose); //Fixed quantization step mode
				if(parameters.entropyCoderType == 2)  codeBIAC(verbose);
				
			}
			else if (quantizationMode == 1){
				int[][][] maskSamples = null;
				int [] ROISamplesLine = null;
				int ROISamples = 0;
				int [][] ROIsSamplesLine = null;
				int [] ROIsSamples = null;
				
				if(RCStrategy == 1) {
					ROISamplesLine = new int [ySize];
					maskSamples = getMask();
					for(int y = 0; y < ySize; y++) {
						for(int x = 0; x < xSize; x++) {
							if(maskSamples[0][y][x] == 255) {
								ROISamplesLine[y]++;
							}
						}
						ROISamples = ROISamples + ROISamplesLine[y];
					}
					ROISamples = ROISamples * zSize;
				}
				
				
				if(RCStrategy == 2) {
					ROISamplesLine = new int [ySize];
					maskSamples = getMask();
					for(int y = 0; y < ySize; y++) {
						for(int x = 0; x < xSize; x++) {
							if(maskSamples[0][y][x] == 255) {
								ROISamplesLine[y]++;
							}
						}
						ROISamples = ROISamples + ROISamplesLine[y];
					}
					ROISamples = ROISamples * zSize;
				}
				
				if(RCStrategy == 3) {
					ROISamplesLine = new int [ySize];
					maskSamples = getMask();
					for(int y = 0; y < ySize; y++) {
						for(int x = 0; x < xSize; x++) {
							if(maskSamples[0][y][x] == 255) {
								ROISamplesLine[y]++;
							}
						}
						ROISamples = ROISamples + ROISamplesLine[y];
					}
					ROISamples = ROISamples * zSize;
				}
				
				//TODO
				T = targetRate;
				
				/*if(parameters.entropyCoderType == CONS.SAMPLE_ADAPTIVE_ENCODER || parameters.entropyCoderType == CONS.BLOCK_ADAPTIVE_ENCODER) {
					switch(RCStrategy){
					case 0:
						codeBIRateControlValsesia(verbose, numberREALSamples, maskSamples); //Fixed rate mode
						break;
					case 1:
						this.codeBIRateControlROI(verbose, numberREALSamples, maskSamples);
						break;
					}
				}*/
					
					
				if(parameters.entropyCoderType == CONS.ARITHMETIC_ENCODER_FLW){
					
					switch(RCStrategy){
					case 0:
						codeBIACRateControlValsesia(verbose); //Fixed plain rate mode
						break;
					case 1:
						//RATE IS DISTRIBUTED ALONG THE ROI AND BG AREA (ONLY ONE OR MULTIPLE ROIs BUT WITH SAME PRIORITY (QSTEP)). 
						//THE FIRST BYTES ARE USED TO ENCODE THE ROI AREA TILL THE ROI AREA IS ENCODED A THE PAE ACCORDED BY THE USER.
						//THE REMAINING BYTES ARE EMPLOYED FOR ENCODING THE BG AREA
						codeBIACRateControlROI(verbose, ROISamplesLine, ROISamples, maskSamples, quantizationSteps);
						break;
					case 2:
						//RATE IS DISTRIBUTED ALONG THE ROI AND BG AREA (ONLY ONE OR MULTIPLE ROIs WITH SAME PRIORITY NOT CODING THE FIRST LINE (QSTEP)).
						//THE FIRST BYTES ARE USED TO ENCODE THE ROI AREA TILL THE ROI AREA IS ENCODED A THE PAE ACCORDED BY THE USER.
						//THE REMAINING BYTES ARE EMPLOYED FOR ENCODING THE BG AREA
						codeBIACRateControlROI2(verbose, ROISamplesLine, ROISamples, maskSamples, quantizationSteps);
						break;
					case 3:
						//ROI IS LOSSLESSLY ENCODED. THE TARGET RATE IS DISTRIBUTED ONLY FOR BG AREA. 
						//HOWEVER THE ROI MUST BE ENCODED ACCORDING TO THE PAE RESTRICTION. CAN OCCUR THAT MORE BYTES THAN THE TARGET RATE ARE EMPLOYED, SINCE THE PAE IS THE MOST RESTRICTION CONDITION.
						//THE BYTES USED FOR THE ROI DO NOT HAVE IMPACT TO THE AMOUNT OF BYTES USED FOR THE BG ENCODING
						codeBIACRateControlROI3(verbose, ROISamplesLine, ROISamples, maskSamples, quantizationSteps);
						break;
					}
				}
			}
			
		}
		
		int oddBytes = (int) (file.length() % parameters.outputWordSize);
		if (oddBytes != 0) {
			byte b[] = new byte[parameters.outputWordSize - oddBytes];
			Arrays.fill(b, (byte)0);
			fileStream.write(b);
		}
		fileStream.close();
		/*
		if(parameters.entropyCoderType != CONS.ENTROPY){
			if(parameters.entropyCoderType == CONS.AIC){
				System.out.println(entropy/(float)(geo[CONS.BANDS]*geo[CONS.HEIGHT]*geo[CONS.WIDTH]));
			}else{
				System.out.println(file.length()*8/(float)(geo[CONS.BANDS]*geo[CONS.HEIGHT]*geo[CONS.WIDTH]));
			}
		}else{
			System.out.println(entropy/(float)(geo[CONS.BANDS]*geo[CONS.HEIGHT]*geo[CONS.WIDTH]));	
		}*/
		

		if (verbose) {
			//System.out.println("\rWritten " + file.length() + " bytes  ");
			System.out.println("\rRate " + file.length()*8/(float)(geo[CONS.BANDS]*geo[CONS.HEIGHT]*geo[CONS.WIDTH]) + " bpppb \n");
		}

	}
	

	/**
	 * Returns the max value of the samples one they have been predicted
	 *
	 * @param predicted Samples
	 * @return the maximum value of the predicted Samples.
	 */
	public static int[] getElementCount(int[][] predictedSamples){
		
		int[] elementData = new int[2];
		elementData[0] = 0; // max
		elementData[1] = Integer.MAX_VALUE; 
		int value = 0;
		for (int y = 0; y < predictedSamples.length; y ++) {
			for (int x = 0; x < predictedSamples[0].length; x ++) {
				value = predictedSamples[y][x];
				if(value > elementData[0]) elementData[0] = value;
				if(value < elementData[1]) elementData[1] = value;
			}
		}
		
		return elementData;
	}
	
	
	
	
	
	

	/**
	 * BSQ Arithmetic Coder with sign coding
	 * @param predictedSamples
	 * @param Previous1
	 * @param Previous2
	 * @param z
	 */
	private void BSQACWithSign(int[][] predictedSamples, int[][] Previous1, int[][] Previous2, int z){
		boolean realBit = false;
		boolean signBit = false;
		int context = -1;
		int prob = -1;
		int [][] MapSign = new int [ySize][xSize];//1 --> positive, -1 --> negative
		int [][] statusMapSignificance = new int [ySize][xSize];
		
		for (int y = 0; y < ySize; y ++) {
		for (int x = 0; x < xSize; x ++) {
			MapSign[y][x] = (int)Math.signum(predictedSamples[y][x]);
			predictedSamples[y][x] = Math.abs(predictedSamples[y][x]);
		}}
		
		//this for goes from 32 to 0, since in 15th bit the sign is stored
		for (int bit = MAXBITS-2; bit >= 0; bit--){
		for (int y = 0; y < ySize; y ++) {
		for (int x = 0; x < xSize; x ++) {
			realBit = (predictedSamples[y][x] & BIT_MASKS2[bit]) != 0;
			//check first time significant
			if(realBit == true && statusMapSignificance[y][x] == 0){
				statusMapSignificance[y][x] = 1;
				
				if(MapSign[y][x] == 1){
					signBit = true;
				}else{
					if(MapSign[y][x] == -1){
						signBit = false;
					}
				}
							 
				///////////////////////// realBit coding ///////////////////////
				context = cm.getContext(predictedSamples, Previous1, Previous2, z, y, x, bit);//get context   --------> 6.18 bps
				//context = cm.getContextSignificance(statusMapSignificance, y, x);//get context					--------> 4.06 bps
				prob = cp.getProbability(context);//get probability for the computed context
				cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
				ec.encodeBitProb(realBit, prob);//encode the bit using the specific probability
				ec.encodeBit(signBit);
			}else{
				context = cm.getContext(predictedSamples, Previous1, Previous2, z, y, x, bit);//get context
				prob = cp.getProbability(context);//get probability for the computed context
				cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
				ec.encodeBitProb(realBit, prob);//encode the bit using the specific probability
				//ec.encodeBit(realBit);//encode the bit using the specific probability
			}
			
					
		}}}
	}
	
	/**
	 * BSQ Arithmetic Coder
	 * @param predictedSamples
	 * @param Previous1
	 * @param Previous2
	 * @param z
	 */
	private void BSQAC(int[][] predictedSamples, int[][] Previous1, int[][] Previous2, int z){
		boolean realBit = false;
		int context = -1;
		for (int bit = MAXBITS-1; bit >= 0; bit--){
		for (int y = 0; y < ySize; y ++) {
		for (int x = 0; x < xSize; x ++) {
			realBit = (predictedSamples[y][x] & BIT_MASKS2[bit]) != 0;
			context = cm.getContext(predictedSamples, Previous1, Previous2, z, y, x, bit);//get context
			int prob = cp.getProbability(context);//get probability for the computed context
			cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
			ec.encodeBitProb(realBit, prob);//encode the bit using the specific probability
					
		}}}
		//System.out.println(z+" "+(double)ec.getNumBitsWritten()/((double)height*(double)width));
		ec.resetNumBitsWritten();
	}

	/**
	 * Encodes an image in BSQ order using Fixed Quantization Step Mode
	 *
	 * @param verbose indicates whether to display information
	 * @throws Exception 
	 */
//	BEFORE DOING THIS IS NECESSARY INITIALISATE BUFFERED WRITER BR_HIST AS CODER ARGUMENT BufferedWriter br_hist = null;

	private void codeBSQ(boolean verbose) throws Exception {
		int bands[][][] = new int[parameters.numberPredictionBands + 1][][];
		int value;
		boolean realBit = false;
		int context = -1;
		if(verbose || debugMode) {
			System.out.println("Coding BSQ");
		}
		try {
			RawImage image = new RawImage(inputFile, geo, originalPixelOrder, RawImage.READ);
			
			RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.READ, true);
			
			if(debugMode) {
				System.err.println("debugInfo: RawImage created");
			}
			
			int Previous1[][] = new int[ySize][xSize];
			int Previous2[][] = new int[ySize][xSize];
			if(icm != null) Previous2 = new int[ySize][xSize];
				
			DataOutputStream output = null;
			
			for (int z = 0; z < zSize; z ++) {
				
				if (verbose) {
					System.out.print("\rCoding band: " + z);
				}
				
				prepareBands(z, bands, it, ySize, xSize);
				
				int predictedSamples[][] = new int[ySize][xSize];
				
				switch(samplePrediction){
				case 2:
					//samplePredictor = new SamplePrediction(parameters);
					break;
				}
							
				
				switch(parameters.entropyCoderType){
				case CONS.SAMPLE_ADAPTIVE_ENCODER://Entropy Integer Code
					ec.init(z);
					for (int y = 0; y < ySize; y ++) {
					for (int x = 0; x < xSize; x ++) {
						value = predictor.compress(bands, z, y, x, parameters.numberPredictionBands, y, uq);
						ec.codeSample(value, y*xSize + x, z);
						ec.update(value, y*xSize + x, z);
						ec.updateHistogram(value);
					}}
					break;
				case CONS.BLOCK_ADAPTIVE_ENCODER://Block Adaptative Coder
					ec.init(z);
					for (int y = 0; y < ySize; y ++) {
					for (int x = 0; x < xSize; x ++) {
						value = predictor.compress(bands, z, y, x, parameters.numberPredictionBands, y, uq);
						ec.codeSample(value, y*xSize + x, z);
						ec.update(value, y*xSize + x, z);
						ec.updateHistogram(value);
					}}
					break;
	
				case CONS.ARITHMETIC_ENCODER_FLW:
					if(z == 0){
						ec.restartEncoding();
						ec.init(z);
					}
					cp.reset();
					
					
					/*
					if(z == 0){
						output = new DataOutputStream(new FileOutputStream("predicted"+this.inputFile,false));
					}
				*/
					
					
					
					switch(samplePrediction){
						case 0:
							for (int y = 0; y < ySize; y ++) {
							for (int x = 0; x < xSize; x ++) {
								predictedSamples[y][x] = bands[parameters.numberPredictionBands][y][x];			
							}}
							BSQAC(predictedSamples, Previous1, Previous2, z);
							break;
						case 1:
							
							for (int y = 0; y < ySize; y ++) {
							predictor.resetMSE();
							for (int x = 0; x < xSize; x ++) {
								predictedSamples[y][x] = predictor.compress(bands, z, y, x, parameters.numberPredictionBands, y, uq);		
								/*
								try {
									output.writeShort(predictedSamples[y][x]);
								} catch (IOException e) {
										System.err.println("No s'ha pogut ESCRIURE en el fitxer.");
								}
								*/
							}
							//System.out.println(z+":"+y+":"+predictor.getMSE(width)+":x");
							
							}
							
							BSQAC(predictedSamples, Previous1, Previous2, z);
							break;
						case 2:
							for (int y = 0; y < ySize; y ++) {
							for (int x = 0; x < xSize; x ++) {
								//int prediction = samplePredictor.predict(y, x);
								//predictedSamples[y][x] = samplePredictor.getMappedResidual(y, x, prediction);
							}}
							BSQACWithSign(predictedSamples, Previous1, Previous2, z);
						break;
					}
						
					
					break;
					
				case CONS.SYMBOL_ARITHMETIC_ENCODER_FLW:
					//System.out.println("SYMBOL_ARITHMETIC_ENCODER_FLW: "+samplePrediction);
					
					
					switch(samplePrediction){
						
						case 1:
							for (int y = 0; y < ySize; y ++) {
							for (int x = 0; x < xSize; x ++) {
								//predictedSamples[y][x] = predictor.compress(bands, z, y, x, parameters.numberPredictionBands, y, uq);	
								predictedSamples[y][x] = bands[parameters.numberPredictionBands][y][x];	
							}}
							if(z > 1){
								cmsymbol.setData(Previous2, Previous1, predictedSamples, 0, z);
								cmsymbol.buildProbabilityTable(z);
								double entropy = cmsymbol.getEntropy();
								double entropyINBPS = entropy / ((double)ySize*(double)xSize);
								System.out.println(z+" "+entropyINBPS);
							}
							//BSQAC(predictedSamples, Previous1, Previous2, z);
							break;
					}
					break;
					
					
				case CONS.INTERLEAVE_ENTROPY_CODER:
					for (int y = 0; y < ySize; y ++) {
					for (int x = 0; x < xSize; x ++) {
						predictedSamples[y][x] = predictor.compress(bands, z, y, x, parameters.numberPredictionBands, y, uq);
					}}
					for (int bit = 15; bit >= 0; bit--){
					for (int y = 0; y < ySize; y ++) {
					for (int x = 0; x < xSize; x ++) {
						 	realBit = (predictedSamples[y][x] & BIT_MASKS2[bit]) != 0;
							//int context = cm.getContext(predictedSamples, y, x, bit);//get context
							context = cm.getContext(predictedSamples, Previous1, z, y, x, bit);//get context
							int prob = cp.getProbability(context);//get probaility for the computed context
							cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
							iec.encodeBitProb(realBit, prob*2);//The probability for the IEC must be multiplied by a factor of two fit in the bins definition.
					}}}
					break;
				case CONS.DUMB_MQ_ENCODER:
					for (int y = 0; y < ySize; y ++) {
					for (int x = 0; x < xSize; x ++) {
						predictedSamples[y][x] = predictor.compress(bands, z, y, x, parameters.numberPredictionBands, y, uq);
					}}
					for (int bit = 15; bit >= 0; bit--){
					for (int y = 0; y < ySize; y ++) {
					for (int x = 0; x < xSize; x ++) {
							realBit = (predictedSamples[y][x] & BIT_MASKS2[bit]) != 0;
							context = cm.getContext(predictedSamples, y, x, bit);//get context
							//int context = cm.getContext(predictedSamples, predictedSamplesPrevious1, z, y, x, bit);//get context
							int prob = cp.getProbability(context);//get probaility for the computed context
							prob = 0xAC01-(int) ((float)prob / (1 << numBitsPrecision) * 0xAC01);//it uses the probability of one instead of 0. And the max value for the probability is 0xAC01
							cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
							dmq.encodeBitProb(realBit, prob);
					}}}
						
					break;
				
					
					
				case CONS.ENTROPY:
					for (int y = 0; y < ySize; y ++) {
					for (int x = 0; x < xSize; x ++) {
						predictedSamples[y][x] = predictor.compress(bands, z, y, x, parameters.numberPredictionBands, y, uq);
					}}
					for (int bit = 15; bit >= 0; bit--){
					for (int y = 0; y < ySize; y ++) {
					for (int x = 0; x < xSize; x ++) {
							realBit = (predictedSamples[y][x] & BIT_MASKS2[bit]) != 0;
							//int context = cm.getContext(predictedSamples, y, x, bit);//get context
							context = cm.getContext(predictedSamples, Previous1, z, y, x, bit);//get context
							//int context = cm.getContextMedical(predictedSamples, predictedSamplesPrevious1, predictedSamplesPrevious2, z, y, x, bit);//get context
							int prob = 0;
							if(probabilityModel == 3){
								prob = cp.getProbability(realBit,context);//get probability for the computed context
							}else{
								prob = cp.getProbability(context);//get probability for the computed context
							}
							
							cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
							double probq = prob / (double) (1 << numBitsPrecision);
														
							if(realBit){
								probq = 1 - probq;
							}
							
							assert(probq != 0);
							assert(probq != 1);
							
							double symbolCost = - (Math.log10(probq) / Math.log10(2));
							
							assert(symbolCost > 0);
							
							entropy += symbolCost; //https://en.wikipedia.org/wiki/Arithmetic_coding
					}}}
					break;
					
				case CONS.AIC:
					
					int maxCurrent = 0;
					int maxPrevious1 = 0;
					int maxPrevious2 = 0;
					int qstepCurrent = 0;
					int qstepPrevious1 = 0;
					int qstepPrevious2 = 0;
					
					for (int y = 0; y < ySize; y ++) {
					for (int x = 0; x < xSize; x ++) {
						if(maxCurrent < bands[parameters.numberPredictionBands][y][x]) maxCurrent = bands[parameters.numberPredictionBands][y][x];
					}}
					if(z > 0){
						for (int y = 0; y < ySize; y ++) {
						for (int x = 0; x < xSize; x ++) {
							if(maxPrevious1 < Previous1[y][x]) maxPrevious1 = Previous1[y][x];
						}}
					}
					if(z > 1){
						for (int y = 0; y < ySize; y ++) {
						for (int x = 0; x < xSize; x ++) {
							if(maxPrevious2 < Previous2[y][x]) maxPrevious2 = Previous2[y][x];
						}}
					}
					//System.out.println(maxCurrent+" "+maxPrevious1+" "+maxPrevious2);
					qstepCurrent = (int) Math.ceil((double)maxCurrent / (double)IntegerContexts);
					qstepPrevious1 = (int) Math.ceil((double)maxPrevious1 / (double)IntegerContexts);
					qstepPrevious2 = (int) Math.ceil((double)maxPrevious2 / (double)IntegerContexts);
					if(qstepCurrent == 0)qstepCurrent = 1;
					if(qstepPrevious1 == 0)qstepPrevious1 = 1;
					if(qstepPrevious2 == 0)qstepPrevious2 = 1;
					
					for (int y = 0; y < ySize; y ++) {
					for (int x = 0; x < xSize; x ++) {
						
						
						context = icm.getContext(bands, Previous1, Previous2, parameters.numberPredictionBands, y, x, qstepCurrent, qstepPrevious1, qstepPrevious2);//get context
						
						//System.out.println(context);
						int prob = icp.getProbability(bands[parameters.numberPredictionBands][y][x], context, maxCurrent, maxPrevious1, maxPrevious2);//get probability for the computed context
						//ec.encodeIntegerProb(bands[parameters.numberPredictionBands][y][x], prob);//encode the bit using the specific probability
						icp.updateSymbols(bands[parameters.numberPredictionBands][y][x], context);//updates the symbols decoded to properly compute the probability
						double probq = prob / (double) (1 << numBitsPrecision);
						double symbolCost = - (Math.log10(probq) / Math.log10(2));
						assert(symbolCost > 0);
						entropy += symbolCost; //https://en.wikipedia.org/wiki/Arithmetic_coding
						//System.out.println(probq+" "+symbolCost+" "+entropy);
						
						 
					}}
					
					break;
					
					default:
						throw new Error();
					
				}
				
				//When 3D contexts are used we need to copy the predicted into temporal structures
				if(contextModel == 13 || parameters.entropyCoderType == CONS.SYMBOL_ARITHMETIC_ENCODER_FLW){
					if(z == 0){
						for (int y = 0; y < ySize; y ++) {
						for (int x = 0; x < xSize; x ++) {
							Previous1[y][x]  = predictedSamples[y][x];
						}}
					}
					if(z > 0){
						for (int y = 0; y < ySize; y ++) {
						for (int x = 0; x < xSize; x ++) {
							Previous2[y][x]  = Previous1[y][x];
							Previous1[y][x]  = predictedSamples[y][x];
						}}
						
					}
					
				}
				
				
			}
			
			//cp.printContextState();
			
			switch(parameters.entropyCoderType){
				case CONS.SAMPLE_ADAPTIVE_ENCODER://Entropy Integer Code
					ec.getRate(verbose);
					ec.terminate(verbose);
					break;
				case CONS.BLOCK_ADAPTIVE_ENCODER://Block Adaptative Coder
					ec.getRate(verbose);
					ec.terminate(verbose);
					break;
		
				case CONS.ARITHMETIC_ENCODER_FLW:
					ec.terminate();
					fileStream.write(ec.getByteStream().getByteStream(),0,(int) ec.getByteStream().getLength());
					break;
					
				case CONS.INTERLEAVE_ENTROPY_CODER:
					iec.terminate();
					BitSet outputstream = iec.getOutput();
					for(int i = 0; i < outputstream.length(); i++){
						fbos.write(1, outputstream.get(i));
					}
					fbos.flush();
					break;
				
				case CONS.DUMB_MQ_ENCODER:
					dmq.terminate();
					break;
					
				
			}
			/*
			if(output != null){
				output.flush();
				output.close();
			}
			*/
			if(quantizer == 10){
				pw.flush();
				pw.close();
				uq.setWindowsize();
			}
			image.close(it);
	
		}catch(UnsupportedOperationException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(IndexOutOfBoundsException e) {
			e.printStackTrace();
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(ClassCastException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}
	}

	
	/**
	 * Encodes an image in BSQ order using Fixed Quantization Step Mode
	 *
	 * @param verbose indicates whether to display information
	 * @throws Exception 
	 */
//	BEFORE DOING THIS IS NECESSARY INITIALISATE BUFFERED WRITER BR_HIST AS CODER ARGUMENT BufferedWriter br_hist = null;

	private void codeBSQRateControl(boolean verbose) throws Exception {
		
		int bands[][][] = new int[parameters.numberPredictionBands + 1][][];
		int value = 0;
		FileWriter file = new FileWriter("qsteps.txt");
		PrintWriter pw = new PrintWriter(file);
		T = targetRate;
		List<Float> currentMedian = new ArrayList<Float>();
		List<Float> medianLine = new ArrayList<Float>();
		List<Float> medianBands = new ArrayList<Float>();
		List<Float> inputRate = new ArrayList<Float>();
		List<Float> outputRate = new ArrayList<Float>();
		
		int L = 17;
		int Qstep = 1;
		float [] updateQstepValues = new float[2];
		updateQstepValues[0] = Qstep;
		updateQstepValues[1] = T;

		if(verbose || debugMode) {
			System.out.println("Coding BSQ");
		}
		try {
			RawImage image = new RawImage(inputFile, geo, originalPixelOrder, RawImage.READ);
			
			RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.READ, true);
			
			if(debugMode) {
				System.err.println("debugInfo: RawImage created");
			}
			
			int predictedSamplesPrevious1[][] = new int[ySize][xSize];
			int predictedSamplesPrevious2[][] = new int[ySize][xSize];
			if(icm != null) predictedSamplesPrevious2 = new int[ySize][xSize];
				
			//DataOutputStream output = null;
			
			for (int z = 0; z < zSize; z ++) {
				
				if (verbose) {
					System.out.print("\rCoding band: " + z);
				}
				
				prepareBands(z, bands, it, ySize, xSize);
				
				int predictedSamples[][] = new int[ySize][xSize];
				
				if(z == 0){
					ec.restartEncoding();
					ec.init(z);
				}
				
				/*if(z == 0){
					output = new DataOutputStream(new FileOutputStream("predicted"+this.inputFile,false));
				}*/
				
				uq.setQuantizationStep(1);
				for (int y = 0; y < ySize; y ++) {
					for (int x = 0; x < xSize; x ++) {
						value = predictorRC.compress(bands, z, y, x, parameters.numberPredictionBands, y, uq);
						//System.out.println("value: "+value);
						currentMedian.add((float)value);
						
						if(x % L == L - 1){
							Collections.sort(currentMedian);
							//puts the median of a current L segment into a list that contains all the medians of the line
							medianLine.add(getMedianFromaList(currentMedian));
							//clears the current median segment	
							currentMedian.clear();
						}	
					}
					Collections.sort(medianLine);
					//puts the median of a current line into a list that contains the medians for all bands
					medianBands.add(getMedianFromaList(medianLine));
					//clears the current median line
					medianLine.clear();
				}
				
					this.targetRate = BSQupdateTargetRateValsesia(z, inputRate, outputRate, updateQstepValues, medianBands);
					updateQstepValues = updateQstep(medianBands, outputRate, this.targetRate, 1);
					Qstep = (int)updateQstepValues[0];
					pw.println(String.valueOf(Qstep));
					uq.setQuantizationStep(Qstep);
					//uq.setQuantizationStep(1);
				
				
				switch(parameters.entropyCoderType){
				
					case CONS.SAMPLE_ADAPTIVE_ENCODER://Entropy Integer Code
						//System.out.println("CONS.SAMPLE_ADAPTIVE_ENCODER");
						ec.init(z);
						for (int y = 0; y < ySize; y ++) {
						for (int x = 0; x < xSize; x ++) {
							
							value = predictor.compress(bands, z, y, x, parameters.numberPredictionBands, y, uq);
							ec.codeSample(value, y*xSize + x, z);
							ec.update(value, y*xSize + x, z);
							ec.updateHistogram(value);
						}}
						break;
						
					case CONS.BLOCK_ADAPTIVE_ENCODER://Block Adaptative Coder
						//System.out.println("CONS.BLOCK_ADAPTIVE_ENCODER");
						ec.init(z);
						for (int y = 0; y < ySize; y ++) {
						for (int x = 0; x < xSize; x ++) {
							value = predictor.compress(bands, z, y, x, parameters.numberPredictionBands, y, uq);
							ec.codeSample(value, y*xSize + x, z);
							ec.update(value, y*xSize + x, z);
							ec.updateHistogram(value);
						}}
						break;
		
					case CONS.ARITHMETIC_ENCODER_FLW:
						//System.out.println("CONS.ARITHMETIC_ENCODER_FLW");
						for (int y = 0; y < ySize; y ++) {
						for (int x = 0; x < xSize; x ++) {
							predictedSamples[y][x] = predictor.compress(bands, z, y, x, parameters.numberPredictionBands, y, uq);
						}}
						
						for (int bit = MAXBITS-1; bit >= 0; bit--){
						for (int y = 0; y < ySize; y ++) {
						for (int x = 0; x < xSize; x ++) {
						    boolean realBit = (predictedSamples[y][x] & BIT_MASKS2[bit]) != 0;
							//int context = cm.getContext(predictedSamples, y, x, bit);//get context
							//int context = cm.getContext(predictedSamples, predictedSamplesPrevious1, z, y, x, bit);//get context
							int context = cm.getContext(predictedSamples, predictedSamplesPrevious1, predictedSamplesPrevious2, z, y, x, bit);//get context
							int prob = cp.getProbability(context);//get probaility for the computed context
							cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
							ec.encodeBitProb(realBit, prob);//encode the bit using the specific probability
									
						}}}
					break;
				}
				
				
				
				/*if(z == 0){
					updateQstepValues = updateQstep(medianBands, inputRate, outputRate, this.targetRate);
					this.targetRate = updateTargetRateValsesia(z, inputRate, outputRate, updateQstepValues, medianBands);
				}*/
				medianBands.clear();
				
				//When 3D contexts are used we need to copy the predicted into temporal structures
				if(contextModel > 4){
					if(z == 0){
						for (int y = 0; y < ySize; y ++) {
						for (int x = 0; x < xSize; x ++) {
							predictedSamplesPrevious1[y][x]  = predictedSamples[y][x];
						}}
					}
					if(z > 0){
						for (int y = 0; y < ySize; y ++) {
						for (int x = 0; x < xSize; x ++) {
							predictedSamplesPrevious2[y][x]  = predictedSamplesPrevious1[y][x];
							predictedSamplesPrevious1[y][x]  = predictedSamples[y][x];
						}}
						
					}
				}
				
				
			}
			
			ec.terminate();
			if(parameters.entropyCoderType == CONS.ARITHMETIC_ENCODER_FLW){
				fileStream.write(ec.getByteStream().getByteStream(),0,(int) ec.getByteStream().getLength());
			}
			
					
			pw.flush();
			pw.close();
			/*if(output != null){
				output.flush();
				output.close();
			}*/
			image.close(it);
	
		}catch(UnsupportedOperationException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(IndexOutOfBoundsException e) {
			e.printStackTrace();
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(ClassCastException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}
	}
	
	


	/**
	 * Reads the next band of the image from the input file.
	 * @param it the BSQ iterator over the image
	 * @return the next band of the image
	 */
	private void readBand(RawImageIterator<int[]> it, int[][] band) {
		
		for(int i=0;i<geo[CONS.HEIGHT];i++) {
			band[i] = it.next();
		}
	}
	
	/**
	 * Tries to read the band z of the image, and reorders all the bands
	 * in memory.
	 * @param z the band to be read
	 * @param bands an array with all the bands needed in the compression process
	 * @param it the BSQ iterator over the image
	 */
	private void prepareBands(int z, int[][][] bands, RawImageIterator<int[]> it, int height, int width) {
		bands[0] = null;
		for(int i = 0; i < parameters.numberPredictionBands; i ++) {
			bands[i] = bands[i + 1];
		}
		bands[parameters.numberPredictionBands] = new int[height][];
		readBand(it, bands[parameters.numberPredictionBands]);
	}

	/**
	 * Encodes an image in BI order
	 *
	 * @param verbose indicates whether to display information
	 * @throws IOException if can not write information to the file
	 */
	private void codeBI(boolean verbose) throws IOException {

		int M = parameters.subframeInterleavingDepth;
		int auxValue = (geo[CONS.BANDS] % M == 0) ?
				geo[CONS.BANDS] / M :
					geo[CONS.BANDS] / M + 1;

		int value;
		int bands[][][] = new int[geo[CONS.BANDS]][2][];

		if(verbose || debugMode) {
			System.out.println("Coding BI");
		}
		try {
			RawImage image = new RawImage(inputFile, geo, originalPixelOrder, RawImage.READ);
			RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.READ, true);
			for (int y = 0; y < geo[CONS.HEIGHT]; y++) {
				prepareLines(y, bands, it);
				if (verbose && geo[CONS.HEIGHT] % 10 == 0) {
					System.out.print("\rCoding rows: " + y + " to " + Math.min(y+10, geo[CONS.HEIGHT]));
				}
				for (int i = 0; i < auxValue; i++) {
					for (int x = 0; x < geo[CONS.WIDTH]; x++) {
						for (int z = i * M; z < Math.min((i+1) * M, geo[CONS.BANDS]); z++) {
							if (x == 0 && y == 0) {
								ec.init(z);
							}
							value = predictor.compress(bands, z, y, x, z, 1, uq);
							ec.codeSample(value, y*geo[CONS.WIDTH] + x, z);
							ec.update(value, y*geo[CONS.WIDTH] + x, z);
							
						}
					}
				}
			}
			ec.terminate();
			image.close(it);
			if (verbose || debugMode) {
				System.out.print("\rCoding image finished");
			}
		}catch(UnsupportedOperationException e) {
			e.printStackTrace(System.err);
			
		}catch(IndexOutOfBoundsException e) {
			e.printStackTrace(System.err);
			
		}catch(ClassCastException e) {
			e.printStackTrace(System.err);
			
		}
	}
	

	
	
	
	private int[][][] getMask() {
		float[][][] maskSamples = null;
		int[][][] maskSamplesInt = new int[1][geo[CONS.HEIGHT]][geo[CONS.WIDTH]];
		if(maskFile != null){
			LoadFile mask = null;
			if(LoadFile.isRaw(maskFile)){
				try {
					mask = new LoadFile(maskFile, 1, geo[CONS.HEIGHT], geo[CONS.WIDTH], 1, 0, false);
				} catch (WarningException e) {
					e.printStackTrace();
				}
			}
			maskSamples = mask.getImage();	
		}
		
		for (int y = 0; y < geo[CONS.HEIGHT]; y++) {	
		for (int x = 0; x < geo[CONS.WIDTH]; x++) {
			maskSamplesInt[0][y][x] = (int) maskSamples[0][y][x];
		}}
		
		return maskSamplesInt;
	}
	
	//	
		private void codeBIACRateControlROI3(boolean verbose, int[] ROISamplesLine, int ROISamples, int[][][] maskSamples, int[] quantizationSteps) throws IOException, ParameterException, ClassNotFoundException, CloneNotSupportedException {
			PrintWriter pwBG = new PrintWriter(new FileWriter("qstepsBG.txt"));
			PrintWriter pwR = new PrintWriter(new FileWriter("qstepsROI.txt"));
			
			float targetRate = this.targetRate;
			//with low target bit-rates there are some imprecisions that can be tunned through adapting the initial target rate
			
			float targetRateROI = 0;
			float targetRateBG = 0;
			float inputT = targetRate;
			
			int M = parameters.subframeInterleavingDepth;
			
			
			float numbitsCurrentLine[] = new float[ySize];
			
			float deviationRateROI[] = new float[ySize];
			float numbitsBeforeROI[] = new float[ySize];
			float numbitsCurrentLineROI[] = new float[ySize];
			float deviationRateBG[] = new float[ySize];
			float numbitsBeforeBG[] = new float[ySize];
			float numbitsCurrentLineBG[] = new float[ySize];
			float numSamplesROI[] = new float[ySize];
			float numSamplesBG[] = new float[ySize];
			
			float targetBitsROIEstimation = 0;
			float targetBitsBGEstimation = 0;
			float targetBits = 0;
			float inputTROIEstimation = 0;
			float inputTBGEstimation = 0;
			int lines[][][] = new int[zSize][2][];
			int predictedLines[][][] = new int[zSize][2][xSize];
			int linesToEncode[][][] = new int[zSize][2][xSize];
			
			List<Float> currentMedian = new ArrayList<Float>();
			List<Float> medianLine = new ArrayList<Float>();
			List<Float> medianBands = new ArrayList<Float>();
			List<Float> currentMedianROI = new ArrayList<Float>();
			List<Float> medianLineROI = new ArrayList<Float>();
			List<Float> medianBandsROI = new ArrayList<Float>();
			List<Float> outputRateROI = new ArrayList<Float>();
			List<Float> currentMedianBG = new ArrayList<Float>();
			List<Float> medianLineBG = new ArrayList<Float>();
			List<Float> medianBandsBG = new ArrayList<Float>();
			List<Float> outputRateBG = new ArrayList<Float>();
			boolean EBits = true; //Enough bits for the ROI
			int L = 17;
			int QstepROI = 1;
			int QstepBG = 1;
			int QstepBGLast = 1;
			int inputQstep = uq.getQuantizationStep();
			float [] updateQstepValuesROI = new float[2];
			float [] updateQstepValuesBG = new float[2];
			float bpsinputQstep = 0;
			float BGbps = 0;
			int yROIFirst = -1;
			int yBGFirst = -1;
			
			try {
				RawImage image = new RawImage(inputFile, geo, originalPixelOrder, RawImage.READ);
				RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.READ, true);
				for (int y = 0; y < ySize; y++) {
					
					if(y != 0) {
						for(int i=0;i<zSize;i++) {
							lines[i][0] = linesToEncode[i][1];
							linesToEncode[i][0] = linesToEncode[i][1];
						}
					}
					for(int i=0;i<zSize;i++) {
						linesToEncode[i][1] = lines[i][1] = it.next();
						
					}
					
					
					if (verbose && ySize % 10 == 0) {
						System.out.print("\rCoding rows: " + y + " to " + Math.min(y+10, ySize));
					}
					
					uq.setQuantizationStep(1);
					
					
					for (int z = 0; z < zSize; z++) {
						for (int x = 0; x < xSize; x++) {
							
							
							
							int value = predictorRC.compress(lines, z, y, x, z, 1, uq);
							currentMedian.add((float)value);
							if(maskSamples[0][y][x] == 255)	currentMedianROI.add((float)value);
							else							currentMedianBG.add((float)value);
						
							if(x % L == L - 1){
								
								Collections.sort(currentMedian);
								Collections.sort(currentMedianROI);
								Collections.sort(currentMedianBG);
								//puts the median of a current L segment into a list that contains all the medians of the line
								medianLine.add(getMedianFromaList(currentMedian));
								medianLineROI.add(getMedianFromaList(currentMedianROI));
								medianLineBG.add(getMedianFromaList(currentMedianBG));
								//clears the current median segment	
								currentMedian.clear();
								currentMedianROI.clear();
								currentMedianBG.clear();
							}
						}
						
						Collections.sort(medianLine);
						Collections.sort(medianLineROI);
						Collections.sort(medianLineBG);
						//puts the median of a current line into a list that contains the medians for all bands
						medianBands.add(getMedianFromaList(medianLine));
						medianBandsROI.add(getMedianFromaList(medianLineROI));
						medianBandsBG.add(getMedianFromaList(medianLineBG));
						//clears the current median line
						medianLine.clear();
						medianLineROI.clear();
						medianLineBG.clear();
					}
						
					//QstepROI = 10;
					//QstepBG = 30;
					if(y == 0) {
						QstepROI = inputQstep;//quantizationSteps[0];
						QstepBG = inputQstep;//quantizationSteps[0];
						QstepBGLast = QstepBG;
					}
					
					/*if(QstepBG < QstepBGLast-1) {
						QstepBG = QstepBGLast-1;
					}
					if(QstepBG > QstepBGLast+1) {
						QstepBG = QstepBGLast+1;
					}
					QstepBGLast = QstepBG;
					*/
					
					pwR.println(String.valueOf(QstepROI));
					if(EBits == true) {
						//Per intentar apurar més el target bit-rate
						//if(BGbps != 0) {
						//	if(Math.abs(inputT - BGbps) > 0.0001) {
						//		if(inputT < BGbps) QstepBG = QstepBG + 1;
						//			else			   QstepBG = QstepBG - 1;
						//		}	
						//}
						/////////
						pwBG.println(String.valueOf(QstepBG));
					}
					else {
						pwBG.println(0);
					}
					
					
					
					for (int z = 0; z < zSize; z++) {
						for (int x = 0; x < xSize; x++) {
							predictedLines[z][0][x] = predictedLines[z][1][x];
							if(maskSamples[0][y][x] == 255) {
								if(yROIFirst == -1) yROIFirst = y;
								uq.setQuantizationStep(QstepROI);
								numSamplesROI[y]++;
							}else{		
								if(yBGFirst == -1) yBGFirst = y;
								uq.setQuantizationStep(QstepBG);
								numSamplesBG[y]++;	
							}
							int value = 0;
							value = predictor.compress(linesToEncode, z, y, x, z, 1, uq); 
							predictedLines[z][1][x] = value;
							
							
						}
						BIACROI3(M, z, y, predictedLines, maskSamples, numbitsCurrentLine, numbitsCurrentLineROI, numbitsCurrentLineBG);
					}
					
					float ROIbits = 0;
					float BGbits = 0;
					float totalbits = 0;
					float AccumulatedROISamples = 0;
					float AccumulatedBGSamples = 0;
					
					for(int i = 0; i < ySize; i++) {
						totalbits += numbitsCurrentLine[i];
						BGbits += numbitsCurrentLineBG[i];
						ROIbits += numbitsCurrentLineROI[i];
						AccumulatedROISamples += numSamplesROI[i];
						AccumulatedBGSamples += numSamplesBG[i];
					}
					
					
					
					targetBits = inputT * ((zSize * ySize * xSize));
					
					float remainingBits = targetBits - BGbits;
					inputTBGEstimation = remainingBits / ((zSize * ySize * xSize) - ROISamples - AccumulatedBGSamples);
					if(inputTBGEstimation < 0f) inputTBGEstimation = 0.000001f;
					
					
					BGbps = BGbits / ((zSize * ySize * xSize) - (ROISamples));
					float bpsline = BGbits / ((zSize * xSize * (y+1)));
					double MSELine = predictor.getMSE(xSize);
					predictor.resetMSE();
					System.out.println("line: "+y+" targetBits: "+targetBits+ " ("+inputT+") inputTBGEstimation: "+inputTBGEstimation+" BGbits:"+BGbits+" ("+BGbps+")"+" remainingBits:"+remainingBits+" QstepBG:"+QstepBG+" QstepROI:"+QstepROI+" ROIbits: "+numbitsCurrentLineROI[y]+" BGbits: "+numbitsCurrentLineBG[y]+" ("+bpsline+") MSELine: "+MSELine);
					
					
					
					
					if(numSamplesBG[y] != 0) {
						targetRateBG = BIupdateTargetRateValsesia(y, inputTBGEstimation, outputRateBG, numbitsBeforeBG, numbitsCurrentLineBG, deviationRateBG, numSamplesBG, yBGFirst);
						updateQstepValuesBG = updateQstep(medianBandsBG, outputRateBG, targetRateBG, 0);
						QstepBG = (int)updateQstepValuesBG[0];
						
					}else {
						targetRateBG = inputTBGEstimation;
					}
					
					
					
					
					medianBands.clear();
					medianBandsBG.clear();
					medianBandsROI.clear();
					
					//String area = "BG";
					//System.out.println(area+" y:"+(y+1)+" Q:"+inputQstep+" QBG:"+QstepBG+" QROI:"+QstepROI+" TRate:"+targetRate+" TRateROI:"+targetRateROI +"("+inputTROIEstimation+") TRateBG:"+targetRateBG+"("+inputTBGEstimation+") "
					//		+"bitsCLineROI[y]: "+numbitsCurrentLineROI[y]+"("+ROIbpsline+") ROIbits:"+ROIbits
					//		+" ("+ROIbps+") bitsCLineBG[y]: "+numbitsCurrentLineBG[y]+" ("+BGbpsline+") BGbits:"+BGbits+" ("+BGbps+") NSROI:"+numSamplesROI[y]+" NSBG:"+numSamplesBG[y]+" --> tbps:"+totalbps);
					
					
				}
				//System.out.println("predictor.getPAE(): "+predictor.getPAE());
				//System.out.println("predictor.getMSE(): "+predictor.getMSE(geo[CONS.BANDS]*geo[CONS.WIDTH]*geo[CONS.HEIGHT]));
				
				pwR.flush();
				pwBG.flush();
				pwR.close();
				pwBG.close();
				
				float BGbits = 0;
				float ROIbits = 0;
				float totalbits = 0;
				float AccumulatedROISamples = 0;
				float AccumulatedBGSamples = 0;
				
				for(int i = 0; i < ySize; i++) {
					totalbits += numbitsCurrentLine[i];
					BGbits += numbitsCurrentLineBG[i];
					ROIbits += numbitsCurrentLineROI[i];
					AccumulatedROISamples += numSamplesROI[i];
					AccumulatedBGSamples += numSamplesBG[i];
				}
				
				float bps = totalbits / ((zSize * ySize * xSize));
				float ROIbps = ROIbits / ((zSize * ySize * xSize));
			    BGbps = BGbits / ((zSize * ySize * xSize));
				
				System.out.println(bps+" "+ROIbps+" "+BGbps);
				ec.terminate();
				fileStream.write(ec.getByteStream().getByteStream(),0,(int) ec.getByteStream().getLength());
				image.close(it);
				if (verbose || debugMode) {
					System.out.print("\rCoding image finished");
				}
			}catch(UnsupportedOperationException e) {
				e.printStackTrace(System.err);
				
			}catch(IndexOutOfBoundsException e) {
				e.printStackTrace(System.err);
				
			}catch(ClassCastException e) {
				e.printStackTrace(System.err);
				
			}
			
		}
		
	//TODO
	private void codeBIACRateControlROI2(boolean verbose, int[] ROISamplesLine, int ROISamples, int[][][] maskSamples, int[] quantizationSteps) throws IOException, ParameterException, ClassNotFoundException, CloneNotSupportedException {
		PrintWriter pwA = new PrintWriter(new FileWriter("auxiliary.txt"));
		PrintWriter pwBG = new PrintWriter(new FileWriter("qstepsBG.txt"));
		PrintWriter pwR = new PrintWriter(new FileWriter("qstepsROI.txt"));
		
		float targetRate = this.targetRate;
		//with low target bit-rates there are some imprecisions that can be tunned through adapting the initial target rate
		
		float targetRateROI = 0;
		float targetRateBG = 0;
		float inputT = targetRate;
		
		int M = parameters.subframeInterleavingDepth;
		
		
		float numbitsCurrentLine[] = new float[ySize];
		
		float deviationRateROI[] = new float[ySize];
		float numbitsBeforeROI[] = new float[ySize];
		float numbitsCurrentLineROI[] = new float[ySize];
		float deviationRateBG[] = new float[ySize];
		float numbitsBeforeBG[] = new float[ySize];
		float numbitsCurrentLineBG[] = new float[ySize];
		float numSamplesROI[] = new float[ySize];
		float numSamplesBG[] = new float[ySize];
		
		float targetBitsROIEstimation = 0;
		float targetBitsBGEstimation = 0;
		float targetBits = 0;
		float inputTROIEstimation = 0;
		float inputTBGEstimation = 0;
		int lines[][][] = new int[zSize][2][];
		int predictedLines[][][] = new int[zSize][2][xSize];
		int predictedLinesS[][][] = new int[zSize][2][xSize];
		int linesToEncode[][][] = new int[zSize][2][xSize];
		
		List<Float> currentMedian = new ArrayList<Float>();
		List<Float> medianLine = new ArrayList<Float>();
		List<Float> medianBands = new ArrayList<Float>();
		List<Float> currentMedianROI = new ArrayList<Float>();
		List<Float> medianLineROI = new ArrayList<Float>();
		List<Float> medianBandsROI = new ArrayList<Float>();
		List<Float> outputRateROI = new ArrayList<Float>();
		List<Float> currentMedianBG = new ArrayList<Float>();
		List<Float> medianLineBG = new ArrayList<Float>();
		List<Float> medianBandsBG = new ArrayList<Float>();
		List<Float> outputRateBG = new ArrayList<Float>();
		boolean EBits = true; //Enough bits for the ROI
		int L = 17;
		int QstepROI = 1;
		int QstepBG = 1;
		int inputQstep = uq.getQuantizationStep();
		float [] updateQstepValuesROI = new float[2];
		float [] updateQstepValuesBG = new float[2];
		float bpsinputQstep = 0;
	
		float ROIbits = 0;
		float totalbits = 0;
		float AccumulatedROISamples = 0;
		float AccumulatedBGSamples = 0;
		
		int yROIFirst = -1;
		int yBGFirst = -1;
		
		try {
			RawImage image = new RawImage(inputFile, geo, originalPixelOrder, RawImage.READ);
			RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.READ, true);
			for (int y = 0; y < ySize; y++) {
				
				if(y != 0) {
					for(int i=0;i<zSize;i++) {
						lines[i][0] = linesToEncode[i][1];
						linesToEncode[i][0] = linesToEncode[i][1];
					}
				}
				for(int i=0;i<zSize;i++) {
					linesToEncode[i][1] = lines[i][1] = it.next();
					
				}
				
				
				if (verbose && ySize % 10 == 0) {
					System.out.print("\rCoding rows: " + y + " to " + Math.min(y+10, ySize));
				}
				
				uq.setQuantizationStep(1);
				
				for (int z = 0; z < zSize; z++) {
					for (int x = 0; x < xSize; x++) {
						
						int value = predictorRC.compress(lines, z, y, x, z, 1, uq);
						predictedLinesS[z][1][x] = value;
						currentMedian.add((float)value);
						if(maskSamples[0][y][x] == 255)	currentMedianROI.add((float)value);
						else							currentMedianBG.add((float)value);
					
						if(x % L == L - 1){
							
							Collections.sort(currentMedian);
							Collections.sort(currentMedianROI);
							Collections.sort(currentMedianBG);
							//puts the median of a current L segment into a list that contains all the medians of the line
							medianLine.add(getMedianFromaList(currentMedian));
							medianLineROI.add(getMedianFromaList(currentMedianROI));
							medianLineBG.add(getMedianFromaList(currentMedianBG));
							//clears the current median segment	
							currentMedian.clear();
							currentMedianROI.clear();
							currentMedianBG.clear();
						}
					}
					
					Collections.sort(medianLine);
					Collections.sort(medianLineROI);
					Collections.sort(medianLineBG);
					//puts the median of a current line into a list that contains the medians for all bands
					medianBands.add(getMedianFromaList(medianLine));
					medianBandsROI.add(getMedianFromaList(medianLineROI));
					medianBandsBG.add(getMedianFromaList(medianLineBG));
					//clears the current median line
					medianLine.clear();
					medianLineROI.clear();
					medianLineBG.clear();
				}
					
				//QstepROI = 10;
				//QstepBG = 30;
				if(y == 0) {
					QstepROI = quantizationSteps[0];
					QstepBG = quantizationSteps[0];
				}
				
				
				pwR.println(String.valueOf(QstepROI));
				if(EBits == true) pwBG.println(String.valueOf(QstepBG));
				else {
					pwBG.println(0);
				}
				
				/////////After simulating coding the first line the amount of bits needed for ROI coding are estimated. Considering to encode the ROI only or also BG data
				if(y == 0)  {
					totalbits = 0;
					for (int z = 0; z < zSize; z++) {
						totalbits  += BIACS(M, z, y, predictedLinesS, maskSamples);
					}
					
					////////Counting bits for encoding the first line losslessly////////
					//totalbits = 0;
					//for(int i = 0; i < ySize; i++) {
					//	totalbits += numbitsCurrentLine[i];
					//}
					////////////////////////////////////////////////////////
					
					////////Estimating the amount of bits needed for the ROI coding.
					bpsinputQstep = totalbits / (float)(xSize*zSize);
					targetBitsROIEstimation = bpsinputQstep * (float)ROISamples;
					targetBits = inputT * zSize * ySize * xSize;
					if(targetBits < targetBitsROIEstimation) {
						targetBitsROIEstimation = targetBits;
						EBits = false;
					}else {
						EBits = true;
					}
					//saving auxiliary data to know if there are enough bits for coding some BG data
					if(EBits) pwA.println(1);
					else      pwA.println(0);
				}
				////////////////////////////////////////////////////////

						
				for (int z = 0; z < zSize; z++) {
					predictor.canBeInizialized = true;
					predictor.resetMSE();
					for (int x = 0; x < xSize; x++) {
						predictedLines[z][0][x] = predictedLines[z][1][x];
						if(maskSamples[0][y][x] == 255) {
							if(yROIFirst == -1) yROIFirst = y;
							uq.setQuantizationStep(QstepROI);
							numSamplesROI[y]++;
						}else{		
							if(yBGFirst == -1) yBGFirst = y;
							uq.setQuantizationStep(QstepBG);
							numSamplesBG[y]++;	
						}
						int value = 0;
						//if(y == 0 || (y > 0 && maskSamples[0][y][x] == 255 ) || EBits == true) {
						if(maskSamples[0][y][x] == 255 || EBits == true) {
							value = predictor.compressROI(linesToEncode, z, y, x, z, 1, uq, yROIFirst); 
							predictedLines[z][1][x] = value;
						}else {
							linesToEncode[z][1][x] = 0;
							value = 0;
							predictedLines[z][1][x] = value;
						}
						
					}
					if(numSamplesBG[y] == xSize) yROIFirst = -1;
					BIACROI(M, z, y, predictedLines, maskSamples, numbitsCurrentLine, numbitsCurrentLineROI, numbitsCurrentLineBG, EBits, yROIFirst);
				}
				
				
				////////Counting ROI and total bits, and ROI and BG Samples encoded////////
				ROIbits = 0;
				totalbits = 0;
				AccumulatedROISamples = 0;
				AccumulatedBGSamples = 0;
				for(int i = 0; i < ySize; i++) {
					totalbits += numbitsCurrentLine[i];
					ROIbits += numbitsCurrentLineROI[i];
					AccumulatedROISamples += numSamplesROI[i];
					AccumulatedBGSamples += numSamplesBG[i];
				}
				if(y == 0) System.out.println("totalbits: "+totalbits);
				////////////////////////////////////////////////////////
				
				
				if(ROIbits > 0) bpsinputQstep = ROIbits / (AccumulatedROISamples*zSize);
				else bpsinputQstep = 0.000001f;
				targetBitsROIEstimation = bpsinputQstep * (float)(ROISamples - AccumulatedROISamples);
				float remainingBits = targetBits - totalbits;
				if(EBits == false) {
					targetBitsBGEstimation = 0.000001f;
				}else {
					targetBitsBGEstimation = remainingBits - targetBitsROIEstimation;
				}
				
				if(targetBitsROIEstimation > 0) {
					inputTROIEstimation = remainingBits / (float)(ROISamples - AccumulatedROISamples);
				}else {
					inputTROIEstimation = 0.000001f;
				}
				
				if(targetBitsBGEstimation > 0) {
					inputTBGEstimation = targetBitsBGEstimation / ((zSize * ySize * xSize) - ROISamples - AccumulatedBGSamples);
				}else {
					inputTBGEstimation = 0.000001f;
				}
				
				//System.out.println("remainingBits:"+remainingBits+" bpsinputQstep:"+bpsinputQstep+" targetBitsROIEstimation:"+targetBitsROIEstimation +"("+inputTROIEstimation+") targetBitsBGEstimation: "+targetBitsBGEstimation+ " ("+inputTBGEstimation+")");
				
				String area = "BG";
				
				if(numSamplesBG[y] != 0) {
					targetRateBG = BIupdateTargetRateValsesia(y, inputTBGEstimation, outputRateBG, numbitsBeforeBG, numbitsCurrentLineBG, deviationRateBG, numSamplesBG, yBGFirst);
					updateQstepValuesBG = updateQstep(medianBandsBG, outputRateBG, targetRateBG, 0);
					QstepBG = (int)updateQstepValuesBG[0];
				}else {
					targetRateBG = inputTBGEstimation;
				}
				
				if(numSamplesROI[y] != 0) {
					area = "ROI";
					targetRateROI = BIupdateTargetRateValsesia(y, inputTROIEstimation, outputRateROI, numbitsBeforeROI, numbitsCurrentLineROI, deviationRateROI, numSamplesROI, yROIFirst);
					updateQstepValuesROI = updateQstep(medianBandsROI, outputRateROI, targetRateROI, 0);
					QstepROI = (int)updateQstepValuesROI[0];
				}else {
					targetRateROI = inputTROIEstimation;
					QstepROI = 0;
				}
				if(EBits == true) {
					QstepROI = inputQstep;
				}
				
				//max error restriction over the ROI area
				if(QstepROI != 0 && QstepROI < quantizationSteps[0]) QstepROI = quantizationSteps[0];
				if(QstepBG != 0 && QstepBG < QstepROI) QstepROI = QstepBG;
				//if(EBits == false) QstepBG = 0;
				
				
				
				medianBands.clear();
				medianBandsBG.clear();
				medianBandsROI.clear();
				
				//System.out.println(area+" y:"+(y+1)+" Q:"+inputQstep+" QBG:"+QstepBG+" QROI:"+QstepROI+" TRate:"+targetRate+" TRateROI:"+targetRateROI +"("+inputTROIEstimation+") TRateBG:"+targetRateBG+"("+inputTBGEstimation+") "
				//		+"bitsCLineROI[y]: "+numbitsCurrentLineROI[y]+"("+ROIbpsline+") ROIbits:"+ROIbits
				//		+" ("+ROIbps+") bitsCLineBG[y]: "+numbitsCurrentLineBG[y]+" ("+BGbpsline+") BGbits:"+BGbits+" ("+BGbps+") NSROI:"+numSamplesROI[y]+" NSBG:"+numSamplesBG[y]+" --> tbps:"+totalbps);
				
				
			}
			//System.out.println("predictor.getPAE(): "+predictor.getPAE());
			//System.out.println("predictor.getMSE(): "+predictor.getMSE(geo[CONS.BANDS]*geo[CONS.WIDTH]*geo[CONS.HEIGHT]));
			
			pwR.flush();
			pwBG.flush();
			pwA.flush();
			pwR.close();
			pwBG.close();
			pwA.close();
			
			float BGbits = 0;
			ROIbits = 0;
			totalbits = 0;
			AccumulatedROISamples = 0;
			AccumulatedBGSamples = 0;
			
			for(int i = 0; i < ySize; i++) {
				totalbits += numbitsCurrentLine[i];
				BGbits += numbitsCurrentLineBG[i];
				ROIbits += numbitsCurrentLineROI[i];
				AccumulatedROISamples += numSamplesROI[i];
				AccumulatedBGSamples += numSamplesBG[i];
			}
			
			float bps = totalbits / ((zSize * ySize * xSize));
			float ROIbps = ROIbits / ((zSize * ySize * xSize));
			//float BGbps = BGbits / ((zSize * ySize * xSize) - (ROISamples)); //employ following line instead of this one depending on how the bps are calculated (Uncomment similar line upper)
			float BGbps = BGbits / ((zSize * ySize * xSize));
			
			System.out.println(bps+" "+ROIbps+" "+BGbps);
			
			
			ec.terminate();
			fileStream.write(ec.getByteStream().getByteStream(),0,(int) ec.getByteStream().getLength());
			image.close(it);
			if (verbose || debugMode) {
				System.out.print("\rCoding image finished");
			}
		}catch(UnsupportedOperationException e) {
			e.printStackTrace(System.err);
			
		}catch(IndexOutOfBoundsException e) {
			e.printStackTrace(System.err);
			
		}catch(ClassCastException e) {
			e.printStackTrace(System.err);
			
		}
		
		
	}
	
	
	private void codeBIACRateControlROI(boolean verbose, int[] ROISamplesLine, int ROISamples, int[][][] maskSamples, int[] quantizationSteps) throws IOException, ParameterException, ClassNotFoundException, CloneNotSupportedException {
		PrintWriter pwA = new PrintWriter(new FileWriter("auxiliary.txt"));
		PrintWriter pwBG = new PrintWriter(new FileWriter("qstepsBG.txt"));
		PrintWriter pwR = new PrintWriter(new FileWriter("qstepsROI.txt"));
		
		float targetRate = this.targetRate;
		//with low target bit-rates there are some imprecisions that can be tunned through adapting the initial target rate
		
		float targetRateROI = 0;
		float targetRateBG = 0;
		float inputT = targetRate;
		
		int M = parameters.subframeInterleavingDepth;
		
		
		float numbitsCurrentLine[] = new float[ySize];
		
		float deviationRateROI[] = new float[ySize];
		float numbitsBeforeROI[] = new float[ySize];
		float numbitsCurrentLineROI[] = new float[ySize];
		float deviationRateBG[] = new float[ySize];
		float numbitsBeforeBG[] = new float[ySize];
		float numbitsCurrentLineBG[] = new float[ySize];
		float numSamplesROI[] = new float[ySize];
		float numSamplesBG[] = new float[ySize];
		
		float targetBitsROIEstimation = 0;
		float targetBitsBGEstimation = 0;
		float targetBits = 0;
		float inputTROIEstimation = 0;
		float inputTBGEstimation = 0;
		int lines[][][] = new int[zSize][2][];
		int predictedLines[][][] = new int[zSize][2][xSize];
		int linesToEncode[][][] = new int[zSize][2][xSize];
		
		List<Float> currentMedian = new ArrayList<Float>();
		List<Float> medianLine = new ArrayList<Float>();
		List<Float> medianBands = new ArrayList<Float>();
		List<Float> currentMedianROI = new ArrayList<Float>();
		List<Float> medianLineROI = new ArrayList<Float>();
		List<Float> medianBandsROI = new ArrayList<Float>();
		List<Float> outputRateROI = new ArrayList<Float>();
		List<Float> currentMedianBG = new ArrayList<Float>();
		List<Float> medianLineBG = new ArrayList<Float>();
		List<Float> medianBandsBG = new ArrayList<Float>();
		List<Float> outputRateBG = new ArrayList<Float>();
		boolean EBits = true; //Enough bits for the ROI
		int L = 17;
		int QstepROI = 1;
		int QstepBG = 1;
		int inputQstep = uq.getQuantizationStep();
		float [] updateQstepValuesROI = new float[2];
		float [] updateQstepValuesBG = new float[2];
		float bpsinputQstep = 0;
	
		int yROIFirst = -1;
		int yBGFirst = -1;
		
		try {
			RawImage image = new RawImage(inputFile, geo, originalPixelOrder, RawImage.READ);
			RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.READ, true);
			for (int y = 0; y < ySize; y++) {
				
				if(y != 0) {
					for(int i=0;i<zSize;i++) {
						lines[i][0] = linesToEncode[i][1];
						linesToEncode[i][0] = linesToEncode[i][1];
					}
				}
				for(int i=0;i<zSize;i++) {
					linesToEncode[i][1] = lines[i][1] = it.next();
					
				}
				
				
				if (verbose && ySize % 10 == 0) {
					System.out.print("\rCoding rows: " + y + " to " + Math.min(y+10, ySize));
				}
				
				uq.setQuantizationStep(1);
				
				for (int z = 0; z < zSize; z++) {
					for (int x = 0; x < xSize; x++) {
						
						int value = predictorRC.compress(lines, z, y, x, z, 1, uq);
						currentMedian.add((float)value);
						if(maskSamples[0][y][x] == 255)	currentMedianROI.add((float)value);
						else							currentMedianBG.add((float)value);
					
						if(x % L == L - 1){
							
							Collections.sort(currentMedian);
							Collections.sort(currentMedianROI);
							Collections.sort(currentMedianBG);
							//puts the median of a current L segment into a list that contains all the medians of the line
							medianLine.add(getMedianFromaList(currentMedian));
							medianLineROI.add(getMedianFromaList(currentMedianROI));
							medianLineBG.add(getMedianFromaList(currentMedianBG));
							//clears the current median segment	
							currentMedian.clear();
							currentMedianROI.clear();
							currentMedianBG.clear();
						}
					}
					
					Collections.sort(medianLine);
					Collections.sort(medianLineROI);
					Collections.sort(medianLineBG);
					//puts the median of a current line into a list that contains the medians for all bands
					medianBands.add(getMedianFromaList(medianLine));
					medianBandsROI.add(getMedianFromaList(medianLineROI));
					medianBandsBG.add(getMedianFromaList(medianLineBG));
					//clears the current median line
					medianLine.clear();
					medianLineROI.clear();
					medianLineBG.clear();
				}
					
				//QstepROI = 10;
				//QstepBG = 30;
				if(y == 0) {
					QstepROI = quantizationSteps[0];
					QstepBG = quantizationSteps[0];
				}
				
				
				pwR.println(String.valueOf(QstepROI));
				if(EBits == true) pwBG.println(String.valueOf(QstepBG));
				else {
					pwBG.println(0);
				}
				
				
				for (int z = 0; z < zSize; z++) {
					predictor.resetMSE();
					for (int x = 0; x < xSize; x++) {
						predictedLines[z][0][x] = predictedLines[z][1][x];
						if(maskSamples[0][y][x] == 255) {
							if(yROIFirst == -1) yROIFirst = y;
							uq.setQuantizationStep(QstepROI);
							numSamplesROI[y]++;
						}else{		
							if(yBGFirst == -1) yBGFirst = y;
							uq.setQuantizationStep(QstepBG);
							numSamplesBG[y]++;	
						}
						int value = 0;
						if(y == 0 || (y > 0 && maskSamples[0][y][x] == 255 ) || EBits == true) {
							value = predictor.compress(linesToEncode, z, y, x, z, 1, uq); 
							predictedLines[z][1][x] = value;
						}else {
							linesToEncode[z][1][x] = 0;
							value = 0;
							predictedLines[z][1][x] = value;
						}
						
					}
					if(numSamplesBG[y] == xSize) yROIFirst = -1;
					BIAC(M, z, y, predictedLines, maskSamples, numbitsCurrentLine, numbitsCurrentLineROI, numbitsCurrentLineBG, EBits);
				}
				
				float ROIbits = 0;
				float totalbits = 0;
				float AccumulatedROISamples = 0;
				float AccumulatedBGSamples = 0;
				
				for(int i = 0; i < ySize; i++) {
					totalbits += numbitsCurrentLine[i];
					ROIbits += numbitsCurrentLineROI[i];
					AccumulatedROISamples += numSamplesROI[i];
					AccumulatedBGSamples += numSamplesBG[i];
				}
				
				
				if(y == 0 )  {
					bpsinputQstep = totalbits / (float)(xSize*zSize);
					targetBitsROIEstimation = bpsinputQstep * (float)ROISamples;
					targetBits = inputT * zSize * ySize * xSize;
					if(targetBits < targetBitsROIEstimation) {
						targetBitsROIEstimation = targetBits;
						EBits = false;
					}else {
						EBits = true;
					}
					//saving auxiliary data to know if there are enough bits for coding some BG data
					if(EBits) pwA.println(1);
					else      pwA.println(0);
				}
	

				if(ROIbits > 0) bpsinputQstep = ROIbits / (AccumulatedROISamples*zSize);
				else bpsinputQstep = 0.000001f;
				targetBitsROIEstimation = bpsinputQstep * (float)(ROISamples - AccumulatedROISamples);
				float remainingBits = targetBits - totalbits;
				if(EBits == false) {
					targetBitsBGEstimation = 0.000001f;
				}else {
					targetBitsBGEstimation = remainingBits - targetBitsROIEstimation;
				}
				
				if(targetBitsROIEstimation > 0) {
					inputTROIEstimation = remainingBits / (float)(ROISamples - AccumulatedROISamples);
				}else {
					inputTROIEstimation = 0.000001f;
				}
				
				if(targetBitsBGEstimation > 0) {
					inputTBGEstimation = targetBitsBGEstimation / ((zSize * ySize * xSize) - ROISamples - AccumulatedBGSamples);
				}else {
					inputTBGEstimation = 0.000001f;
				}
				
				//System.out.println("remainingBits:"+remainingBits+" bpsinputQstep:"+bpsinputQstep+" targetBitsROIEstimation:"+targetBitsROIEstimation +"("+inputTROIEstimation+") targetBitsBGEstimation: "+targetBitsBGEstimation+ " ("+inputTBGEstimation+")");
				
				String area = "BG";
				
				if(numSamplesBG[y] != 0) {
					targetRateBG = BIupdateTargetRateValsesia(y, inputTBGEstimation, outputRateBG, numbitsBeforeBG, numbitsCurrentLineBG, deviationRateBG, numSamplesBG, yBGFirst);
					updateQstepValuesBG = updateQstep(medianBandsBG, outputRateBG, targetRateBG, 0);
					QstepBG = (int)updateQstepValuesBG[0];
				}else {
					targetRateBG = inputTBGEstimation;
				}
				
				if(numSamplesROI[y] != 0) {
					area = "ROI";
					targetRateROI = BIupdateTargetRateValsesia(y, inputTROIEstimation, outputRateROI, numbitsBeforeROI, numbitsCurrentLineROI, deviationRateROI, numSamplesROI, yROIFirst);
					updateQstepValuesROI = updateQstep(medianBandsROI, outputRateROI, targetRateROI, 0);
					QstepROI = (int)updateQstepValuesROI[0];
				}else {
					targetRateROI = inputTROIEstimation;
					QstepROI = 0;
				}
				if(EBits == true) {
					QstepROI = inputQstep;
				}
				
				//max error restriction over the ROI area
				if(QstepROI != 0 && QstepROI < quantizationSteps[0]) QstepROI = quantizationSteps[0];
				if(QstepBG != 0 && QstepBG < QstepROI) QstepROI = QstepBG;
				//if(EBits == false) QstepBG = 0;
				
				
				
				medianBands.clear();
				medianBandsBG.clear();
				medianBandsROI.clear();
				
				//System.out.println(area+" y:"+(y+1)+" Q:"+inputQstep+" QBG:"+QstepBG+" QROI:"+QstepROI+" TRate:"+targetRate+" TRateROI:"+targetRateROI +"("+inputTROIEstimation+") TRateBG:"+targetRateBG+"("+inputTBGEstimation+") "
				//		+"bitsCLineROI[y]: "+numbitsCurrentLineROI[y]+"("+ROIbpsline+") ROIbits:"+ROIbits
				//		+" ("+ROIbps+") bitsCLineBG[y]: "+numbitsCurrentLineBG[y]+" ("+BGbpsline+") BGbits:"+BGbits+" ("+BGbps+") NSROI:"+numSamplesROI[y]+" NSBG:"+numSamplesBG[y]+" --> tbps:"+totalbps);
				
				
			}
			//System.out.println("predictor.getPAE(): "+predictor.getPAE());
			//System.out.println("predictor.getMSE(): "+predictor.getMSE(geo[CONS.BANDS]*geo[CONS.WIDTH]*geo[CONS.HEIGHT]));
			
			pwR.flush();
			pwBG.flush();
			pwA.flush();
			pwR.close();
			pwBG.close();
			pwA.close();
			
			ec.terminate();
			
			float bps = (ec.getByteStream().getLength()*8) / ((zSize * ySize * xSize));
			float ROIbps = -1;
			float BGbps = -1;
			
			System.out.println(bps+" "+ROIbps+" "+BGbps);
			
			
			fileStream.write(ec.getByteStream().getByteStream(),0,(int) ec.getByteStream().getLength());
			image.close(it);
			if (verbose || debugMode) {
				System.out.print("\rCoding image finished");
			}
		}catch(UnsupportedOperationException e) {
			e.printStackTrace(System.err);
			
		}catch(IndexOutOfBoundsException e) {
			e.printStackTrace(System.err);
			
		}catch(ClassCastException e) {
			e.printStackTrace(System.err);
			
		}
		
		
	}

	
	private double BIACS(int M, int z, int y, int predictedLines[][][], int maskSamples[][][]){
		double numbits = 0;
		for (int bit = 15; bit >= 0; bit--){
		for (int x = 0; x < xSize; x ++) {
				boolean realBit = false;
				int context = 0;
				realBit = (predictedLines[z][1][x] & BIT_MASKS2[bit]) != 0;
				context = cm.getContext(predictedLines, z, 1, x, bit);//get context
				int prob = cps.getProbability(context);//get probability for the computed context
				cps.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
					
				double probq = prob / (double) (1 << numBitsPrecision);
				
				if(realBit){
					probq = 1 - probq;
				}
				
				assert(probq != 0);
				assert(probq != 1);
				
				double symbolCost = - (Math.log10(probq) / Math.log10(2));
				
				assert(symbolCost > 0);
				
				numbits += symbolCost; //https://en.wikipedia.org/wiki/Arithmetic_coding	
		}}
		return numbits;
	}

	private void BIACROI3(int M, int z, int y, int predictedLines[][][], int maskSamples[][][], float numbitsCurrentLine[], float numbitsCurrentLineROI[],  float numbitsCurrentLineBG[]){
		for (int bit = 15; bit >= 0; bit--){
		for (int x = 0; x < xSize; x ++) {
				if (x == 0 && y == 0 && z == 0 && bit == 15) {
					ec.init(z);
				}
				
				boolean realBit = false;
				int context = 0;
				realBit = (predictedLines[z][1][x] & BIT_MASKS2[bit]) != 0;
				context = cm.getContext(predictedLines, z, 1, x, bit);//get context
				int prob = cp.getProbability(context);//get probability for the computed context
				cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
				long SizeBitstreamBeforeCoding = ec.getNumBitsWritten();
				ec.encodeBitProb(realBit, prob);//encode the bit using the specific probability
				//get the amount of bits used for the ROI and the BG
				if(maskSamples[0][y][x] == 255) {
					numbitsCurrentLineROI[y] = numbitsCurrentLineROI[y] + (float)(ec.getNumBitsWritten() - SizeBitstreamBeforeCoding );
				}else{
					numbitsCurrentLineBG[y] = numbitsCurrentLineBG[y] + (float)(ec.getNumBitsWritten() - SizeBitstreamBeforeCoding );
				}
				numbitsCurrentLine[y] = numbitsCurrentLine[y] + (float)(ec.getNumBitsWritten() - SizeBitstreamBeforeCoding );
				
				
		}}
	}
	
	private void BIACROI(int M, int z, int y, int predictedLines[][][], int maskSamples[][][], float numbitsCurrentLine[], float numbitsCurrentLineROI[],  float numbitsCurrentLineBG[], boolean EBits, int yROIFirst){
		for (int bit = 15; bit >= 0; bit--){
		for (int x = 0; x < xSize; x ++) {
				if (x == 0 && y == 0 && z == 0 && bit == 15) {
					ec.init(z);
				}
				
				//if(y == 0 || (y > 0 && maskSamples[0][y][x] == 255 ) || EBits == true) {
				if(maskSamples[0][y][x] == 255 || EBits == true) {
					boolean realBit = false;
					int context = 0;
					realBit = (predictedLines[z][1][x] & BIT_MASKS2[bit]) != 0;
					context = cm.getContext(predictedLines, z, 1, x, bit);//get context
					int prob = cp.getProbability(context);//get probability for the computed context
					cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
					long SizeBitstreamBeforeCoding = ec.getNumBitsWritten();
					ec.encodeBitProb(realBit, prob);//encode the bit using the specific probability
					//get the amount of bits used for the ROI and the BG
					if(maskSamples[0][y][x] == 255) {
						numbitsCurrentLineROI[y] = numbitsCurrentLineROI[y] + (float)(ec.getNumBitsWritten() - SizeBitstreamBeforeCoding );
					}else{
						numbitsCurrentLineBG[y] = numbitsCurrentLineBG[y] + (float)(ec.getNumBitsWritten() - SizeBitstreamBeforeCoding );
					}
					numbitsCurrentLine[y] = numbitsCurrentLine[y] + (float)(ec.getNumBitsWritten() - SizeBitstreamBeforeCoding );
				}
				
		}}
	}
	
	
	private void BIAC(int M, int z, int y, int predictedLines[][][], int maskSamples[][][], float numbitsCurrentLine[], float numbitsCurrentLineROI[],  float numbitsCurrentLineBG[], boolean EBits){
		for (int bit = 15; bit >= 0; bit--){
		for (int x = 0; x < xSize; x ++) {
				if (x == 0 && y == 0 && z == 0 && bit == 15) {
					ec.init(z);
				}
				
				if(y == 0 || (y > 0 && maskSamples[0][y][x] == 255 ) || EBits == true) {
					boolean realBit = false;
					int context = 0;
					realBit = (predictedLines[z][1][x] & BIT_MASKS2[bit]) != 0;
					context = cm.getContext(predictedLines, z, 1, x, bit);//get context
					int prob = cp.getProbability(context);//get probability for the computed context
					cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
					long SizeBitstreamBeforeCoding = ec.getNumBitsWritten();
					ec.encodeBitProb(realBit, prob);//encode the bit using the specific probability
					//get the amount of bits used for the ROI and the BG
					if(maskSamples[0][y][x] == 255) {
						numbitsCurrentLineROI[y] = numbitsCurrentLineROI[y] + (float)(ec.getNumBitsWritten() - SizeBitstreamBeforeCoding );
					}else{
						numbitsCurrentLineBG[y] = numbitsCurrentLineBG[y] + (float)(ec.getNumBitsWritten() - SizeBitstreamBeforeCoding );
					}
					numbitsCurrentLine[y] = numbitsCurrentLine[y] + (float)(ec.getNumBitsWritten() - SizeBitstreamBeforeCoding );
				}
				
		}}
	}


	/**
	 * Encodes an image in BI order
	 *
	 * @param verbose indicates whether to display information
	 * @throws IOException if can not write information to the file
	 */
	private void codeBIACRateControlValsesia(boolean verbose) throws IOException {
		FileWriter file = new FileWriter("qsteps.txt");
		PrintWriter pw = new PrintWriter(file);
		T = targetRate;
		float inputT = targetRate;
		ySize = geo[CONS.HEIGHT];
		xSize = geo[CONS.WIDTH];
		zSize = geo[CONS.BANDS];
		this.mu = new float [ySize+1];
		this.C = new float [ySize+1];
		this.W = new float [ySize+1];
		int M = parameters.subframeInterleavingDepth;

		

		int lines[][][] = new int[zSize][2][];
		int predictedLines[][][] = new int[zSize][2][xSize];
		int linesToEncode[][][] = new int[zSize][2][xSize];

		float deviationRate[] = new float[ySize];
		float numbitsBefore[] = new float[ySize];
		float numbitsCurrentLine[] = new float[ySize];
		
		List<Float> currentMedian = new ArrayList<Float>();
		List<Float> medianLine = new ArrayList<Float>();
		List<Float> medianBands = new ArrayList<Float>();
		List<Float> outputRate = new ArrayList<Float>();
		List<Float> deviation = new ArrayList<Float>();
		
		int L = 17;
		int Qstep = 1;
		float [] updateQstepValues = new float[2];
		updateQstepValues[0] = Qstep;
		updateQstepValues[1] = T;

		if(verbose || debugMode) {
			System.out.println("Coding BI");
		}
		
		try {
			RawImage image = new RawImage(inputFile, geo, originalPixelOrder, RawImage.READ);
			RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.READ, true);
			for (int y = 0; y < geo[CONS.HEIGHT]; y++) {
				
				if(y != 0) {
					for(int i=0;i<geo[CONS.BANDS];i++) {
						lines[i][0] = linesToEncode[i][1];
						linesToEncode[i][0] = linesToEncode[i][1];
					}
				}
				for(int i=0;i<geo[CONS.BANDS];i++) {
					linesToEncode[i][1] = lines[i][1] = it.next();
					
				}
				
				
				if (verbose && geo[CONS.HEIGHT] % 10 == 0) {
					System.out.print("\rCoding rows: " + y + " to " + Math.min(y+10, geo[CONS.HEIGHT]));
				}
				
				uq.setQuantizationStep(1);
				
				for (int z = 0; z < geo[CONS.BANDS]; z++) {
					for (int x = 0; x < geo[CONS.WIDTH]; x++) {
						
						int value = predictorRC.compress(lines, z, y, x, z, 1, uq);
						currentMedian.add((float)value);
					
						if(x % L == L - 1){
							
							//removes duplicate elements and sorts a list
							/*Set<Float> hs = new HashSet<>();
							hs.addAll(currentMedian);
							currentMedian.clear();
							currentMedian.addAll(hs);
							*/
							Collections.sort(currentMedian);
							//puts the median of a current L segment into a list that contains all the medians of the line
							medianLine.add(getMedianFromaList(currentMedian));
							//clears the current median segment	
							currentMedian.clear();
						}
					}
					
					//removes duplicate elements and sorts a list
					/*Set<Float> hs = new HashSet<>();
					hs.addAll(medianLine);
					medianLine.clear();
					medianLine.addAll(hs);
					*/
					Collections.sort(medianLine);
					//puts the median of a current line into a list that contains the medians for all bands
					medianBands.add(getMedianFromaList(medianLine));
					//clears the current median line
					medianLine.clear();
				}
					
					
					if(y == 0)	Qstep = 1;
					//Qstep = 1;
					uq.setQuantizationStep(Qstep);
					pw.println(String.valueOf(Qstep));
				
					//ec.resetNumBitsWritten();
					//predictor.resetMSE();
					for (int z = 0; z < geo[CONS.BANDS]; z++) {
						predictor.resetMSE();
						for (int x = 0; x < geo[CONS.WIDTH]; x++) {
							predictedLines[z][0][x] = predictedLines[z][1][x];
							int value = predictor.compress(linesToEncode, z, y, x, z, 1, uq);
							predictedLines[z][1][x] = value;
						}
						BIAC(M, z, y, predictedLines, numbitsCurrentLine);
						
						//System.out.println(z+":"+y+":"+predictor.getMSE(geo[CONS.WIDTH])+":"+((double)(ec.getNumBitsWritten()-previousbits)/(double)(geo[CONS.WIDTH])));
						
					}
					
				this.targetRate = BIupdateTargetRateValsesia(y, inputT, outputRate, numbitsBefore, numbitsCurrentLine, deviationRate);
				updateQstepValues = updateQstep(medianBands, outputRate, this.targetRate, 0);
				Qstep = (int)updateQstepValues[0];
				medianBands.clear();
				System.out.println("y: "+(y+1)+" Qstep: "+Qstep+" targetRate: "+targetRate);
				
				
			}
			//System.out.println("predictor.getPAE(): "+predictor.getPAE());
			//System.out.println("predictor.getMSE(): "+predictor.getMSE(geo[CONS.BANDS]*geo[CONS.WIDTH]*geo[CONS.HEIGHT]));
			
			
			pw.flush();
			pw.close();
			ec.terminate();
			fileStream.write(ec.getByteStream().getByteStream(),0,(int) ec.getByteStream().getLength());
			image.close(it);
			if (verbose || debugMode) {
				System.out.print("\rCoding image finished");
			}
		}catch(UnsupportedOperationException e) {
			e.printStackTrace(System.err);
			
		}catch(IndexOutOfBoundsException e) {
			e.printStackTrace(System.err);
			
		}catch(ClassCastException e) {
			e.printStackTrace(System.err);
			
		}
	}
	
	private void BIAC(int M, int z, int y, int predictedLines[][][], float numbitsCurrentLine[]){
		long SizeBitstreamBeforeCoding = ec.getNumBitsWritten();
		for (int bit = 15; bit >= 0; bit--){
		for (int x = 0; x < xSize; x ++) {
				if (x == 0 && y == 0 && z == 0 && bit == 15) {
					ec.init(z);
				}
				boolean realBit = false;
				int context = 0;
				realBit = (predictedLines[z][1][x] & BIT_MASKS2[bit]) != 0;
				//context = cm.getContext(predictedLines[z], 1, x, bit);//get context
				context = cm.getContext(predictedLines, z, 1, x, bit);//get context
				int prob = cp.getProbability(context);//get probability for the computed context
				cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
				ec.encodeBitProb(realBit, prob);//encode the bit using the specific probability
		}}
		numbitsCurrentLine[y] = numbitsCurrentLine[y] + (float)(ec.getNumBitsWritten() - SizeBitstreamBeforeCoding );
	}
	
	private float BSQupdateTargetRateValsesia(int z, List<Float> inputRate, List<Float> outputRate, float []updateQstepValues, List<Float> medianBands){

		float newTargetRate = 0;
		float w = 0;
		float actualOutputRate = 0;
		float theta = 5;
		float numbitsWrittenCurrentBand = 0;
		float currentDeviationRate = 0;
		
		float correction = 0;
		float etanext = 0;
		//float J = 0;
		
		if(parameters.entropyCoderType == CONS.ARITHMETIC_ENCODER_FLW){
			numbitsWritten = ec.getNumBitsWritten();
		}else{
			numbitsWritten = file.length()*8;
		}
		
		//System.out.println("ec.getNumBitsWritten(): "+ec.getNumBitsWritten()+" file.length():"+(file.length()*8));
		theta = geo[CONS.BANDS] - z;	
		//Measure the actual outputRate
		numbitsWrittenCurrentBand = numbitsWritten - numbitsWrittenBefore; 
		if(z == 0) numbitsWritten = 0;
		numbitsWrittenBefore = numbitsWritten;
		
		actualOutputRate = numbitsWritten/(z*geo[CONS.HEIGHT]*geo[CONS.WIDTH]);
		System.out.println("actualOutputRate: "+actualOutputRate);
		numbitsWrittenCurrentBand = numbitsWrittenCurrentBand/(geo[CONS.HEIGHT]*geo[CONS.WIDTH]);
		
		currentDeviationRate = deviationRate;
		correction = currentDeviationRate / theta;
		deviationRate = currentDeviationRate + (T - numbitsWrittenCurrentBand);
		if(z == 0){
			w = 1;
			
			//if(w*w > 2) w = (float) Math.sqrt(2);
			//eta = numbitsWrittenCurrentLine;
			eta = (numbitsWrittenCurrentBand / w) - (currentDeviationRate / (theta * w));
			////////////////////////etanext = numbitsWrittenCurrentLine;
			etanext =  eta +  w * (T - numbitsWrittenCurrentBand + correction);////////////////
			newTargetRate =  etanext + (deviationRate / theta) * (1 / w);
			//J = Math.abs(T - numbitsWrittenCurrentLine) + Math.abs(T - numbitsWrittenCurrentLine + (2 * currentDeviationRate) / theta);  
			
		}else{
			//J = Math.abs(T - numbitsWrittenCurrentLine) + Math.abs(T - numbitsWrittenCurrentLine + (2 * currentDeviationRate) / theta);
			w = numbitsWrittenCurrentBand / outputRate.get(z-1);
			//if(w*w > 2) w = (float) Math.sqrt(2);
			//eta = numbitsWrittenCurrentLine;
			eta = (numbitsWrittenCurrentBand / w) - (currentDeviationRate / (theta * w));
			etanext =  eta +  w * (T - numbitsWrittenCurrentBand + correction);
			newTargetRate =  etanext + (deviationRate / theta) * (1 / w);
			
		}	
		
		//System.out.println("newTargetRate: "+newTargetRate);
		if(z == 0) newTargetRate =  T * w; 
		if(z > 0 && newTargetRate < 0){
			newTargetRate = outputRate.get(z-1);
		}
		//System.out.println("newTargetRate: "+newTargetRate);
		
		//if(newTargetRate < numbitsWrittenCurrentLine){
		//	System.out.println("newTargetRate1: "+newTargetRate);
		//	if(newTargetRate < 0){
		//		if(y > 0)newTargetRate =  outputRate.get(y-1);
		//		else newTargetRate = etanext + (deviationRate / theta) * (1 / w);
		//		System.out.println("newTargetRate2: "+newTargetRate);
		//	}
		//	
		//	if(y == 0) newTargetRate = (T + numbitsWrittenCurrentLine*2) / 3;
		//	else 	   newTargetRate = (outputRate.get(y-1) + numbitsWrittenCurrentLine*2) / 3;
		//}
		//System.out.println("newTargetRate3: "+newTargetRate);
		
		
		
		
		eta = etanext;
		currentDeviationRate = deviationRate;
		outputRate.add(newTargetRate);
		inputRate.add(numbitsWrittenCurrentBand);
		return(newTargetRate);
	}
	
	
	
	private float BIupdateTargetRateValsesia(int y, float inputT, List<Float> outputRate, float[] numbitsBefore, float[] numbitsCurrentLine, float[] deviationRate, float[] numSamples, int yFirst){
		float newTargetRate = 0;
		float w = 0;
		float targetbitsline = 0;
		float theta = 5;
		float munext = 0;
		
		float previousNewRate = 0;
		float mu = 0;
		float a = 0;
		
		float deviationlinebits = 0;
		
		
		float linebps =  numbitsCurrentLine[y] / (numSamples[y]);
		if(linebps == 0) {
			linebps = 0.001f;
		}
		
		
		if(y == yFirst){
			targetbitsline = inputT * (numSamples[y]);
			deviationlinebits = numbitsCurrentLine[y]-targetbitsline;
			previousNewRate = inputT;
			w = linebps / inputT;
			deviationRate[y] = deviationlinebits / (numSamples[y]*(y+1));
			
			mu = (linebps / w) - (deviationRate[y] /(w*theta));
			a = (inputT - linebps + (deviationRate[y] / theta));
			munext = mu + w * a;
			newTargetRate = (float)(munext + (deviationRate[y] / theta) * (1 / w));
				
		}else{
			
			previousNewRate = outputRate.get(y-yFirst-1);
			targetbitsline = previousNewRate * (numSamples[y]);
			deviationlinebits = numbitsCurrentLine[y]-targetbitsline;
			w = linebps / previousNewRate;
			if(w*w > 2) w = 1.4142f;
			
			
			if(inputT >= 1.25) {
				deviationRate[y] = deviationRate[y-1] + (inputT - linebps);
			}else {
				deviationRate[y] = (deviationRate[y-1] + (inputT - linebps))/(y+1);
			}
			
			mu = (linebps / w) - (deviationRate[y-1] /(w*theta));
			a = (inputT - linebps + (deviationRate[y-1] / theta));
			munext = mu + w * a;
			newTargetRate = (float)(munext + (deviationRate[y] / theta) * (1 / w));
		}
		
		//System.out.println("y:"+y+" currentbps:"+currentbps+" linebps:"+linebps+" previousNewRate:"+previousNewRate+" T:"+T+" mu:"+mu+" a:"+a+" deviationRate: "+deviationRate+" numbitsWritten:"+numbitsWritten+" w:"+w+" newTargetRate:"+newTargetRate);
		
		if(newTargetRate < 0){
			
			if(y == yFirst) newTargetRate = inputT * w;
			else newTargetRate = previousNewRate * (1 / w);
		}else{
			
		}
		if(newTargetRate > inputT*10) newTargetRate = inputT*10;
		//System.out.println(" --> newTargetRate: "+newTargetRate);
		
		mu = munext;
		outputRate.add(y-yFirst, newTargetRate);
		
		
		return(newTargetRate);
	}
	
	
	private float BIupdateTargetRateValsesia(int y, float inputT, List<Float> outputRate, float[] numbitsBefore, float[] numbitsCurrentLine, float[] deviationRate){
		float newTargetRate = 0;
		float w = 0;
		float targetbitsline = 0;
		float theta = 5;
		float munext = 0;
		
		float previousNewRate = 0;
		float mu = 0;
		float a = 0;
		
		float deviationlinebits = 0;
		
		
		float linebps =  numbitsCurrentLine[y] / (zSize*xSize);
		if(linebps == 0) linebps = 0.0001f;
		
		
		if(y == 0){
			targetbitsline = inputT * (zSize*xSize);
			deviationlinebits = numbitsCurrentLine[y]-targetbitsline;
			previousNewRate = inputT;
			w = linebps / inputT;
			deviationRate[y] = deviationlinebits / (zSize*xSize*(y+1));
			
			mu = (linebps / w) - (deviationRate[y] /(w*theta));
			a = (inputT - linebps + (deviationRate[y] / theta));
			munext = mu + w * a;
			newTargetRate = (float)(munext + (deviationRate[y] / theta) * (1 / w));
			
			
		}else{
			
			previousNewRate = outputRate.get(y-1);
			targetbitsline = previousNewRate * (zSize*xSize);
			deviationlinebits = numbitsCurrentLine[y]-targetbitsline;
			w = linebps / previousNewRate;
			if(w*w > 2) w = 1.4142f;
			
			
			if(inputT >= 1.25) {
				deviationRate[y] = deviationRate[y-1] + (inputT - linebps);
			}else {
				deviationRate[y] = (deviationRate[y-1] + (inputT - linebps))/(y+1);
			}
			
			mu = (linebps / w) - (deviationRate[y-1] /(w*theta));
			a = (inputT - linebps + (deviationRate[y-1] / theta));
			munext = mu + w * a;
			newTargetRate = (float)(munext + (deviationRate[y] / theta) * (1 / w));
		}
		
		//System.out.println("y:"+y+" currentbps:"+currentbps+" linebps:"+linebps+" previousNewRate:"+previousNewRate+" T:"+T+" mu:"+mu+" a:"+a+" deviationRate: "+deviationRate+" numbitsWritten:"+numbitsWritten+" w:"+w+" newTargetRate:"+newTargetRate);
		
		if(newTargetRate < 0){
			if(y == 0) newTargetRate = inputT * w;
			else newTargetRate = inputT * w;
		}else{
			
		}
		if(newTargetRate > inputT*10) newTargetRate = inputT*10;
		//System.out.println(" --> newTargetRate: "+newTargetRate);
		
		mu = munext;
		outputRate.add(newTargetRate);
		
		return(newTargetRate);
	}
	
	


	private float[] updateQstep(List<Float> medianBands, List<Float> outputRate, float targetRate, int BSQ){
		
		
		float [] returnedValues = new float[2];
		
		
		int currentQstep =  uq.getQuantizationStep();
		int nextQstep = currentQstep;
		double R = 0;
		double ROld = 0;
		for(int index = 0; index < medianBands.size(); index++){
			//System.out.println(medianBands.get(index)+" "+GetValesesiaRate((double)medianBands.get(index), nextQstep));
			if(medianBands.get(index) > 0 ) {
				R = R + GetValesesiaRate((double)medianBands.get(index), nextQstep);				
			}
		}
		if(BSQ == 1) R = R / ySize;
		else R = R / zSize;
		if(Double.isNaN(R)) {
			R = this.T;
		}
		//System.out.print("nextQstep: "+nextQstep+" R: "+R+" ROld: "+ROld+" targetRate: "+targetRate+" "+medianBands.size());
		//for(int i = 0; i < medianBands.size(); i++) System.out.print(" "+medianBands.get(i));
		//System.out.println();
		if(R >= targetRate){
			while( (R >= targetRate) && (nextQstep < MAXQSTEP)){
				ROld = R;
				nextQstep = nextQstep + 2;
				R = 0;
				for(int index = 0; index < medianBands.size(); index++){
					 R = R + GetValesesiaRate((double)medianBands.get(index), nextQstep);
				}
				if(BSQ == 1) R = R / ySize;
				else R = R / zSize;
				//System.out.println("nextQstep: "+nextQstep+" R: "+R+" ROld: "+ROld+" targetRate: "+targetRate);
			}
			if(Math.abs(R - targetRate) > Math.abs(ROld - targetRate)){
				nextQstep = nextQstep - 2;
			}
		}else{
			while( (R <= targetRate) && (nextQstep > 1)){
				ROld = R;
				nextQstep = nextQstep - 2;
				R = 0;
				for(int index = 0; index < medianBands.size(); index++){
					 R = R + GetValesesiaRate((double)medianBands.get(index), nextQstep);
				}
				if(BSQ == 1) R = R / ySize;
				else R = R / zSize;
				//System.out.println("nextQstep: "+nextQstep+" R: "+R+" ROld: "+ROld+" targetRate: "+targetRate);
			}
			if(Math.abs(R - targetRate) > Math.abs(ROld - targetRate)){
				nextQstep = nextQstep + 2;
			}
			
		}
		//if(nextQstep == 0) nextQstep = 4;
		//System.out.println("nextQstep: "+nextQstep+" \n");
		returnedValues[0] = (float)nextQstep;
		returnedValues[1] = (float)R;
		return returnedValues;
	}

	/**
	 * returns the median value from an Integer List of values
	 * @param list
	 * @return the median
	 */
	private float getMedianFromaList(List<Float> list){
		float median = 0;
		if(list.size() == 0) {
			return(1);
		}
		if(list.size() == 1) {
			return(1);
		}
		if(list.size() % 2 == 1){
			//odd size
			//median = (list.get((list.size())/2));
			median = (list.get((list.size()+1)/2));
		}else{
			//even size
			median = (list.get((int) (list.size()/2)-1) + list.get((list.size()/2))) / 2;
			//median = (list.get((list.size()/2)) + list.get((list.size()/2)+1)) / 2;
		}
		//if(median == 0) median = 1;
		return(median);
	}
	
	/**
	 *  Computes the rate according to the Valsessia and Magli GRSL paper
	 * @param m is the median
	 * @param Q is the qstep 
	 * @return the rate
	 */
	private double GetValesesiaRate(double m, double Q){
		double rate = 0;
		double a = Math.pow(Math.E,-((Q/(2*m))));
		double aa = 1 - a;
		double b = Math.pow(Math.E,-(Q/m));
		double bb = 1 - b;
		double cc = Math.log(aa)/Math.log(2);
		double aacc = -aa * cc;
		double d = -(a/Math.log(2));
		double e = (Math.log(bb/2)) + (Q/(2*m)) - (Q / (m*bb));
		rate = aacc + d * e;
		//return (rate / (geo[CONS.WIDTH] * geo[CONS.HEIGHT])) * 1000 / geo[CONS.BANDS]; 
		return rate;
		//500;
		
		
	}
	
	
	/**
	 * Encodes an image in BI order
	 *
	 * @param verbose indicates whether to display information
	 * @throws IOException if can not write information to the file
	 */
	private void codeBIAC(boolean verbose) throws IOException {
		
		int M = parameters.subframeInterleavingDepth;
				int lines[][][] = new int[geo[CONS.BANDS]][2][];
		int predictedLines[][][] = new int[geo[CONS.BANDS]][2][geo[CONS.WIDTH]];

		if(verbose || debugMode) {
			System.out.println("Coding BI");
		}
		
		try {
			RawImage image = new RawImage(inputFile, geo, originalPixelOrder, RawImage.READ);
			RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.READ, true);
			for (int y = 0; y < geo[CONS.HEIGHT]; y++) {
				prepareLines(y, lines, it);
				if (verbose && geo[CONS.HEIGHT] % 10 == 0) {
					System.out.print("\rCoding rows: " + y + " to " + Math.min(y+10, geo[CONS.HEIGHT]));
				}
				
				for (int z = 0; z < geo[CONS.BANDS]; z++) {		
				for (int x = 0; x < geo[CONS.WIDTH]; x++) {
					predictedLines[z][0][x] = predictedLines[z][1][x];
					predictedLines[z][1][x] = predictor.compress(lines, z, y, x, z, 1, uq);
				}
					BIAC(M, z, y, predictedLines);
				}
				
				
				//System.out.println(y+":"+predictor.getMSE(geo[CONS.WIDTH]*geo[CONS.BANDS])+":"+((float)ec.getNumBitsWritten()/(geo[CONS.WIDTH]*geo[CONS.BANDS])));
				predictor.resetMSE();
				ec.resetNumBitsWritten();
				
			}
			ec.terminate();
			fileStream.write(ec.getByteStream().getByteStream(),0,(int) ec.getByteStream().getLength());
			image.close(it);
			if (verbose || debugMode) {
				System.out.print("\rCoding image finished");
			}
		}catch(UnsupportedOperationException e) {
			e.printStackTrace(System.err);
			
		}catch(IndexOutOfBoundsException e) {
			e.printStackTrace(System.err);
			
		}catch(ClassCastException e) {
			e.printStackTrace(System.err);
			
		}
	}
	
	

	private void BIAC(int M, int z, int y, int predictedLines[][][]){
		for (int bit = 15; bit >= 0; bit--){
		for (int x = 0; x < xSize; x ++) {
				if (x == 0 && y == 0 && z == 0 && bit == 15) {
					ec.init(z);
				}
				boolean realBit = false;
				int context = 0;
				realBit = (predictedLines[z][1][x] & BIT_MASKS2[bit]) != 0;
				//context = cm.getContext(predictedLines[z], 1, x, bit);//get context
				context = cm.getContext(predictedLines, z, 1, x, bit);//get context
				int prob = cp.getProbability(context);//get probability for the computed context
				cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
				ec.encodeBitProb(realBit, prob);//encode the bit using the specific probability
		}}
	}
	
	private void BIAC3(int z, int M, int i, int y, int predictedLines[][][]){
		for (int bit = 15; bit >= 0; bit--){
		for (int x = 0; x < xSize; x ++) {
				if (x == 0 && y == 0 && z == 0 && bit == 15) {
					ec.init(z);
				}
				boolean realBit = false;
				int context = 0;
				realBit = (predictedLines[z][1][x] & BIT_MASKS2[bit]) != 0;
				context = cm.getContext(predictedLines, z, 1, x, bit);//get context
				int prob = cp.getProbability(context);//get probability for the computed context
				cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
				ec.encodeBitProb(realBit, prob);//encode the bit using the specific probability
		}}	
	}
	
	private double BIBinaryContextualEntropy(int y, int predictedLines[][][]){
		double binaryEntropy = 0;
		for (int bit = 15; bit >= 0; bit--){
			for (int z = 0; z < geo[CONS.BANDS]; z++) {
			for (int x = 0; x < xSize; x ++) {
			boolean realBit = false;
			int context = 0;
			realBit = (predictedLines[z][1][x] & BIT_MASKS2[bit]) != 0;
			context = cm.getContext(predictedLines[z], 1, x, bit);//get context
			int prob = cp.getProbability(context);//get probability for the computed context
			cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
			double probq = prob / (double) (1 << numBitsPrecision);
			
			if(realBit){
				probq = 1 - probq;
			}
			
			assert(probq != 0);
			assert(probq != 1);
			
			double symbolCost = - (Math.log10(probq) / Math.log10(2));
			assert(symbolCost > 0);
			binaryEntropy += symbolCost; //https://en.wikipedia.org/wiki/Arithmetic_coding
			
		}}}	
		
		return(binaryEntropy);
	}
	
	private double BIBinaryContextualEntropy2(int y, int predictedLines[][][]){
		double binaryEntropy = 0;
		for (int bit = 15; bit >= 0; bit--){
			for (int z = 0; z < geo[CONS.BANDS]; z++) {
			for (int x = 0; x < geo[CONS.WIDTH]; x ++) {
			boolean realBit = false;
			int context = 0;
			realBit = (predictedLines[z][1][x] & BIT_MASKS2[bit]) != 0;
			//context = cmRC.getContext(predictedLines[z], 1, x, bit);//get context
			context = cmRC.getContext(predictedLines, z, 1, x, bit);//get context
			int prob = cpRC.getProbability(context);//get probability for the computed context
			cpRC.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
			double probq = prob / (double) (1 << numBitsPrecision);
			if(realBit){
				probq = 1 - probq;
			}
			
			assert(probq != 0);
			assert(probq != 1);
			
			double symbolCost = - (Math.log10(probq) / Math.log10(2));
			assert(symbolCost > 0);
			binaryEntropy += symbolCost; //https://en.wikipedia.org/wiki/Arithmetic_coding
			
		}}}	
		
		return(binaryEntropy);
	}
	
	
	private double BIBinaryContextualEntropy3(int z, int y, int predictedLines[][][]){
		double binaryEntropy = 0;
		for (int bit = 15; bit >= 0; bit--){
			for (int x = 0; x < geo[CONS.WIDTH]; x ++) {
			boolean realBit = false;
			int context = 0;
			realBit = (predictedLines[z][1][x] & BIT_MASKS2[bit]) != 0;
			//context = cmRC.getContext(predictedLines[z], 1, x, bit);//get context
			context = cm.getContext(predictedLines, z, 1, x, bit);//get context
			int prob = cpRC.getProbability(context);//get probability for the computed context
			cpRC.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
			double probq = prob / (double) (1 << numBitsPrecision);
			if(realBit){
				probq = 1 - probq;
			}
			
			assert(probq != 0);
			assert(probq != 1);
			
			double symbolCost = - (Math.log10(probq) / Math.log10(2));
			assert(symbolCost > 0);
			binaryEntropy += symbolCost; //https://en.wikipedia.org/wiki/Arithmetic_coding
			
		}}	
		
		return(binaryEntropy);
	}
	
	
	private void getSerializedPredictorCPLineObject() throws IOException, ClassNotFoundException{

		//predictor
		FileInputStream filePredictorSerIn = null;//new FileInputStream("/tmp/predictor.ser");
		ObjectInputStream predictorSerIn = null;//new ObjectInputStream(filePredictorSerIn);
		filePredictorSerIn = new FileInputStream("/tmp/"+System.getProperty("user.dir").hashCode()+"predictorline.ser");
		//filePredictorSerIn = new FileInputStream("/tmp/rdisk/"+System.getProperty("user.dir").hashCode()+"predictorline.ser");
		predictorSerIn = new ObjectInputStream(filePredictorSerIn);
		predictorRC = (Predictor) predictorSerIn.readObject();
	    predictorSerIn.close();
	    filePredictorSerIn.close();
	    
	    //cp
	    FileInputStream fileCPSerIn = null;//new FileInputStream("/tmp/predictor.ser");
	    ObjectInputStream cpSerIn = null;//new ObjectInputStream(filePredictorSerIn);
	    fileCPSerIn = new FileInputStream("/tmp/"+System.getProperty("user.dir").hashCode()+"cpline.ser");
	    //fileCPSerIn = new FileInputStream("/tmp/rdisk/"+System.getProperty("user.dir").hashCode()+"cpline.ser");
	    cpSerIn = new ObjectInputStream(fileCPSerIn);
	    cpRC = (ContextProbability) cpSerIn.readObject();
	    cpSerIn.close();
	    fileCPSerIn.close();
	    
	 
	}
	
	

	private void putSerializedPredictorCPLineObject() throws IOException{
		//predictor
		FileOutputStream filePredictorSerOut = null;//new FileOutputStream("/tmp/predictor.ser");
		ObjectOutputStream predictorSerOut = null;//new ObjectOutputStream(filePredictorSerOut);
		filePredictorSerOut = new FileOutputStream("/tmp/"+System.getProperty("user.dir").hashCode()+"predictorline.ser");
		//filePredictorSerOut = new FileOutputStream("/tmp/rdisk/"+System.getProperty("user.dir").hashCode()+"predictorline.ser");
		predictorSerOut = new ObjectOutputStream(filePredictorSerOut);
		predictorSerOut.writeObject(predictorRC);
		predictorSerOut.close();
		filePredictorSerOut.close();
		
		//cp
		FileOutputStream fileCPSerOut = null;//new FileOutputStream("/tmp/predictor.ser");
		ObjectOutputStream cpSerOut = null;//new ObjectOutputStream(filePredictorSerOut);
		fileCPSerOut = new FileOutputStream("/tmp/"+System.getProperty("user.dir").hashCode()+"cpline.ser");
		//fileCPSerOut = new FileOutputStream("/tmp/rdisk/"+System.getProperty("user.dir").hashCode()+"cpline.ser");
		cpSerOut = new ObjectOutputStream(fileCPSerOut);
		cpSerOut.writeObject(cpRC);
		cpSerOut.close();
		fileCPSerOut.close();
		
		

	}
	
	private void getSerializedPredictorCPBufferObject() throws IOException, ClassNotFoundException{
		
		//predictor
		FileInputStream filePredictorSerIn = null;//new FileInputStream("/tmp/predictor.ser");
		ObjectInputStream predictorSerIn = null;//new ObjectInputStream(filePredictorSerIn);
		filePredictorSerIn = new FileInputStream("/tmp/"+System.getProperty("user.dir").hashCode()+"predictorbuffer.ser");
		//filePredictorSerIn = new FileInputStream("/tmp/rdisk/"+System.getProperty("user.dir").hashCode()+"predictorbuffer.ser");
		predictorSerIn = new ObjectInputStream(filePredictorSerIn);
		predictorRC = (Predictor) predictorSerIn.readObject();
	    predictorSerIn.close();
	    filePredictorSerIn.close();
	    
	    //cp
	    FileInputStream fileCPSerIn = null;//new FileInputStream("/tmp/predictor.ser");
	    ObjectInputStream cpSerIn = null;//new ObjectInputStream(filePredictorSerIn);
	    fileCPSerIn = new FileInputStream("/tmp/"+System.getProperty("user.dir").hashCode()+"cpbuffer.ser");
	    //fileCPSerIn = new FileInputStream("/tmp/rdisk/"+System.getProperty("user.dir").hashCode()+"cpbuffer.ser");
	    cpSerIn = new ObjectInputStream(fileCPSerIn);
	    cpRC = (ContextProbability) cpSerIn.readObject();
	    cpSerIn.close();
	    fileCPSerIn.close();
	    
	 
	}

	private void putSerializedPredictorCPBufferObject() throws IOException{
		//predictor
		FileOutputStream filePredictorSerOut = null;//new FileOutputStream("/tmp/predictor.ser");
		ObjectOutputStream predictorSerOut = null;//new ObjectOutputStream(filePredictorSerOut);
		filePredictorSerOut = new FileOutputStream("/tmp/"+System.getProperty("user.dir").hashCode()+"predictorbuffer.ser");
		//filePredictorSerOut = new FileOutputStream("/tmp/rdisk/"+System.getProperty("user.dir").hashCode()+"predictorbuffer.ser");
		predictorSerOut = new ObjectOutputStream(filePredictorSerOut);
		predictorSerOut.writeObject(predictorRC);
		predictorSerOut.close();
		filePredictorSerOut.close();
		
		//cp
		FileOutputStream fileCPSerOut = null;//new FileOutputStream("/tmp/predictor.ser");
		ObjectOutputStream cpSerOut = null;//new ObjectOutputStream(filePredictorSerOut);
		fileCPSerOut = new FileOutputStream("/tmp/"+System.getProperty("user.dir").hashCode()+"cpbuffer.ser");
		//fileCPSerOut = new FileOutputStream("/tmp/rdisk/"+System.getProperty("user.dir").hashCode()+"cpbuffer.ser");
		cpSerOut = new ObjectOutputStream(fileCPSerOut);
		cpSerOut.writeObject(cpRC);
		cpSerOut.close();
		fileCPSerOut.close();
		

	}
	
	
	
	/**
	 * 
	 * @param z the band to be loaded
	 * @param y the line to be loaded
	 * @param lines is where data is stored
	 * @param buffer is the original data
	 */
	private int[][][] loadOriginalLineNotMoving(int z, int y, int[][][] lines, int[][][]buffer){
		
		for(int x=0;x<geo[CONS.WIDTH];x++) {
			lines[z][1][x] = buffer[z][y][x];
		}
		return(lines);
	}
	
	/**
	 * 
	 * @param y the line to be loaded
	 * @param lines is where data is stored
	 * @param buffer is the original data
	 */
	private int[][][] loadOriginalSpectralLineNotMoving(int y, int[][][] lines, int[][][]buffer){
		
		for(int i=0;i<geo[CONS.BANDS];i++) {
		for(int x=0;x<geo[CONS.WIDTH];x++) {
			lines[i][1][x] = buffer[i][y][x];
		}}
		return(lines);
	}
	
	/**
	 * 
	 * @param z the band to be loaded
	 * @param y the line to be loaded
	 * @param lines is where data is stored
	 * @param buffer is the original data
	 */
	private int[][][] loadOriginalLine(int z, int y, int[][][] lines, int[][][]buffer){
		if(y != 0) {
			for(int x=0;x<geo[CONS.WIDTH];x++) {
				lines[z][0][x] = lines[z][1][x];
			}
		}
		for(int x=0;x<geo[CONS.WIDTH];x++) {
			lines[z][1][x] = buffer[z][y][x];
		}
		return(lines);
	}
	
	/**
	 * 
	 * @param y the line to be loaded
	 * @param lines is where data is stored
	 * @param buffer is the original data
	 */
	private int[][][] loadOriginalSpectralLine(int y, int[][][] lines, int[][][]buffer){
		if(y != 0) {
			for(int i=0;i<geo[CONS.BANDS];i++) {
			for(int x=0;x<geo[CONS.WIDTH];x++) {
				lines[i][0][x] = lines[i][1][x];
			}}
		}
		for(int i=0;i<geo[CONS.BANDS];i++) {
		for(int x=0;x<geo[CONS.WIDTH];x++) {
			lines[i][1][x] = buffer[i][y][x];
		}}
		return(lines);
	}
	
	private void resetPredictorAndACFLW() throws ParameterException{
		numBitsPrecision = 15;
		coderWordLength = 48;
		predictor = new Predictor(this.parameters);
		predictor.resetMSE();
		cm = new ContextModelling(contextModel);
		numOfContexts = cm.getNumberOfContexts(MAXBITS);
		cp = new ContextProbability(probabilityModel, numOfContexts, numBitsPrecision, quantizerProbabilityLUT, parameters.entropyCoderType, WINDOW_PROB, UPDATE_PROB0);
		cp.reset();
		ec = new ArithmeticCoderFLW(coderWordLength, numBitsPrecision, numOfContexts);
		ec.reset();
	}
	
					

	
	
	

	/**
	 * Estimate the MSE according Valssessia 2014
	 * @param variance
	 * @param qstep
	 * @return
	 */
	private double estimateMSE(double variance, int qstep) {
		double MSE = 0;
		double delta = Math.sqrt(2/variance);
		double a = Math.pow(Math.E,(-delta*((double)qstep/(double)2)));
		double b =  Math.pow(Math.E,(delta*(double)qstep));
		double c = (2 - 0.25 * a * (delta*delta*(double)qstep*(double)qstep+4*delta*(double)qstep+8d)) / (delta*delta);
		double d = (-delta*(double)qstep*(delta*(double)qstep+4d)+b*(delta*(double)qstep*(delta*(double)qstep - 4 ) + 8)-8) / (4*delta*delta);
		double e = Math.pow(Math.E,(-((double)3/(double)2)*delta*(double)qstep)) / (double)(1-Math.pow(Math.E,-delta*(double)qstep));
		MSE = c + d * e;
		return(MSE);
	}
	
	/**
	 * Estimate the rate according Valssessia 2014
	 * @param variance
	 * @param qstep
	 * @return
	 */
	private double estimateRate(double variance, int qstep) {
		
		double rate = 0;
		double delta = Math.sqrt(2/variance);
		double a = Math.pow(Math.E,(-delta*((double)qstep/(double)2)));
		double b = (1 - a);
		double c = Math.log(b) / Math.log(2);
		double d =  (1 - Math.pow(Math.E,(-(double)delta*(double)qstep)));
		double e = a / Math.log(2);
		double f = Math.log((double)d/(double)2);
		double g = (delta*(double)qstep) / (double)2;
		double h = (delta*(double)qstep) / (double)d;
		
		rate = -b * c - e * (f + g - h);
		
		return(rate);
	}
	
	private double computeVariance(int [][][] predictedLines) {
		double variance = 0;
		double mean = 0;
		for (int z = 0; z < geo[CONS.BANDS]; z++) {
		for (int x = 0; x < geo[CONS.WIDTH]; x++) {
			mean = mean + predictedLines[z][1][x];
		}}
		mean = mean / (geo[CONS.BANDS] * geo[CONS.WIDTH]);
		
		for (int z = 0; z < geo[CONS.BANDS]; z++) {
		for (int x = 0; x < geo[CONS.WIDTH]; x++) {
			variance = variance + ((predictedLines[z][1][x] - mean)*(predictedLines[z][1][x] - mean));
		}}
		variance = variance / ((geo[CONS.BANDS] * geo[CONS.WIDTH]) - 1);
		return variance;
	}
	
	
	private double computeVarianceLinebyLine(int z, int [][][] predictedLines) {
		double variance = 0;
		double mean = 0;
		for (int x = 0; x < geo[CONS.WIDTH]; x++) {
			mean = mean + predictedLines[z][1][x];
		}
		mean = mean / geo[CONS.WIDTH];
		
		for (int x = 0; x < geo[CONS.WIDTH]; x++) {
			variance = variance + ((predictedLines[z][1][x] - mean)*(predictedLines[z][1][x] - mean));
		}
		variance = variance / (geo[CONS.WIDTH] - 1);
		return variance;
	}
	
	
	
	
	/**
	 * Tries to read the line y of all bands and reorders the lines for prediction
	 * @param y is the line of the image that will be loaded for all bands
	 * @param bands is the array with the last lines loaded
	 * @param it the BIL iterator over the image
	 */
	private void getLine(int y, int[][][] bands, RawImageIterator<int[]> it) {
		for(int i=0;i<geo[CONS.BANDS];i++) {
			bands[i][0] = it.next();
		}
		
	}
	
	/**
	 * Tries to read the line y of all bands and reorders the lines for prediction
	 * @param y is the line of the image that will be loaded for all bands
	 * @param bands is the array with the last lines loaded
	 * @param it the BIL iterator over the image
	 */
	private void prepareLines(int y, int[][][] bands, RawImageIterator<int[]> it) {
		if(y != 0) {
			for(int i=0;i<geo[CONS.BANDS];i++) {
				bands[i][0] = bands[i][1];
			}
		}
		for(int i=0;i<geo[CONS.BANDS];i++) {
			bands[i][1] = it.next();
			
		}
		
	}
	
	
	
	
	
	
	private int chooseRate(double[] rate, double [] error){
		int selection = N - 1;
		
		for (int i = N - 1 ; i >= 0 ; i--){	
			if (adaptiveTargetRate >= rate[i]){
				selection = i;
			}		
		}
		//System.out.println(adaptiveTargetRate+" "+targetRate+" "+rate[selection]);
		adaptiveTargetRate = adaptiveTargetRate + targetRate - (float)rate[selection];
		//System.out.println(" --> "+adaptiveTargetRate+" "+targetRate+" "+rate[selection]);
		return selection;
	}
	
	private int chooseRateOptim(float [] rate, PrintStream ps, int segmentNumber){
		
		int M = parameters.subframeInterleavingDepth;//parameters.subframeInterleavingDepth
		int selection = N - 1;
		
		//The first segment is coded with small qstep
		if(segmentNumber != 1){
			for (int i = N - 1 ; i >= 0 ; i--){	
				if (targetRate >= rate[i]){
					selection = i;
				}		
			}
		}else{
			for (int i = N - 1 ; i >= 0 ; i--){	
				if (targetRate >= rate[i]){
					selection = i;
				}		
			}
			if(selection > 1){
				selection = 1;
			}
		}
		
		
		adaptiveTargetRate = adaptiveTargetRate + (targetRate - rate[selection]);

		float variacio = targetRate - rate[selection];
		ps.println(variacio);
		
		return selection;
	}

	private int chooseRate2(float [] rate, PrintStream ps, int segmentNumber){
		
		int M = parameters.subframeInterleavingDepth;//parameters.subframeInterleavingDepth
		int selection = N - 1;
		
		for (int i = N - 1 ; i >= 0 ; i--){	
			if (adaptiveTargetRate >= rate[i]){
				selection = i;
			}		
		}
				adaptiveTargetRate = adaptiveTargetRate + (targetRate - rate[selection]);

		float variacio = targetRate - rate[selection];
		ps.println(variacio);
		
		return selection;
	}
	
	/**
	 * Deep copies a 3 dimensional array
	 * @param origen is the 3 dimensional array to be copied
	 * @param destination is the output 3 dimensional array, which is a deep copy of array
	 */
	private int[][][] copyOf3Dim(int[][][] origen, int[][][]destination) {

		for (int z = origen.length -1; z >= 0; z--) { 
        	if (origen[z] != null){
        		destination[z] = new int[origen[z].length][];
        		for (int y = origen[z].length - 1; y >= 0; y--) { 
        			if(origen[z][y]!=null){
        				destination[z][y] = new int[origen[z][y].length];
        				for (int x = origen[z][y].length - 1; x >= 0 ; x--) {
        					destination[z][y][x] = origen[z][y][x];  
        				}
        			}else destination[z][y] = null;
        		}  
        	}else destination[z] = null;
        }
        
        
        return destination;
	}
	/**
	 * Deep copies a band of a 3 dimensional array
	 * @param array is the 3 dimensional array to be copied
	 * @param bands is the output copy of the 3 dimensional array
	 * @param z is the band to be copied
	 */
	private int[][][] copyBand(int[][][] array, int[][][]copy, int z) {

        	if (array[z] != null){
        		copy[z] = new int[array[z].length][];
        		for (int y = array[z].length - 1; y >= 0 ; y--) { 
        			if(array[z][y]!=null){
        				copy[z][y] = new int[array[z][y].length];
        				for (int x = array[z][y].length - 1; x >= 0 ; x--) {
        					copy[z][y][x] = array[z][y][x]; 
        				}
        			}else copy[z][y] = null;
        		}  
        	}else copy[z] = null;
        
        return copy;
	}
	
	/**
	 * Deep copies a band of a 3 dimensional array
	 * @param array is the 3 dimensional array to be copied
	 * @param bands is the output copy of the 3 dimensional array
	 * @param z is the band to be copied
	 */
	private int[][] copy2DBand(int[][] origen, int[][]destination) {

		for (int y = 0; y < origen.length ; y++) { 
		for (int x = 0; x < origen[y].length ; x++) {
			destination[y][x] = origen[y][x]; 
		}}
        
        return destination;
	}
	
	
	
	private int getEfficientQstep(double[] rate, double[] Errors) {
		//Initalization f qstep to 0 in purpose. If something wrong happens we encode the component lossless.
		int selection = 0;
		double minError = Double.MAX_VALUE;
		double minRate = Double.MAX_VALUE;
		boolean found = false;
		double previousRate = Double.MAX_VALUE;
		for(int i = 0; i < N; i++){
			
			if(rate[i] <= targetRate){
				found = true;
				if(minError > Errors[i]){
					selection = i;
					minError =  Errors[i];
				}
				
				
			}
			//System.out.println(i+" "+quantizationStepList.get(i)+" "+found+" "+height+" "+width+" "+rate[i]+" "+targetRate+" "+Errors[i]);
		}
		
		if(found == false){
			//System.out.println("============");
			for(int i = 0; i < N; i++){
				if(rate[i] < minRate && previousRate - rate[i] > 0.1){
					minRate = rate[i];
					selection = i;
				}
				previousRate = rate[i];
				
			}
			//System.out.println("minRate: "+quantizationStepList.get(selection)+" "+selection+" "+found+" "+height+" "+width+" "+minRate);
			for(int i = selection; i < N; i++){
				if(Errors[i] < minError){
					minError = Errors[i];
					selection = i;
				}
			}
			//System.out.println("minError: "+quantizationStepList.get(selection)+" "+selection+" "+found+" "+height+" "+width+" "+minError);
			//System.out.println("============");
		}
		//System.out.println("------> "+quantizationStepList.get(selection)+" "+selection+" "+rate[selection]+" "+rate[selection]+" "+Errors[selection]);
		
		//System.out.println(adaptiveTargetRate+" "+targetRate+" "+rate[selection]);
		//adaptiveTargetRate = adaptiveTargetRate + targetRate - (float)rate[selection];
		//System.out.println(" --> "+adaptiveTargetRate+" "+targetRate+" "+rate[selection]);
		return selection;
	}

}
