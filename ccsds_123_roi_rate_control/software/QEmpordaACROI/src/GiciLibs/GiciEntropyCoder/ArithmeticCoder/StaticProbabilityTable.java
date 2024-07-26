package GiciEntropyCoder.ArithmeticCoder;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import emporda.CONS;

/**
 * Represents a compressed-range probability table (ideal for arithmetic coding).
 * It is intended to provide support to very large symbol tables, and several-bytes
 * symbols. 
 * @author ian
 *
 */
public class StaticProbabilityTable implements ProbabilityTable {
		
	// Initial probability for each bound. This is to represent the range of symbols to be coded.
	// S1 = [rangeBounds[0], rangeBounds[1]),
	// S2 = [rangeBounds[1], rangeBounds[2]),
	// SN = [rangeBounds[N-2], totalRange);
	public BigInteger[] rangeBounds;// TODO: change again to private. This change was made to debug
	private BigInteger totalRange;
	
	public StaticProbabilityTable(){
		this.rangeBounds = new BigInteger[10];
		this.totalRange = BigInteger.valueOf(0);
	}
	
	
	public StaticProbabilityTable(final BigInteger[] rangeBounds) {
		// Assert at least two elements and a power-of-two total count.
		assert(rangeBounds.length > 2);
		assert(rangeBounds[rangeBounds.length - 1].bitLength() == rangeBounds[rangeBounds.length - 1].getLowestSetBit() + 1);
		
		// Copy to our table (perhaps not required)
		this.rangeBounds = new BigInteger[rangeBounds.length];
		
		for (int i = 0; i < rangeBounds.length; i++) {
			assert(rangeBounds[i] != null);
			this.rangeBounds[i] = rangeBounds[i];
		}
		
		totalRange = rangeBounds[rangeBounds.length - 1];
	}
	
	/**
	 * Updates the probability table of the encoder as the predicted samples are being coded.
	 * 
	 * @param window or vector containing all predicted samples appeared partially
	 * @param predSamples
	 */
	public void updateProbabilityTable(int[] windowUpdate) {
				
		for(int w = 0; w < windowUpdate.length; w++){
			if(windowUpdate[w]!= 0){
				for(int i = w; i<rangeBounds.length; i++) {
					rangeBounds[i] = rangeBounds[i].add(BigInteger.valueOf(windowUpdate[w]));
				}
			}
		}
		this.setLast();
		
	}
	
	public void setLast(){
		
		BigInteger totalBound = BigInteger.ONE;
		
		while (totalBound.compareTo(rangeBounds[rangeBounds.length-1]) < 0) {
			totalBound = totalBound.shiftLeft(1);
		}
		rangeBounds[rangeBounds.length-1] = totalBound;
	}

	public BigInteger getSymbolCount() {
		return BigInteger.valueOf(rangeBounds.length - 1);
	}
	
	public BigInteger getObservationCount() {
		return totalRange;
	}
	
	private int bigIntegerToIndex (BigInteger symbol) {
		
		int index = symbol.intValue();
		// symbol is allowed to be = rangeBounds.length - 1 to get the total cumulative frequency
		assert(index >= 0 && index < rangeBounds.length);
		
		return index;
		
	}
	
	public BigInteger getCumulativeFrequency(BigInteger symbol) {
		
//		TODO: DEBUG TRY/CATCH 
		try{
			return rangeBounds[bigIntegerToIndex(symbol)];
		}catch(IndexOutOfBoundsException e){
			int value = bigIntegerToIndex(symbol);
		}
		return rangeBounds[bigIntegerToIndex(symbol)];
		
	}

	/*
	public BigInteger getFrequency(BigInteger symbol) {
		int index = bigIntegerToIndex(symbol);
		
		return rangeBounds[index + 1].subtract(rangeBounds[index]);
	}
*/
	
	public BigInteger findSymbolFromFrequency(BigInteger cumulativeFrequency) {
		assert (cumulativeFrequency.compareTo(BigInteger.ZERO) >= 0);
		assert (cumulativeFrequency.compareTo(totalRange) < 0);
		
		int result = Arrays.binarySearch(rangeBounds, cumulativeFrequency);
				
		if (result < 0) {
			int insertionPoint = -result - 1;
			result = insertionPoint - 1;
		}
		
		assert (result >= 0 && result < rangeBounds.length - 1);
		
		return BigInteger.valueOf(result);
	}
	
	final public int getSymbolByteSize() {
		int v = totalRange.bitLength() - 1;
		
		return (v) / 8 + (v % 8 != 0 ? 1 : 0);
	}
	
	final public void updateState(final BigInteger symbol) {
		// Not dynamic, so do nothing.
	}
	
	/**
	 * Allows to convert a normal multidimensional array of int (int[][]) to a normal int array (int[]). This is necessary to create the initiate rangeBounds correctly.
	 *
	 * @param matrix is the multidimensional array that will be turned on an array
	 * @return the final array
	 */
	public static int[] mat2Array(int matrix[][]) {
		//must keep in mind that matrix are always square number X number
		int height  = matrix.length;
		int width  = matrix[0].length;
		int result[] = new int[height * width];
		for(int y = 0;y < height;y++){
			for(int x = 0; x < width; x++) result[y*width + x] = matrix[y][x];
		}
		return result;
	}
	
	public static int[] prepareArray(int[] array) {
//		Must use List type because dynamic range is needed. The final numbers of elements can change
		List<Integer> temp = new ArrayList<Integer>();
//		Must the first element be 0 always?
		temp.add(0);
		for(int i = 0;i<array.length;i++){
			if(!temp.contains(array[i])){
				temp.add(array[i]);
//				temp.add(array[i]);
			}
		}

//		UTILITZAR temp.toArray() que és exactament el mateix
		int[] result = arrayList2array(temp);
		
		return result;
		
		
	}
	
	public static List<Integer> prepareDynArray(int[] array) {
//		Must use List type because dynamic range is needed. The final numbers of elements can change
		List<Integer> temp = new ArrayList<Integer>();
//		Must the first element be 0 always?
		temp.add(0);
		for(int i = 0;i<array.length;i++){
			if(!temp.contains(array[i])){
				temp.add(array[i]);
//				temp.add(array[i]);
			}
		}
		
		return temp;
		
		
	}

	public static int[] arrayList2array(List<Integer> temp) {
		int [] result = new int[temp.size()];
		for (int i = 0; i<temp.size(); i++) result[i] = temp.get(i);
		return result;
	}
	
	private BigInteger[] intArray2BigIntArray(int[] predictedAsArray) {
		BigInteger [] result = new BigInteger[predictedAsArray.length];
		for(int i = 0;i<predictedAsArray.length;i++) result[i] = BigInteger.valueOf(predictedAsArray[i]);
		return result;
	}
	
}
