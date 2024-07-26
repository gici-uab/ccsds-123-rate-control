package GiciEntropyCoder.ArithmeticCoder;

import java.math.BigInteger;

public interface ProbabilityTable {	
	// This is the cumulative frequency up to the previous symbol
	// i.e., getCumulativeFrequency(0) == 0
	public BigInteger getCumulativeFrequency(BigInteger symbol);
	// Frequency of the current symbol
	//public BigInteger getFrequency(BigInteger symbol); This is not used in  DUBMMQ
	public BigInteger getObservationCount();
	
	// These two methods are deprecated (used by the range encoder)
	public int getSymbolByteSize();
	public BigInteger findSymbolFromFrequency(BigInteger frequency);
	public BigInteger getSymbolCount();
	
	//public boolean update(); //?, improve
	//public void updateState (BigInteger symbol);
}