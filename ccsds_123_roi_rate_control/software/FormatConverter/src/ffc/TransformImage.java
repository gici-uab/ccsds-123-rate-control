package ffc;

import java.util.Arrays;
import GiciException.*;
import GiciTransform.*;



/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */

public class TransformImage{
	/**
	 * Image samples (index meaning [z][y][x]).
	 */
	float[][][] imageSamples = null;
	
	/**
	 * Constructor, receives the transformed image samples.
	 *
	 * @param imageSamples definition in {@link #imageSamples}
	 */
	public TransformImage(float[][][] imageSamples){
		//Image data copy
		this.imageSamples = imageSamples;
	}
	
	public float[][][] forwardSpectralDWT(int spectralWTLevels, int spectralWTType) throws ErrorException {
		
		//1D+2D non pyramidal Wavelet Transform
		int[] levels = new int[3];
		int[] types = new int[3];
		for(int k=0;k<2;k++){// no spatial wavelet transform
			levels[k] = 0;
			types[k] = 0;
		}
		levels[2] = spectralWTLevels;
		types[2] = spectralWTType;
		ForwardNonPyramidal3D fwt = new ForwardNonPyramidal3D(imageSamples);
		fwt.setParameters(types,levels);
		imageSamples = fwt.run();
		fwt = null;
		
		return imageSamples;
	}
	
	public float[][][] inverseSpectralDWT(int spectralWTLevels, int spectralWTType) throws ErrorException {
		
		//1D+2D non pyramidal Wavelet Transform
		int[] levels = new int[3];
		int[] types = new int[3];
		for(int k=0;k<2;k++){// no spatial wavelet transform
			levels[k] =0;
			types[k] = 0;
		}
		levels[2] = spectralWTLevels;
		types[2] = spectralWTType;
		InverseNonPyramidal3D iwt = new InverseNonPyramidal3D(imageSamples);
		iwt.setParameters(types,levels);
		imageSamples = iwt.run();
		iwt = null;		
		return imageSamples;
	}
	
	public float[][][] coefficientsApproximation(int[] coefficientsApproximation,
						     float[] minValue, float[] maxValue, float[] lowerThresholdValue, float[] upperThresholdValue, float[] subtractValue, float[] subtractValueForNonZeroPixels, float[] subtractValueIfMatch)  throws ErrorException {
		
		int zSize = imageSamples.length;
		// int comptador = 0;

		for(int z=0; z<zSize ; z++){
			int ySize = imageSamples[z].length;
			int xSize = imageSamples[z][0].length;
			
			for(int y=0; y<ySize ; y++){
				for(int x=0; x<xSize ; x++){
					
					if (maxValue!=null){
						if (imageSamples[z][y][x] > maxValue[z]){
							imageSamples[z][y][x] = maxValue[z];
						}
					}
					
					if (minValue!=null){
						if (imageSamples[z][y][x] < minValue[z] ){
							imageSamples[z][y][x] = minValue[z];
						}
					}
					

					if (lowerThresholdValue!=null){
					    if (Math.abs(imageSamples[z][y][x]) < lowerThresholdValue[z] ){
							imageSamples[z][y][x] = 0;
						}
					}

					if (upperThresholdValue!=null){
					    if (Math.abs(imageSamples[z][y][x]) >= upperThresholdValue[z] ){
							imageSamples[z][y][x] = 0;
						}
					}

					if (subtractValue!=null){
							// System.out.println("comptador: " + comptador);
							// System.out.println("imageSamples[z][y][x]" + imageSamples[z][y][x] + " subtractValue[z]: " + subtractValue[z]);
							imageSamples[z][y][x] -= (float) subtractValue[z];
							imageSamples[z][y][x] = (float) imageSamples[z][y][x];
							// System.out.println("updated imageSamples[z][y][x]" + imageSamples[z][y][x]);
							// comptador += 1;
					}
					
					if (subtractValueForNonZeroPixels!=null){
					    if (imageSamples[z][y][x] != 0){
						imageSamples[z][y][x] -= subtractValueForNonZeroPixels[z];
					    }
					}
					
					if (subtractValueIfMatch!=null){
					    if (imageSamples[z][y][x] == subtractValueIfMatch[z]){
						imageSamples[z][y][x] = 0;
					    }
					}

					if (coefficientsApproximation[z]==1){
						imageSamples[z][y][x]= (float) Math.rint(imageSamples[z][y][x]);
					} else if (coefficientsApproximation[z]==2){
						imageSamples[z][y][x]= (float) Math.floor(imageSamples[z][y][x]);
					} else if (coefficientsApproximation[z]==3){
						imageSamples[z][y][x]= (float) Math.ceil(imageSamples[z][y][x]);
					}
				}
			}
		}
		return imageSamples;
	}
	
	public float[][][] selectBands(int firstBand, int numBands){
		
		int zSize = numBands ;
		
		float newImage[][][] = new float[zSize][][];
		for(int z=0;z<zSize;z++){
			int ySize = imageSamples[z+firstBand].length;
			newImage[z] = new float[ySize][];
			for(int y=0;y<ySize;y++){
				int xSize = imageSamples[z+firstBand][y].length;
				newImage[z][y] = new float[xSize];
				for(int x=0;x<xSize;x++){
					newImage[z][y][x] = imageSamples[z+firstBand][y][x];
				}
			}
		}
		imageSamples = null;
		return newImage;
	}

    public float[][][] nonNullCoefficients(int outZsize, int outYsize, int outXsize, int NumberNonNullPixels){

		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;

		int index=0;
		
		int outZ=0;
		int outY=0;
		int outX=0;

		float newImage[][][] = new float[outZsize][outYsize][outXsize];

		for(int z=0;z<outZsize;z++){
			for(int y=0;y<outYsize;y++){
				for(int x=0;x<outXsize;x++){
					newImage[outZ][outY][outX] = 0;
				}
			}
		}

		for(int z=0;z<zSize;z++){
			for(int y=0;y<ySize;y++){
				for(int x=0;x<xSize;x++){
				    if (Math.abs(imageSamples[z][y][x]) > 0 ){
					newImage[outZ][outY][outX] = imageSamples[z][y][x];
					index++;
					outX++;
				    }
				    if ( outX == outXsize ) {
					outY++;
					outX=0;
				    }
				    if ( outY == outYsize ) {
					outZ++;
					outY=0;
				    }
				}
			}
		}

		if (NumberNonNullPixels != index){
		    System.err.println("Gici ERROR: Transform Image. NotZero: " + index);
		}

		return newImage;
	}

    public float[][][] SpatialForwardLeftDPCM(){
	
	int zSize = imageSamples.length;
	int ySize = imageSamples[0].length;
	int xSize = imageSamples[0][0].length;
	
	float newImage[][][] = new float[zSize][ySize][xSize];
	
	for(int z=0;z<zSize;z++){
	    for(int y=0;y<ySize;y++){
		newImage[z][y][0] = imageSamples[z][y][0];
		for(int x=1;x<xSize;x++){
		    newImage[z][y][x] = imageSamples[z][y][x] - imageSamples[z][y][x-1];
		}
	    }
	}
	
	return newImage;
    }
    
    public float[][][] SpatialReverseLeftDPCM(){

	int zSize = imageSamples.length;
	int ySize = imageSamples[0].length;
	int xSize = imageSamples[0][0].length;

	float newImage[][][] = new float[zSize][ySize][xSize];

	for(int z=0;z<zSize;z++){
	    for(int y=0;y<ySize;y++){
		newImage[z][y][0] = imageSamples[z][y][0];
		for(int x=1;x<xSize;x++){
		    newImage[z][y][x] = imageSamples[z][y][x] + newImage[z][y][x-1];
		}
	    }
	}

	return newImage;
    }

    public float[][][] SpatialForwardRightDPCM(){
	
	int zSize = imageSamples.length;
	int ySize = imageSamples[0].length;
	int xSize = imageSamples[0][0].length;
	
	float newImage[][][] = new float[zSize][ySize][xSize];
	
	for(int z=0;z<zSize;z++){
	    for(int y=0;y<ySize;y++){
		newImage[z][y][xSize-1] = imageSamples[z][y][xSize-1];
		for(int x=(xSize-2);x>=0;x--){
		    newImage[z][y][x] = imageSamples[z][y][x] - imageSamples[z][y][x+1];
		}
	    }
	}
	
	return newImage;
    }
	
    public float[][][] SpatialReverseRightDPCM(){
	
	int zSize = imageSamples.length;
	int ySize = imageSamples[0].length;
	int xSize = imageSamples[0][0].length;
	
	float newImage[][][] = new float[zSize][ySize][xSize];
	
	for(int z=0;z<zSize;z++){
	    for(int y=0;y<ySize;y++){
		newImage[z][y][xSize-1] = imageSamples[z][y][xSize-1];
		for(int x=(xSize-2);x>=0;x--){
		    newImage[z][y][x] = imageSamples[z][y][x] + newImage[z][y][x+1];
		}
	    }
	}
	
	return newImage;
    }

	public float[][][] cropImage(int z0, int y0, int x0, int zLength, int yLength, int xLength){
		
		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;
		
		if (zLength == 0){
			zLength = zSize;
		}
		if (yLength == 0){
			yLength = ySize;
		}
		if (xLength == 0){
			xLength = xSize;
		}
		float newImage[][][] = new float[zLength][yLength][xLength];
		for(int z=0;z<zLength;z++){
			for(int y=0;y<yLength;y++){
				for(int x=0;x<xLength;x++){
					if (z+z0 < 0 || y+y0 < 0 || x+x0 < 0
							|| z+z0 >= zSize || y+y0 >= ySize || x+x0 >= xSize) {
						continue;
					}
					
					newImage[z][y][x] = imageSamples[z+z0][y+y0][x+x0];
				}
			}
		}
		
		return newImage;
	}
	
		public float[][][] cropImage(int[][] zr, int y0, int x0, int yLength, int xLength) {
		
		// Converts zRanges to an array of components
		int numComps = 0;
		for (int i = 0; i < zr.length; i++) numComps += zr[i][1] - zr[i][0] + 1;
		int[] comps = new int[numComps];
		int compIndex = 0;
		for (int i = 0; i < zr.length; i++) {
			for (int c = zr[i][0]; c <= zr[i][1]; c++) {
				comps[compIndex++] = c;
			}
		}
		
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;
		
		if (yLength == 0){
			yLength = ySize;
		}
		if (xLength == 0){
			xLength = xSize;
		}
		
		float newImage[][][] = new float[numComps][yLength][xLength];
		int z = 0;
		for(int comp : comps){
			for(int y=0;y<yLength;y++){
				for(int x=0;x<xLength;x++){
					if (y+y0 < 0 || x+x0 < 0 || y+y0 >= ySize || x+x0 >= xSize) {
						continue;
					}
					
					newImage[z][y][x] = imageSamples[comp][y+y0][x+x0];
				}
			}
			z++;
		}
		
		return newImage;
	}
	
	public float[][][] rotateImage(int rotate){
		
		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;

		float newImage[][][] = new float[zSize][ySize][xSize];
		
		for(int z=0;z<zSize;z++){
			for(int y=0;y<ySize;y++){
					newImage[z][(ySize-1)-y] = imageSamples[z][y];
			}
		}
		
		return newImage;
	}

	public float[][][] permuteComponents(int permutation,int zSize){
		float[][][] newImage = new float[imageSamples.length][imageSamples[0].length][imageSamples[0][0].length];
		int nComponents=imageSamples.length;
		System.out.println("ncomponents: "+nComponents);
		int value=0;
		switch (permutation) {
		case 0:
			for(int i=0;i<nComponents;i++){
				for(int j=0;j<nComponents/zSize;j++){
					newImage[i+j]=imageSamples[j*zSize+value];
				}
				i=i+nComponents/zSize-1;
				value++;
			}
			break;
		default:
			break;
		}
		return newImage;
	}
	
	public float[][][] permuteImage(int dimension, int [] permutation){
		
		assert (dimension >= 0 && dimension <= 2);
		
		int zSize = imageSamples.length;
		int ySize = imageSamples[0].length;
		int xSize = imageSamples[0][0].length;
		
		float newImage[][][] = new float[zSize][ySize][xSize];
		
		/* Check permutation for correctness */
		int[] permTest = new int[permutation.length];
		System.arraycopy(permutation, 0, permTest, 0, permutation.length);
		Arrays.sort(permTest);
		for (int i = 0; i < permTest.length; i++) {
			assert(permTest[i] == i);
		}
		
		switch(dimension) {
		case 0:
			assert(permutation.length == zSize);
			
			for (int z = 0; z < zSize; z++) {
				newImage[z] = imageSamples[permutation[z]];
			}
			
			break;
		case 1:
			assert(permutation.length == ySize);
			
			for (int z = 0; z < zSize; z++) {
				for (int y = 0; y < ySize; y++) {
					newImage[z][y] = imageSamples[z][permutation[y]];
				}
			}
			
			break;
		case 2:
			assert(permutation.length == xSize);
			
			for (int z = 0; z < zSize; z++) {
				for (int y = 0; y < ySize; y++) {
					for (int x = 0; y < ySize; y++) {
						newImage[z][y][x] = imageSamples[z][y][permutation[x]];
					}
				}
			}
			
			break;
		}
		
		return newImage;
	}
}