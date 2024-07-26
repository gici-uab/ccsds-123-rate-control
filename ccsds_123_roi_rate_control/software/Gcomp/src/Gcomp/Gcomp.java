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

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import GiciAnalysis.ConsoleComparisonMap;
import GiciAnalysis.ImageCompareInterface;
import GiciAnalysis.ImageCompareLowMem;
import GiciAnalysis.ImageCompareSA;
import GiciException.ErrorException;
import GiciException.ParameterException;
import GiciException.WarningException;
import GiciFile.LoadFile;
import GiciFile.LoadRawFile;
import GiciMask.GenerateMask;
import GiciMask.LoadMask;


/**
 * Application to compare 2 images.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 3.0
 */
public class Gcomp{

	private static void prettyPrint(final int format, Set<Integer> measures, final String[][] output, final int component) {
		if (format == 0) {
			// Output a pretty formatted text
			if (component >= 0) {
				System.out.println("COMPONENT " + component + ":");
			} else {
				System.out.println("TOTALS:");
			}

			for (int i = 0; i < output.length; i++) {
				if (measures.contains(i)) {
					String a = "  " + output[i][0];

					// Pad up to 9 spaces
					while (a.length() < 9) { a = a + " "; }

					System.out.println(a + ": " + output[i][1]);
				}
			}
		} else {
			// Output a computer-readable formatted text
			boolean first = true;
			for (int i = 0; i < output.length; i++) {
				if (measures.contains(i)) {
					if (first) {
						System.out.print(output[i][1]);
						first = false;
					} else {
						System.out.print(":" + output[i][1]);
					}
				}
			}
			System.out.print("\n");
		}
	}
	
	/**
	 * Main method of Gcomp application. It takes program arguments, loads images and compare them.
	 *
	 * @param args an array of strings that contains program parameters
	 */
	public static void main(String[] args)throws ErrorException{
		//Parse arguments
		GcompParser parser = null;

		try {
			parser = new GcompParser(args);
		} catch(ErrorException e) {
			System.out.println("RUN ERROR:");
			e.printStackTrace();
			System.out.println("Please report this error (specifying image type and parameters) to: gici-dev@abra.uab.es");
			System.exit(1);
		} catch(ParameterException e) {
			System.out.println("ARGUMENTS ERROR: " +  e.getMessage());
			System.exit(2);
		}

		//Images load
		final String imageFile1     = parser.getImageFile1();
		final int[]  imageGeometry1 = parser.getImageGeometry1();
		final String imageFile2     = parser.getImageFile2();
		final int[]  imageGeometry2 = parser.getImageGeometry2();
		String maskFile       = parser.getMaskFile();
		int inverse		 	  = parser.getInverse();
		float[] ROIValues	  = parser.getROIValues();
		int[] pixelBitDepth   = parser.getPixelBitDepth();
		int component		  = parser.getComponent();
		
		int zSize = imageGeometry1[0];
		
		//Images compare
		try {
			ImageCompareInterface ic = null;
			
			if (!parser.getLowMemory()) {

				LoadFile image1 = null;
				LoadFile image2 = null;	

				//Images load
				try {
					if (LoadFile.isRaw(imageFile1)) {
						image1 = new LoadFile(imageFile1, imageGeometry1[0], imageGeometry1[1], imageGeometry1[2], imageGeometry1[3], imageGeometry1[4], false);
					} else {
						image1 = new LoadFile(imageFile1);
					}

					if (LoadFile.isRaw(imageFile2)) {
						image2 = new LoadFile(imageFile2, imageGeometry2[0], imageGeometry2[1], imageGeometry2[2], imageGeometry2[3], imageGeometry2[4], false);
					} else {
						image2 = new LoadFile(imageFile2);
					}
				} catch(IllegalArgumentException e) {
					System.out.println("IMAGE LOAD ERROR Valid formats are: pgm, ppm, pbm, jpg, tiff, png, bmp, gif, fpx. If image is raw data file extension must be \".raw\" or \".img\"");
					System.exit(3);

				} catch(RuntimeException e) {
					System.out.println("IMAGE LOAD ERROR Valid formats are: pgm, ppm, pbm, jpg, tiff, png, bmp, gif, fpx. If image is raw data file extension must be \".raw\" or \".img\"");
					System.exit(3);

				} catch(WarningException e) {
					System.out.println("IMAGE LOAD ERROR: " + e.getMessage());
					System.exit(3);
				}

				//Mask load
				byte[][][] maskSamples = null;
				if (maskFile != null) {

					LoadMask lm = null;

					try {
						if (! LoadFile.isRaw(maskFile)) {
							lm = new LoadMask(maskFile);
						} else {
							lm = new LoadMask(maskFile, 1, imageGeometry1[1], imageGeometry1[2], 0, 1);
						}
					} catch(WarningException e) {
						System.out.println(e.getMessage());
						System.exit(0);
					}

					if (ROIValues != null) {
						//ROI
						maskSamples = lm.getMaskSamplesByteValue();
					} else {
						//NO-DATA
						maskSamples = lm.getMaskSamplesByte();
					}

					// If only one mask component is provided
					// set the same mask for all the components				
					if (maskSamples.length == 1 && maskSamples.length != zSize) {
						byte[][][] newMaskSamples = maskSamples;

						maskSamples = new byte[zSize][][];
						for (int z = 0; z < zSize; z++) {
							maskSamples[z] = newMaskSamples[0];
						}
					}
				}


				//Invert the mask for the ROI applications
				if ((inverse == 1) && (ROIValues != null)) {
					for (int z = 0; z < maskSamples.length; z++) {
						for (int y = 0; y < maskSamples[z].length; y++) {
							for (int x = 0; x < maskSamples[z][y].length; x++) {
								if (maskSamples[z][y][x] == -128) {
									maskSamples[z][y][x] = 127;
								} else {
									maskSamples[z][y][x] = -128;
								}
							}
						}
					}
				}

				//Invert the mask for the no-data applications
				if ((inverse == 1) && (ROIValues == null)) {
					for (int z = 0; z < maskSamples.length; z++) {
						for (int y = 0; y < maskSamples[z].length; y++) {
							for (int x = 0; x < maskSamples[z][y].length; x++) {
								if(maskSamples[z][y][x] == 0) {
									maskSamples[z][y][x] = 1;
								} else {
									maskSamples[z][y][x] = 0;
								}
							}
						}
					}
				}

				
				//Sets the mask from a list of no-data values in the original image
				float[] noDataValues = parser.getNoDataValues();
				if (noDataValues != null) {
					try {
						if (maskSamples != null) {
							throw new WarningException("The no-data mask can not be defined twice.");
						}
						GenerateMask gm = new GenerateMask(image1.getImage());
						gm.setNoDataValuesFromParser(noDataValues);
						gm.run();
						maskSamples = gm.getMaskSamplesByte();
					} catch(WarningException e) {
						System.out.println("GENERATE MASK PROCESS ERROR: " + e.getMessage());
						System.exit(5);
					}
				}

				//Check image types
				@SuppressWarnings("rawtypes")
				Class[] classImage1 = image1.getTypes();
				@SuppressWarnings("rawtypes")
				Class[] classImage2 = image2.getTypes();

				if (component == -1) {
					if (classImage1.length != classImage2.length) {
						throw new WarningException("Number of image components must be the same for both images.");
					}

					for (int z = 0; z < classImage1.length; z++) {
						//if(classImage1[z] != classImage2[z]){
						//	throw new WarningException("Image class types must be the same for both images.");
						//}
					}
				} else {
					if (component >= classImage1.length) {
						throw new WarningException("The original image does not have so many components.");
					}

					if (classImage1[component] != classImage2[0]) {
						throw new WarningException("Image class types must be the same for both images.");
					}
				}

				// Visual comparison map if requested
				if (parser.getShowVisualComparisionMap() > 0) {
					System.out.print(ConsoleComparisonMap.compare(image1.getImage(), image2.getImage(),
							parser.getShowVisualComparisionMap() > 1));
				}

				// Compare
				if(pixelBitDepth != null){
					int bitDepth[] = null;
					if (pixelBitDepth.length < zSize){
						bitDepth = new int[zSize];
						for(int z = 0; z < pixelBitDepth.length; z++){
							bitDepth[z] = pixelBitDepth[z];
						}
						for(int z = pixelBitDepth.length; z < zSize; z++){
							bitDepth[z] = pixelBitDepth[pixelBitDepth.length-1];
						}
					} else{
						bitDepth = pixelBitDepth;
					}
					ic = new ImageCompareSA(image1.getImage(), image2.getImage(), bitDepth, maskSamples, ROIValues, inverse, component, 0, null, null);
				} else {
					ic = new ImageCompareSA(image1.getImage(), image2.getImage(), image1.getPixelBitDepth(), maskSamples, ROIValues, inverse, component, 0, null, null);
				}

			} else {
				// Low memory
				final ImageCompareLowMem iclm = new ImageCompareLowMem(imageGeometry1, imageGeometry2);
				
				/*
				List<Integer> components = new ArrayList<Integer>();
				
				for (int z = 0; z < imageGeometry1[0]; z++) {
					components.add(z);
				}
				
				ParallelMap.map(new ParallelMap.ListIterator(components), new ParallelMap.MapInterface<Integer>() {
					public void apply(final Integer z) {
						try {
							int[][][] inputImage1Band;
							int[][][] inputImage2Band;
							
							inputImage1Band = LoadRawFile.loadRawComponentDataToInteger(imageFile1, imageGeometry1[0], 
									imageGeometry1[1], imageGeometry1[2], imageGeometry1[3], imageGeometry1[4], z);
							
							inputImage2Band = LoadRawFile.loadRawComponentDataToInteger(imageFile2, imageGeometry2[0],
									imageGeometry2[1], imageGeometry2[2], imageGeometry2[3], imageGeometry2[4], z);
							
							iclm.processBand(inputImage1Band[0], inputImage2Band[0], z);
						} catch (IOException e) {
							System.out.println("IMAGE LOAD ERROR: " + e.getMessage());
							System.exit(3);
						}
					}
				});
				 */
				
				try {
					for (int z = 0; z < imageGeometry1[0]; z++) {
						int[][][] inputImage1Band;
						int[][][] inputImage2Band;

						inputImage1Band = LoadRawFile.loadRawComponentDataToInteger(imageFile1, imageGeometry1[0], 
								imageGeometry1[1], imageGeometry1[2], imageGeometry1[3], imageGeometry1[4], z);

						inputImage2Band = LoadRawFile.loadRawComponentDataToInteger(imageFile2, imageGeometry2[0],
								imageGeometry2[1], imageGeometry2[2], imageGeometry2[3], imageGeometry2[4], z);

						iclm.processBand(inputImage1Band[0], inputImage2Band[0], z);
					}
				} catch (IOException e) {
					System.out.println("IMAGE LOAD ERROR: " + e.getMessage());
					System.exit(3);
				}
				
				iclm.produceTotalResults();
				
				ic = iclm;
			}
			
			double[] mae = ic.getMAE();
			double totalMAE = ic.getTotalMAE();
			double[] pae = ic.getPAE();
			double totalPAE = ic.getTotalPAE();
			double[] mse = ic.getMSE();
			double totalMSE = ic.getTotalMSE();
			double[] rmse = ic.getRMSE();
			double totalRMSE = ic.getTotalRMSE();
			double[] me = ic.getME();
			double totalME = ic.getTotalME();
			double[] snr = ic.getSNR();
			double totalSNR = ic.getTotalSNR();
			double[] psnr = ic.getPSNR();
			double totalPSNR = ic.getTotalPSNR();
			double[] psnrSalomon = ic.getPSNRSALOMON();
			double totalPSNRSALOMON = ic.getTotalPSNRSALOMON();
			boolean[] equal = ic.getEQUAL();
			boolean totalEQUAL = ic.getTotalEQUAL();
			double[] snrVar = ic.getSNRVAR();
			double totalSNRVAR = ic.getTotalSNRVAR();
			double[] ssim = ic.getSSIM();
			double totalSSIM = ic.getTotalSSIM();
			double[] rrmse = ic.getRRMSE();
			double totalRRMSE = ic.getTotalRRMSE();
			double[] nmse = ic.getNMSE();
			double totalNMSE = ic.getTotalNMSE();
			double[] psnrNc = ic.getPSNRNC();
			double totalPSNRNC = ic.getTotalPSNRNC();

			//double[] covariance = ic.getCovariance();

			//Show metrics
			int totals = parser.getTotals();
			int format = parser.getFormat();

			// Which measures will be shown
			// TODO: extend the conversion to the parameter parser
			Set<Integer> measures = new TreeSet<Integer>();
			
			if (parser.getMeasure() == 0) {
				for (int i = 0; i < 14; i++) {
					measures.add(i);
				}
			} else {
				measures.add(parser.getMeasure() - 1);
			}
			
			if(totals < 2){ // Per component results are required
				//We are comparing a specific component
				if(component != -1){
					zSize = 1;
				}
				
				for(int z = 0; z < zSize; z++){
					if(component != -1){
						z = component;
					}
					
					// Show results for component "z".										
					String[][] output = {
							{"MAE", Float.toString((float)mae[z])},
							{"PAE", Float.toString((float)pae[z])},
							{ROIValues == null ? "MSE": "P-MSE", Float.toString((float)mse[z])},
							{"RMSE", Float.toString((float)rmse[z])},
							{"ME", Float.toString((float)me[z])},
							{"SNR", Float.toString((float)snr[z])},
							{ROIValues == null ? "PSNR": "P-PSNR", Float.toString((float)psnr[z])},
							{"PSNR-S", Float.toString((float)psnrSalomon[z])},
							{"SNRVAR", Float.toString((float)snrVar[z])},
							{"SSIM", Float.toString((float)ssim[z])},
							{"RRMSE", Float.toString((float)rrmse[z])},
							{"NMSE", Float.toString((float)nmse[z])},
							{"PSNR-NC", Float.toString((float)psnrNc[z])},
							{"EQUAL", Boolean.toString(equal[z])},
					};
								
					prettyPrint(format, measures, output, z);
					
					if(component != -1){
						z = zSize;
					}
				}
			}

			if(totals > 0) {
				// Show total results.				
				String[][] output = {
						{"MAE", Float.toString((float)totalMAE)},
						{"PAE", Float.toString((float)totalPAE)},
						{ROIValues == null ? "MSE": "P-MSE", Float.toString((float)totalMSE)},
						{"RMSE", Float.toString((float)totalRMSE)},
						{"ME", Float.toString((float)totalME)},
						{"SNR", Float.toString((float)totalSNR)},
						{ROIValues == null ? "PSNR": "P-PSNR", Float.toString((float)totalPSNR)},
						{"PSNR-S", Float.toString((float)totalPSNRSALOMON)},
						{"SNRVAR", Float.toString((float)totalSNRVAR)},
						{"SSIM", Float.toString((float)totalSSIM)},
						{"RRMSE", Float.toString((float)totalRRMSE)},
						{"NMSE", Float.toString((float)totalNMSE)},
						{"PSNR-NC", Float.toString((float)totalPSNRNC)},
						{"EQUAL", Boolean.toString(totalEQUAL)},						
				};
				
				prettyPrint(format, measures, output, -1);
			}

		}catch(WarningException e){
			System.out.println("IMAGE COMPARE ERROR: " + e.getMessage());
			System.exit(4);
		}

		System.exit(0);
	}

}
