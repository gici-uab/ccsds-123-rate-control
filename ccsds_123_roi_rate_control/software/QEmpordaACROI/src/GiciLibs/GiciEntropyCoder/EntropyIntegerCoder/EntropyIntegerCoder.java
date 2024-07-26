/*
 * GiciLibs - EntropyIntegerCoder an implementation of MHDC Recommendation for Image Data Compression
 * Copyright (C) 2011  Group on Interactive Coding of Images (GICI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Group on Interactive Coding of Images (GICI)
 * Department of Information and Communication Engineering
 * Autonomous University of Barcelona
 * 08193 - Bellaterra - Cerdanyola del Valles (Barcelona)
 * Spain
 *
 * http://gici.uab.es
 * http://sourceforge.net/projects/emporda
 * gici-info@deic.uab.es
 */
package GiciEntropyCoder.EntropyIntegerCoder;

import java.io.IOException;

import GiciEntropyCoder.ArithmeticCoder.ProbabilityTable;
import GiciEntropyCoder.Interface.EntropyCoder;
import GiciException.ParameterException;
import GiciMath.IntegerMath;
import GiciStream.BitOutputStream;
import GiciStream.ByteStream;

/**
 * Coder class of the EntropyIntegerCoder. EntropyIntegerCoder is a coder of
 * the Recommended Standard MHDC-123 White Book.
 * <p>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public class EntropyIntegerCoder implements EntropyCoder {

	private BitOutputStream bos = null;

	/**
	 * the accumulator array, one element of the array for one band
	 */
	private int[] accumulator;

	/**
	 * the counter array, one element of the array for one band
	 */
	private int[] counter;

	/**
	 * number of bits written to the stream
	 */
	private int numBitsWritten;

	/**
	 * number of bits written to the stream for a line
	 */
	private int numBitsWrittenLine;
	
	/**
	 * all the options needed for the encoding process
	 */
	private final int initialCountExponent;
	private final int accumulatorInitConstant;
	private final int rescalingCounterSize;

	private final int dynamicRange;
	private final int unaryLengthLimit;

	/**
	 * array with the initial accumulator values
	 */
	private int[] accumulatorTable = null;

	/**
	 * indicates whether to display information
	 */
	private int Nbins = 400;
	private int Hist[] = new int[Nbins];
	/**
	 * histogram array with Nbins bins
	 */
	
	private final int bands;
	private final int width;
	private final int height;
	
	/**
	 * Constructor.
	 *
	 * @param bos the bit output stream
	 * @param initialCountExponent Initial Count Exponent
	 * @param accumulatorInitConstant Accumulator Initialization Constant
	 * @param rescalingCounterSize Rescaling Counter Size
	 * @param dynamicRange DynamicRange
	 * @param unaryLengthLimit Unary Length Limit
	 * @param bandSequential Sets the sample order to Band Sequential. The alternativew is Band Interleaved.
	 * @param interleavingDepth The interleaving depth for the Band Interleaved order (up to the number of bands).
	 * @param accumulatorTable the table with the initial values of the accumulator
	 * @param verbose Whether to print progress messages.
	 * @param bands is the number of bands of the image.
	 */
	public EntropyIntegerCoder(
		BitOutputStream bos,
		int initialCountExponent,
		int accumulatorInitConstant,
		int rescalingCounterSize,
		int dynamicRange,
		int unaryLengthLimit,
		int[] accumulatorTable,
		int bands,
		int height,
		int width)
	{
		this.bos = bos;

		this.initialCountExponent = initialCountExponent;
		this.accumulatorInitConstant = accumulatorInitConstant;
		this.rescalingCounterSize = rescalingCounterSize;
		this.dynamicRange = dynamicRange;
		this.unaryLengthLimit = unaryLengthLimit;

		this.accumulatorTable = accumulatorTable;
		
		this.bands = bands;
		this.height = height;
		this.width = width;
		
		numBitsWritten = 0;
		numBitsWrittenLine = 0;
		accumulator = new int[bands];
		counter = new int[bands];
		
        for (int bin = 0; bin < Nbins; bin++)	Hist[bin] = 0;
	}
	

	public void copy (EntropyIntegerCoder another, BitOutputStream mbos){
		
		this.bos = mbos;
		this.numBitsWritten = another.numBitsWritten;
		this.numBitsWrittenLine = another.numBitsWrittenLine;
		
		for (int z = 0; z < bands; z++){
			this.accumulator[z] = another.accumulator[z];
			this.counter[z] = another.counter[z];
		}
		
	}
	

	/**
	 * Initializes the statistics
	 *
	 * @param z the band number
	 */
	
	public void init(int z) {
		int accInit = 0;

		counter[z] = 1 << initialCountExponent;
		if (accumulatorInitConstant < 15) {
			accInit = accumulatorInitConstant;
		} else if(accumulatorInitConstant == 15) {
			accInit = accumulatorTable[z];
		} else {
			try {
				throw new ParameterException("PARAMS ERROR: ACCUMULATOR_INITIALIZATION_CONSTANT has been " + 
							"set to an invalid value: " + accumulatorInitConstant);
			} catch (ParameterException e) {
				e.printStackTrace();
			}
		}
		accumulator[z] = (3 * (1 << accInit + 6) - 49) * counter[z];
		accumulator[z] >>= 7;
	}

	/**
	 * Updates the statistics for every sample
	 *
	 * @param sample the sample that has been coded
	 * @param t the number of the sample in the encoding sequence
	 * @param z the band of the sample
	 */
	public void update(int sample, int t, int z) {

		int limit = (1 << rescalingCounterSize) - 1;

		if (t > 0) {
			if (counter[z] < limit) {
				accumulator[z] += sample;
				counter[z]++;

			} else {
				accumulator[z] = accumulator[z] + sample + 1 >> 1;
				counter[z] = counter[z] + 1 >> 1;
			}
		}
	}

	/**
	 * Encodes a sample
	 *
	 * @param sample the sample that is going to be encoded
	 * @param t the sample number of the sequence
	 * @param z the band number
	 * @throws IOException if can not write information to the file
	 */
	public void codeSample(int sample, int t, int z) throws IOException {

		int k_z = 0;
		int u_z = 0;

		if (t == 0) {
			if(sample > 1 << dynamicRange) {
				try {
					throw new ParameterException("PARAMS ERROR: dynamic range too small for this image: " + sample);
				} catch (ParameterException e) {
					e.printStackTrace();
					System.exit(-1);
				}
			}
			
			bos.write(dynamicRange, sample);
			numBitsWritten += dynamicRange;
			numBitsWrittenLine += dynamicRange;

		} else {

			k_z = IntegerMath.log2((accumulator[z] + (49*counter[z] >> 7))/ counter[z]);
			k_z = (k_z < 0) ? 0 : k_z;
			k_z = (k_z > dynamicRange-2) ? dynamicRange-2 : k_z;
			u_z = sample >> k_z;
			u_z = (u_z < 0) ? 0: u_z;

			if (u_z < unaryLengthLimit) {
				bos.write(u_z, 0);
				bos.write(1, 1);
				bos.write(k_z, sample);
				numBitsWritten += k_z + 1 + u_z;
				numBitsWrittenLine += k_z + 1 + u_z;

			} else {
				bos.write(unaryLengthLimit, 0);
				bos.write(dynamicRange, sample);
				numBitsWritten += dynamicRange + unaryLengthLimit;
				numBitsWrittenLine += dynamicRange + unaryLengthLimit;
			}
		}
	}

	/**
	 * Ends the encoding process
	 *
	 * @param verbose if output information of the encoding process must be shown
	 * or not
	 * @throws IOException if can not write information to the file
	 */
	public void terminate() throws IOException {
		bos.flush();
	}
	
	/**
	 * Ends the encoding process
	 *
	 * @param verbose if output information of the encoding process must be shown
	 * or not
	 * @throws IOException if can not write information to the file
	 */
	public void terminate(boolean verbose) throws IOException {
		bos.flush();
	}
	public double getRate(boolean verbose){
		System.out.println("numBitsWritten: "+numBitsWritten);
		System.out.println("bands*height*width: "+bands+" "+height+" "+width);
		double bpppb=numBitsWritten/(float)(bands*height*width);
		if (verbose){
			System.out.println("\n bpppb: "+bpppb);
		}
		return bpppb;
	}
	public double getRate(){
		double bpppb=numBitsWritten/(float)(bands*height*width);
		return bpppb;
	}
	public void updateHistogram (int value){
		if (value> (Nbins-1)) Hist[Nbins-1]++;
		else Hist[value]++;
	}
	public float getEntropy(int samples){
		
		float entropy = 0;
	
		for (int i = 0; i < Nbins; i++){
			double p=Hist[i]/(float)samples;
			if (p!=Double.NaN&&	p!=0) entropy+=(double)p*log2(1/p);	
		}		
		return entropy;
	}
	
	public void resetHistogram(){
		for (int i = 0; i < Nbins; i++){
			Hist[i] = 0;
		}
	}
	
	public long getNumbitsWrittenLine(){
		return numBitsWrittenLine;
	}
	

	public void resetNumBitsWrittenLine(){	
		numBitsWrittenLine = 0;
	}
	
	public long getNumBitsWritten(){
		return numBitsWritten;
	}
	public void resetNumBitsWritten(){	
		numBitsWritten = 0;
	}
	private double log2(double num)
	{
		return (Math.log(num)/Math.log(2));
	}

	public void updateProbabilityTable(ProbabilityTable newProbabilityTable) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		return 0;
	}
	

}
