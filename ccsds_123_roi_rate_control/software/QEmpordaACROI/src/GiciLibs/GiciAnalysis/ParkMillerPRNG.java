package GiciAnalysis;

import java.util.Random;

class ParkMillerPRNG extends Random {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	long m = 4294967291L;
	long a = 279470273L;
	long c = 0L;
	
	long xn1 = 0;
	
	public ParkMillerPRNG() {
		super();
	}
	
	public ParkMillerPRNG(long seed) {
		super(seed);
		
		xn1 = seed;
	}

	public int nextInt(int n) {
		if (n <= 0)
			throw new IllegalArgumentException("n must be positive");
		
		xn1 = (a * xn1 + c) % m;
		
		// scale from [0, m) to [0, n)
		
		// FIXME: fast but buggy if n is large
		assert (n < 1000000);

		long r = xn1 * n / m;
		
		assert (0 <= r && r < n);
		
		return (int)(r);
	}		
}