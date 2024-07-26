/**
 * Copyright (C) 2013 - Francesc Auli-Llinas
 *
 * This program is distributed under the BOI License.
 * This program is distributed in the hope that it will be useful, but without any warranty; without even the implied warranty of merchantability or fitness for a particular purpose.
 * You should have received a copy of the BOI License along with this program. If not, see <http://www.deic.uab.cat/~francesc/software/license/>.
 */
package infima;

import files.ImageLoad;
import statistics.ImageCompare;
import statistics.ImageStatistics;


/**
 * Main class of Infima.<br>
 *
 * Usage: once the main method is called, the program's arguments are processed and the program enters in a single or two files mode. In each of these modes, multiple threads are executed to compute various metrics of the images.<br>
 *
 * Multithreading support: the class must be manipulated by a single thread. There can NOT be multiple objects of this class.
 *
 * @author Francesc Auli-Llinas
 * @version 1.0
 */
public final class Infima{

	/**
	 * Version of Infima.
	 */
	protected final static String INFIMA_VERSION = "1.0";


	/**
	 * Main method of Infima.
	 *
	 * @param args an array of strings containing program's parameters.
	 * @throws Exception when some error occurs
	 */
	public static void main(String[] args) throws Exception{

		//Parses and checks the program's parameters
		Parameters.parseArguments(args);
		Parameters.checkParameters();

		if(Parameters.getFileCompare().equalsIgnoreCase("")){
			singleFileMode();
		}else{
			twoFilesMode();
		}
	}

	/**
	 * Extracts metrics from an image.
	 *
	 * @throws Exception when some problem occurs
	 */
	private static void singleFileMode() throws Exception{
		
		int format = Parameters.getFormat();
		int totals = Parameters.getTotals();

		if((format < 0) || (format > 1)){
			throw new Exception("Unsupported format.");
		}
		if((totals < 0) || (totals > 3)){
			throw new Exception("Unsupported totals.");
		}

		if(totals == 3){
			//Prints only information of the images
			ImageLoad imageOriginal;
			if(Parameters.getGeometry() == null){
				imageOriginal = new ImageLoad(Parameters.getFileOriginal());
			}else{
				imageOriginal = new ImageLoad(Parameters.getFileOriginal(), Parameters.getGeometry());
			}

			String uncompressedRawData = String.format("%.2f", ((double) imageOriginal.getZSize() * (double) imageOriginal.getYSize() * (double) imageOriginal.getXSize() * (double) imageOriginal.getBitDepth()) / (double) (8 * (1 << 20)));
			String bytes1BPS = String.format("%.2f", ((double) imageOriginal.getZSize() * (double) imageOriginal.getYSize() * (double) imageOriginal.getXSize()) / 8d);
			if(format == 0){
				System.out.println("File " + Parameters.getFileOriginal());
				System.out.println("  zSize         : " + imageOriginal.getZSize());
				System.out.println("  ySize         : " + imageOriginal.getYSize());
				System.out.println("  xSize         : " + imageOriginal.getXSize());
				System.out.println("  sampleType    : " + imageOriginal.getSampleType());
				System.out.println("  signedType    : " + imageOriginal.getSignedType());
				System.out.println("  bitDepth      : " + imageOriginal.getBitDepth());
				System.out.println("  byteOrder     : " + imageOriginal.getByteOrder());
				System.out.println("  dataOrder     : " + imageOriginal.getDataOrder());
				System.out.println("  componentsRGB : " + imageOriginal.getComponentsRGB());
				System.out.println("  rawDataSize   : " + uncompressedRawData + " MiB");
				System.out.println("  bytes at 1bps : " + bytes1BPS);
			}else{
				System.out.println(Parameters.getFileOriginal() + " " + imageOriginal.getZSize() + " " + imageOriginal.getYSize() + " " + imageOriginal.getXSize() + " " + imageOriginal.getSampleType() + " " + imageOriginal.getSignedType() + " " + imageOriginal.getBitDepth() + " " + imageOriginal.getByteOrder() + " " + imageOriginal.getDataOrder() + " " + imageOriginal.getComponentsRGB() + " " + uncompressedRawData + " " + bytes1BPS);
			}

		}else{
			ImageStatisticsWorker.setParameters(Parameters.getGeometry(), Parameters.getFileOriginal());

			//Starts the threads
			int numThreads = Parameters.getThreadsNumber();
			ImageStatisticsWorker[] workers = new ImageStatisticsWorker[numThreads];
			Thread[] threads = new Thread[numThreads];
			for(int thread = 0; thread < numThreads; thread++){
				workers[thread] = new ImageStatisticsWorker();
				threads[thread] = new Thread(workers[thread]);
				threads[thread].start();
			}

			//Finishes the threads
			for(int thread = 0; thread < numThreads; thread++){
				threads[thread].join();
			}

			//Gets the results
			ImageStatistics comparison = workers[0].getStatistics();
			double[] min = comparison.getMin();
			double totalMin = comparison.getTotalMin();
			double[] max = comparison.getMax();
			double totalMax = comparison.getTotalMax();
			double[] center = comparison.getCenterRange();
			double totalCenter = comparison.getTotalCenterRange();
			double[] average = comparison.getAverage();
			double totalAverage = comparison.getTotalAverage();
			double[] energy = comparison.getEnergy();
			double totalEnergy = comparison.getTotalEnergy();

			//Prints the metrics
			int[] metrics = Parameters.getMetrics();
			int zSize = min.length;
			boolean displayALL = false;
			boolean displayMin = false;
			boolean displayMax = false;
			boolean displayCenter = false;
			boolean displayAverage = false;
			boolean displayEnergy = false;
			for(int m = 0; m < metrics.length; m++){
				switch(metrics[m]){
				case 0:{
					displayALL = true;
				}break;
				case 1:{
					displayMin = true;
				}break;
				case 2:{
					displayMax = true;
				}break;
				case 3:{
					displayCenter = true;
				}break;
				case 4:{
					displayAverage = true;
				}break;
				case 5:{
					displayEnergy = true;
				}break;
				default:
					throw new Exception("Unsupported metric.");
				}
			}

			if(totals == 2){
				for(int z = 0; z < zSize; z++){
					if(format == 0){
						System.out.println("COMPONENT " + z);
					}else{
						System.out.print(z + " ");
					}
					if(displayALL || displayMin){
						if(format == 0){
							System.out.println("  MIN         : " + min[z]);
						}else{
							System.out.print(min[z] +  " ");
						}
					}
					if(displayALL || displayMax){
						if(format == 0){
							System.out.println("  MAX         : " + max[z]);
						}else{
							System.out.print(max[z] + " ");
						}
					}
					if(displayALL || displayCenter){
						if(format == 0){
							System.out.println("  RANGE CENTER: " + center[z]);
						}else{
							System.out.print(center[z] + " ");
						}
					}
					if(displayALL || displayAverage){
						if(format == 0){
							System.out.println("  AVERAGE     : " + average[z]);
						}else{
							System.out.print(average[z] + " ");
						}
					}
					if(displayALL || displayEnergy){
						if(format == 0){
							System.out.println("  ENERGY      : " + energy[z]);
						}else{
							System.out.print(energy[z] + " ");
						}
					}
					if(format == 1){
						System.out.print("\n");
					}
				}
			}

			if(totals >= 1){
				if(format == 0){
					System.out.println("TOTALS");
				}else{
					System.out.print("TOTALS ");
				}
				if(displayALL || displayMin){
					if(format == 0){
						System.out.println("  MIN         : " + totalMin);
					}else{
						System.out.print(totalMin + " ");
					}
				}
				if(displayALL || displayMax){
					if(format == 0){
						System.out.println("  MAX         : " + totalMax);
					}else{
						System.out.print(totalMax + " ");
					}
				}
				if(displayALL || displayCenter){
					if(format == 0){
						System.out.println("  RANGE CENTER: " + totalCenter);
					}else{
						System.out.print(totalCenter + " ");
					}
				}
				if(displayALL || displayAverage){
					if(format == 0){
						System.out.println("  AVERAGE     : " + totalAverage);
					}else{
						System.out.print(totalAverage + " ");
					}
				}
				if(displayALL || displayEnergy){
					if(format == 0){
						System.out.println("  ENERGY      : " + totalEnergy);
					}else{
						System.out.print(totalEnergy + " ");
					}
				}
				if(format == 1){
					System.out.print("\n");
				}
			}
		}
	}

	/**
	 * Compares two images.
	 *
	 * @throws Exception when some problem occurs
	 */
	private static void twoFilesMode() throws Exception{
		int format = Parameters.getFormat();
		int totals = Parameters.getTotals();

		if((format < 0) || (format > 1)){
			throw new Exception("Unsupported format.");
		}
		if((totals < 0) || (totals > 3)){
			throw new Exception("Unsupported totals.");
		}

		if(totals == 3){
			//Prints only information of the images
			ImageLoad imageOriginal;
			ImageLoad imageCompare;

			if(Parameters.getGeometry() == null){
				imageOriginal = new ImageLoad(Parameters.getFileOriginal());
				imageCompare = new ImageLoad(Parameters.getFileCompare());
			}else{
				imageOriginal = new ImageLoad(Parameters.getFileOriginal(), Parameters.getGeometry());
				imageCompare = new ImageLoad(Parameters.getFileCompare(), Parameters.getGeometry());
			}

			String uncompressedRawData = String.format("%.2f", ((double) imageOriginal.getZSize() * (double) imageOriginal.getYSize() * (double) imageOriginal.getXSize() * (double) imageOriginal.getBitDepth()) / (double) (8 * (1 << 20)));
			String bytes1BPS = String.format("%.2f", ((double) imageOriginal.getZSize() * (double) imageOriginal.getYSize() * (double) imageOriginal.getXSize()) / 8d);
			if(format == 0){
				System.out.println("File " + Parameters.getFileOriginal());
				System.out.println("  zSize         : " + imageOriginal.getZSize());
				System.out.println("  ySize         : " + imageOriginal.getYSize());
				System.out.println("  xSize         : " + imageOriginal.getXSize());
				System.out.println("  sampleType    : " + imageOriginal.getSampleType());
				System.out.println("  signedType    : " + imageOriginal.getSignedType());
				System.out.println("  bitDepth      : " + imageOriginal.getBitDepth());
				System.out.println("  byteOrder     : " + imageOriginal.getByteOrder());
				System.out.println("  dataOrder     : " + imageOriginal.getDataOrder());
				System.out.println("  componentsRGB : " + imageOriginal.getComponentsRGB());
				System.out.println("  rawDataSize   : " + uncompressedRawData + " MiB");
				System.out.println("  bytes at 1bps : " + bytes1BPS);

				System.out.println("File " + Parameters.getFileCompare());
				System.out.println("  zSize         : " + imageCompare.getZSize());
				System.out.println("  ySize         : " + imageCompare.getYSize());
				System.out.println("  xSize         : " + imageCompare.getXSize());
				System.out.println("  sampleType    : " + imageCompare.getSampleType());
				System.out.println("  signedType    : " + imageCompare.getSignedType());
				System.out.println("  bitDepth      : " + imageCompare.getBitDepth());
				System.out.println("  byteOrder     : " + imageCompare.getByteOrder());
				System.out.println("  dataOrder     : " + imageCompare.getDataOrder());
				System.out.println("  componentsRGB : " + imageCompare.getComponentsRGB());
				System.out.println("  rawDataSize   : " + uncompressedRawData + " MiB");
				System.out.println("  bytes at 1bps : " + bytes1BPS);
			}else{
				System.out.println(Parameters.getFileOriginal() + " " + imageOriginal.getZSize() + " " + imageOriginal.getYSize() + " " + imageOriginal.getXSize() + " " + imageOriginal.getSampleType() + " " + imageOriginal.getSignedType() + " " + imageOriginal.getBitDepth() + " " + imageOriginal.getByteOrder() + " " + imageOriginal.getDataOrder() + " " + imageOriginal.getComponentsRGB() + " " + uncompressedRawData + " " + bytes1BPS);
				System.out.println(Parameters.getFileCompare() + " " + imageCompare.getZSize() + " " + imageCompare.getYSize() + " " + imageCompare.getXSize() + " " + imageCompare.getSampleType() + " " + imageCompare.getSignedType() + " " + imageCompare.getBitDepth() + " " + imageCompare.getByteOrder() + " " + imageCompare.getDataOrder() + " " + imageCompare.getComponentsRGB() + " " + uncompressedRawData + " " + bytes1BPS);
			}

		}else{
			ImageCompareWorker.setParameters(Parameters.getGeometry(), Parameters.getFileOriginal(), Parameters.getFileCompare());

			//Starts the threads
			int threadsNumber = Parameters.getThreadsNumber();
			ImageCompareWorker[] workers = new ImageCompareWorker[threadsNumber];
			Thread[] threads = new Thread[threadsNumber];
			for(int thread = 0; thread < threadsNumber; thread++){
				workers[thread] = new ImageCompareWorker();
				threads[thread] = new Thread(workers[thread]);
				threads[thread].start();
			}

			//Finishes the threads
			for(int thread = 0; thread < threadsNumber; thread++){
				threads[thread].join();
			}

			//Gets the results
			ImageCompare comparison = workers[0].getComparison();
			double[] mae = comparison.getMAE();
			double totalMAE = comparison.getTotalMAE();
			double[] pae = comparison.getPAE();
			double totalPAE = comparison.getTotalPAE();
			double[] mse = comparison.getMSE();
			double totalMSE = comparison.getTotalMSE();
			double[] rmse = comparison.getRMSE();
			double totalRMSE = comparison.getTotalRMSE();
			double[] me = comparison.getME();
			double totalME = comparison.getTotalME();
			double[] snr = comparison.getSNR();
			double totalSNR = comparison.getTotalSNR();
			double[] psnr = comparison.getPSNR();
			double totalPSNR = comparison.getTotalPSNR();
			boolean[] equal = comparison.getEQUAL();
			boolean totalEQUAL = comparison.getTotalEQUAL();

			//Prints the metrics
			int[] metrics = Parameters.getMetrics();
			int zSize = mae.length;
			boolean displayALL = false;
			boolean displayMAE = false;
			boolean displayPAE = false;
			boolean displayMSE = false;
			boolean displayRMSE = false;
			boolean displayME = false;
			boolean displaySNR = false;
			boolean displayPSNR = false;
			boolean displayEQUAL = false;
			for(int m = 0; m < metrics.length; m++){
				switch(metrics[m]){
				case 0:{
					displayALL = true;
				}break;
				case 1:{
					displayMAE = true;
				}break;
				case 2:{
					displayPAE = true;
				}break;
				case 3:{
					displayMSE = true;
				}break;
				case 4:{
					displayRMSE = true;
				}break;
				case 5:{
					displayME = true;
				}break;
				case 6:{
					displaySNR = true;
				}break;
				case 7:{
					displayPSNR = true;
				}break;
				case 8:{
					displayEQUAL = true;
				}break;
				default:
					throw new Exception("Unsupported metric.");
				}
			}

			if(totals == 2){
				for(int z = 0; z < zSize; z++){
					if(format == 0){
						System.out.println("COMPONENT " + z);
					}else{
						System.out.print(z + " ");
					}
					if(displayALL || displayMAE){
						if(format == 0){
							System.out.println("  MAE  : " + mae[z]);
						}else{
							System.out.print(mae[z] +  " ");
						}
					}
					if(displayALL || displayPAE){
						if(format == 0){
							System.out.println("  PAE  : " + pae[z]);
						}else{
							System.out.print(pae[z] + " ");
						}
					}
					if(displayALL || displayMSE){
						if(format == 0){
							System.out.println("  MSE  : " + mse[z]);
						}else{
							System.out.print(mse[z] + " ");
						}
					}
					if(displayALL || displayRMSE){
						if(format == 0){
							System.out.println("  RMSE : " + rmse[z]);
						}else{
							System.out.print(rmse[z] + " ");
						}
					}
					if(displayALL || displayME){
						if(format == 0){
							System.out.println("  ME   : " + me[z]);
						}else{
							System.out.print(me[z] + " ");
						}
					}
					if(displayALL || displaySNR){
						if(format == 0){
							System.out.println("  SNR  : " + snr[z]);
						}else{
							System.out.print(snr[z] + " ");
						}
					}
					if(displayALL || displayPSNR){
						if(format == 0){
							System.out.println("  PSNR : " + psnr[z]);
						}else{
							System.out.print(psnr[z] + " ");
						}
					}
					if(displayALL || displayEQUAL){
						if(format == 0){
							System.out.println("  EQUAL: " + equal[z]);
						}else{
							System.out.print(equal[z] + " ");
						}
					}
					if(format == 1){
						System.out.print("\n");
					}
				}
			}

			if(totals >= 1){
				if(format == 0){
					System.out.println("TOTALS");
				}else{
					System.out.print("TOTALS ");
				}
				if(displayALL || displayMAE){
					if(format == 0){
						System.out.println("  MAE  : " + totalMAE);
					}else{
						System.out.print(totalMAE + " ");
					}
				}
				if(displayALL || displayPAE){
					if(format == 0){
						System.out.println("  PAE  : " + totalPAE);
					}else{
						System.out.print(totalPAE + " ");
					}
				}
				if(displayALL || displayMSE){
					if(format == 0){
						System.out.println("  MSE  : " + totalMSE);
					}else{
						System.out.print(totalMSE + " ");
					}
				}
				if(displayALL || displayRMSE){
					if(format == 0){
						System.out.println("  RMSE : " + totalRMSE);
					}else{
						System.out.print(totalRMSE + " ");
					}
				}
				if(displayALL || displayME){
					if(format == 0){
						System.out.println("  ME   : " + totalME);
					}else{
						System.out.print(totalME + " ");
					}
				}
				if(displayALL || displaySNR){
					if(format == 0){
						System.out.println("  SNR  : " + totalSNR);
					}else{
						System.out.print(totalSNR + " ");
					}
				}
				if(displayALL || displayPSNR){
					if(format == 0){
						System.out.println("  PSNR : " + totalPSNR);
					}else{
						System.out.print(totalPSNR + " ");
					}
				}
				if(displayALL || displayEQUAL){
					if(format == 0){
						System.out.println("  EQUAL: " + totalEQUAL);
					}else{
						System.out.print(totalEQUAL + " ");
					}
				}
				if(format == 1){
					System.out.print("\n");
				}
			}
		}
	}
}
