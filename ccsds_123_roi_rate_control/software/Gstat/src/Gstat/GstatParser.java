package Gstat;
import GiciException.*;
import GiciFile.LoadFile;
import GiciParser.*;

import java.lang.reflect.*;


/**
 * Arguments parser for Gstat (extended from ArgumentsParser).
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public class GstatParser extends ArgumentsParser{

	//ARGUMENTS SPECIFICATION
	String[][] statArguments = {
		{"-i", "--inputImage", "{string}", "", "1", "1",
			"Input image. Valid formats are: pgm, ppm, pbm, jpg, tiff, png, bmp, gif, fpx. If image is raw data file extension must be \".raw\" or \".img\" and \"-ig1\" parameter is mandatory."
		},
		{"-ig", "--inputImageGeometry", "{int int int int int}", "", "0", "1",
			"Geometry of raw image data. Parameters are:\n    1- zSize (number of image components)\n    2- ySize (image height)\n    3- xSize (image width)\n    4- data type. Possible values are:\n      0- boolean (1 byte)\n      1- unsigned int (1 byte)\n      2- unsigned int (2 bytes)\n      3- signed int (2 bytes)\n      4- signed int (4 bytes)\n      5- signed int (8 bytes)\n      6- float (4 bytes)\n      7- double (8 bytes)\n    5- Byte order (0 if BIG ENDIAN, 1 if LITTLE ENDIAN)"
		},
                // MHC-24/11/2011
		{"-s", "--statistic", "{int}", "1", "0", "1",
			"Statistic to show. Valid ones are:\n" +
                        "    0- Do not show any statistic\n" +
                        "    1- All statistics except 5, 6, 7, 8, and 13 will be shown.\n" +
                        "    2- Minimum and maximum values\n" +
                        "    3- Average\n" +
                        "    4- Image range center\n" +
                        "    5- Number of pixels for especific values (see -v parameter),\n" +
                        "       only for unsigned int (1 byte) images.\n" +
                        "    6- % of pixels for especific values (see -v parameter),\n" +
                        "       only for unsigned int (1 byte) images.\n" +
                        "    7- Number of pixels for all values(see -v parameter),\n" +
                        "       only for unsigned int (1 byte) images.\n" +
                        "    8- % for all values(view -v parameter), only for\n" +
                        "       unsigned int (1 byte) images.\n" +
                        "    9- Energy of the image. The energy of each component is\n" +
                        "       stored in a new File. A file is generated with the\n" +
                        "       following format: 'inputImage_energy'\n" +
                        "    10- Variance of the image. The variance of each\n" +
                        "        component is stored in a new File. A file is generated\n" +
                        "        with the following format: 'inputImage_variance'\n" + 
                        "    11- Entropy of the image. The entropy of each component\n" +
                        "        is stored in a new File. A file is generated with the\n" +
                        "        following format: 'inputImage_entropy'\n" +
                        "    12- Moran's I measure (see http://en.wikipedia.org/wiki/Moran%27s_I),\n" +
                        "        using the 9 closest pixels.\n" +
                        "    13- Histogram statistic. This statistic cannot be shown with any other."
		},
                // --
		{"-f", "--format", "{int}", "0", "0", "1",
			"Format to show measures. Valid ones are:\n    0- Long\n    1- Short (if all measure are shown it will be showed as MIN:MAX:AVERAGE:CENTER:ENERGY:VARIANCE:ENTROPY:MORAN'S)"
		},
		{"-t", "--totals", "{int}", "1" , "0", "1",
			"To show total measures (average of all components when image have more than one). Valid values are:\n    0- No show totals (only show components)\n    1- Show components and totals (totals is only shown when image have more than one component)\n    2- Show only totals (only valid when image have more than one component)"
		},
		{"-r", "--rate", "{int float[ float[ ...]]}", "0 0" , "0", "1",
			"This options is useful to calculate the number of bytes for the image using an specified compression factor. First values specifies the measure to specify this compression factor; it can be:\n    0- This option is disabled\n    1- CF Compression Factor (SizeOriginalImage/SizeCompressedImage)\n    2- CR Compression Ratio (SizeCompressedImage/SizeOriginalImage) \n    3- BPS Bits Per Sample (how many bits are used for each image sample in the compressed image)\n    Second and next values are the compressions that will be computed."
		},
		{"-v", "--values", "{int[ int[ int[ ...]]]}", "", "0", "1",
			"Values of pixels to be measure."
		},
		{"-h", "--help", "", "", "0", "1",
			"Displays this help and exits program."
		}
	};

	//ARGUMENTS VARIABLES
	String imageFile = "";
	int[] imageGeometry = null;
	int statistic = 1;
	int format = 0;
	int totals = 1;
	int rate = 0;
	int measure = -1;
	int[] values = null;
	float[] compressionFactors = null;

	/**
	 * Receives program arguments and parses it, setting to arguments variables.
	 *
	 * @param arguments the array of strings passed at the command line
	 *
	 * @throws ParameterException when an invalid parsing is detected
	 * @throws ErrorException when some problem with method invocation occurs
	 */
	public GstatParser(String[] arguments) throws ParameterException, ErrorException{
		try{
			Method m = this.getClass().getMethod("parseArgument", new Class[] {int.class, String[].class});
			parse(statArguments, arguments, this, m);
		}catch(NoSuchMethodException e){
			throw new ErrorException("Coder parser error invoking parse function.");
		}
	}

	/**
	 * Parse an argument using parse functions from super class and put its value/s to the desired variable. This function is called from parse function of the super class.
	 *
	 * @param argFound number of parameter (the index of the array compArguments)
	 * @param options the command line options of the argument
	 *
	 * @throws ParameterException when some error about parameters passed (type, number of params, etc.) occurs
	 */
	public void parseArgument(int argFound, String[] options) throws ParameterException{
		switch(argFound){
		case  0: //-i  --inputImage
			imageFile = parseString(options);
			if(LoadFile.isRaw(imageFile)){
				statArguments[1][4] = "1";
			}
			break;
		case  1: //-ig  --inputImageGeometry
			imageGeometry = parseIntegerArray(options, 5);
			checkImageGeometry(imageGeometry);
			break;
		case  2: //-s --statistic
			statistic = parseIntegerPositive(options);
			if((statistic < 0) || (statistic > 13)){
				throw new ParameterException("Statistic must be between 0 to 13.");
			}
			break;
		case  3: //-f --format
			format = parseIntegerPositive(options);
			if((format < 0) || (format > 2)){
				throw new ParameterException("Format must be between 0 to 1.");
			}
			break;
		case  4: //-t --totals
			totals = parseIntegerPositive(options);
			if((totals < 0) || (totals > 2)){
				throw new ParameterException("Format must be between 0 to 2.");
			}
			break;
		case  5: //-r --rate
			if(options.length < 3){
				throw new ParameterException("Rate parameter needs 2 arguments.");
			}
			String[] tmpOptions = new String[2];
			tmpOptions[0] = options[0];
			tmpOptions[1] = options[1];
			rate = parseIntegerPositive(tmpOptions);
			if((rate < 0) || (rate > 3)){
				throw new ParameterException("Rate must be between 0 to 3.");
			}
			tmpOptions = new String[options.length-1];
			tmpOptions[0] = options[0];
			for(int i = 2; i < options.length; i++){
				tmpOptions[i-1] = options[i];
			}
			compressionFactors = parseFloatArray(tmpOptions);
			break;
		case  6: //-v --values
			values = parseIntegerArray(options);
			for(int value = 0; value < values.length; value++){
				if(values[value] < 0 || values[value] > 255){
					throw new ParameterException("Values must be between 0 to 255.");
				}
			}
			break;
		case  7: //-h  --help
			showArgsInfo();
			System.exit(0);
			break;
		}
	}

	//CHECK PARAMETERS FUNCTIONS
	/**
	 * Check image geometry parameters if image file is raw.
	 *
	 * @param imageGeometry geometry of image
	 *
	 * @throws ParameterException when some parameter is wrong
	 */
	void checkImageGeometry(int[] imageGeometry) throws ParameterException{
		if((imageGeometry[0] <= 0) || (imageGeometry[1] <= 0) || (imageGeometry[2] <= 0)){
			throw new ParameterException("Image dimensions in \".raw\" or \".img\" data files must be positive (\"-h\" displays help).");
		}
		if((imageGeometry[3] < 0) || (imageGeometry[3] > 7)){
			throw new ParameterException("Image type in \".raw\" or \".img\" data must be between 0 to 7 (\"-h\" displays help).");
		}
		if((imageGeometry[4] != 0) && (imageGeometry[4] != 1)){
			throw new ParameterException("Image byte order  in \".raw\" or \".img\" data must be 0 or 1 (\"-h\" displays help).");
		}
	}

	//ARGUMENTS GET FUNCTIONS
	public String getImageFile(){
		return(imageFile);
	}
	public int[] getImageGeometry(){
		return(imageGeometry);
	}
	public int getStatistic(){
		return(statistic);
	}
	public int getTotals(){
		return(totals);
	}
	public int getFormat(){
		return(format);
	}
	public int getRate(){
		return(rate);
	}
	public float[] getCompressionFactors(){
		return(compressionFactors);
	}
	public int[] getValues(){
		return(values);
	}

}