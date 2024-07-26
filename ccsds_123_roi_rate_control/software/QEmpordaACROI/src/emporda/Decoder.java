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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import GiciContextModel.ContextModelling;
import GiciContextModel.ContextProbability;
import GiciEntropyCoder.EntropyBlockCoder.EntropyBlockDecoder;
import GiciEntropyCoder.EntropyIntegerCoder.EntropyIntegerDecoder;
import GiciEntropyCoder.Interface.EntropyCoder;
import GiciEntropyCoder.Interface.EntropyDecoder;
import GiciEntropyCoder.InterleavedEntropycoder.InterleavedEntropyDecoder;
import GiciEntropyCoder.ArithmeticCoder.ArithmeticCoderFLW;
import GiciEntropyCoder.ArithmeticCoder.ArithmeticDecoder;
import GiciEntropyCoder.ArithmeticCoder.DecoderHeaderAC;
import GiciEntropyCoder.ArithmeticCoder.DumbMQDecoder;
import GiciEntropyCoder.ArithmeticCoder.ProbabilityTable;
import GiciEntropyCoder.ArithmeticCoder.SimpleProbabilityTables;
import GiciEntropyCoder.ArithmeticCoder.StaticProbabilityTable;
import GiciException.ParameterException;
import GiciException.WarningException;
import GiciFile.RawImage.LoadFile;
import GiciFile.RawImage.OrderConverter;
import GiciFile.RawImage.RawImage;
import GiciFile.RawImage.RawImageIterator;
import GiciPredictor.SamplePrediction;
import GiciStream.BitInputStream;
import GiciStream.ByteStream;


/**
 * Decoder class of EMPORDA application. Decoder is a decoder of the Recommended Standard MHDC-123 White Book.
 * <p>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public class Decoder {

	private final File file;
	private FileInputStream fileStream;
	private final BitInputStream bis;
	private EntropyDecoder ed;
	private EntropyCoder ec;
	private ContextModelling cm;
	private ContextProbability cp;
	private InterleavedEntropyDecoder iec;
	private DumbMQDecoder dmq;
	private Parameters parameters = null;
	private Predictor predictor = null;
	private boolean debugMode = false;
	private int[] savedPixelOrder = null;
	private String outputFile = null;
	private String inputFile = null;
	private String maskFile = null;
	private int[] pixelOrderTransformation = null;
	private int sampleOrder;
	private Quantizer uq;
	private int quantizationMode;
	private int segmentSize;
	private float targetRate;
	private int window_size;
	private int contextModel = 0;
	private int probabilityModel = 0;
	private int quantizerProbabilityLUT = 0;
	private int numBitsPrecision = 0;
	private int entropyCoderType = 0;
	private int UPDATE_PROB0 = 0;
	private int WINDOW_PROB = 0;
	private FileReader fr = null;
	private int quantizer = 0;
	private int windowsize = 0;
	private int samplePrediction = 0;
    private SamplePrediction samplePredictor = null;
    private int MAXBITS = 32;
    private int sampleType = -1;
    private int RCStrategy = 0;
	
//	this is the probability table used by the arithmetic coder. This is a class variable, because it needs to be updated and used in more than one place.
	ProbabilityTable probTable;
//	Buffered reader to read the prob Tables file
	private BufferedReader br = null;
	
	/**
	 * Bit masks (employed when coding integers).
	 * <p>
	 * The position in the array indicates the bit for which the mask is computed.
	 */
	protected static final int[] BIT_MASKS2 = {1, 1 << 1, 1 << 2, 1 << 3, 1 << 4, 1 << 5, 1 << 6,
	1 << 7, 1 << 8, 1 << 9, 1 << 10, 1 << 11, 1 << 12, 1 << 13, 1 << 14, 1 << 15, 1 << 16, 1 << 17,
	1 << 18, 1 << 19, 1 << 20, 1 << 21, 1 << 22, 1 << 23, 1 << 24, 1 << 25, 1 << 26, 1 << 27,
	1 << 28, 1 << 29, 1 << 30, 1 << 31, 1 << 32};
	
	ByteStream stream;
	
	
	/**
	 * Constructor of Decoder. It receives the name of the input file.
	 *
	 * @param inputFile the file where is saved the image that is going
	 * to be decompressed
	 * @throws FileNotFoundException when something goes wrong and writing must be stopped
	 */
	public Decoder (String inputFile, String outputFile, int sampleType, boolean debugMode, int sampleOrder, Quantizer uq, int quantizer, int quantizationMode, int windowsize, int segmentSize, float targetRate, int contextModel, int probabilityModel, int quantizerProbabilityLUT, int entropyCoderType, int encoderWP, int encoderUP, int samplePrediction, int RCStrategy, String maskFile) throws FileNotFoundException {
		
		file = new File(inputFile);
		fileStream = new FileInputStream(file);
		bis = new BitInputStream( new BufferedInputStream( fileStream ) );
		this.sampleType = sampleType;
		this.sampleOrder = sampleOrder;
		this.debugMode = debugMode;
		this.outputFile = outputFile;
		File f = new File(outputFile);
		f.delete();
		f = null;
		this.inputFile = inputFile;
		this.uq = uq;
		this.quantizer = quantizer;
		this.quantizationMode = quantizationMode;
		this.segmentSize = segmentSize;
		this.targetRate = targetRate;
		this.window_size = 0; //only needed in AD
		this.contextModel = contextModel;
		this.probabilityModel =  probabilityModel;
		this.quantizerProbabilityLUT = quantizerProbabilityLUT;
		this.entropyCoderType = entropyCoderType;
		this.WINDOW_PROB = encoderWP;
		this.UPDATE_PROB0 = encoderUP;
		this.windowsize = windowsize;
		this.samplePrediction = samplePrediction;
		this.RCStrategy = RCStrategy;
		this.maskFile = maskFile;
		
	}
	

	/**
	 * Reads the header with all the needed information for  the decompression process.
	 *
	 * @return object with all the information about the compression process
	 * @throws IOException when something goes wrong and writing must be stopped
	 * @throws ParameterException when an invalid parameter is detected
	 */
	public Parameters readHeader(String optionFile) throws IOException, ParameterException {
		
//		it is no necessary to set the value of the encoder and the creation of probability tables, so we can just put 0 in that argument
//		generateAll has been changed as it was needed to set the value of the decoder. In the encoder that value was true when creating the parameters.
		
		
		parameters = new Parameters(optionFile, null, true, debugMode, 0, 0);
		DecoderHeader dh;
		dh = new DecoderHeader(bis, parameters, debugMode);
		dh.readImageHeader();
		dh.finish();
		switch(sampleOrder) {
			case 0: //BSQ
				savedPixelOrder = OrderConverter.DIM_TRANSP_IDENTITY;
				if (parameters.sampleEncodingOrder == CONS.BAND_SEQUENTIAL) {
					pixelOrderTransformation = OrderConverter.DIM_TRANSP_IDENTITY;
				}else {
					pixelOrderTransformation = OrderConverter.DIM_TRANSP_BSQ_TO_BIL;
				}
				break;
			case 1: //BIL
				savedPixelOrder = OrderConverter.DIM_TRANSP_BSQ_TO_BIL;
				if (parameters.sampleEncodingOrder == CONS.BAND_SEQUENTIAL) {
					pixelOrderTransformation = OrderConverter.DIM_TRANSP_BIL_TO_BSQ;
				}else {
					pixelOrderTransformation = OrderConverter.DIM_TRANSP_IDENTITY;
				}
				break;
			case 2: //BIP
				savedPixelOrder = OrderConverter.DIM_TRANSP_BSQ_TO_BIP;
				if (parameters.sampleEncodingOrder == CONS.BAND_SEQUENTIAL) {
					pixelOrderTransformation = OrderConverter.DIM_TRANSP_BIP_TO_BSQ;
				}else {
					pixelOrderTransformation = OrderConverter.DIM_TRANSP_BIP_TO_BIL;
				}
				break;
		}
		return parameters;
	}

	/**
	 * Compiles all the information needed to create the entropy decoder,
	 * and creates it.
	 * @param verbose indicates whether to display information
	 * @throws Exception 
	 * @throws EOFException 
	 */
	private void startDecoder(boolean verbose) throws Exception {
		
		switch(entropyCoderType)
		{
			case CONS.SAMPLE_ADAPTIVE_DECODER:
				try {
					ed = new EntropyIntegerDecoder(
						bis,
						parameters.initialCountExponent,  // initial count exponent
						parameters.accInitConstant,  // accumulator init constant
						parameters.rescalingCounterSize,  // rescaling counter size
						parameters.dynamicRange,     // dynamic range
						parameters.unaryLengthLimit, // unaly length limit
						parameters.getAccInitTable(),
						parameters.getImageGeometry()[CONS.BANDS],
						verbose
						);
				} catch (ParameterException e) {
					e.printStackTrace();
					System.exit(-1);
				}
				if(verbose) {
					System.out.println("Starting sample adaptive decoder");
				}
				
				break;
				
			case CONS.BLOCK_ADAPTIVE_DECODER:
				ed = new EntropyBlockDecoder(
						bis,
						parameters.blockSize,  // block size
						parameters.dynamicRange,    // dynamic range
						parameters.referenceSampleInterval, // reference sample interval
						verbose);
				if(verbose) {
					System.out.println("Starting block adaptive decoder");
				}
					
				break;
			
			case CONS.ARITHMETIC_ENCODER_FLW:
				cm = new ContextModelling(contextModel);
				int numOfContexts = cm.getNumberOfContexts(MAXBITS);
				numBitsPrecision = 15;
				int coderWordLength = 48;
				cp = new ContextProbability(probabilityModel, numOfContexts, numBitsPrecision, quantizerProbabilityLUT, parameters.entropyCoderType, WINDOW_PROB, UPDATE_PROB0);
				ec = new ArithmeticCoderFLW(coderWordLength, numBitsPrecision, numOfContexts);
				
				fileStream.close();
				fileStream = new FileInputStream(file);
				stream = new ByteStream(fileStream.getChannel());
				stream.putFileSegment(19,fileStream.available());//jump 19 bytes corresponding to the headers
				ec.changeStream(stream);
				ec.restartDecoding();
				ec.reset();
				break;
			
			case CONS.INTERLEAVE_ENTROPY_CODER:
				cm = new ContextModelling(contextModel);
				numOfContexts = cm.getNumberOfContexts(MAXBITS);
				numBitsPrecision = 15;
				coderWordLength = 48;
				cp = new ContextProbability(probabilityModel, numOfContexts, numBitsPrecision, quantizerProbabilityLUT, parameters.entropyCoderType, WINDOW_PROB, UPDATE_PROB0);
				iec = new InterleavedEntropyDecoder();
				break;
				
			case CONS.DUMB_MQ_ENCODER:
				cm = new ContextModelling(contextModel);
				numOfContexts = cm.getNumberOfContexts(MAXBITS);
				numBitsPrecision = 15;
				coderWordLength = 48;
				cp = new ContextProbability(probabilityModel, numOfContexts, numBitsPrecision, quantizerProbabilityLUT, parameters.entropyCoderType, WINDOW_PROB, UPDATE_PROB0);
				dmq = new DumbMQDecoder((InputStream) bis);
				break;
				
		}
		this.predictor = new Predictor(parameters);
	}

	
	/**
	 * Runs the EMPORDA decoder algorithm to decompress the image.
	 *
	 * @return the whole image decoded
	 * @throws Exception 
	 */
	public void decode(boolean verbose) throws Exception {
		
		int readBytes = bis.available();
		int[] imageGeometry = parameters.getImageGeometry();
		float[][][] maskSamples = null;
		
		try {
			startDecoder(verbose);
		} catch (ParameterException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		if(quantizer == 10){
			fr = new FileReader("quantizer.data");
			br = new BufferedReader(fr);
		}
		/*File file = new File("numencodedbits.txt");
		if(file.exists() || this.targetRate != 0) {
			decodeBSQACWithRateControlDecode(verbose);
			System.exit(1);
		}
		file = null;
		*/
		if (parameters.sampleEncodingOrder == CONS.BAND_SEQUENTIAL) {
			if (quantizationMode == 0) decodeBSQ(verbose); //Fixed quantization step mode
			if (quantizationMode == 1) decodeBSQ(verbose); //Fixed quantization step mode
		} else {		
			if (quantizationMode == 0){
				if(parameters.entropyCoderType == CONS.ARITHMETIC_ENCODER_FLW){
					decodeBIAC(verbose); //Fixed rate mode
				}else{
					decodeBI(verbose); //Fixed quantization step mode
				}
				
			}
			else if (quantizationMode == 1){
				switch(RCStrategy) {
				case 0:
					if(parameters.entropyCoderType == CONS.ARITHMETIC_ENCODER_FLW){
						decodeBIACRateControl(verbose); //Fixed rate mode
					}
					break;
				case 1:
					maskSamples = getMask();
					if(parameters.entropyCoderType == CONS.ARITHMETIC_ENCODER_FLW){
						decodeBIACRateControlROI(verbose, maskSamples); //Fixed rate mode
					}
					break;
				case 2:
					maskSamples = getMask();
					if(parameters.entropyCoderType == CONS.ARITHMETIC_ENCODER_FLW){
						decodeBIACRateControlROI2(verbose, maskSamples); //Fixed rate mode
					}
					break;
				case 3:
					maskSamples = getMask();
					if(parameters.entropyCoderType == CONS.ARITHMETIC_ENCODER_FLW){
						decodeBIACRateControlROI3(verbose, maskSamples); //Fixed rate mode
					}
				break;

				
				}
				
			}
		}
		
		switch(entropyCoderType)
		{
			case CONS.SAMPLE_ADAPTIVE_DECODER:
				ed.terminate();
				break;
			case CONS.BLOCK_ADAPTIVE_DECODER:
				ed.terminate();
				break;
		}

		if (verbose) {
			System.out.println("\rRead " + readBytes + " bytes            ");
			System.out.println("\rRate " + readBytes*8/(float)(imageGeometry[CONS.BANDS]*imageGeometry[CONS.HEIGHT]*imageGeometry[CONS.WIDTH]) + " bpppb  ");
		}
		
		//used fo researching purposes can be deleted.
		uq.setWindowsize();
	}
	

	private void decodeBIACRateControlROI(boolean verbose, float[][][] maskSamples) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("decodeBIFLWRateControl ROI");
		int[] imageGeometry = parameters.getImageGeometry();
		int zSize = imageGeometry[CONS.BANDS];
		int ySize = imageGeometry[CONS.HEIGHT];
		int xSize = imageGeometry[CONS.WIDTH];
		int QstepROI = -1;
		int QstepBG = -1;
		int inputQstep = uq.getQuantizationStep();
		
		
		File fileROI = new File("qstepsROI.txt");
		FileReader frROI = new FileReader(fileROI);
		BufferedReader brROI = new BufferedReader(frROI);
		File fileBG = new File("qstepsBG.txt");
		FileReader frBG = new FileReader(fileBG);
		BufferedReader brBG = new BufferedReader(frBG);
		File fileA = new File("auxiliary.txt");
		FileReader frA = new FileReader(fileA);
		BufferedReader brA = new BufferedReader(frA);
		boolean EBits = true;
		
		if(Integer.valueOf(brA.readLine()) == 1) {
			EBits = true;
		}else {
			EBits = false;
		}
		
		
		long qsteps = ySize;//brROI.lines().count();
		
		
		
		
		
		int M = parameters.subframeInterleavingDepth;
		int imageBands[][][] = new int[zSize][2][xSize];
		int predictedImageBands[][][] = new int[zSize][2][xSize];

		try {
			RawImage image = new RawImage(outputFile, parameters.getImageGeometry(), savedPixelOrder, RawImage.WRITE);
			RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.WRITE, true);
			int auxValue = (zSize % M == 0) ?
					zSize / M :
					zSize / M + 1;
			
			for (int y = 0; y < ySize; y++) {
				if (verbose && ySize % 10 == 0) {
					System.out.print("\rDecoding rows: " + y + " to " + Math.min(y+10, ySize));
				}
				QstepROI = Integer.valueOf(brROI.readLine());
				QstepBG = Integer.valueOf(brBG.readLine());
				
				//max error restriction over the ROI area
				if(QstepROI != 0 && QstepROI < inputQstep) QstepROI = inputQstep;
				if(QstepBG != 0 && QstepBG < QstepROI) QstepROI = QstepBG;
				
				
				for (int i = 0; i < auxValue; i++) {
					for (int bit = 15; bit >= 0; bit--){
					for (int z = i * M; z < Math.min((i+1) * M, zSize); z++) {
						
					for (int x = 0; x < xSize; x++) {
						if(bit == 15) imageBands[z][1][x] = 0;
						
						if (x == 0 && y == 0 && z == 0 && bit == 15) {
							ec.restartDecoding();
						}
						
						if(y == 0 || (y > 0 && maskSamples[0][y][x] == 255 ) || EBits == true) {
						
							boolean realBit = false;
							int context = cm.getContext(imageBands, z, 1, x, bit);//get context
							int prob = cp.getProbability(context);//get probability for the computed context
							realBit = ec.decodeBitProb(prob);//decode the bit using the specific probability
							cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability	
							imageBands[z][1][x] += realBit == true ?  BIT_MASKS2[bit] : 0;
						
						}
						
						
						
						
					}}}
				
				for (int z = i * M; z < Math.min((i+1) * M, zSize); z++) {
				for (int x = 0; x < xSize; x++) {
					if(y == 0 || (y > 0 && maskSamples[0][y][x] == 255 ) || EBits == true) {
						imageBands[z][0][x] = imageBands[z][1][x];
						predictedImageBands[z][1][x] = imageBands[z][1][x];
						if(maskSamples[0][y][x] == 255) {
							
							uq.setQuantizationStep(QstepROI);
							
						}else{
							uq.setQuantizationStep(QstepBG);
						}
						predictedImageBands[z][1][x] = predictor.decompress(predictedImageBands, z, y, x, z, 1, uq);
					}else {
						imageBands[z][1][x] = 0;
						imageBands[z][0][x] = imageBands[z][1][x];
						predictedImageBands[z][1][x] = imageBands[z][1][x];
					}
					
				}}
					
				}
				prepareLines(predictedImageBands, it);
				
			}
			if (verbose) {
				System.out.print("\rDecoding image finished");
			}
			image.close(it);
		}catch(UnsupportedOperationException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(IndexOutOfBoundsException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(ClassCastException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}
	}

	private void decodeBIACRateControlROI3(boolean verbose, float[][][] maskSamples) throws Exception {
		// TODO Auto-generated method stub
				System.out.println("decodeBIFLWRateControl ROI");
				int[] imageGeometry = parameters.getImageGeometry();
				int zSize = imageGeometry[CONS.BANDS];
				int ySize = imageGeometry[CONS.HEIGHT];
				int xSize = imageGeometry[CONS.WIDTH];
				int QstepROI = -1;
				int QstepBG = -1;
				int inputQstep = uq.getQuantizationStep();
				
				
				
				BufferedReader brROI = new BufferedReader(new FileReader(new File("qstepsROI.txt")));
				BufferedReader brBG = new BufferedReader(new FileReader(new File("qstepsBG.txt")));
				
				long qsteps = ySize;//brROI.lines().count();
				
				
				
				
				
				int M = parameters.subframeInterleavingDepth;
				int imageBands[][][] = new int[zSize][2][xSize];
				int predictedImageBands[][][] = new int[zSize][2][xSize];

				try {
					RawImage image = new RawImage(outputFile, parameters.getImageGeometry(), savedPixelOrder, RawImage.WRITE);
					RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.WRITE, true);
					int auxValue = (zSize % M == 0) ?
							zSize / M :
							zSize / M + 1;
					
					for (int y = 0; y < ySize; y++) {
						if (verbose && ySize % 10 == 0) {
							System.out.print("\rDecoding rows: " + y + " to " + Math.min(y+10, ySize));
						}
						QstepROI = Integer.valueOf(brROI.readLine());
						QstepBG = Integer.valueOf(brBG.readLine());
						
						//max error restriction over the ROI area
						if(QstepROI != 0 && QstepROI < inputQstep) QstepROI = inputQstep;
						if(QstepBG != 0 && QstepBG < QstepROI) QstepROI = QstepBG;
						
						
						for (int i = 0; i < auxValue; i++) {
							for (int bit = 15; bit >= 0; bit--){
							for (int z = i * M; z < Math.min((i+1) * M, zSize); z++) {
								
							for (int x = 0; x < xSize; x++) {
								if(bit == 15) imageBands[z][1][x] = 0;
								
								if (x == 0 && y == 0 && z == 0 && bit == 15) {
									ec.restartDecoding();
								}
								boolean realBit = false;
								int context = cm.getContext(imageBands, z, 1, x, bit);//get context
								int prob = cp.getProbability(context);//get probability for the computed context
								realBit = ec.decodeBitProb(prob);//decode the bit using the specific probability
								cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability	
								imageBands[z][1][x] += realBit == true ?  BIT_MASKS2[bit] : 0;
							}}}
						
						for (int z = i * M; z < Math.min((i+1) * M, zSize); z++) {
						for (int x = 0; x < xSize; x++) {
								imageBands[z][0][x] = imageBands[z][1][x];
								predictedImageBands[z][1][x] = imageBands[z][1][x];
								if(maskSamples[0][y][x] == 255) {
									uq.setQuantizationStep(inputQstep);
								}else{
									uq.setQuantizationStep(QstepBG);
								}
								predictedImageBands[z][1][x] = predictor.decompress(predictedImageBands, z, y, x, z, 1, uq);
							
							
						}}
							
						}
						
						
						prepareLines(predictedImageBands, it);
						
					}
					if (verbose) {
						System.out.print("\rDecoding image finished");
					}
					image.close(it);
				}catch(UnsupportedOperationException e) {
					throw new Error("Unexpected exception ocurred "+e.getMessage());
				}catch(IndexOutOfBoundsException e) {
					throw new Error("Unexpected exception ocurred "+e.getMessage());
				}catch(ClassCastException e) {
					throw new Error("Unexpected exception ocurred "+e.getMessage());
				}
	}
	
	private void decodeBIACRateControlROI2(boolean verbose, float[][][] maskSamples) throws Exception {
		// TODO Auto-generated method stub
		int[] imageGeometry = parameters.getImageGeometry();
		int zSize = imageGeometry[CONS.BANDS];
		int ySize = imageGeometry[CONS.HEIGHT];
		int xSize = imageGeometry[CONS.WIDTH];
		int QstepROI = -1;
		int QstepBG = -1;
		int inputQstep = uq.getQuantizationStep();
		int yROIFirst = -1;
		
		
		
		BufferedReader brROI = new BufferedReader(new FileReader(new File("qstepsROI.txt")));
		BufferedReader brBG = new BufferedReader(new FileReader(new File("qstepsBG.txt")));
		BufferedReader brA = new BufferedReader(new FileReader(new File("auxiliary.txt")));
		boolean EBits = true;
		
		if(Integer.valueOf(brA.readLine()) == 1) {
			EBits = true;
		}else {
			EBits = false;
		}
		
		
		long qsteps = ySize;//brROI.lines().count();
		
		
		
		
		
		int M = parameters.subframeInterleavingDepth;
		int imageBands[][][] = new int[zSize][2][xSize];
		int predictedImageBands[][][] = new int[zSize][2][xSize];

		try {
			RawImage image = new RawImage(outputFile, parameters.getImageGeometry(), savedPixelOrder, RawImage.WRITE);
			RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.WRITE, true);
			int auxValue = (zSize % M == 0) ?
					zSize / M :
					zSize / M + 1;
			
			for (int y = 0; y < ySize; y++) {
				if (verbose && ySize % 10 == 0) {
					System.out.print("\rDecoding rows: " + y + " to " + Math.min(y+10, ySize));
				}
				QstepROI = Integer.valueOf(brROI.readLine());
				QstepBG = Integer.valueOf(brBG.readLine());
				
				//max error restriction over the ROI area
				if(QstepROI != 0 && QstepROI < inputQstep) QstepROI = inputQstep;
				if(QstepBG != 0 && QstepBG < QstepROI) QstepROI = QstepBG;
				
				
				for (int i = 0; i < auxValue; i++) {
					for (int bit = 15; bit >= 0; bit--){
					for (int z = i * M; z < Math.min((i+1) * M, zSize); z++) {
					predictor.canBeInizialized = true;	
					for (int x = 0; x < xSize; x++) {
						if(maskSamples[0][y][x] == 255) {
							if(yROIFirst == -1) yROIFirst = y;
						}
						if(bit == 15) imageBands[z][1][x] = 0;
						
						if (x == 0 && y == 0 && z == 0 && bit == 15) {
							ec.restartDecoding();
						}
						
						//if(y == 0 || (y > 0 && maskSamples[0][y][x] == 255 ) || EBits == true) {
						if(maskSamples[0][y][x] == 255 || EBits == true) {
						
							boolean realBit = false;
							int context = cm.getContext(imageBands, z, 1, x, bit);//get context
							int prob = cp.getProbability(context);//get probability for the computed context
							realBit = ec.decodeBitProb(prob);//decode the bit using the specific probability
							cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability	
							imageBands[z][1][x] += realBit == true ?  BIT_MASKS2[bit] : 0;
						
						}
						
						
						
						
					}}}
				
				for (int z = i * M; z < Math.min((i+1) * M, zSize); z++) {
				for (int x = 0; x < xSize; x++) {
					//if(y == 0 || (y > 0 && maskSamples[0][y][x] == 255 ) || EBits == true) {
					if(maskSamples[0][y][x] == 255 || EBits == true) {
						imageBands[z][0][x] = imageBands[z][1][x];
						predictedImageBands[z][1][x] = imageBands[z][1][x];
						if(maskSamples[0][y][x] == 255) {
							uq.setQuantizationStep(QstepROI);
						}else{
							uq.setQuantizationStep(QstepBG);
						}
						predictedImageBands[z][1][x] = predictor.decompressROI(predictedImageBands, z, y, x, z, 1, uq, yROIFirst);
					}else {
						imageBands[z][1][x] = 0;
						imageBands[z][0][x] = imageBands[z][1][x];
						predictedImageBands[z][1][x] = imageBands[z][1][x];
					}
					
				}}
					
				}
				prepareLines(predictedImageBands, it);
				
			}
			if (verbose) {
				System.out.print("\rDecoding image finished");
			}
			image.close(it);
			brROI.close();
			brBG.close();
			brA.close();
		}catch(UnsupportedOperationException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(IndexOutOfBoundsException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(ClassCastException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}
	}

	
	private void decodeBIRateControlROI(boolean verbose, float[][][] maskSamples) throws IOException{
		// TODO Auto-generated method stub
		System.out.println("decodeBI ROI");
		
		File file = new File("qsteps.txt");
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		
		
		
        int[] imageGeometry = parameters.getImageGeometry();
		int bands = imageGeometry[CONS.BANDS];
		int height = imageGeometry[CONS.HEIGHT];
		int width = imageGeometry[CONS.WIDTH];
		int M = parameters.subframeInterleavingDepth;
		M = imageGeometry[CONS.BANDS];
		int imageBands[][][] = new int[bands][2][width];
		int Qstep = 1;
		
		try {
			RawImage image = new RawImage(outputFile, parameters.getImageGeometry(), savedPixelOrder, RawImage.WRITE);
			RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.WRITE, true);
			int auxValue = (bands % M == 0) ?
					bands / M :
					bands / M + 1;
			
			for (int y = 0; y < height; y++) {
				Qstep = Integer.valueOf(br.readLine());
				//System.out.println(Qstep);
				uq.setQuantizationStep(Qstep);
				
				if (verbose && height % 10 == 0) {
					System.out.print("\rDecoding rows: " + y + " to " + Math.min(y+10, height));
				}
				for (int i = 0; i < auxValue; i++) {
					for (int z = i * M; z < Math.min((i+1) * M, bands); z++) {
					for (int x = 0; x < width; x++) {
							if (x == 0 && y == 0) {
								ed.init(z);
							}
							imageBands[z][1][x] = ed.decodeSample(y*width + x, z);
							ed.update(imageBands[z][1][x], y*width + x, z);
							//FOR ROI CODING PURPOSES
							if(maskSamples[0][y][x] != 0) {
								uq.setQuantizationStep(1);
							}else {
								uq.setQuantizationStep(Qstep);
							}
							
							imageBands[z][1][x] = predictor.decompress(imageBands, z, y, x, z, 1, uq);
						}
					}
				}
				prepareLines(imageBands, it);
			}
			if (verbose) {
				System.out.print("\rDecoding image finished");
			}
			image.close(it);
		}catch(UnsupportedOperationException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(IndexOutOfBoundsException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(ClassCastException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}
	}


	private void entropyDecoder(int imageBands[][][] ,int Previous1[][], int Previous2[][], int z) throws Exception{
		int height = imageBands[parameters.numberPredictionBands].length;
		int width = imageBands[parameters.numberPredictionBands][0].length;
		int bands = parameters.getImageGeometry()[CONS.BANDS];
		cp.reset();
			for (int bit = MAXBITS-1; bit >= 0; bit--){
			for (int y = 0; y < height; y ++) {
			for (int x = 0; x < width; x ++) {
				boolean realBit = false;
				int context = cm.getContext(imageBands[parameters.numberPredictionBands], Previous1, Previous2, z, y, x, bit);//get context
				int prob = cp.getProbability(context);//get probability for the computed context
				realBit = ec.decodeBitProb(prob);//decode the bit using the specific probability
				cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability	
				imageBands[parameters.numberPredictionBands][y][x] += realBit == true ?  BIT_MASKS2[bit] : 0; 
			}}}
	}

	private void entropyDecoderWithSign(int imageBands[][][] ,int Previous1[][], int Previous2[][], int z) throws Exception{
		int height = imageBands[parameters.numberPredictionBands].length;
		int width = imageBands[parameters.numberPredictionBands][0].length;
		boolean realBit = false;
		boolean signBit = false;
		int context = -1;
		int prob = -1;
		int [][] MapSign = new int [height][width];//1 --> positive, -1 --> negative
		int [][] statusMapSign = new int [height][width];//0 --> not known, 1 --> positive, -1 --> negative
		int [][] statusMapSignificance = new int [height][width];
		long numbits = 0;
		long totalbits = height * width * 16;
		long numzeros = 0;
		
		
		for (int bit = MAXBITS-2; bit >= 0; bit--){
		for (int y = 0; y < height; y ++) {
		for (int x = 0; x < width; x ++) {
			realBit = false;
			context = cm.getContext(imageBands[parameters.numberPredictionBands], Previous1, Previous2, z, y, x, bit);//get context
			prob = cp.getProbability(context);//get probability for the computed context
			realBit = ec.decodeBitProb(prob);//decode the bit using the specific probability
			cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability
			imageBands[parameters.numberPredictionBands][y][x] += realBit == true ?  BIT_MASKS2[bit] : 0;
			if(realBit == true && statusMapSignificance[y][x] == 0){
				statusMapSignificance[y][x] = 1;
				signBit = ec.decodeBit();
				if(signBit == false) statusMapSign[y][x] = -1;
				else statusMapSign[y][x] = 1;
				
			}
			if(bit == 0) imageBands[parameters.numberPredictionBands][y][x] *= statusMapSign[y][x]; 
			//if(y == 0 && x == 70)
			//if(y == 350 && x == 449)System.out.println("x: "+x +"y: "+y+" bit: "+bit+" realBit: "+realBit+" "+imageBands[parameters.numberPredictionBands][y][x]);
			
		}}}
		
		
	}
	
	private void  decodeBSQACWithRateControlDecode(boolean verbose)throws Exception {
		File file = new File("numencodedbits.txt");
		FileReader fr = new FileReader(file);
		long bitsToDecode = -1; 
		int[] imageGeometry = parameters.getImageGeometry();
		int components = imageGeometry[CONS.BANDS];
		int height = imageGeometry[CONS.HEIGHT];
		int width = imageGeometry[CONS.WIDTH];
		if(file.exists()) {
			BufferedReader br = new BufferedReader(fr);
			bitsToDecode = Long.valueOf(br.readLine());
		}
		long bitcounter = 0;
		
		
		//The sampleType is forced to be of the commandline not the stored in the headers. This must be modfied for a final product.
		parameters.getImageGeometry()[CONS.TYPE] = sampleType;
		int bands[][][] = new int[components][height][width];
		short bitBandsDecoded[][][] = new short[components][height][width];
		int context = -1;
		boolean realBit = false;
		boolean allDecoded = false;
		ec.restartDecoding();
		
		try {
			RawImage image = new RawImage(outputFile, parameters.getImageGeometry(), savedPixelOrder, RawImage.WRITE);
			RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.WRITE, true);
			
			for (int bit = MAXBITS-1; bit >= 0; bit--){
				for (int z = 0; z < components; z ++) {
					for (int y = 0; y < height; y ++) {
						for (int x = 0; x < width; x ++) {
							if(z == 0) {
								context = cm.getContext(bands[z], bands[z], bands[z], z, y, x, bit);//get context	
							}else {
								if(z == 1) {
									context = cm.getContext(bands[z], bands[z-1], bands[z-1], z, y, x, bit);//get context	
								}else {
									context = cm.getContext(bands[z], bands[z-1], bands[z-2], z, y, x, bit);//get context
								}
							}
							int prob = cp.getProbability(context);//get probability for the computed context
							realBit = ec.decodeBitProb(prob);//decode the bit using the specific probability
							cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability	
							bands[z][y][x] += realBit == true ?  BIT_MASKS2[bit] : 0;
							bitBandsDecoded[z][y][x] = (short)(bit);
							bitcounter++;
							if(bitcounter == bitsToDecode) {
								allDecoded = true;
								break;
							}
						}
						if(allDecoded) break;
					}
					if(allDecoded) break;
				}
				if(allDecoded) break;
			}
			
			//Aproximate decoded value to the half range of the remaining bits to decode.
			for (int z = 0; z < components; z ++) {
			for (int y = 0; y < height; y ++) {
			for (int x = 0; x < width; x ++) {
				if(bitBandsDecoded[z][y][x] > 1)bands[z][y][x] += (1 << bitBandsDecoded[z][y][x] - 1); 
			}}}
			
			/*samplePredictor = new SamplePrediction(this.parameters);
			int prediction = -1;
		
			
			for (int z = 0; z < components; z ++) {
				samplePredictor.setSamples(bands[z]);
				for (int y = 0; y < height; y ++) {
				for (int x = 0; x < width; x ++) {
					bands[z][y][x] = samplePredictor.getUnmappedSample(bands[z][y][x], 0, 0);
					prediction = samplePredictor.predict(y, x);
					bands[z][y][x] = samplePredictor.getSampleRecovered(bands[z][y][x], prediction, z, y, x);
				}}
			}*/
			
			
			for (int z = 0; z < components; z ++) {
				writeBand(bands[z], it);
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
		
		
	
		
		/*
		private void prepareBands(int[][][] bands, RawImageIterator<int[]> it) {
			int[][] tmpBand = bands[0];
			writeBand(bands[parameters.numberPredictionBands], it);		
			for(int i = 0; i < parameters.numberPredictionBands; i ++) {
				bands[i] = bands[i + 1];
			}
			bands[parameters.numberPredictionBands] = tmpBand;
		}
		*/
		
	}
	
	
	
	/**
	 * Decodes an image in BSQ order
	 * @throws Exception 
	 *
	 * @params img the whole image
	 * @params bands the number of bands of the image
	 * @params height the number of lines of one band of the image
	 * @params width the number of samples of one line of the image
	 */
	private void decodeBSQ(boolean verbose) throws Exception {
		File newFile = new File("predicted-"+inputFile);
		File file = new File("qsteps.txt");
		FileReader fr = null;
		BufferedReader br = null;
		if(file.exists()){
			 fr = new FileReader(file);
			 br = new BufferedReader(fr);
		}
		
		newFile.delete();
		newFile = null;
		FileOutputStream fos = null;
		DataOutputStream dos = null;
		
		
		int[] imageGeometry = parameters.getImageGeometry();
		int bands = imageGeometry[CONS.BANDS];
		int height = imageGeometry[CONS.HEIGHT];
		int width = imageGeometry[CONS.WIDTH];
		//The sampleType is forced to be of the commandline not the stored in the headers. This must be modfied for a final product.
		parameters.getImageGeometry()[CONS.TYPE] = sampleType;
		int Previous1[][] = new int[height][width];
		int Previous2[][] = new int[height][width];
		
		int imageBands[][][] = new int[parameters.numberPredictionBands + 1][height][width];
		try {
			RawImage image = new RawImage(outputFile, parameters.getImageGeometry(), savedPixelOrder, RawImage.WRITE);
			
			RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.WRITE, true);
			
			switch(samplePrediction){
			case 2:
				//samplePredictor = new SamplePrediction(this.parameters);
				break;
			}
			
			switch(entropyCoderType){
				case CONS.SAMPLE_ADAPTIVE_DECODER:
					for (int z = 0; z < bands; z ++) {
						if(br != null){
							int Qstep = Integer.valueOf(br.readLine());
							uq.setQuantizationStep(Qstep);
						}
						if (verbose) {
							System.out.print("\rDecoding band: " + z);
						}
						ed.init(z);
						
						for (int y = 0; y < height; y ++) {
							for (int x = 0; x < width; x ++) {
								
								imageBands[parameters.numberPredictionBands][y][x] = ed.decodeSample(y*width + x, z);
								ed.update(imageBands[parameters.numberPredictionBands][y][x], y*width + x, z);
								imageBands[parameters.numberPredictionBands][y][x] = predictor.decompress(imageBands, z, y, x, parameters.numberPredictionBands, y, uq);
								
							}
						}
						prepareBands(imageBands, it);
					}
				break;
				
				case CONS.BLOCK_ADAPTIVE_DECODER:
					for (int z = 0; z < bands; z ++) {
						if(br != null){
							int Qstep = Integer.valueOf(br.readLine());
							uq.setQuantizationStep(Qstep);
						}
						
						if (verbose) {
							System.out.print("\rDecoding band: " + z);
						}
						ed.init(z);
						for (int y = 0; y < height; y ++) {
						for (int x = 0; x < width; x ++) {
							imageBands[parameters.numberPredictionBands][y][x] = ed.decodeSample(y*width + x, z);
							ed.update(imageBands[parameters.numberPredictionBands][y][x], y*width + x, z);
							imageBands[parameters.numberPredictionBands][y][x] = predictor.decompress(imageBands, z, y, x, parameters.numberPredictionBands, y, uq);
						}}
						prepareBands(imageBands, it);
					}
				break;
				
				case CONS.ARITHMETIC_DECODER_FLW:
					for (int z = 0; z < bands; z ++) {
						
						for (int y = 0; y < height; y ++){
						for (int x = 0; x < width; x ++){
							imageBands[parameters.numberPredictionBands][y][x] = 0;
						}}
						
						if(br != null){
							int Qstep = Integer.valueOf(br.readLine());
							uq.setQuantizationStep(Qstep);
						}
						
						
						
						switch(this.samplePrediction){
						case 0:
							entropyDecoder(imageBands,Previous1, Previous2, z);
							break;
							
						case 1:
							entropyDecoder(imageBands,Previous1, Previous2, z);
							//entropyDecoderWithSign(imageBands,Previous1, Previous2, z);
							
							for(int y = 0; y < height; y ++){
							for(int x = 0; x < width; x ++){
								imageBands[parameters.numberPredictionBands][y][x] = predictor.decompress(imageBands, z, y, x, parameters.numberPredictionBands, y, uq);
							}}
							break;
						case 2:
							entropyDecoder(imageBands,Previous1, Previous2, z);
							
							//When 3D contexts are used we need to copy the predicted into temporal structures
							if(contextModel == 13){
								if(z == 0){
									for (int y = 0; y < height; y ++) {
									for (int x = 0; x < width; x ++) {
										Previous1[y][x]  = imageBands[parameters.numberPredictionBands][y][x];
									}}
								}
								if(z > 0){
									for (int y = 0; y < height; y ++) {
									for (int x = 0; x < width; x ++) {
										Previous2[y][x]  = Previous1[y][x];
										Previous1[y][x]  = imageBands[parameters.numberPredictionBands][y][x];
									}}
									
								}
							}
							
							for (int y = 0; y < height; y ++) {
							for (int x = 0; x < width; x ++) {
								//imageBands[parameters.numberPredictionBands][y][x] = samplePredictor.getUnmappedSample(imageBands[parameters.numberPredictionBands][y][x], 0, 0);
								//imageBands[parameters.numberPredictionBands][y][x] = imageBands[parameters.numberPredictionBands][y][x];
							}}
						}
						
						
						
						//reordering bands
						prepareBands(imageBands, it);
						
						//ec.reset();
						
					}
					break;
				
				
					
			}
			
			
			
			
			
			image.close(it);
			if (verbose) {
				System.out.print("\rDecoding image finished");
			}
		}catch(UnsupportedOperationException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(IndexOutOfBoundsException e) {
			e.printStackTrace();
		}catch(ClassCastException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}
	}
	

	
	

	
	/**
	 * Write the next band of the image.
	 * @param it the BSQ iterator over the image
	 */
	private void writeBand(int[][] band, RawImageIterator<int[]> it) {
		int height =  parameters.getImageGeometry()[CONS.HEIGHT];

		for(int i = 0; i < height; i ++) {
			it.next();
			it.set(band[i]);
		}
	}
	
	/**
	 * Save the first band and reordered bands for prediction
	 * @param bands an array with the prediction bands
	 * @param it the BSQ iterator over the image
	 */
	private void prepareBands(int[][][] bands, RawImageIterator<int[]> it) {
		int[][] tmpBand = bands[0];
		writeBand(bands[parameters.numberPredictionBands], it);		
		for(int i = 0; i < parameters.numberPredictionBands; i ++) {
			bands[i] = bands[i + 1];
		}
		bands[parameters.numberPredictionBands] = tmpBand;
	}
	
	/**
	 * Decodes an image in BI order
	 *
	 * @params img the whole image
	 * @params bands the number of bands of the image
	 * @params height the number of lines of one band of the image
	 * @params width the number of samples of one line of the image
	 * @params M the encoding interleaving value
	 * @throws IOException if can not read file informationn
	 */
	private void decodeBIRateControl(boolean verbose) throws IOException {
		System.out.println("decodeBI");
		
		File file = new File("qsteps.txt");
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		
		
		
        int[] imageGeometry = parameters.getImageGeometry();
		int bands = imageGeometry[CONS.BANDS];
		int height = imageGeometry[CONS.HEIGHT];
		int width = imageGeometry[CONS.WIDTH];
		int M = parameters.subframeInterleavingDepth;
		M = imageGeometry[CONS.BANDS];
		int imageBands[][][] = new int[bands][2][width];
		int Qstep = 1;
		
		try {
			RawImage image = new RawImage(outputFile, parameters.getImageGeometry(), savedPixelOrder, RawImage.WRITE);
			RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.WRITE, true);
			int auxValue = (bands % M == 0) ?
					bands / M :
					bands / M + 1;
			
			for (int y = 0; y < height; y++) {
				Qstep = Integer.valueOf(br.readLine());
				//System.out.println(Qstep);
				uq.setQuantizationStep(Qstep);
				
				if (verbose && height % 10 == 0) {
					System.out.print("\rDecoding rows: " + y + " to " + Math.min(y+10, height));
				}
				for (int i = 0; i < auxValue; i++) {
					for (int z = i * M; z < Math.min((i+1) * M, bands); z++) {
					for (int x = 0; x < width; x++) {
							if (x == 0 && y == 0) {
								ed.init(z);
							}
							imageBands[z][1][x] = ed.decodeSample(y*width + x, z);
							/*if(z == 1 && y == 10 & x < 10){
								System.out.println("z:" +z+" y:" +y+" x:"+x+" "+imageBands[z][1][x]+" qstep: "+uq.getQuantizationStep());
							}*/
							ed.update(imageBands[z][1][x], y*width + x, z);
							imageBands[z][1][x] = predictor.decompress(imageBands, z, y, x, z, 1, uq);
						}
					}
				}
				prepareLines(imageBands, it);
			}
			if (verbose) {
				System.out.print("\rDecoding image finished");
			}
			image.close(it);
		}catch(UnsupportedOperationException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(IndexOutOfBoundsException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(ClassCastException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}
	}
	
	/**
	 * Decodes an image in BI order
	 *
	 * @params img the whole image
	 * @params bands the number of bands of the image
	 * @params height the number of lines of one band of the image
	 * @params width the number of samples of one line of the image
	 * @params M the encoding interleaving value
	 * @throws IOException if can not read file informationn
	 */
	private void decodeBI(boolean verbose) throws IOException {
		
		int[] imageGeometry = parameters.getImageGeometry();
		int bands = imageGeometry[CONS.BANDS];
		int height = imageGeometry[CONS.HEIGHT];
		int width = imageGeometry[CONS.WIDTH];
		int M = parameters.subframeInterleavingDepth;
		int imageBands[][][] = new int[bands][2][width];

		try {
			RawImage image = new RawImage(outputFile, parameters.getImageGeometry(), savedPixelOrder, RawImage.WRITE);
			RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.WRITE, true);
			int auxValue = (bands % M == 0) ?
					bands / M :
					bands / M + 1;
			
			for (int y = 0; y < height; y++) {
				if (verbose && height % 10 == 0) {
					System.out.print("\rDecoding rows: " + y + " to " + Math.min(y+10, height));
				}
				for (int i = 0; i < auxValue; i++) {
					for (int x = 0; x < width; x++) {
						for (int z = i * M; z < Math.min((i+1) * M, bands); z++) {
							if (x == 0 && y == 0) {
								ed.init(z);
							}
							imageBands[z][1][x] = ed.decodeSample(y*width + x, z);
							ed.update(imageBands[z][1][x], y*width + x, z);
							imageBands[z][1][x] = predictor.decompress(imageBands, z, y, x, z, 1, uq);
						}
					}
				}
				prepareLines(imageBands, it);
			}
			if (verbose) {
				System.out.print("\rDecoding image finished");
			}
			image.close(it);
		}catch(UnsupportedOperationException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(IndexOutOfBoundsException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(ClassCastException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}
	}
	
	
	/**
	 * Decodes an image in BI order
	 * @throws Exception 
	 *
	 * @params img the whole image
	 * @params bands the number of bands of the image
	 * @params height the number of lines of one band of the image
	 * @params width the number of samples of one line of the image
	 * @params M the encoding interleaving value
	 */
	private void decodeBIAC(boolean verbose) throws Exception {
		int[] imageGeometry = parameters.getImageGeometry();
		int bands = imageGeometry[CONS.BANDS];
		int height = imageGeometry[CONS.HEIGHT];
		int width = imageGeometry[CONS.WIDTH];
		int M = parameters.subframeInterleavingDepth;
		int imageBands[][][] = new int[bands][2][width];
		int predictedImageBands[][][] = new int[bands][2][width];

		try {
			RawImage image = new RawImage(outputFile, parameters.getImageGeometry(), savedPixelOrder, RawImage.WRITE);
			RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.WRITE, true);
			int auxValue = (bands % M == 0) ?
					bands / M :
					bands / M + 1;
			
			for (int y = 0; y < height; y++) {
				if (verbose && height % 10 == 0) {
					System.out.print("\rDecoding rows: " + y + " to " + Math.min(y+10, height));
				}
				
				
				for (int bit = 15; bit >= 0; bit--){
				for (int z = 0; z < bands; z++) {
				for (int x = 0; x < width; x++) {
				
					if (x == 0 && y == 0 && z == 0 && bit == 15) {
						ec.restartDecoding();
					}
					if(bit == 15) imageBands[z][1][x] = 0;
					
					
					boolean realBit = false;
					//int context = cm.getContext(imageBands[z], 1, x, bit);//get context
					int context = cm.getContext(imageBands, z, 1, x, bit);//get context
					int prob = cp.getProbability(context);//get probability for the computed context
					realBit = ec.decodeBitProb(prob);//decode the bit using the specific probability
					
					cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability	
					imageBands[z][1][x] += realBit == true ?  BIT_MASKS2[bit] : 0;
				}}}
				
				for (int z = 0; z < bands; z++) {
				for (int x = 0; x < width; x++) {
					imageBands[z][0][x] = imageBands[z][1][x];
					predictedImageBands[z][1][x] = imageBands[z][1][x];
					predictedImageBands[z][1][x] = predictor.decompress(predictedImageBands, z, y, x, z, 1, uq);
				}}
					
				
				prepareLines(predictedImageBands, it);
				
			}
			if (verbose) {
				System.out.print("\rDecoding image finished");
			}
			image.close(it);
		}catch(UnsupportedOperationException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(IndexOutOfBoundsException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(ClassCastException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}
	}
	
	/**
	 * Decodes an image in BI order
	 * @throws Exception 
	 *
	 * @params img the whole image
	 * @params bands the number of bands of the image
	 * @params height the number of lines of one band of the image
	 * @params width the number of samples of one line of the image
	 * @params M the encoding interleaving value
	 */
	private void decodeBIACRateControl(boolean verbose) throws Exception {
		//TODO
		System.out.println("decodeBIFLWRateControl");
		int[] imageGeometry = parameters.getImageGeometry();
		int bands = imageGeometry[CONS.BANDS];
		int height = imageGeometry[CONS.HEIGHT];
		int width = imageGeometry[CONS.WIDTH];
		int Qstep = -1;
		File file = new File("qsteps.txt");
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		long qsteps = br.lines().count();
		file = new File("qsteps.txt");
		fr = new FileReader(file);
		br = new BufferedReader(fr);
		
		
		
		
		int M = parameters.subframeInterleavingDepth;
		int imageBands[][][] = new int[bands][2][width];
		int predictedImageBands[][][] = new int[bands][2][width];

		try {
			RawImage image = new RawImage(outputFile, parameters.getImageGeometry(), savedPixelOrder, RawImage.WRITE);
			RawImageIterator<int[]> it = (RawImageIterator<int[]>) image.getIterator(new int[0], pixelOrderTransformation, RawImage.WRITE, true);
			int auxValue = (bands % M == 0) ?
					bands / M :
					bands / M + 1;
			
			for (int y = 0; y < height; y++) {
				if (verbose && height % 10 == 0) {
					System.out.print("\rDecoding rows: " + y + " to " + Math.min(y+10, height));
				}
				if(qsteps == height){
					Qstep = Integer.valueOf(br.readLine());
					uq.setQuantizationStep(Qstep);
				}
				
				
				for (int i = 0; i < auxValue; i++) {
					for (int bit = 15; bit >= 0; bit--){
					for (int z = i * M; z < Math.min((i+1) * M, bands); z++) {
						if(qsteps != height && bit == 15){
							Qstep = Integer.valueOf(br.readLine());
							uq.setQuantizationStep(Qstep);
						}
					for (int x = 0; x < width; x++) {
					
						if (x == 0 && y == 0 && z == 0 && bit == 15) {
							ec.restartDecoding();
						}
						if(bit == 15) imageBands[z][1][x] = 0;
						
						
						boolean realBit = false;
						//int context = cm.getContext(imageBands[z], 1, x, bit);//get context
						int context = cm.getContext(imageBands, z, 1, x, bit);//get context
						int prob = cp.getProbability(context);//get probability for the computed context
						realBit = ec.decodeBitProb(prob);//decode the bit using the specific probability
						cp.updateSymbols(realBit, context);//updates the symbols decoded to properly compute the probability	
						imageBands[z][1][x] += realBit == true ?  BIT_MASKS2[bit] : 0;
					}}}
				for (int z = i * M; z < Math.min((i+1) * M, bands); z++) {
				for (int x = 0; x < width; x++) {
					imageBands[z][0][x] = imageBands[z][1][x];
					predictedImageBands[z][1][x] = imageBands[z][1][x];
					predictedImageBands[z][1][x] = predictor.decompress(predictedImageBands, z, y, x, z, 1, uq);
				}}
					
				}
				prepareLines(predictedImageBands, it);
				
			}
			if (verbose) {
				System.out.print("\rDecoding image finished");
			}
			image.close(it);
		}catch(UnsupportedOperationException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(IndexOutOfBoundsException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(ClassCastException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}
	}
	
	
	
	/**
	 * Try to write a line of all bands and reordered lines for prediction
	 * @param bands is the array with the last lines loaded
	 * @param it the BIL iterator over the image
	 */
	private void prepareLines(int[][][] bands) {
		int numBands = parameters.getImageGeometry()[CONS.BANDS];
		int width = bands[0][0].length;
		for(int i = 0; i < numBands; i ++) {
			for(int x = 0; x < width; x ++) {
				bands[i][0][x] = bands[i][1][x];
			}
		}
	}
	
	/**
	 * Try to write a line of all bands and reordered lines for prediction
	 * @param bands is the array with the last lines loaded
	 * @param it the BIL iterator over the image
	 */
	private void prepareLines(int[][][] bands, RawImageIterator<int[]> it) {
		int numBands = parameters.getImageGeometry()[CONS.BANDS];
		int width = bands[0][0].length;
		for(int i = 0; i < numBands; i ++) {
			for(int x = 0; x < width; x ++) {
				bands[i][0][x] = bands[i][1][x];
			}
		}
		for(int i = 0; i < numBands; i ++) {
			it.next();
			it.set(bands[i][1]);
		}	
	}
	
	private float[][][] getMask() {
		float[][][] maskSamples = null;
		if(maskFile != null){
			LoadFile mask = null;
			try {
				LoadFile.isRaw(maskFile);
				mask = new LoadFile(maskFile, 1, parameters.getImageGeometry()[CONS.HEIGHT], parameters.getImageGeometry()[CONS.WIDTH], 1, 0, false);
				maskSamples = mask.getImage();
			} catch (WarningException e) {
				e.printStackTrace();
			}
		}
		return maskSamples;
	}
	
}
