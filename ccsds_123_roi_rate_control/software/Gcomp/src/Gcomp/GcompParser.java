/*
 * GICI Applications -
 * Copyright (C) 2007  Group on Interactive Coding of Images (GICI)
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
package Gcomp;
import GiciException.*;
import GiciFile.LoadFile;
import GiciParser.*;

import java.lang.reflect.*;


/**
 * Arguments parser for Gcomp (extended from ArgumentsParser).
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public class GcompParser extends ArgumentsParser {

	//ARGUMENTS SPECIFICATION
	String[][] compArguments = {
		{"-i1", "--inputImage1", "{string}", "", "1", "1",
			"Input image 1 (it must be the ORIGINAL image). Valid formats are: pgm, ppm, pbm, jpg, tiff, png, bmp, gif, fpx. If image is raw data file extension must be \".raw\" or \".rawl\" or \".img\" and \"-ig1\" parameter is mandatory."
		},
		{"-ig1", "--inputImageGeometry1", "{int int int int int}", "", "0", "1",
			"Geometry of raw image data 1. Parameters are:\n    1- zSize (number of image components)\n    2- ySize (image height)\n    3- xSize (image width)\n    4- data type. Possible values are:\n      0- boolean (1 byte)\n      1- unsigned int (1 byte)\n      2- unsigned int (2 bytes)\n      3- signed int (2 bytes)\n      4- signed int (4 bytes)\n      5- signed int (8 bytes)\n      6- float (4 bytes)\n      7- double (8 bytes)\n    5- Byte order (0 if BIG ENDIAN, 1 if LITTLE ENDIAN)"
		},
		{"-i2", "--inputImage2", "{string}", "", "1", "1",
			"Input image 2. Valid formats are: pgm, ppm, pbm, jpg, tiff, png, bmp, gif, fpx. If image is raw data file extension must be \".raw\" or \".img\" and \"-ig2\" parameter is mandatory."
		},
		{"-ig2", "--inputImageGeometry2", "{int int int int int}", "", "0", "1",
			"Geometry of raw image data 2. Parameters are:\n    1- zSize (number of image components)\n    2- ySize (image height)\n    3- xSize (image width)\n    4- data type. Possible values are:\n      0- boolean (1 byte)\n      1- unsigned int (1 byte)\n      2- unsigned int (2 bytes)\n      3- signed int (2 bytes)\n      4- signed int (4 bytes)\n      5- signed int (8 bytes)\n      6- float (4 bytes)\n      7- double (8 bytes)\n    5- Byte order (0 if BIG ENDIAN, 1 if LITTLE ENDIAN)"
		},
		{"-c", "--component", "{int}", "", "0", "1",
			"If this flag is enabled it indicates the component to compare. Values allowed are positve intergers greater than 0. 1 for the first component, 2 for the second component and succesively. Original image, could be multi, hyper or ultra spectral, while second image is monocomponent. Parameters are:\n    1- The component to compare."
		},
		{"-mk", "--mask", "{string}", "", "0", "1",
			"Mask that indicates the samples to be compared with the original image. Values of the mask can be ranged from 0 to 255. If is a black and white mask, the white sample will be compared with the original mask, the black ones will be ignored."
		},
		{"-inv", "--inverse", "{int}", "0", "0", "1",
			"Inverse the mask utility. Valid values are:\n    0- Mask is used as default utility, like is exposed in -mk parameter\n    1- Mask is used inverting his usability, all samples different to black are set to black, and the black samples will be changed to white."
		},
		{"-nd", "--noDataValues", "{float[ float[ float[ ...]]]}", "no No-data values", "0", "1",
			"Pixels in the input image (-i1) considered No-data values. No-data values will not be considered in the image comparasion."
		},
		{"-m", "--measure", "{int}", "0", "0", "1",
			"Measure to show. Valid values are:\n    0- All measures will be shown\n"
			+ "    1- MAE Mean Absolute Error\n"
			+ "    2- PAE Peak Absolute Error\n"
			+ "    3- MSE Mean Squared Error or P-MSE if mask and weights values are defined\n"
			+ "    4- RMSE Root Mean Squared Error\n"
			+ "    5- ME Mean Error\n"
			+ "    6- SNR Signal to Noise Ratio [ 10*log10(AverageEnergy/MSE) ]\n"
			+ "    7- PSNR Peak Signal to Noise Ratio [ 10*log10((2^dynamicRange-1)^2/MSE) ]\n"
			+ "    8- PSNR-S computed as is said in Salomon's book, or P-PNSR if mask and weights values are defined\n"
			+ "    9- SNRVAR Signal to Noise Ratio calculated with the original image Variance\n"
			+ "    10- SSIM Structural Similarity (local statistics are computed over the entire image, and K1 = 0.01 and K2 = 0.03)\n"
			+ "    11- RRMSE Relative RMSE\n"
			+ "    12- NMSE Normalized MSE [ 100 * MSE / Energy ]\n"
			+ "    13- PSNR-NC Non-classical PSNR [ 10*log10(Max(Original)^2/MSE) ]\n"
			+ "    14- EQUALITY"
		},
		{"-f", "--format", "{int}", "0", "0", "1",
			"Format to show measures. Valid values are:\n    0- Long\n    1- Short (if all measure are shown it will be shown as MAE:PAE:MSE:RMSE:ME:SNR:PSNR:PSNR-S:SNRVAR:SSIM:RRMSE:NMSE:PNSR-NC:EQUALITY)"
		},
		{"-t", "--totals", "{int}", "1" , "0", "1",
			"To show total measures (average of all components when image have more than one). Valid values are:\n    0- Do not show totals (only show components)\n    1- Show components and totals (two identical lines will appear when only one component is compared)\n    2- Show only totals"
		},
		{"-v", "--values", "{float[ float[ float[ ...]]]}", "no values", "0", "1",
			"Relation between mask value and weight applied. First value is the mask value, and the second value indicates the factor to applied to the corresponent sample, (x[n] - x'[n]) * factor. A pair of values is needed for each weight to be applied"
		},
		{"-pbd", "--pixelBitDepth","{int[ int[ int[ ...]]]}", "get from the image format" ,"0","1",
			"Integer array which indicates the pixel bit depth for each component. First value is for the first component, second value for the second component and so on. If only one value is specified, the pixel bit depth will be the same for all component. Note that sometimes the format used to store the image requires more bytes than the needed to store the original image, in these cases this parameter must be employed."
		},
		{"-map", "--showVisualComparisonMap", "", "", "0", "1",
			"Show a visual comparision of the image."			
		},
		{"-emap", "--showEnhancedVisualComparisonMap", "", "", "0", "1",
			"Show an enhanced visual comparision of the image."			
		},
		{"-lm", "--lowMemory", "", "", "0", "1",
			"Employes a low memory consumption strategy. With this option some features of Gcomp are unavailable, including some metrics."			
		},
		{"-h", "--help", "", "", "0", "1",
			"Displays this help and exits program."
		}
	};

	//ARGUMENTS VARIABLES
	String imageFile1 = "";
	int[] imageGeometry1 = null;
	String imageFile2 = "";
	int[] imageGeometry2 = null;
	String maskFile = null;
	float[] noDataValues = null;
	float[] ROIValues = null;
	int measure = 0;
	int format = 0;
	int totals = 1;
	int inverse = 0;
	int[] pixelBitDepth = null;
	int component = -1;
	int showVisualComparisionMap = 0;
	boolean lowMemory = false;

	/**
	 * Receives program arguments and parses it, setting to arguments variables.
	 *
	 * @param arguments the array of strings passed at the command line
	 *
	 * @throws ParameterException when an invalid parsing is detected
	 * @throws ErrorException when some problem with method invocation occurs
	 */
	public GcompParser(String[] arguments) throws ParameterException, ErrorException {
		try {
			Method m = this.getClass().getMethod("parseArgument", new Class[] {int.class, String[].class});
			parse(compArguments, arguments, this, m);
		} catch(NoSuchMethodException e) {
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
		case  0: //-i1  --inputImage1
			imageFile1 = parseString(options);
			if (LoadFile.isRaw(imageFile1)) {
				compArguments[1][4] = "1";
			}
			break;
		case  1: //-ig1  --inputImageGeometry1
			imageGeometry1 = parseIntegerArray(options, 5);
			checkImageGeometry(imageGeometry1);
			break;
		case  2: //-i2  --inputImage2
			imageFile2 = parseString(options);
			if (LoadFile.isRaw(imageFile2)) {
				compArguments[3][4] = "1";
			}
			break;
		case  3: //-ig2  --inputImageGeometry2
			imageGeometry2 = parseIntegerArray(options, 5);
			checkImageGeometry(imageGeometry2);
			break;
		case 4: //-c  --component
			component = parseIntegerPositive(options);
			if ((component < 0)) {
				throw new ParameterException("Component must be greater than or equal to zero.");
			}
			break;
		case 5: //-mk  --mask
			maskFile = parseString(options);
			break;
		case 6:
			inverse = parseIntegerPositive(options);
			if ((inverse < 0) || (inverse > 1)) {
				throw new ParameterException("Inverse values only can be 0 or 1.");
			}
			break;
		case 7: //-nd  --noDataValues
			noDataValues = parseFloatArray(options);
			break;
		case 8: //-m --measure
			measure = parseIntegerPositive(options);
			if (measure > 14) {
				throw new ParameterException("Measure must be between 0 and 14 (but got " + options[1] +").");
			}
			break;
		case 9: //-f --format
			format = parseIntegerPositive(options);
			if ((format < 0) || (format > 1)) {
				throw new ParameterException("Format must be between 0 and 1.");
			}
			break;
		case 10: //-t --totals
			totals = parseIntegerPositive(options);
			if ((totals < 0) || (totals > 2)) {
				throw new ParameterException("Format must be between 0 and 2.");
			}
			break;
		case 11: //-v --values
			ROIValues = parseFloatArray(options);
			break;
		case 12: //-pbd --pixelBitDepth
			pixelBitDepth = parseIntegerArray(options);
			for (int k = 0; k < pixelBitDepth.length; k++) {
				if (pixelBitDepth[k] < 0) {
					throw new ParameterException("Pixel bit depth must be positive.");
				}
			}
			break;
		case 13: // -map, --showVisualComparisonMap
			showVisualComparisionMap = 1;
			break;
		case 14: // -emap --showEnhancedVisualComparisonMap
			showVisualComparisionMap = 2;
			break;
		case 15: // -lm --lowMemory
			lowMemory = true;
			break;
		case 16: //-h  --help
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
		if ((imageGeometry[0] <= 0) || (imageGeometry[1] <= 0) || (imageGeometry[2] <= 0)) {
			throw new ParameterException("Image dimensions in \".raw\" or \".img\" data files must be positive (\"-h\" displays help).");
		}
		if ((imageGeometry[3] < 0) || (imageGeometry[3] > 7)) {
			throw new ParameterException("Image type in \".raw\" or \".img\" data must be between 0 to 7 (\"-h\" displays help).");
		}
		if ((imageGeometry[4] != 0) && (imageGeometry[4] != 1)) {
			throw new ParameterException("Image byte order  in \".raw\" or \".img\" data must be 0 or 1 (\"-h\" displays help).");
		}
	}

	//ARGUMENTS GET FUNCTIONS
	public String getImageFile1() {
		return imageFile1;
	}
	public int[] getImageGeometry1() {
		return imageGeometry1;
	}
	public String getImageFile2() {
		return imageFile2;
	}
	public int getComponent() {
		return component;
	}
	public String getMaskFile() {
		return maskFile;
	}
	public int getInverse() {
		return inverse;
	}
	public float[] getNoDataValues() {
		return noDataValues;
	}
	public int[] getImageGeometry2() {
		return imageGeometry2;
	}
	public int getMeasure() {
		return measure;
	}
	public int getTotals() {
		return totals;
	}
	public int getFormat() {
		return format;
	}
	public float[] getROIValues() {
		return ROIValues;
	}
	public int[] getPixelBitDepth() {
		return pixelBitDepth;
	}
	public int getShowVisualComparisionMap() {
		return showVisualComparisionMap;
	}
	public boolean getLowMemory() {
		return lowMemory;
	}
}

