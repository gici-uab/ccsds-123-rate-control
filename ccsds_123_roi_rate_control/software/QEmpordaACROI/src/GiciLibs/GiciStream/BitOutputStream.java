package GiciStream;

import java.io.IOException;
import java.util.List;

public interface BitOutputStream {

	/**
	 * Flushes bits not yet written. Either this function or
	 * <code>close</code> must be called to ensure that all bits will be
	 * written.
	 *
	 * @throws IOException if there's a problem writing bits.
	 */
	public abstract void flush() throws IOException;

	/**
	 * Releases system resources associated with the stream and flushes bits
	 * not yet written. Either this function or <code>flush</code> must be
	 * called to ensure that all bits will be written.
	 *
	 * @throws IOException if close fails.
	 */
	public abstract void close() throws IOException;

	/**
	 * Write specified number of bits from value to the stream.
	 *
	 * @param howManyBits is number of bits to write (0-32).
	 * @param value is the source of bits. Rightmost bits are written.
	 *
	 * @throws IOException if there's an I/O problem writing bits
	 */
	public abstract void write(int howManyBits, int value) throws IOException;

	public abstract void write(int value) throws IOException;

	public abstract void write(byte[] values, int off, int len)
			throws IOException;

	public abstract void write(byte[] values) throws IOException;
	
	public void writeToOutputStream(BitOutputStream fbos)throws IOException;

	public abstract void write(int howManyBits, boolean b) throws IOException;
	

}