package GiciEntropyCoder.ArithmeticCoder;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class decodes bits for the fractional bit plane decoder of JPEG2000, decoding them with the MQ decoder. This class allows the use of the following MQ options: restart, reset, bypass. Usage example: <br>
 * &nbsp; construct<br>
 * &nbsp; decodeBit<br>
 * &nbsp; decodeBit<br>
 * &nbsp; decodeBit<br>
 * &nbsp; swapInputByteStream<br>
 * &nbsp; reset<br>
 * &nbsp; restart<br>
 * &nbsp; decodeBit<br>
 * &nbsp; decodeBit<br>
 * &nbsp; ...<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.2
 */
public class DumbMQDecoder {

	private final InputStream inputStream;

	/**
	 * Definition in {@link BOI.BOICoder.Code.MQCoder#A}
	 */
	private int A;

	/**
	 * Definition in {@link BOI.BOICoder.Code.MQCoder#C}
	 */
	private int C;

	/**
	 * Definition in {@link BOI.BOICoder.Code.MQCoder#t}
	 */
	private int t;

	/**
	 * Definition in {@link BOI.BOICoder.Code.MQCoder#Tr}
	 */
	private int Tr;

	/**
	 * Definition in {@link BOI.BOICoder.Code.MQCoder#K}
	 */
	private int L;

	
	private boolean initialized = false;
	
	/**
	 * Constructor that initializes internal registers of the MQ decoder.
	 *
	 * @param inputByteStream the input stream of bytes
	 *
	 * @throws ErrorException when end of ByteStream is reached
	 */
	public DumbMQDecoder(InputStream inputStream) {
		this.inputStream = inputStream;		
	}

	/**
	 * Decode a bit.
	 *
	 * @param probability for the bit to be 1 (from 0x0001 to 0xAC01, where 0x5601 is 0.5?)
	 * @return a boolean indicating the bit decoded
	 * 
	 * @throws IOException 
	 */
	public boolean decodeBitProb(int probability) throws IOException{
	
		// Delay a possible IOException
		if (! initialized) {
			//Initialize internal registers
			initialized = true;
			restart();
		}
		
		assert (probability >= 0x0001 && probability <= 0xAC01);
		boolean mostProbableSymbol = false;
		
		if (probability > 0x5601) {
			mostProbableSymbol = true;
			probability = 0xAC02 - probability;
		}
		
		//OPTIMIZATION OF JPEG2000 BOOK (p 646) by Taubman/Marcellin
		int p = probability;
		int s = mostProbableSymbol ? 1 : 0;
		int x = s;

		A -= p;
		if ((C & 0x00FFFF00) >= (p << 8)) {
			C = ((C & ~0xFFFFFF00) | ((C & 0x00FFFF00) - (p << 8)));
			if (A < (1 << 15)) {
				if (A < p) {
					x = 1 - s;
				}

				while (A < (1 << 15)) {
					if(t == 0){
						fillLSB();
					}
					A <<= 1;
					C <<= 1;
					t--;
				}
			}
		} else {
			if (A >= p) {
				x = 1 - s;
			}
			A = p;

			while (A < (1 << 15)) {
				if(t == 0){
					fillLSB();
				}
				A <<= 1;
				C <<= 1;
				t--;
			}
		}
		
		
		/*
		//"NORMAL" ALGORITHM
		int x;
		
		int p = probability;
		int s = mostProbableSymbol ? 1 : 0;

		// Construction of the new sub-interval. Assign new values to C and A from the probability p.
		A -= p;
		// Conditional exchange of MPS and LPS in order to associate the longest interval (p or A) to the MPS.
		if(A < p){
			s = 1 - s;
		}
		int C_active = (C & 0x00FFFF00) >>> 8;
		if(C_active < p){ //Compare active region of C
			x = 1 - s;
			A = p;
		}else{
			x = s;
			C_active -= p;
			C_active <<= 8;
			C &= (~0xFFFFFF00);
			C |= C_active;
		}

		//Perform re-normalization shift
		while(A < (1 << 15)){
			if(t == 0){
				fillLSB();
			}
			A <<= 1;
			C <<= 1;
			t--;
		}
		*/
		
		return(x == 1);
	}

	/**
	 * Fill the C register with a new byte of inputByteStream. If the end of the inputStream is reached, then this function fills the new byte with a value of 0xFF.
	 */
	private void fillLSB() throws IOException{
		byte BL = 0;
		t = 8;
		
		int bl = inputStream.read();
		
		if (bl >= 0) {
			BL = (byte) bl;
		}
				
		// Reached the end of the inputByteStream OR read a marker code (this should not happen)
		if (bl < 0 || (Tr == 0xFF && BL > 0x8F) ) {
			C += 0xFF;
			
			// Comment this to disable error reporting
			if(bl >= 0){
				throw new IOException("inputStream is corrupted or not an arithmetic codeword");
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
	 * Start the value of the internal variables A, C, ... of the MQ decoder filling them with the values of the inputStream.
	 */
	private void restart() throws IOException{
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

}
