package Gstat;
import GiciAnalysis.*;
import GiciException.*;
import GiciFile.*;
import java.io.*;
import java.text.NumberFormat;

/**
 * Application to show some statitistical of an image.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public class Gstat{

	/**
	 * Main method of Gstat application. It takes program arguments, loads images extracts some statisticals.
	 *
	 * @param args an array of strings that contains program parameters
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException{
		//Parse arguments
		GstatParser parser = null;
		RandomAccessFile energyFile = null;
		RandomAccessFile varianceFile = null;
		RandomAccessFile entropyFile = null;
		try{
			parser = new GstatParser(args);
		}catch(ErrorException e){
			System.out.println("RUN ERROR:");
			e.printStackTrace();
			System.out.println("Please report this error (specifying image type and parameters) to: gici-dev@abra.uab.es");
			System.exit(1);
		}catch(ParameterException e){
			System.out.println("ARGUMENTS ERROR: " +  e.getMessage());
			e.printStackTrace();
			System.exit(2);
		}

		//Image load
		String imageFile = parser.getImageFile();
		int[]  imageGeometry = parser.getImageGeometry();
		int[] values = parser.getValues(); 
		LoadFile image = null;
		try{
			if(LoadFile.isRaw(imageFile)){
				image = new LoadFile(imageFile, imageGeometry[0], imageGeometry[1], imageGeometry[2], imageGeometry[3], imageGeometry[4], false);
			}else{
				image = new LoadFile(imageFile);
			}
		}catch(IllegalArgumentException e){
			System.out.println("IMAGE LOAD ERROR Valid formats are: pgm, ppm, pbm, jpg, tiff, png, bmp, gif, fpx. If image is raw data file extension must be \".raw\" or \".img\"");
			System.exit(3);
			
		}catch(WarningException e){
			System.out.println("IMAGE LOAD ERROR: " + e.getMessage());
			System.exit(3);
		}
		
		//Images statisticals
		ImageStatistical is = new ImageStatistical(image.getImage());
		double[][] minMax = is.getMinMax();
		double[] totalMinMax = is.getTotalMinMax();
		double[] average = is.getAverage();
		double totalAverage = is.getTotalAverage();
		double[] centerRange = is.getCenterRange();
		double totalCenterRange = is.getTotalCenterRange();
		int[][] countedValues = is.getcountedValues();
		double[] energy = is.getEnergy();
		double totalEnergy = is.getTotalEnergy();
		double[] varianze = is.getVariance();
		double totalVarianze = is.getTotalVariance();
		double[] entropy = is.getEntropy();
		double totalEntropy = is.getTotalEntropy();
        double[] morans = is.getMoransMeasure();
        double totalMorans = is.getTotalMoransMeasure();
       
        float[][][] imageSamples = image.getImage();
        
        float [] imagehist = new float[imageSamples.length*imageSamples[0].length*imageSamples[0][0].length];
		for (int z = 0; z < imageSamples.length; z ++) {
		for (int y = 0; y < imageSamples[0].length; y ++) {
		for (int x = 0; x < imageSamples[0][0].length; x ++) {
			//imagehist[z*imageSamples[0].length*imageSamples[0][0].length+(y*imageSamples[0].length)+x] = imageSamples[z][y][x];
			imagehist[0] = imageSamples[z][y][x];
		}}}
		
        //Show metrics
		int statistic = parser.getStatistic();
		int totals = parser.getTotals();
		int format = parser.getFormat();
		
		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;
		
		//checking parameters to count values
		if(statistic >= 5 && statistic <= 6 && values == null){
			System.out.println("ARGUMENTS ERROR: For this measure specific value/s is/are required");
			System.exit(3);
		}
		if(statistic >= 5 && statistic <= 8){
			int[] samplesType = image.getPixelBitDepth();
			for(int z = 0; z < samplesType.length; z++){
				if(samplesType[z] != 8){
					System.out.println("ERROR image must be Byte type");
					System.exit(3);
				}
			}
		}
		
		if(statistic == 1 || statistic == 9){
			energyFile = new RandomAccessFile(imageFile+"_energy", "rw");
		}
		if(statistic == 1 || statistic == 10){
			varianceFile = new RandomAccessFile(imageFile+"_variance", "rw");
		} 
		if(statistic == 1 || statistic == 11){
			entropyFile = new RandomAccessFile(imageFile+"_entropy", "rw");
		}
		
		if(((zSize > 1) && (totals <= 1)) || (zSize == 1)){
			for(int z = 0; z < zSize; z++){
				if((statistic > 0) && (format == 0)) System.out.println("COMPONENT " + z + ":");
				if((statistic == 1) || (statistic == 2)){
					if(format == 0) System.out.println("  MIN         : " +(float) minMax[z][0]);
					if(format == 1) System.out.print((float)minMax[z][0]);
				}
				if((statistic == 1) && (format == 1)) System.out.print(":");
				if((statistic == 1) || (statistic == 2)){
					if(format == 0) System.out.println("  MAX         : " +(float) minMax[z][1]);
					if(format == 1) System.out.print(":"+(float)minMax[z][1]);
				}
				if((statistic == 1) && (format == 1)) System.out.print(":");
				if((statistic == 1) || (statistic == 3)){
					if(format == 0) System.out.println("  AVERAGE     : " + (float)average[z]);
					if(format == 1) System.out.print((float)average[z]);
				}
				if((statistic == 1) && (format == 1)) System.out.print(":");
				if((statistic == 1) || (statistic == 4)){
					if(format == 0) System.out.println("  CENTER RANGE: " + (float)centerRange[z]);
					if(format == 1) System.out.print((float)centerRange[z]);
				}
				if((statistic == 5) && (format == 1)) System.out.print(":");
				if(statistic == 5){
					if(format == 0){
						for(int value = 0; value < values.length; value++){
							System.out.println(" - NUMBER OF PIXELS WITH VALUE = "+values[value]+" ARE "+countedValues[z][values[value]]);
						}
					}
					if(format == 1){
						for(int value = 0; value < values.length; value++){
							System.out.println(values[value]+" - "+countedValues[z][values[value]]);
						}
					}
				}
				if((statistic == 6) && (format == 1)) System.out.print(":");
				if(statistic == 6){
					if(format == 0){
						for(int value = 0; value < values.length; value++){
							System.out.println(" - % OF PIXELS VALUES = "+values[value]+" ARE "+((float)countedValues[z][values[value]] * 100  /(zSize*xSize*ySize))+"%");
						}
					}
					if(format == 1){
						for(int value = 0; value < values.length; value++){
							System.out.println(values[value]+" - "+((float)countedValues[z][values[value]] * 100  /(zSize*xSize*ySize))+"%");
						}
					}
				}
				if((statistic == 7) && (format == 1)) System.out.print(":");
				if(statistic == 7){
					if(format == 0){
						for(int value = 0; value < countedValues[z].length; value++){
								System.out.println(" - NUMBER OF PIXELS WITH VALUE = "+value+" ARE "+countedValues[z][value]);
						}
					}
					if(format == 1){
						for(int value = 0; value < countedValues[z].length; value++){
							System.out.println(value+" - "+countedValues[z][value]);
						}
					}
				}
				if((statistic == 8) && (format == 1)) System.out.print(":");
				if(statistic == 8){
					if(format == 0){
						for(int value = 0; value < countedValues[z].length; value++){
							System.out.println(" - % OF PIXELS WITH VALUE = "+value+" ARE "+((float)countedValues[z][value] * 100  /(zSize*xSize*ySize))+"%");	
						}
					}
					if(format == 1){
						for(int value = 0; value < countedValues[z].length; value++){
							System.out.println(value+" - "+((float)countedValues[z][value] * 100  /(zSize*xSize*ySize))+"%");
						}
					}
				}
				if(statistic == 13){
					Histogram histogram = new Histogram(imagehist, (float)1);
					histogram.call();
					int counts[] = histogram.getCounts();
					long totalcounts = 0;
					for(int i = 0; i < counts.length; i++){
						totalcounts = totalcounts + counts[i];
					}
					for(int i = 0; i < counts.length; i++){
						float prob = (float)counts[i]/(float)totalcounts;
						System.out.println(i+" "+prob);
					}
				}
				if((statistic == 1) && (format == 1)) System.out.print(":");
				if((statistic == 1) || (statistic == 9)){
					NumberFormat f = NumberFormat.getInstance();
					f.setMaximumFractionDigits(100);
					f.setGroupingUsed(false);
					if(format == 0) System.out.println("  ENERGY      : " + (energy[z])); 
					if(format == 1) System.out.print((energy[z]));
					energyFile.writeBytes(String.valueOf(f.format(energy[z])));
					energyFile.writeBytes("\n");
				}
				if((statistic == 1) && (format == 1)) System.out.print(":");
				if((statistic == 1) || (statistic == 10)){
					NumberFormat f = NumberFormat.getInstance();
					f.setMaximumFractionDigits(100);
					f.setGroupingUsed(false);
					if(format == 0) System.out.println("  VARIANCE    : " + (varianze[z]));
					if(format == 1) System.out.print((varianze[z]));
					varianceFile.writeBytes(String.valueOf(f.format(varianze[z])));
					varianceFile.writeBytes("\n");
				}
				if((statistic == 1) && (format == 1)) System.out.print(":");
				if((statistic == 1) || (statistic == 11)){
					NumberFormat f = NumberFormat.getInstance();
					f.setMaximumFractionDigits(100);
					f.setGroupingUsed(false);
					if(format == 0) System.out.println("  ENTROPY     : " + (entropy[z]));
					if(format == 1) System.out.print((entropy[z]));
					entropyFile.writeBytes(String.valueOf(f.format(entropy[z])));
					entropyFile.writeBytes("\n");
				}
                                // MHC-24/11/2011
                                if((statistic == 1) && (format == 1)) System.out.print(":");
                                if ((statistic == 1) || (statistic == 12)) {
                                        NumberFormat f = NumberFormat.getInstance();
                                        f.setMaximumFractionDigits(100);
                                        f.setGroupingUsed(false);
                                        if(format == 0) System.out.println("  MORAN'S I   : " + (morans[z]));
                                        if(format == 1) System.out.print((morans[z]));
                                }
                                // --
				if((statistic > 0) && (format == 1)) System.out.print("\n");
			}
		}

		if((zSize > 1) && (totals >= 1)){
			if((statistic > 0) && (format == 0)) System.out.println("TOTALS:");
			if((statistic == 1) || (statistic == 2)){
				if(format == 0) System.out.println("  MIN         : " + (float)totalMinMax[0]);
				if(format == 1) System.out.print((float)totalMinMax[0]);
			}
			if((statistic ==1) && (format == 1)) System.out.print(":");
			if((statistic == 1) || (statistic == 2)){
				if(format == 0) System.out.println("  MAX         : " + (float)totalMinMax[1]);
				if(format == 1) System.out.print((float)totalMinMax[1]);
			}
			if((statistic == 1) && (format == 1)) System.out.print(":");
			if((statistic == 1) || (statistic == 3)){
				if(format == 0) System.out.println("  AVERAGE     : " + (float)totalAverage);
				if(format == 1) System.out.print((float)totalAverage);
			}
			if((statistic == 1) && (format == 1)) System.out.print(":");
			if((statistic == 1) || (statistic == 4)){
				if(format == 0) System.out.println("  CENTER RANGE: " + (float)totalCenterRange);
				if(format == 1) System.out.print((float)totalCenterRange);
			}
			if((statistic == 5) && (format == 1)) System.out.print(":");
			if(statistic == 5){
				if(format == 0){
					for(int value = 0; value < values.length; value++){
						System.out.println(" - NUMBER OF PIXELS WITH VALUE = "+values[value]+" ARE "+countedValues[0][values[value]]);
					}
				}
				if(format == 1){
					for(int value = 0; value < values.length; value++){
						System.out.println(values[value]+" - "+countedValues[0][values[value]]);
					}
				}
			}
			if((statistic == 6) && (format == 1)) System.out.print(":");
			if(statistic == 6){
				if(format == 0){
					for(int value = 0; value < values.length; value++){
						System.out.println(" - % OF PIXELS VALUES = "+values[value]+" ARE "+((float)countedValues[0][values[value]] * 100  /(zSize*xSize*ySize))+"%");
					}
				}
				if(format == 1){
					for(int value = 0; value < values.length; value++){
						System.out.println(values[value]+" - "+((float)countedValues[0][values[value]] * 100  /(zSize*xSize*ySize))+"%");
					}
				}
			}
			if((statistic == 7) && (format == 1)) System.out.print(":");
			if(statistic == 7){
				if(format == 0){
					for(int value = 0; value < countedValues[0].length; value++){
							System.out.println(" - NUMBER OF PIXELS WITH VALUE = "+value+" ARE "+countedValues[0][value]);
					}
				}
				if(format == 1){
					for(int value = 0; value < countedValues[0].length; value++){
						System.out.println(value+" - "+countedValues[0][value]);
					}
				}
			}
			if((statistic == 8) && (format == 1)) System.out.print(":");
			if(statistic == 8){
				if(format == 0){
					for(int value = 0; value < countedValues[0].length; value++){
						System.out.println(" - % OF PIXELS WITH VALUE = "+value+" ARE "+((float)countedValues[0][value] * 100  /(zSize*xSize*ySize))+"%");	
					}
				}
				if(format == 1){
					for(int value = 0; value < countedValues[0].length; value++){
						System.out.println(value+" - "+((float)countedValues[0][value] * 100  /(zSize*xSize*ySize))+"%");
					}
				}
			}
			if((statistic == 1) && (format == 1)) System.out.print(":");
			if((statistic == 1) || (statistic == 9)){
				NumberFormat f = NumberFormat.getInstance();
				f.setMaximumFractionDigits(100);
				f.setGroupingUsed(false);
				if(format == 0) System.out.println("  ENERGY      : " + (totalEnergy));
				if(format == 1) System.out.print((totalEnergy));
				energyFile.writeBytes(String.valueOf(f.format(totalEnergy)));
				energyFile.writeBytes("\n");
			}
			if((statistic == 1) && (format == 1)) System.out.print(":");
			if((statistic == 1) || (statistic == 10)){
				NumberFormat f = NumberFormat.getInstance();
				f.setMaximumFractionDigits(100);
				f.setGroupingUsed(false);
				if(format == 0) System.out.println("  VARIANCE    : " + (totalVarianze));
				if(format == 1) System.out.print((totalVarianze));
				varianceFile.writeBytes(String.valueOf(f.format(totalVarianze)));
				varianceFile.writeBytes("\n");
			}
			if((statistic == 1) && (format == 1)) System.out.print(":");
			if((statistic == 1) || (statistic == 11)){
				NumberFormat f = NumberFormat.getInstance();
				f.setMaximumFractionDigits(100);
				f.setGroupingUsed(false);
				if(format == 0) System.out.println("  ENTROPY     : " + (totalEntropy));
				if(format == 1) System.out.print((totalEntropy));
				entropyFile.writeBytes(String.valueOf(f.format(totalEntropy)));
				entropyFile.writeBytes("\n");
			}
                        // MHC-24/11/2011
						if((statistic == 1) && (format == 1)) System.out.print(":");
                        if ((statistic == 1) || (statistic == 12)) {
                                NumberFormat f = NumberFormat.getInstance();
                                f.setMaximumFractionDigits(100);
                                f.setGroupingUsed(false);
                                if(format == 0) System.out.println("  MORAN'S I   : " + (totalMorans));
                                if(format == 1) System.out.print((totalMorans));
                        }
                        // --
			if((statistic > 0) && (format == 1)) System.out.print("\n");
			
			
		}
		if(statistic == 9){
			energyFile.close();
		}
		if(statistic == 10){
			varianceFile.close();
		} 
		
		//Compression factors/ratios/bpp calculations
		try{
			int rate = parser.getRate();
			float[] compressionFactors = parser.getCompressionFactors();

			if(rate > 0){
				int[] sampleBitDepths = image.getPixelBitDepth();
				for(int z = 1; z < zSize; z++){
					if(sampleBitDepths[z] != sampleBitDepths[0]){
						throw new WarningException("Ratios cannor be calculated because sample type differs in some components.");
					}
				}

				double totalImageBytes = (sampleBitDepths[0] * zSize * ySize * xSize) / 8;
				long compressedImageBytes = 0;

				if(format == 0) System.out.print("RATIOS NUM BYTES:");
				for(int cf = 0; cf < compressionFactors.length; cf++){
					switch(rate){
					case 1: //Compression factor
						compressedImageBytes = (long) (totalImageBytes / compressionFactors[cf]);
						break;
					case 2: //Compression ratio
						compressedImageBytes = (long) (totalImageBytes * compressionFactors[cf]);
						break;
					case 3: //Bits per Sample
						compressedImageBytes = (long) ((compressionFactors[cf] * zSize * ySize * xSize) / 8);
						break;
					default:
						throw new WarningException("Unrecognized rate type.");
					}
					System.out.print(" " + compressedImageBytes);
				}
				System.out.print("\n");
			}
		}catch(WarningException e){
			System.out.println("RATE CALCULATION ERROR: " + e.getMessage());
		}

	}

}
