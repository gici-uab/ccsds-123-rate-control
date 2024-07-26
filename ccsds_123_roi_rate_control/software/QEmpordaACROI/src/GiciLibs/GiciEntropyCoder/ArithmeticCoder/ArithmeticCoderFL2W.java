/**
 * Copyright (C) 2013 - Francesc Auli-Llinas
 *
 * This program is distributed under the BOI License.
 * This program is distributed in the hope that it will be useful, but without any warranty; without
 * even the implied warranty of merchantability or fitness for a particular purpose.
 * You should have received a copy of the BOI License along with this program. If not,
 * see <http://www.deic.uab.cat/~francesc/software/license/>.
 */
package GiciEntropyCoder.ArithmeticCoder;

import java.io.IOException;

import GiciEntropyCoder.Interface.EntropyCoder;
import GiciStream.ByteStream;


/**
 * This class implements an arithmetic coder with 2 codewords of fixed length to increase
 * efficiency. This class can perform both the operations of the encoder and the decoder.
 * The constructor of the class permits the use of codewords with different lengths, and
 * the use of probabilities with different precision. It also permits the use of
 * context-adaptive coding. As the MQ coder, this coder stuffs one 0 bit after a byte
 * equal to 0xFF.<br>
 *
 * Usage: once the object is created, the functions to code/decode symbols are used to code
 * the message. Instead of destroying the object and creating another one to encode a new
 * message, it is more computationally efficient to reuse the same object. When encoding,
 * the same object can be reused by calling the functions <code>terminate</code>, get the stream
 * wherever is needed, <code>changeStream</code>, <code>restartEncoding</code> and <code>reset</code>
 * in this order. To reuse the decoder, the functions <code>changeStream</code>,
 * <code>restartDecoding</code>, and <code>reset</code> have to be called in this order.<br>
 *
 * Multithreading support: the object must be created and manipulated by a single thread.
 * There can be many objects of this class running simultaneously as long as a single thread
 * manipulates each object.<br>
 *
 * @author Francesc Auli-Llinas
 * @version 1.1
 */
public final class ArithmeticCoderFL2W implements EntropyCoder{

	/**
	 * Minimum size of the interval at which the percentage of TOLERANCE begins to apply.
	 * <p>
	 * Must be greater than 2 and smaller than 2^codewordLength - 1.
	 */
	private static final int MIN_SIZE = 15;

	/**
	 * Percentage of tolerance between the real probability of the symbol coded and the
	 * probability given by the current interval.
	 * <p>
	 * Must be greater than 0 and smaller than 0.5.
	 */
	private static final float TOLERANCE = 0.05f;

	/**
	 * Indicates the number of symbols coded before updating the context probability.
	 * <p>
	 * Must be 2^X - 1.
	 */
	private static final int UPDATE_PROB0 = 7;

	/**
	 * Indicates the number of symbols appeared more recently that are taken into account to
	 * compute the probability of the context.
	 * <p>
	 * Must be 2^X - 1.
	 */
	private static final int WINDOW_PROB = 2047;

	/**
	 * ByteStream employed by the coder to write/read the output/input bytes.
	 * <p>
	 * The stream may contain zero bytes.
	 */
	private ByteStream stream;

	/**
	 * Length of the codeword encoded. It can be changed by the constructor.
	 * <p>
	 * If must be greater than 0 and shorter than 60.
	 */
	private int codewordLength = 32;

	/**
	 * Number of bits to represent the probability employed to code the symbols. Note that to
	 * use fewer bits than the codewordLength may decrease compression efficiency for very
	 * high/low probabilities (to use more bits is inconsequential). It can be changed by the
	 * constructor.
	 * <p>
	 * If must be greater than 0 and smaller than 64. Be aware that codewordLength + precisionBits < 64.
	 */
	private int precisionBits = 15;

	/**
	 * Maximum allowed value of the codeword.
	 * <p>
	 * It is 2^codewordLength - 1, and is computed by the constructor of the class.
	 */
	private long codewordMax = -1;

	/**
	 * Number of complete bytes of the codeword.
	 * <p>
	 * It is ceil(codewordLength / 8), and is computed by the constructor of the class.
	 */
	private int codeword2Bytes = -1;

	/**
	 * Probability 0.5 using the precision bits of the class.
	 * <p>
	 * It is 2^(precisionBits - 1), and is computed by the constructor of the class.
	 */
	private int precisionMid = -1;

	/**
	 * Tolerance stored in the internal representation of the probabilities.
	 * <p>
	 * It is prob0ToFL2W(TOLERANCE, precisionBits), and is computed by the constructor of the class.
	 */
	private long tolerance = -1;

	/**
	 * Minimum value of the intervals.
	 * <p>
	 * Must be greater than 0 and smaller than 2^codewordLength - 1. Each index of the array
	 * correspond to one of the codewords being coded.
	 */
	private long[] intervalMin = {-1, -1};

	/**
	 * Size of the interval - 1.
	 * <p>
	 * Must be greater than 0 and equal or smaller than 2^codewordLength - 1. Each index of the
	 * array correspond to one of the codewords being coded.
	 */
	private long[] intervalSize = {-1, -1};

	/**
	 * Number within the interval stored in the codeword.
	 * <p>
	 * Must be equal or greater than intervalMin and equal or smaller than
	 * intervalMin + intervalSize.  Each index of the array correspond to one of the codewords
	 * being coded.
	 */
	private long[] interval = {-1, -1};

	/**
	 * Byte to flush out/read.
	 * <p>
	 * Flushed byte to the stream.
	 */
	private int Tr = -1;

	/**
	 * Number of bits to transfer.
	 * <p>
	 * It is useful when the codewordLength does not correspond to a fixed number of bytes.
	 */
	private int t = -1;

	/**
	 * Current byte read/write.
	 * <p>
	 * Current position in the stream.
	 */
	private int L;

	/**
	 * Forcing one codeword to finish.
	 * <p>
	 * -1 indicates that no codeword is forced to finish, 0 and 1 indicate the index of the
	 * codeword that is forced to finish
	 */
	private int forcingWord = -1;

	/**
	 * Number of contexts.
	 * <p>
	 * Set when the class is instantiated.
	 */
	private int numContexts = -1;

	/**
	 * Current probability of each context.
	 * <p>
	 * Each index corresponds to one context. The probability is computed via {@link #prob0ToFLW}.
	 */
	private int[] contextProb0FL2W = null;

	/**
	 * Number of 0s coded in this context.
	 * <p>
	 * It is initialized to 0 in the <code>reset</code> function.
	 */
	private int[] context0s = null;

	/**
	 * Number of 0s coded in the last WINDOW_PROB symbols coded.
	 * <p>
	 * It is initialized to 0 in the <code>reset</code> function.
	 */
	private int[] context0sWindow = null;

	/**
	 * Total number of symbols coded in this context.
	 * <p>
	 * It is initialized to 0 in the <code>reset</code> function.
	 */
	private int[] contextTotal = null;

	/**
	 * Indicates whether the function <code>fillInterval</code> fills the interval with
	 * zeroes when no more bytes are in the stream.
	 * <p>
	 * True indicates that replenishment with 0s is activated.
	 */
	private boolean replenishment = true;

	/**
	 * Masks employed when transferring the bits of the interval to the stream.
	 * <p>
	 * The position in the array indicates the number of least significant bits for which
	 * the mask is computed.
	 */
	private static final long[] BIT_MASKS = {0x0, 0x01, 0x03, 0x07, 0x0F, 0x1F, 0x3F, 0x7F, 0xFF};


	/**
	 * Initializes internal registers. Before using the coder, a stream has to be
	 * set through <code>changeStream</code>.
	 */
	public ArithmeticCoderFL2W(){
		assert(codewordLength > 0);
		assert(precisionBits > 0);
		assert(codewordLength + precisionBits < 64);

		codewordMax = ((long) 1 << codewordLength) - 1;
		codeword2Bytes = (int) Math.ceil((float) codewordLength / 8f) * 2;
		precisionMid = 1 << (precisionBits - 1);
		tolerance = prob0ToFL2W(TOLERANCE, precisionBits);
		restartEncoding();
	}

	/**
	 * Initializes internal registers. Before using the coder, a stream has to be
	 * set through <code>changeStream</code>.
	 *
	 * @param codewordLength number of bits of the codeword
	 */
	public ArithmeticCoderFL2W(int codewordLength){
		this.codewordLength = codewordLength;

		assert(codewordLength > 0);
		assert(precisionBits > 0);
		assert(codewordLength + precisionBits < 64);

		codewordMax = ((long) 1 << codewordLength) - 1;
		codeword2Bytes = (int) Math.ceil((float) codewordLength / 8f) * 2;
		precisionMid = 1 << (precisionBits - 1);
		tolerance = prob0ToFL2W(TOLERANCE, precisionBits);
		restartEncoding();
	}

	/**
	 * Initializes internal registers. Before using the coder, a stream has to be
	 * set through <code>changeStream</code>.
	 *
	 * @param codewordLength number of bits of the codeword
	 * @param precisionBits number of bits employed in the probabilities
	 */
	public ArithmeticCoderFL2W(int codewordLength, int precisionBits){
		this.codewordLength = codewordLength;
		this.precisionBits = precisionBits;

		assert(codewordLength > 0);
		assert(precisionBits > 0);
		assert(codewordLength + precisionBits < 64);

		codewordMax = ((long) 1 << codewordLength) - 1;
		codeword2Bytes = (int) Math.ceil((float) codewordLength / 8f) * 2;
		precisionMid = 1 << (precisionBits - 1);
		tolerance = prob0ToFL2W(TOLERANCE, precisionBits);
		restartEncoding();
	}

	/**
	 * Initializes internal registers. Before using the coder, a stream has to be
	 * set through <code>changeStream</code>.
	 *
	 * @param codewordLength number of bits of the codeword
	 * @param precisionBits number of bits employed in the probabilities
	 * @param numContexts number of contexts available for this object
	 */
	public ArithmeticCoderFL2W(int codewordLength, int precisionBits, int numContexts){
		this.codewordLength = codewordLength;
		this.precisionBits = precisionBits;
		this.numContexts = numContexts;

		assert(codewordLength > 0);
		assert(precisionBits > 0);
		assert(numContexts > 0);
		assert(codewordLength + precisionBits < 64);

		codewordMax = ((long) 1 << codewordLength) - 1;
		codeword2Bytes = (int) Math.ceil((float) codewordLength / 8f) * 2;
		precisionMid = 1 << (precisionBits - 1);
		tolerance = prob0ToFL2W(TOLERANCE, precisionBits);
		contextProb0FL2W = new int[numContexts];
		context0s = new int[numContexts];
		context0sWindow = new int[numContexts];
		contextTotal = new int[numContexts];
		reset();
		restartEncoding();
	}

	/**
	 * Transforms the probability of the symbol 0 (or false) in the range (0,1) into the
	 * integer required in the FL2W coder to represent that probability.
	 *
	 * @param prob0 in the range [0:1]. If 0 or 1, the closest probability to 0/1 given the
	 * precision bits employed will be used --but it will not be 0 or 1.
	 * @param precisionBits number of bits employed to represent the probability in the returned
	 * integer
	 * @return integer that can be fed to the FL2W coder. It is computed as (2^precisionBits) * prob0.
	 */
	public static int prob0ToFL2W(float prob0, int precisionBits){
		assert((prob0 >= 0f) && (prob0 <= 1f));

		int prob0FL2W = (int) ((float) (1 << precisionBits) * prob0);
		if(prob0FL2W == 0){
			prob0FL2W = 1;
		}else if(prob0FL2W == (1 << precisionBits)){
			prob0FL2W = (1 << precisionBits) - 1;
		}
		assert((prob0FL2W > 0) && (prob0FL2W < (1 << precisionBits)));
		return(prob0FL2W);
	}

	/**
	 * Transforms the FL2W integer to the probability of the symbol 0 (or false) in the range (0,1).
	 *
	 * @param prob0FL2W integer fed to the FL2W coder. It is (2^precisionBits) * prob0.
	 * @param precisionBits number of bits employed to represent the probability in prob0FL2W.
	 * It is recommended to be, at least, equal to the codewordLength. To use fewer bits than the
	 * codewordLength may decrease compression efficiency for very high/low probabilities, and to
	 * use more is inconsequential. Also, be aware that codewordLength + precisionBits <= 64.
	 * @return prob0 in the range (0:1)
	 */
	public static float FL2WToProb0(int prob0FL2W, int precisionBits){
		assert((prob0FL2W > 0) && (prob0FL2W < (1 << precisionBits)));

		float prob0 = (float) prob0FL2W / (float) (1 << precisionBits);
		assert((prob0 > 0f) && (prob0 < 1f));
		return(prob0);
	}

	/**
	 * Encodes a bit without using any probability model.
	 *
	 * @param bit input
	 */
	public void encodeBit(boolean bit){
		encodeBitProb(bit, precisionMid);
	}

	/**
	 * Decodes a bit without using any probability model.
	 *
	 * @return bit output
	 * @throws Exception when some problem manipulating the stream occurs
	 */
	public boolean decodeBit() throws Exception{
		return(decodeBitProb(precisionMid));
	}

	/**
	 * Encodes a bit using a context so that the probabilities are adaptively adjusted depending
	 * on the incoming symbols.
	 *
	 * @param bit input
	 * @param context context of the symbol
	 */
	public void encodeBitContext(boolean bit, int context){
		//Updates the probability of this context, when necessary
		if((contextTotal[context] & UPDATE_PROB0) == UPDATE_PROB0){
			if(context0s[context] == 0){
				contextProb0FL2W[context] = 1;
			}else if(context0s[context] == contextTotal[context]){
				contextProb0FL2W[context] = (1 << precisionBits) - 1;
			}else{
				contextProb0FL2W[context] = (context0s[context] << precisionBits) / contextTotal[context];
			}
			assert((contextProb0FL2W[context] > 0) && (contextProb0FL2W[context] < (1 << precisionBits)));
			if((contextTotal[context] & WINDOW_PROB) == WINDOW_PROB){
				if(context0sWindow[context] != -1){
					contextTotal[context] -= WINDOW_PROB;
					context0s[context] -= context0sWindow[context];
				}
				context0sWindow[context] = context0s[context];
			}
		}

		//Encodes the bit
		encodeBitProb(bit, contextProb0FL2W[context]);

		//Updates the number of symbols coded for this context
		if(bit == false){
			context0s[context]++;
		}
		contextTotal[context]++;
	}

	/**
	 * Decodes a bit using a context so that the probabilities are adaptively adjusted depending
	 * on the outcoming symbols.
	 *
	 * @param context context of the symbols
	 * @return output bit
	 * @throws Exception when some problem manipulating the stream occurs
	 */
	public boolean decodeBitContext(int context) throws Exception{
		//Updates the probability of this context, when necessary
		if((contextTotal[context] & UPDATE_PROB0) == UPDATE_PROB0){
			if(context0s[context] == 0){
				contextProb0FL2W[context] = 1;
			}else if(context0s[context] == contextTotal[context]){
				contextProb0FL2W[context] = (1 << precisionBits) - 1;
			}else{
				contextProb0FL2W[context] = (context0s[context] << precisionBits) / contextTotal[context];
			}
			assert((contextProb0FL2W[context] > 0) && (contextProb0FL2W[context] < (1 << precisionBits)));
			if((contextTotal[context] & WINDOW_PROB) == WINDOW_PROB){
				if(context0sWindow[context] != -1){
					contextTotal[context] -= WINDOW_PROB;
					context0s[context] -= context0sWindow[context];
				}
				context0sWindow[context] = context0s[context];
			}
		}

		//Decodes the bit
		boolean bit = decodeBitProb(contextProb0FL2W[context]);

		//Updates the number of symbols coded for this context
		if(bit == false){
			context0s[context]++;
		}
		contextTotal[context]++;
		return(bit);
	}

	/**
	 * Encodes a bit using a specified probability.
	 *
	 * @param bit input
	 * @param prob0FL2W probability of bit == false.
	 */
	public void encodeBitProb(boolean bit, int prob0FL2W){
		assert(codewordLength + precisionBits < 64);
		assert((prob0FL2W > 0) && (prob0FL2W < (1 << precisionBits)));

		int selectedWord;
		if((forcingWord != 0) && (intervalSize[0] <= MIN_SIZE)){
			if(intervalSize[1] <= MIN_SIZE){
				long[] intervalMid = new long[2];
				intervalMid[0] = ((long) prob0FL2W * intervalSize[0]) >> precisionBits;
				intervalMid[1] = ((long) prob0FL2W * intervalSize[1]) >> precisionBits;
				long[] intervalMidFL2W = new long[2];
				intervalMidFL2W[0] = (intervalMid[0] + 1) << precisionBits;
				intervalMidFL2W[1] = (intervalMid[1] + 1) << precisionBits;
				long[] effectiveProb0FL2W = new long[2];
				effectiveProb0FL2W[0] = intervalMidFL2W[0] / (intervalSize[0] + 1);
				effectiveProb0FL2W[1] = intervalMidFL2W[1] / (intervalSize[1] + 1);
				long[] diff = new long[2];
				diff[0] = Math.abs(prob0FL2W - effectiveProb0FL2W[0]);
				diff[1] = Math.abs(prob0FL2W - effectiveProb0FL2W[1]);

				if(diff[0] <= diff[1]){
					selectedWord = 0;
				}else{
					selectedWord = 1;
				}
				if(bit == false){
					intervalSize[selectedWord] = intervalMid[selectedWord];
				}else{
					long tmp = intervalMid[selectedWord] + 1;
					intervalMin[selectedWord] += tmp;
					intervalSize[selectedWord] -= tmp;
				}

			}else{
				long intervalMid = ((long) prob0FL2W * intervalSize[0]) >> precisionBits;
				long intervalMidFL2W = (intervalMid + 1) << precisionBits;
				long effectiveProb0FL2W = intervalMidFL2W / (intervalSize[0] + 1);
				long diff = Math.abs(prob0FL2W - effectiveProb0FL2W);

				if(diff <= tolerance){
					selectedWord = 0;
					if(bit == false){
						intervalSize[0] = intervalMid;
					}else{
						long tmp = intervalMid + 1;
						intervalMin[0] += tmp;
						intervalSize[0] -= tmp;
					}
				}else{
					selectedWord = 1;
					if(bit == false){
						intervalSize[1] = ((long) prob0FL2W * intervalSize[1]) >> precisionBits;
					}else{
						long tmp = (((long) prob0FL2W * intervalSize[1]) >> precisionBits) + 1;
						intervalMin[1] += tmp;
						intervalSize[1] -= tmp;
					}
				}
			}

		}else{
			selectedWord = 0;
			if(bit == false){
				intervalSize[0] = ((long) prob0FL2W * intervalSize[0]) >> precisionBits;
			}else{
				long tmp = (((long) prob0FL2W * intervalSize[0]) >> precisionBits) + 1;
				intervalMin[0] += tmp;
				intervalSize[0] -= tmp;
			}
		}

		if(intervalSize[selectedWord] == 0){
			if(selectedWord == 0){
				interval[0] = intervalMin[0];
				transferInterval(codewordLength);
				if(intervalSize[1] == 0){
					interval[0] = intervalMin[1];
					transferInterval(codewordLength);
					intervalMin[0] = 0;
					intervalSize[0] = codewordMax;
				}else{
					intervalMin[0] = intervalMin[1];
					intervalSize[0] = intervalSize[1];
				}
				intervalMin[1] = 0;
				intervalSize[1] = codewordMax;
				forcingWord = -1;
			}else{
				forcingWord = 0;
			}
		}
	}

	/**
	 * Decodes a bit using a specified probability.
	 *
	 * @param prob0FL2W probability of bit == false.
	 * @return bit decoded bit
	 * @throws Exception when some problem manipulating the stream occurs
	 */
	public boolean decodeBitProb(int prob0FL2W) throws Exception{
		assert(codewordLength + precisionBits < 64);
		assert((prob0FL2W > 0) && (prob0FL2W < (1 << precisionBits)));

		if(intervalSize[0] == 0){
			if(intervalSize[1] == 0){
				fillInterval();
				intervalMin[0] = 0;
				intervalSize[0] = codewordMax;
				interval[0] = interval[1];
				fillInterval();
				intervalMin[1] = 0;
				intervalSize[1] = codewordMax;
			}else{
				intervalMin[0] = intervalMin[1];
				intervalSize[0] = intervalSize[1];
				interval[0] = interval[1];
				fillInterval();
				intervalMin[1] = 0;
				intervalSize[1] = codewordMax;
			}
			forcingWord = -1;
		}else if(intervalSize[1] == 0){
			forcingWord = 0;
		}

		boolean bit;
		if((forcingWord != 0) && (intervalSize[0] <= MIN_SIZE)){
			if(intervalSize[1] <= MIN_SIZE){
				long[] intervalMid = new long[2];
				intervalMid[0] = ((long) prob0FL2W * intervalSize[0]) >> precisionBits;
				intervalMid[1] = ((long) prob0FL2W * intervalSize[1]) >> precisionBits;
				long[] intervalMidFL2W = new long[2];
				intervalMidFL2W[0] = (intervalMid[0] + 1) << precisionBits;
				intervalMidFL2W[1] = (intervalMid[1] + 1) << precisionBits;
				long[] effectiveProb0FL2W = new long[2];
				effectiveProb0FL2W[0] = intervalMidFL2W[0] / (intervalSize[0] + 1);
				effectiveProb0FL2W[1] = intervalMidFL2W[1] / (intervalSize[1] + 1);
				long[] diff = new long[2];
				diff[0] = Math.abs(prob0FL2W - effectiveProb0FL2W[0]);
				diff[1] = Math.abs(prob0FL2W - effectiveProb0FL2W[1]);

				int selectedWord;
				if(diff[0] <= diff[1]){
					selectedWord = 0;
				}else{
					selectedWord = 1;
				}
				long tmp = intervalMid[selectedWord] + 1;
				long mid = intervalMin[selectedWord] + tmp;
				if(interval[selectedWord] >= mid){
					bit = true;
					intervalMin[selectedWord] = mid;
					intervalSize[selectedWord] -= tmp;
				}else{
					bit = false;
					intervalSize[selectedWord] = tmp - 1;
				}

			}else{
				long intervalMid = ((long) prob0FL2W * intervalSize[0]) >> precisionBits;
				long intervalMidFL2W = (intervalMid + 1) << precisionBits;
				long effectiveProb0FL2W = intervalMidFL2W / (intervalSize[0] + 1);
				long diff = Math.abs(prob0FL2W - effectiveProb0FL2W);

				if(diff <= tolerance){
					long tmp = intervalMid + 1;
					long mid = intervalMin[0] + tmp;
					if(interval[0] >= mid){
						bit = true;
						intervalMin[0] = mid;
						intervalSize[0] -= tmp;
					}else{
						bit = false;
						intervalSize[0] = tmp - 1;
					}
				}else{
					long tmp = (((long) prob0FL2W * intervalSize[1]) >> precisionBits) + 1;
					long mid = intervalMin[1] + tmp;
					if(interval[1] >= mid){
						bit = true;
						intervalMin[1] = mid;
						intervalSize[1] -= tmp;
					}else{
						bit = false;
						intervalSize[1] = tmp - 1;
					}
				}
			}

		}else{
			long tmp = (((long) prob0FL2W * intervalSize[0]) >> precisionBits) + 1;
			long mid = intervalMin[0] + tmp;
			if(interval[0] >= mid){
				bit = true;
				intervalMin[0] = mid;
				intervalSize[0] -= tmp;
			}else{
				bit = false;
				intervalSize[0] = tmp - 1;
			}
		}
		return(bit);
	}

	/**
	 * Transfers the interval of the first codeword to the stream (for encoding only).
	 *
	 * @param length number of bits of the interval to be transferred
	 */
	private void transferInterval(int length){
		int transferredBits = 0;
		while(transferredBits < length){
			int remainingBits = codewordLength - transferredBits;
			int transfer = remainingBits <= t? remainingBits: t;

			if((remainingBits - t) >= 0){
				Tr |= (interval[0] >> (remainingBits - t)) & BIT_MASKS[transfer];
			}else{
				Tr |= (interval[0] & BIT_MASKS[transfer]) << (t - remainingBits);
			}

			t -= transfer;
			if(t == 0){
				stream.putByte((byte) Tr);
				if(Tr == 0xFF){
					t = 7;
				}else{
					t = 8;
				}
				Tr = 0;
				L++;
			}
			assert(t >= 0 && t <= 8);

			transferredBits += transfer;
		}
	}

	/**
	 * Reads the interval of the second codeword from the stream (for decoding only). If at the
	 * middle of reading the interval, the stream runs out of bytes, this function fills the least
	 * significant bits of the codeword with 0s.
	 */
	private void fillInterval() throws Exception{
		if(!replenishment && (L >= stream.getLength())){
			//This placed here to throw the exception only when trying to fill a new interval
			throw new Exception("No more data in the stream to fill the interval.");
		}

		int readBits = 0;
		interval[1] = 0;
		do{
			if(t == 0){
				if(L < stream.getLength()){
					if(Tr == 0xFF){
						t = 7;
					}else{
						t = 8;
					}
					Tr = (0x000000FF & stream.getByte(L));
					L++;
				}else{
					//Fills with 0s
					Tr = 0;
					t = 8;
				}
			}

			int remainingBits = codewordLength - readBits;
			int read = remainingBits <= t? remainingBits: t;

			if((remainingBits - t) >= 0){
				interval[1] |= ((long) Tr & BIT_MASKS[read]) << (remainingBits - t);
			}else{
				interval[1] |= ((long) Tr >> (t - remainingBits)) & BIT_MASKS[read];
			}
			assert((interval[1] >= 0) && (interval[1] <= codewordMax));

			t -= read;
			assert(t >= 0 && t <= 8);

			readBits += read;
		}while(readBits < codewordLength);
	}

	/**
	 * Changes the current stream. When encoding, before calling this function the stream
	 * should be terminated calling the <code>terminate</code> function, and after calling this
	 * function the function <code>restartEncoding</code> must be called. When decoding, after calling
	 * this function the function <code>restartDecoding</code> must be called.
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
	 * Resets the state of all contexts.
	 */
	public void reset(){
		for(int c = 0; c < numContexts; c++){
			contextProb0FL2W[c] = prob0ToFL2W(0.66f, precisionBits); //Slightly biased towards 0
			context0s[c] = 2;
			context0sWindow[c] = -1;
			contextTotal[c] = 3;
		}
	}

	/**
	 * Restarts the internal registers of the coder for encoding.
	 */
	public void restartEncoding(){
		intervalMin[0] = 0;
		intervalSize[0] = codewordMax;
		intervalMin[1] = 0;
		intervalSize[1] = codewordMax;
		Tr = 0;
		t = 8;
		L = -1;
		forcingWord = -1;
	}

	/**
	 * Restarts the internal registers of the coder for decoding.
	 *
	 * @throws Exception when some problem manipulating the stream occurs
	 */
	public void restartDecoding() throws Exception{
		intervalMin[0] = 0;
		intervalSize[0] = 0;
		intervalMin[1] = 0;
		intervalSize[1] = 0;
		Tr = 0;
		t = 0;
		L = 0;
		forcingWord = -1;
	}

	/**
	 * Terminates the current stream using an optimal termination (for encoding purposes).
	 */
	public void terminate(){
		//Stores all bits of the first codeword
		interval[0] = intervalMin[0];
		transferInterval(codewordLength);

		//The second codeword can be terminated discarding some of the least significant bits
		long interval1 = 0;
		long interval2 = 0;
		int bits = codewordLength;
		long intervalMax = intervalMin[1] + intervalSize[1];
		while(((interval1 < intervalMin[1]) || (interval1 > intervalMax))
			&& ((interval2 < intervalMin[1]) || (interval2 > intervalMax))){
			bits--;
			interval1 |= ((long) 1 << bits) & intervalMin[1];
			interval2 |= ((long) 1 << bits) & intervalMax;
		}

		if((interval1 >= intervalMin[1]) && (interval1 <= intervalMax)){
			interval[0] = interval1;
		}else{
			assert((interval2 >= intervalMin[1]) && (interval2 <= intervalMax));
			interval[0] = interval2;
		}
		assert(bits >= 0);

		transferInterval(codewordLength - bits);
		if(t != 8){
			stream.putByte((byte) Tr);
			Tr = 0;
			t = 8;
		}
	}

	/**
	 * Computes the number of bytes belonging to the currently encoded data needed to flush
	 * the internal registers (for encoding purposes). This function is useful to determine
	 * potential truncation points of the stream.
	 *
	 * @return number of bytes
	 */
	public int remainingBytes(){
		if(t == 8){
			return(codeword2Bytes );
		}else{
			return(codeword2Bytes + 1);
		}
	}

	/**
	 * Gets the number of bytes read or written to the stream associated to the coder.
	 *
	 * @return number of bytes
	 */
	public int getReadBytes(){
		return(L);
	}

	/**
	 * Sets the {@link #replenishment} parameter.
	 *
	 * @param replenishment the new value for the parameter
	 */
	public void setReplenishment(boolean replenishment){
		this.replenishment = replenishment;
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
