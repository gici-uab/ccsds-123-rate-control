package ffc;

import GiciException.*;
import GiciFile.*;
import GiciTransform.DPCMSpectralDecorrelator;
import GiciTransform.ForwardWaveletTransform;
import GiciTransform.InverseWaveletTransform;
import GiciTransform.Reshape;
import GiciQuantization.*;


/**
 * This application is useful to perform simple transformations over an image.
 * 
 * NOTE: Only one transformation can be performed at once. Therefore, although
 * the blocks of code for each available transformation are in a sequential
 * order, only one of them will be run.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.2 2008/10/28
 */
public class FormatFileConverter{

	/**
	 * Main method of Gcomp application. It takes program arguments, loads images and compare them.
	 *
	 * @param args an array of strings that contains program parameters
	 */
	public static void main(String[] args)throws ErrorException{
		
		// PARSE ARGUMENTS
		ArgsParser parser = null;
		try{
			parser = new ArgsParser(args);
		}catch(Exception e){
			System.err.println("ARGUMENTS ERROR: " +  e.getMessage());
			e.printStackTrace();
			System.out.println("Please report this error (specifying image type and parameters) to: gici-dev@abra.uab.es");
			System.exit(1);
		}	
		
		String inputFile = parser.getInputImageFile();
		String outputFile = parser.getOutputImageFile();
		int[] inputImageGeometry = parser.getInputImageGeometry();
		int[] outputImageGeometry = parser.getOutputImageGeometry();
		
		
		// IMAGE LOAD
		LoadFile inputImage = null;
		try{
			if(LoadFile.isRaw(inputFile)){				
				//Check parameters of image geometry
				if((inputImageGeometry[0] <= 0) || (inputImageGeometry[1] <= 0) || (inputImageGeometry[2] <= 0)){
					throw new Exception("Image dimensions in \".raw\" or \".img\" data files must be positive (\"-h\" displays help).");
				}
				if((inputImageGeometry[3] < 0) || (inputImageGeometry[3] > 7)){
					throw new Exception("Image type in \".raw\" or \".img\" data must be between 0 to 7 (\"-h\" displays help).");
				}
				if((inputImageGeometry[4] != 0) && (inputImageGeometry[4] != 1)){
					throw new Exception("Image byte order  in \".raw\" or \".img\" data must be 0 or 1 (\"-h\" displays help).");
				}
				if((inputImageGeometry[5] != 0) && (inputImageGeometry[5] != 1)){
					throw new Exception("Image RGB specification in \".raw\" or \".img\" data must be between 0 or 1 (\"-h\" displays help).");
				}
				inputImage = new LoadFile(inputFile, inputImageGeometry[0], inputImageGeometry[1], inputImageGeometry[2], inputImageGeometry[3], 
						inputImageGeometry[4], inputImageGeometry[5] == 0 ? false: true);
			}else{
				inputImage = new LoadFile(inputFile);	
			}
		}catch(Exception e){
			System.err.println("IMAGE LOADING ERROR: " + e.getMessage());
			System.exit(2);
		}
		
		float[][][] image = inputImage.getImage();

		// Loose this reference because of the inside reference to image
		inputImage = null;
		
		// COEFFICENTS APPROXIMATION
		try{
			int zSize = image.length;
			int[] coefficientsApproximation = null;
			float[] minValue = parser.getMinValue();
			float[] maxValue = parser.getMaxValue();
			float[] lowerThresholdValue = parser.getLowerThresholdValue();
			float[] upperThresholdValue = parser.getUpperThresholdValue();
			float[] subtractValue = parser.getSubtractValue();
			float[] subtractValueForNonZeroPixels = parser.getSubtractValueForNonZeroPixels();
			float[] subtractValueIfMatch = parser.getSubtractValueIfMatch();

			if((minValue != null) || (maxValue != null) || (lowerThresholdValue != null) || (upperThresholdValue != null) || (subtractValue != null) || (subtractValueForNonZeroPixels != null) || (subtractValueIfMatch != null)){
				coefficientsApproximation = setParameterInt(parser.getCoefficientsApproximation(),zSize,0);
				if (minValue!=null){
					minValue = setParameterFloat(minValue,zSize,minValue[0]);
				}
				if (maxValue!=null){
					maxValue = setParameterFloat(maxValue,zSize,maxValue[0]);
				}
				if (lowerThresholdValue!=null){
					lowerThresholdValue = setParameterFloat(lowerThresholdValue,zSize,lowerThresholdValue[0]);
				}
				if (upperThresholdValue!=null){
					upperThresholdValue = setParameterFloat(upperThresholdValue,zSize,upperThresholdValue[0]);
				}
				if (subtractValue!=null){
					subtractValue = setParameterFloat(subtractValue,zSize,subtractValue[0]);
				}
				if (subtractValueForNonZeroPixels!=null){
					subtractValueForNonZeroPixels = setParameterFloat(subtractValueForNonZeroPixels,zSize,subtractValueForNonZeroPixels[0]);
				}
				if (subtractValueIfMatch!=null){
					subtractValueIfMatch = setParameterFloat(subtractValue,zSize,subtractValueIfMatch[0]);
				}
				
				TransformImage ca = new TransformImage(image);
				image = ca.coefficientsApproximation(coefficientsApproximation,minValue,maxValue,lowerThresholdValue,upperThresholdValue,subtractValue,subtractValueForNonZeroPixels,subtractValueIfMatch);
				ca = null;
			}
		} catch(Exception e){
			System.err.println("Parameters error: " + e.getMessage());
			System.exit(2);
		}
		

		// SPECTRAL DWT TRANSFORM
		try{
			
			int spectralWaveletLevel = parser.getSpectralWaveletLevel();
			int spectralWaveletType = parser.getSpectralWaveletType();
			if (spectralWaveletType>0){
				TransformImage fwt = new TransformImage(image);
				image = fwt.forwardSpectralDWT(spectralWaveletLevel,spectralWaveletType);
				fwt = null;
			} else if (spectralWaveletType<0){
				TransformImage iwt = new TransformImage(image);
				spectralWaveletType = -spectralWaveletType;
				image = iwt.inverseSpectralDWT(spectralWaveletLevel,spectralWaveletType);
				iwt = null;
			}
		} catch(Exception e){
			e.printStackTrace();
			System.err.println("Gici ERROR: " + e.getMessage());
			System.exit(4);
		}
		
		
		// SPATIAL DWT TRANSFORM
		try{
			
			int wl = parser.getSpatialWaveletLevel();
			int wt = parser.getSpatialWaveletType();
			// wo:
			//      0: horizontal/vertical
			//      1: as in JPEG2000 (vertical/horizontal)
			//      2: only horizontal
			int wo = parser.getSpatialOrder();
			if (wt>0){
				//Forward Wavelet Transform
				int zSize = image.length;
				int WTType[] = setParameterInt(null,zSize,wt);
				int WTLevels[] = setParameterInt(null,zSize,wl);
				int WTOrder[] = setParameterInt(null,zSize,wo);
				ForwardWaveletTransform fwt = new ForwardWaveletTransform(image);
				fwt.setParameters(WTType,WTLevels,WTOrder);
				image = fwt.run();
				fwt = null;
			} else if (wt < 0){
				int zSize = image.length;
				int WTType[] = setParameterInt(null,zSize,-wt);
				int WTLevels[] = setParameterInt(null,zSize,wl);
				int WTOrder[] = setParameterInt(null,zSize,wo);
				//Inverse Wavelet transform
				InverseWaveletTransform iwt = new InverseWaveletTransform(image);
				iwt.setParameters(WTType,WTLevels,WTOrder);
				image = iwt.run();
				iwt = null;
			}
		} catch(Exception e){
			e.printStackTrace();
			System.err.println("Gici ERROR: " + e.getMessage());
			System.exit(4);
		}
		
		
		// SPECTRAL DPCM
		try{
			int dpcmIndex = parser.getDPCMIndex();
			int dpcm = parser.getDPCM();
			if (dpcm==1){
				DPCMSpectralDecorrelator fdpcm = new DPCMSpectralDecorrelator(image);
				image = fdpcm.forwardDPCM(dpcmIndex);
				fdpcm = null;
			} else if (dpcm == -1){
				DPCMSpectralDecorrelator idpcm = new DPCMSpectralDecorrelator(image);
				image = idpcm.inverseDPCM(dpcmIndex);
				idpcm = null;
			} else if (dpcm == 2){
				DPCMSpectralDecorrelator fdpcm = new DPCMSpectralDecorrelator(image);
				image = fdpcm.forwardDifferentialReference(dpcmIndex);
				fdpcm = null;
			} else if (dpcm == -2){
				DPCMSpectralDecorrelator idpcm = new DPCMSpectralDecorrelator(image);
				image = idpcm.inverseDifferentialReference(dpcmIndex);
				idpcm = null;
			}
		} catch(Exception e){
			e.printStackTrace();
			System.err.println("Gici ERROR: " + e.getMessage());
			System.exit(4);
		}
		

		// SPATIAL DPCM
		try{
			int spatialDPCM = parser.getSpatialDPCM();

			TransformImage spatialDPCMimage = new TransformImage(image);

			if (spatialDPCM == 1){
			    image = spatialDPCMimage.SpatialForwardLeftDPCM();
			    spatialDPCMimage = null;
			} else if (spatialDPCM == -1){
			    image = spatialDPCMimage.SpatialReverseLeftDPCM();
			    spatialDPCMimage = null;
			} else if (spatialDPCM == 2){
			    image = spatialDPCMimage.SpatialForwardRightDPCM();
			    spatialDPCMimage = null;
			} else if (spatialDPCM == -2){
			    image = spatialDPCMimage.SpatialReverseRightDPCM();
			    spatialDPCMimage = null;
			}

		}catch(Exception e){
			e.printStackTrace();
			System.err.println("Gici ERROR: " + e.getMessage());
			System.exit(4);
		}
		
		// CROP IMAGE
		try{
			int z0 = parser.getZInit();
			int zl = parser.getZLength();
			int[][] zr = parser.getZRanges();
			int y0 = parser.getYInit();
			int yl = parser.getYLength();
			int x0 = parser.getXInit();
			int xl = parser.getXLength();
			if (xl!=0 || yl!=0 || zl!=0 || zr != null){
				TransformImage crop = new TransformImage(image);
				if (zr == null) {
					image = crop.cropImage(z0,y0,x0,zl,yl,xl);
				} else {
					image = crop.cropImage(zr,y0,x0,yl,xl);
				}
				crop = null;
			}
		}catch(Exception e){
			e.printStackTrace();
			System.err.println("Gici ERROR: " + e.getMessage());
			System.exit(4);
		}
		
		// ROTATE IMAGE
		try{
			int toRotate = parser.getRotate();
			if(toRotate != 0){
				TransformImage rotate = new TransformImage(image);
				image = rotate.rotateImage(toRotate);
				rotate = null;
			}
			
		}catch(Exception e){
			e.printStackTrace();
			System.err.println("Gici ERROR: " + e.getMessage());
			System.exit(4);
		}

		// REMOVE NON NULL COEFFICIENTS OF THE IMAGE
		
		try{

			int zSizeOut = outputImageGeometry[0];
			int ySizeOut = outputImageGeometry[1];
			int xSizeOut = outputImageGeometry[2];

			int toRemoveNullCoefficients = parser.getRemoveNullCoefficients();
			if(toRemoveNullCoefficients != 0){
				TransformImage noZeroPixel = new TransformImage(image);
				image = noZeroPixel.nonNullCoefficients(zSizeOut,ySizeOut,xSizeOut,toRemoveNullCoefficients);
				noZeroPixel = null;
			}
			
		}catch(Exception e){
			e.printStackTrace();
			System.err.println("Gici ERROR: " + e.getMessage());
			System.exit(4);
		}
		

		// CHANGE IMAGES DIMENSIONS
				
		try{
			int[] changeDim = parser.getChangeDim();
			
			if (changeDim != null) {
				if (changeDim.length != 3) {
					throw new ParameterException("Invalid dimension permutation.");
				}
				
				Reshape r = new Reshape(image);
				image = null;
				r.dimensionTranspose(changeDim);
				image = r.getImageFloat();
				
				
			}
		}catch (Exception e) {
			e.printStackTrace();
			System.err.println("Gici ERROR: " + e.getMessage());
			System.exit(4);
		}
		
		//RANGEMODIFICATION
		try{
			float[] rangeModification = null;
			float[] nodataValues = null;
			rangeModification = parser.getRangeModification();
			nodataValues = parser.getNodataValues();
			if(rangeModification != null){
				
					//find max and min value each component must be managed independently
					float max = Float.MIN_VALUE;
					float min = Float.MAX_VALUE;
					for(int z = 0; z < image.length; z++){
						for(int y = 0; y < image[z].length; y++){
						for(int x = 0; x < image[z][y].length; x++){
							if(nodataValues != null){
								for(int nd = 0; nd < nodataValues.length; nd++){
									if(image[z][y][x] != nodataValues[nd]){
										if(image[z][y][x] > max) max = image[z][y][x];
										if(image[z][y][x] < min) min = image[z][y][x];
									}
								}
							}else{
								if(image[z][y][x] > max) max = image[z][y][x];
								if(image[z][y][x] < min) min = image[z][y][x];
							}
						}}
						float tmp = (max - min) * rangeModification[0] / 100;
						min = min - tmp;
						max = max - tmp;
						for(int y = 0; y < image[z].length; y++){
						for(int x = 0; x < image[z][y].length; x++){
							if(nodataValues != null){
								for(int nd = 0; nd < nodataValues.length; nd++){
									if(image[z][y][x] != nodataValues[nd]){
											image[z][y][x] = (image[z][y][x] * Math.abs(min) * 255) / (max-min);
									}else{
										image[z][y][x] = 0;
									}
								}
							}else{
								image[z][y][x] = ((image[z][y][x] + Math.abs(min)) * 255) / (max-min);
							}
						}}
						max = Float.MIN_VALUE;
						min = Float.MAX_VALUE;	
					}
			}
			
		}catch (Exception e){
			e.printStackTrace();
			System.err.println("Gici ERROR: " + e.getMessage());
			System.exit(4);
		}
		// PERMUTE COMPONENTS
		try{
			int[] permuteType = null;
			permuteType = parser.getPermuteType();
			if(permuteType != null){
				TransformImage permute = new TransformImage(image);
				image = permute.permuteComponents(permuteType[0],permuteType[1]);
				permute = null;
			}
			
		}catch(Exception e){
			e.printStackTrace();
			System.err.println("Gici ERROR: " + e.getMessage());
			System.exit(4);
		}
		
		//QUANTIZATION and DEQUANTIZATION
		try{
			int[] quantization = null;
			quantization = parser.getQuantization();
			if(quantization != null){
				int direction = quantization[0];
				int quantizer = quantization[1];
				int qstep = quantization[2];
				if(direction == 1){
					switch(quantizer){
					case 0:
						SDQ SDQquantizer = new SDQ();
						SDQquantizer.setQuantizerStep(qstep);
						for(int z = 0; z < image.length; z++){
						for(int y = 0; y < image[z].length; y++){
						for(int x = 0; x < image[z][y].length; x++){
							image[z][y][x] = SDQquantizer.quantize((long)image[z][y][x]);
						}}}
						break;
					case 1:
						UQ UQquantizer = new UQ();
						UQquantizer.setQuantizerStep(qstep);
						for(int z = 0; z < image.length; z++){
						for(int y = 0; y < image[z].length; y++){
						for(int x = 0; x < image[z][y].length; x++){
							image[z][y][x] = UQquantizer.quantize((long)image[z][y][x]);
						}}}
						break;
					}
				}
				if(direction == -1){
					switch(quantizer){
					case 0:
						SDQ SDQquantizer = new SDQ();
						SDQquantizer.setQuantizerStep(qstep);
						for(int z = 0; z < image.length; z++){
						for(int y = 0; y < image[z].length; y++){
						for(int x = 0; x < image[z][y].length; x++){
							image[z][y][x] = SDQquantizer.dequantize((long)image[z][y][x]);
						}}}
						break;
					case 1:
						UQ UQquantizer = new UQ();
						UQquantizer.setQuantizerStep(qstep);
						for(int z = 0; z < image.length; z++){
						for(int y = 0; y < image[z].length; y++){
						for(int x = 0; x < image[z][y].length; x++){
							image[z][y][x] = UQquantizer.dequantize((long)image[z][y][x]);
						}}}
						break;
					}
				}
				
				
			}
			
		}catch(Exception e){
			e.printStackTrace();
			System.err.println("Gici ERROR: " + e.getMessage());
			System.exit(4);
		}
		
		// SAVE FILE
		try{
			int format = SaveFile.getFormat(outputFile);
			switch(format){
			case 0:
				SaveFile.SaveFileFormat(image, outputFile, format);
				break;
			case 5:
				SaveFile.SaveFileByExtension(image,outputFile,outputImageGeometry);
				break;
			}
			
		} catch(Exception e){
			e.printStackTrace();
			System.err.println("Gici SaveFile ERROR: " + e.getMessage());
			System.exit(4);
		}

	}

	/**
	 * Set the parameter.
	 *
	 * @param  inputParameter is an array of integers given by the user
	 * @param  size integer that indicates the number of values to be set
	 * @param  defaultValue integer that contains default value
	 *
	 * @return an integer array containing the required amount values to perform the coding process
	 */
	public static int[] setParameterInt(int[] inputParameter, int size, int defaultValue) throws ParameterException{
		int [] parameter;
		if( inputParameter != null){
			if ( inputParameter.length <= size ){
				parameter = new int[size];
				for(int k=0; k<inputParameter.length ; k++){//copy of common indices
					//if the number user parameters is higher than the number 
					//of parameters, last vaules given are discarded
					parameter[k] = inputParameter[k];
				}
				for(int k=inputParameter.length ; k<size ; k++){
					//in case there were not enough values in the parameters given by the  
					//user the last value is repeated as many times as necessary
					parameter[k] = inputParameter[inputParameter.length - 1 ];
				}
			} else{// the amount of parameters given by the user is greater than neeed
				// an exception is thrown
				throw new ParameterException("Too much values have been given for some of the values");
			}
		} else{//default parameters are set
			parameter = new int[size];
			for(int k=0; k<size ; k++){
				parameter[k] = defaultValue;
			}
		}
		
		return parameter;
		
	}

	/**
	 * Set the parameter containing float values.
	 *
	 * @param  inputParameter is an array of floats given by the user
	 * @param  size integer that indicates the number of values to be set
	 * @param  defaultValue integer that contains default value
	 *
	 * @return a float array containing the required amount values to perform the coding process
	 */
	public static float[] setParameterFloat(float[] inputParameter, int size, float defaultValue) throws ParameterException{
		float [] parameter;
		if( inputParameter != null){
			if ( inputParameter.length <= size ){
				parameter = new float[size];
				for(int k=0; k<inputParameter.length ; k++){//copy of common indices
					//if the number user parameters is higher than the number 
					//of parameters, last vaules given are discarded
					parameter[k] = inputParameter[k];
				}
				for(int k=inputParameter.length ; k<size ; k++){
					//in case there were not enough values in the parameters given by the  
					//user the last value is repeated as many times as necessary
					parameter[k] = inputParameter[inputParameter.length - 1 ];
				}
			} else{// the amount of parameters given by the user is greater than neeed
				// an exception is thrown
				throw new ParameterException("Too much values have been given for some of the values");
			}
		} else{//default parameters are set
			parameter = new float[size];
			for(int k=0; k<size ; k++){
				parameter[k] = defaultValue;
			}
		}
		
		return parameter;
		
	}
}
