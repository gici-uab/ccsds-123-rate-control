package GiciAnalysis;

import java.util.*;
import java.util.concurrent.Callable;

public strictfp class IsoData implements Callable<int[]> {
	final float[][] points;
	final int numberOfPoints;
	final int dimension;
	
	/* Metric */
	final VectorDistance metric;
	
	/* Internal parameters */
	int iterationsRemaining;
	
	final int desiredClusters;
	final int maximumPairToBeMerged;
	final int minimumSamplesPerCluster;
	final float deviationToSplit;
	final float distanceToMerge;
	
	/* Optimizations */
	Integer [][] neighborClusters;
	
	/* state */
	int clusters;
	int [] pointToCluster;	
	float [][] clusterCenters;
	
	private class QueueElement {
		public final float weight;
		public final int a;
		public final int b;
		
		public QueueElement (final int _a, final int _b, final float _w) {
			a = _a;
			b = _b;
			weight = _w;
		}
	}
	
	private class CompareQueueElements implements Comparator<QueueElement> {
		public int compare(QueueElement o1, QueueElement o2) {
			float f1 = o1.weight;
			float f2 = o2.weight;
			
			if (f1 < f2) {
				return -1;
			} else if (f1 > f2) {
				return 1;
			} else {
				return 0;
			}			
		}
		
		public boolean equals(Object obj) {
			// infinite loop?
			return this.equals(obj);
		}
	}

	// _points[number of points][coordinates of each point]
	
	// deprecated constructors
	public IsoData(final float[][] _points, final VectorDistance _metric) {
		this(_points, _metric, 2);
	}
	
	public IsoData(final float[][] _points, final VectorDistance _metric, final int _desiredClusters) {
		this(_points, _metric, _desiredClusters, 100, 0, Float.POSITIVE_INFINITY, 0, 0);
	}

	/**
	 * k-means constructor.
	 * @param _points
	 * @param _metric
	 * @param _desiredClusters
	 * @param _iterations
	 */
	public IsoData(final float[][] _points, final VectorDistance _metric, final int _desiredClusters, final int _iterations) {
		this(_points, _metric, _desiredClusters, _iterations, 0, Float.POSITIVE_INFINITY, 0, 0);
	}
	
	/**
	 * Isodata constructor.
	 * @param _points
	 * @param _metric
	 * @param _desiredClusters
	 * @param _iterations
	 * @param _minimumSamplesPerCluster
	 * @param _deviationToSplit
	 * @param _maximumPairToBeMerged
	 * @param _distanceToMerge
	 */
	public IsoData(final float[][] _points, final VectorDistance _metric,
			 final int _desiredClusters, final int _iterations,
			 final int _minimumSamplesPerCluster, final float _deviationToSplit,
			 final int _maximumPairToBeMerged, final float _distanceToMerge) {
		
		/* set internal parameters */
		iterationsRemaining = _iterations;
		
		desiredClusters = _desiredClusters;
		maximumPairToBeMerged = _maximumPairToBeMerged;
		minimumSamplesPerCluster = _minimumSamplesPerCluster;
		deviationToSplit = _deviationToSplit;
		distanceToMerge = _distanceToMerge;
		
		/* set metric */
		metric = _metric;
		
		/* points */
		points = _points;
		
		numberOfPoints = points.length;	
		assert(numberOfPoints > 0);
	
		dimension = points[0].length;	
		assert(dimension > 0);
		
		assert(minimumSamplesPerCluster * desiredClusters <= numberOfPoints);
		
		pointToCluster = new int[numberOfPoints];
		
		/* clusters */
		clusters = desiredClusters;
		// No more that 2 * desiredClusters allowed
		clusterCenters = new float[2 * clusters][dimension];
		
		final boolean disable = false;
		if (disable) {
			// Create random clustersCenters
			for (int i = 0; i < clusters; i++) {
				for (int j = 0; j < dimension; j++) {
					clusterCenters[i][j] = (j / (dimension / clusters) == i? 1 : 0);
				}
			}
		} else {
			// Pick random clustersCenters
			Random r = new Random(13);
			
			for (int i = 0; i < clusters; i++) {
				int w = r.nextInt(points.length);
				
				for (int j = 0; j < dimension; j++) {
					clusterCenters[i][j] = points[w][j];
				}
			}
		}
		
		/* Optimization initialization */
		neighborClusters = new Integer[clusters][clusters];
		for (int i = 0; i < clusters; i++) {		
			for (int j = 0; j < clusters; j++) {
				neighborClusters[i][j] = j;
			}			
		}
	}
	
	class ClossestToCentroid implements Comparator<Integer> {
		final float[][] distances;
		int origin;
		
		public ClossestToCentroid(float[][] distances) {
			this.distances = distances;
		}
		
		public void setCentroid (int c) {
			origin = c;
		}
		
		public int compare(Integer i, Integer j) {
			if (distances[i][origin] > distances[j][origin]) {
				return 1;
			} else if (distances[i][origin] < distances[j][origin]) {
				return -1;
			} else {
				return 0;
			}
		}
	};	
	
	private void iterationReclassifyPoints() {
		int i, j;
		
		// Assign points to the closer center
		// Assuming the transitive property of metrics try to optimize this a little bit 
		
		//System.out.println("1:" + System.currentTimeMillis());
				
		float[][] mf = new float[clusters][clusters];
	
		for (i = 0; i < clusters; i++) {
			for (j = i; j < clusters; j++) {
				mf[j][i] = mf[i][j] = metric.distance(clusterCenters[i], clusterCenters[j]) / 2;
				
				assert (! Float.isNaN(mf[i][j]));
			}
		}
		
		// Sort clusters in relation to distance to each other to scan clusters in
		// order of expected success
		ClossestToCentroid ctc = new ClossestToCentroid(mf);
		
		for (i = 0; i < clusters; i++) {
			ctc.setCentroid(i);
			Arrays.sort(neighborClusters[i], ctc);			
		}
		
		//System.out.println("2:" + System.currentTimeMillis());
		
		// FIXME check proper distances for negative metrics
		int[] pendingClusters = new int[clusters];
		
		for (i = 0; i < numberOfPoints; i++) {
			float min = Float.POSITIVE_INFINITY; 
			int pos = -1;
			
			// 0=pending
			
			for (j = 0; j < clusters; j++) 
				pendingClusters[j] = 0;
			
			int oldCluster = pointToCluster[i];
			Integer[] preferedScanOrder = neighborClusters[oldCluster];
			
			for (int jp = 0; jp < clusters; jp++) {
				// Start with clusters that are closer to where we were
				j = preferedScanOrder[jp];
				
				// Skip clusters that we know are far away from the current best
				if (pendingClusters[j] != 0)
					continue;
				
				float t = metric.distance(clusterCenters[j], points[i]);
				
				assert (! Float.isNaN(t));
				
				if (min >= t) {
					min = t;
					pos = j;
					
					// Mark to skip others
					for (int kp = jp + 1; kp < clusters; kp++) {
						int k = preferedScanOrder[kp];
						
						if (mf[pos][k] > min)
							pendingClusters[k] = 1;
					}
				}
			}
			
			//assert(pos>=0);
			pointToCluster[i] = pos;
			
			assert(pos >= 0);
		}
	}
	
	private int[] iterationCleanUp() {
		int i, j;
		
		// Discard clusters with few samples
		//System.out.println("2:" + System.currentTimeMillis());
		
		int [] pointsPerCluster = new int[clusters];
		int [] clusterMap = new int[clusters];
		int deletedClusters = 0;
		
		for (i = 0; i < clusters; i++) {
			clusterMap[i] = i;
		}
		
		for (i = 0; i < numberOfPoints; i++) {
			pointsPerCluster[pointToCluster[i]]++;
		}
		
		for (i = 0; i < clusters - deletedClusters; i++) {
			if (pointsPerCluster[i] < minimumSamplesPerCluster) {
				clusterMap[i] = -1;
				clusterMap[clusters - deletedClusters - 1] = i;
				
				pointsPerCluster[i] = pointsPerCluster[clusters - deletedClusters - 1];
				pointsPerCluster[clusters - deletedClusters - 1] = 0;
				
				deletedClusters++;
				
				// force reexamine this swap
				i--;
			}
		}
		
		for (i = 0; i < numberOfPoints; i++) {
			pointToCluster[i] = clusterMap[pointToCluster[i]];
		}
		
		clusters -= deletedClusters;
		
		// Update cluster centers
		//System.out.println("3:" + System.currentTimeMillis());
		
		for (i = 0; i < clusters; i++) {
			if (pointsPerCluster[i] == 0)
				continue;
			
			// Zero cluster
			for (j = 0; j < dimension; j++) {
				clusterCenters[i][j] = 0;
			}
		}
		
		for (i = 0; i < numberOfPoints; i++) {
			// Accumulate
			for (j = 0; j < dimension; j++) {
				if (pointToCluster[i] < 0)
					continue;
				
				clusterCenters[pointToCluster[i]][j] += points[i][j];	
			}
		}
		
		for (i = 0; i < clusters; i++) {
			if (pointsPerCluster[i] == 0)
				continue;
			
			// Average with pointsPerCluster
			for (j = 0; j < dimension; j++) {
				clusterCenters[i][j] /= pointsPerCluster[i];
			}
		}
			
		return pointsPerCluster;
	}
	
	/**
	 * 
	 * @return whether it should count as an iteration or not
	 */
	private boolean iteration() {
		int i, j;
		
		iterationReclassifyPoints();
		int [] pointsPerCluster = iterationCleanUp();
		
		//System.out.println("4:" + System.currentTimeMillis());
		
		if (clusters < 4 * desiredClusters / 5) {
			// split
			
			// Compute Average Distances
			float[] averageDistances = new float[clusters];
			float averageDistance = 0;
			int clusteredPoints = 0;
			
			for (i = 0; i < clusters; i++) {
				// Zero
				averageDistances[i] = 0;
			}
			
			for (i = 0; i < numberOfPoints; i++) {
				// Accumulate
				if (pointToCluster[i] < 0)
					continue;
				
				averageDistances[pointToCluster[i]] += metric.distance(clusterCenters[pointToCluster[i]], points[i]);
			}
			
			for (i = 0; i < clusters; i++) {
				// Average with pointsPerCluster
				averageDistance += averageDistances[i];
				clusteredPoints += pointsPerCluster[i];
				
				averageDistances[i] /= pointsPerCluster[i];
			}
			
			averageDistance /= clusteredPoints;
			
			// standard deviation for each cluster
			float[][] deviationVector = new float[clusters][dimension];
			float deviation;
			
			for (i = 0; i < numberOfPoints; i++) {
				if (pointToCluster[i] < 0)
					continue;
				
				for (j = 0; j < dimension; j++) {
					deviationVector[pointToCluster[i]][j] += (clusterCenters[pointToCluster[i]][j] - points[i][j])
						* (clusterCenters[pointToCluster[i]][j] - points[i][j]);
				}
			}
			
			int clustersSplited = 0;
			
			for (i = 0; i < clusters; i++) {
				
				float max = Float.NEGATIVE_INFINITY;
				int pos = -1;
				
				for (j = 0; j < dimension; j++) {
					if (deviationVector[i][j] > max) {
						max = deviationVector[i][j];
						pos = j;
					}
				}
				
				deviation = (float)Math.sqrt(deviationVector[i][pos] / pointsPerCluster[i]);
				
				if (deviation > deviationToSplit && averageDistances[i] > averageDistance
						&& pointsPerCluster[i] > 2 * minimumSamplesPerCluster) {
					// Split shall we (we won't add more that desiredClusters / 2 in any case)
					
					System.arraycopy(clusterCenters[i], 0, clusterCenters[clusters + clustersSplited], 0, clusterCenters[i].length);
					
					float alpha = 0.9f;
					
					clusterCenters[i][j] += alpha * deviation;
					clusterCenters[clusters + clustersSplited][j] -= alpha * deviation;
					
					clustersSplited ++;
				}
			
				clusters += clustersSplited;
			}
			
			// 
			
			return true;
		} else if (3 * clusters > 4 * desiredClusters) {
			// merge
			
			// Pairwise distances between cluster centers
			
			PriorityQueue<QueueElement> queue = new PriorityQueue<QueueElement>(2, new CompareQueueElements());
			
			for (i = 0; i < clusters; i++) {
				for (j = 0; j < clusters; j++) {
					if (i >= j) {
						continue;
					}
					
					QueueElement o = new QueueElement(i, j, metric.distance(clusterCenters[i], clusterCenters[j]));
					queue.offer(o);
				}
			}
			
			boolean[] usedClusters = new boolean[clusters];
			int[] mergeMap = new int[clusters];
			int mergedClusters = 0;
			
			for (i = 0; i < clusters; i++) {
				mergeMap[i] = i;
			}
			
			for (i = 0; i < maximumPairToBeMerged; i++) {
				QueueElement o = queue.poll();
				
				if (usedClusters[o.a] || usedClusters[o.b] || o.weight >= distanceToMerge) {
					continue;
				}
				
				usedClusters[o.a] = true;
				usedClusters[o.b] = true;
				
				mergeMap[o.b] = o.a;
				
				mergedClusters++;
			}
			
			for (i = 0; i < numberOfPoints; i++) {
				if (pointToCluster[i] < 0)
					continue;
				
				pointToCluster[i] = mergeMap[pointToCluster[i]];
			}
			
			clusters -= mergedClusters;
			
			return false;
		} else {
			// we are done for this iteration
			//System.out.println("5:" + System.currentTimeMillis());
			
			return true;
		}
	}
	
	public int[] call() throws Exception {
		int stable = 0;
		
		while (iterationsRemaining > 0) {
			float[][] savedClusterCenters = new float[clusters][]; 
			
			for (int i = 0; i < clusters; i++) {
				savedClusterCenters[i] = Arrays.copyOf(clusterCenters[i], dimension);
			}
			
			int oldClusters = clusters;
			
			if (iteration())
				iterationsRemaining--;
			
			// Check stability
			stable++;
			
			if (oldClusters != clusters) {
				stable = 0;
			} else {
				done: for (int i = 0; i < clusters; i++) {
					for (int j = 0; j < dimension; j++) {
						if (Math.abs(savedClusterCenters[i][j] - clusterCenters[i][j]) > 0.0001f) {
							stable = 0;
							break done;
						}
					}
				}
			}
			
			if (stable > 2) {
				//System.err.println("done clustering by stability.");
				break;
			}
		}
		
		return pointToCluster;
	}
}
