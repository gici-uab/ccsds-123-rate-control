/**
 * Copyright (C) 2013 - Francesc Auli-Llinas
 *
 * This program is distributed under the BOI License.
 * This program is distributed in the hope that it will be useful, but without any warranty; without even the implied warranty of merchantability or fitness for a particular purpose.
 * You should have received a copy of the BOI License along with this program. If not, see <http://www.deic.uab.cat/~francesc/software/license/>.
 */
package infima;

import files.ImageLoad;
import java.util.logging.Level;
import java.util.logging.Logger;
import statistics.ImageStatistics;


/**
 * This class extracts some metrics of an image.<br>
 *
 * Usage: multiple objects of this class are created, each processing different chunks of data of the image. Before creating any object of this class, the function <code>setParameters</code> must be called to initialize some variables. Then multiple objects can be manipulated by different threads until all tasks are finished.<br>
 *
 * Multithreading support: each object of the class is manipulated by one thread. Multiple objects of the class are executed simultaneously. All the objects of this class manipulate the same image.
 *
 * @author Francesc Auli-Llinas
 * @version 1.0
 */
public final class ImageStatisticsWorker implements Runnable{

	/**
	 * This instance of ImageLoad has the image.
	 * <p>
	 * The instance of the class is created by the first thread.
	 */
	private static ImageLoad imageOriginal = null;

	/**
	 * Instance of the ImageStatistics class employed to compute the metrics.
	 * <p>
	 * The instance of the class is created by the first thread.
	 */
	private static ImageStatistics statistics = null;

	/**
	 * Number of rows processed in each chunk.
	 * <p>
	 * It is initialized by the first constructor of the class depending on the image size.
	 */
	private static int yChunkSize = -1;

	/**
	 * Next chunk of data to be processed for the next available thread. It is initialized to 0 and incremented in <code>nextChunk</code> every time a new thread is available.
	 * <p>
	 * The array contains 2 positions; the first indicates the component, and the second the row.
	 */
	private static int[] globalChunk = {0, 0};

	/**
	 * Approximate number of bytes processed by each thread for each chunk of data.
	 * <p>
	 * In general, 8 KB provide good performance (this may depend on the device).
	 */
	private static final int BYTES_CHUNK = 8192;

	/**
	 * This object is useful to lock the access to the variables shared by multiple threads in the <code>nextChunk</code> function.
	 * <p>
	 * The object is never modified, its only purpose is to allow a lock.
	 */
	private static final Object lock = new Object();

	/**
	 * The chunk of data that is computed in the current thread.
	 * <p>
	 * The array contains 2 positions; the first indicates the component, and the second the row.
	 */
	private int[] threadChunk = {-1, -1};


	/**
	 * Initializes the required structures and fields.
	 *
	 * @param geometry an array with nine positions containing the geometry of the image, or null, when it is not required. Each position contains the geometry according to {@link Parameters#geometry}
	 * @param fileOriginal the original image file
	 * @throws Exception when some problem loading the image occurs
	 */
	public static void setParameters(int[] geometry, String fileOriginal) throws Exception{
		assert(imageOriginal == null);
		assert(statistics == null);

		//Creates the required instances
		if(geometry == null){
			imageOriginal = new ImageLoad(fileOriginal);
		}else{
			imageOriginal = new ImageLoad(fileOriginal, geometry);
		}
		statistics = new ImageStatistics(imageOriginal.getZSize(), imageOriginal.getYSize(), imageOriginal.getXSize(), imageOriginal.getBitDepth());

		int sampleType = imageOriginal.getSampleType();
		int multBytesType = ImageLoad.getTypeNumBytes(sampleType);

		//Sets yChunkSize
		yChunkSize = 1;
		int zSize = imageOriginal.getZSize();
		int ySize = imageOriginal.getYSize();
		int xSize = imageOriginal.getXSize();
		if(imageOriginal.getDataOrder() == 0){
			if(xSize * multBytesType < BYTES_CHUNK){
				yChunkSize = BYTES_CHUNK / (xSize * multBytesType);
			}
		}else{
			if(xSize * zSize * multBytesType < BYTES_CHUNK){
				yChunkSize = BYTES_CHUNK / (xSize * zSize * multBytesType);
			}
		}
		yChunkSize = yChunkSize <= 0 ? 1: yChunkSize;
		yChunkSize = yChunkSize > ySize ? ySize: yChunkSize;
	}

	/**
	 * Sets the data chunk for this thread.
	 */
	public ImageStatisticsWorker(){
		nextChunk();
	}

	/**
	 * Sets the chunk of data that this thread processes.
	 */
	private void nextChunk(){
		synchronized(lock){
			if(imageOriginal.getDataOrder() == 0){
				if(globalChunk[0] < imageOriginal.getZSize()){
					threadChunk[0] = globalChunk[0];
					threadChunk[1] = globalChunk[1];
					if(globalChunk[1] + yChunkSize < imageOriginal.getYSize()){
						globalChunk[1] += yChunkSize;
					}else{
						globalChunk[0]++;
						globalChunk[1] = 0;
					}
				}else{
					threadChunk[0] = -1;
				}
			}else{
				if(globalChunk[1] < imageOriginal.getYSize()){
					threadChunk[0] = 0;
					threadChunk[1] = globalChunk[1];
					globalChunk[1] += yChunkSize;
				}else{
					threadChunk[0] = -1;
				}
			}
		}
	}

	/**
	 * Computes the entropy of the image for one or more step sizes.
	 */
	@Override public void run(){
		try{
			while(threadChunk[0] != -1){
				int z = threadChunk[0];
				int yBegin = threadChunk[1];
				int zSize = imageOriginal.getZSize();
				int ySize = imageOriginal.getYSize();
				int xSize = imageOriginal.getXSize();

				int yLength = yChunkSize;
				if(yBegin + yChunkSize > ySize){
					yLength = ySize - yBegin;
				}

				if(imageOriginal.getDataOrder() == 0){
					float[][][] chunkOriginal = imageOriginal.getImageChunk(z, yBegin, 0, 1, yLength, xSize, 0, 0);
					assert(chunkOriginal.length == 1);
					statistics.accumulateStatistics(chunkOriginal[0], z);
					chunkOriginal = null;
				}else{
					float[][][] chunkOriginal = imageOriginal.getImageChunk(z, yBegin, 0, zSize, yLength, xSize, 0, 0);
					assert(chunkOriginal.length == zSize);
					statistics.accumulateStatistics(chunkOriginal, 0);
					chunkOriginal = null;
				}

				//Get next step size to compute in this thread
				nextChunk();
			}
		}catch(Exception e){
			Logger.getLogger(ImageStatisticsWorker.class.getName()).log(Level.SEVERE, null, e);
			System.exit(0);
		}
	}

	/**
	 * Returns the instance of the ImageCompare class employed to perform the comparison. This function should only be called when all threads have finished their execution.
	 *
	 * @return the instance of the ImageComparison
	 */
	public static ImageStatistics getStatistics(){
		return(statistics);
	}
}
