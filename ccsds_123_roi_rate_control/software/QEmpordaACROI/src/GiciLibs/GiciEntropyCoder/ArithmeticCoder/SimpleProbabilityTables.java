package GiciEntropyCoder.ArithmeticCoder;

import GiciStream.BitInputStream;
import GiciStream.FileBitOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;


public class SimpleProbabilityTables extends StaticProbabilityTable {

	protected SimpleProbabilityTables(BigInteger rangeBounds) {
		super(null);
	}
	
	static public StaticProbabilityTable getProbabilityTableByRange (BigInteger[] rangeBounds) {
		BigInteger[] result = new BigInteger[rangeBounds.length];	
		for (int i = 0; i < rangeBounds.length; i++) {
			result[i] = rangeBounds[i].multiply(BigInteger.valueOf(2 << 
					20)).divide(rangeBounds[rangeBounds.length - 1]);
		}
		
		assert(result[rangeBounds.length - 1].bitLength() == 
			result[rangeBounds.length - 1].getLowestSetBit() + 1);
			
		return new StaticProbabilityTable(result);
	}

	static public StaticProbabilityTable getBinaryProbabilityTable(final float zeroFrequency) {
		BigInteger[] rangeBounds = new BigInteger[3];
		
		final int shift = 2 << 20;
	
		rangeBounds[0] = BigInteger.ZERO;
		rangeBounds[1] = BigInteger.valueOf((int) (zeroFrequency * shift));
		rangeBounds[2] = BigInteger.valueOf(shift);
		
		if (rangeBounds[1].compareTo(rangeBounds[0]) <= 0) {
			rangeBounds[1] = BigInteger.ONE;
		} else if (rangeBounds[1].compareTo(rangeBounds[2]) >= 0) {
			rangeBounds[1] = rangeBounds[2].subtract(BigInteger.ONE);
		}
		
		return new StaticProbabilityTable(rangeBounds);
	}
	
	/*
	 * Probability table creation option 1/0
	 * 
	 * In this method we only take care about the maximum
	 * symbol of the predicted values to create prob tables. rangeBounds will be a list of as many elements
	 * as the number of the maximum symbol.
	 */
	static public StaticProbabilityTable getEquiprobableTable(final int elementCount) {
		
		assert (elementCount > 1);
		
		final BigInteger[] rangeBounds = new BigInteger[elementCount + 1];
		
		final BigInteger elementCountBig = BigInteger.valueOf(elementCount);
		BigInteger totalBound = BigInteger.ONE;
		
		while (totalBound.compareTo(elementCountBig) < 0) {
			totalBound = totalBound.shiftLeft(1);
		}
		
		for (int i = 0; i < elementCount; i++) {
			rangeBounds[i] = BigInteger.valueOf(i).multiply(totalBound).divide(elementCountBig);
		}
		
		rangeBounds[elementCount] = totalBound;
		
		return new StaticProbabilityTable(rangeBounds);
	}
	
	/*
	 * Probability table creation option 2
	 * 
	 * In this method we use max and min symbols in the predicted values
	 * if the image has a minimum value bigger than 0, then using the previous method (option 1) we would be 
	 * creating a bigger list than needed.
	 */
	static public StaticProbabilityTable getEquiprobableTable(int max, int min) {
		
		assert (max > 1);
		
		final BigInteger[] rangeBounds = new BigInteger[max + 1];
		
		final BigInteger elementCountBig = BigInteger.valueOf(max);
		BigInteger totalBound = BigInteger.ONE;

		while (totalBound.compareTo(elementCountBig) < 0) {
			totalBound = totalBound.shiftLeft(1);
		}
		
		for (int i = 0; i < max; i++) {
//			we only want equiprobability in those elements that we found in predicted samples.
			if(i < min) {
				rangeBounds[i] = BigInteger.valueOf(0);
			}else{
				rangeBounds[i] = BigInteger.valueOf(i);//.multiply(totalBound).divide(elementCountBig);
			}
		}
		rangeBounds[max] = totalBound;
		
		return new StaticProbabilityTable(rangeBounds);
	}
	
	/*
	 * Probability table creation option 3
	 * 
	 * In this method we only focus on those elements that appear in predicted Values.
	 * The point is that the table has the same structure as before (as many elements as number of the biggest element in pred values)
	 * but we only change the frequency of those that will be coded (appear in predicted Samples).
	 */
	static public StaticProbabilityTable getPTfromPredSamples(int max, int[] predSamples, int band, boolean coder) throws IOException{
		
		assert (max > 1);
		assert (predSamples != null);
		
//		BufferedWriter probTabOutput = null;
		FileBitOutputStream fbos = null;
		//After this predSamples will be an ordered array with ONLY those elements that appear in predictedSamples
		Arrays.sort(predSamples);
		//		System.out.println(predSamples.length);
		
		final BigInteger[] rangeBounds = new BigInteger[max + 1];
		
		BigInteger totalBound = BigInteger.ONE;
		
		for(int i = 0; i < rangeBounds.length; i++){
			rangeBounds[i] = BigInteger.valueOf(0); 
		}
//		This bufferedWriter is needed to write the predictedValues found. They will be used to create prob tables in decoder.
		if(coder){
			try {
	//			Declaration of the file that will be written and the stream used
		          File file = new File("side_"+band);
		          FileOutputStream fileStream = new FileOutputStream(file);
		          fbos = new FileBitOutputStream( new BufferedOutputStream(fileStream));
//		          probTabOutput = new BufferedWriter(new FileWriter(file));
		    } catch ( IOException e ) {
		           e.printStackTrace();
		    }
		}
		int count = 0; //used to count the number of elements found
		if(rangeBounds[predSamples[0]].compareTo(BigInteger.ZERO) != 0){
			// TODO: linia original sense if
			fbos.write(16, predSamples[0]);
			count++;
			rangeBounds[0] = rangeBounds[0].add(BigInteger.valueOf(1));
			for(int x = 1; x < rangeBounds.length; x++) {
				// As new elements have been coded, accum freq must be modified
				rangeBounds[x] = rangeBounds[x].add(BigInteger.valueOf(1));
			}
		}
		
		for(int y = 1; y < predSamples.length; y++){
//			TODO: If there's an image with a predictedValue that cannot be expressed with 16 bit, this won't work.
			if (coder) fbos.write(16, predSamples[y]);
			count++;
			if(rangeBounds[predSamples[y]].compareTo(rangeBounds[predSamples[y-1]]) == 0){
				for(int x = predSamples[y]; x < rangeBounds.length; x++) {
					// As new elements have been coded, accum freq must be modified
					rangeBounds[x] = BigInteger.valueOf(count);
//					rangeBounds[x] = rangeBounds[x].add(BigInteger.valueOf(1));
				}
			}
		}
		
		
//		En aquest cas no es pot cridar el mètode setLast, per que s'ha de tenir creada la rangeBounds correctament per crear un staticProbtable
		while (totalBound.compareTo(rangeBounds[rangeBounds.length-1]) < 0) {
			totalBound = totalBound.shiftLeft(1);
		}
		rangeBounds[rangeBounds.length-1] = totalBound;
		
		if (coder) fbos.close();
		StaticProbabilityTable pt = new StaticProbabilityTable(rangeBounds); 
		
//		pt.setLast(predSamples);
//		
		
		return pt;
	}
	
	
//	Used to create probability tables with all those values that appear in predicted Samples inside an histogram
	static public StaticProbabilityTable getPTfromHist(int[] symb, int[] freq, int band, boolean coder) throws IOException{
//		System.out.println(symb[symb.length-1]);
		assert (symb[symb.length-1] > 1);
		assert (symb != null && freq != null);
		
//		BufferedWriter probTabOutput = null;
		FileBitOutputStream fbos = null;
		
		final BigInteger[] rangeBounds = new BigInteger[symb[symb.length-1]+2]; // la mida del rangeBounds és igual al nombre més gran dels símbols que apareixen
		
		BigInteger totalBound = BigInteger.ONE;
		
		for(int i = 0; i < rangeBounds.length; i++){
			rangeBounds[i] = BigInteger.valueOf(0); 
		}
//		This bufferedWriter is needed to write the histogram.
		if(coder){
			try {
	//			Declaration of the file that will be written and the stream used
		          File file = new File("hist_"+band);
		          FileOutputStream fileStream = new FileOutputStream(file);
		          fbos = new FileBitOutputStream( new BufferedOutputStream(fileStream));
//		          probTabOutput = new BufferedWriter(new FileWriter(file));
		    } catch ( IOException e ) {
		           e.printStackTrace();
		    }
		}
	
		for(int symbol = 0;symbol<symb.length; symbol++){
			if (coder){
//				System.out.println("symb[symbol]: "+symb[symbol]);
//				System.out.println("freq[symbol]: "+freq[symbol]);
				fbos.write(18,symb[symbol]);
				fbos.write(18,freq[symbol]);
			}
			for(int x = symb[symbol]; x <rangeBounds.length; x++){
				rangeBounds[x] = rangeBounds[x].add(BigInteger.valueOf(freq[symbol]));	
			}
		}
		
//		En aquest cas no es pot cridar el mètode setLast, per que s'ha de tenir creada la rangeBounds correctament per crear un staticProbtable
		while (totalBound.compareTo(rangeBounds[rangeBounds.length-1]) < 0) {
			totalBound = totalBound.shiftLeft(1);
		}
		rangeBounds[rangeBounds.length-1] = totalBound;
		
//		DEBUG PRINT
//		for(int ind = 0;ind<symb.length; ind++){
//			System.out.println("symb["+ind+"]: "+symb[ind]+"\t"+"freq["+ind+"]: "+freq[ind]);
//		}
//		
		if (coder) fbos.close();
		StaticProbabilityTable pt = new StaticProbabilityTable(rangeBounds); 
		
		return pt;
	}
	
	/**
	 * Returns de probability table from the values contained in the general histogram created by
	 * the app hist_creator
	 * 
	 * @params output or path of the file that contains the general histogram
	 * @return the probability table
	 * @throws IOException when the file that contains the general histogram doesn't exist or the path is wrong
	 */
	static public StaticProbabilityTable getPTfromGenHist(String output) throws IOException{
		
		BitInputStream bis = null;
		
		try{
			File out = new File(output);
			FileInputStream fileStream = new FileInputStream(out);
			bis = new BitInputStream( new BufferedInputStream( fileStream ) );
			
		} catch(IOException e){
			e.printStackTrace();
		}
		
		// Reading first the number of bits needed to represent symbols and frequencies
		int s_bits = bis.read(5);
		int f_bits = bis.read(5);
//		System.out.println("s_bits: "+s_bits);
//		System.out.println("f_bits: "+f_bits);
		
		// the size of rangeBounds is written as well in the hist_creator app just after the s and f bits.
		int max_symb = bis.read(s_bits);
//		System.out.println("max_symb: "+max_symb);
		final BigInteger[] rangeBounds = new BigInteger[max_symb+2]; 
//		System.out.println("rangeBounds.length: "+rangeBounds.length);
		
		BigInteger totalBound = BigInteger.ONE;
		
		for(int i = 0; i < rangeBounds.length; i++){
			rangeBounds[i] = BigInteger.valueOf(0); 
		}
		
		while(bis.available()>0){
			
			int curr_symb = bis.read(s_bits);
			int curr_freq = bis.read(f_bits);
			for(int x = curr_symb; x <rangeBounds.length; x++){
				rangeBounds[x] = rangeBounds[x].add(BigInteger.valueOf(curr_freq));	
			}
			
		}
		
		while (totalBound.compareTo(rangeBounds[rangeBounds.length-1]) < 0) {
			totalBound = totalBound.shiftLeft(1);
		}
		rangeBounds[rangeBounds.length-1] = totalBound;
		
//		for(int rind = 0; rind < rangeBounds.length; rind++){
//			System.out.println(rind);
//			System.out.println("rangeBounds[rind]: "+rangeBounds[rind]);
//		}
		
		bis.close();
		
		StaticProbabilityTable pt = new StaticProbabilityTable(rangeBounds);
			
		return pt;
		
	}
}
