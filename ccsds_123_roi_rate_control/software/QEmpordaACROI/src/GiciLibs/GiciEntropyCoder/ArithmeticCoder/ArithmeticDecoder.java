package GiciEntropyCoder.ArithmeticCoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import GiciEntropyCoder.Interface.EntropyDecoder;

public class ArithmeticDecoder implements EntropyDecoder {
	BinaryProbabilityMapping binaryProbabilityMapping;
	final DumbMQDecoder dumbMQDecoder;
	
	int bitCount;
	boolean[] bits;
	
	class LimitedInputStream extends FilterInputStream {
		long sizeLimit;
		long sizeCount;

	    protected LimitedInputStream(InputStream arg0) {
			super(arg0);
			DataInputStream ds = new DataInputStream(arg0);
			try {
				this.sizeLimit = ds.readLong();
				//	ds.close();
			} catch (IOException e) {
				e.printStackTrace();		
			}
			
			this.sizeCount = 0;
		}
		
		public int read() throws IOException {
			if (sizeCount < sizeLimit) {
				sizeCount++;
				return super.read();
			} else {
				return -1;
			}
		}
	}
	
	public ArithmeticDecoder(InputStream inputStream) throws FileNotFoundException {
		this.dumbMQDecoder = new DumbMQDecoder(new LimitedInputStream(inputStream));
	}
	
	public void setProbabilityTable(ProbabilityTable probabilityTable){
		this.binaryProbabilityMapping = new BinaryProbabilityMapping(probabilityTable);
		bitCount = binaryProbabilityMapping.getBitLength();
		bits = new boolean[bitCount];
	}
	
	public BigInteger decodeSymbol() throws IOException {
		for (int i = 0; i < bitCount; i++) {
			int probability = binaryProbabilityMapping.getPartialProbability(bits, i);
			bits[i] = dumbMQDecoder.decodeBitProb(probability);
		}

		return binaryProbabilityMapping.bitsToSymbol(bits);
	}

	@Override
	public void init(int z) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int decodeSample(int t, int z) throws IOException {
		int decodedSample = decodeSymbol().intValue();
		return decodedSample;
	}

	@Override
	public void update(int sample, int t, int z) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void terminate() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateBitCount() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateBits() {
		// TODO Auto-generated method stub
		
	}
}
