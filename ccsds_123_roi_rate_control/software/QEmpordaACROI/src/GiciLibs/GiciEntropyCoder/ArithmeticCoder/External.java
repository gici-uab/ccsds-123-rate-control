package GiciEntropyCoder.ArithmeticCoder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class External {
	
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
	
	/**
	 * Deletes all the repeated elements in the argument array and returns it.
	 * 
	 * @param array
	 * @return array without repeated elements.
	 */
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
//		Now it is needed to delete the last element of the list, as according to the upper code it has been added two times and we only need it one time.
//		temp.remove(temp.size()-1);
		int[] result = arrayList2array(temp);
		
		return result;
		
		
	}
	
	/**
	 * Deletes all the repeated elements in the argument array and returns it as a dynamic Integer List
	 * 
	 * @param array
	 * @return Integer dynamic List
	 */
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
