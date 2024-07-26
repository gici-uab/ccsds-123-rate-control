package GiciEntropyCoder.Interface;

import java.io.IOException;

import GiciEntropyCoder.ArithmeticCoder.ProbabilityTable;
import GiciEntropyCoder.ArithmeticCoder.StaticProbabilityTable;


/**
 * This is the interface for classes that implement online decoding functionality. Online in this case means that the
 * encoded data becomes available incrementaly.
 * Samples are decoded one at a time .
 *
 * This interface extends the <code>Decoder</code> interface. Naturaly, any decoder that can be used online can also be
 * used offline.
 */
public interface EntropyDecoder {

	void init(int z);
	/**
	 * Decode the next sample.
	 *
	 * @return The value of the decoded sample.
	 *
	 * @throws IOException if an IO error prevents the process from completing.
	 */
	int decodeSample(int t, int z) throws IOException;
	
	void update(int sample, int t, int z);
	
	void setProbabilityTable(ProbabilityTable pt);
	
	void terminate() throws IOException;
	
	void updateBitCount();
	
	void updateBits();
	
	

}
