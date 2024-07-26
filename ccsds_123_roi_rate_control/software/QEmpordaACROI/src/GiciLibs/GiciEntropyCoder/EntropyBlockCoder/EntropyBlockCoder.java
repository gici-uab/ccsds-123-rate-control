
package GiciEntropyCoder.EntropyBlockCoder;

import GiciStream.BitOutputStream;
import GiciStream.ByteStream;
import GiciEntropyCoder.Interface.EntropyCoder;
import GiciEntropyCoder.ArithmeticCoder.ProbabilityTable;
import GiciEntropyCoder.BlockAdaptiveCoder.BlockAdaptiveCoder;

import java.io.IOException;


/**
 * This Class implements a block adaptive encoder.
 *
 * The requirements for this coder are specifies in the CCSDS 123.0-R-1 standard.
 *
 * The methods of this class that perform coding, take their inputs as unsigned ints.
 * This means that the most significant bit will be treated as indicating magnitude instead of sign.
 * The above will not matter for emporda, where the maximum dynamic range is 16 bits.
 */
public class EntropyBlockCoder implements EntropyCoder {

	private final BlockAdaptiveCoder coder;



	/**
	 * Constructor.
	 *
	 * @param bos is a <code>BitInputStream</code> from which samples will be decoded.
	 * @param blockSize is used to split data into blocks (8 or 16).
	 * @param dynamicRange specifies how many bits of each sample are significant (from 2 to 16).
	 * @param referenceInterval Reference Sample Interval (up to 4096).
	 * @param bandSequential Sets the sample order to Band Sequential. The alternativew is Band Interleaved.
	 * @param interleavingDepth The interleaving depth for the Band Interleaved order (up to the number of bands).
	 * @param verbose Whether to print progress messages.
	 */
	public EntropyBlockCoder(
		BitOutputStream bos,
		int blockSize,
		int dynamicRange,
		int referenceInterval,
		boolean verbose)
	{
		if (blockSize != 8 && blockSize != 16) {
			throw new RuntimeException("Block Size must be either 8 or 16");
		}
		if (dynamicRange < 2 || dynamicRange > 16) {
			throw new RuntimeException("Dynamic range must be between 2 and 16");
		}
		this.coder = new BlockAdaptiveCoder(bos, blockSize, dynamicRange, false, referenceInterval, 64);

	}

	public void init(int z) {
		coder.init(z);
		// TODO Auto-generated method stub
		
	}


	public void update(int sample, int t, int z) {
		coder.update(sample, t, z);
		// TODO Auto-generated method stub
		
	}


	public void codeSample(int sample, int t, int z) throws IOException {
		coder.codeSample(sample, t, z);
		// TODO Auto-generated method stub
		
	}


	public void terminate() throws IOException {
		coder.terminate();
		// TODO Auto-generated method stub
		
	}
	public void terminate(boolean verbose) throws IOException {
		coder.terminate();
		// TODO Auto-generated method stub
		
	}
	public double getRate(boolean verbose){
		// TODO Auto-generated method stub
		double bpppb = 0;
		return bpppb;
	}
	public float getEntropy(int samples){
		float entropy = 0;
		// TODO Auto-generated method stub
		return entropy;
	}
	public void updateHistogram(int value){
		// TODO Auto-generated method stub
	}
	public long getNumBitsWritten(){
		return coder.getNumBitsWritten();
	}
	public void resetNumBitsWritten(){
		coder.resetNumBitsWritten();
	}

	public void updateProbabilityTable(ProbabilityTable newProbabilityTable) {
		
	}

	@Override
	public void setProbabilityTable(ProbabilityTable pt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void encodeBit(boolean bit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean decodeBit() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void encodeBitContext(boolean bit, int context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean decodeBitContext(int context) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void changeStream(ByteStream stream) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restartDecoding() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int remainingBytes() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getReadBytes() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void restartEncoding() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void encodeInteger(int num, int numBits) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ByteStream getByteStream() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int decodeInteger(int numBits) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void encodeBitProb(boolean bit, int prob) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean decodeBitProb(int prob) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void encodeIntegerProb(int i, int prob) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getNumBitsWrittenLine() {
		
		return  coder.getNumBitsWrittenLine();
	}

	@Override
	public void resetNumBitsWrittenLine() {
		// TODO Auto-generated method stub
		
	}
	

}
