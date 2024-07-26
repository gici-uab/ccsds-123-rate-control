package GiciPredictor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import GiciMath.IntegerMath;
import emporda.CONS;
import emporda.Parameters;
import emporda.Quantizer;

public class SamplePrediction {
	
	int [][] samples = null;
	int [][] samplesPrevious1 = null;
	int [][] samplesClass = null;
	int [] sample = new int[3];
	private final int MIN = 0;
	private final int MAX = 1;
	private final int MID = 2;
	private int geo[];
	
	
	
	
	public SamplePrediction(Parameters parameters){
		geo = parameters.getImageGeometry();
		
		if (geo[CONS.TYPE] != 3) {
			sample[MIN] = 0;
			sample[MAX] = (1 << parameters.dynamicRange) - 1;
			sample[MID] = 1 << (parameters.dynamicRange - 1);
		
		} else { 
			sample[MIN] = -1 << parameters.dynamicRange - 1;
			sample[MAX] = (1 << parameters.dynamicRange - 1) - 1;			
			sample[MID] = 0;
		}
		sample[MAX] = (1 << 17 - 1) - 1;	
		sample[MIN] = -1 << 17 - 1;	
		
	}
	
	public void setSamples(int [][] samples) {
		this.samples = samples;
		samplesClass = new int[samples.length][samples[0].length];
	}
	
	public int getResidual(int z, int y, int x, int predict){
		int residual = samples[y][x] - predict;
		//residual = getMappedResidual(predict, 0, residual);
		return(residual);
	}
	
	public int getSampleRecovered(int residual, int predict, int z, int y, int x){
		samples[y][x] = residual + predict;
		return(samples[y][x]);
	}
	
	public int predict(int z, int y){
		int prediction = 0;
		
		
		/*if(z == 0) {
			if(y > 0){
				prediction = samples[y-1][x];
			}
		}*/
		
		
	
		
		return(prediction);
	}
	
	/**
	 * Return the mapped residual of the sample s[z][y][x].
	 * 
	 * @param s is the sample value
	 * @param s_aprox is the predicted sample value
	 * @param s_scaled is the scaled predicted sample value
	 * @return the mapped residual of the sample s[z][y][x]
	 */
	public int getMappedResidual(int s_aprox, int s_scaled, int q_residual){
		int minDifference;
		int mappedValue = 0;
		
		minDifference = Math.min(s_aprox - sample[MIN], sample[MAX] - s_aprox);					
		
		if (Math.abs(q_residual) > minDifference) {
			mappedValue = Math.abs(q_residual) + minDifference;
		} else if (s_scaled % 2 == 0 && q_residual >= 0 || s_scaled % 2 != 0 && q_residual <= 0) {
			mappedValue = Math.abs(q_residual) << 1;
		} else {
			mappedValue = (Math.abs(q_residual) << 1) - 1;
		}
		return mappedValue;	
	}
	
	/**
	 * Return the original value of the sample.
	 * 
	 * @param mappedResidual is the mapped residual of a sample
	 * @param s_aprox is the predicted sample value
	 * @param s_scaled is the scaled predicted sample value
	 * @return the original value of the sample
	 */
	public int getUnmappedSample(int mappedResidual, int s_aprox, int s_scaled) {
		int minDifference, residual, q_residual;
		
		minDifference = Math.min(s_aprox - sample[MIN], sample[MAX] - s_aprox);		
		if (mappedResidual > minDifference << 1) {
			if (minDifference == s_aprox - sample[MIN]) {
				q_residual = mappedResidual - minDifference;
			} else {
				q_residual = -mappedResidual + minDifference;
			}
		} else if (mappedResidual % 2 == 0) {
			q_residual = s_scaled % 2 == 0 ? mappedResidual >> 1 : -mappedResidual >> 1;
		} else {
			q_residual = s_scaled % 2 != 0 ? mappedResidual + 1 >> 1 : -mappedResidual - 1 >> 1;
		}
		
		//residual= uq.dequantize(q_residual);
		residual= q_residual;
	
		return IntegerMath.clip(residual+s_aprox, sample[MIN], sample[MAX]);
	}

	
	
	
	
	public void keepPreviousBand() {
		this.samplesPrevious1 = this.samples;
	}

	public void run() {
		int range = sample[MAX] - sample[MIN];
		int numberOfClasses = 16;
		int rangePerclasse = range / numberOfClasses;
		
		for(int y = 0; y < samples.length; y++) {
		for(int x = 0; x < samples[y].length; x++) {
		for(int c = 0; c < numberOfClasses; c++) {
			if(samples[y][x] > c * rangePerclasse) {
				samplesClass[y][x] = c;
				//System.out.println(y+" "+x+" "+samples[y][x]+" "+samplesClass[y][x]);
			}
		}}}
	}

	public int[][] getSamplesClasses() {
		
		return samplesClass;
	}
	
}
