package GiciAnalysis;

import java.util.concurrent.Callable;

public class SpectralMetrics implements Callable<SpectralMetrics> {
	// State
	boolean called = false;
	
	// Source Data
	// original
	final float[][][] image1;
	// modified
	final float[][][] image2;
	
	final int sizeX;
	final int sizeY;
	final int sizeZ;

	// Results
	
	// From "Quality Criteria Benchmark for Hyperspectral Imagery"
	// Emmanuel Christophe, Student Member, IEEE, Dominique Léger, and Corinne Mailhes, Member, IEEE
	
	/* specific to hyperspectral */
	
	// S. Rupert, M. Sharp, J. Sweet, and E. Cincotta, "Noise perspectral data compression," in Proc. IGARSS, vol. 94-96.
	double maximumSpectralSimilarity;
	// 
	double maximumSpectralAngle;
	// B. Aiazzi, L. Alparone, S. Baronti, C. Lastri, L. Santurri, and M. Selva, "Spectral distortion evaluation 
	// in lossy compression of hyperspectral imagery," in Proc. IGARSS, vol. 3, Jul. 2003, pp. 1817-1819.
    // This criterion is based on the Kullback–Leibler distance, which measures the distance between
	// two spectra viewed as distributions.
	double maximumSpectralInformationDivergence;
	// 
	double minimumPearsonsCorrelation;

	/* adaptations of image criteria */
	
	// A Universal Image Quality Index Zhou Wang and Alan C. Bovik
	double wangIndexLambda;
	double wangIndexStack;
	double wangIndexBoth;

	// A. M. Eskicioglu and P. S. Fisher, "Image quality performance," IEEE Trans. Commun., vol. 43, no. 12, Dec. 1995.
	double eskiciogluCubeFidelity;
	double eskiciogluSpectralFidelity;
	double eskiciogluStackFidelity;
	
	// From the CCSDS requirements
	double biasError;
	
	/**
	 * Order might matter.
	 * @param image1
	 * @param image2
	 */
	public SpectralMetrics(final float[][][] image1, final float[][][] image2) {
		super();
		
		this.image1 = image1;
		this.image2 = image2;

		assert(image1.length == image2.length);
		assert(image1[0].length == image2[0].length);
		assert(image1[0][0].length == image2[0][0].length);
		
		this.sizeZ = image1.length;
		this.sizeY = image1[0].length;
		this.sizeX = image1[0][0].length;
	}

	private double WangQ (double m1, double m2, double v1, double v2, double v12) {
		return (4 * v12 * m1 * m2) / ((v1 + v2) * (m1 * m1 + m2 * m2));
	}
	
	public SpectralMetrics call() throws Exception {
		// lets do the work
		
		// MSS
		maximumSpectralSimilarity = Double.NEGATIVE_INFINITY;
		minimumPearsonsCorrelation = Double.POSITIVE_INFINITY;
		
		// MSA
		maximumSpectralAngle = Double.NEGATIVE_INFINITY;
		
		// MSID
		maximumSpectralInformationDivergence = Double.NEGATIVE_INFINITY;
		//System.out.println("1:" + System.currentTimeMillis());
		
		double[] biasErrorPerBand = new double[sizeZ];
		
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				//MSS / Pearson 
				double mse = 0;
				
				for (int z = 0; z < sizeZ; z++) {
					double d = image1[z][y][x] - image2[z][y][x];
					mse += (d * d);
					
					biasErrorPerBand[z] += d;
				}
				
				mse /= (double) sizeZ;
				
				double m1 = 0, m2 = 0;
								
				for (int z = 0; z < sizeZ; z++) {
					m1 += image1[z][y][x];
					m2 += image2[z][y][x]; 
				}
				
				m1 /= (double) sizeZ;
				m2 /= (double) sizeZ;
				
				double v1 = 0, v2 = 0;
				double corr = 0;
				
				for (int z = 0; z < sizeZ; z++) {
					v1 += (image1[z][y][x] - m1) * (image1[z][y][x] - m1);
					v2 += (image2[z][y][x] - m2) * (image2[z][y][x] - m2);
					corr += (image1[z][y][x] - m1) * (image2[z][y][x] - m2); 
				}
				
				v1 /= (double) sizeZ - 1;
				v2 /= (double) sizeZ - 1;
				
				corr /= (sizeZ - 1) * Math.sqrt(v1) * Math.sqrt(v2);
				
				if (! Double.isNaN(corr)) {
					// Ignore correlations on components with 0 variance
					minimumPearsonsCorrelation = Math.min(minimumPearsonsCorrelation, corr);

					double corr2m1 = 1 - corr * corr;
					double ss = Math.sqrt(mse + corr2m1 * corr2m1);
				
					maximumSpectralSimilarity = Math.max(maximumSpectralSimilarity, ss);
				}
				
				// MSA
				double s1 = 0, s2 = 0, s3 = 0;
				
				for (int z = 0; z < sizeZ; z++) {
						s1 += image1[z][y][x] * image2[z][y][x];
						s2 += image1[z][y][x] * image1[z][y][x];
						s3 += image2[z][y][x] * image2[z][y][x];
				}
				
				// TODO check precision here
				double t = s1 / Math.sqrt(s2 * s3); 
				
				// Check for slightly larger dot products that produce NaNs on acos.
				// For the same price we get 0 degrees on a zero vector
				if (Math.abs(t) > 1) {
					t = Math.signum(t);
				}
				
				maximumSpectralAngle = Math.max(maximumSpectralAngle, Math.acos(t));
		
				double manhattanNorm1 = 0, manhattanNorm2 = 0;
				
				for (int z = 0; z < sizeZ; z++) {
					manhattanNorm1 += Math.abs(image1[z][y][x]);
					manhattanNorm2 += Math.abs(image2[z][y][x]);
				}
				
				double id = 0;
				
				for (int z = 0; z < sizeZ; z++) {
					double p1 = image1[z][y][x] / manhattanNorm1;
					double p2 = image2[z][y][x] / manhattanNorm2;
					
					id += (p1 - p2) * (Math.log(p1) - Math.log(p2));
				}

				maximumSpectralInformationDivergence = Math.max(maximumSpectralInformationDivergence, id);
			}
		}
		
		biasError = Double.NEGATIVE_INFINITY;
		
		for (int z = 0; z < sizeZ; z++) {
			biasError = Math.max(biasError, biasErrorPerBand[z] / (sizeX * sizeY));
		}
		
		// WangMetrics
		//System.out.println("2:" + System.currentTimeMillis());
		wangIndexLambda = Double.POSITIVE_INFINITY;
		
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				double m1 = 0, m2 = 0, v1 = 0, v2 = 0, v12 = 0;
					
				for (int z = 0; z < sizeZ; z++) {
					m1 += image1[z][y][x];
					m2 += image2[z][y][x]; 
				}
				
				m1 /= (double) sizeZ;
				m2 /= (double) sizeZ;
				
				for (int z = 0; z < sizeZ; z++) {
					v1 += (image1[z][y][x] - m1) * (image1[z][y][x] - m1);
					v2 += (image2[z][y][x] - m2) * (image2[z][y][x] - m2);
					v12 += (image1[z][y][x] - m1) * (image2[z][y][x] - m2);
				}
				
				v1 /= (double) sizeZ - 1;
				v2 /= (double) sizeZ - 1;
				v12 /= (double) sizeZ - 1;
				
				double q = WangQ(m1, m2, v1, v2, v12);
				
				if (! Double.isNaN(q))
					wangIndexLambda = Math.min(wangIndexLambda, q);
			}
		}
		
		wangIndexStack = Double.POSITIVE_INFINITY;
		
		for (int z = 0; z < sizeZ; z++) {
			double m1 = 0, m2 = 0, v1 = 0, v2 = 0, v12 = 0;
			for (int x = 0; x < sizeX; x++) {
				for (int y = 0; y < sizeY; y++) {
					m1 += image1[z][y][x];
					m2 += image2[z][y][x]; 
				}
			}

			m1 /= (double) sizeX * sizeY;
			m2 /= (double) sizeX * sizeY;

			for (int x = 0; x < sizeX; x++) {
				for (int y = 0; y < sizeY; y++) {
					v1 += (image1[z][y][x] - m1) * (image1[z][y][x] - m1);
					v2 += (image2[z][y][x] - m2) * (image2[z][y][x] - m2);
					v12 += (image1[z][y][x] - m1) * (image2[z][y][x] - m2);
				}
			}

			v1 /= (double) (sizeX * sizeY) - 1;
			v2 /= (double) (sizeX * sizeY) - 1;
			v12 /= (double) (sizeX * sizeY) - 1;

			double q = WangQ(m1, m2, v1, v2, v12);
				
			if (! Double.isNaN(q))
				wangIndexStack = Math.min(wangIndexStack, q);
		}
	
		wangIndexBoth = wangIndexLambda * wangIndexStack;

		// Eskicioglu
		//System.out.println("3:" + System.currentTimeMillis());
		
		eskiciogluSpectralFidelity = Double.POSITIVE_INFINITY;
		
		eskiciogluCubeFidelity = 0;
		double totalEnergy = 0;
		
		for (int x = 0; x < sizeX; x++) {
			for (int y = 0; y < sizeY; y++) {
				double SpectralEngery = 0;
				double eLambda = 0;
				
				for (int z = 0; z < sizeZ; z++) {
					double d = image1[z][y][x] - image2[z][y][x];	
					
					eLambda += d * d;
					SpectralEngery += image1[z][y][x] * image1[z][y][x];
					
					eskiciogluCubeFidelity += d * d;
					totalEnergy += image1[z][y][x] * image1[z][y][x];
				}
				
				eskiciogluSpectralFidelity = Math.min(eskiciogluSpectralFidelity, 1 - eLambda / SpectralEngery);
			}
		}
		
		eskiciogluCubeFidelity = 1 - eskiciogluCubeFidelity / totalEnergy;
		
		eskiciogluStackFidelity = Double.POSITIVE_INFINITY;
		
		for (int z = 0; z < sizeZ; z++) {
			double StackEngery = 0;
			double eLambda = 0;
			
			for (int x = 0; x < sizeX; x++) {	
				for (int y = 0; y < sizeY; y++) {
					double d = image1[z][y][x] - image2[z][y][x];	

					eLambda += d * d;
					StackEngery += image1[z][y][x] * image1[z][y][x];
				}
			}

			eskiciogluStackFidelity = Math.min(eskiciogluStackFidelity, 1 - eLambda / StackEngery);
		}
		
		// done
		called = true;
		return this;
	}

	// Getters
	public double getEskiciogluCubeFidelity() {
		assert(called);
		return eskiciogluCubeFidelity;
	}

	public double getEskiciogluSpectralFidelity() {
		assert(called);
		return eskiciogluSpectralFidelity;
	}

	public double getEskiciogluStackFidelity() {
		assert(called);
		return eskiciogluStackFidelity;
	}

	public double getMaximumSpectralAngle() {
		assert(called);
		return maximumSpectralAngle;
	}

	public double getMaximumSpectralInformationDivergence() {
		assert(called);
		return maximumSpectralInformationDivergence;
	}

	public double getMaximumSpectralSimilarity() {
		assert(called);
		return maximumSpectralSimilarity;
	}

	public double getMinimumPearsonsCorrelation() {
		assert(called);
		return minimumPearsonsCorrelation;
	}

	public double getWangIndexBoth() {
		assert(called);
		return wangIndexBoth;
	}

	public double getWangIndexLambda() {
		assert(called);
		return wangIndexLambda;
	}

	public double getWangIndexStack() {
		assert(called);
		return wangIndexStack;
	}
	
	public double getBiasError() {
		assert(called);
		return biasError;
	}
}
