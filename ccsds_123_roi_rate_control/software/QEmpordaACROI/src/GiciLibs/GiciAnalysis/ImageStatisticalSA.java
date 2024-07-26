/*
  * GICI Library -
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
package GiciAnalysis;

/**
 * This class receives an image and calculates some statistical information about the image.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public class ImageStatisticalSA{

	double regressionDR[] = null;
	double regressionNDR[] = null;
	double totalRegressionDR = 0;
	double totalRegressionNDR = 0;

	/**
	 * Array where min (minMax[component][0]) and max (minMax[component][1]) values of each component will be stored.
	 * <p>
	 * All values allowed.
	 */
	float minMax[][] = null;

	/**
	 * Global min (minMax[0]) and max (minMax[1]) values of the image.
	 * <p>
	 * All values allowed.
	 */
	float totalMinMax[] = null;

	/**
	 * Energy of each component.
	 * <p>
	 * All values allowed.
	 */
	double energyDR[] = null;

	/**
	 * Energy of whole image.
	 * <p>
	 * All values allowed.
	 */
	double totalEnergyDR = 0;

	/**
	 * Average for the data values of each component.
	 * <p>
	 * All values allowed.
	 */
	double averageDR[] = null;

	/**
	 * Average for data values of whole image.
	 * <p>
	 * All values allowed.
	 */
	double totalAverageDR;

	/**
	 * Average for the no-data values of each component.
	 * <p>
	 * All values allowed.
	 */
	double averageNDR[] = null;

	/**
	 * Average for no-data values of whole image.
	 * <p>
	 * All values allowed.
	 */
	double totalAverageNDR;

	/**
	 * Center of range for each data values in the input image components.
	 * <p>
	 * All values allowed.
	 */
	float centerRange[] = null;

	/**
	 * Center of range of data values in the image.
	 * <p>
	 * All values allowed.
	 */
	float totalCenterRange=0;

	/**
	 * Number of data pixels for each component of whole image.
	 * <p>
	 * All values allowed.
	 */
	long numberOfPixelsData[] = null;

	/**
	 * Number of no-data pixels for each component of whole image.
	 * <p>
	 * All values allowed.
	 */
	long numberOfPixelsNoData[] = null;

	/**
	 * Number of data pixels for all component of whole image.
	 * <p>
	 * All values allowed.
	 */
	long totalNumberOfPixelsData;

	/**
	 * Number of data pixels for all component of whole image.
	 * <p>
	 * All values allowed.
	 */
	long totalNumberOfPixelsNoData;

	/**
	 * Difference with the average in mean for all data pixels for each component.
	 * <p>
	 * All values allowed.
	 */
	double averageStdDevDR[] = null;

	/**
	 * Difference with the average in mean for all data pixels for all components.
	 * <p>
	 * All values allowed.
	 */
	double totalAverageStdDevDR = 0;

	/**
	 * Difference with the average in mean for all no-data pixels for each component.
	 * <p>
	 * All values allowed.
	 */
	double averageStdDevNDR[] = null;

	/**
	 * Difference with the average in mean for all no-data pixels for all components.
	 * <p>
	 * All values allowed.
	 */
	double totalAverageStdDevNDR = 0;

	/**
	 * Difference with the average in mean for data pixels against of no-data region.
	 * <p>
	 * All values allowed.
	 */
	double averageStdDevDNDR[] = null;

	/**
	 * Difference with the average in mean for all data pixels against of no-data regions for all components.
	 * <p>
	 * All values allowed.
	 */
	double totalAverageStdDevDNDR = 0;

	/**
	 * Variance of each component.
	 * <p>
	 * All values allowed.
	 */
	double variance[] = null;

	/**
	 * Varianze of whole image.
	 * <p>
	 * All values allowed.
	 */
	double totalVariance = 0;

	/**
	 * Histogram of image.
	 * <p>
	 * Only values between the mininum and maximum values of the image are allowed.
	 */
	int hist[][] = null;

	/**
	 * Histogram of whole image
	 * <p>
	 * Only values between the mininum and maximum values of the image are allowed.
	 */
	int totalHist[] = null;

	/**
	 * Entropy of the image.
	 */
	double entropy[] = null;

	/**
	 * Entropy of whole image.
	 */
	double totalEntropy = 0.0D;
	
	/**
         * Number of zeros, by component
         */
	long[] numberOfZeros;
	/**
         * Total number of zeros
         */
	long totalNumberOfZeros;

        // MHC - 10/02/2012 - chi-square significance levels in the different configurations
        // To obtain the significance level of the z-th component and
        // the i-th bitplane, list[z][i] should be accessed.
        //
        // The following versions of the test are available:
        /// * considering each component and each bitplane separately
        float[][] allImageBitplaneSignificanceLevels = null;
        float[] totalChi2AllImageSignificance;
        /// * considering regions for each component and each bitplane separately
        /// [more info about regions: http://ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=126907]
        float[][] regionsBitplaneSignificanceLevels = null;
        float[] totalChi2RegionsSignificance;
        // * By-value chi-square significance
        float[] byValueSignificanceLevels; 
        float totalByValueSignificanceLevels;
        
        // MHC - 08/03/2012 - Bitplane entropy
        // To obtain the entropy of the i-th bitplane (BP0=LSbit) of the z-th component,
        // list[z][i] should be accessed
        float[][] bitplaneEntropy;
        float[] totalBitplaneEntropy;

        /**
         * Constructor that does all the operations to calculate min and max, average and center range of the image.
         *
         * By default, 16 bitplanes are considered for the chi-square test,
         * and regions are assumed 64x64.
         *
         * @param imageSamples a 3D float array that contains image samples
         * @param maskSamples a 3D byte array that contains the mask for each component in the input image
         */
        public ImageStatisticalSA(float[][][] imageSamples, byte[][][] maskSamples) {
            this(imageSamples, maskSamples, 16, 64);
        }

        /**
         * Constructor that does all the operations to calculate min and max, average and center range of the image.
         *
         * By default, regions are assumed 64x64.
         *
         * @param imageSamples a 3D float array that contains image samples
         * @param maskSamples a 3D byte array that contains the mask for each component in the input image
         * * @param bitdepth number of bitplanes to consider in the chi-square calculation
         */
        public ImageStatisticalSA(float[][][] imageSamples, byte[][][] maskSamples, int bitdepth) {
            this(imageSamples, maskSamples, bitdepth, 64);
        }

        /**
         * Constructor that does all the operations to calculate min and max, average and center range of the image.
         *
         * @param imageSamples a 3D float array that contains image samples
         * @param maskSamples a 3D byte array that contains the mask for each component in the input image
         * @param bitdepth number of bitplanes to consider in the chi-square calculation
         * @param regionSize size in pixels of each region on which the by-region Chi^2 test
         * is calculated
         */
        public ImageStatisticalSA(float[][][] imageSamples, byte[][][] maskSamples, int bitdepth, int regionSize) {
            this(imageSamples, maskSamples, bitdepth, regionSize, false);
        }

        /**
         * Constructor that does all the operations to calculate min and max, average and center range of the image.
         *
         * @param imageSamples a 3D float array that contains image samples
         * @param maskSamples a 3D byte array that contains the mask for each component in the input image
         * @param bitdepth number of bitplanes to consider in the chi-square calculation
         * @param regionSize size in pixels of each region on which the by-region Chi^2 test
         * is calculated
         */
        public ImageStatisticalSA(float[][][] imageSamples, byte[][][] maskSamples, int bitdepth, int regionSize, boolean chiVerbose) {
            this(imageSamples, maskSamples, bitdepth, regionSize, chiVerbose, -1);
        }

	/**
	 * Constructor that does all the operations to calculate min and max, average and center range of the image,
         * and allows being verbose calculating the Chi-Square test.
         *
	 * @param imageSamples a 3D float array that contains image samples
	 * @param maskSamples a 3D byte array that contains the mask for each component in the input image
         * @param bitdepth number of bitplanes to consider in the chi-square calculation
         * @param regionSize size in pixels of each region on which the by-region Chi^2 test
         * is calculated
         * @param chiVerbose be verbose when calculating the Chi-square test?
	 */
        public ImageStatisticalSA(float[][][] imageSamples, byte[][][] maskSamples, int bitdepth, int regionSize, boolean chiVerbose, int chiTheta) {
		//Size set
		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;

		//Memory allocation
		minMax = new float[zSize][2];
		energyDR = new double[zSize];
		averageDR = new double[zSize];
		averageNDR = new double[zSize];
		centerRange = new float[zSize];
		totalMinMax = new float[2];
		numberOfPixelsData = new long[zSize];
		numberOfPixelsNoData = new long[zSize];
		averageStdDevDR = new double[zSize];
		averageStdDevNDR = new double[zSize];
		averageStdDevDNDR = new double[zSize];
		variance = new double[zSize];
		regressionDR = new double [zSize-1];
		regressionNDR = new double [zSize-1];
                allImageBitplaneSignificanceLevels = new float[zSize][bitdepth];
                regionsBitplaneSignificanceLevels = new float[zSize][bitdepth];
                byValueSignificanceLevels = new float[zSize];
                bitplaneEntropy = new float[zSize][bitdepth];
                totalBitplaneEntropy = new float[bitdepth];
                numberOfZeros = new long[zSize];

		//Initializations
		totalMinMax[0] = Float.POSITIVE_INFINITY;
		totalMinMax[1] = Float.NEGATIVE_INFINITY;
		for(int z = 0; z < zSize; z++){
			minMax[z][0] = Float.POSITIVE_INFINITY;
			minMax[z][1] = Float.NEGATIVE_INFINITY;
			averageDR[z] = 0;
			averageNDR[z] = 0;
			variance[z] = 0;
		}
		// MHC - 10/02/2012 - support for chi-square significance level calculation
		/// Bitplane masks
                int[] bitplaneMask = new int[bitdepth];
		/// Total number of ones per bitplane - access: [z][bitplane]
		long[][] bitplaneOnesCount = new long[zSize][bitdepth];
                /// Total number of ones per bitplane and region - access [z][regionX][regionY][bitplane]
                /// Regions are set in a 2D array.
                //// Number of regions
                int regionXCount = (int) Math.ceil(((float)xSize)/regionSize);
                int regionYCount = (int) Math.ceil(((float)ySize)/regionSize);
                long [][][][] regionBitplaneOnesCount = new long[zSize][regionYCount][regionXCount][bitdepth];
                long [][][] regionNumberOfPixelsData = new long[zSize][regionYCount][regionXCount];
                for (int z = 0; z < zSize; z++) {
                    for (int i = 0; i < bitdepth; i++) {
                        bitplaneMask[i] = 1 << i;
                        bitplaneOnesCount[z][i] = 0;
                        for (int rx = 0; rx < regionXCount; rx++) {
                            for (int ry = 0; ry < regionYCount; ry++) {
                                regionBitplaneOnesCount[z][ry][rx][i] = 0;
                            }
                        }
                    }
                }

                // Ensures mask validity
                // MHC - 14/02/2012 - Allways iterated, needed for by-region Chi^2 test
                for (int z = 0; z < zSize; z++) {
                    for (int y = 0; y < ySize; y++) {
                        for (int x = 0; x < xSize; x++) {
                            if(maskSamples == null || maskSamples[z][y][x] != 0){
                                    //Updates the data pixels counter
                                    numberOfPixelsData[z]++;
                                    regionNumberOfPixelsData[z][y/regionSize][x/regionSize]++;
                            } else {
                                numberOfPixelsNoData[z]++;
                            }
                        }
                    }
                }

		//Calculus
		for(int z = 0; z < zSize; z++){
			//BigDecimal LaverageDR = new BigDecimal("0");

			for(int y = 0; y < ySize; y++){
				for(int x = 0; x < xSize; x++){
					if(maskSamples == null || maskSamples[z][y][x] != 0){
						//Min and max
						minMax[z][0] = Math.min(minMax[z][0], imageSamples[z][y][x]);
						minMax[z][1] = Math.max(minMax[z][1], imageSamples[z][y][x]);

						//Average of data region
						averageDR[z] += imageSamples[z][y][x];

                                                // MHC - 10/02/2012 - Bitplane number of ones count
                                                // for chi-square and bitplane entropy
                                                for (int i = 0; i < bitdepth; i++) {
                                                    if (((int) imageSamples[z][y][x] & bitplaneMask[i]) != 0) {
                                                        regionBitplaneOnesCount[z][y/regionSize][x/regionSize][i]++;
                                                        bitplaneOnesCount[z][i]++;
                                                    }
                                                }
					}else{
						//Average of no-data region
						averageNDR[z] += imageSamples[z][y][x];
					}
				}
			}

			totalMinMax[0] = Math.min(totalMinMax[0], minMax[z][0]);
			totalMinMax[1] = Math.max(totalMinMax[1], minMax[z][1]);

			averageDR[z] /= numberOfPixelsData[z];
			averageNDR[z] /= ((xSize * ySize) - numberOfPixelsData[z]);

			for(int y = 0; y < ySize; y++){
			for(int x = 0; x < xSize; x++){
				if(maskSamples == null || maskSamples[z][y][x] != 0){
					variance[z] += ((imageSamples[z][y][x] - averageDR[z]) * (imageSamples[z][y][x] - averageDR[z]));
				}
			}}
			variance[z] /= numberOfPixelsData[z];

		}

		//Standard Deviation and energy
		for(int z = 0; z < zSize; z++){
			//BigDecimal mess2 = new BigDecimal("0");

			for(int y = 0; y < ySize; y++){
			for(int x = 0; x < xSize; x++){
				if(maskSamples == null || maskSamples[z][y][x] != 0){
					averageStdDevDR[z] += Math.pow(averageDR[z] - imageSamples[z][y][x], 2);
					energyDR[z] += imageSamples[z][y][x] * imageSamples[z][y][x];
				}else{
					averageStdDevDNDR[z] += Math.pow(averageDR[z] - imageSamples[z][y][x], 2);
					averageStdDevNDR[z] += Math.pow(averageNDR[z] - imageSamples[z][y][x], 2);
				}
			}}

			averageStdDevDR[z] = Math.sqrt(averageStdDevDR[z] / numberOfPixelsData[z]);
			averageStdDevNDR[z] = Math.sqrt(averageStdDevNDR[z] / (xSize * ySize - numberOfPixelsData[z]));
			averageStdDevDNDR[z] = Math.sqrt(averageStdDevDNDR[z] / (xSize * ySize - numberOfPixelsData[z]));

			totalEnergyDR += energyDR[z];
		}

		//centerRange
		for(int z = 0; z < zSize; z++){
			centerRange[z] = (minMax[z][0] + minMax[z][1]) / 2;
		}
		double numeradorDR = 0;
		double numeradorNDR = 0;

		for(int z = 0; z < zSize-1; z++){
			numeradorDR = 0;
			numeradorNDR = 0;
			for(int y = 0; y < ySize; y++){
			for(int x = 0; x < xSize; x++){
				if(maskSamples == null || maskSamples[z][y][x] != 0){
					numeradorDR += ((imageSamples[z][y][x]-averageDR[z])*(imageSamples[z+1][y][x]-averageDR[z+1]));
				}else{
					numeradorNDR += ((imageSamples[z][y][x]-averageNDR[z])*(imageSamples[z+1][y][x]-averageNDR[z+1]));
				}
			}}

			regressionDR[z] = numeradorDR/((numberOfPixelsData[z]-1)*(averageStdDevDR[z] * averageStdDevDR[z+1]));
			regressionNDR[z] = numeradorNDR/((numberOfPixelsData[z]-1)*(averageStdDevNDR[z] * averageStdDevNDR[z+1]));
		}

		// MHC - 10/02/2012 - Chi-square calculation
		/// Whole component chi-square calculation
		for (int z = 0; z < zSize; z++) {
                    for (int i = 0; i < bitdepth; i++) {
                        float classZeroObserved = numberOfPixelsData[z] - bitplaneOnesCount[z][i];
                        float classZeroExpected = ((float)(numberOfPixelsData[z])) / 2;
                        float classOneObserved = bitplaneOnesCount[z][i];
                        float classOneExpected = ((float)(numberOfPixelsData[z])) / 2;

                        double X2 = Math.pow(classZeroObserved - classZeroExpected, 2) / classZeroExpected
                                + Math.pow(classOneObserved - classOneExpected, 2) / classOneExpected;
                        double chi2significance = ChiSquare.getSignificanceLevel(1, X2);

                        if (chiVerbose == true) {
                            System.out.println(">>>>>> z="+z+" bitplane#"+i+": Whole component");
                            System.out.println("    Zeros observed/expected(%) = "
                                               + classZeroObserved + "/" + classZeroExpected
                                               + "(" + ((100.0*classZeroObserved)/classZeroExpected) + "%)");
                            System.out.println("    X2="+X2);
                            System.out.printf("    Chi-square CV for 0.05/0.01 = %.5f/%.5f\n",
                                              ChiSquare.getCriticalValue(1, 0.05),
                                              ChiSquare.getCriticalValue(1, 0.01));
                        }

                        allImageBitplaneSignificanceLevels[z][i] = (float) chi2significance;
                    }
                }
                /// By-region chi-square calculation
                if (regionXCount * regionYCount > 1) {
                    for (int z = 0; z < zSize; z++) {
                        for (int i = 0; i < bitdepth; i++) {
                            double X2 = 0;

                            int skippedRegions = 0;
                            for (int ry = 0; ry < regionYCount; ry++) {
                                for (int rx = 0; rx < regionXCount; rx++) {
                                    if (regionNumberOfPixelsData[z][ry][rx] == 0) {
                                        // Regions without pixels are not taken into account
                                        skippedRegions++;
                                        continue;
                                    }
                                    float onesObserved = regionBitplaneOnesCount[z][ry][rx][i];
                                    float onesExpected = ((float)regionNumberOfPixelsData[z][ry][rx]) / 2;
                                    X2 += Math.pow(onesObserved - onesExpected, 2) / onesExpected;
                                }
                            }

                            if (chiVerbose == true) {
                                System.out.println(">>>>>> z="+z+" bitplane#"+i+": By region");
                                System.out.println("    Regions (total/skipped)=("+(regionXCount*regionYCount)+"/"+skippedRegions+")");
                                System.out.println("    X2="+X2);
                                System.out.printf("    Chi-square CV alpha=0.05/0.01 = %.5f/%.5f\n",
                                              ChiSquare.getCriticalValue(regionXCount*regionYCount - skippedRegions - 1, 0.05),
                                              ChiSquare.getCriticalValue(regionXCount*regionYCount - skippedRegions - 1, 0.01));
                            }

                            double chi2significance = ChiSquare.getSignificanceLevel(regionXCount*regionYCount - skippedRegions - 1, X2);
                            regionsBitplaneSignificanceLevels[z][i] = (float) chi2significance;
                        }
                    }
                } else {
                    for (int z = 0; z < zSize; z++) {
                        for (int i = 0; i < bitdepth; i++) {
                            regionsBitplaneSignificanceLevels[z][i] = allImageBitplaneSignificanceLevels[z][i];
                        }
                    }
                }

		//Totals
		totalAverageDR = 0F;
		totalAverageNDR = 0F;
		totalCenterRange = 0F;
		totalAverageStdDevDR = 0F;
		totalAverageStdDevNDR = 0F;
		totalAverageStdDevDNDR = 0F;
		totalNumberOfPixelsData = 0;
		totalNumberOfPixelsNoData = 0;
		totalRegressionDR = 0F;
		totalRegressionNDR = 0F;
                totalChi2AllImageSignificance = new float[bitdepth];
                totalChi2RegionsSignificance = new float[bitdepth];
                for(int i = 0; i < bitdepth; i++){
                    totalChi2AllImageSignificance[i] = 0;
                    totalChi2RegionsSignificance[i] = 0;
                }

		for(int z = 0; z < zSize; z++){
			totalAverageDR += averageDR[z];
			totalCenterRange += centerRange[z];
			totalAverageStdDevDR += averageStdDevDR[z];
			totalNumberOfPixelsData += numberOfPixelsData[z];
			totalNumberOfPixelsNoData += numberOfPixelsNoData[z];
                        for (int i = 0; i < bitdepth; i++) {
                            totalChi2AllImageSignificance[i] += allImageBitplaneSignificanceLevels[z][i];
                            totalChi2RegionsSignificance[i] += regionsBitplaneSignificanceLevels[z][i];
                        }
		}
		for(int z = 0; z < zSize-1; z++){
			totalRegressionDR += regressionDR[z];
			totalRegressionNDR += regressionNDR[z];
		}
		totalAverageDR /= zSize;
		totalAverageNDR /= zSize;
		totalCenterRange /= zSize;
		totalAverageStdDevDR /= zSize;
		totalAverageStdDevNDR /= zSize;
		totalAverageStdDevDNDR /= zSize;
		totalRegressionDR /= zSize-1;
		totalRegressionNDR /= zSize-1;
                for (int i = 0; i < bitdepth; i++) {
                    totalChi2AllImageSignificance[i] /= zSize;
                    totalChi2RegionsSignificance[i] /= zSize;
                }

		// Histogram, totalVariance, numberOfZeros
		hist = new int[zSize][];
		totalHist = new int[(int)totalMinMax[1] - (int)totalMinMax[0] + 1];
		int totalMin = (int)totalMinMax[0];
		for(int z = 0; z < zSize; z++) {
			int min = (int)minMax[z][0];
                        
                        // histogram, totalVariance
			hist[z] = new int[(int)minMax[z][1] - (int)minMax[z][0] + 1];
			for (int i = 0; i < hist[z].length; i++) {
				hist[z][i] = 0;
			}
			for(int y = 0; y < ySize; y++) {
			for(int x = 0; x < xSize; x++){
				if(maskSamples == null || maskSamples[z][y][x] != 0){
					totalVariance += (imageSamples[z][y][x] - totalAverageDR) * (imageSamples[z][y][x] - totalAverageDR);
					hist[z][(int)imageSamples[z][y][x]-min]++;
					totalHist[(int)imageSamples[z][y][x]-totalMin]++;
				}
			}}
			
			// Number of zeros
			if (min <= 0) {
                            numberOfZeros[z] = hist[z][0-min];
                        } else {
                            numberOfZeros[z] = 0;
                        }
		}
		totalVariance /= totalNumberOfPixelsData;
		if (totalMin <= 0){
                    totalNumberOfZeros = totalHist[0-totalMin];
		} else {
                    totalNumberOfZeros = 0;
                }

                // MHC - 05/03/2012 - by-value chi-square (using histogram calculation)
                // Uniformity in [0,chiTheta] is tested. If chiTheta is not specified,
                // the maximum value found in the image will be used
                totalByValueSignificanceLevels = 0;
                for(int z = 0; z < zSize; z++) {
    				// Identification of the [0, theta] interval
                    int maxFound = (int)minMax[z][1];
                    int theta = chiTheta;
                    if (theta < 0) {
                        theta = maxFound;
                    }
                    // Count of pixels <= theta
                    int pixelsLETheta = 0;
                    int min = (int)minMax[z][0];
                    for (int i = min; i <= theta; i++) {
                        pixelsLETheta += hist[z][i-min];
                    }
                    // Expected frequency for each value in [0,theta]
                    // (there are theta+1 classes)
                    float expectedFrequency = ((float)pixelsLETheta) / ((float)theta + 1);
                    // X^2 calculation
                    float X2 = 0;
                    for (int i = min; i <= theta; i++) {
                        X2 += ((hist[z][i-min] - expectedFrequency)*(hist[z][i-min] - expectedFrequency))/expectedFrequency;
                    }
                    byValueSignificanceLevels[z] = (float) ChiSquare.getSignificanceLevel(theta, X2);
                    if (chiVerbose == true) {
                        System.out.println(">>>>>> byValueSignificanceLevels["+z+"]="+byValueSignificanceLevels[z]);
                        System.out.println("\tX2="+X2);
                        System.out.printf("\tcritical k="+theta+ " 0.05/0.01 = %.3f/%.3f\n",
                                        ChiSquare.getCriticalValue(theta, 0.05),
                                        ChiSquare.getCriticalValue(theta, 0.01));
                    }
                    totalByValueSignificanceLevels += byValueSignificanceLevels[z];
                }
                totalByValueSignificanceLevels /= zSize;

		// Whole-component entropy
		entropy = new double[zSize];
		for(int z = 0; z < zSize; z++) {
			entropy[z] = 0.0D;
			for (int i = 0; i < hist[z].length; i++) {
				double p = ((double)hist[z][i]) / (numberOfPixelsData[z]);
				if (p > 0.0D) {
					entropy[z] -= p * Math.log(p);
				}
			}
			entropy[z] /= Math.log(2.0D);
		}
                // Average whole-component entropy
		totalEntropy = 0.0D;
		for (int i = 0; i < totalHist.length; i++) {
			double totalP = ((double)totalHist[i]) / (totalNumberOfPixelsData);
			if (totalP > 0.0D) {
				totalEntropy -= totalP * Math.log(totalP);
			}
		}
		totalEntropy /= Math.log(2);
                
                // MHC - 08/03/2012 - Bitplane entropy
                bitplaneEntropy = new float[zSize][bitdepth];
                for (int z = 0; z < zSize; z++) {
                    for (int i = 0; i < bitdepth; i++) {
                        double n = numberOfPixelsData[z];
                        double n1 = bitplaneOnesCount[z][i];
                        double n0 = n - n1;
                        bitplaneEntropy[z][i] = 0;
                        if (n0 != 0 && n1 != 0) {
                            // If n0 or n1 is 0, entropy is 0
                            double p0 = n0/n;
                            double p1 = n1/n;
                            bitplaneEntropy[z][i] = (float) (-1 * (p0*Math.log(p0) + p1*Math.log(p1)));
                            // We want entropy in bits, need log_2
                            bitplaneEntropy[z][i] /= Math.log(2D);
                        }
                    }
                }
                // MHC - 08/03/2012 - Average bitplane entropy
                totalBitplaneEntropy = new float[bitdepth];
                for (int i = 0; i < bitdepth; i++) {
                    totalBitplaneEntropy[i] = 0;
                    for (int z = 0; z < zSize; z++) {
                        totalBitplaneEntropy[i] += bitplaneEntropy[z][i];
                    }
                    totalBitplaneEntropy[i] /= zSize;
                }
	}

	public double[][] jointChiTest(float[][][] imageSamples, byte[][][] maskSamples){
		//Size set
				int zSize = imageSamples.length;
				int ySize = imageSamples[0].length;
				int xSize = imageSamples[0][0].length;

				int categories = xSize*ySize/8*8;
				int xOffset = 0;
				int yOffset = 0;
				//float metric[][] = null;
				int BPnum = 0;
				int b = 0;
				int w = 0;
				int[][] BW = new int[zSize][];
				int[][] noBW = new int[zSize][];
				int N = xSize*ySize;
				float S0 = 0;
				float S1 = 0;
				float S2 = 0;
				float T0 = 0;
				float T1 = 0;
				float T2 = 0;
				double[][] mean = new double[zSize][];
				double[][] desv = new double[zSize][];
				double [][] Z= new double[zSize][];


				
				for(int z=0;z<imageSamples.length;z++){
					BPnum = (int) (Math.log((double)this.getMinMax()[z][1])/Math.log(2.0))+1;
					System.out.println("Num BP: "+BPnum);
					if (BPnum > 0){
						BW[z]= new int[BPnum];
						noBW[z]= new int[BPnum];
						Z[z] = new double[BPnum];
						mean[z] = new double[BPnum];
						desv[z] = new double[BPnum];
					}else{
						return Z;
					}

					for(int i=0;i<BPnum;i++){

						/*----Bit Plane Z calculation----*/
						for (int k=0;k<categories;k++){

							BW[z][i]=0;
							noBW[z][i]=0;

							//S* calculation
							S0 = 2*(2*ySize*xSize - ySize - xSize);
							S1 = 2*S0;
							S2 = 8*(8*ySize*xSize - 7*ySize - 7*xSize +4);
							System.out.println("S0: "+S0);
							System.out.println("S1: "+S1);
							System.out.println("S2: "+S2);
							//BW, b and w computation

							xOffset=(8*k)%xSize;
							yOffset=8*(k/(xSize/8));

							for(int y=0;y<8;y++){
								for(int x=0;x<8-1;x++){
									if (((int)imageSamples[z][y+yOffset][x+xOffset] & (int)Math.pow(2,i)) != ((int)imageSamples[z][y+yOffset][x+xOffset+1] & (int)Math.pow(2,i))){
						//				System.out.println("BitPlaneH Values:" +Integer.toBinaryString((int)imageSamples[z][y][x] & (int)Math.pow(2,i))+" "+Integer.toBinaryString((int)imageSamples[z][y][x+1] & (int)Math.pow(2,i)));
										BW[z][i]++;
									}else{
										noBW[z][i]++;
									}
									if (((int)imageSamples[z][y+yOffset][x+xOffset] & (int)Math.pow(2,i)) == (int)Math.pow(2,i)){
										w++;
									}else{
										b++;
									}
								}
								if (((int)imageSamples[z][y+yOffset][8-1+xOffset] & (int)Math.pow(2,i)) == (int)Math.pow(2,i)){
									w++;
								}else{
									b++;
								}

							}
							for(int y=0;y<imageSamples[z].length-1;y++){
								for(int x=0;x<imageSamples[z][y].length;x++){
                                                                        try {
                                                                            if (((int)imageSamples[z][y+yOffset][x+xOffset] & (int)Math.pow(2,i)) != ((int)imageSamples[z][y+yOffset+1][x+xOffset] & (int)Math.pow(2,i))){
                                            //					System.out.println("BitPlaneV Values:" +Integer.toBinaryString((int)imageSamples[z][y][x] & (int)Math.pow(2,i))+" "+Integer.toBinaryString((int)imageSamples[z][y+1][x] & (int)Math.pow(2,i)));
                                                                                    BW[z][i]++;
                                                                            }else{
                                                                                    noBW[z][i]++;
                                                                            }
                                                                        } catch (ArrayIndexOutOfBoundsException ex) {
                                                                            System.out.println("IndexOutOfBounds. x=" + x + ", y=" + y);
                                                                        }
								}

							}
							System.out.println("b: "+b);
							System.out.println("w: "+w);


							//T* calculation
							T0 = 2*b*w;
							T1 = 2*T0;
							T2 = N*b*w;

							System.out.println("T0: "+T0);
							System.out.println("T1: "+T1);
							System.out.println("T2: "+T2);

							//Mean,variance and Z calculation
							mean[z][i] = S0*T0 / (2*N*(N-1));
							desv[z][i] = ((S0 - 2*S1)*(T0 -2*T1))/(16*N*(N-1)*(N-2))+(S1*T1)/(8*N*(N-1))+((Math.pow(S0,2)+S1-S2)*((Math.pow(T0,2)+T1-T2)))/(4*N*(N-1)*(N-2)*(N-3))-Math.pow(mean[z][i],2);
							Z[z][i] = (BW[z][i]-mean[z][i])/Math.sqrt(desv[z][i]);
							System.out.println("BW: "+BW[z][i]);
							System.out.println("mean: "+mean[z][i]);
							System.out.println("desv: "+desv[z][i]);
							System.out.println("Z: "+Z[z][i]);
							b=0;
							w=0;
						}
					}
				}
				return Z;
	}

	/**
	 * @return minMax definition in this class
	 */
	public float[][] getMinMax(){
		return(minMax);
	}

	/**
	 * @return totalMinMax definition in this class
	 */
	public float[] getTotalMinMax(){
		return(totalMinMax);
	}

	/**
	 * @return Energy definition in this class
	 */
	public double[] getEnergyDR(){
		return(energyDR);
	}

	/**
	 * @return totalEnergy definition in this class
	 */
	public double getTotalEnergyDR(){
		return(totalEnergyDR);
	}

	/**
	 * @return averageDR definition in this class
	 */
	public double[] getAverageDR(){
		return(averageDR);
	}

	/**
	 * @return totalAverageDR definition in this class
	 */
	public double getTotalAverageDR(){
		return(totalAverageDR);
	}

	/**
	 * @return averageNDR definition in this class
	 */
	public double[] getAverageNDR(){
		return(averageNDR);
	}

	/**
	 * @return totalAverageNDR definition in this class
	 */
	public double getTotalAverageNDR(){
		return(totalAverageNDR);
	}

	/**
	 * @return averageStdDevDR definition in this class
	 */
	public double[] getAverageStdDevDR(){
		return(averageStdDevDR);
	}

	/**
	 * @return totalAverageStdDevDR definition in this class
	 */
	public double getTotalAverageStdDevDR(){
		return(totalAverageStdDevDR);
	}

	/**
	 * @return averageStdDevNDR definition in this class
	 */
	public double[] getAverageStdDevNDR(){
		return(averageStdDevNDR);
	}

	/**
	 * @return totalAverageStdDevDR definition in this class
	 */
	public double getTotalAverageStdDevNDR(){
		return(totalAverageStdDevNDR);
	}

	/**
	 * @return averageStdDevDNDR definition in this class
	 */
	public double[] getAverageStdDevDNDR(){
		return(averageStdDevDNDR);
	}

	/**
	 * @return totalAverageStdDevDR definition in this class
	 */
	public double getTotalAverageStdDevDNDR(){
		return(totalAverageStdDevDNDR);
	}

	/**
	 * @return centerRange definition in this class
	 */
	public float[] getCenterRange(){
		return(centerRange);
	}

	/**
	 * @return totalCenterRange definition in this class
	 */
	public float getTotalCenterRange(){
		return(totalCenterRange);
	}

	/**
	 * @return numberOfPixelsData definition in this class
	 */
	public long[] getNumberOfPixelsData(){
		return(numberOfPixelsData);
	}

	/**
	 * @return numberOfPixelsNoData definition in this class
	 */
	public long[] getNumberOfPixelsNoData(){
		return(numberOfPixelsNoData);
	}

	/**
	 * @return totalNumberOfPixelsData definition in this class
	 */
	public long getTotalNumberOfPixelsData(){
		return(totalNumberOfPixelsData);
	}
	/**
	 * @return totalNumberOfPixelsNoData definition in this class
	 */
	public long getTotalNumberOfPixelsNoData(){
		return(totalNumberOfPixelsNoData);
	}

	/**
	 * @return Variance definition in this class
	 */
	public double[] getVariance(){
		return(variance);
	}

	/**
	 * @return totalVariance definition in this class
	 */
	public double getTotalVariance(){
		return((float)totalVariance);
	}

	/**
	 * @return entropy definition in this class.
	 */
	public double[] getEntropy() {
		return(entropy);
	}

	/**
	 * @return totalEntropy definition in this class.
	 */
	public double getTotalEntropy() {
		return(totalEntropy);
	}

	/**
	 * @return regression definition in this class.
	 */

	public double[] getRegressionDR(){
		return(regressionDR);
	}

	/**
	 * @return regression definition in this class.
	 */

	public double[] getRegressionNDR(){
		return(regressionNDR);
	}

	/**
	 * @return total regression definition in this class.
	 */

	public double getTotalRegressionDR(){
		return(totalRegressionDR);
	}

	/**
	 * @return total regression definition in this class.
	 */

	public double getTotalRegressionNDR(){
		return(totalRegressionNDR);
	}

        // MHC - 10/02/2012 - different Chi-Square significance level calculation
        /**
         * Get the significance levels of the Chi-Square test
         * (against a uniform distribution) considering
         * the whole image (without dividing into regions) and
         * each bitplane independently.
         * The significance level is the stimated probability of
         * an uniform distribution test sample being "less uniform" than
         * the image data. If lower than 0.05 (and possibly greater than 0.99),
         * H_0 can be rejected: the image data does not show an uniform random distribution.
         * @see http://www.stat.yale.edu/Courses/1997-98/101/chigf.htm
         *
         * @return a list of lists with the significance level for each
         * component and bitplane.
         * To obtain the significance level of the z-th component and
         * the i-th bitplane, list[z][i] should be accessed.
         */
        public float[][] getAllImageBitplaneSignificanceLevels() {
            return allImageBitplaneSignificanceLevels;
        }

        /**
         * Get the average value of getAllImageBitplaneSignificanceLevels()
         * for each component.
         */
        public float[] getTotalAllImageBitplaneSignificanceLevels() {
            return totalChi2AllImageSignificance;
        }

        /**
         * Get the significance levels of the Chi-Square test
         * (against a uniform distribution) considering
         * regions (for the specified region size)
         * [more info about regions: http://ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=126907]
         * and each bitplane independently.
         * The significance level is the stimated probability of
         * an uniform distribution test sample being "less uniform" than
         * the image data. If lower than 0.05 (and possibly greater than 0.99),
         * H_0 can be rejected: the image data does not show an uniform random distribution.
         * @see http://www.stat.yale.edu/Courses/1997-98/101/chigf.htm
         *
         * @return a list of lists with the significance level for each
         * component and bitplane.
         * To obtain the significance level of the z-th component and
         * the i-th bitplane, list[z][i] should be accessed.
         */
        public float[][] getRegionsBitplaneSignificanceLevels() {
            return regionsBitplaneSignificanceLevels;
        }

        /**
         * Get the average value of getRegionsBitplaneSignificanceLevels()
         * for each component
         */
        public float[] getTotalRegionsBitplaneSignificanceLevels() {
            return totalChi2RegionsSignificance;
        }
        
        
        /**
         * Get the significance levels of the Chi-Square test for uniformity
         * (against a uniform distribution) considering
         * pixel values from each component
         *
         * @return a list with the significance levels for the chi-square test 
         * for each component
         */
        public float[] getByValueSignificanceLevels() {
            return byValueSignificanceLevels;
        }
        
        /**
         * Get the average significance level of the Chi-Square test for uniformity
         * (against a uniform distribution) considering
         * pixel values from each component
         *
         * @return the average by-value chi-square significance of the different components
         */
        public float getTotalByValueSignificanceLevels() {
            return totalByValueSignificanceLevels;
        }
        
        /**
         * Get te bitplane entropy of each bitplane of each component
         */
        public float[][] getBitplaneEntropy() {
            return bitplaneEntropy;
        }
        
        /**
         * Get the AVERAGE bitplane entropy of each bitplane of the whole image
         */
        public float[] getAverageBitplaneEntropy() {
            return totalBitplaneEntropy;
        }
        
        /**
         * Get the number of zeros of each component
         */
        public long[] getNumberOfZeros() {
            return numberOfZeros;
        }
        
        /**
         * Get the total number of zeros of the image
         */
        public long getTotalNumberOfZeros() {
            return totalNumberOfZeros;
        }
        
        /**
         * Get the total number of zeros of the image
         */
        public int[][] getImageHist() {
            return hist;
        }
}
