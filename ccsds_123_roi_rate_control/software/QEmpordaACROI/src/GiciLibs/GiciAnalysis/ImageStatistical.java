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

import java.util.HashMap;
import java.util.Map;


/**
 * This class receives an image and calculates some statistical information about the image.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public class ImageStatistical{
	
	/**
	 * Array where min (minMax[component][0]) and max (minMax[component][1]) values of each component will be stored.
	 * <p>
	 * All values allowed.
	 */
	double minMax[][] = null;

	/**
	 * Global min (minMax[0]) and max (minMax[1]) values of the image.
	 * <p>
	 * All values allowed.
	 */
	double totalMinMax[] = null;

	/**
	 * Average of each component.
	 * <p>
	 * All values allowed.
	 */
	double average[] = null;

	/**
	 * Average of whole image.
	 * <p>
	 * All values allowed.
	 */
	double totalAverage=0;

	/**
	 * Center of range for each image component.
	 * <p>
	 * All values allowed.
	 */
	double centerRange[] = null;

	/**
	 * Center of range of whole image.
	 * <p>
	 * All values allowed.
	 */
	double totalCenterRange=0;

	/**
	 * Array where are stored the how many times is appeared all values in Byte image.
	 * <p>
	 * All values allowed-
	 */
	int[][] countedValues = null;
	
	/**
	 * Energy of each component.
	 * <p>
	 * All values allowed.
	 */
	double energy[] = null;
	
	/**
	 * Energy of whole image.
	 * <p>
	 * All values allowed.
	 */
	double totalEnergy = 0;
	
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

        // MHC-24/11/2011
        /**
         * Moran's I measure for the image
         */
        double morans[] = null;
        /**
         * Moran's I measure for the whole image 
         */
        double totalMorans = 0.0D;
        // --
	
	/**
	 * Constructor that does all the operations to calculate min and max, average and center range of the image.
	 *
	 * @param imageSamples a 3D float array that contains image samples
	 */
	public ImageStatistical(float[][][] imageSamples){
		//Size set
		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;

		//Memory allocation
		minMax = new double[zSize][2];
		average = new double[zSize];
		centerRange = new double[zSize];
		totalMinMax = new double[2];
		countedValues = new int[zSize][256];
		energy = new double[zSize];
		variance = new double[zSize];
                morans = new double[zSize];
		//Initializations
		totalMinMax[0] = Float.POSITIVE_INFINITY;
		totalMinMax[1] = Float.NEGATIVE_INFINITY;
		for(int z = 0; z < zSize; z++){
			minMax[z][0] = Float.POSITIVE_INFINITY;
			minMax[z][1] = Float.NEGATIVE_INFINITY;
			average[z] = 0;
			energy[z] = 0;
			variance[z] = 0;
                        morans[z] = 0;
		}
		
		
		//Calculus
		for(int z = 0; z < zSize; z++){
			for(int y = 0; y < ySize; y++){
				for(int x = 0; x < xSize; x++){
					energy[z] += (imageSamples[z][y][x] * imageSamples[z][y][x]);
					//to count values
					if(Math.abs(imageSamples[z][y][x]) < 256){
						countedValues[z][(int)Math.abs(imageSamples[z][y][x])]++;
					}
					//Min and max
					if(imageSamples[z][y][x] < minMax[z][0]){
						minMax[z][0] = imageSamples[z][y][x];
						if(imageSamples[z][y][x] < totalMinMax[0]){
							totalMinMax[0] = imageSamples[z][y][x];
						}
					}
					if(imageSamples[z][y][x] > minMax[z][1]){
						minMax[z][1] = imageSamples[z][y][x];
						if(imageSamples[z][y][x] > totalMinMax[1]){
							totalMinMax[1] = imageSamples[z][y][x];
						}
					}
					//Average
					average[z] += imageSamples[z][y][x];
				}
			}
			average[z] /= (xSize*ySize);
			
			
                        // MHC-24/11/2011
                        // Moran's I measure (see http://en.wikipedia.org/wiki/Moran%27s_I)
                        // (uses calculated average values)
                        // Using neighbour matrix [1,1,1; 1,0,1; 1,1,1]
                        /// Total number of ones
                        int nOnes = 8;
                        /// Number of ones for the side pixels
                        int nOnesSide = 5;
                        /// Number of ones for the corner pixels
                        int nOnesCorner = 3;
                        /// \sum_{i,j} w_{i,j} in the formula
                        int totalNeighbours = 
                            (xSize-1)*(ySize-1)*nOnes +
                            (2*(xSize-2) + 2*(ySize-2))*nOnesSide +
                            4*nOnesCorner;
			for(int y = 0; y < ySize; y++){
				for(int x = 0; x < xSize; x++){
					variance[z] += ((imageSamples[z][y][x] - average[z]) * (imageSamples[z][y][x] - average[z]));
                                        
                                        // Accumulator for the numerator for this pixel
                                        double localMorans = 0;
                                        double thisDifference = imageSamples[z][y][x] - average[z];
                                        if (x > 0) {
                                            // W
                                            localMorans += thisDifference * (imageSamples[z][y][x-1] - average[z]);
                                        }
                                        if (x < xSize - 1) {
                                            // E
                                            localMorans += thisDifference * (imageSamples[z][y][x+1] - average[z]);
                                        }
                                        if (y > 0) {
                                            // N
                                            localMorans += thisDifference * (imageSamples[z][y-1][x] - average[z]);
                                        }
                                        if (y < ySize - 1) {
                                            // S
                                            localMorans += thisDifference * (imageSamples[z][y+1][x] - average[z]);
                                        }
                                        if (x > 0 && y > 0) {
                                            // NW
                                            localMorans += thisDifference * (imageSamples[z][y-1][x-1] - average[z]);
                                        }
                                        if (x < xSize -1 && y > 0) {
                                            // NE
                                            localMorans += thisDifference * (imageSamples[z][y-1][x+1] - average[z]);
                                        }
                                        if (x < xSize - 1 && y < ySize - 1) {
                                            // SE
                                            localMorans += thisDifference * (imageSamples[z][y+1][x+1] - average[z]);
                                        }
                                        if (x > 0 && y < ySize - 1) {
                                            // SW
                                            localMorans += thisDifference * (imageSamples[z][y+1][x-1] - average[z]);
                                        }
                                        morans[z] += localMorans;  
				}
			}
			morans[z] *= xSize*ySize;
                        morans[z] /= (double) totalNeighbours;
                        if (variance[z] != 0) {
                            // Avoid DIV/0 problem
                            morans[z] /= variance[z]; // This is \sum_i (x_i - \bar{x})^2, before dividing
                        } else {
                            // Constant image: Morans'I will be set to 1
                            morans[z] = 1;
                        }
			variance[z] /= (imageSamples[z].length * imageSamples[z][0].length);
                        // --
		}
		
		//centerRange, totalVariance
		for(int z = 0; z < zSize; z++){
			centerRange[z] = (minMax[z][0] + minMax[z][1]) / 2;
		}
		
		//Totals
		totalAverage = 0F;
		totalCenterRange = 0F;
                totalMorans = 0F;
		for(int z = 0; z < zSize; z++){
			totalAverage += average[z];
			totalCenterRange += centerRange[z];
			totalEnergy += energy[z];
                        totalMorans += morans[z];
		}
		totalAverage /= zSize;
		totalCenterRange /= zSize;
                totalMorans /= zSize;
		
		// Histogram, totalVariance
		Map<Integer,Integer> totalHist = new HashMap<Integer,Integer>();
//		hist = new int[zSize][];
//		totalHist = new int[(int)totalMinMax[1] - (int)totalMinMax[0] + 1];
//		int totalMin = (int)totalMinMax[0];
		
//		 Entropy
		entropy = new double[zSize];
		
		for(int z = 0; z < zSize; z++) {
			Map<Integer,Integer> hist = new HashMap<Integer,Integer>();
			
//			int min = (int)minMax[z][0];
//			hist[z] = new int[(int)minMax[z][1] - (int)minMax[z][0] + 1];
//			for (int i = 0; i < hist[z].length; i++) {
//				hist[z][i] = 0;
//			}
			for(int y = 0; y < ySize; y++) {
				for(int x = 0; x < xSize; x++){
					totalVariance += (imageSamples[z][y][x] - totalAverage) * (imageSamples[z][y][x] - totalAverage);
					
					int key = (int) imageSamples[z][y][x];
					
					if(hist.containsKey(key)) {
						hist.put(key, (hist.get(key) + 1));
					} else {
						hist.put(key, 1);
					}
					
					if(totalHist.containsKey(key)) {
						totalHist.put(key, (totalHist.get(key) + 1));
					} else {
						totalHist.put(key, 1);
					}
					
//					hist[z][-min]++;
//					totalHist[(int)imageSamples[z][y][x]-totalMin]++;
				}
			}
			
			entropy[z] = 0.0D;
			for (int key : hist.keySet()) {
				double p = ((double)hist.get(key)) / (ySize*xSize);
				if (p > 0.0D) {
					entropy[z] -= p * Math.log(p);
				}
			}
			entropy[z] /= Math.log(2.0D);
		}
		
		totalVariance /= (imageSamples.length * imageSamples[0].length * imageSamples[0][0].length);
		
		totalEntropy = 0.0D;
		for (int key : totalHist.keySet()) {
			double totalP = ((double)totalHist.get(key)) / (zSize*ySize*xSize);
			if (totalP > 0.0D) {
				totalEntropy -= totalP * Math.log(totalP);
			}
		}
		totalEntropy /= Math.log(2);

	}

	/**
	 * Constructor that does all the operations to calculate min and max, average and center range of the image.
	 *
	 * @param imageSamples a 3D float array that contains image samples
	 */
	public ImageStatistical(int[][][] imageSamples){
		//Size set
		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;
		System.out.println("bbb");
		
		//Memory allocation
		minMax = new double[zSize][2];
		countedValues = new int[zSize][256];
		average = new double[zSize];
		centerRange = new double[zSize];
		totalMinMax = new double[2];
		double E[] = new double[zSize];
		double totalE = 0;
		//Initializations
		totalMinMax[0] = Float.POSITIVE_INFINITY;
		totalMinMax[1] = Float.NEGATIVE_INFINITY;
		for(int z = 0; z < zSize; z++){
			minMax[z][0] = Float.POSITIVE_INFINITY;
			minMax[z][1] = Float.NEGATIVE_INFINITY;
			average[z] = 0;
			E[z] = 0;
		}
		//Calculus
		for(int z = 0; z < zSize; z++){
			for(int y = 0; y < ySize; y++){
				for(int x = 0; x < xSize; x++){
					energy[z] += (imageSamples[z][y][x] * imageSamples[z][y][x]);
					//to count values
					countedValues[z][(int)imageSamples[z][y][x]]++;
					//Min and max
					if(imageSamples[z][y][x] < minMax[z][0]){
						minMax[z][0] = imageSamples[z][y][x];
						if(imageSamples[z][y][x] < totalMinMax[0]){
							totalMinMax[0] = imageSamples[z][y][x];
						}
					}
					if(imageSamples[z][y][x] > minMax[z][1]){
						minMax[z][1] = imageSamples[z][y][x];
						if(imageSamples[z][y][x] > totalMinMax[1]){
							totalMinMax[1] = imageSamples[z][y][x];
						}
					}
					//Average
					average[z] += (imageSamples[z][y][x] / (xSize*ySize));
				}

			}
			E[z] /= (imageSamples[z].length * imageSamples[z][0].length);
			totalE += E[z];
			
                        // MHC-24/11/2011
                        // Moran's I measure (see http://en.wikipedia.org/wiki/Moran%27s_I)
                        // (uses calculated average values)
                        // Using neighbour matrix [1,1,1; 1,0,1; 1,1,1]
                        /// Total number of ones
                        int nOnes = 8;
                        /// Number of ones for the side pixels
                        int nOnesSide = 5;
                        /// Number of ones for the corner pixels
                        int nOnesCorner = 3;
                        /// \sum_{i,j} w_{i,j} in the formula
                        int totalNeighbours = 
                            (xSize-1)*(ySize-1)*nOnes +
                            (2*(xSize-2) + 2*(ySize-2))*nOnesSide +
                            4*nOnesCorner;
			for(int y = 0; y < ySize; y++){
				for(int x = 0; x < xSize; x++){
					variance[z] = (imageSamples[z][y][x] - E[z]) * (imageSamples[z][y][x] - E[z]);
                                        // Accumulator for the numerator for this pixel
                                        double localMorans = 0;
                                        double thisDifference = imageSamples[z][y][x] - average[z];
                                        if (x > 0) {
                                            // W
                                            localMorans += thisDifference * (imageSamples[z][y][x-1] - average[z]);
                                        }
                                        if (x < xSize - 1) {
                                            // E
                                            localMorans += thisDifference * (imageSamples[z][y][x+1] - average[z]);
                                        }
                                        if (y > 0) {
                                            // N
                                            localMorans += thisDifference * (imageSamples[z][y-1][x] - average[z]);
                                        }
                                        if (y < ySize - 1) {
                                            // S
                                            localMorans += thisDifference * (imageSamples[z][y+1][x] - average[z]);
                                        }
                                        if (x > 0 && y > 0) {
                                            // NW
                                            localMorans += thisDifference * (imageSamples[z][y-1][x-1] - average[z]);
                                        }
                                        if (x < xSize -1 && y > 0) {
                                            // NE
                                            localMorans += thisDifference * (imageSamples[z][y-1][x+1] - average[z]);
                                        }
                                        if (x < xSize - 1 && y < ySize - 1) {
                                            // SE
                                            localMorans += thisDifference * (imageSamples[z][y+1][x+1] - average[z]);
                                        }
                                        if (x > 0 && y < ySize - 1) {
                                            // SW
                                            localMorans += thisDifference * (imageSamples[z][y+1][x-1] - average[z]);
                                        }
                                        morans[z] += localMorans;
				}
			}
			morans[z] *= xSize*ySize;
                        morans[z] /= (double) totalNeighbours;
                        morans[z] /= variance[z]; // This is \sum_i (x_i - \bar{x})^2, before dividing
			variance[z] /= (imageSamples[z].length * imageSamples[z][0].length);
		}
		totalE /= zSize;
		
		//centerRange, totalVariance, hist
		hist = new int[zSize][];
		totalHist = new int[(int)totalMinMax[1] - (int)totalMinMax[0] + 1];
		int totalMin = (int)totalMinMax[0];
		for(int z = 0; z < zSize; z++){
			int min = (int)minMax[z][0];
			hist[z] = new int[(int)minMax[z][1] - (int)minMax[z][0] + 1];
			for (int i = 0; i < hist[z].length; i++) {
				hist[z][i] = 0;
			}
			centerRange[z] = (minMax[z][0] + minMax[z][1]) / 2;
			for(int y = 0; y < ySize; y++){
				for(int x = 0; x < xSize; x++){
					totalVariance = (imageSamples[z][y][x] - totalE) * (imageSamples[z][y][x] - totalE);
					hist[z][imageSamples[z][y][x]-min]++;
					totalHist[(int)imageSamples[z][y][x]-totalMin]++;
				}
			}
		}
		
		// Entropy
		entropy = new double[zSize];
		for(int z = 0; z < zSize; z++) {
			entropy[z] = 0.0D;
			for (int i = 0; i < hist[z].length; i++) {
				double p = ((double)hist[z][i]) / (ySize*xSize);
				if (p > 0.0D) {
					entropy[z] -= p * Math.log(p);
				}
			}
			entropy[z] /= Math.log(2.0D);
		}
		
		totalEntropy = 0.0D;
		for (int i = 0; i < totalHist.length; i++) {
			double totalP = ((double)totalHist[i]) / (zSize*ySize*xSize);
			if (totalP > 0.0D) {
				totalEntropy -= totalP * Math.log(totalP);
			}
		}
		totalEntropy /= Math.log(2);
				
		//Totals
		totalAverage = 0F;
		totalCenterRange = 0F;
		for(int z = 0; z < zSize; z++){
			totalAverage += average[z];
			totalCenterRange += centerRange[z];
			totalEnergy += energy[z];
                        totalMorans += morans[z];
		}
		totalAverage /= zSize;
		totalCenterRange /= zSize;
                totalMorans /= zSize;
		totalVariance /= (imageSamples.length * imageSamples[0].length * imageSamples[0][0].length);
	}
	public ImageStatistical(int[][] predictedSamples) {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return countedValues definition in this class
	 */
	public int[][] getcountedValues(){
		return(countedValues);
	}
	
	/**
	 * @return minMax definition in this class
	 */
	public double[][] getMinMax(){
		return(minMax);
	}

	/**
	 * @return totalMinMax definition in this class
	 */
	public double[] getTotalMinMax(){
		return(totalMinMax);
	}

	/**
	 * @return average definition in this class
	 */
	public double[] getAverage(){
		return(average);
	}

	/**
	 * @return totalAverage definition in this class
	 */
	public double getTotalAverage(){
		return(totalAverage);
	}

	/**
	 * @return centerRange definition in this class
	 */
	public double[] getCenterRange(){
		return(centerRange);
	}

	/**
	 * @return totalCenterRange definition in this class
	 */
	public double getTotalCenterRange(){
		return(totalCenterRange);
	}
	
	/**
	 * @return Energy definition in this class
	 */
	public double[] getEnergy(){
		return(energy);
	}

	/**
	 * @return totalEnergy definition in this class
	 */
	public double getTotalEnergy(){
		return(totalEnergy);
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
         * @return the array of Moran's I measures for all the components
         */
	public double[] getMoransMeasure() {
                return(morans);
        }
        
        /**
         * @return the average Moran's I measure for all the images
         */
        public double getTotalMoransMeasure() {
                return(totalMorans);
        }
}
