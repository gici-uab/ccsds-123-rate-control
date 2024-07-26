/**
 * Copyright (C) 2013 - Francesc Auli-Llinas
 *
 * This program is distributed under the BOI License.
 * This program is distributed in the hope that it will be useful, but without any warranty; without even the implied warranty of merchantability or fitness for a particular purpose.
 * You should have received a copy of the BOI License along with this program. If not, see <http://www.deic.uab.cat/~francesc/software/license/>.
 */
package statistics;


/**
 * This class receives two images and computes some metrics such as MAE, MSE, SNR, PSNR, etc.
 *
 * Usage: the constructor receives basic parameters and initializes the structures of the class. Then, the images can be compared incrementally passing chunks of the images to the <code>accumulateStatistics</code> function. Once all chunks of data are passed, the computed metrics are extracted via the <code>get</code> functions. To compare another two images, a new object has to be created.
 *
 * Multithreading support: once the object is created, multiple threads can call the function <code>accumulateStatistics</code>. There can be many objects of this class running simultaneously, one for each image comparison.
 *
 * @author Francesc Auli-Llinas
 * @version 1.2
 */
public final class ImageCompare{

	/**
	 * Number of components of the images to be compared.
	 * <p>
	 * Only positive values allowed.
	 */
	private int zSize = -1;

	/**
	 * Number of rows of the images to be compared.
	 * <p>
	 * Only positive values allowed.
	 */
	private int ySize = -1;

	/**
	 * Number of columns of the images to be compared.
	 * <p>
	 * Only positive values allowed.
	 */
	private int xSize = -1;

	/**
	 * Bit depth of the images to be compared.
	 * <p>
	 * Only positive values allowed.
	 */
	private int bitDepth = -1;

	/**
	 * Number of samples compared for each component.
	 * <p>
	 * Statistics accumulated in the <code>accumulateStatistics</code> functions.
	 */
	private long[] accumulatedSamples = null;

	/**
	 * Accumulated ME for each component.
	 * <p>
	 * Statistics accumulated in the <code>accumulateStatistics</code> functions.
	 */
	private double[] accumulatedME = null;

	/**
	 * Accumulated MAE for each component.
	 * <p>
	 * Statistics accumulated in the <code>accumulateStatistics</code> functions.
	 */
	private double[] accumulatedMAE = null;

	/**
	 * Accumulated PAE for each component.
	 * <p>
	 * Statistics accumulated in the <code>accumulateStatistics</code> functions.
	 */
	private double[] accumulatedPAE = null;

	/**
	 * Accumulated MSE for each component.
	 * <p>
	 * Statistics accumulated in the <code>accumulateStatistics</code> functions.
	 */
	private double[] accumulatedMSE = null;

	/**
	 * Accumulated SNR for each component.
	 * <p>
	 * Statistics accumulated in the <code>accumulateStatistics</code> functions.
	 */
	private double[] accumulatedSNR = null;

	/**
	 * Lock object employed to synchronize the access of variables by multiple threads.
	 * <p>
	 * This lock is solely employed in the <code>accumulateStatistics</code> function.
	 */
	private final Object lock = new Object();


	/**
	 * The constructor receives the image geometry and initializes required structures.
	 *
	 * @param zSize number of components
	 * @param ySize number of rows
	 * @param xSize number of columns
	 * @param bitDepth bit-depth of the image
	 * @throws Exception when the sizes of both images are not the same
	 */
	public ImageCompare(int zSize, int ySize, int xSize, int bitDepth) throws Exception{
		//Set sizes
		this.zSize = zSize;
		this.ySize = ySize;
		this.xSize = xSize;
		this.bitDepth = bitDepth;

		//Memory allocation
		accumulatedSamples = new long[zSize];
		accumulatedME = new double[zSize];
		accumulatedMAE = new double[zSize];
		accumulatedPAE = new double[zSize];
		accumulatedMSE = new double[zSize];
		accumulatedSNR = new double[zSize];
	}

	/**
	 * Accumulates statistics for a chunk of the image belonging to one component.
	 *
	 * @param imageChunk1 a 2D array of floats containing the image samples (indices of the array are [y][x]). This is the original image.
	 * @param imageChunk2 a 2D array of floats containing the image samples (indices of the array are [y][x]). This is the image to be compared.
	 * @param z component to which the chunks belong
	 * @throws Exception when the sizes of both images are not the same or the component is not valid
	 */
	public void accumulateStatistics(float[][] imageChunk1, float[][] imageChunk2, int z) throws Exception{
		//Check that both images have the same size
		if((imageChunk1.length != imageChunk2.length) || (imageChunk1[0].length != imageChunk2[0].length)){
			throw new Exception("The size of both images must be the same.");
		}
		if((z < 0) || (z >= zSize)){
			throw new Exception("Invalid component " + z + ".");
		}

		//Accumulate statistics
		long accumulatedSamplesTMP = 0;
		double accumulatedMETMP = 0;
		double accumulatedMAETMP = 0;
		double accumulatedPAETMP = 0;
		double accumulatedMSETMP = 0;
		double accumulatedSNRTMP = 0;
		for(int y = 0; y < imageChunk1.length; y++){
		for(int x = 0; x < imageChunk1[y].length; x++){
			accumulatedSamplesTMP++;

			double diff = imageChunk1[y][x] - imageChunk2[y][x];
			accumulatedMETMP += diff;
			diff = Math.abs(diff);
			accumulatedMAETMP += diff;
			if(diff > accumulatedPAETMP){
				accumulatedPAETMP = diff;
			}
			accumulatedMSETMP += diff * diff;
			accumulatedSNRTMP += imageChunk1[y][x] * imageChunk1[y][x];
		}}

		synchronized(lock){
			accumulatedSamples[z] += accumulatedSamplesTMP;
			accumulatedME[z] += accumulatedMETMP;
			accumulatedMAE[z] += accumulatedMAETMP;
			if(accumulatedPAETMP > accumulatedPAE[z]){
				accumulatedPAE[z] = accumulatedPAETMP;
			}
			accumulatedMSE[z] += accumulatedMSETMP;
			accumulatedSNR[z] += accumulatedSNRTMP;
		}
	}

	/**
	 * Accumulated statistics for a chunk of the image belonging to some (possibly all) consecutive components.
	 *
	 * @param imageChunk1 a 3D array of floats containing the image samples (indices of the array are [z][y][x]). This is the original image.
	 * @param imageChunk2 a 3D array of floats containing the image samples (indices of the array are [z][y][x]). This is the image to be compared.
	 * @param zImageOffset component of the original image to which the first component of the chunk corresponds
	 * @throws Exception when the sizes of both images are not the same or the component is not valid
	 */
	public void accumulateStatistics(float[][][] imageChunk1, float[][][] imageChunk2, int zImageOffset) throws Exception{
		//Check that both images have the same size
		if(imageChunk1.length != imageChunk2.length){
			throw new Exception("The size of both images must be the same.");
		}

		//Accumulate statistics
		for(int z = 0; z < imageChunk1.length; z++){
			accumulateStatistics(imageChunk1[z], imageChunk2[z], zImageOffset + z);
		}
	}

	/**
	 * Returns the Mean Absolute Error (MAE) for each image component.
	 *
	 * @return and array of zSize positions containing the result for each component
	 */
	public double[] getMAE(){
		double[] MAE = new double[zSize];
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				MAE[z] = (accumulatedMAE[z] / ((double) ySize * (double) xSize));
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		return(MAE);
	}

	/**
	 * Returns the Mean Absolute Error (MAE) for all components of the image.
	 *
	 * @return the result
	 */
	public double getTotalMAE(){
		double totalMAE = 0d;
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				totalMAE += accumulatedMAE[z];
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		totalMAE /= ((double) zSize * (double) ySize * (double) xSize);
		return(totalMAE);
	}

	/**
	 * Returns the Peak Absolute Error (MAE) for each image component.
	 *
	 * @return and array of zSize positions containing the result for each component
	 */
	public double[] getPAE(){
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] != (long) ySize * (long) xSize){
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		return(accumulatedPAE);
	}

	/**
	 * Returns the Peak Absolute Error (MSE) for all components of the image.
	 *
	 * @return the result
	 */
	public double getTotalPAE(){
		double totalPAE = 0d;
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				if(accumulatedPAE[z] > totalPAE){
					totalPAE = accumulatedPAE[z];
				}
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		return(totalPAE);
	}

	/**
	 * Returns the Mean Squared Error (MSE) for each image component.
	 *
	 * @return and array of zSize positions containing the result for each component
	 */
	public double[] getMSE(){
		double[] MSE = new double[zSize];
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				MSE[z] = (accumulatedMSE[z] / ((double) ySize * (double) xSize));
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		return(MSE);
	}

	/**
	 * Returns the Mean Squared Error (MSE) for all components of the image.
	 *
	 * @return the result
	 */
	public double getTotalMSE(){
		double totalMSE = 0d;
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				totalMSE += accumulatedMSE[z];
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		totalMSE /= ((double) zSize * (double) ySize * (double) xSize);
		return(totalMSE);
	}

	/**
	 * Returns the Root Mean Squared Error (MSE) for each image component.
	 *
	 * @return and array of zSize positions containing the result for each component
	 */
	public double[] getRMSE(){
		double[] RMSE = new double[zSize];
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				RMSE[z] = Math.sqrt((accumulatedMSE[z] / ((double) ySize * (double) xSize)));
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		return(RMSE);
	}

	/**
	 * Returns the Root Mean Squared Error (MSE) for all components of the image.
	 *
	 * @return the result
	 */
	public double getTotalRMSE(){
		double totalRMSE = 0d;
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				totalRMSE += accumulatedMSE[z];
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		totalRMSE = Math.sqrt(totalRMSE / ((double) (zSize * ySize * xSize)));
		return(totalRMSE);
	}

	/**
	 * Returns the Mean Error (ME) for each image component.
	 *
	 * @return and array of zSize positions containing the result for each component
	 */
	public double[] getME(){
		double[] ME = new double[zSize];
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				ME[z] = (accumulatedME[z] / ((double) ySize * (double) xSize));
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		return(ME);
	}

	/**
	 * Returns the Mean Error (MSE) for all components of the image.
	 *
	 * @return the result
	 */
	public double getTotalME(){
		double totalME = 0d;
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				totalME += accumulatedME[z];
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		totalME /= ((double) zSize * (double) ySize * (double) xSize);
		return(totalME);
	}

	/**
	 * Returns the Signal to Noise Ratio (PSNR) for each image component.
	 *
	 * @return and array of zSize positions containing the result for each component
	 */
	public double[] getSNR(){
		double[] SNR = new double[zSize];
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				double tmpSNR = (accumulatedSNR[z] / ((double) ySize * (double) xSize));
				double MSE = (accumulatedMSE[z] / ((double) ySize * (double) xSize));
				SNR[z] = 10d * Math.log(tmpSNR / MSE) / Math.log(10d);
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		return(SNR);
	}

	/**
	 * Returns the Signal to Noise Ratio (PSNR) for all components of the image.
	 *
	 * @return the result
	 */
	public double getTotalSNR(){
		double totalSNR = 0d;
		double tmpTotalSNR = 0d;
		double totalMSE = 0d;
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				tmpTotalSNR += accumulatedSNR[z];
				totalMSE += accumulatedMSE[z];
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		tmpTotalSNR /= ((double) zSize * (double) ySize * (double) xSize);
		totalMSE /= ((double) zSize * (double) ySize * (double) xSize);
		totalSNR = 10d * Math.log(tmpTotalSNR / totalMSE) / Math.log(10d);
		return(totalSNR);
	}

	/**
	 * Returns the Peak Signal to Noise Ratio (PSNR) for each image component.
	 *
	 * @return and array of zSize positions containing the result for each component
	 */
	public double[] getPSNR(){
		double[] PSNR = new double[zSize];
		double range = Math.pow(2d, (double) bitDepth) - 1d;
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				double MSE = (accumulatedMSE[z] / ((double) ySize * (double) xSize));
				PSNR[z] = 10d * (Math.log(range * range / MSE) / Math.log(10d));
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		return(PSNR);
	}

	/**
	 * Returns the Peak Signal to Noise Ratio (PSNR) for all components of the image.
	 *
	 * @return the result
	 */
	public double getTotalPSNR(){
		double totalPSNR = 0d;
		double totalMSE = 0d;
		double range = (Math.pow(2d, (double) bitDepth) - 1);
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				totalMSE += accumulatedMSE[z];
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		totalMSE /= ((double) zSize * (double) ySize * (double) xSize);
		totalPSNR = 10d * (Math.log(range * range / totalMSE) / Math.log(10d));
		return(totalPSNR);
	}

	/**
	 * Returns the Equality for each image component.
	 *
	 * @return and array of zSize positions containing the result for each component
	 */
	public boolean[] getEQUAL(){
		boolean[] EQUAL = new boolean[zSize];
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				EQUAL[z] = accumulatedMAE[z] == 0;
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		return(EQUAL);
	}

	/**
	 * Returns the Equality for all components of the image.
	 *
	 * @return the result
	 */
	public boolean getTotalEQUAL(){
		boolean totalEQUAL = true;
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				if(accumulatedMAE[z] != 0){
					totalEQUAL = false;
				}
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		return(totalEQUAL);
	}
}