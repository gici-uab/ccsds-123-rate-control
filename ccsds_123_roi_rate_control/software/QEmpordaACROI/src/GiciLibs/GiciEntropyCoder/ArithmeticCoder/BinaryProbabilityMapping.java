package GiciEntropyCoder.ArithmeticCoder;

import java.math.BigInteger;
import GiciEntropyCoder.ArithmeticCoder.ProbabilityTable;

class BinaryProbabilityMapping {
	final ProbabilityTable probabilityTable;
	final int bitsPerSymbol;
	final BigInteger symbolCount;
	
	final boolean observationCountFitsLong;
	
	public BinaryProbabilityMapping(ProbabilityTable p) {
		this.probabilityTable = p;
		
		
		observationCountFitsLong = p.getObservationCount().compareTo(BigInteger.valueOf(Long.MAX_VALUE)) < 0;
		
		assert (observationCountFitsLong);
		
		symbolCount = p.getSymbolCount(); //number of symbols in rangeBounds
		bitsPerSymbol = symbolCount.subtract(BigInteger.ONE).bitLength(); //length in bits of (symbol in rangeBounds - 1)
	}

	public int[] getProbabilities(boolean[] bits) {
		// Could be done faster by in-lining getPartialProbability temporary results
		assert (bits.length == bitsPerSymbol);
		
		int[] probabilities = new int[bits.length];
		
		for (int i = 0; i < bits.length; i++) {
			probabilities[i] = getPartialProbability(bits, i);
		}
		
		return probabilities;
	}

	public int getBitLength() {
		return bitsPerSymbol;
	}

	/*
	 * Probability for the bit in bits[i] to be 1 knowing bits[0..(i-1)].
	 * Result is in the interval from 0x0001 to 0xAC01, where 0x5601 is for 0 and 1 equally probable (P=0.5).
	 */
	strictfp public int getPartialProbability(boolean[] bits, int i) {	
		assert (i < bits.length && i >= 0);
			
		int extraBits = bits.length - i;
		BigInteger low = bitsToSymbol(bits, i);
		BigInteger high = low.add(BigInteger.ONE.shiftLeft(extraBits));
		
		if (high.compareTo(symbolCount) > 0) {
			high = symbolCount;
		}
		assert (low.compareTo(symbolCount) < 0);
		
		// Set midPoint to the start (included) of the subinterval where the next bit is one. 
		BigInteger midPoint = low.add(BigInteger.ONE.shiftLeft(extraBits - 1)); 
		
		// If the interval that starts with one (midPoint) is over high is means that
		// the combination is not possible and should have 0 probability.
		if (midPoint.compareTo(high) > 0) {
			midPoint = high;
		}
		
		assert (low.compareTo(midPoint) <= 0);
		long cumulativeFrequencyHigh = probabilityTable.getCumulativeFrequency(high).longValue();
		long cumulativeFrequencyLow = probabilityTable.getCumulativeFrequency(low).longValue();
		long cumulativeFrequencyMid = probabilityTable.getCumulativeFrequency(midPoint).longValue();
		
		long totalFrequency = cumulativeFrequencyHigh - cumulativeFrequencyLow;
		long oneFrequency = cumulativeFrequencyHigh - cumulativeFrequencyMid;
		
		int probability = (int) Math.rint(oneFrequency * (double)0xAC02 / totalFrequency);
		
		assert (probability >= 0 && probability <= 0xAC02 + 1);
		
		if (probability < 1) {
			probability = 1;
		} else if (probability > 0xAC01) {
			probability = 0xAC01;
		}
		
		return probability;
	}
	
	/**
	 * Convert one symbol to an equivalent binary codification.
	 * 
	 * @param symbol
	 * @return A bit array where the first position "[0]" is the most significant bit of the binary 
	 * representation of the symbol.
	 */
	public boolean[] getBits(BigInteger symbol) {
		boolean[] r = new boolean[bitsPerSymbol];
		
		for (int i = 0; i < bitsPerSymbol; i++) {
			r[i] = symbol.testBit(bitsPerSymbol - i - 1);
		}
		
		return r;
	}

	/**
	 * Inverse of getBits.
	 * @param bits
	 * @return
	 */
	public BigInteger bitsToSymbol(boolean[] bits) {
//		TODO: debug points
		return bitsToSymbol(bits, bits.length);
	}
	
	/**
	 * Uses the length most significant bits of bits to build a symbols, assuming the remaining bits are set to zero.
	 * @param bits
	 * @param length
	 * @return
	 */
	private BigInteger bitsToSymbol(boolean[] bits, int length) {
		
		assert (bits.length == bitsPerSymbol);
		assert (length <= bits.length);
		
		BigInteger symbol = BigInteger.ZERO;
		
		for (int i = 0; i < length; i++) {
			if (bits[i]) {
				symbol = symbol.setBit(bitsPerSymbol - i - 1);
			}
		}
		
		return symbol;
	}
}
