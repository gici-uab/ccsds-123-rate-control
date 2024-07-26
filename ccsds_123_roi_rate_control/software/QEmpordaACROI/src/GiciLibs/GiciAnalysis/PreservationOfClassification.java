package GiciAnalysis;

import java.util.*;
import java.util.concurrent.*;

public class PreservationOfClassification implements Callable<Float> {

	final int[] classification1;
	final int[] classification2;
		
	/**
	 * This class tries to solve the class matching problem of the POC metric.
	 * Very similar classifications are expected for good results.
	 * @param _classification1
	 * @param _classification2
	 */
	public PreservationOfClassification (final int[] _classification1, final int[] _classification2) {
		super();
		this.classification1 = _classification1;
		this.classification2 = _classification2;
		
		assert(classification1.length == classification2.length);
	}

	public Float call() throws Exception {		
		final Map<Integer,Set<Integer>> reverseMap1 = new TreeMap<Integer,Set<Integer>>();
		final Map<Integer,Set<Integer>> reverseMap2 = new TreeMap<Integer,Set<Integer>>();
		
		// Extract the classes
		for (int i = 0; i < classification1.length; i++) {	
			Set<Integer> s1 = reverseMap1.get(classification1[i]);
			Set<Integer> s2 = reverseMap2.get(classification2[i]);
			
			if (s1 == null)
				s1 = new TreeSet<Integer>();
			
			if (s2== null)
				s2 = new TreeSet<Integer>();
			
			s1.add(i);
			s2.add(i);
			
			reverseMap1.put(classification1[i], s1);
			reverseMap2.put(classification2[i], s2);
		}
		
		// Match them
		/* Lets follow a very greedy method for class matching:
		 * Just map classes from classification1 to classification2.
		 * Start with classes from classification1, first the ones with more members,
		 * and match them to their best immediate fit of classification2. 
		 */
		
		List<Integer> keys = new ArrayList<Integer>(reverseMap1.keySet());
		
		Collections.sort(keys, new Comparator<Integer>() {
			public int compare(Integer arg0, Integer arg1) {
				return reverseMap1.get(arg1).size() - reverseMap1.get(arg0).size();
			}
		});
		
		int count = 0;
		
		for (Integer key1: keys) {
			// Biggest first
			
			int bestMatch = 0;
			Integer bestKey2 = null;
			
			for (Integer key2: reverseMap2.keySet()) {
				Set<Integer> t = new TreeSet<Integer>(reverseMap2.get(key2));
				
				t.retainAll(reverseMap1.get(key1));
				
				if (t.size() >= bestMatch) {
					bestMatch = t.size();
					bestKey2 = key2;
				}
			}
			
			// By now we must have a winner
			// System.err.println("best match for class " + key1 + "(" + reverseMap1.get(key1).size() + ") is " + bestMatch);
			count += bestMatch;
			reverseMap2.remove(bestKey2);
			
			if (reverseMap2.size() == 0)
				break;
		}
		
		return count / (float) classification1.length;
	}
	
	
}
