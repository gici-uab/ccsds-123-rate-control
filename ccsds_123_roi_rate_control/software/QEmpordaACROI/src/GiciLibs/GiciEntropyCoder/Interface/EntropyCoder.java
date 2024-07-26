package GiciEntropyCoder.Interface;

import java.io.IOException;

import GiciEntropyCoder.ArithmeticCoder.ProbabilityTable;
import GiciStream.ByteStream;


/**
 * This is the interface for classes that implement online encoding functionality. Online in this case means that the
 * data to be encoded becomes available incrementaly.
 * Samples are encoded one at a time.
 *
 * This interface extends the <code>Coder</code> interface. Naturaly any encoder that can be used online can also be
 * used offline.
 */
public interface EntropyCoder {

	void setProbabilityTable(ProbabilityTable pt);
	
	void init(int z);
	
	void update(int sample, int t, int z);
	
	/**
	 * Encodes the next sample.
	 *
	 * @param sample The next sample to be encoded.
	 *
	 * @throws IOException if an IO error prevents the process from completing.
	 */
	void codeSample(int sample, int t, int z) throws IOException;


	/**
	 * Ensures that the encoding process has finished correctly.
	 * The classes that implement this interface might be doing any number of things behind the scenes (such as
	 * buffering or preprocessing). This function must allways be called after the last <code>codeSample</code> call to
	 * ensure that all data has been written to the output stream.
	 *
	 * @throws IOException if an IO error prevents the process from completing.
	 */
	void terminate() throws IOException;
	
	void terminate(boolean verbose) throws IOException;
	
	double getRate(boolean verbose);
	
	void updateHistogram (int value);
	
	float getEntropy (int samples);
	
	public long getNumBitsWrittenLine();
	
	public void resetNumBitsWrittenLine();

	public long getNumBitsWritten();
	
	public void resetNumBitsWritten();
	
	public void encodeBit(boolean bit);
	
	public boolean decodeBit() throws Exception;
	
	public void encodeBitContext(boolean bit, int context);
	
	public void encodeBitProb(boolean bit, int prob);
	
	public boolean decodeBitContext(int context) throws Exception;
	
	public boolean decodeBitProb(int prob) throws Exception;
	
	public void changeStream(ByteStream stream);
	
	public void reset();
	
	public void restartDecoding() throws Exception;
	
	public int remainingBytes();
	
	public int getReadBytes();
	
	public void restartEncoding();

	void encodeInteger(int num, int numBits);
	
	public int decodeInteger(int numBits);

	public ByteStream getByteStream();

	void encodeIntegerProb(int i, int prob);
}
