package ffc;

import GiciException.ParameterException;

/**
 * Arguments parser for ffc. This class analyses a string of arguments and extract and check its validity.
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; [showArgsInfo]<br>
 * &nbsp; [get functions]<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1
 */
public class ArgsParser{

	/**
	 * Arguments specificiation. The array describes argument, explain what is used and its default parameters. First index of array is argument; second specifies:<br>
	 *   <ul>
	 *     <li> 0 - short argument specification (i.e. "-i")
	 *     <li> 1 - long argument specification (i.e. "--inputFile")
	 *     <li> 2 - parsing specification of argument ({} indicates mandatority, [] optionality)
	 *     <li> 3 - default values
	 *     <li> 4 - mandatory argument ("1") or non mandatory argument ("0")
	 *     <li> 5 - explanation
	 *   </ul>
	 * <p>
	 * String arguments.
	 */
	String[][] argsSpecification = {
		{"-h", "--help", "", "", "0",
			"Displays this help and exits program."
		},
		{"-i", "--inputImageFile", "{string}", "", "1",
			"Input image. Valid formats are: pgm, ppm, pbm, jpg, tiff, png, bmp, gif, fpx. If image is raw data file extension must be \".raw\" or \".img\" and \"-ig\" parameter is mandatory."
		},
		{"-ig", "--inputImageGeometry", "{int int int int boolean}", "", "0",
			"Geometry of input raw image data. Parameters are:\n    1- zSize (number of image components)\n    2- ySize (image height)\n    3- xSize (image width)\n    4- data type. Possible values are:\n \t 0- boolean (1 byte)\n \t 1- unsigned int (1 byte)\n \t 2- unsigned int (2 bytes)\n \t 3- signed int (2 bytes)\n \t 4- signed int (4 bytes)\n \t 5- signed int (8 bytes)\n \t 6- float (4 bytes)\n \t 7- double (8 bytes)\n    5- Byte order (0 if BIG ENDIAN, 1 if LITTLE ENDIAN)\n    6- 1 if 3 first components are RGB, 0 otherwise."
		},
		{"-o", "--outputImageFile", "{string}", "", "1",
			"Output image file name. Valid formats are: pgm, ppm, pbm, jpg, tiff, png, bmp, gif, fpx. If image is raw data file extension must be \".raw\" or \".img\" and \"-og\" parameter is mandatory."
		},
		{"-og", "--outputImageGeometry", "{int int int int boolean}", "", "0",
			"Geometry of output raw image data. Parameters are:\n    1- zSize (number of image components)\n    2- ySize (image height)\n    3- xSize (image width)\n    4- data type. Possible values are:\n \t 0- boolean (1 byte)\n \t 1- unsigned int (1 byte)\n \t 2- unsigned int (2 bytes)\n \t 3- signed int (2 bytes)\n \t 4- signed int (4 bytes)\n \t 5- signed int (8 bytes)\n \t 6- float (4 bytes)\n \t 7- double (8 bytes)\n    5- Byte order (0 if BIG ENDIAN, 1 if LITTLE ENDIAN)\n    6- 1 if 3 first components are RGB, 0 otherwise."
		},
		{"-ca", "--coefficientsApproximation", "{int[ int[ int[ ...]]]}", "0", "0",
			"This parameter specifies the approximation to be applied to coefficients of each channel. First value is for the first channel, second value for the second channel and so on. If only one value is specified, it will be the same for all channels. Valid values are:\n 0 - None  \n 1 - Round  \n 2 - Floor  \n 3 - Ceil "	
		},
		{"-mn", "--minValue", "{float[ float[ float[ ...]]]}", "null", "0",
			"This parameter specifies the mininum value that can appear in a channel. First value is for the first channel, second value for the second channel and so on. If only one value is specified, it will be the same for all channels. If no value is given, no minimum value is considered."	
		},
		{"-mx", "--maxValue", "{float[ float[ float[ ...]]]}", "null", "0",
			"This parameter specifies the maximum value that can appear in a channel. First value is for the first channel, second value for the second channel and so on. If only one value is specified, it will be the same for all channels. If no value is given, no maximum value is considered."	
		},
		{"-ltv", "--lowerThresholdValue", "{float[ float[ float[ ...]]]}", "null", "0",
			"This parameter specifies a lower threshold value such that, all other values strictly lower than this threshold --in absolute value-- will be set to zero. First threshold is for the first channel, second threshold for the second channel and so on. If only one threshold is specified, it will be the same for all channels. If no threshold value is provided, a (threshold = 0) is assumed by default."	
		},
		{"-utv", "--upperThresholdValue", "{float[ float[ float[ ...]]]}", "null", "0",
			"This parameter specifies an upper threshold value such that, all other values strictly larger than this threshold --in absolute value-- will be set to zero. First threshold is for the first channel, second threshold for the second channel and so on. If only one threshold is specified, it will be the same for all channels. If no threshold value is provided, a (threshold = 0) is assumed by default."	
		},
		{"-sv", "--subtractValue", "{float[ float[ float[ ...]]]}", "null", "0",
		"This parameter specifies the subtraction value that will be subtracted to all image pixels. First value is for the first channel, second value for the second channel and so on. If only one value is specified, it will be the same for all channels. If no value is given, no subtraction value is considered (subtract = 0)."	
		},
		{"-swl", "--spectralWaveletLevel", "{int}", "null", "0",
			"This parameter specifies the number of DWT levels to be applied in the spectral domain. If no value is given, no DWT will be applied."
		},
		{"-swt", "--spectralWaveletType", "{int}", "null", "0",
			"This parameter specifies the DWT type to be applied in the spectral domain. If no value is given, no DWT will be applied."
		},
		{"-wl", "--spatialWaveletLevel", "{int}", "null", "0",
			"This parameter specifies the number of DWT levels to be applied in the spatial domain. If no value is given, no DWT will be applied."
		},
		{"-wt", "--spatialWaveletType", "{int}", "null", "0",
			"This parameter specifies the DWT type to be applied in the spatial domain.\n"+
                        "    If no value is given, no DWT will be applied.\n"+
                        "    Accepted types (must comply with FordardDWTCore definitions) are:\n"+
                        "\t1 - Reversible 5/3 DWT\n"+
                        "\t2 - Irreversible 9/7 DWT Isorange\n"+
                        "\t3 - Irreversible 9/7 DWT Isonorm\n"+
                        "\t4 - Integer (reversible) 9/7 M (CCSDS Recommended )\n"+
                        "\t7 - Haar\n"+
                        "\t8 - Reversible Haar (S-transform)"
		},
		{"-z0", "--zInit", "{int}", "null", "0",
			"This parameter specifies the first band selected. If no value is given, the first band will be taken."
		},
		{"-zl", "--zLength", "{int}", "null", "0",
			"This parameter specifies the amount of bands selected. If no value is given, then no selection of bands will be performed."
		},
		{"-zr", "--zRanges", "{int-int [int-int [int-int [ ...]]]}", "", "0", "1",
			"This parameter specifies a range of bands selected. If no value is given, then no selection of bands will be performed.  This parameters is not compatible with the components (\"-z0\") parameter. "
		},
		{"-y0", "--yInit", "{int}", "null", "0",
			"This parameter specifies the first column selected. If no value is given, the first column will be taken."
		},
		{"-yl", "--yLength", "{int}", "null", "0",
			"This parameter specifies the amount of columns selected. If no value is given, then no selection of columns will be performed."
		},
		{"-x0", "--xInit", "{int}", "null", "0",
			"This parameter specifies the first row selected. If no value is given, the first row will be taken."
		},
		{"-xl", "--xLength", "{int}", "null", "0",
			"This parameter specifies the amount of rows selected. If no value is given, then no selection of rows will be performed."
		},
		{"-df", "--DPCM", "{int}", "null", "0",
			"This parameter specifies how DCPM is applied in the spectral domain. If no value is given, no DPCM will be applied. (See source code for more information)"
		},
		{"-di", "--dpcmIndex", "{int}", "null", "0",
			"This parameter specifies if DCPM index is restarted or the indicate reference band. If no value is given, the only reference value will be the first one. (See source code for more information)"
		},
		{"-ro", "--rotateImage", "{int}", "null", "0",
			"This parameter specifies if the image will be rotated 180 degrees."
		},
		{"-cd", "--changeDimension", "{int int int}", "null", "0",
			"This parameter specifies the permutation of the dimensions that will apply. Valid values are from 0 to 2. E.g.: \"1 0 2\" changes from BIL to BSQ and \"2 0 1\" changes from BIP to BSQ."
		},
		{"-rm", "--rangeModification", "{float[ float[ float[ ...]]]}", "null", "0",
			"This parameter specifies the amount in % that the histogram is shorted. The output components are always set to 8 bits per sample. The no-Data value is set to 0 (black) (see --noDataValues parameter)."
		},
		{"-nd", "--noDataValues", "{float[ float[ float[ ...]]]}", "No no-data values", "0", "1",
			"Pixels in the input image considered no-data values."
		},
		{"-pc", "--permuteComponents", "{int int}", "null", "0",
			"This parameter specifies the permutation of the components that will apply."
		},
		{"-pd", "--permuteDimension", "{int}", "null", "0",
			"This parameter specifies if the image will be permuted and in which direction."
		},
		{"-ps", "--permutationSequence", "{int[ int[ int[ ...]]]}", "null", "0",
			"This parameter specifies the permutation applied if --permuteDimension is used (e.g. 0 2 1 3 swaps the middle components on a four component dimension)."
		},
		{"-nnc", "--nonNullCoefficients", "{int}", "null", "0",
			"This parameter indicates the number of pixels in the input image that are different from zero(0). An output image is produced that contains only the original non-null pixels (i.e., all null pixels have been removed). The geometry of the output image can be thought of as a 1-dimensional array, where all non-null coefficients appear sequentially in a single row, or appropriate spatial dimensions can be passed as arguments. This routine is programmed only for application to a single component."
		},
		{"-sv4not0p", "--subtractValueForNonZeroPixels", "{float[ float[ float[ ...]]]}", "null", "0",
		"This parameter specifies the subtraction value that will be subtracted to those image pixels that are not zero(0). First value is for the first channel, second value for the second channel and so on. If only one value is specified, it will be the same for all channels. If no value is given, no subtraction value is considered (subtract = 0)."	
		},
		{"-svim", "--subtractValueIfMatch", "{float[ float[ float[ ...]]]}", "null", "0",
		"This parameter indicates which original pixel values will be set to zero (0) in case of a match. First value is for the first channel, second value for the second channel and so on. If only one value is specified, it will be the same for all channels. If no value is given, no subtraction value is considered (subtract = 0)."	
		},
		{"-sptDpcm", "--SpatialDPCM", "{int}", "null", "0",
		 "This parameter indicates that a spatial DPCM is applied to the image coefficients \n"+
		 "\t 1 - Forward Left Spatial DPCM: Intensity magnitude of column x is substracted from intensity magnitude of colum (x+1). Intensity magnitude of column 1 is left untouched.\n"+	
		 "\t -1 - Reverse Left Spatial DPCM: Intensity magnitude of column (x-1) is aded to intensity magnitude of colum x. Intensity magnitude of column 1 is left untouched.\n"+	
		 "\t 2 - Forward Right Spatial DPCM: Intensity magnitude of column x is substracted from intensity magnitude of colum (x-1). Intensity magnitude of the last column is left untouched.\n"+
		 "\t -2 - Reverse Right Spatial DPCM: Intensity magnitude of column (x+1) is added to intensity magnitude of colum x. Intensity magnitude of the last column is left untouched.\n"
		},
		{"-wo", "--waveletOrder", "{int}", "0", "0",
		"Determine the order in which the spatial wavelet transform (if any) is applied.\n"+ 
		"This parameter is relevant for the reversible transforms only.\n" +
                "\t 0: (default) horizontal/vertical (CCSDS standard)\n"+
                "\t 1 : vertical/horizontal (JPEG2000 standard).\n"},
		{"-q", "--quantization", "{int int}", "null", "0",
			"This parameter specifies the quantization type that will be applied and the delta value. To know the relation between delta and the PAE please see the sourcecode. The 1st parameter is to select quantization (1) or dequantization (-1) operation,  2nd parameter for the quantizer type, and the 3rd for the delta value.\n"
					+ "0.- Scalar Deadzone Quantizer. (PAE = delta) \n"
					+ "1.- Uniform quantizer. (PAE = delta)."

		},
	};

	//ARGUMENTS VARIABLES
	String inputImageFile= null;
	String outputImageFile = null;
	int[] inputImageGeometry = null;
	int[] outputImageGeometry = null;
	int[] coefficientsApproximation = null;
	float[] minValue = null;
	float[] maxValue = null;
	float[] lowerThresholdValue = null;
	float[] upperThresholdValue = null;
	float[] subtractValue = null;
	int spectralWaveletType = 0;
	int spectralWaveletLevel = 0;
	int spatialWaveletType = 0;
	int spatialWaveletLevel = 0;
	int z0 = 0;
	int zl = 0;
	int[][] zr = null;
	int y0 = 0;
	int yl = 0;
	int x0 = 0;
	int xl = 0;
	int dpcm = 0;
	int dpcmIndex = 0;
	int rotate = 0;
	int[] changeDim = null;
	float[] rangeModification = null;
	float[] nodataValues = null;
	int[] permuteType=null;
	int permuteDimension = -1;
	int[] permutationSequence = null;
	int nonNullCoefficients = 0;
	float[] subtractValueForNonZeroPixels = null;
	float[] subtractValueIfMatch = null;
	int spatialDPCM = 0;
	int spatialOrder = 0;
	int [] quantization = null;

	
	private boolean isCoefficientApproximation = false;
	private boolean isSpectralWavelet = false;
	private boolean isDPCM = false;
	private boolean isSpatialWavlet = false;
	private boolean isCrop = false;
	private boolean isRotate = false;
	private boolean isPermutation = false;
	private boolean isRemoveNullCoefficients = false;
	private boolean isSpatialDPCM = false;
	private boolean isQuantization = false;
	
	 /**
	  * Class constructor that receives the arguments string and initializes all the arguments
	  * 
	  * @param args the array of strings passed at the command line
	  * 
	  * @throws Exception when an invalid parsing is detected or some problem with method invocation occurs
	  */
	public ArgsParser(String[] args) throws Exception{
		int argNum = 0;
		boolean[] argsFound = new boolean[argsSpecification.length];

		//Arguments parsing
		for(int i = 0; i < argsSpecification.length; i++){
			argsFound[i] = false;
		}
		while(argNum < args.length){
			int argFound = argFind(args[argNum]);
			if(argFound != -1){
				if(!argsFound[argFound]){
					argsFound[argFound] = true;
					int argOptions = argNum + 1;
					while(argOptions < args.length){
						if(argFind(args[argOptions]) != -1){
							break;
						}else{
							argOptions++;
						}
					}
					int numOptions = argOptions - argNum;
					String[] options = new String[numOptions];
					System.arraycopy(args, argNum, options, 0, numOptions);
					argNum = argOptions;
					switch(argFound){
					case 0: //-h  --help
						showArgsInfo();
						System.exit(1);
						break;
					case 1: //-i  --inputImageFile
						inputImageFile = parseString(options);
						if(inputImageFile.endsWith(".raw")){
							argsSpecification[2][4] = "1";
						}
						break;
					case 2: //-ig  --inputImageGeometry
						inputImageGeometry = parseIntegerArray(options, 6);
						break;
					case 3: //-o  --outputImageFile
						outputImageFile = parseString(options);
						break;
					case 4: //-og  --outputImageGeometry
						outputImageGeometry = parseIntegerArray(options, 6);
						break;	
					case 5: //-ca --coefficientsApproximation
						coefficientsApproximation =	parseIntegerArray(options);
						isCoefficientApproximation = true;
						break;
					case 6: //"-mn", "--minValue"
						minValue = parseFloatArray(options);
						isCoefficientApproximation = true;
						break;
					case 7: //"-mx", "--maxValue"
						maxValue = parseFloatArray(options);
						isCoefficientApproximation = true;
						break;
					case 8: //"-ltv", "--lowerThresholdValue"
						lowerThresholdValue = parseFloatArray(options);
						isCoefficientApproximation = true;
						break;
					case 9: //"-utv", "--upperThresholdValue"
						upperThresholdValue = parseFloatArray(options);
						isCoefficientApproximation = true;
						break;
					case 10: //"-sv", "--subtractValue"
						subtractValue = parseFloatArray(options);
						isCoefficientApproximation = true;
						break;
					case 11: //"-swl", "--spectralWaveletLevel"
						spectralWaveletLevel = parseInteger(options);
						isSpectralWavelet = true;
						break;
					case 12: //"-swt", "--spectralWaveletType"
						spectralWaveletType = parseInteger(options);
						isSpectralWavelet = true;
						break;
					case 13: //"-wl", "--spatialWaveletLevel"
						spatialWaveletLevel = parseInteger(options);
						isSpatialWavlet = true;
						break;
					case 14: //"-wt", "--spatialWaveletType"
						spatialWaveletType = parseInteger(options);
						isSpatialWavlet = true;
						break;
					case 15: //"-z0", "--zInit"
						if (zr != null) throw new ParameterException("The components ranges (\"-zr\") parameter is not compatible with the components (\"-z0\") parameter");
						z0 = parseInteger(options);
						isCrop = true;
						break;
					case 16: //"-zl", "--zLength"
						if (zr != null) throw new ParameterException("The components ranges (\"-zr\") parameter is not compatible with the components (\"-zl\") parameter");
						zl = parseIntegerPositive(options);
						isCrop = true;
						break;
					case 17: //"-zr", "--zRanges"	
						if (zl > 0) throw new ParameterException("The components ranges (\"-zr\") parameter is not compatible with the components (\"-z0\") parameter");
						zr = parseIntegerRangesArray(options);		
						isCrop = true;
						break;
					case 18: //"-y0", "--yInit"
						y0 = parseIntegerPositive(options);
						isCrop = true;
						break;
					case 19: //"-yl", "--yLength"
						yl = parseIntegerPositive(options);
						isCrop = true;
						break;
					case 20: //"-x0", "--xInit"
						x0 = parseIntegerPositive(options);
						isCrop = true;
						break;
					case 21: //"-xl", "--xLength"
						xl = parseIntegerPositive(options);
						isCrop = true;
						break;
					case 22: //"-df", "--DPCM"
						dpcm = parseInteger(options);
						isDPCM = true;
						break;
					case 23: //"-di", "--dpcmIndex"
						dpcmIndex = parseIntegerPositive(options);
						isDPCM = true;
						break;
					case 24: //"-ro", "--rotate"
						rotate = parseIntegerPositive(options);
						isRotate = true;
						break;
					case 25: //"-cd", "--changeDim"
						changeDim = parseIntegerArray(options);
						break;
					case 26://"rm", "--rangeModification"
						rangeModification = parseFloatArray(options);
						break;
					case 27://"-nd", "--noDataValues"
						nodataValues = parseFloatArray(options);
						break;
					case 28://"-pc", "--permuteComponents"
						permuteType = parseIntegerArray(options);
						break;
					case 29: //"-pd", "--permuteDimension"
						permuteDimension = parseIntegerPositive(options);
						isPermutation = true;
						break;
					case 30: //"-ps", "--permutationSequence"
						permutationSequence = parseIntegerArray(options);
						isPermutation = true;
						break;
					case 31://"-nnc", "--nonNullCoefficients"
						nonNullCoefficients = parseInteger(options);
						isRemoveNullCoefficients = true;
						break;
					case 32: //"-sv4not0p", "--subtractValueForNonZeroPixels"
						subtractValueForNonZeroPixels = parseFloatArray(options);
						isCoefficientApproximation = true;
						break;
					case 33: //"-svim", "--subtractValueIfMatch"
						subtractValueIfMatch = parseFloatArray(options);
						isCoefficientApproximation = true;
						break;
					case 34://"-sptDpcm", "--SpatialDPCM"
						spatialDPCM = parseInteger(options);
						isSpatialDPCM = true;
						break;
					case 35://"-wo", "--waveletSpatialOrder"
                        spatialOrder = parseInteger(options);
                        break;
					case 36://"-q", "--quantization"
						quantization = parseIntegerArray(options);
						isQuantization = true;
						break;
					}
				}else{
					throw new Exception("Argument \"" + args[argNum] + "\" repeated.");
				}
			}else{
				throw new Exception("Argument \"" + args[argNum] + "\" unrecognized.");
			}
			
			// Check max. number of operations (only 1 is allowed)
			int numOfOperations = 0;
			if (isCoefficientApproximation) numOfOperations++;
			if (isSpectralWavelet) numOfOperations++;
			if (isDPCM) numOfOperations++;
			if (isSpatialWavlet) numOfOperations++;
			if (isCrop) numOfOperations++;
			if (isRotate) numOfOperations++;
			if (rangeModification != null && nodataValues != null) numOfOperations++;
			if (isPermutation) numOfOperations++;
			if (isRemoveNullCoefficients) numOfOperations++;
			if (isSpatialDPCM) numOfOperations++;
			if (isQuantization) numOfOperations++;
			
			if (numOfOperations >= 2) {
				throw new Exception("FormatFileConverter can only perform one transformation at once");
			}
			
		}

		//Check mandatory arguments
		for(int i = 0; i < argsSpecification.length; i++){
			if(argsSpecification[i][4].compareTo("1") == 0){
				if(!argsFound[i]){
					throw new Exception("Argument \"" + argsSpecification[i][0] + "\" is mandatory (\"-h\" displays help).");
				}
			}
		}
	}

	/**
	 * Finds the argument string in arguments specification array.
	 *
	 * @param arg argument to find out in argsSpecification
	 * @return the argument index of argsSpecification (-1 if it doesn't exist)
	 */
	int argFind(String arg){
		int argFound = 0;
		boolean found = false;

		while((argFound < argsSpecification.length) && !found){
			if((arg.compareTo(argsSpecification[argFound][0]) == 0) || (arg.compareTo(argsSpecification[argFound][1]) == 0)){
				found = true;
			}else{
				argFound++;
			}
		}
		return(found ? argFound: -1);
	}

	/**
	 * This function shows arguments information to console.
	 */
	public void showArgsInfo(){
		System.out.println(	"NOTE: Only one transformation can be performed at once");
		
		System.out.println("Arguments specification: ");
		for(int numArg = 0; numArg < argsSpecification.length; numArg++){
			char beginMandatory = '{', endMandatory = '}';
			if(argsSpecification[numArg][4].compareTo("0") == 0){
				//No mandatory argument
				beginMandatory = '[';
				endMandatory = ']';
			}
			System.out.print("\n" + beginMandatory + " ");
			System.out.print("{" + argsSpecification[numArg][0] + "|" + argsSpecification[numArg][1] + "} " + argsSpecification[numArg][2]);
			System.out.println(" " + endMandatory);
			System.out.println("  Explanation:\n    " + argsSpecification[numArg][5]);
			System.out.println("  Default value: " + argsSpecification[numArg][3]);
		}
	}


	/////////////////////
	//PARSING FUNCTIONS//
	/////////////////////
	//These functions receives a string array that contains in first position the argument and then their options//

	int parseIntegerPositive(String[] options) throws Exception{
		int value = 0;

		if(options.length == 2){
			try{
				value = Integer.parseInt(options[1]);
				if(value < 0){
					throw new Exception("\"" + options[1] + "\" of argument \"" + options[0] + "\" is must be a positive integer.");
				}
			}catch(NumberFormatException e){
				throw new Exception("\"" + options[1] + "\" of argument \"" + options[0] + "\" is not a parsable integer.");
			}
		}else{
			throw new Exception("Argument \"" + options[0] + "\" takes one option. Try \"-h\" to display help.");
		}
		return(value);
	}

	int parseInteger(String[] options) throws Exception{
		int value = 0;

		if(options.length == 2){
			try{
				value = Integer.parseInt(options[1]);
			}catch(NumberFormatException e){
				throw new Exception("\"" + options[1] + "\" of argument \"" + options[0] + "\" is not a parsable integer.");
			}
		}else{
			throw new Exception("Argument \"" + options[0] + "\" takes one option. Try \"-h\" to display help.");
		}
		return(value);
	}
	
	String parseString(String[] options) throws Exception{
		String value = "";

		if(options.length == 2){
			value = options[1];
		}else{
			throw new Exception("Argument \"" + options[0] + "\" takes one option. Try \"-h\" to display help.");
		}
		return(value);
	}

	int[] parseIntegerArray(String[] options) throws Exception{
		int[] value = null;

		if(options.length >= 2){
			value = new int[options.length - 1];
			for(int numOption = 1; numOption < options.length; numOption++){
				try{
						value[numOption - 1] = Integer.parseInt(options[numOption]);
				}catch(NumberFormatException e){
					throw new Exception("\"" + options[numOption] + "\" of argument \"" + options[0] + "\" is not a parsable integer.");
				}
			}
		}else{
			throw new Exception("Argument \"" + options[0] + "\" takes one or more options. Try \"-h\" to display help.");
		}
		return(value);
	}

	int[] parseIntegerArray(String[] options, int numOptions) throws Exception{
		int[] value = null;

		if(options.length == numOptions+1){
			value = new int[options.length - 1];
			for(int numOption = 1; numOption < options.length; numOption++){
				try{
						value[numOption - 1] = Integer.parseInt(options[numOption]);
				}catch(NumberFormatException e){
					throw new Exception("\"" + options[numOption] + "\" of argument \"" + options[0] + "\" is not a parsable integer.");
				}
			}
		}else{
			throw new Exception("Argument \"" + options[0] + "\" takes " + numOptions +" options. Try \"-h\" to display help.");
		}
		return(value);
	}
	
	float[] parseFloatArray(String[] options) throws Exception{
		float[] value = null;

		if(options.length >= 2){
			value = new float[options.length - 1];
			for(int numOption = 1; numOption < options.length; numOption++){
				try{
						value[numOption - 1] = Float.parseFloat(options[numOption]);
				}catch(NumberFormatException e){
					throw new Exception("\"" + options[numOption] + "\" of argument \"" + options[0] + "\" is not a parsable float.");
				}
			}
		}else{
			throw new Exception("Argument \"" + options[0] + "\" takes one or more options. Try \"-h\" to display help.");
		}
		return(value);
	}

	public int[][] parseIntegerRangesArray(String[] options) throws ParameterException{
		int[][] value = null;

		if(options.length >= 2){
			value = new int[options.length - 1][2];
			for(int numOption = 1; numOption < options.length; numOption++){
				String[] range = options[numOption].split("-");
				if (range.length != 2) {
					throw new ParameterException("\"" + options[numOption] + "\" of argument \"" + options[0] + "\" is not a parsable range.");
				}
				try{
					value[numOption-1][0] = Integer.parseInt(range[0]);
					value[numOption-1][1] = Integer.parseInt(range[1]);
				}catch(NumberFormatException e){
					throw new ParameterException("\"" + options[numOption] + "\" of argument \"" + options[0] + "\" is not a parsable integer.");
				}
			}
		}else{
			throw new ParameterException("Argument \"" + options[0] + "\" takes one or more options. Try \"-h\" to display help.");
		}
				
		return(value);
	}

	///////////////////////////
	//ARGUMENTS GET FUNCTIONS//
	///////////////////////////

	public String getInputImageFile(){
		return(inputImageFile);
	}
	public String getOutputImageFile(){
		return(outputImageFile);
	}
	public int[] getInputImageGeometry(){
		return(inputImageGeometry);
	}
	public int[] getOutputImageGeometry(){
		return(outputImageGeometry);
	}
	public int[] getCoefficientsApproximation(){
		return(coefficientsApproximation);
	}
	public float[] getMinValue(){
		return(minValue);
	}
	public float[] getMaxValue(){
		return(maxValue);
	}
	public float[] getLowerThresholdValue(){
		return(lowerThresholdValue);
	}
	public float[] getUpperThresholdValue(){
		return(upperThresholdValue);
	}
	public float[] getSubtractValue(){
		return(subtractValue);
	}
	public int getSpectralWaveletLevel (){
		return(this.spectralWaveletLevel);
	}
	public int getSpectralWaveletType (){
		return(this.spectralWaveletType);
	}
	public int getSpatialWaveletLevel (){
		return(this.spatialWaveletLevel);
	}
	public int getSpatialWaveletType (){
		return(this.spatialWaveletType);
	}
	public int getZInit (){
		return(this.z0);
	}
	public int getZLength (){
		return(this.zl);
	}
	public int[][] getZRanges (){
		return(this.zr);
	}
	public int getYInit (){
		return(this.y0);
	}
	public int getYLength (){
		return(this.yl);
	}
	public int getXInit (){
		return(this.x0);
	}
	public int getXLength (){
		return(this.xl);
	}
	public int getDPCM(){
		return(this.dpcm);
	}
	public int getDPCMIndex(){
		return(this.dpcmIndex);
	}
	public int getRotate(){
		return(this.rotate);
	}
	public int[] getChangeDim(){
		return (changeDim);
	}
	public float[] getRangeModification(){
		return (rangeModification);
	}
	public float[] getNodataValues(){
		return (nodataValues);
	}
	public int[] getPermuteType(){
		return (permuteType);
	}
	public int getPermuteDimension(){
		return(this.permuteDimension);
	}
	public int[] getPermutationSequence(){
		return(this.permutationSequence);
	}
	public int getRemoveNullCoefficients(){
		return(this.nonNullCoefficients);
	}
	public float[] getSubtractValueForNonZeroPixels(){
		return(subtractValueForNonZeroPixels);
	}
	public float[] getSubtractValueIfMatch(){
		return(subtractValueIfMatch);
	}
    public int getSpatialDPCM (){
        return(this.spatialDPCM);
    }
    public int getSpatialOrder (){
            return(this.spatialOrder);
    }
    public int[] getQuantization(){
    		return(this.quantization);
    }
}

