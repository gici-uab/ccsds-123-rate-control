package GiciContextModel;

public class IntegerContextModelling {

	/**
	 * Context Model employed
	 * <p>
	 * <ul>
	 * <li>0: No context model is used.</li>
	 * <li>1: 2 contexts are used. It check the Vertical neighbor.</li>
	 * <li>2: 2 contexts are used. It check the Left neighbor.</li>
	 * <li>3: 4 contexts are used. It check the Vertical and Left neighbor.</li>
	 * <li>4: 8 contexts are used. It check the Vertical, Left and Diagonal Upper-Left neighbor.</li>
	 * </ul>
	 * Each bit of this variable indicates whether the coding variation is employed or not. To use a selected combination of variations, sum up the numbers in front of each coding variation.
	 */
	private int contextModel = 0;
	
	private int numContexts = 0;
	
	/**
	 * Constructor of the ContextModelling. It receives and integer number that indicates the strategy used
	 * to compute the context model.
	 * 
	 * @param contextModel. 0 - no context model is used.
	 */
	public IntegerContextModelling(int contextModel, int numContexts){
		this.contextModel = contextModel;
		this.numContexts = numContexts;
	}
	
	public int getContext(int[][][] bands, int[][] predictedSamplesPrevious1, int[][] predictedSamplesPrevious2, int z, int y, int x, int qstepCurrent, int qstepPrevious1, int qstepPrevious2){
		long context = 0;
		
		if(numContexts == 1)	return(0);
		
		switch(contextModel){
			case 0:
				context = 0;
				break;
			case 1:
				if(x > 0 & y > 0){
					//context = (long)(bands[z][y][x-1]/qstepCurrent) + ((long)(numContexts)*(long)(predictedSamplesPrevious1[y][x]/qstepPrevious1)) + ((long)(numContexts)*2*(long)(predictedSamplesPrevious2[y][x]/qstepPrevious2));
					context = (long)(bands[z][y][x-1]/qstepCurrent) + ((long)(numContexts)*(long)(bands[z][y-1][x]/qstepCurrent)) + ((long)(numContexts)*2*(long)(predictedSamplesPrevious1[y][x]/qstepPrevious1));
				 
				}
				break;
			default:
				throw new Error();
		}
		
		assert(context < Math.pow(numContexts,3));
		
		return (int)context;
	}

	public int getNumContexts(){
		return (int) (Math.pow(numContexts,3));
	}
}
