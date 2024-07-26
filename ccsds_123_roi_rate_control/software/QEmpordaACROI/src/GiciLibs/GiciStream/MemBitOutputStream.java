package GiciStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This Class implements a BitOutputStream for writing to an ArrayList buffer.
 */
public class MemBitOutputStream implements BitOutputStream {
	private List<Integer> buffer = new ArrayList<Integer>();
	
	public MemBitOutputStream(){
		
	}

	public void flush() throws IOException {
		buffer.clear();
		//buffer = null;
		//buffer = new ArrayList<Integer>();
	}

	public void close() throws IOException {
		flush();
	}

	/**
	 * Write specified number of bits from value to a buffer.
	 *
	 * @param howManyBits is number of bits to write (0-32).
	 * @param value is the source of bits. Rightmost bits are written.
	 *
	 * @throws IOException if there's an I/O problem writing bits
	 */
	public void write(int howManyBits, int value) throws IOException {
		if (howManyBits > 32 || howManyBits < 0) {
			throw new RuntimeException("BitInputStream can only write from 0 to 32 bits.");
		}
		buffer.add(howManyBits);
		buffer.add(value);
	}
	
	public void write(int value) throws IOException {
//		write(value, 8);
		write(8,value);
	}

	public void write(byte[] values, int off, int len) throws IOException {
		for (int i = off; i < off + len; i++) {
			write(values[i]);
		}
	}


	public void write(byte[] values) throws IOException {
		write(values, 0, values.length);
	}
	
	public void writeToOutputStream(BitOutputStream fbos)throws IOException {
		for (int i = 0; i < buffer.size();  i+=2){
			fbos.write(buffer.get(i), buffer.get(i+1));
		}
	}
	public List<Integer> getBuffer(){
		return buffer;
	}

	@Override
	public void write(int howManyBits, boolean b) throws IOException {
		if(b){
			write(howManyBits,1);
		}else{
			write(howManyBits,0);
		}
	}
}
