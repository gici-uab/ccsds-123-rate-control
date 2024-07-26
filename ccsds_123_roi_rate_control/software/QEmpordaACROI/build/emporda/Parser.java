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

import GiciException.ErrorException;
import GiciException.ParameterException;
import GiciException.WarningException;
import GiciParser.BaseArgumentsParser;


/**
 * Arguments parser for EMPORDA application (extended from ArgumentsParser).
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public class Parser extends BaseArgumentsParser {

	//ARGUMENTS SPECIFICATION
	private String[][] coderArguments = {

			{"-c", "--compress", "", "", "1", "1",
				"Compress de input image."
			},

			{"-d", "--decompress", "", "", "1", "1",
				"Decompress de input image."
			},

			{"-i", "--inputImage", "{string}", "", "1", "1",
				"It must be a raw image"
			},

			{"-o", "--outputFile", "{string}", "", "1", "1",
				"Output file.\n"
				+ "COMPRESSING: Output file name without extension.\n"
				+ "DECOMPRESSING: Output image file WITH extension."
			},

			{"-ig", "--inputImageGeometry", "{int int int int int int}", "", "1", "1",
				"Geometry of raw image data. Only nessesary if image is raw data. Parameters are:\n" + 
				"1- zSize (number of image components)\n"+
				"2- ySize (image height)\n"+ 
				"3- xSize (image width)\n"+
				"4- data type. Possible values are:\n" + 
				"	1- unsigned int (1 byte)\n"+
				"	2- unsigned int (2 bytes)\n"+ 
				"	3- signed int (2 bytes)\n" + 
				"	4- signed int (4 bytes)\n" + 
				"5- Byte order (0 if BIG ENDIAN, 1 if LITTLE ENDIAN)\n"+
				"6- 1 if 3 first components are RGB, or 0 otherwise\n" 

			},
			{"-so", "--sample-order", "{int}", "", "0", "1",
				"The sample order in which the image will be loaded or saved. \n"
				+ "0.- BSQ, 1.- BIL, 2.- BIP. This value is 0 by default. "
			},
			{"-v", "--verbose", "", "", "0", "1",
				"Displays information about the progress in the task done by the program."
			},
			{"-e", "--endianess", "", "", "0", "1",
				"Is used to tell the program the image's endianess after decompression. \n"
				+ "0 BIG_ENDIAN, 1 LITTLE_ENDIAN. This value is 0 by default"
			},
			{"-f", "--option-file", "{String}", "", "0", "1",
				"sets the option file for the compression process"
			},
			{"-dbg", "--debug-mode", "", "", "0", "1", 
				"sets debug mode, in which the program outputs useful information for debugging tasks"
			},
			{"-qm", "--quantization-mode", "{int}", "", "1", "1",
				"The quantization mode to be used. \n"
				+ "0.- Fixed Quantization Step, 1.- Rate Allocation "
			},
			{"-q", "--quantizer", "{int}", "", "0", "1",
				"The quantizer used for quantization. \n"
				+ "0.- UTQ_URQ. \n"
				+ "1.- UTQDZ_NURQ. \n"
				+ "2.- UTQDZ_URQ. \n"
				+ "3.- URURQ. \n"
				+ "4.- OptimalUTQDZ. \n"
				+ "5.- UTQDZ_SingleOffset. \n"
				+ "6.- URQMAX (M-CALIC Quantizer). \n"
				+ "7.- URQ.\n"
				+ "8.- 2 Step Deadzone Quantizer. \n"
				+ "9.- Deadzone Quantizer. \n"
				+ "This value is 0 by default."
			},
			{"-qs", "--quantization-step", "[int]", "", "0", "1",
				"sets the quantization step. This value is 1 by default."
			},
			{"-wm", "--window-size", "{int}", "", "0", "1",
				"window size used for computing the median to be used in the 2 Step Deadzone Quantizer (-q 8). This value is 17 by default."
			},
			{"-tr", "--target-rate", "{double}", "", "0", "1",
				"sets the target rate to achieve. This parameter is mandatory if the Fixed Rate Mode is chosen. Number of bits per sample used for the final codestream. This value is 0 by default, meaning the maximum length of the codestram is stored. That means Lossless recovery."
			},
			{"-ss", "--segment-size", "{int}", "", "0", "1",
				"sets the segment size (in samples) used to update the rate. This parameter is mandatory if the Fixed Rate Mode is chosen."
			},
			{"-ec", "--encoder-type", "{int}", "", "0", "1",
				"The encoder type that will encode the image. \n"
				+ "0.- Entropy Integer Coder. \n"
				+ "1.- Block Adaptative Coder.\n"
				+ "2.- Arithmetic coder with codewords of fixed length.\n"
				+ "3.- Interleaved Entropy Coder. DECODER IS NOT IMPLEMENTED!!!\n"
				+ "4.- Dumb MQ Coder. DECODER DOES NOT WORK. LOOKS LIKE AN ERROR IN THE INPUTSTREAM\n"
				+ "5.- Computes the binary entropy. Decoder side is not useful. This is only for research analitic purposes.\n"
				+ "6.- Dual Arithmetic coder with codewords of fixed length..\n"
				+ "7.- Symbol Arithmetic coder with codewords of fixed length..\n"
				+ " This value is 0 by default. "
			},
			{"-cm", "--context model", "{int}", "", "0", "1",
				"BE CAREFUL! IT DOES NOT CORRESPONDS WITH THE SOURCECODE BECAUSE IT IS EXPERIMENTAL IMPLEMATATION. The context used to compute the probabilities for the entropy coder. This value is 0 by default.\n"
				+ "0.- No context model is used.\n"
				+ "1.- 2 contexts are used. It check the Vertical neighbor (It Checks if the bit has been significant in previous bitplanes).\n"
				+ "2.- 2 contexts are used. It check the Left neighbor (It Checks if the bit has been significant in previous bitplanes).\n"
				+ "3.- 4 contexts are used. It check the Vertical and Left neighbor (It Checks if the bit has been significant in previous bitplanes).\n"
				+ "4.- 8 contexts are used. It check the Vertical, Left and Diagonal Upper-Left neighbor (It Checks if the bit has been significant in previous bitplanes).\n"
				+ "5.- 8 contexts are used. It check the Vertical, Left and Previous band neighbor (It Checks if the bit has been significant in previous bitplanes).\n"
				+ "6.- 2 contexts are used. It check the Vertical neighbor (It Checks if the bit is significant in the current bitplane).\n"
				+ "7.- 2 contexts are used. It check the Left neighbor (It Checks if the bit is significant in the current bitplane).\n"
				+ "8.- 4 contexts are used. It check the Vertical and Left neighbor (It Checks if the bit is significant in the current bitplane).\n"
				+ "9.- 8 contexts are used. It check the Vertical, Left and Diagonal Upper-Left neighbor (It Checks if the bit is significant in the current bitplane).\n"
				+ "10.- 8 contexts are used. It check the Vertical, Left and Previous band neighbor (It Checks if the bit is significant in the current bitplane).\n"
			},
			{"-pm", "--probability model", "{int}", "", "0", "1",
				"Probability model employed for the entropy coder. This value is 0 by default.\n"
				+ "0.- The probability is estimated using a full division operation.\n"
				+ "1.- The probability is estimated using a division implemented through a quantized Look Up Table. This option must be used with -qlut option.\n"
				+ "2.- The probability is estimated using only bitwise operators and witout division. When this option is used -wp and -up parameters must be the same value of form 2^X."
				+ "3.- The probability is estimated using a full division operation in a symbol-by-symbol fashion. Before to encode the symbol the probabilty is updated with the division, then the window is updated."
			},
			{"-wp", "--windowProbability", "{int}", "", "0", "1",
				"Indicates the maximum number of symbols within the variable-size sliding window that are employed for the Entropy Coder to compute the probability of the context. Must be of the form 2^X. This value is 8 by default.\n"
			},
			{"-up", "--updateProbability", "{int}", "", "0", "1",
				"Indicates the number of symbols coded before updating the context probability in the Entropy Coder. Must be of the form 2^X. This value is 2048 by default.\n"
			},
			{"-qlut", "--lutquantizer", "{int}", "", "0", "1",
				"Step size of the LUT used to compute the probability. This value is 0 by default, meaning that the size of the LUT is not quantized.\n"
			},
			{"-rcs", "--RateControlStrategy", "", "", "0", "1",
				"Rate Control Strategies are only implemented for BIL Encoding Order, which must be set into the options file.\n"
				+ "0.- Strategy 0 (Valsesia & Magli 2016)\n"
				+ "1.- Strategy 1 (Proposal 1 - process one buffer then the rate is adapted according to the target bit-rate)\n"
				+ "2.- Strategy 2 (Proposal 2 - process buffer-by-buffer). The quality trys to be constant for all lines.\n"
				+ "4.- Strategy 4 (Proposal 4 - process buffer-by-buffer). The quality trys to be constant for all lines but achives the target during the coding process.\n"
				+ "8.- Strategy 8 (Proposal 8 - process buffer-by-buffer). The quality trys to be constant for all lines but achives the target during the coding process. The rate and the error are estimate instead of comuted to reduce de computational resources\n"
				+ "3.- Strategy 3 (Proposal 3 - process buffer-by-buffer). The quality trys to be constant for all lines and spectrum.\n"
				+ "6.- Strategy 3 (Proposal 3 - process buffer-by-buffer). The quality trys to be constant for all lines and spectrum achives the target during the coding process.\n"
			},
			{"-sp", "--samplePrediction", "{int}", "", "0", "1",
				"Indicates if the sample prediction is applied before the entropy encoder (this is only implemented for BSQCode()).\n"
				+ "0.- No sample preditction\n"
				+ "1.- CCSDS-123 Sample Prediction\n"
				+ "2.- FLIF Predictor"
			},
			{"-bf", "--buffersize", "{int}", "", "0", "1",
				"Buffer size used for some rate control techniques. This value is needed for: -rcs 1 \n"
			},
			{"-mk", "--inputMask", "{string}", "", "0", "1",
			"File of the mask. It must be in RAW format, 8 bpp. " +
                        "The mask must have the same xSize, ySize than the image to compress but with zSize = 1." +
                        "The same 2D region will be considered for all the bands. " +
                        "ROI will be those samples that are white (sample value = 255) and BG those that are black (sample value = 0).\n"
			},
			{"-h", "--help", "", "", "0", "1",
				"Displays help and exits program."
			},
			
	};

	
	
	 
	 
	 
	//ARGUMENTS VARIABLES + Default Values
	private int action = 0; // Indicates if the program compress 0 or decompress 1
	private String inputFile = ""; // Input File
	private String outputFile = ""; // Output File
	private String maskFile = ""; // MAsk File
	private int[] imageGeometry = null; // Image geometry
	private boolean verbose = false; // The program shows informatin about the compression
	private String optionFile = null; //option file
	private int sampleOrder = 0;     // sample order of the image
	private int encoderType = 0;	// Type of encoder
	private int encoderWP = 2048;	// Type of encoder
	private int encoderUP = 8;	// Type of encoder
	private int AC_option = 1; 		// Way of probability table's creation
	private int endianess = 0; // endianess of the image decompressed
	private boolean debugMode = false; //debug mode
	private int quantizationStep = 1; //quantization step
	private int[] quantizationSteps = null; //quantization steps
	private int quantizationMode = 0; //quantization mode
	private int quantizer = 0; // quantizer used
	private float targetRate = 0; // target rate
	private int segmentSize = 0; // number of samples
	private int contextModel = 0;
	private int probabilityModel = 0;
	private int quantizerProbabilityLUT = 0;
	private int RCStrategy = 0;//rateControl Strategy
	private int windowsize = 256;
	private int bufferSize = 1;
	private int samplePrediction = 0;// Type of spatial sample prediction
	
	
	/** Receives program arguments and parses it, setting to arguments variables.
	 *
	 * @param arguments the array of strings passed at the command line
	 *
	 * @throws ParameterException when an invalid parsing is detected
	 * @throws ErrorException when some problem with method invocation occurs
	 */
	public Parser(String[] arguments) throws ParameterException, ErrorException {

		parse(coderArguments, arguments);

	}

	/**
	 * Parse an argument using parse functions from super class and put its value/s to the desired variable.
	 * This function is called from parse function of the super class.
	 *
	 * @param argFound number of parameter (the index of the array coderArguments)
	 * @param options the command line options of the argument
	 *
	 * @throws ParameterException when some error about parameters passed (type, number of params, etc.) occurs
	 */
	public void parseArgument(int argFound, String[] options) throws ParameterException {

		switch(argFound) {

		case 0: // -c --compress
			action = 0;
			coderArguments[1][4] = "0";
			coderArguments[1][5] = "0";
			// should set encodertype nor Prob Tables option
			coderArguments[16][4] = "1";
			break;

		case 1: // -d -decompress 
			action = 1;
			coderArguments[0][4] = "0";
			coderArguments[0][5] = "0";
			coderArguments[4][4] = "0";
			coderArguments[4][5] = "0";
			
			break;

		case  2: //-i  --inputImage
			inputFile = parseString(options);
			break;

		case  3: //-o  --outputFile
			outputFile = parseString(options);
			break;

		case  4: //-ig  --inputImageGeometry
			imageGeometry = parseIntegerArray(options, 6);
	        if(imageGeometry[CONS.TYPE] < 1 || imageGeometry[CONS.TYPE] > 4) {
                throw new ParameterException("datatype in image" + 
                " geometry must be: \n" + 
                "1.- Unsigned int (1 byte)\n" + 
                "2.- Unsigned int (2 bytes)\n" + 
                "3.- Signed int (2 bytes)\n" +
                "4.- igned int (4 bytes)");
            }
            coderArguments[1][4] = "0";
			coderArguments[1][5] = "0";
			break;
			
		case 5: //-so --sample-order
			sampleOrder = parseInteger(options);
			break;

		case 6: //-v --verbose
			verbose = true;
			break;

		case 7: // -e --endianess
			endianess = parseInteger(options);
			break;

		case 8: //-f --option-file
			optionFile = parseString(options);
			break;
			
		case 9: //-dbg --debug-mode
			debugMode = true;
			break;
			
		case 10: //-qm --quantization-mode
			quantizationMode = parseInteger (options);
			if(quantizationMode < 0 || quantizationMode > 1) {
	                throw new ParameterException("quantization mode" + 
	                " must be: \n" + 
	                "0.- Fixed Quantization Step\n" + 
	                "1.- Rate Allocation\n");
	        }
			break;
			
		case 11: //-q --quantizer
			quantizer = parseInteger (options);
			if(quantizer < 0 || quantizer > 9) {
	                throw new ParameterException("the quantizer used" + 
	                " must be: \n" + 
	                "0.- UTQ_URQ \n "
	              + "1.- UTQDZ_NURQ \n"
	              + "2.- UTQDZ_URQ \n"
	              + "3.- URURQ \n"
	              + "4.- OptimalUTQDZ \n"
	              + "5.- UTQDZ_SingleOffset \n"
	              + "6.- URQMAX (M-CALIC QUANTIZER) \n"
	              + "7.- URQ \n"
	              + "8.- 2 Step Deadzone Quantizer \n"
	              + "9.- Deadzone Quantizer");
	        }
			break;	
			
		case 12: //-qs --quantization-step
			//quantizationStep = parseInteger(options);
			quantizationSteps = parseIntegerArray(options);
			break;
			
		case 13:// -wm
			windowsize = parseInteger(options);
			break;
			
		case 14: //-tr --target-rate
			targetRate = parseFloatPositive(options);
			break;
		case 15: //-ss --segment-size
			segmentSize = parseInteger(options);
			break;
			
		// ----- New case added. We now can choose the type of encoder
		case 16: //-ec --encoder
			encoderType = parseInteger(options);
			if(encoderType < 0 || encoderType > 8) {
	                throw new ParameterException("the type of the encoder used" + 
	                " must be: \n" + 
	                "0.- Entropy Integer Coder \n "+ 
	                "1.- Block Adaptative Coder \n "+
	                "2.- Arithmetic Coder with codewords of fixed length \n "+
	                "3.- Interleaved Entropy Coder. DECODER IS NOT IMPLEMENTED!!! \n "+
	                "5.- Computes the binary entropy. Decoder side is not useful. This is only for research analitic purposes. \n"+
	                "6.- Arithmetic Integer Coder. \n"+
	                "7.- Symbol Arithmetic Encoder.");
	        }
			break;
			
		case 17: //-cm --context-model
			contextModel = parseInteger(options);
			/*if(contextModel < 0 || contextModel > 3) {
	                throw new ParameterException("the type of the encoder used" + 
	                " must be: \n" + 
	                "0.- Entropy Integer Coder \n 1.- Block Adaptative Coder \n 2.- Arithmetic Coder \n 3.- Arithmetic Coder with codewords of fixed length ");
	        }*/
			break;	
			
		case 18: //-pm
			probabilityModel = parseInteger(options);
			break;
		
		case 19: //-wp
			encoderWP = parseInteger(options);
			if(Integer.SIZE != Integer.numberOfLeadingZeros(encoderWP) + Integer.numberOfTrailingZeros(encoderWP) + 1){
				 throw new ParameterException("encoderWP must be of the form 2^X");
			}
			break;
			
		case 20: //-up
			encoderUP = parseInteger(options);
			if(Integer.SIZE != Integer.numberOfLeadingZeros(encoderUP) + Integer.numberOfTrailingZeros(encoderUP) + 1){
				 throw new ParameterException("encoderUP must be of the form 2^X");
			}
			break;
			
		case 21: //-qlut
			quantizerProbabilityLUT = parseInteger(options);
			break;
		
		case 22: //-rcs --rate-control-stratgey
			RCStrategy = parseInteger(options);
			break;

		case 23: //-rcs
			samplePrediction = parseInteger(options);
			break;
			
		case 24: //-bf
			bufferSize = parseInteger(options);
			break;
			
		case 25: //-mk  --maskFile
			maskFile = parseString(options);
			break;
		
    		case 26: //-h  --help
			System.out.println("Emporda");
			showArgsInfo();
			System.exit(0);
			break;
		default:
			throw new ParameterException("Unknown Parameter");
		}

	}


	/**
	 * get action.
	 * 
	 * @return action tells if compression or decompression must be done
	 */
	public int getAction() {

		return action;
	}

	/**
	 * get input file.
	 *
	 * @return the input file, from where the data will be read
	 */
	public String getInputFile() {

		return inputFile;
	}

	/**
	 * get input mask.
	 *
	 * @return the input file, from where the data will be read
	 */
	public String getInputMaskFile() {

		return maskFile;
	}
	
	/** 
	 * get output file.
	 *
	 * @return the output file, where the generated data will be saved
	 */
	public String getOutputFile() {

		return outputFile;
	}

	/**
	 * get the image geometry.
	 * 
	 * @return an array with the image geometry information
	 */
	public int[] getImageGeometry() {

		try {

			if ((imageGeometry[CONS.BANDS] <= 0) || (imageGeometry[CONS.HEIGHT] <= 0)
					|| (imageGeometry[CONS.WIDTH] <= 0)) {

				throw new WarningException("Image dimensions in \".raw\" or \".img\" data files"
						+ " must be positive (\"-h\" displays help).");
			}

			if ((imageGeometry[CONS.TYPE] < 0) || (imageGeometry[CONS.TYPE] > 7)) {

				throw new WarningException("Image type in \".raw\" or \".img\" data must be between"
						+ "0 to 7 (\"-h\" displays help).");
			}   

			if ((imageGeometry[CONS.ENDIANESS] != 0) && (imageGeometry[CONS.ENDIANESS] != 1)) {

				throw new WarningException("Image byte order  in \".raw\" or \".img\" data must be 0"
						+ "or 1 (\"-h\" displays help).");
			}  

			if ((imageGeometry[CONS.RGB] != 0) && (imageGeometry[CONS.RGB] != 1)) {

				throw new WarningException("Image RGB specification in \".raw\" or \".img\" data must "
						+ "be between 0 or 1 (\"-h\" displays help).");
			}   

		} catch (WarningException e) {

			e.printStackTrace();
		}

		return imageGeometry;
	}

	/**
	 * get sampleOrder.
	 *
	 * @return the sample order of the image.
	 */
	public int getSampleOrder() {

		try {
			if (sampleOrder < 0 || sampleOrder > 2) {

				throw new WarningException("Sample Order must be 0, 1 or 2");
			}
		} catch (WarningException e) {

			e.printStackTrace();
		}

		return sampleOrder;
	}

	/**
	 * get endianess.
	 *
	 * @return 0 if image must be saved in BIG_ENDIAN byte order, 
	 * 1 if LITTLE_ENDIAN.
	 */
	public int getEndianess() {

		return endianess;
	}

	/**
	 * get verbose.
	 *
	 * @return true if output information must be given, false otherwise
	 */
	public boolean getVerbose() {

		return verbose;
	}

	/**
	 * get optionFile.
	 *
	 * @return the file where can be defines parameters for the algorithms
	 */
	public String getOptionFile() {

		return optionFile;
	}
	/**
	 * get debugMode.
	 *
	 * @return the value which indicates if debug mode is used
	 */
	public boolean getDebugMode() {
		return debugMode;
	}
	/**
	 * get quantizationStep.
	 * 
	 * @return the quantization step integer value
	 */
	public int getQuantizationStep() {
		return quantizationStep;
	}
	
	/**
	 * get quantizationStep.
	 * 
	 * @return the quantization step integer value
	 */
	public int[] getQuantizationSteps() {
		return quantizationSteps;
	}
	
	/**
	 * get quantizationMode.
	 * 
	 * @return the quantization mode integer value
	 */
	public int getQuantizationMode() {
		try {
			if (quantizationMode < 0 || quantizationMode > 1) {

				throw new WarningException("Quantization mode must be 0 or 1");
			}
		} catch (WarningException e) {

			e.printStackTrace();
		}
		return quantizationMode;
	}
	/**
	 * get quantizer.
	 * 
	 * @return the quantizer integer value
	 */
	public int getQuantizer() {
		try {
			if (quantizer < 0 || quantizer > 10) {

				throw new WarningException("Quantizer must be 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10");
			}
		} catch (WarningException e) {

			e.printStackTrace();
		}
		
		return quantizer;
	}
	/**
	 * get targetRate.
	 * 
	 * @return the target rate integer?? value
	 */
	public float getTargetRate() {
		
		try {
			if (quantizationMode == 1 && targetRate == 0) {
				throw new WarningException("A target rate must be selected");
			}
		} catch (WarningException e) {

			e.printStackTrace();
		}
		
		return targetRate;
	}
	/**
	 * get refinedQuantizationStep.
	 * 
	 * @return the refined quantization step integer?? value
	 */
	
	public int getSegmentSize() {
		
		
		return segmentSize;
	}
	/**
	 * get EncoderType.
	 * 
	 * @return the Encoder type integer value
	 */
	public int getEncoderType() {
		return encoderType;
	}
	
	/**
	 * get ContextModel.
	 * 
	 * @return the ContextModel type integer value
	 */
	public int getContextModel() {
	
		return contextModel;
	}
	
	/**
	 * get ProbabilityModel.
	 * 
	 * @return the ProbabilityModel type integer value
	 */
	public int getProbabilityModel() {
		
		return probabilityModel;
	}
	
	/**
	 * get window Probability update.
	 * 
	 * @return the encoderWP type integer value
	 */
	public int getEncoderWP(){
		return encoderWP;
	}
	
	/**
	 * get update probability.
	 * 
	 * @return the encoderUP type integer value
	 */
	public int getEncoderUP(){
		return encoderUP;
	}
	
	/**
	 * get QuantizerProbabilityLUT.
	 * 
	 * @return the QuantizerProbabilityLUT type integer value
	 */
	public int getQuantizerProbabilityLUT() {
		
		return quantizerProbabilityLUT;
	}
	
	/**
	 * get RateControl strategy.
	 * 
	 * @return the RCStrategy type int value
	 */
	public int getRCStrategy() {
		
		return RCStrategy;
	}
	
	/**
	 * get AC_option
	 * 
	 * @return the way prob tables will be created
	 */
	public int getAC_option() {
		
		return AC_option; 
	}
	
	/**
	 * get window size used duign the 2 step deadzone quantizers
	 * @return windowsize
	 */
	public int getWindowSize(){
		return windowsize;
	}
	
	/**
	 * get the type of spatial sample prediction performed
	 * @return samplePrediction
	 */
	public int getSamplePrediction(){
		return(samplePrediction);
	}
	
	public int getBufferSize(){
		return(bufferSize);
	}
}

