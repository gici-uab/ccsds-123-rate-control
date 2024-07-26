package GiciContextModel;

import java.io.Serializable;

import GiciEntropyCoder.ArithmeticCoder.ArithmeticCoderFLW;

public class ContextProbability implements Serializable{
	
	/**
	 * Indicates the number of symbols coded before updating the context probability.
	 * <p>
	 * Must be of the form 2^X - 1 for probabilityModel = 0 and probabilityModel = 1, whereas for probabilityModel = 2 must be of form 2^X. The form of this values is given in the constructor of ContextProbability.
	 */
	private int UPDATE_PROB0 = 7;

	/**
	 * Indicates the maximum number of symbols within the variable-size sliding window that are employed
	 * to compute the probability of the context.
	 * <p>
	 * Must be of the form 2^X - 1 for probabilityModel = 0 and probabilityModel = 1, whereas for probabilityModel = 2 must be of form 2^X. The form of this values is given in the constructor of ContextProbability.
	 */
	private int WINDOW_PROB = 2047;

	
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
	private int[] contextProb0FLW = null;
	
	/**
	 * Current probability of each context.
	 * <p>
	 * Each index corresponds to one context. The probability is computed via {@link #prob0ToFLW}.
	 */
	private int[] contextProb0FLWPreviousState = null;

	/**
	 * Number of 0s coded in this context.
	 * <p>
	 * It is initialized to 0 in the <code>reset</code> function.
	 */
	private int[] context0s = null;
	
	/**
	 * Previous state of the number of 0s coded in this context.
	 * <p>
	 * It is initialized to 0 in the <code>reset</code> function.
	 */
	private int[] context0sPreviousState = null;

	/**
	 * Number of 0s coded in the last WINDOW_PROB symbols coded.
	 * <p>
	 * It is initialized to 0 in the <code>reset</code> function.
	 */
	private int[] context0sWindow = null;

	/**
	 * Number of 0s coded in the last WINDOW_PROB symbols coded.
	 * <p>
	 * It is initialized to 0 in the <code>reset</code> function.
	 */
	private boolean[][] context0sSlidingWindow = null;
	
	/**
	 * Number of 0s coded in the last WINDOW_PROB symbols coded.
	 * <p>
	 * It is initialized to 0 in the <code>reset</code> function.
	 */
	private int[] context0sWindowPreviousState = null;
	
	/**
	 * Total number of symbols coded in this context.
	 * <p>
	 * It is initialized to 0 in the <code>reset</code> function.
	 */
	private int[] contextTotal = null;
	
	/**
	 * Previous state of the total number of symbols coded in this context.
	 * <p>
	 * It is initialized to 0 in the <code>reset</code> function.
	 */
	private int[] contextTotalPreviousState = null;
	
	/**
	 * Number of bits to represent the probability employed to code the symbols. Note that to use fewer
	 * bits than the codewordLength may decrease compression efficiency for very high/low probabilities
	 * (to use more bits is inconsequential). It can be changed by the constructor.
	 * <p>
	 * If must be greater than 0 and smaller than 64. Be aware that codewordLength + precisionBits < 64.
	 */
	private int precisionBits = 0;
	
	/**
	 * Probability model employed
	 * <p>
	 * <ul>
	 * <li>0: The probability is computed using a full division is computed. Max Precision.</li>
	 * <li>1: The probability is computed using a division implemented through a quantized Look Up Table.</li>
	 * <li>2: The probability is computed using a the Arithmetic Mean.</li>
	 * </ul>
	 */
	private int probabilityModel = 0;
	
	/**
	 * Look Up Table that stores the precomputed probabilities.
	 * <p>
	 * Its size is ({@link #WINDOW_PROB}+1)*({@link #WINDOW_PROB}+1).
	 */
	private int [][] LUTProbs = null;
	
	/**
	 * Value that controls the size of the @link #LUTProbs.
	 * <p>
	 * It is initialized to 0, meaning that the size of @link #LUTProbs is not quantized.
	 */
	private int LUTProbQuantizer = 0;
	
	private int[] symbolCounter = null;
	
	public ContextProbability(int probabilityModel, int numContexts, int precisionBits, int quantizerProbabilityLUT, int encoderType, int WINDOW_PROB, int UPDATE_PROB0){
		if(probabilityModel == 0) {
			this.WINDOW_PROB = WINDOW_PROB - 1;
			this.UPDATE_PROB0 = UPDATE_PROB0 - 1;
		}
		if(probabilityModel == 2) {
			this.WINDOW_PROB = WINDOW_PROB;
			this.UPDATE_PROB0 = UPDATE_PROB0 = WINDOW_PROB;
		}
		
		contextProb0FLW = new int[numContexts];
		context0s = new int[numContexts];
		contextTotal = new int[numContexts];
		context0sPreviousState = new int[numContexts];
		contextTotalPreviousState = new int[numContexts];
		context0sWindowPreviousState = new int[numContexts];
		contextProb0FLWPreviousState = new int[numContexts];
		context0sWindow = new int[numContexts];
		context0sSlidingWindow = new boolean[numContexts][this.WINDOW_PROB];
		this.probabilityModel = probabilityModel;
		this.precisionBits = precisionBits;
		this.numContexts = numContexts;
		this.LUTProbQuantizer = quantizerProbabilityLUT;
		
		/*if(this.probabilityModel == 0 || this.probabilityModel == 1){
			this.WINDOW_PROB--;
			this.UPDATE_PROB0--;
		}*/
		symbolCounter = new int[numContexts];
		reset();
		
		if(probabilityModel == 1){
			setLUTProb();
		}
	}
	
	/**
	 * Updates the probabilities, which adaptively adjusted
	 * depending on the incoming symbols. Only updates the probability of this context, when necessary
	 *
	 * @param bit input
	 * @param context of the symbol
	 */
	public int getProbability(int context){
		
		int prob = 0;
		
		switch(probabilityModel){
			case 0:
				if((contextTotal[context] & UPDATE_PROB0) == UPDATE_PROB0){
					if(context0s[context] == 0){
						contextProb0FLW[context] = 1;
					}else if(context0s[context] == contextTotal[context]){
						contextProb0FLW[context] = (1 << precisionBits) - 1;
					}else{
						assert(context0s[context] * (double) (1 << precisionBits) <= Integer.MAX_VALUE);
						
						contextProb0FLW[context] = (context0s[context] << precisionBits) / contextTotal[context];
					}
					
					assert((contextProb0FLW[context] > 0) && (contextProb0FLW[context] < (1 << precisionBits)));
					
					if((contextTotal[context] & WINDOW_PROB) == WINDOW_PROB){
						if(context0sWindow[context] != -1){
							contextTotal[context] -= WINDOW_PROB;
							context0s[context] -= context0sWindow[context];
						}
						context0sWindow[context] = context0s[context];
					}
				}
				prob = contextProb0FLW[context];
				
				//System.out.println(contextProb0FLW[context]);
				
				break;
	
					
			case 1:
				if((contextTotal[context] & UPDATE_PROB0) == UPDATE_PROB0){
					if(context0s[context] == 0){
						contextProb0FLW[context] = 1;
					}else if(context0s[context] == contextTotal[context]){
						contextProb0FLW[context] = (1 << precisionBits) - 1;
					}else{
						contextProb0FLW[context] = LUTProbs[context0s[context] >> LUTProbQuantizer ][contextTotal[context] >> LUTProbQuantizer];
					}
					assert((contextProb0FLW[context] > 0) && (contextProb0FLW[context] < (1 << precisionBits)));
					if((contextTotal[context] & WINDOW_PROB) == WINDOW_PROB){
						if(context0sWindow[context] != -1){
							contextTotal[context] -= WINDOW_PROB;
							context0s[context] -= context0sWindow[context];
						}
						context0sWindow[context] = context0s[context];
					}
				}
				// TODO check prob != 0 i != max?
				if(contextProb0FLW[context] == 0)contextProb0FLW[context] = 1;
				prob = contextProb0FLW[context];
				break;
	
			case 2:
				assert(UPDATE_PROB0 <= WINDOW_PROB);
				
				if((contextTotal[context] & UPDATE_PROB0) == UPDATE_PROB0){
					
					if(context0s[context] == 0){
						contextProb0FLW[context] = 1;
					}else if(context0s[context] == contextTotal[context]){
						contextProb0FLW[context] = (1 << precisionBits) - 1;
					}else{
						contextProb0FLW[context] = (int) (((long)context0s[context] << (long)precisionBits) >> (long)(Integer.SIZE - Integer.numberOfLeadingZeros(UPDATE_PROB0 - 1)));
						if(contextProb0FLW[context] == 0) contextProb0FLW[context] = 1;
					}
					context0s[context] = context0s[context] >> 1;
					contextTotal[context] = contextTotal[context] >> 1;

				}
				
				// TODO check prob != 0 i != max?
				
				if(contextProb0FLW[context] == 0){
					contextProb0FLW[context] = 1;
				}				
				prob = contextProb0FLW[context];
				break;
				
			default:
				throw new Error();
		}
		
		return(prob);
	}
	
	/**
	 * Updates the probabilities, which adaptively adjusted
	 * depending on the incoming symbols. Only updates the probability of this context, when necessary
	 *
	 * @param bit input
	 * @param context of the symbol
	 */
	public int getProbability(boolean bit, int context){
		
		int prob = 0;
		int numOfZeros = 0;
		symbolCounter[context]++;
		if(symbolCounter[context] == WINDOW_PROB){
			
			//get 0 from context0sSlidingWindow[][]
			numOfZeros = get0sFromcontext0sSlidingWindowAndUpdate(bit, context);
			symbolCounter[context]--;
			if(numOfZeros == 0){
				contextProb0FLW[context] = 1;
			}else if(numOfZeros == contextTotal[context]){
				contextProb0FLW[context] = (1 << precisionBits) - 1;
			}else{
				assert(context0s[context] * (double) (1 << precisionBits) <= Integer.MAX_VALUE);
				
				
				contextProb0FLW[context] = (numOfZeros << precisionBits) / contextTotal[context];
			}
			
			assert((contextProb0FLW[context] > 0) && (contextProb0FLW[context] < (1 << precisionBits)));
			
				contextTotal[context]--;
			
				
			
		}
		prob = contextProb0FLW[context];
		//if(prob == 0)System.out.println(symbolCounter[context]+" "+numOfZeros+" "+(prob / (double) (1 << 15)));
				
		
		return(prob);
	}
		
	private int get0sFromcontext0sSlidingWindowAndUpdate(boolean bit, int context) {
		int count = 0;
		for(int symbol = 0; symbol < WINDOW_PROB; symbol++){
			if(!context0sSlidingWindow[context][symbol]) count++;
			if(symbol < WINDOW_PROB-1)context0sSlidingWindow[context][symbol] = context0sSlidingWindow[context][symbol+1];
		}
		context0sSlidingWindow[context][WINDOW_PROB-1] = bit;
		return count;
	}

	/**
	 * Updates the number of symbols coded for this context.
	 * 
	 * @param bit input
	 * @param context of the symbol
	 */
	public void updateSymbols(boolean bit, int context){
		//Updates the number of symbols coded for this context
		if(bit == false){
			context0s[context]++;
		}
		contextTotal[context]++;	
	}
	
	/**
	 * Saves the current ocntext State
	 */
	public void saveContextCurrentState(){
		for(int c = 0; c < numContexts; c++){
			context0sPreviousState[c] = context0s[c];
			contextTotalPreviousState[c] = contextTotal[c];
			context0sWindowPreviousState[c] = context0sWindow[c];
			contextProb0FLWPreviousState[c] = contextProb0FLW[c];
		}
	}
	
	public void printContextState(){
		for(int c = 0; c < numContexts; c++){
			if(context0s[c] != 2 )System.out.println("Context: "+c+" "+context0s[c]+" "+contextTotal[c]+" "+context0sWindow[c]+" "+(contextProb0FLW[c])/ (double) (1 << 15));
		}	
	}
	
	/**
	 * Loads the current context State
	 */
	public void loadContextStatePreviousState(){
		for(int c = 0; c < numContexts; c++){
			context0s[c] = context0sPreviousState[c];
			contextTotal[c] = contextTotalPreviousState[c];
			context0sWindow[c]  = context0sWindowPreviousState[c];
			contextProb0FLW[c] = contextProb0FLWPreviousState[c];
		}
	}
	
	/**
	 * Resets the state of all contexts.
	 */
	public void reset(){
		for(int c = 0; c < numContexts; c++){
			contextProb0FLW[c] = prob0ToFLW(0.66f, precisionBits); //Slightly biased towards 0
			context0s[c] = 2;
			context0sWindow[c] = -1;
			contextTotal[c] = 3;
		}
	}
	
	/**
	 * Transforms the probability of the symbol 0 (or false) in the range (0,1) into the integer
	 * required in the FLW coder to represent that probability.
	 *
	 * @param prob0 in the range [0:1]. If 0 or 1, the closest probability to 0/1 given the
	 * precision bits employed will be used --but it will not be 0 or 1.
	 * @param precisionBits number of bits employed to represent the probability in the
	 * returned integer
	 * @return integer that can be fed to the FLW coder. It is computed as (2^precisionBits) * prob0.
	 */
	public int prob0ToFLW(float prob0, int precisionBits){
		assert((prob0 >= 0f) && (prob0 <= 1f));

		int prob0FLW = (int) ((float) (1 << precisionBits) * prob0);
		if(prob0FLW == 0){
			prob0FLW = 1;
		}else if(prob0FLW == (1 << precisionBits)){
			prob0FLW = (1 << precisionBits) - 1;
		}
		assert((prob0FLW > 0) && (prob0FLW < (1 << precisionBits)));
		return(prob0FLW);
	}
	
	/**
	 * Sets the LUT Prob
	 * @param LUTProbQuantizer
	 */
	private void setLUTProb(){
		int WINDOW_PROB_bits = Integer.SIZE - Integer.numberOfLeadingZeros(WINDOW_PROB) + 1;
		LUTProbs = new int[1 << WINDOW_PROB_bits >> LUTProbQuantizer][1 << WINDOW_PROB_bits >> LUTProbQuantizer];
		for(int numerador = 1; numerador < LUTProbs.length; numerador++){
		for(int denominador = 1; denominador < LUTProbs.length; denominador++){
			LUTProbs[numerador][denominador] = (numerador << precisionBits) / denominador;
			if(LUTProbs[numerador][denominador] == 0) LUTProbs[numerador][denominador] = 1; // FIXME
			if(LUTProbs[numerador][denominador] == (1 << precisionBits)) LUTProbs[numerador][denominador] = (1 << precisionBits) - 1 ;
		}}
	}
	
	public void copy (ContextProbability another){
		
		for(int c = 0; c < numContexts; c++){
			this.context0s[c] = another.context0s[c];
			this.contextTotal[c] = another.contextTotal[c]; 
			this.context0sWindow[c]  = another.context0sWindow[c];
			this.contextProb0FLW[c] = another.contextProb0FLW[c];
		}
		this.probabilityModel = another.probabilityModel;
	}
}






