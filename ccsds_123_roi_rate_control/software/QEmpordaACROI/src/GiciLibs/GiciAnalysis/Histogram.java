package GiciAnalysis;

import java.util.Arrays;
import java.util.concurrent.Callable;

public class Histogram implements Callable<Histogram> {
	final int counts[];
	
	final float min;
	final float max;
	final float step;

	final float[] values;
	
	boolean called = false;
	
	final float[] bins;
	int outOfRangeCount = 0;

	private float[] intitializeBins() {
		float[] bins = new float[counts.length + 1];

		bins[0] = min;
		
		for (int i = 1; i < bins.length; i++)
			bins[i] = bins[i - 1] + step;
		
		return bins;
	}
	
	public Histogram(float[] values, float min, float max, float step) {
		this.min = min;
		this.max = max;
		this.values = values;
		this.step = step;
		this.counts = new int[(int)Math.ceil((max - min) / step)];
		
		this.bins = intitializeBins();
	}
	
	public Histogram(float[] values, float min, float max, int count) {
		this.min = min;
		this.max = max;
		this.values = values;
		this.step = (max - min) / (float)count;
		this.counts = new int[count];
		
		this.bins = intitializeBins();
	}
	
	private static float min (float[] v) {
		float minVal = Float.POSITIVE_INFINITY;
		
		for (float a: v)
			if (a < minVal)
				minVal = a;
	
		return minVal;
	}
	
	private static float max (float[] v) {
		float maxVal = Float.NEGATIVE_INFINITY;	
		
		for (float a: v)
			if (a > maxVal)
				maxVal = a;
		
		return maxVal;
	}
	
	public Histogram(float[] values, int count) {
		this(values, min(values), max(values), count);
	}

	public Histogram call() {
		for (float v: values) {
			int i = Arrays.binarySearch(bins, v);
			
			if (i < 0) { 
				i = Math.abs(i + 1);
				
				if (i == 0 || i == bins.length) {
					outOfRangeCount++;
					continue;
				}
			}
			
			if (i == counts.length)
				i--;

			counts[i] ++;
		}
		
		called = true;
		return this;
	}
	
	public float[] getMidPoints() {
		assert (called);
		
		float[] r = new float[counts.length];
		
		if (r.length > 0) {
			r[0] = min + step / 2;
		}
		
		for (int i = 1; i < r.length; i++)
			r[i] = r[i - 1] + step;
		
		return r;
	}
	
	public int[] getCounts() {
		assert (called);
		return counts;
	}

	public float getMax() {
		assert (called);
		return max;
	}

	public float getMin() {
		assert (called);
		return min;
	}

	public float getStep() {
		assert (called);
		return step;
	}

	public int getOutOfRangeCount() {
		assert (called);
		return outOfRangeCount;
	}

	public float[] getBins() {
		assert (called);
		return bins;
	}
	
	static private int clipRange(final int index, final int lenght) {
		return (index < 0 ? 0 : (index >= lenght ? lenght - 1: index));
	}
	
	/**
	 * 
	 * @param input
	 * @return [min, q1, median, q3, max]
	 */
	static public double[] getQuartiles(double[] input) {
		assert (input.length > 0);
		
		double[] array = Arrays.copyOf(input, input.length);
		
		Arrays.sort(array);
		
	    float q1 = (array.length + 1.0f) * (25.0f / 100.0f) - 1;
	    float q2 = (array.length + 1.0f) * (50.0f / 100.0f) - 1;
	    float q3 = (array.length + 1.0f) * (75.0f / 100.0f) - 1;
		
	    int q1l = (int) Math.floor(q1);
	    int q1u = (int) Math.ceil(q1);
	    
	    int q2l = (int) Math.floor(q2);
	    int q2u = (int) Math.ceil(q2);
	    
	    int q3l = (int) Math.floor(q3);
	    int q3u = (int) Math.ceil(q3);
	    
	    double min = array[0];
	    double max = array[array.length - 1];
	    
	    q1l = clipRange(q1l, array.length);
	    q1u = clipRange(q1u, array.length);
	    q2l = clipRange(q2l, array.length);
	    q2u = clipRange(q2u, array.length);
	    q3l = clipRange(q3l, array.length);
	    q3u = clipRange(q3u, array.length);
	    
	    double q1v = array[q1l] * (1 - q1 + q1l) + array[q1u] * (q1 - q1l);
	    double q2v = array[q2l] * (1 - q2 + q2l) + array[q2u] * (q2 - q2l);
	    double q3v = array[q3l] * (1 - q3 + q3l) + array[q3u] * (q3 - q3l);
	    
	    double[] result = {min, q1v, q2v, q3v, max};
	    
	    return result;
	}
	
	static public double[] getQuartiles(int[] input) {
			double[] array = new double[input.length];
			
			for (int i = 0; i < input.length; i++) {
				array[i] = input[i];
			}
			
			return getQuartiles(array);
	}
}
