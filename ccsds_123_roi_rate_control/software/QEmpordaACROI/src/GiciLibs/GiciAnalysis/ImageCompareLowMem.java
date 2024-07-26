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
import java.util.Arrays;
import GiciException.WarningException;

/**
 * This class receives two images and calculates its difference information (MSE and PSNR).
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public final class ImageCompareLowMem implements ImageCompareInterface {
	
	// Per-component results
	private double[] variance;
	private double[] mae;
	private double[] pae;
	private double[] mse;
	private double[] rmse;
	private double[] me;
	private double[] snr;
	private double[] snrVar;
	private double[] psnr;
	private double[] psnrSalomon;
	private boolean[] equal;
	private double[] rrmse;
	private double[] nmse;
	private double[] psnrNc;
	
	// Total results
	private double totalVariance;
	private double totalEnergy;
	private double totalMAE;
	private double totalPAE;
	private double totalMSE;
	private double totalRMSE;
	private double totalME;
	private double totalSNR;
	private double totalSNRVAR;
	private double totalPSNR;
	private double totalPSNRSALOMON;
	private boolean totalEQUAL;
	private double totalRRMSE;
	private double totalNMSE;
	private double totalPSNRNC;
	
	// Image characteristics
	final int zSize;
	final int ySize;
	final int xSize;
	
	final long perBandImagePixels;
	final long totalImagePixels; 
	
	final int pixelBitDepth;
	final boolean isSigned;
	
	// Per-band intermediate results
	final double[] mean1;
	final double[] mean2;
	final double[] sum1;
	final double[] sum2;
	final double[] min1;
	final double[] max1;
	final double[] min2;
	final double[] max2;
	final double[] absoluteErrorSum;
	final double[] absoluteErrorPeak;
	final double[] squaredErrorSum;
	final double[] errorSum;
	final double[] squaredRelativeErrorSum;
	final double[] energy1;
	final double[] energy2;
		
	// Results calculated for
	final boolean[] perBandResultsCalculated;
	boolean totalResultsCalculated;
	
	/**
	 * @param z indicating the component
	 * @return range for an specific component
	 */
	private double getRealRange(final int z) {
		assert (perBandResultsCalculated[z]);
		
		double min = Math.min(min1[z], min2[z]);
		double max = Math.max(max1[z], max2[z]);

		// Assume signed
		int pixelBitDepth;
		
		if (isSigned) {
			pixelBitDepth = (int) Math.max(Math.ceil(Math.log(max + 1) / Math.log(2)), Math.ceil(Math.log(Math.abs(min)) / Math.log(2))) + 1;
		} else {
			pixelBitDepth = (int)Math.ceil(Math.log(max + 1) / Math.log(2));
		}
		
		double range = Math.pow(2D, (double) pixelBitDepth) - 1;

		return range;
	}
	
	/**
	 * @param z indicating the component
	 * @param pixelBitDepth
	 * @return range for an specific component
	 */
	private double getRange(final int pixelBitDepth) {
		return Math.pow(2D, (double) pixelBitDepth) - 1;
	}
	
	/**
	 * 
	 * @param a
	 * @param previousMax
	 * @return
	 */
	private int imprecisionBits(final double a, final int previousMax) {
		final double largestExactDouble = 0x1FFFFFFFFFFFFFL;
		int r = 0;
		
		if (a > largestExactDouble) {
			r = Math.max(previousMax, (int) Math.ceil(Math.log(a / largestExactDouble) / Math.log(2)));
		}
		
		return r;
	}
	
	@SuppressWarnings("unused")
	private void reportOverflow(int imprecisionBits) {
		// Report overflows in case they occur
		if (imprecisionBits > 0) {
			System.err.println("Inexact results may be produced due to insufficient mantissa bits (at least "
					+ imprecisionBits + " more bits required).");
			
			double voxels = totalImagePixels;

			double maxBitDepthRange = 0;

			if (pixelBitDepth > 12) {
				for (int z = 0; z < zSize; z++) {
					maxBitDepthRange = Math.max(getRealRange(z), maxBitDepthRange);
				}
			} else {
				maxBitDepthRange = getRange(pixelBitDepth);
			}
			

			// requiredBits be inaccurate due to insufficient mantissa bits, but it shall work anyway
			// maxBitDepthRange is squared because it is also squared in the computation
			double requiredBits = Math.ceil(Math.log(voxels * maxBitDepthRange * maxBitDepthRange) / Math.log(2));
			double requiredBitsNoMSE = Math.ceil(Math.log(voxels * maxBitDepthRange) / Math.log(2));
			double requiredBitsNoMulti = Math.ceil(Math.log(ySize * xSize * maxBitDepthRange * maxBitDepthRange) / Math.log(2));
			double requiredBitsNoMultiMSE = Math.ceil(Math.log(ySize * xSize * maxBitDepthRange) / Math.log(2));

			final int maxMantisa = 52; 

			if (requiredBits > maxMantisa) {
				System.err.println("Image too large for exact computations. Required precision for this image is "
						+ requiredBits + " bits.");

				if (requiredBitsNoMSE <= maxMantisa) {
					System.err.println("* All but MSE related measures (MSE, PSNR, SNR) are still accurate.");
				}

				if (requiredBitsNoMulti <= maxMantisa) {
					System.err.println("* Individual component results are still accurate");
				}

				if (requiredBitsNoMultiMSE <= maxMantisa && requiredBitsNoMSE > maxMantisa && requiredBitsNoMulti > maxMantisa) {
					System.err.println("* Individual component results of all but MSE related measures (MSE, PSNR, SNR)"
							+ " are still accurate.");
				}
			}
		}
	}
	
	/**
	 * Constructor that does all the operations to compare images.
	 *
	 * @param inputImage1 a 3D float array of image samples (index are [z][y][x])
	 * @param inputImage2 a 3D float array of image samples (index are [z][y][x])
	 * @param pixelBitDepth number of bits for the specified image sample type (for each component)
	 * @param variance_deprecated is no longer used because is cheap to compute and the aggregated 
	 * variance is not trivial to compute (although not impossible if you have the energy)
	 *
	 * @throws WarningException when image sizes are not the same
	 */
	public ImageCompareLowMem(int[] image1Geometry, int[] image2Geometry) throws WarningException {
		
		//Size set
		zSize = image1Geometry[0];
		ySize = image1Geometry[1];
		xSize = image1Geometry[2];		
		
		perBandImagePixels = (long) ySize * (long) xSize;
		totalImagePixels = (long) zSize * (long) ySize * (long) xSize;
		
		// Check if images have the same size
		if ((zSize != image2Geometry[0]) || (ySize != image2Geometry[1]) || (xSize != image2Geometry[2])) {
			throw new WarningException("Image sizes (zSize, ySize and xSize) must be the same on both images to perform"
					+ " comparisons for a specific components.");
		}

		// Set bit depth
		switch(image1Geometry[3]) {		
		case 0: //boolean - 1 byte
			pixelBitDepth = 1; isSigned = false; break;
		case 1: //byte
			pixelBitDepth = 8; isSigned = false; break;
		case 2: //char
			pixelBitDepth = 16; isSigned = false; break;
		case 3: //short
			pixelBitDepth = 16; isSigned = true; break;
		case 4: //int
			pixelBitDepth = 32; isSigned = true; break;
		default:
			throw new WarningException("Unsupported image data type.");
		}

		// Memory allocation for the results
		variance = new double[zSize];
		mae = new double[zSize];
		pae = new double[zSize];
		mse = new double[zSize];
		rmse = new double[zSize];
		me = new double[zSize];
		snr = new double[zSize];
		snrVar = new double[zSize];
		psnr = new double[zSize];
		psnrSalomon = new double[zSize];
		equal = new boolean[zSize];
		rrmse = new double[zSize];
		nmse = new double[zSize];
		psnrNc = new double[zSize];
		
		// Memory allocation for the intermediate results
		mean1 = new double[zSize];
		mean2 = new double[zSize];
		sum1 = new double[zSize];
		sum2 = new double[zSize];
		min1 = new double[zSize];
		max1 = new double[zSize];
		min2 = new double[zSize];
		max2 = new double[zSize];
		absoluteErrorSum = new double[zSize];
		absoluteErrorPeak = new double[zSize];
		squaredErrorSum = new double[zSize];
		errorSum = new double[zSize];
		squaredRelativeErrorSum = new double[zSize];	
		energy1 = new double[zSize];
		energy2 = new double[zSize];
		
		// Done?
		perBandResultsCalculated = new boolean[zSize];
	}
	
	public void processBand(int[][] inputImage1Band, int[][] inputImage2Band, int z) { 
		// Initialize (all the others are properly initialized as 0)
		min1[z] = Double.POSITIVE_INFINITY;
		max1[z] = Double.NEGATIVE_INFINITY;
		min2[z] = Double.POSITIVE_INFINITY;
		max2[z] = Double.NEGATIVE_INFINITY;
		absoluteErrorPeak[z] = Double.NEGATIVE_INFINITY;

		for (int y = 0; y < ySize; y++) {
			for (int x = 0; x < xSize; x++) {
				final double value1 = inputImage1Band[y][x];
				final double value2 = inputImage2Band[y][x];
				final double error = inputImage1Band[y][x] - inputImage2Band[y][x];
				
				sum1[z] += value1;
				sum2[z] += value2;
				energy1[z] += value1 * value1;
				energy2[z] += value2 * value2;
				max1[z] = Math.max(max1[z], value1);
				max2[z] = Math.max(max2[z], value2);
				min1[z] = Math.min(min1[z], value1);
				min2[z] = Math.min(min2[z], value2);
				
				absoluteErrorSum[z] += Math.abs(error);
				absoluteErrorPeak[z] = Math.max(absoluteErrorPeak[z], Math.abs(error));
				squaredErrorSum[z] += error * error;
				errorSum[z] += error;

				final double relativeError;

				if (error == 0) {
					relativeError = 0; // When error is zero, relativeError is also zero regardless of value.
				} else {
					relativeError = (error * error) / (value1 * value1);
				}

				squaredRelativeErrorSum[z] += relativeError;
			}
		}

		mean1[z] = sum1[z] / perBandImagePixels;
		mean2[z] = sum2[z] / perBandImagePixels;
		variance[z] = (energy1[z] - (sum1[z] * sum1[z] / perBandImagePixels)) / perBandImagePixels;
		
		double range = getRange(pixelBitDepth);
		
		mae[z] = absoluteErrorSum[z] / perBandImagePixels;
		pae[z] = absoluteErrorPeak[z];
		mse[z] = squaredErrorSum[z] / perBandImagePixels;
		rmse[z] = Math.sqrt(mse[z]);
		me[z] = errorSum[z] / perBandImagePixels;
		snr[z] = 10 * Math.log10(energy1[z]  / (mse[z] * perBandImagePixels));
		snrVar[z] = 10 * Math.log10(variance[z] / mse[z]);
		psnr[z] = 10 * Math.log10(range * range / mse[z]);
		psnrSalomon[z] = 10 * Math.log10((range + 1) * (range + 1) / (4 * mse[z]));
		equal[z] = (absoluteErrorSum[z] == 0);						
		rrmse[z] = Math.sqrt(squaredRelativeErrorSum[z] / perBandImagePixels);
		nmse[z] = 100 * mse[z] / energy1[z];
		psnrNc[z] = 10 * Math.log10(max1[z] * max1[z] / mse[z]);
		
		perBandResultsCalculated[z] = true;
	}
		
	public void produceTotalResults() { 	
		// Check for overflows
		// We are using IEEE 754 binary64 doubles with 53 bits of mantissa 	
		// New strategy: check for the error after it occurs		
		int imprecisionBits = 0;
		
		// Accumulate parallel results
		double totalSum1 = 0;
		double totalSum2 = 0;
		double totalMax = Double.NEGATIVE_INFINITY;
		double totalAbsoluteErrorSum = 0;
		double totalAbsoluteErrorPeak = Double.NEGATIVE_INFINITY;
		double totalSquaredErrorSum = 0;
		double totalErrorSum = 0;
		totalRRMSE = 0;
		
		for (int z = 0; z < zSize; z++) {
			totalSum1 += sum1[z];
			totalSum2 += sum2[z];
			totalEnergy += energy1[z];
			totalMax = Math.max(totalMax, max1[z]);
			totalAbsoluteErrorSum += absoluteErrorSum[z];
			totalAbsoluteErrorPeak = Math.max(totalAbsoluteErrorPeak, absoluteErrorPeak[z]);
			totalSquaredErrorSum += squaredErrorSum[z];
			totalErrorSum += errorSum[z];
			totalRRMSE += squaredRelativeErrorSum[z];
		}
				
		// Overflow check
		imprecisionBits = imprecisionBits(totalEnergy, imprecisionBits);
		imprecisionBits = imprecisionBits(totalSum1, imprecisionBits);
		imprecisionBits = imprecisionBits(totalSum1 * totalSum1, imprecisionBits);
		imprecisionBits = imprecisionBits(totalSum2, imprecisionBits);
		imprecisionBits = imprecisionBits(totalAbsoluteErrorSum, imprecisionBits);
		imprecisionBits = imprecisionBits(totalAbsoluteErrorPeak, imprecisionBits);
		imprecisionBits = imprecisionBits(totalSquaredErrorSum, imprecisionBits);
	
		totalVariance = (totalEnergy - (totalSum1 * totalSum1 / perBandImagePixels)) / perBandImagePixels;
		
		System.err.println("" + totalEnergy + " " + totalSum1 + " " + totalVariance);
		
		// Calculus of Total final results
		double totalRange = getRange(pixelBitDepth);
		
		totalMAE = totalAbsoluteErrorSum / totalImagePixels;
		totalPAE = totalAbsoluteErrorPeak;
		totalMSE = totalSquaredErrorSum / totalImagePixels;  
		totalRMSE = Math.sqrt(totalMSE);
		totalME = totalErrorSum / totalImagePixels;
		totalSNR = 10 * Math.log10(totalEnergy  / (totalMSE * totalImagePixels));
		totalSNRVAR = 10 * Math.log10(totalVariance / totalMSE);
		totalPSNR = 10 * Math.log10(totalRange * totalRange / totalMSE);
		totalPSNRSALOMON = 10 * Math.log10((totalRange + 1) * (totalRange + 1) / (4 * totalMSE));
		totalEQUAL = (totalAbsoluteErrorSum == 0);
		totalRRMSE = Math.sqrt(totalRRMSE / totalImagePixels);
		totalNMSE = 100 * totalMSE / totalEnergy;
		totalPSNRNC = 10 * Math.log10(totalMax * totalMax / totalMSE);
		
		totalResultsCalculated = true;
		
		reportOverflow(imprecisionBits);
	}
	
	/**
	 * @return mae definition in this class
	 */
	public double[] getMAE() {
		return mae;
	}

	/**
	 * @return totalMAE definition in this class
	 */
	public double getTotalMAE() {
		return totalMAE;
	}

	/**
	 * @return pae definition in this class
	 */
	public double[] getPAE() {
		return pae;
	}

	/**
	 * @return totalPAE definition in this class
	 */
	public double getTotalPAE() {
		return totalPAE;
	}

	/**
	 * @return mse definition in this class
	 */
	public double[] getMSE() {
		return mse;
	}

	/**
	 * @return totalMSE definition in this class
	 */
	public double getTotalMSE() {
		return totalMSE;
	}

	/**
	 * @return rmse definition in this class
	 */
	public double[] getRMSE() {
		return rmse;
	}

	/**
	 * @return totalRMSE definition in this class
	 */
	public double getTotalRMSE() {
		return totalRMSE;
	}

		/**
	 * @return me definition in this class
	 */
	public double[] getME() {
		return me;
	}

	/**
	 * @return totalME definition in this class
	 */
	public double getTotalME() {
		return totalME;
	}

	/**
	 * @return snr definition in this class
	 */
	public double[] getSNR() {
		return snr;
	}

	/**
	 * @return totalSNR definition in this class
	 */
	public double getTotalSNR()  {
		return totalSNR;
	}

	/**
	 * @return psnr definition in this class
	 */
	public double[] getPSNR() {
		return psnr;
	}

	/**
	 * @return totalPSNR definition in this class
	 */
	public double getTotalPSNR() {
		return totalPSNR;
	}
	
	/**
	 * @return psnr definition in this class
	 */
	public double[] getPSNRSALOMON() {
		return psnrSalomon;
	}

	/**
	 * @return totalPSNR definition in this class
	 */
	public double getTotalPSNRSALOMON() {
		return totalPSNRSALOMON;
	}

	/**
	 * @return equal definition in this class
	 */
	public boolean[] getEQUAL() {
		return equal;
	}

	/**
	 * @return totalEQUAL definition in this class
	 */
	public boolean getTotalEQUAL() {
		return totalEQUAL;
	}

	/**
	 * @return snr definition in this class
	 */
	public double[] getSNRVAR() {
		return snrVar;
	}

	/**
	 * @return totalSNR definition in this class
	 */
	public double getTotalSNRVAR() {
		return totalSNRVAR;
	}
	
	public double[] getRRMSE() {
		return rrmse;
	}

	public double getTotalRRMSE() {
		return totalRRMSE;
	}

	public double[] getNMSE() {
		return nmse;
	}

	public double getTotalNMSE() {
		return totalNMSE;
	}

	public double[] getPSNRNC() {
		return psnrNc;
	}

	public double getTotalPSNRNC() {
		return totalPSNRNC;
	}

	// Unimplemented
	public double[] getCovariance() {
		double [] r = new double[zSize];
		Arrays.fill(r, Double.NaN);
		return r;
	}

	public double[] getSSIM() {
		double [] r = new double[zSize];
		Arrays.fill(r, Double.NaN);
		return r;
	}

	public double getTotalSSIM() {
		return Double.NaN;
	}
}
