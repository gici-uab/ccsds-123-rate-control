/**
 * Copyright (C) 2013 - Francesc Auli-Llinas
 *
 * This program is distributed under the BOI License.
 * This program is distributed in the hope that it will be useful, but without any warranty; without even the implied warranty of merchantability or fitness for a particular purpose.
 * You should have received a copy of the BOI License along with this program. If not, see <http://www.deic.uab.cat/~francesc/software/license/>.
 */
package statistics;


/**
 * This class computes some statistics of an image such as min, max, average, etc.
 *
 * Usage: the constructor receives basic parameters and initializes the structures of the class. Then, the images can be compared incrementally passing chunks of the images to the <code>accumulateStatistics</code> function. Once all chunks of data are passed, the computed metrics are extracted via the <code>get</code> functions. To compare another two images, a new object has to be created.
 *
 * Multithreading support: once the object is created, multiple threads can call the function <code>accumulateStatistics</code>. There can be many objects of this class running simultaneously, one for each image comparison.
 *
 * @author Francesc Auli-Llinas
 * @version 1.1
 */
public final class ImageStatistics{

	/**
	 * Number of components of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	private int zSize = -1;

	/**
	 * Number of rows of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	private int ySize = -1;

	/**
	 * Number of columns of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	private int xSize = -1;

	/**
	 * Bit depth of the image.
	 * <p>
	 * Only positive values allowed.
	 */
	private int bitDepth = -1;

	/**
	 * Number of samples computed for each component.
	 * <p>
	 * Statistics accumulated in the <code>accumulateStatistics</code> functions.
	 */
	private long[] accumulatedSamples = null;

	/**
	 * Accumulated minimum value for each component.
	 * <p>
	 * Statistics accumulated in the <code>accumulateStatistics</code> functions.
	 */
	private double[] accumulatedMin = null;

	/**
	 * Accumulated maximum value for each component.
	 * <p>
	 * Statistics accumulated in the <code>accumulateStatistics</code> functions.
	 */
	private double[] accumulatedMax = null;

	/**
	 * Accumulated average for each component.
	 * <p>
	 * Statistics accumulated in the <code>accumulateStatistics</code> functions.
	 */
	private double[] accumulatedAverage = null;

	/**
	 * Accumulated energy for each component.
	 * <p>
	 * Statistics accumulated in the <code>accumulateStatistics</code> functions.
	 */
	private double[] accumulatedEnergy = null;

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
	public ImageStatistics(int zSize, int ySize, int xSize, int bitDepth) throws Exception{
		//Set sizes
		this.zSize = zSize;
		this.ySize = ySize;
		this.xSize = xSize;
		this.bitDepth = bitDepth;

		//Memory allocation
		accumulatedSamples = new long[zSize];
		accumulatedMin = new double[zSize];
		accumulatedMax = new double[zSize];
		accumulatedAverage = new double[zSize];
		accumulatedEnergy = new double[zSize];
		for(int z = 0; z < zSize; z++){
			accumulatedMin[z] = Double.MAX_VALUE;
			accumulatedMax[z] = Double.MIN_VALUE;
		}
	}

	/**
	 * Accumulates statistics for a chunk of the image belonging to one component.
	 *
	 * @param imageChunk a 2D array of floats containing the image samples (indices of the array are [y][x])
	 * @param z component to which the chunks belong
	 * @throws Exception when the component is not valid
	 */
	public void accumulateStatistics(float[][] imageChunk, int z) throws Exception{
		if((z < 0) || (z >= zSize)){
			throw new Exception("Invalid component " + z + ".");
		}

		//Accumulate statistics
		long accumulatedSamplesTMP = 0;
		double accumulatedMinTMP = Double.MAX_VALUE;
		double accumulatedMaxTMP = Double.MIN_VALUE;
		double accumulatedAverageTMP = 0;
		double accumulatedEnergyTMP = 0;
		for(int y = 0; y < imageChunk.length; y++){
		for(int x = 0; x < imageChunk[y].length; x++){
			accumulatedSamplesTMP++;
			float sample = imageChunk[y][x];

			if(sample < accumulatedMinTMP){
				accumulatedMinTMP = sample;
			}
			if(sample > accumulatedMaxTMP){
				accumulatedMaxTMP = sample;
			}
			accumulatedAverageTMP += sample;
			accumulatedEnergyTMP += sample * sample;
		}}

		synchronized(lock){
			accumulatedSamples[z] += accumulatedSamplesTMP;
			if(accumulatedMinTMP < accumulatedMin[z]){
				accumulatedMin[z] = accumulatedMinTMP;
			}
			if(accumulatedMaxTMP > accumulatedMax[z]){
				accumulatedMax[z] = accumulatedMaxTMP;
			}
			accumulatedAverage[z] += accumulatedAverageTMP;
			accumulatedEnergy[z] += accumulatedEnergyTMP;
		}
	}

	/**
	 * Accumulated statistics for a chunk of the image belonging to some (possibly all) consecutive components.
	 *
	 * @param imageChunk a 3D array of floats containing the image samples (indices of the array are [z][y][x])
	 * @param firstZ first component of the chunk
	 * @throws Exception when the component is not valid
	 */
	public void accumulateStatistics(float[][][] imageChunk, int firstZ) throws Exception{
		//Accumulate statistics
		for(int z = 0; z < imageChunk.length; z++){
			accumulateStatistics(imageChunk[z], firstZ + z);
		}
	}

	/**
	 * Returns the minimum sample for each image component.
	 *
	 * @return and array of zSize positions containing the result for each component
	 */
	public double[] getMin(){
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] != (long) ySize * (long) xSize){
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		return(accumulatedMin);
	}

	/**
	 * Returns the minimum sample of the image.
	 *
	 * @return the result
	 */
	public double getTotalMin(){
		double totalMin = Double.MAX_VALUE;
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				if(accumulatedMin[z] < totalMin){
					totalMin = accumulatedMin[z];
				}
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		return(totalMin);
	}

	/**
	 * Returns the maximum sample for each image component.
	 *
	 * @return and array of zSize positions containing the result for each component
	 */
	public double[] getMax(){
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] != (long) ySize * (long) xSize){
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		return(accumulatedMax);
	}

	/**
	 * Returns the maximum sample of the image.
	 *
	 * @return the result
	 */
	public double getTotalMax(){
		double totalMax = Double.MIN_VALUE;
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				if(accumulatedMax[z] > totalMax){
					totalMax = accumulatedMax[z];
				}
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		return(totalMax);
	}

	/**
	 * Returns the range center for each image component.
	 *
	 * @return and array of zSize positions containing the result for each component
	 */
	public double[] getCenterRange(){
		double[] center = new double[zSize];
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				center[z] = (accumulatedMin[z] + accumulatedMax[z]) / 2d;
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		return(center);
	}

	/**
	 * Returns the range center of the image.
	 *
	 * @return the result
	 */
	public double getTotalCenterRange(){
		double totalCenter = 0;
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				totalCenter += (accumulatedMin[z] + accumulatedMax[z]) / 2d;
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		totalCenter /= (double) zSize;
		return(totalCenter);
	}

	/**
	 * Returns the average for each image component.
	 *
	 * @return and array of zSize positions containing the result for each component
	 */
	public double[] getAverage(){
		double[] average = new double[zSize];
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				average[z] = (accumulatedAverage[z] / ((double) ySize * (double) xSize));
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		return(average);
	}

	/**
	 * Returns the average for all components of the image.
	 *
	 * @return the result
	 */
	public double getTotalAverage(){
		double totalAverage = 0d;
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				totalAverage += accumulatedAverage[z];
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		totalAverage /= ((double) zSize * (double) ySize * (double) xSize);
		return(totalAverage);
	}

	/**
	 * Returns the energy for each image component.
	 *
	 * @return and array of zSize positions containing the result for each component
	 */
	public double[] getEnergy(){
		double[] energy = new double[zSize];
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				energy[z] = (accumulatedEnergy[z] / ((double) ySize * (double) xSize));
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		return(energy);
	}

	/**
	 * Returns the energy for all components of the image.
	 *
	 * @return the result
	 */
	public double getTotalEnergy(){
		double totalEnergy = 0d;
		for(int z = 0; z < zSize; z++){
			if(accumulatedSamples[z] == (long) ySize * (long) xSize){
				totalEnergy += accumulatedEnergy[z];
			}else{
				throw new Error("Incorrect number of samples accumulated for component " + z + ".");
			}
		}
		totalEnergy /= ((double) zSize * (double) ySize * (double) xSize);
		return(totalEnergy);
	}
}