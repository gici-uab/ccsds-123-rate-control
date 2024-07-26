package GiciAnalysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * From: Graphs, Networks and Algorithms 3rd ed - D. Jungnickel (Springer, 2008)
 * 
 * @author Ian
 */
public class HungarianAlgorithm {
	
	public interface WeightFunction {
		float weight(int v, int u);
	}
	
	public static WeightFunction getPrecomputedWeightFunction(final int count, final WeightFunction w) {		
		final float[][] matrix = new float[count][count];

		for (int i = 0; i < count; i++) {
			for (int j = 0; j < count; j++) {
				matrix[i][j] = w.weight(i, j);
			}
		}

		return new WeightFunction() {
			public float weight(int v, int u) {
				return matrix[v][u];
			}
		};
	}
	
	/*
	public static WeightFunction getPrecomputedWeightFunctionWithThreshold(final int count, final WeightFunction w, final float threshold) {		
		final BitSet mask = new BitSet(count * count);
		final Map<Integer,Float> map = new TreeMap<Integer,Float>();
		
		for (int i = 0; i < count; i++) {
			for (int j = 0; j < count; j++) {
				float weight = w.weight(i, j);
			}
		}

		return new WeightFunction() {
			public float weight(int v, int u) {
				return matrix[v][u];
			}
		};
	}
	*/
	
	// Parameters
	private final int count;
	private final WeightFunction wf;
	private final boolean maximize;
	
	public HungarianAlgorithm (final int count, final float[][] w , boolean maximize) {
		this.count = count;
		this.maximize = maximize;

		if (maximize) {
			this.wf = new WeightFunction() {
				public final float weight(int v, int u) {
					return w[v][u];
				}
			};
		} else {
			this.wf = new WeightFunction() {
				public final float weight(int v, int u) {
					return -w[v][u];
				}
			};
		}
	}
	
	public HungarianAlgorithm (final int count, final WeightFunction w, boolean maximize) {
		this.count = count;
		this.maximize = maximize;
		
		if (maximize) {
			this.wf = w;
		} else {
			this.wf = new WeightFunction() {
				public final float weight(int v, int u) {
					return -w.weight(v, u);
				}
			};
		}
	}
	
	public HungarianAlgorithm (final int count, final WeightFunction w) {
		this(count, w, true);
	}
	
	// Current match
	private int[] mateStoT, mateTtoS;
	private float[] u, v;
	
	// MAXIMIZATION version.
	public void solve() {
		// Feasible labeling
		mateStoT = new int[count];
		mateTtoS = new int[count];
		Arrays.fill(mateStoT, -1);
		Arrays.fill(mateTtoS, -1);
		
		u = new float[count];
		v = new float[count];

		for (int i = 0; i < count; i++) {
			// Initialize Feasible Labeling
			float max = Float.NEGATIVE_INFINITY;
			
			for (int j = 0; j < count; j++) {
				max = Math.max(max, wf.weight(i,j));
			}
			
			u[i] = max;
			v[i] = 0;
		}
		
		int nrex = count;
		
		
		while (nrex > 0) {
			final float[] delta = new float[count];	
			// which vertex i has the minimum slack for each j.
			final int[] p = new int[count];
			// ?
			final boolean[] m = new boolean[count];
			Arrays.fill(m, false);
			
			Arrays.fill(p, -1);
			Arrays.fill(delta, Float.POSITIVE_INFINITY);
			
			boolean aug = false;
			
			Set<Integer> Q = new TreeSet<Integer>();

			for (int i = 0; i < count; i++) {
				if (mateStoT[i] < 0) {
					Q.add(i);
				}
			}
			
			do {
				{   // Limit the scope of i
					// Pop one vertex from Q.
					assert (Q.size() > 0);

					Iterator<Integer> iterator = Q.iterator();
					int i = iterator.next();
					iterator.remove();

					m[i] = true;

					for (int j = 0; j < count && ! aug; j++) {
						if (mateStoT[i] != j) {
							final float wc = wf.weight(i,j);
							final float nc = u[i] + v[j] - wc;
							final float ncp = Math.max(nc, 0);
							
							if (ncp < delta[j]) {								
								delta[j] = nc;
								p[j] = i;

								if (delta[j] <= 0) {
									if (mateTtoS[j] < 0) {
										augment(mateStoT, mateTtoS, p, j);
										aug = true;
										nrex--;
									} else {
										Q.add(mateTtoS[j]);
									}
								}
							}
						}
					}
				}
				
				if (! aug && Q.size() == 0) {					
					float d = Float.POSITIVE_INFINITY;
					
					for (int j = 0; j < count; j++) {
						if (delta[j] > 0) {
							d = Math.min(d, delta[j]);
						}
					}
					
					for (int i = 0; i < count; i++) {
						if (m[i]) {
							u[i] -= d;
						}
					}
					
					// Positions in delta that have become zero.
					List<Integer> X = new ArrayList<Integer>();
					
					// Found one j that mateTtoS[j] < 0?
					boolean found = false;
					int value = -1;
					
					{   // Limit the scope of j
						int j;
						for (j = 0; j < count; j++) {
							if (delta[j] <= 0) {
								v[j] += d;
							} else {
								delta[j] -= d;

								if (delta[j] <= 0) {
									if (mateTtoS[j] < 0) {
										// If we found one match, we can forget about updating X any further,
										// so lets break into another loop that doesn't check for that condition.
										value = j;
										found = true;
										j++;
										break;
									}
									
									X.add(mateTtoS[j]);
								}
							}
						}
						
						for (; j < count; j++) {
							if (delta[j] <= 0) {
								v[j] += d;
							} else {
								delta[j] -= d;
							}
						}
					}
					
					if (! found) {
						Q.addAll(X);
					} else {
						augment(mateStoT, mateTtoS, p, value);
						aug = true;
						nrex--;
					}
				}
			} while (! aug);
		}
	}
	
	private void augment(int[] mateStoT, int[] mateTtoS, int[] p, int j) {
		int next;
		
		do {
			int i = p[j];
			mateTtoS[j] = i;
			next = mateStoT[i];
			mateStoT[i] = j;
			
			if (next >= 0) { j = next; }
		} while (next >= 0);
	}

	// output to input
	public int[] getMateTtoS() {
		return mateTtoS;
	}
	
	// input to output
	public int[] getMateStoT() {
		return mateStoT;
	}
	
	public float getScore() {
		// Triple-check result.
		float total1 = 0;
		float total2 = 0;
		float total3 = 0;
		
		for (int i = 0; i < count; i++) {
			total1 += wf.weight(i,mateStoT[i]);
			total2 += wf.weight(mateTtoS[i],i);
			total3 += u[i] + v[i];
		}
		
		float diff1 = Math.abs(total1 - total2);
		float diff2 = Math.abs(total2 - total3);

		float tolerance = 0.001f;
		
		assert(diff1 <= Math.abs(total1) * tolerance);
		assert(diff1 <= Math.abs(total2) * tolerance);
		assert(diff2 <= Math.abs(total2) * tolerance);
		assert(diff2 <= Math.abs(total3) * tolerance);		
		
		return (maximize ? total1: - total1);
	}
	
	public float getScoreWithDummyLastInputs(int dummyCount) {
		float total1 = 0;
		
		for (int i = 0; i < count - dummyCount; i++) {
			total1 += wf.weight(i,mateStoT[i]);
		}
				
		return (maximize ? total1: - total1);
	}
}
