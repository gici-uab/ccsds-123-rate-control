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
import GiciException.*;


/**
 * This class receives two images and calculates its difference information (MSE and PSNR).
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public class ImageCompare {

	/**
	 * Mean Absolute Error (MAE) for each image component.
	 * <p>
	 * Only positive values allowed.
	 */
	float[] mae = null;

	/**
	 * Global Mean Absolute Error (MSE) of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	float totalMAE;

	/**
	 * Peak Absolute Error (MAE) for each image component.
	 * <p>
	 * Only positive values allowed.
	 */
	float[] pae = null;

	/**
	 * Global Peak Absolute Error (MSE) of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	float totalPAE;

	/**
	 * Mean Squared Error (MSE) for each image component.
	 * <p>
	 * Only positive values allowed.
	 */
	float[] mse = null;

	/**
	 * Global Mean Squared Error (MSE) of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	float totalMSE;

	/**
	 * Root Mean Squared Error (MSE) for each image component.
	 * <p>
	 * Only positive values allowed.
	 */
	float[] rmse = null;

	/**
	 * Global Root Mean Squared Error (MSE) of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	float totalRMSE;

	/**
	 * Mean Error (ME) for each image component.
	 * <p>
	 * All values allowed.
	 */
	float[] me = null;

	/**
	 * Global Mean Error (MSE) of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	float totalME;

	/**
	 * Signal to Noise Ratio (PSNR) for each image component.
	 * <p>
	 * Only positive values allowed.
	 */
	float[] snr = null;

	/**
	 * Global Signal to Noise Ratio (PSNR) of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	float totalSNR;

	/**
	 * Peak Signal to Noise Ratio (PSNR) for each image component.
	 * <p>
	 * Only positive values allowed.
	 */
	float[] psnr = null;

	/**
	 * Global Peak Signal to Noise Ratio (PSNR) of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	float totalPSNR;

	/**
	 * Equality for each image component.
	 * <p>
	 * True or false.
	 */
	boolean[] equal;

	/**
	 * Global equality of the image.
	 * <p>
	 * True or false.
	 */
	boolean totalEQUAL;


	/**
	 * Constructor that does all the operations to compare images.
	 *
	 * @param image1 a 3D float array of image samples (index are [z][y][x])
	 * @param image2 a 3D float array of image samples (index are [z][y][x])
	 * @param pixelBitDepth number of bits for the specified image sample type (for each component)
	 *
	 * @throws WarningException when image sizes are not the same
	 */
	public ImageCompare(float[][][] image1, float[][][] image2, int[] pixelBitDepth) throws WarningException{
		//Size set
		int zSize1 = image1.length;
		int ySize1 = image1[0].length;
		int xSize1 = image1[0][0].length;

		int zSize2 = image2.length;
		int ySize2 = image2[0].length;
		int xSize2 = image2[0][0].length;

		//Check if images have same sizes
		if((zSize1 != zSize2) || (ySize1 != ySize2) || (xSize1 != xSize2)){
			throw new WarningException("Image sizes must be the same to perform comparisons.");
		}

		//Memory allocation
		mae = new float[zSize1];
		pae = new float[zSize1];
		mse = new float[zSize1];
		rmse = new float[zSize1];
		me = new float[zSize1];
		snr = new float[zSize1];
		psnr = new float[zSize1];
		equal = new boolean[zSize1];

		//Calculus of MSE
		totalMAE = 0F;
		totalPAE = -1F;
		totalMSE = 0F;
		totalRMSE = 0F;
		totalME = 0F;
		totalSNR = 0F;
		totalPSNR = 0F;
		totalEQUAL = true;
		for(int z = 0; z < zSize1; z++){
			mae[z] = 0F;
			pae[z] = -1F;
			mse[z] = 0F;
			rmse[z] = 0F;
			me[z] = 0F;
			snr[z] = 0F;
			psnr[z] = 0F;
			equal[z] = false;

			double tmpMAE = 0D;
			double tmpMSE = 0D;
			double tmpME = 0D;
			double tmpSNR = 0D;
			for(int y = 0; y < ySize1; y++){
				for(int x = 0; x < xSize1; x++){
					double diff = 0F;
					diff = image1[z][y][x] - image2[z][y][x];

					tmpME += diff;
					diff = Math.abs(diff);
					tmpMAE += diff;
					if(diff > pae[z]){
						pae[z] = (float) diff;
					}
					tmpMSE += diff * diff;
					tmpSNR += image1[z][y][x] * image1[z][y][x];
				}
			}
			totalMAE += (float) tmpMAE;
			totalME += (float) tmpME;
			if(pae[z] > totalPAE){
				totalPAE = pae[z];
			}
			totalMSE += (float) tmpMSE;
			mae[z] = (float) (tmpMAE / ((double) ySize1 * (double) xSize1));
			me[z] = (float) (tmpME / ((double) ySize1 * (double) xSize1));
			mse[z] = (float) (tmpMSE / ((double) ySize1 * (double) xSize1));
			rmse[z] = (float) Math.sqrt(mse[z]);
			snr[z] = 10 * (float) Math.log( (tmpSNR / ((double) ySize1 * (double) xSize1)) / mse[z]) / (float) Math.log(10);
			//snr[z] = 20 * (float) Math.log( Math.sqrt((tmpSNR / ((double) ySize1 * (double) xSize1))) / rmse[z]) / (float) Math.log(10);
			float range = ((float) Math.pow(2D, (double) pixelBitDepth[z]) - 1);
			psnr[z] = 10 * (float) (Math.log(range*range / mse[z]) / (float) Math.log(10));
			//psnr[z] = 20 * (float) (Math.log(range / rmse[z]) / (float) Math.log(10));
			equal[z] = mae[z] == 0;
			if(!equal[z]){
				totalEQUAL = false;
			}
		}
		totalMAE /= (zSize1 * ySize1 * xSize1);
		totalMSE /= (zSize1 * ySize1 * xSize1);
		totalRMSE = (float) Math.sqrt(totalMSE);
		totalME /= (zSize1 * ySize1 * xSize1);
		for(int z = 0; z < zSize1; z++){
			totalSNR += snr[z];
			totalPSNR += psnr[z];
		}
		totalSNR /= zSize1;
		totalPSNR /= zSize1;
	}

	/**
	 * @return mae definition in this class
	 */
	public float[] getMAE(){
		return(mae);
	}

	/**
	 * @return totalMAE definition in this class
	 */
	public float getTotalMAE(){
		return(totalMAE);
	}

	/**
	 * @return pae definition in this class
	 */
	public float[] getPAE(){
		return(pae);
	}

	/**
	 * @return totalPAE definition in this class
	 */
	public float getTotalPAE(){
		return(totalPAE);
	}

	/**
	 * @return mse definition in this class
	 */
	public float[] getMSE(){
		return(mse);
	}

	/**
	 * @return totalMSE definition in this class
	 */
	public float getTotalMSE(){
		return(totalMSE);
	}

	/**
	 * @return rmse definition in this class
	 */
	public float[] getRMSE(){
		return(rmse);
	}

	/**
	 * @return totalRMSE definition in this class
	 */
	public float getTotalRMSE(){
		return(totalRMSE);
	}

	/**
	 * @return me definition in this class
	 */
	public float[] getME(){
		return(me);
	}

	/**
	 * @return totalME definition in this class
	 */
	public float getTotalME(){
		return(totalME);
	}

	/**
	 * @return snr definition in this class
	 */
	public float[] getSNR(){
		return(snr);
	}

	/**
	 * @return totalSNR definition in this class
	 */
	public float getTotalSNR(){
		return(totalSNR);
	}

	/**
	 * @return psnr definition in this class
	 */
	public float[] getPSNR(){
		return(psnr);
	}

	/**
	 * @return totalPSNR definition in this class
	 */
	public float getTotalPSNR(){
		return(totalPSNR);
	}

	/**
	 * @return equal definition in this class
	 */
	public boolean[] getEQUAL(){
		return(equal);
	}

	/**
	 * @return totalEQUAL definition in this class
	 */
	public boolean getTotalEQUAL(){
		return(totalEQUAL);
	}

}
