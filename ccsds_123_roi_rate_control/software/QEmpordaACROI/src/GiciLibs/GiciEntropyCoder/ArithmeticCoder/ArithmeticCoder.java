package GiciEntropyCoder.ArithmeticCoder;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

import GiciEntropyCoder.Interface.EntropyCoder;
import GiciStream.ByteStream;


/**
 * write
 *
 */

public class ArithmeticCoder implements EntropyCoder {
	
	private final int bands;
	private final int width;
	private final int height;
	
	BinaryProbabilityMapping binaryProbabilityMapping;
	final OutputStream outputStream;
	
	final ByteArrayOutputStream outputByteStream;
	final DumbMQCoder dumbMQCoder;
	
	public ArithmeticCoder(OutputStream outputStream, int bands, int height, int width) {
		
		this.outputStream = outputStream;
		
		this.outputByteStream = new ByteArrayOutputStream();
		this.dumbMQCoder = new DumbMQCoder(outputByteStream);
		this.bands = bands;
		this.height = height;
		this.width = width;
	}
	
	public void setProbabilityTable(ProbabilityTable probabilityTable){
		this.binaryProbabilityMapping = new BinaryProbabilityMapping(probabilityTable);
	}

	public void codeSymbol (BigInteger symbol) throws IOException {	
		boolean [] bits = binaryProbabilityMapping.getBits(symbol);
		int [] probabilities = binaryProbabilityMapping.getProbabilities(bits);

		for (int i = 0; i < bits.length; i++) {
			dumbMQCoder.encodeBitProb(bits[i], probabilities[i]);
		}
	}

	boolean terminated = false;
	
	public void terminate() throws IOException {
		
		assert (!terminated);
		terminated = true;
		
		dumbMQCoder.terminate();
		DataOutputStream dos = new DataOutputStream(outputStream);
		dos.writeLong(outputByteStream.size());
		dos.flush();
		
		outputByteStream.writeTo(outputStream);
	}

	@Override
	public void init(int z) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(int sample, int t, int z) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void codeSample(int sample, int t, int z) throws IOException {
		BigInteger symbol = BigInteger.valueOf(sample);
		codeSymbol(symbol);
	}

	@Override
	public void terminate(boolean verbose) throws IOException {
		terminate();
	}

	@Override
	public double getRate(boolean verbose) {
		double bpppb=outputByteStream.size()/(float)(bands*height*width);
		if (verbose){
			System.out.println("\n bpppb: "+bpppb);
		}
		return bpppb;
	}

	@Override
	public void updateHistogram(int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getEntropy(int samples) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getNumBitsWritten() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void resetNumBitsWritten() {
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
	public boolean decodeBitProb(int prob) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void encodeIntegerProb(int i, int prob) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getNumBitsWrittenLine() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void resetNumBitsWrittenLine() {
		// TODO Auto-generated method stub
		
	}

}
