package GiciContextModel;

public class IntegerContextProbability {
	
	final int dynamicRange = 16;//2^16 = 65536
	
	/**
	 * Indicates the number of symbols coded before updating the context probability.
	 * <p>
	 * Must be of the form 2^X - 1.
	 */
	private static final int UPDATE_PROB0 = 1;

	/**
	 * Indicates the maximum number of symbols within the variable-size sliding window that are employed
	 * to compute the probability of the context.
	 * <p>
	 * Must be of the form 2^X - 1.
	 */
	private static final int WINDOW_PROB = 2047;

	
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
	private int[][] contextProbFLW = null;

	/**
	 * Number of 0s coded in this context.
	 * <p>
	 * It is initialized to 0 in the <code>reset</code> function.
	 */
	private int[][] contexts = null;

	/**
	 * Number of 0s coded in the last WINDOW_PROB symbols coded.
	 * <p>
	 * It is initialized to 0 in the <code>reset</code> function.
	 */
	private int[][] contextsWindow = null;

	/**
	 * Total number of symbols coded in this context.
	 * <p>
	 * It is initialized to 0 in the <code>reset</code> function.
	 */
	private int[] contextTotal = null;
	
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
	
	
	private int probd = 0;
	
	long LongMaxNumContexts = 0;
	
	private int dynamicrange = 0;
	
	public IntegerContextProbability(int probabilityModel, int precisionBits, int quantizerProbabilityLUT, int numContexts){
		
		assert(LongMaxNumContexts < Integer.MAX_VALUE - 1);
	
		this.numContexts = (int)Math.pow(numContexts,3);
		assert(this.numContexts < Integer.MAX_VALUE);
		
		dynamicrange = 3000;//put max value
		contextProbFLW = new int[this.numContexts][dynamicrange];
		contexts = new int[this.numContexts][dynamicrange];
		contextsWindow = new int[this.numContexts][dynamicrange];
		contextTotal = new int[this.numContexts];
		this.probabilityModel = probabilityModel;
		this.precisionBits = precisionBits;
		this.LUTProbQuantizer = quantizerProbabilityLUT;
		reset();
		probd = Integer.SIZE - Integer.numberOfLeadingZeros(UPDATE_PROB0 + 1);
	}
	
	/**
	 * Updates the probabilities, which adaptively adjusted
	 * depending on the incoming symbols. Only updates the probability of this context, when necessary
	 *
	 * @param bit input
	 * @param context of the symbol
	 */
	public int getProbability(int sample, int context, int maxCurrent, int maxPrevious1, int maxPrevious2){
		
		if(context == numContexts) context--;
		int prob = 0;
		switch(probabilityModel){
			case 0:
				if((contextTotal[context] & UPDATE_PROB0) == UPDATE_PROB0){
					if(contexts[context][sample] == 0){
						contextProbFLW[context][sample] = 1;
					}else if(contexts[context][sample] == contextTotal[context]){
						contextProbFLW[context][sample] = (1 << precisionBits) - 1;
					}else{
						assert(contexts[context][sample] * (double) (1 << precisionBits) <= Integer.MAX_VALUE);
						contextProbFLW[context][sample] = (contexts[context][sample] << precisionBits) / contextTotal[context];
						//System.out.println(contextProbFLW[context][sample]+" "+contexts[context][sample]+" "+ contextTotal[context]);
						
					}
					
					
					assert((contextProbFLW[context][sample] > 0) && (contextProbFLW[context][sample] < (1 << precisionBits)));
					
					if((contextTotal[context] & WINDOW_PROB) == WINDOW_PROB){
						boolean reset = true;
						
						for(int s = 0; s < contextsWindow[context].length; s++){
							if(contextsWindow[context][s] != -1){
								if(reset){
									reset = false;
									contextTotal[context] -= WINDOW_PROB;
								}
								contexts[context][s] -= contextsWindow[context][s];
							}
							contextsWindow[context][s] = contexts[context][s];
						}
					
					}
				}
				prob = contextProbFLW[context][sample];
				break;
	
			default:
				throw new Error();
		}
		
		return(prob);
	}
	
		
	/**
	 * Updates the number of symbols coded for this context.
	 * 
	 * @param bit input
	 * @param context of the symbol
	 */
	public void updateSymbols(int sample, int context){
		
		//Updates the number of symbols coded for this context
		contexts[context][sample]++;
		contextTotal[context]++;
	}
	
	
	/**
	 * Resets the state of all contexts.
	 */
	public void reset(){
		for(int c = 0; c < contextProbFLW.length; c++){
			for(int s = 0; s < contextProbFLW[c].length; s++){
				//contextProbFLW[c][s] = prob0ToFLW(0.66f, precisionBits); //Slightly biased towards 0
				contextProbFLW[c][s] = (1 << precisionBits) / dynamicrange;
				contexts[c][s] = 2;
				contextsWindow[c][s] = -1;
			}
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

	public void memoryAllocating() {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
