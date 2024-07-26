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

import java.util.*;
import java.io.*;

import GiciException.ParameterException;

/**
 * Parameters parser for EMPORDA application. It reads a file with the options set,
 * and gives the parameters to the classes that need them.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public class Parameters implements Serializable{

	/** 
	 * Used to save all options set in an optionsFile.
	 */
	private Properties property;

	/**
	 * Geometry of the image.
	 */
	private int[] imageGeometry = null;
    
	/**
	 * These variables represents general information about the compression process
	 * of the image and how it will be coded or it has been decoded 
	 */
    public int dynamicRange;
    public int sampleEncodingOrder;
    public int subframeInterleavingDepth;
    public int outputWordSize;
    public int entropyCoderType;
    public int AC_option;
   

    //public static final int predictorMetadataFlag;      //deprecated
    //public static final int entropyCoderMetadataFlag;   //deprecated

    /**
     * These variables represents information specifically used by the predictor
     * in the compression process.
     */
    public int numberPredictionBands;
    public int predictionMode;
    public int localSumMode;
    public int registerSize;
    public int weightComponentResolution;
    public int tinc;
    public int vmin;
    public int vmax;
    public int weightInitMethod;
    public int weightInitTableFlag;
    public int weightInitResolution;
    private int[][] weightInitTable = null;

    /**
     * These variables represents information specifically used by the sample entropy
     * coder in the coding process.
     */
    public int unaryLengthLimit;
    public int rescalingCounterSize;
    public int initialCountExponent;
    public int accInitConstant;
    public int accInitTableFlag;
    private int[] accInitTable = null;
    // This variable won't be written in coderHeader as is only needed by the coder.
    public int winSize = 0;

    /**
     * These variables represents information specifically used by the block entropy
     * coder in the coding process.
     */
    public int referenceSampleInterval;
    public int blockSize;

	/* Constants used to check the options */
    
    /**
     * Options relative to the image and how it will be handled by the program
     */
	private static final String DYNAMIC_RANGE = "DYNAMIC_RANGE";
	private static final String SAMPLE_ENCODING_ORDER = "SAMPLE_ENCODING_ORDER";
	private static final String SUBFRAME_INTERLEAVING_DEPTH = "SUBFRAME_INTERLEAVING_DEPTH";
	private static final String OUTPUT_WORD_SIZE = "OUTPUT_WORD_SIZE";
	private static final String ENTROPY_CODER_TYPE = "ENTROPY_CODER_TYPE";
	private static final String PROB_TABLES_CREATION = "PROB_TABLES_CREATION";
	
// TODO: New variable to control decoder must be created. Possible values of the variable must be checked as well as "ENTROPY_CODER_TYPE"

	/* these options are deprecated */
//	private static final String PREDICTOR_METADATA_FLAG = "PREDICTOR_METADATA_FLAG";
//	private static final String ENTROPYCODER_METADATA_FLAG = "ENTROPYCODER_METADATA_FLAG";

	/**
	 * Specific option of the predictor used in the compression process
	 */
	private static final String NUMBER_PREDICTION_BANDS = "NUMBER_PREDICTION_BANDS";
	private static final String PREDICTION_MODE = "PREDICTION_MODE";
	private static final String LOCAL_SUM_MODE = "LOCAL_SUM_MODE";
	private static final String REGISTER_SIZE = "REGISTER_SIZE";
	private static final String WEIGHT_COMPONENT_RESOLUTION = "WEIGHT_COMPONENT_RESOLUTION";
	private static final String WEIGHT_UPDATE_SECI = "WEIGHT_UPDATE_SECI";
	private static final String WEIGHT_UPDATE_SE = "WEIGHT_UPDATE_SE";
	private static final String WEIGHT_UPDATE_SEFP = "WEIGHT_UPDATE_SEFP";
	private static final String WEIGHT_INITIALIZATION_METHOD = "WEIGHT_INITIALIZATION_METHOD";
	private static final String WEIGHT_INITIALIZATION_TF = "WEIGHT_INITIALIZATION_TF";
	private static final String WEIGHT_INITIALIZATION_RESOLUTION = "WEIGHT_INITIALIZATION_RESOLUTION";
	private static final String WEIGHT_INITIALIZATION_TABLE = "WEIGHT_INITIALIZATION_TABLE";


	/**
	 * Specific options of the sample entropy coder used in the coding process
	 */
	private static final String UNARY_LENGTH_LIMIT = "UNARY_LENGTH_LIMIT";
	private static final String RESCALING_COUNTER_SIZE = "RESCALING_COUNTER_SIZE";
	private static final String INITIAL_COUNT_EXPONENT = "INITIAL_COUNT_EXPONENT";
	private static final String ACCUMULATOR_INITIALIZATION_TF = "ACCUMULATOR_INITIALIZATION_TF";
	private static final String ACCUMULATOR_INITIALIZATION_CONSTANT = "ACCUMULATOR_INITIALIZATION_CONSTANT";
	private static final String ACCUMULATOR_INITIALIZATION_TABLE = "ACCUMULATOR_INITIALIZATION_TABLE";
	private static final String WINDOW_SIZE = "WINDOW_SIZE"; 

	/**
	 * Specific options of the block entropy coder used in the coding process.
	 */
	private static final String REFERENCE_SAMPLE_INTERVAL = "REFERENCE_SAMPLE_INTERVAL";
	private static final String BLOCK_SIZE = "BLOCK_SIZE";

	/**
	 * Set used to save all options set by the user in an optionsFile.
	 */
	private Set<String> infoSet = null;

	/**
	 * Constructor of paramParser.
	 * It receives the name of the file where the parameters can be found,
	 * and the geometry of the image.
	 *
	 * @param file the file where the parameters are set
	 * @param imageGeometry the geometry of the image
	 * @param generateAll indicates if the variables of an instance of this class must
	 * be set by reading the optionsFile or not.
	 * @throws IOException when something goes wrong and read must be stopped
	 */
	public Parameters(String file, int[] imageGeometry, boolean generateAll, boolean debugMode, int encoderType, int PT_options) throws IOException {

		
		this.imageGeometry = imageGeometry;
		property = new Properties();

		if(file != null) {
    		property.load(new FileInputStream(file));
//    		TODO: Generate all = false?
        }
	    init();
        if(file != null && debugMode) {
            debugInfo();
        }
    	try {
    		if(generateAll) {
    			// parameter added to control the encoderType
    			generateVariables(encoderType, PT_options);
    		}
    		
		} catch (ParameterException e) {
			e.printStackTrace();
			System.exit(-1);
		}
        
	}

	/**
	 * This function is used in debug tasks to check which parameters have been set in an options file
	 * and with which values. It only outputs this information.
	 */
	public void debugInfo() {
		
		Iterator<String> i = infoSet.iterator();
		while(i.hasNext()) {
			String str = i.next();
			String p = property.getProperty(str, null);
			if (p != null) {
				System.err.println("ParamParser: read " + str + " with value " + p);
			} else {
				System.err.println("ParamParser: option " + str + " not set");
			}
		}
	}
	
	/**
	 * Does necessary initializations.
	 * 
	 * @param imageGeometry the geometry of the image
	 * @throws IOException when something goes wrong and read must be stopped
	 */
	private void init() throws IOException {
		
		fillSet();
		checkFile();
	}

	/**
	 * Generates the values for all the variables, using first the values set by the user, 
	 * and if a variable is not set, this function gives a default value to it.
	 * 
	 * @throws ParameterException thrown if a variable is set with an invalid value.
	 */
	private void generateVariables(int encoderType, int PT_options) throws ParameterException {
		generateImageInformation(encoderType, PT_options);
		generatePredictorVariables();
		generateEntropyVariables();
	}
	/**
	 * Checks that the file is correct, and it does not contain invalid options.
	 *
	 * @throws IOException when something goes wrong and read must be stopped
	 */
	private void checkFile() throws IOException {
		if(!infoSet.containsAll(property.stringPropertyNames())) {
			throw new IOException("Error reading option file: this file contains " +
			"illegal parameters");
		}
	}

	/**
	 * Fills the HashSet infoset with all the possible options of the program.
	 */
	private void fillSet()  {
		infoSet = new HashSet<String>();
		infoSet.add(DYNAMIC_RANGE);
		infoSet.add(SAMPLE_ENCODING_ORDER);
		infoSet.add(SUBFRAME_INTERLEAVING_DEPTH);
		infoSet.add(OUTPUT_WORD_SIZE);

		infoSet.add(ENTROPY_CODER_TYPE);
		infoSet.add(PROB_TABLES_CREATION);

		//		deprecated options 
		//
		//		infoSet.add(PREDICTOR_METADATA_FLAG);
		//		infoSet.add(ENTROPYCODER_METADATA_FLAG);        
		
        infoSet.add(NUMBER_PREDICTION_BANDS);
		infoSet.add(PREDICTION_MODE);
		infoSet.add(LOCAL_SUM_MODE);
		infoSet.add(REGISTER_SIZE);

		infoSet.add(WEIGHT_COMPONENT_RESOLUTION);
		infoSet.add(WEIGHT_UPDATE_SECI);
		infoSet.add(WEIGHT_UPDATE_SE);
		infoSet.add(WEIGHT_UPDATE_SEFP);
		infoSet.add(WEIGHT_INITIALIZATION_METHOD);
		infoSet.add(WEIGHT_INITIALIZATION_TF);
		infoSet.add(WEIGHT_INITIALIZATION_RESOLUTION);

		infoSet.add(WEIGHT_INITIALIZATION_TABLE);

		infoSet.add(UNARY_LENGTH_LIMIT);
		infoSet.add(RESCALING_COUNTER_SIZE);
		infoSet.add(INITIAL_COUNT_EXPONENT);
		infoSet.add(ACCUMULATOR_INITIALIZATION_TF);
		infoSet.add(ACCUMULATOR_INITIALIZATION_CONSTANT);
		infoSet.add(REFERENCE_SAMPLE_INTERVAL);
		infoSet.add(BLOCK_SIZE);

		infoSet.add(ACCUMULATOR_INITIALIZATION_TABLE);

	}

	/** 
	 * Generates the variables relative to image information, 
	 * reading the parameters file, and setting the values that are
	 * not in that file to default. It checks too that all values
	 * are correctly set
	 *
	 * @throws ParameterException when an invalid parameter is detected
	 */
//	Added new parameter to control the encoder type
	private void generateImageInformation(int encoderType, int PT_options) throws ParameterException {

		dynamicRange = Integer.parseInt(property.getProperty(
				DYNAMIC_RANGE, "16"));
		sampleEncodingOrder = Integer.parseInt(property.getProperty(
				SAMPLE_ENCODING_ORDER, "1")); 
		subframeInterleavingDepth = Integer.parseInt(property.getProperty(
				SUBFRAME_INTERLEAVING_DEPTH, "0")); 
		outputWordSize = Integer.parseInt(property.getProperty(
				OUTPUT_WORD_SIZE, "4"));
		entropyCoderType = Integer.parseInt(property.getProperty(
				ENTROPY_CODER_TYPE, Integer.toString(encoderType)));
        AC_option = Integer.parseInt(property.getProperty(
        		PROB_TABLES_CREATION, Integer.toString(PT_options)));
        
//      Deprecated options
//  
//		imageFlags[CONS.PMF] = Integer.parseInt(property.getProperty(
//				PREDICTOR_METADATA_FLAG, "0"));
//		imageFlags[CONS.ECMF] =Integer.parseInt(property.getProperty(
//				ENTROPYCODER_METADATA_FLAG, "0"));

		/* check that options are correct */

		
		/* check that the options are correct */
		if (dynamicRange < 2 || dynamicRange > 16) {
			throw new ParameterException("PARAMS ERROR: DYNAMIC_RANGE must be "
					+ "in range 2 <= D <= 16");
		}
		if (sampleEncodingOrder < 0 || sampleEncodingOrder > 1) {
			throw new ParameterException("PARAMS ERROR: SAMPLE_ENCODING_ORDER must be" 
					+ " 1 for BSQ encoding, or\n " 
					+ " 0 if it uses BI coding");
		}
		//FIXME
		//if ((subframeInterleavingDepth != 0 && sampleEncodingOrder == 1) || (subframeInterleavingDepth < 1 || subframeInterleavingDepth > imageGeometry[CONS.BANDS]) && sampleEncodingOrder == 0 )   this is the correct if, however imageGeometry is null since is not initialized before.
		if ((subframeInterleavingDepth != 0 && sampleEncodingOrder == 1)
				|| (subframeInterleavingDepth < 1)
				&& sampleEncodingOrder == 0 ) {
			throw new ParameterException("PARAMS ERROR: SUBFRAME_INTERLEAVING_DEPTH must" 
					+ " be 0, for BSQ encoding, or\n "
					+ "a value between 1 and image bands for BI coding");
		}
		if (outputWordSize < 1 || outputWordSize > 8) {
			throw new ParameterException("PARAMS ERROR: OUTPUT_WORD_SIZE must"
					+ " be in range 1 <= B <= 8");
		}
		if (entropyCoderType < 0 || entropyCoderType > 7) {
			throw new ParameterException("PARAMS ERROR: ENTROPY_CODER_TYPE must be\n"
					+ "\t 0 -> Entropy Integer Coder.\n"
					+ "\t 1 -> Block Adaptative Coder.\n"
					+ "\t 2 -> Arithmetic coder with codewords of fixed length.\n"
					+ "\t 3 -> Interleaved Entropy Coder. DECODER IS NOT IMPLEMENTED!!!\n"
					+ "\t 4 -> MQ\n"
					+ "\t 5 -> Computes the binary entropy. Decoder side is not useful. This is only for research analitic purposes.\n"
					+ "\t 6 -> Dual Arithmetic coder with codewords of fixed length.\n"
					+ "\t 7 -> Symbol Arithmetic coder with codewords of fixed length.\n");
		}
               
        /*
        Conditions for the deprecated options
   
        if (imageFlags[CONS.PMF] != 0) {
			throw new ParameterException("PARAMS ERROR: PREDICTOR_METADATA_FLAG \n"
					+ "is a reserved bit, because predictor metada header is mandatory \n "
					+ "so it must be set to 0");
		}
		if (imageFlags[CONS.ECMF] != 0) {
			throw new ParameterException("PARAMS ERROR: ENTROPYCODER_METADATA_FLAG \n"
					+ "is a reserved bit, because predictor metadata header is mandatory \n "
					+ "so it must be set to 0");
		}
*/

	}

	/** 
	 * Generates the options relative to the predictor, 
	 * reading the parameters file, and setting the values that are
	 * not in that file to default. It checks too that all values
	 * are correctly set
	 *
	 * @throws ParameterException when an invalid parameter is detected
	 */	 
	private void generatePredictorVariables() throws ParameterException{

		numberPredictionBands = Integer.parseInt(property.getProperty(
				NUMBER_PREDICTION_BANDS, "15"));
		predictionMode = Integer.parseInt(property.getProperty(
				PREDICTION_MODE, "0"));
		localSumMode = Integer.parseInt(property.getProperty(
				LOCAL_SUM_MODE, "0"));
		registerSize = Integer.parseInt(property.getProperty(
				REGISTER_SIZE, "32"));
        weightComponentResolution= Integer.parseInt(property.getProperty(
				WEIGHT_COMPONENT_RESOLUTION, "13"));
		tinc = Integer.parseInt(property.getProperty(
				WEIGHT_UPDATE_SECI, "6"));
		vmin = Integer.parseInt(property.getProperty(
				WEIGHT_UPDATE_SE, "-1"));
		vmax = Integer.parseInt(property.getProperty(
				WEIGHT_UPDATE_SEFP, "3"));
		weightInitMethod = Integer.parseInt(property.getProperty(
				WEIGHT_INITIALIZATION_METHOD, "0"));
		weightInitTableFlag = Integer.parseInt(property.getProperty(
				WEIGHT_INITIALIZATION_TF, "0"));
		weightInitResolution = Integer.parseInt(property.getProperty(
				WEIGHT_INITIALIZATION_RESOLUTION, "0"));



		/* check if options are correct */

		if (numberPredictionBands < 0 || numberPredictionBands > 15) {
			throw new ParameterException("PARAMS ERROR: NUMBER_PREDICTION_BANDS must"
					+ " be in range 0 <= P <= 15");
		}
		if (predictionMode < 0 || predictionMode > 1) {
			throw new ParameterException("PARAMS ERROR: PREDICTION_MODE must be \n"
					+ "\t 0 -> if full prediction mode is used\n"
					+ "\t 1 -> if reduced prediction mode is used");
		}
		if (localSumMode < 0 || localSumMode > 1) {
			throw new ParameterException("PARAMS ERROR: LOCAL_SUM_MODE must be \n"
					+ "\t 0 -> if neighbor oriented mode is used\n"
					+ "\t 1 -> if columnt oriented mode is used");
		}
		if (registerSize < 32 || registerSize > 64) {
			throw new ParameterException("PARAMS ERROR: REGISTER_SIZE must be "
					+ "in range 32 <= R <= 64");
		}
		
		if (weightComponentResolution < 4 || weightComponentResolution > 19 ) {
			throw new ParameterException("PARAMS ERROR: WEIGHT_COMPONENT_RESOULTION"
					+ "must be in range 4 <= omega <= 19");
		}
		if (tinc < 4 || tinc > 11) {
			throw new ParameterException("PARAMS ERROR: WEIGHT_UPDATE_SECI must be an "
					+ "integer in range 4 <= WEIGHT_UPDATE_SECI <= 11");
		}
		if (vmin > vmax || vmin < -6 
				|| vmax > 9) {
			throw new ParameterException("PARAMS ERROR: WEIGHT_UPDATE_SE and WEIGHT_UPDATE_SEFP"
					+ "must be in range -6 <= WEIGHT_UPDATE_SE <= 9, and "
					+ "WEIGHT_UPDATE_SE <= WEIGHT_UPDATE_SEFP");
		}
		if (weightInitMethod < 0 || weightInitMethod > 1) {
			throw new ParameterException("PARAMS ERROR: WEIGHT_INITIALIZATION_METHOD must be \n" 
					+ "\t 0 -> default initialization used\n" 
					+ "\t 1 -> custom initialization used");
		}
		if (weightInitTableFlag < 0 || weightInitTableFlag > 1) {
			throw new ParameterException("PARAMS ERROR: WEIGHT_INITIALIZAION_TF must be \n"
					+ "\t 0 -> table not included in predictor metadata\n"
					+ "\t 1 -> table included in predictor metadata");
		}
	/*	
	 * This condition is not needed, because it's not mandatory to have the weight initialization table
	 * in image header.
	 * 
	 * if (weightInitMethod == 1 && weightInitTableFlag == 0) {
			throw new ParameterException("PARAMS ERROR: for custom weight initialization, "
					+ "WEIGHT_INITIALIZATION_TF must be 1, and WEIGHT_INITIALIZATION_TABLE "
					+ "must be set");
		} */
		if (weightInitMethod == 0 && weightInitTableFlag == 1) {
			throw new ParameterException("PARAMS ERROR: WEIGHT_INITIALIZATION_TF must be 0 " 
					+ "if WEIGH_INITIALIZATION_METHOD is default (= 0)");
		}
		if (weightInitMethod == 0 && weightInitResolution != 0) {
			throw new ParameterException("PARAMS ERROR: WEIGHT_INITIALIZATION_RESOLUTION must be 0 if "
					+ "WEIGHT_INITIALIZATION_METHOD is 0");
		}
		if (weightInitMethod == 1 && (weightInitResolution < 3 
				|| weightInitResolution > weightComponentResolution + 3)) {
			throw new ParameterException("PARAMS ERROR: WEIGHT_INITIALIZATION_RESOLUTION must be in range " 
					+ "3 <= Q <= omega + 3 for custom weight initialization");
		}
		if (registerSize < dynamicRange + weightComponentResolution + 2) {
			throw new ParameterException("PARAMS ERROR: REGISTER_SIZE must be "
					+ "at least DYNAMIC_RANGE + WEIGHT_COMPONENT_RESOLUTION + 2");
		}

	}

	/** 
	 * Generates the options relative to the entropy coders, 
	 * reading the parameters file, and setting the values that are
	 * not in that file to default. It checks too that all values
	 * are correctly set
	 * 
	 * @throws ParameterException when an invalid parameter is detected
	 */	 
	private void generateEntropyVariables() throws ParameterException {

		unaryLengthLimit = Integer.parseInt(property.getProperty(
				UNARY_LENGTH_LIMIT, "16"));
		rescalingCounterSize = Integer.parseInt(property.getProperty(
				RESCALING_COUNTER_SIZE, "6"));
		initialCountExponent = Integer.parseInt(property.getProperty(
				INITIAL_COUNT_EXPONENT, "1"));
		accInitTableFlag = Integer.parseInt(property.getProperty(
				ACCUMULATOR_INITIALIZATION_TF, "0"));
		accInitConstant = Integer.parseInt(property.getProperty(
				ACCUMULATOR_INITIALIZATION_CONSTANT, "5"));
		blockSize = Integer.parseInt(property.getProperty(
				BLOCK_SIZE, "16"));
		referenceSampleInterval = Integer.parseInt(property.getProperty(
				REFERENCE_SAMPLE_INTERVAL, "1"));
		
		if (unaryLengthLimit < 8 || unaryLengthLimit > 32) {
			throw new ParameterException("PARAMS ERROR: UNARY_LENGTH_LIMIT must be between 8 and 32, 8 <= Umax <= 32");
		}
		if (rescalingCounterSize < 4 || rescalingCounterSize > 9) {
			throw new ParameterException("PARAMS ERROR: RESCALING_COUNTER_SIZE must "
					+ "be in range 4 <= gamma* <= 9");
		}
		if (initialCountExponent < 1 || initialCountExponent > 8) {
			throw new ParameterException("PARAMS ERROR: INITIAL_COUNT_EXPONENT "
					+ "must be in range 1 <= gamma0 <= 8");
		}
		if (rescalingCounterSize <= initialCountExponent) {
			throw new ParameterException("PARAMS ERROR: INITIAL_COUNT_EXPONENT "
					+ "must be in range 1 <= gamma0 < RESCALING_COUNTER_SIZE");
		}
		if (accInitTableFlag < 0 || accInitTableFlag > 1) {
			throw new ParameterException("PARAMS ERROR: ACCUMULATOR_INITIALIZATION_TF must be\n"
					+ "\t 0 -> accumulator initialization table not included\n"
					+ "\t 1 -> accumulator initialization table included");
		}
		if (accInitConstant < 0 || (accInitConstant > dynamicRange - 2 && accInitConstant != 15)) {
			throw new ParameterException("PARAMS ERROR: ACCUMULATOR_INITIALIZATION_CONSTANT must be"
					+ " in range 0 <= K <= D - 2");
		}
		if (blockSize != 8 && blockSize != 16) {
			throw new ParameterException("PARAMS ERROR: BLOCK_SIZE must be 8 or 16");
		}
		if (referenceSampleInterval < 1 || referenceSampleInterval > 256) {
			throw new ParameterException("PARAMS ERROR: REFERENCE_SAMPLE_INTERVAL must"
					+ "be in range 1 <= r <= 256");
		}
		if (accInitConstant != 15 && accInitTableFlag == 1 ) {
			throw new ParameterException("PARAMS ERROR: ACCUMULATOR_INITIALIZATION_TF must be 0 " 
					+ "if ACCUMULTATOR_INITIALIZATION_CONSTANT is not 15.");
		}
	}

	/** 
	 * Generates the initialization table,
	 * used in the weight initialization step of the compression algorithm,
	 * reading the parameters file, and setting the values that are
	 * not in that file to default. It checks too that all values
	 * are correctly set
	 * 
	 * @throws ParameterException when an invalid parameter is detected
	 */	 
	private void generateWeightInitTable() throws ParameterException{

		String matrix = property.getProperty(WEIGHT_INITIALIZATION_TABLE, null);
		if (matrix == null) {
			System.out.println("Matrix is null");
			weightInitTable = null;
	
		} else {
			weightInitTable = new int[imageGeometry[CONS.BANDS]][];
			StringTokenizer tokens = new StringTokenizer(matrix, "[], ");
			int predictionModeBands = 3 * (1 - predictionMode);
			int i = 0;
			int length = 0;
			int minValue = -1 << weightInitResolution - 1;
			int maxValue = (1 << weightInitResolution - 1) - 1;

			for ( ; i < imageGeometry[CONS.BANDS] && tokens.hasMoreTokens(); i++) {
				length = Math.min(i, numberPredictionBands) + predictionModeBands;
				if (length > 0) {
					weightInitTable[i] = new int[length];
				} else {
					weightInitTable[i] = new int[1];
					weightInitTable[i][0] = 0;
				}
				int j = 0;

				for ( ; j < length && tokens.hasMoreTokens(); j++) {
					weightInitTable[i][j] = Integer.parseInt(tokens.nextToken());
					if (weightInitTable[i][j] < minValue || weightInitTable[i][j] > maxValue)  {
						throw new ParameterException("PARAMS ERROR: INITIALIZATION_TABLE ERROR, "
								+ "every value of the table must be"
								+ "in range (-1 << WEIGHT_INITIALIZATION_RESOLUTION)"
								+ " - 1 < VALUE < (1 << "
								+ " WEIGHT_INITIALIZATION_RESOLUTION - 1) - 1");
					}
				}
				if (j < length) {
					throw new ParameterException("PARAMS ERROR: WEIGHT_INITIALIZATION_TABLE "
							+ "has " + j + " elements in its band " + i
							+ " and needs " + length);
				}
			}
			if (i < imageGeometry[CONS.BANDS]) {
				throw new ParameterException("PARAMS ERROR: WEIGHT_INITIALIZATION_TABLE has "
						+ i + " elements for its "
						+ imageGeometry[CONS.BANDS] + " so it is missing lines ");
			}
			if (tokens.hasMoreTokens()) {
				throw new ParameterException("PARAMS WARNING: WEIGHT_INITIALIZATION_TABLE has "
						+ "more elements, than it actually needs");
			}
		}
	}

	/** 
	 * Generates the accumulator table,
	 * used in the entropy coder algorithm, reading the parameters file, 
	 * and setting the values that are not in that file to default. It 
	 * checks too that all values are correctly set
	 * 
	 * @throws ParameterException when an invalid parameter is detected
	 */	 
	private void generateAccInitTable() throws ParameterException {

		String table = property.getProperty(ACCUMULATOR_INITIALIZATION_TABLE, null);
		if (table == null) {
			accInitTable = null;
			
		} else {
			StringTokenizer tokens =  new StringTokenizer(table, "[], ");
			accInitTable = new int[imageGeometry[CONS.BANDS]];
			int i = 0, maxValue = dynamicRange - 2;

			for (; i < imageGeometry[CONS.BANDS] && tokens.hasMoreTokens(); i++) {
				accInitTable[i] = Integer.parseInt(tokens.nextToken());
				if (accInitTable[i] < 0 || accInitTable[i] > maxValue) {
					throw new ParameterException("PARAMS ERROR: ACCUMULATOR_INITIALIZATION_TABLE with value "
							+ accInitTable[i] + ", all values must be " 
							+ "in range 0 <= VALUE <= DYNAMIC_RANGE - 2");
				}
			}
			if (i < imageGeometry[CONS.BANDS]) {
				throw new ParameterException("PARAMS ERROR: ACCUMULATOR_INITIALIZATION_TABLE has "
						+ i + " elements, that are "
						+ "not enough for the number of bands of the image");
			}
			if (tokens.hasMoreTokens()) {
				throw new ParameterException("PARAMS WARNING: ACCUMULATOR_INITIALIZATION_TABLE has "
						+ "more elements than it actually needs");
			}

		}
	}
	
	/**
	 * gets weightInitTable.
	 *
	 * @return weightInitTable table with the initial values of the weights
	 * 
	 * @throws ParameterException when an invalid parameter is detected
	 */
	public int[][] getWeightInitTable() throws ParameterException {
		if(weightInitMethod == 1 && weightInitTable == null) {
			generateWeightInitTable();
			if(weightInitTable == null) {
				throw new ParameterException ("PARAMS WARNING: WEIGHT_INITIALIZATION_TABLE is not set, but " 
						+ "WEIGHT_INITIALIZATION_METHOD is set to custom (= 1)");
			}
		}
		
		return weightInitTable;
	}

	/**
	 * get accInitTable.
	 *
	 * @return accInitTable table with the initial accumulator values
	 * 
	 * @throws ParameterException when an invalid parameter is detected
	 */
	public int[] getAccInitTable() throws ParameterException {
		if(accInitConstant == 15 && accInitTable == null) {
			generateAccInitTable(); 
			if(accInitTable == null) {
				throw new ParameterException ("PARAMS WARNING: ACCUMULATOR_INITIALIZATION_TABLE is not set " 
						+ "but ACCUMULATOR_INITIALIZATION_CONSTANT has value 15");
			}
		}
	
		return accInitTable;
	}

	/**
	 * get imageGeometry.
	 *
	 * @return imageGeometry geometry of the image
	 */
	public int[] getImageGeometry() {
		if(imageGeometry == null) {
			imageGeometry = new int[6];
		}
		return imageGeometry;
	}

	/**
	 * sets the initializationTable array.
	 *
	 * @param weightInitTable table with the initial values of the weights
	 */
	public void setWeightInitTable(int[][] weightInitTable) {
		this.weightInitTable = weightInitTable;
	}   

	/**
	 * sets the accInitTable array.
	 *
	 * @param accInitTable table with the initial accumulator values
	 */
	public void setAccInitTable(int[] accInitTable) {
		this.accInitTable = accInitTable;
	}
	
	/**
	 * sets the image geometry
	 * 
	 * @param imageGeometry an array representing the geometry of the image the program is 
	 * working on
	 */
	public void setImageGeometry(int[] imageGeometry) {
		this.imageGeometry = imageGeometry;
	}
}
