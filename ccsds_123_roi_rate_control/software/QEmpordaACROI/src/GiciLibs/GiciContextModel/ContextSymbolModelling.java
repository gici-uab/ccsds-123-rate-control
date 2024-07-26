package GiciContextModel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import GiciStream.FileBitOutputStream;

public class ContextSymbolModelling implements Serializable{


	private int contextModel = 0;
	private int [][] samplesP0 = null;
	private int [][] samplesP1 = null;
	private int [][] samplesP2 = null;
	private int cb2 [] = null;
	private int cb1 [] = null;
	private int cb0 [] = null;
	private int [][][] contextos = null;
	private int [][] contextosTotal = null;
	private int qstep = 1;
	private int ySize = 0;
	private int xSize = 0;
	private int band = 0;
	private double entropy = 0;
	
	int bandslower64 = 0;
	int bandsupper64 = 0;
	double maxMemory = Double.MIN_VALUE;
	double memory = 0;

	/**
	 * Constructor of the ContextModelling. It receives and integer number that indicates the strategy used
	 * to compute the context model.
	 * 
	 * @param contextModel. 0 - no context model is used.
	 */
	public ContextSymbolModelling(){

	}
	
	public void setData(int[][] predictedSamplesPrevious2, int[][] predictedSamplesPrevious1, int[][]samples, int contextModel, int z){
		this.contextModel = contextModel;
		this.samplesP2 = predictedSamplesPrevious2;
		this.samplesP1 = predictedSamplesPrevious1;
		this.samplesP0 = samples;
		this.ySize = samples.length;
		this.xSize = samples[0].length;
		this.band = z;
	}
	
	
	public void buildProbabilityTable(int z){
		switch(contextModel){
		case 0:
			
			Map<String, Integer> contexts = new HashMap<String, Integer>();
			Map<String, Integer> contextsSample = new HashMap<String, Integer>();
			ArrayList<Double> probs = new ArrayList<Double>();
			
			entropy = computeCausalContextualEntropy(contexts, contextsSample, samplesP0, samplesP1, samplesP2);
			
			//defineContextualInformation(contexts, contextsSample, samplesP0, samplesP1, samplesP2);
			//computeProbabilities(contexts,contextsSample, probs);
			//saveDataToFile(contexts, "contexts_"+band+".data");
			//saveDataToFile(contextsSample, "contextsSample_"+band+".data");
			
			//entropy = computeEntropy(probs);
			//entropy = computeCodingEntropy(contexts, contextsSample);
			break;
		}
	}
	
	

	public double getEntropy() {
		return entropy;
	}
	
	/**
	 * Save the data needed for the decoder into a file 
	 * @param contexts
	 */
	private void saveDataToFile(Map<String, Integer> contexts, String filename) {
		try{
			FileWriter file = new FileWriter(filename);
			PrintWriter pw = new PrintWriter(file);
	        Set<String> keyset = contexts.keySet();
	        Iterator<String> iterator = keyset.iterator();
	        while(iterator.hasNext()) {
	            String context = iterator.next();
	            int contextCounter = contexts.get(context);
	            pw.write(context+" "+contextCounter);
	        }
	        pw.flush();
	        pw.close();
	    }catch(IOException e){
			e.printStackTrace();
		}
	}
	

	private double computeCodingEntropy(Map<String, Integer> contexts, Map<String, Integer> contextsSample) {
		double entropy = 0;
		for(int y = 0; y < ySize; y++){
			for(int x = 0; x < xSize; x++){
				int valueP0 = samplesP0[y][x]/qstep;
				int valueP1 = samplesP1[y][x]/qstep;
				int valueP2 = samplesP2[y][x]/qstep;
				
				String context = defineContext(valueP2, valueP1);
				String contextSample = defineContextSample(valueP2, valueP1, valueP0);
				
				int counterContexts = contexts.get(context);
				int counterContextsSample = contextsSample.get(contextSample);
				
				double prob = (double)counterContextsSample / (double)counterContexts;
				entropy = entropy + (-1 * (Math.log10(prob) / Math.log10(2)));
			}}
		
		return entropy;
	}
	

	private double computeEntropy(ArrayList<Double> probs) {
		double entropy = 0;
		for(int i = 0; i < probs.size(); i++) {
			double prob = probs.get(i);
			double currentEntropy = -1*(prob * Math.log10(prob)/Math.log10(2));
			entropy = entropy + currentEntropy;
		}
		
		return entropy;
	}

	/**
	 * Constructs contexts and contextsSample Maps. Key element identify the contexts or the context+Sample as string, value contains the amount of key elements of each 
	 * @param contexts Map
	 * @param contextsSampel Map
	 * @param samplesP0 integer matix belonging to the current band
	 * @param samplesP1 integer matix belonging to the previous band
	 * @param samplesP2 integer matix belonging to the second previous band
	 * @return
	 */
	private double computeCausalContextualEntropy(Map<String, Integer> contexts, Map<String, Integer> contextsSample,  int [][]samplesP0, int [][]samplesP1, int [][]samplesP2){
		double entropy = 0;
		
		try {
			
			FileWriter file = new FileWriter("side_"+band+".data");
			PrintWriter pw = new PrintWriter(file);
			
			/*
			ArrayList<Integer> TMP = new ArrayList<Integer>();
			for(int y = 0; y < ySize; y++){
			for(int x = 0; x < xSize; x++){
				int valueP0 = samplesP0[y][x]/qstep;
				if(!TMP.contains(valueP0)) TMP.add(valueP0);
			}}
			
			int difSymbols = TMP.size();
			*/
			
			for(int y = 0; y < ySize; y++){
			for(int x = 0; x < xSize; x++){
				
				int valueP0 = samplesP0[y][x]/qstep;
				int valueP1 = samplesP1[y][x]/qstep;
				int valueP2 = samplesP2[y][x]/qstep;
				
				String context = defineContext(valueP2, valueP1);
				String contextSample = defineContextSample(valueP2, valueP1, valueP0);
				double prob = 0;
				
				//There not exists context and in consequence neither contextSample
				if(!contexts.containsKey(context)) {
					contextsSample.put(contextSample,1);
					contexts.put(context,2);
					prob = (double)1 / (double)2;
					/////////add symbol to code/////////
					//pw.write(valueP0);
					pw.print(valueP0);
				}else {
					
					//There not exists contextSample but exists context
					if(contexts.containsKey(context) && !contextsSample.containsKey(contextSample)) {
						int counterContextSample = 1;
						contextsSample.put(contextSample,counterContextSample);
						int counterContext = contexts.get(context);
						counterContext++;
						contexts.put(context,counterContext);
						prob = (double)counterContextSample / (double)counterContext;
						/////////add symbol to code/////////
						//pw.write(valueP0);
						pw.print(valueP0);
					}else {
						
						//There exists contextSample and in consequence exists context
						
						//compute probability
						int counterContextSample = contextsSample.get(contextSample);
						int counterContext = contexts.get(context);
						prob = (double)counterContextSample / (double)counterContext;
						
						//update counters
						counterContextSample++;
						contextsSample.put(contextSample,counterContextSample);
						counterContext++;
						contexts.put(context,counterContext);
						
					}
				}
				
				
				
				
				
				
				
				entropy = entropy + (-1 * (Math.log10(prob) / Math.log10(2)));
				
			}}
			
			pw.flush();
		    pw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return entropy;
		
	}
	
	/**
	 * Constructs contexts and contextsSample Maps. Key element identify the contexts or the context+Sample as string, value contains the amount of key elements of each 
	 * @param contexts Map
	 * @param contextsSampel Map
	 * @param samplesP0 integer matix belonging to the current band
	 * @param samplesP1 integer matix belonging to the previous band
	 * @param samplesP2 integer matix belonging to the second previous band
	 * @return
	 */
	void defineContextualInformation(Map<String, Integer> contexts, Map<String, Integer> contextsSample,  int [][]samplesP0, int [][]samplesP1, int [][]samplesP2){
		
		for(int y = 0; y < ySize; y++){
			for(int x = 0; x < xSize; x++){
				int valueP0 = samplesP0[y][x]/qstep;
				int valueP1 = samplesP1[y][x]/qstep;
				int valueP2 = samplesP2[y][x]/qstep;
				
				String context = defineContext(valueP2, valueP1);
				String contextSample = defineContextSample(valueP2, valueP1, valueP0);
				
				
				if(contexts.containsKey(context)) {
					int counter = contexts.get(context);
					counter++;
					contexts.put(context,counter);
				}else {
					contexts.put(context,1);
				}
				
				if(contextsSample.containsKey(contextSample)) {
					int counter = contextsSample.get(contextSample);
					counter++;
					contextsSample.put(contextSample,counter);
				}else {
					contextsSample.put(contextSample,1);
				}
			}}
		}
	
	/**
	 * Defines the context + sample
	 * @param valueP2
	 * @param valueP1
	 * @param valueP0
	 * @return
	 */
	private String defineContextSample(int valueP2, int valueP1, int valueP0) {
		String contextSample = Integer.toString(valueP2) + Integer.toString(valueP1) + Integer.toString(valueP0);
		return contextSample;
	}

	/**
	 * Defines the context 
	 * @param valueP1
	 * @param valueP0
	 * @return
	 */
	private String defineContext(int valueP1, int valueP0) {
		String context = Integer.toString(valueP1) + Integer.toString(valueP0);
		return context;
	}

	/**
	 * Compute probabilites for all the contexts defined
	 * @param contexts
	 * @param contextsSample
	 * @param probs
	 */
	private void computeProbabilities(Map<String, Integer> contexts, Map<String, Integer> contextsSample, ArrayList<Double> probs) {
		for(int y = 0; y < ySize; y++){
		for(int x = 0; x < xSize; x++){
			int valueP0 = samplesP0[y][x]/qstep;
			int valueP1 = samplesP1[y][x]/qstep;
			int valueP2 = samplesP2[y][x]/qstep;
			
			String context = defineContext(valueP2, valueP1);
			String contextSample = defineContextSample(valueP2, valueP1, valueP0);
			
			int counterContexts = contexts.get(context);
			int counterContextsSample = contextsSample.get(contextSample);
			
			double prob = (double)counterContextsSample / (double)counterContexts;
			probs.add(prob);
		}}

	}

	/**
	 * The memory in GB needed to store all the contexts according to the contextModel selected
	 * @return memory
	 */
	double getMemoryNeeded() {
		return(this.memory);
	}
	
	/**
	 * return the contextModel employed
	 * @return contextModel
	 */
	int getContextModel() {
		return(this.contextModel);
	}

	/**
	 * Return an ArrayList taht contains only the samples that exist in the samples matrix.
	 * @param usefullSamples
	 * @param samples
	 * @return
	 */
	void usefullData(ArrayList<Integer> usefullSamples, int [][]samples){
		
		for(int y = 0; y < ySize; y++){
		for(int x = 0; x < xSize; x++){
			int value = samples[y][x]/qstep;
			if(!usefullSamples.contains(value))	usefullSamples.add(value);
		}}

	}
	
	
	
}
