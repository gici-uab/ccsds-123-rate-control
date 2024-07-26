package GiciAnalysis;

import java.util.Arrays;

public class OrthogonalityTest {
	
	private static float stableAccumulation(double[] a) {
		double[] r = Arrays.copyOfRange(a, 0, a.length);
		
		if (r.length == 0)
			return Float.NaN;
		
		int l = r.length;
		
		while (l > 1) {	
			for (int i = 0; i < l / 2; i++) {
				r[i] = r[i * 2] + r[i * 2 + 1];
			}
			
			if (l % 2 != 0) {
				r[l / 2] = r[l-1];
			}
			
			l = l / 2 + l % 2;
		}
		
		return (float) r[0];
	}
	
	private static float columnDotProduct(float[][] a, int i, int j) {
		double[] p = new double[a.length];
		
		for(int k = 0; k < a.length; k++) {
			p[k] = a[k][i] * a[k][j];
		}
		
		return (float) stableAccumulation(p);
	}
	
	private static float rowDotProduct(float[][] a, int i, int j) {
		assert(a[i].length == a[j].length);
		
		double[] p = new double[a[i].length];
		
		for(int k = 0; k < a[i].length; k++) {
			p[k] = a[i][k] * a[j][k];
		}
		
		return (float) stableAccumulation(p);
	}

	
	/* returns
	 * 0 - min dot product for row vectors i!=j
	 * 1 - max dot product for row vectors i!=j
 	 * 2 - min dot product for row vectors i==j
	 * 3 - max dot product for row vectors i==j
	 * 4 - min dot product for col vectors i!=j
	 * 5 - max dot product for col vectors i!=j
	 * 6 - min dot product for col vectors i==j
	 * 7 - max dot product for col vectors i==j
	 */
	static public float[] test(float[][] a) {
		float minDotRowD = Float.POSITIVE_INFINITY;
		float maxDotRowD = Float.NEGATIVE_INFINITY;
		float minDotRowE = Float.POSITIVE_INFINITY;
		float maxDotRowE = Float.NEGATIVE_INFINITY;
		float minDotColD = Float.POSITIVE_INFINITY;
		float maxDotColD = Float.NEGATIVE_INFINITY;
		float minDotColE = Float.POSITIVE_INFINITY;
		float maxDotColE = Float.NEGATIVE_INFINITY;
		
		for (int i = 0; i < a.length; i++) {
			for (int j = i; j < a.length; j++) {				
				
				float colResult = columnDotProduct(a, i, j);
				float rowResult = rowDotProduct(a, i, j);
				
				if (i == j) {
					minDotRowE = Math.min(minDotRowE, rowResult);
					maxDotRowE = Math.max(maxDotRowE, rowResult);
					minDotColE = Math.min(minDotColE, colResult);
					maxDotColE = Math.max(maxDotColE, colResult);
				} else {
					minDotRowD = Math.min(minDotRowD, rowResult);
					maxDotRowD = Math.max(maxDotRowD, rowResult);
					minDotColD = Math.min(minDotColD, colResult);
					maxDotColD = Math.max(maxDotColD, colResult);
				}
			}
		}
		
		float[] r = {minDotRowD, maxDotRowD, minDotRowE, maxDotRowE, minDotColD, maxDotColD, minDotColE, maxDotColE};
		return r;
	}
}
