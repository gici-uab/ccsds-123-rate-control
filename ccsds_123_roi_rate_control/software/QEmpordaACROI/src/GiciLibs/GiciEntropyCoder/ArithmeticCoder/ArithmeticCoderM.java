/**
 * Copyright (C) 2013 - Francesc Auli-Llinas
 *
 * This program is distributed under the BOI License.
 * This program is distributed in the hope that it will be useful, but without any
 * warranty; without even the implied warranty of merchantability or fitness for a particular purpose.
 * You should have received a copy of the BOI License along with this program. If not, see
 * <http://www.deic.uab.cat/~francesc/software/license/>.
 */
package GiciEntropyCoder.ArithmeticCoder;

import java.io.IOException;

import GiciEntropyCoder.Interface.EntropyCoder;
import GiciStream.ByteStream;


/**
 * This class implements the M coder defined in the HEVC standard. This class can perform both the
 * operations of the encoder and the decoder. The sourcecode is translated from C++ to Java using
 * the reference software of the HEVC project provided
 * in https://hevc.hhi.fraunhofer.de/svn/svn_HEVCSoftware/trunk (revision 3517).<br>
 *
 * Usage: once the object is created, the functions to code/decode symbols are used to code the
 * message. Instead of destroying the object and creating another one to encode a new message, it
 * is more computationally efficient to reuse the same object. When encoding, the same object can be
 * reused by calling the functions <code>terminate</code>, get the stream wherever is needed, and
 * call <code>changeStream</code> and <code>restartEncoding</code> in this order. To reuse the
 * decoder, call the functions <code>changeStream</code> and <code>restartDecoding</code> in this order.<br>
 *
 * Multithreading support: the object must be created and manipulated by a single thread. There
 * can be many objects of this class running simultaneously as long as a single thread manipulates each object.<br>
 *
 * @author Francesc Auli-Llinas
 * @version 1.0
 */
public final class ArithmeticCoderM implements EntropyCoder{

	//Variables needed to encode
	private long m_uiLow;
	private int m_bitsLeft;
	private int m_numBufferedBytes;
	private int m_bufferedByte;

	//Variables needed to decode
	private long m_uiValue;
	private int m_bitsNeeded;

	//Variables needed both to encode and to decode
	private int L;
	private ByteStream stream;
	private long m_uiRange;
	private int[] m_ucState = null;
	final static private short[][] sm_aucLPSTable = {
		{ 128, 176, 208, 240},
		{ 128, 167, 197, 227},
		{ 128, 158, 187, 216},
		{ 123, 150, 178, 205},
		{ 116, 142, 169, 195},
		{ 111, 135, 160, 185},
		{ 105, 128, 152, 175},
		{ 100, 122, 144, 166},
		{  95, 116, 137, 158},
		{  90, 110, 130, 150},
		{  85, 104, 123, 142},
		{  81,  99, 117, 135},
		{  77,  94, 111, 128},
		{  73,  89, 105, 122},
		{  69,  85, 100, 116},
		{  66,  80,  95, 110},
		{  62,  76,  90, 104},
		{  59,  72,  86,  99},
		{  56,  69,  81,  94},
		{  53,  65,  77,  89},
		{  51,  62,  73,  85},
		{  48,  59,  69,  80},
		{  46,  56,  66,  76},
		{  43,  53,  63,  72},
		{  41,  50,  59,  69},
		{  39,  48,  56,  65},
		{  37,  45,  54,  62},
		{  35,  43,  51,  59},
		{  33,  41,  48,  56},
		{  32,  39,  46,  53},
		{  30,  37,  43,  50},
		{  29,  35,  41,  48},
		{  27,  33,  39,  45},
		{  26,  31,  37,  43},
		{  24,  30,  35,  41},
		{  23,  28,  33,  39},
		{  22,  27,  32,  37},
		{  21,  26,  30,  35},
		{  20,  24,  29,  33},
		{  19,  23,  27,  31},
		{  18,  22,  26,  30},
		{  17,  21,  25,  28},
		{  16,  20,  23,  27},
		{  15,  19,  22,  25},
		{  14,  18,  21,  24},
		{  14,  17,  20,  23},
		{  13,  16,  19,  22},
		{  12,  15,  18,  21},
		{  12,  14,  17,  20},
		{  11,  14,  16,  19},
		{  11,  13,  15,  18},
		{  10,  12,  15,  17},
		{  10,  12,  14,  16},
		{   9,  11,  13,  15},
		{   9,  11,  12,  14},
		{   8,  10,  12,  14},
		{   8,   9,  11,  13},
		{   7,   9,  11,  12},
		{   7,   9,  10,  12},
		{   7,   8,  10,  11},
		{   6,   8,   9,  11},
		{   6,   7,   9,  10},
		{   6,   7,   8,   9},
		{   2,   2,   2,   2}
	};
	final static private byte[] sm_aucRenormTable = {
		6,  5,  4,  4, 3,  3,  3,  3, 2,  2,  2,  2, 2,  2,  2,  2, 1,  1,  1,  1,
		1,  1,  1,  1, 1,  1,  1,  1, 1,  1,  1,  1};
	final static private byte[] m_aucNextStateMPS = {
		2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
		24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43,
		44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63,
		64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83,
		84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102,
		103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118,
		119, 120, 121, 122, 123, 124, 125, 124, 125, 126, 127};
	final static private byte[] m_aucNextStateLPS = {
		1, 0, 0, 1, 2, 3, 4, 5, 4, 5, 8, 9, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
		18, 19, 22, 23, 22, 23, 24, 25, 26, 27, 26, 27, 30, 31, 30, 31, 32, 33, 32, 33, 36,
		37, 36, 37, 38, 39, 38, 39, 42, 43, 42, 43, 44, 45, 44, 45, 46, 47, 48, 49, 48, 49,
		50, 51, 52, 53, 52, 53, 54, 55, 54, 55, 56, 57, 58, 59, 58, 59, 60, 61, 60, 61, 60,
		61, 62, 63, 64, 65, 64, 65, 66, 67, 66, 67, 66, 67, 68, 69, 68, 69, 70, 71, 70, 71,
		70, 71, 72, 73, 72, 73, 72, 73, 74, 75, 74, 75, 74, 75, 76, 77, 76, 77, 126, 127};


	/**
	 * Constructor of the class. Initializes required variables.
	 *
	 * @param numContexts number of contexts that will be used
	 */
	public ArithmeticCoderM(int numContexts){
		assert(numContexts > 0);
		m_ucState = new int[numContexts];
	}

	/**
	 * Restarts the coder leaving it ready to encode.
	 */
	public void restartEncoding(){
		m_uiLow = 0;
		m_uiRange = 510;
		m_bitsLeft = 23;
		m_numBufferedBytes = 0;
		m_bufferedByte = 0xff;
		for(int c = 0; c < m_ucState.length; c++){
			m_ucState[c] = 0;
		}
		L = 0;
	}

	/**
	 * Restarts the coder leaving it ready to decode.
	 *
	 * @throws Exception when some problem reading the stream occurs
	 */
	public void restartDecoding() throws Exception{
		m_uiRange = 510;
		m_bitsNeeded = -8;
		L = 0;
		int Tr = 0x00;
		if(L < stream.getLength()){
			Tr = (0x000000FF & (int) stream.getByte(L));
			L++;
		}
		m_uiValue = Tr << 8;
		Tr = 0x00;
		if(L < stream.getLength()){
			Tr = (0x000000FF & (int) stream.getByte(L));
			L++;
		}
		m_uiValue += Tr;
		for(int c = 0; c < m_ucState.length; c++){
			m_ucState[c] = 0;
		}
	}

	/**
	 * Encodes a bit using equiprobable probabilities.
	 *
	 * @param binValue the bit to encode
	 */
	public void encodeBit(boolean binValue){
		m_uiLow <<= 1;
		if(binValue){
			m_uiLow += m_uiRange;
		}
		m_bitsLeft--;
		testAndWriteOut();
	}

	/**
	 * Decodes a bit using equiprobable probabilities.
	 *
	 * @return the decoded bit
	 * @throws Exception when some problem reading the stream occurs
	 */
	public boolean decodeBit() throws Exception{
		boolean ruiBin;
		m_uiValue += m_uiValue;
		if(++m_bitsNeeded >= 0){
			m_bitsNeeded = -8;
			int Tr = 0x00;
			if(L < stream.getLength()){
				Tr = (0x000000FF & (int) stream.getByte(L));
				L++;
			}
			m_uiValue += Tr;
		}
		ruiBin = false;
		long scaledRange = m_uiRange << 7;
		if(m_uiValue >= scaledRange){
			ruiBin = true;
			m_uiValue -= scaledRange;
		}
		return(ruiBin);
	}

	/**
	 * Encodes a bit employing context-adaptive mechanisms.
	 *
	 * @param binValue the bit to encode
	 * @param context the context employed to encode the bit
	 */
	public void encodeBitContext(boolean binValue, int context){
		int state = m_ucState[context] >> 1;
		int uiLPS = sm_aucLPSTable[state][(int) ((m_uiRange >> 6) & 3)];
		m_uiRange -= uiLPS;
		boolean MPS = (m_ucState[context] & 1) == 1;

		if(binValue != MPS){
			int numBits = sm_aucRenormTable[uiLPS >> 3];
			m_uiLow = (m_uiLow + m_uiRange) << numBits;
			m_uiRange = uiLPS << numBits;
			m_ucState[context] = m_aucNextStateLPS[m_ucState[context]];
			m_bitsLeft -= numBits;
		}else{
			m_ucState[context] = m_aucNextStateMPS[m_ucState[context]];
			if(m_uiRange >= 256){
			  return;
			}
			m_uiLow <<= 1;
			m_uiRange <<= 1;
			m_bitsLeft--;
		}
		testAndWriteOut();
	}

	/**
	 * Decodes a bit employing context-adaptive mechanisms.
	 *
	 * @param context the context employed to decode the bit
	 * @return the decoded bit
	 * @throws Exception when some problem reading the stream occurs
	 */
	public boolean decodeBitContext(int context) throws Exception{
		boolean ruiBin;
		int state = m_ucState[context] >> 1;
		int uiLPS = sm_aucLPSTable[state][(int) ((m_uiRange >> 6) - 4)];
		m_uiRange -= uiLPS;
		long scaledRange = m_uiRange << 7;
		boolean MPS = (m_ucState[context] & 1) == 1;

		if(m_uiValue < scaledRange){
			ruiBin = MPS;
			m_ucState[context] = m_aucNextStateMPS[m_ucState[context]];
			if(scaledRange >= (256 << 7)){
				return(ruiBin);
			}
			m_uiRange = scaledRange >> 6;
			m_uiValue += m_uiValue;
			if(++m_bitsNeeded == 0){
				m_bitsNeeded = -8;
				int Tr = 0x00;
				if(L < stream.getLength()){
					Tr = (0x000000FF & (int) stream.getByte(L));
					L++;
				}
				m_uiValue += Tr;
			}
		}else{
			int numBits = sm_aucRenormTable[uiLPS >> 3];
			m_uiValue = (m_uiValue - scaledRange) << numBits;
			m_uiRange = uiLPS << numBits;
			ruiBin = !MPS;
			m_ucState[context] = m_aucNextStateLPS[m_ucState[context]];
			m_bitsNeeded += numBits;
			if(m_bitsNeeded >= 0){
				int Tr = 0x00;
				if(L < stream.getLength()){
					Tr = (0x000000FF & (int) stream.getByte(L));
					L++;
				}
				m_uiValue += Tr << m_bitsNeeded;
				m_bitsNeeded -= 8;
			}
		}
		return(ruiBin);
	}

	/**
	 * Encodes a bit employing the probability of symbol 0.
	 *
	 * @param binValue the bit to encode
	 * @param prob0 probability of the symbol 0 computed through the function {@link #prob0ToM(float)}
	 */
	public void encodeBitProb(boolean binValue, int prob0){
		boolean MPS = prob0 < 0;
		int uiLPS = sm_aucLPSTable[Math.abs(prob0)][(int) ((m_uiRange >> 6) & 3)];
		m_uiRange -= uiLPS;

		if(binValue != MPS){
			int numBits = sm_aucRenormTable[uiLPS >> 3];
			m_uiLow = (m_uiLow + m_uiRange) << numBits;
			m_uiRange = uiLPS << numBits;
			m_bitsLeft -= numBits;
		}else{
			if(m_uiRange >= 256){
			  return;
			}
			m_uiLow <<= 1;
			m_uiRange <<= 1;
			m_bitsLeft--;
		}
		testAndWriteOut();
	}

	/**
	 * Decodes a bit employing the probability of symbol 0.
	 *
	 * @param prob0 probability of the symbol 0 computed through the function {@link #prob0ToM(float)}
	 * @return the decoded bit
	 * @throws Exception when some problem reading the stream occurs
	 */
	public boolean decodeBitProb(int prob0) throws Exception{
		boolean MPS = prob0 < 0;
		boolean ruiBin;
		int uiLPS = sm_aucLPSTable[Math.abs(prob0)][(int) ((m_uiRange >> 6) - 4)];
		m_uiRange -= uiLPS;
		long scaledRange = m_uiRange << 7;

		if(m_uiValue < scaledRange){
			ruiBin = MPS;
			if(scaledRange >= (256 << 7)){
				return(ruiBin);
			}
			m_uiRange = scaledRange >> 6;
			m_uiValue += m_uiValue;
			if(++m_bitsNeeded == 0){
				m_bitsNeeded = -8;
				int Tr = 0x00;
				if(L < stream.getLength()){
					Tr = (0x000000FF & (int) stream.getByte(L));
					L++;
				}
				m_uiValue += Tr;
			}
		}else{
			int numBits = sm_aucRenormTable[uiLPS >> 3];
			m_uiValue = (m_uiValue - scaledRange) << numBits;
			m_uiRange = uiLPS << numBits;
			ruiBin = !MPS;
			m_bitsNeeded += numBits;
			if(m_bitsNeeded >= 0){
				int Tr = 0x00;
				if(L < stream.getLength()){
					Tr = (0x000000FF & (int) stream.getByte(L));
					L++;
				}
				m_uiValue += Tr << m_bitsNeeded;
				m_bitsNeeded -= 8;
			}
		}
		return(ruiBin);
	}

	/**
	 * Transforms the probability of the symbol 0 (or false) in the range [0:1] into the integer
	 * required in the M coder to represent that probability.
	 *
	 * @param prob0 in the range [0:1]
	 * @return integer that can be feed to the M coder
	 */
	public static int prob0ToM(float prob0){
		boolean MPS;
		if(prob0 >= 0.5f){
			MPS = false;
		}else{
			MPS = true;
			prob0 = 1 - prob0;
		}
		//determined through reverse engineering (it is the context with closest probability to prob0)
		float base = 12f;
		int prob0M = (int) (((Math.pow(base, (prob0 - 0.5f) * 2f) - 1f) / (base - 1f)) * 61f) + 2;
		if(MPS){
			prob0M = -prob0M;
		}
		return(prob0M);
	}

	/**
	 * Does nothing. Added for compatibility purposes.
	 */
	public void reset(){}

	/**
	 * Swaps the current stream. When encoding, before calling this function the stream should be
	 * terminated calling the <code>terminate</code> function, and after calling this function the
	 * function <code>restartEncoding</code> must be called. When decoding, after calling this
	 * function the function <code>restartDecoding</code> must be called.
	 *
	 * @param stream the new ByteStream
	 */
	public void changeStream(ByteStream stream){
		if(stream == null){
			stream = new ByteStream();
		}
		this.stream = stream;
	}

	/**
	 * Terminates the encoding process dispatching all bits in the register to the stream.
	 */
	public void terminate(){
		encodeBinTrm(1);
		if((m_uiLow >> (32 - m_bitsLeft)) != 0){
			stream.putByte((byte) (m_bufferedByte + 1));
			L++;
			while(m_numBufferedBytes > 1){
				stream.putByte((byte) 0x00);
				L++;
				m_numBufferedBytes--;
			}
			m_uiLow -= 1 << (32 - m_bitsLeft);
		}else{
			if(m_numBufferedBytes > 0){
				stream.putByte((byte) m_bufferedByte);
				L++;
			}
			while(m_numBufferedBytes > 1){
				stream.putByte((byte) 0xFF);
				L++;
				m_numBufferedBytes--;
			}
		}
		int bitsLeft = 24 - m_bitsLeft;
		while(bitsLeft > 0){
			stream.putByte((byte) (m_uiLow >> bitsLeft));
			L++;
			bitsLeft -= 8;
		}
	}

	/**
	 * Gets the number of bytes read or written to the stream associated to the coder.
	 *
	 * @return the number of bytes
	 */
	public int getReadBytes(){
		return(L);
	}

	/**
	 * Computes the number of bytes belonging to the currently encoded data needed to flush the
	 * internal registers (for encoding purposes). This function is useful to determine potential
	 * truncation points of the stream.
	 *
	 * @return number of bytes
	 */
	public int remainingBytes(){
		return(4);
	}

	/**
	 * Tests whether the bits in the register have to be dispatched to the stream.
	 */
	private void testAndWriteOut(){
		if(m_bitsLeft < 12){
			writeOut();
		}
	}

	/**
	 * Writes some bits of the internal register to the stream.
	 */
	private void writeOut(){
		int leadByte = (int) (m_uiLow >> (24 - m_bitsLeft));
		m_bitsLeft += 8;
		m_uiLow &= (0xffffffffl) >> m_bitsLeft;

		if(leadByte == 0xff){
			m_numBufferedBytes++;
		}else{
			if(m_numBufferedBytes > 0){
				int carry = leadByte >> 8;
				int byteB = m_bufferedByte + carry;
				m_bufferedByte = leadByte & 0xff;
				stream.putByte((byte) byteB);
				L++;

				byteB = (0xff + carry) & 0xff;
				while(m_numBufferedBytes > 1){
					stream.putByte((byte) byteB);
					L++;
					m_numBufferedBytes--;
				}
			}else{
				m_numBufferedBytes = 1;
				m_bufferedByte = leadByte;
			}
		}
	}

	/**
	 * Encodes a terminating bit (needed when terminating the encoding process).
	 *
	 * @param binValue the bit encoded
	 */
	public void encodeBinTrm(int binValue){
		m_uiRange -= 2;
		if(binValue != 0){
			m_uiLow  += m_uiRange;
			m_uiLow <<= 7;
			m_uiRange = 2 << 7;
			m_bitsLeft -= 7;
		}else if(m_uiRange >= 256){
			return;
		}else{
			m_uiLow   <<= 1;
			m_uiRange <<= 1;
			m_bitsLeft--;
		}
		testAndWriteOut();
	}

	@Override
	public void setProbabilityTable(ProbabilityTable pt) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void terminate(boolean verbose) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getRate(boolean verbose) {
		// TODO Auto-generated method stub
		return 0;
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
	public void encodeInteger(int num, int numBits) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int decodeInteger(int numBits) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ByteStream getByteStream() {
		// TODO Auto-generated method stub
		return null;
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