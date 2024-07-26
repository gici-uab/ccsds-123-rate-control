/**
 * Copyright (C) 2013 - Francesc Auli-Llinas
 *
 * This program is distributed under the BOI License.
 * This program is distributed in the hope that it will be useful, but without any warranty; without even the implied warranty of merchantability or fitness for a particular purpose.
 * You should have received a copy of the BOI License along with this program. If not, see <http://www.deic.uab.cat/~francesc/software/license/>.
 */
package GiciEntropyCoder.ArithmeticCoder;

import java.io.IOException;

import GiciEntropyCoder.Interface.EntropyCoder;
import GiciStream.ByteStream;


/**
 * This class implements the arithmetic coder MQ defined in the JPEG2000 standard. This class can perform both the operations of the encoder and the decoder.<br>
 *
 * Usage: once the object is created, the functions to code/decode symbols are used to code the message. Instead of destroying the object and creating another one to encode a new message, it is more computationally efficient to reuse the same object. When encoding, the same object can be reused by calling the functions <code>terminate</code>, get the stream wherever is needed, <code>changeStream</code>, <code>restartEncoding</code> and <code>reset</code> in this order. To reuse the decoder, the functions <code>changeStream</code>, <code>restartDecoding</code>, and <code>reset</code> have to be called in this order.<br>
 *
 * Multithreading support: the object must be created and manipulated by a single thread. There can be many objects of this class running simultaneously as long as a single thread manipulates each object.<br>
 *
 * @author Francesc Auli-Llinas
 * @version 1.3
 */
public final class ArithmeticCoderMQ implements EntropyCoder{

	/**
	 * ByteStream employed by the coder to write/read the output/input bytes.
	 * <p>
	 * The stream may contain zero bytes.
	 */
	private ByteStream stream;

	/**
	 * Interval range.
	 * <p>
	 * From right to left: 8 register bits, 3 spacer bits, 8 partial code bits, and 1 carry bit.
	 */
	private int A;

	/**
	 * Lower down interval.
	 * <p>
	 * From right to left: 8 register bits, 3 spacer bits, 8 partial code bits, and 1 carry bit.
	 */
	private int C;

	/**
	 * Number of bits to transfer.
	 * <p>
	 * It is set to 8 except when carry situations occur, in which is set to 7.
	 */
	private int t;

	/**
	 * Byte to flush out/read.
	 * <p>
	 * Flushed byte to the stream.
	 */
	private int Tr;

	/**
	 * Current byte read/write.
	 * <p>
	 * Current position in the stream.
	 */
	private int L;

	/**
	 * Context state.
	 * <p>
	 * The indices are [context][0- current state corresponding to one of STATE_TRANSITIONS, 1- most probable symbol].
	 */
	private int[][] contextState = null;

	/**
	 * Number of contexts.
	 * <p>
	 * This includes all contexts employed by the bitplane coding engine.
	 */
	private static final int NUM_CONTEXTS = 19;

	/**
	 * Initialization of the contexts.
	 * <p>
	 * The indices of the array are [context][0- initial state, 1- initial most probable symbol].
	 */
	private static final int[][] INITIAL_CONTEXT_STATES = {
		{4,  0}, //context  0 (SPP/CP)
		{0,  0}, //context  1 (SPP/CP)
		{0,  0}, //context  2 (SPP/CP)
		{0,  0}, //context  3 (SPP/CP)
		{0,  0}, //context  4 (SPP/CP)
		{0,  0}, //context  5 (SPP/CP)
		{0,  0}, //context  6 (SPP/CP)
		{0,  0}, //context  7 (SPP/CP)
		{0,  0}, //context  8 (SPP/CP)
		{3,  0}, //context  9 (CP run mode)
		{0,  0}, //context 10 (SIGN)
		{0,  0}, //context 11 (SIGN)
		{0,  0}, //context 12 (SIGN)
		{0,  0}, //context 13 (SIGN)
		{0,  0}, //context 14 (SIGN)
		{0,  0}, //context 15 (MRP)
		{0,  0}, //context 16 (MRP)
		{0,  0}, //context 17 (MRP)
		{46, 0}  //context 18 (CP run mode)
	};

	/**
	 * Transition between the state (i.e., probability) of the context.
	 * <p>
	 * The array indices are [currentState][0- transition to the next state when coding the most probable symbol, 1- transition when coding the least probable symbol, 2- when 1 swap most probable symbol, 3- coded probability]. The real probability can be computed as the coded probability 0xXXXX / (2^16 * \alpha), where \alpha is 0.708.
	 */
	private static final int[][] STATE_TRANSITIONS = {
		{ 1,  1, 1, 0x5601}, //state  0
		{ 2,  6, 0, 0x3401}, //state  1
		{ 3,  9, 0, 0x1801}, //state  2
		{ 4, 12, 0, 0x0AC1}, //state  3
		{ 5, 29, 0, 0x0521}, //state  4
		{38, 33, 0, 0x0221}, //state  5
		{ 7,  6, 1, 0x5601}, //state  6
		{ 8, 14, 0, 0x5401}, //state  7
		{ 9, 14, 0, 0x4801}, //state  8
		{10, 14, 0, 0x3801}, //state  9
		{11, 17, 0, 0x3001}, //state 10
		{12, 18, 0, 0x2401}, //state 11
		{13, 20, 0, 0x1C01}, //state 12
		{29, 21, 0, 0x1601}, //state 13
		{15, 14, 1, 0x5601}, //state 14
		{16, 14, 0, 0x5401}, //state 15
		{17, 15, 0, 0x5101}, //state 16
		{18, 16, 0, 0x4801}, //state 17
		{19, 17, 0, 0x3801}, //state 18
		{20, 18, 0, 0x3401}, //state 19
		{21, 19, 0, 0x3001}, //state 20
		{22, 19, 0, 0x2801}, //state 21
		{23, 20, 0, 0x2401}, //state 22
		{24, 21, 0, 0x2201}, //state 23
		{25, 22, 0, 0x1C01}, //state 24
		{26, 23, 0, 0x1801}, //state 25
		{27, 24, 0, 0x1601}, //state 26
		{28, 25, 0, 0x1401}, //state 27
		{29, 26, 0, 0x1201}, //state 28
		{30, 27, 0, 0x1101}, //state 29
		{31, 28, 0, 0x0AC1}, //state 30
		{32, 29, 0, 0x09C1}, //state 31
		{33, 30, 0, 0x08A1}, //state 32
		{34, 31, 0, 0x0521}, //state 33
		{35, 32, 0, 0x0441}, //state 34
		{36, 33, 0, 0x02A1}, //state 35
		{37, 34, 0, 0x0221}, //state 36
		{38, 35, 0, 0x0141}, //state 37
		{39, 36, 0, 0x0111}, //state 38
		{40, 37, 0, 0x0085}, //state 39
		{41, 38, 0, 0x0049}, //state 40
		{42, 39, 0, 0x0025}, //state 41
		{43, 40, 0, 0x0015}, //state 42
		{44, 41, 0, 0x0009}, //state 43
		{45, 42, 0, 0x0005}, //state 44
		{45, 43, 0, 0x0001}, //state 45
		{46, 46, 0, 0x5601}  //state 46
	};


	/**
	 * Initializes internal registers. Before using the coder, a stream has to be set through <code>changeStream</code>.
	 */
	public ArithmeticCoderMQ(){
		contextState = new int[NUM_CONTEXTS][2];
		reset();
		restartEncoding();
	}

	/**
	 * Codes a bit employing the context 18 (equivalent probabilities).
	 *
	 * @param bit input
	 */
	@Override public void encodeBit(boolean bit){
		encodeBitContext(bit, (byte) 18);
	}

	/**
	 * Decodes a bit employing the context 18 (equivalent probabilities).
	 *
	 * @return output bit
	 * @throws Exception when some problem manipulating the stream occurs
	 */
	@Override public boolean decodeBit() throws Exception{
		return(decodeBitContext((byte) 18));
	}

	/**
	 * Codes a bit. This function has been implemented using the recommendations given in the JPEG2000 book by Taubman and Marcellin (p. 646).
	 *
	 * @param bit input
	 * @param context context of the symbol
	 */
	@Override public void encodeBitContext(boolean bit, int context){
		int x = bit ? 1 : 0;
		int s = contextState[context][1];
		int p = STATE_TRANSITIONS[contextState[context][0]][3];

		A -= p;
		if(x == s){ //Codes the most probable symbol
			if(A >= (1 << 15)){
				C += p;
			}else{
				if(A < p){
					A = p;
				}else{
					C += p;
				}
				contextState[context][0] = STATE_TRANSITIONS[contextState[context][0]][0];
				while(A < (1 << 15)){
					A <<= 1;
					C <<= 1;
					t--;
					if(t == 0){
						transferByte();
					}
				}
			}
		}else{ //Codes the least probable symbol
			if(A < p){
				C += p;
			}else{
				A = p;
			}
			if(STATE_TRANSITIONS[contextState[context][0]][2] == 1){
				contextState[context][1] = contextState[context][1] == 0 ? 1: 0;
			}
			contextState[context][0] = STATE_TRANSITIONS[contextState[context][0]][1];
			while(A < (1 << 15)){
				A <<= 1;
				C <<= 1;
				t--;
				if(t == 0){
					transferByte();
				}
			}
		}
	}

	/**
	 * Decodes a bit. This function has been implemented using the recommendations given in the JPEG2000 book by Taubman and Marcellin (p. 646).
	 *
	 * @param context context of the symbol
	 * @return output bit
	 * @throws Exception when some problem manipulating the stream occurs
	 */
	@Override public boolean decodeBitContext(int context) throws Exception{
		int p = STATE_TRANSITIONS[contextState[context][0]][3];
		int s = contextState[context][1];
		int x = s;

		A -= p;
		if((C & 0x00FFFF00) >= (p << 8)){
			C = ((C & ~0xFFFFFF00) | ((C & 0x00FFFF00) - (p << 8)));
			if(A < (1 << 15)){
				if(A < p){
					x = 1 - s;
					if(STATE_TRANSITIONS[contextState[context][0]][2] == 1){
						//Swaps most probable symbol if necessary
						contextState[context][1] = contextState[context][1] == 0 ? 1: 0;
					}
					contextState[context][0] = STATE_TRANSITIONS[contextState[context][0]][1];
				}else{
					contextState[context][0] = STATE_TRANSITIONS[contextState[context][0]][0];
				}
				while(A < (1 << 15)){
					if(t == 0){
						fillLSB();
					}
					A <<= 1;
					C <<= 1;
					t--;
				}
			}
		}else{
			if(A < p){
				contextState[context][0] = STATE_TRANSITIONS[contextState[context][0]][0];
			}else{
				x = 1 - s;
				if(STATE_TRANSITIONS[contextState[context][0]][2] == 1){
					//Swaps most probable symbol if necessary
					contextState[context][1] = contextState[context][1] == 0 ? 1: 0;
				}
				contextState[context][0] = STATE_TRANSITIONS[contextState[context][0]][1];
			}
			A = p;
			while(A < (1 << 15)){
				if(t == 0){
					fillLSB();
				}
				A <<= 1;
				C <<= 1;
				t--;
			}
		}
		return(x == 1);
	}

	/**
	 * Transfers a byte to the stream (for encoding purposes).
	 */
	private void transferByte(){
		if(Tr == 0xFF){ //Bit stuff
			stream.putByte((byte) Tr);
			L++;
			Tr = (C >>> 20); //Puts C_msbs to Tr
			C &= (~0xFFF00000); //Puts 0 to C_msbs
			t = 7;
		}else{
			if(C >= 0x08000000){
				//Propagates any carry bit from C into Tr
				Tr += 0x01;
				C &= (~0xF8000000); //Resets the carry bit
			}
			if(L >= 0){
				stream.putByte((byte) Tr);
			}
			L++;
			if(Tr == 0xFF){ //Bit stuff
				//Although it may not be a bit carry
				Tr = (C >>> 20); //Puts C_msbs to Tr
				C &= (~0xFFF00000); //Puts 0 to C_msbs
				t = 7;
			}else{
				Tr = (C >>> 19); //Puts C_partial to Tr
				C &= (~0xFFF80000); //Puts 0 to C_partial
				t = 8;
			}
		}
	}

	/**
	 * Fills the C register with a byte from the stream or with 0xFF when the end of the stream is reached (for decoding purposes).
	 *
	 * @throws Exception when some problem manipulating the stream occurs
	 */
	private void fillLSB() throws Exception{
		byte BL = 0;
		t = 8;
		if(L < stream.getLength()){
			BL = stream.getByte(L);
		}
		//Reached the end of the stream
		if((L == stream.getLength()) || ((Tr == 0xFF) && (BL > 0x8F))){
			C += 0xFF;
			if(L != stream.getLength()){
				throw new Exception("Read marker 0xFF in the stream.");
			}
		}else{
			if(Tr == 0xFF){
				t = 7;
			}
			Tr = (0x000000FF & (int) BL);
			L++;
			C += (Tr << (8 - t));
		}
	}

	/**
	 * Swaps the current stream. When encoding, before calling this function the stream should be terminated calling the <code>terminate</code> function, and after calling this function the functions <code>restartEncoding</code> and <code>reset</code> must be called. When decoding, after calling this function the functions <code>restartDecoding</code> and <code>reset</code> must be called.
	 *
	 * @param stream the new ByteStream
	 */
	@Override public void changeStream(ByteStream stream){
		if(stream == null){
			stream = new ByteStream();
		}
		this.stream = stream;
	}

	/**
	 * Resets the state of all contexts.
	 */
	@Override public void reset(){
		for(int c = 0; c < NUM_CONTEXTS; c++){
			contextState[c][0] = INITIAL_CONTEXT_STATES[c][0];
			contextState[c][1] = INITIAL_CONTEXT_STATES[c][1];
		}
	}

	/**
	 * Restarts the internal registers of the coder for encoding.
	 */
	@Override public void restartEncoding(){
		A  = 0x8000;
		C  = 0;
		t = 12;
		Tr = 0;
		L = -1;
	}

	/**
	 * Restarts the internal registers of the coder for decoding.
	 *
	 * @throws Exception when some problem manipulating the stream occurs
	 */
	@Override public void restartDecoding() throws Exception{
		Tr = 0;
		L  = 0;
		C  = 0;
		fillLSB();
		C <<= t;
		fillLSB();
		C <<= 7;
		t -= 7;
		A = 0x8000;
	}

	/**
	 * Computes the number of bytes belonging to the currently encoded data needed to flush the internal registers (for encoding purposes). This function is useful to determine potential truncation points of the stream.
	 *
	 * @return number of bytes
	 */
	@Override public int remainingBytes(){
		//Estimation taken from the JPEG2000 book by Taubman and Marcellin (p. 498).
		if(27 - t <= 22){
			return(4);
		}else{
			return(5);
		}
	}

	/**
	 * Terminates the current stream using the optimal termination (for encoding purposes).
	 */
	@Override public void terminate(){
		try {
			terminateOptimal();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Gets the number of bytes read or written to the stream associated to the coder.
	 *
	 * @return the number of bytes
	 */
	@Override public int getReadBytes(){
		return(L);
	}

	/**
	 * Terminates the current stream using the easy termination (for encoding purposes).
	 */
	public void terminateEasy(){
		int nBits = 27 - 15 - t;
		C <<= t;
		while(nBits > 0){
			transferByte();
			nBits -= t;
			C <<= t;
		}
		transferByte();
		if(t == 7){
			stream.removeByte();
		}
	}

	/**
	 * Terminates the current stream using the optimal termination (for encoding purposes). The procedure follows the recommendations given in the JPEG2000 book by Taubman and Marcellin (p. 498).
	 *
	 * @throws Exception when some problem manipulating the stream occurs
	 */
	public void terminateOptimal() throws Exception{
		int NZTr = Tr;
		int NZt = t;
		int NZC = C;
		int NZA = A;
		int NZL = L;

		int lengthEmptyTermination = (int) stream.getLength();
		terminateEasy();
		int necessaryBytes = minFlush(NZTr, NZt, NZC, NZA, NZL, lengthEmptyTermination);
		int lengthOptimalTermination = lengthEmptyTermination + necessaryBytes;

		if((lengthOptimalTermination >= 1) && ((stream.getByte(lengthOptimalTermination - 1) == 0xFF))){
			lengthOptimalTermination--;
		}
		boolean elimination;
		do{
			elimination = false;
			if((lengthOptimalTermination >= 2) && ((stream.getByte(lengthOptimalTermination - 2) == 0xFF) && (stream.getByte(lengthOptimalTermination - 1) == 0x7F))){
				lengthOptimalTermination -= 2;
				elimination = true;
			}
		}while(elimination);
		stream.removeBytes((int) stream.getLength() - lengthOptimalTermination);
	}

	/**
	 * Determines the minimum number of bytes needed to terminate the stream while assuring a complete recovering.
	 *
	 * @param NZTr Tr register for the normalization
	 * @param NZt t register for the normalization
	 * @param NZC C register for the normalization
	 * @param NZA A register for the normalization
	 * @param NZL number of flushed bytes
	 * @param lengthEmptyTermination length bytes used by the easy termination
	 * @return the number of bytes that should be flushed to terminate the ByteStream optimally
	 * @throws Exception when some problem manipulating the stream occurs
	 */
	private int minFlush(int NZTr, int NZt, int NZC, int NZA, int NZL, int lengthEmptyTermination) throws Exception{
		long Cr = ((long) NZTr << 27) + ((long) NZC << NZt);
		long Ar = (long) NZA << NZt;
		long Rf = 0;
		int s = 8;
		int Sf = 35;

		int necessaryBytes = 0;
		int maxNecessaryBytes = 5;
		int cutZone = (int) stream.getLength() - lengthEmptyTermination;
		if(maxNecessaryBytes > cutZone){
			maxNecessaryBytes = cutZone;
		}
		if((lengthEmptyTermination == 0) && (((Cr >> 32) & 0xFF) == 0x00) && (NZL == -1)){
			Cr <<= 8;
			Ar <<= 8;
		}
		while((necessaryBytes < maxNecessaryBytes) && ((Rf + ((long) 1 << Sf) - 1 < Cr) || (Rf + ((long) 1 << Sf) - 1 >= Cr + Ar))){
			necessaryBytes++;
			if(necessaryBytes <= 4){
				Sf -= s;
				long b = stream.getByte(lengthEmptyTermination + necessaryBytes - 1);
				if(b < 0){
					b += 256;
				}
				Rf += b << Sf;
				if(b == 0xFF){
					s = 7;
				}else{
					s = 8;
				}
			}
		}
		return(necessaryBytes);
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

	@Override
	public void resetNumBitsWrittenLine() {
		// TODO Auto-generated method stub
		
	}


}