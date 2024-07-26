package GiciContextModel;

import java.io.Serializable;

public class ContextModelling implements Serializable{

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
	private int numberOfContextsMagnitud = 0;
	private int numberOfContextsSign = 0;
	private int numberOfContexts = 0;

	/**
	 * This array is used to compute the context value.
	 */
	private boolean contextWindow [];
	
	/**
	 * Bit masks (employed when coding integers).
	 * <p>
	 * The position in the array indicates the bit for which the mask is computed.
	 */
	protected static final int[] BIT_MASKS2 = {1, 1 << 1, 1 << 2, 1 << 3, 1 << 4, 1 << 5, 1 << 6,
	1 << 7, 1 << 8, 1 << 9, 1 << 10, 1 << 11, 1 << 12, 1 << 13, 1 << 14, 1 << 15, 1 << 16, 1 << 17,
	1 << 18, 1 << 19, 1 << 20, 1 << 21, 1 << 22, 1 << 23, 1 << 24, 1 << 25, 1 << 26, 1 << 27,
	1 << 28, 1 << 29, 1 << 30, 1 << 31, 1 << 32, 1 << 33, 1 << 34, 1 << 35, 1 << 36};
	
	/**
	 * Constructor of the ContextModelling. It receives and integer number that indicates the strategy used
	 * to compute the context model.
	 * 
	 * @param contextModel. 0 - no context model is used.
	 */
	public ContextModelling(int contextModel){
		this.contextModel = contextModel;
	}
	

	
public int getContext(int[][] samples, int y, int x, int bit){
		
		//initialitize contexts
		if(contextModel != 0) for(int i = 0; i < contextWindow.length; i++) contextWindow[i] = false;
		
		switch(contextModel){
			case 0:
				contextWindow[0] = false;
				break;
			case 1:
				if(y > 0){
					if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
				}
				break;
			case 2:
				if(x > 0){
					if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[0] = true;
				}
				break;
			case 3:
				if(y > 0){
					if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
				}
				if(x > 0){
					if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[1] = true;
				}
				break;
			case 4:
				if(x > 0){
					if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[0] = true;
				}
				if(y > 0){
					if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[1] = true;
					if(x > 0){
						if(hasBeenSignificant(samples[y-1][x-1], bit)) contextWindow[2] = true;
					}
				}
				break;
			case 6:
				if(y > 0){
					if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
				}
				break;
			case 7:
				if(x > 0){
					if(isSignificant(samples[y][x-1], bit)) contextWindow[0] = true;
				}
				break;
			case 8:
				if(y > 0){
					if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
				}
				if(x > 0){
					if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[1] = true;
				}
				break;
			case 9:
				if(x > 0){
					if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[0] = true;
				}
				if(y > 0){
					if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[1] = true;
					if(x > 0){
						if(hasBeenSignificant(samples[y-1][x-1], bit)) contextWindow[2] = true;
					}
				}
				break;
			default:
				throw new Error();
		}
		
		int context = 0;
		for(int i = 0; i < contextWindow.length; i++){
			context += contextWindow[i] == true ? BIT_MASKS2[i] : 0;
		}
		if(contextModel != 0){
			context = context + (bit * BIT_MASKS2[contextWindow.length]);
		}
	
		return context;
	}


public int getContext(int[][][] samples, int z, int y, int x, int bit){
	
	//initialitize contexts
	if(contextModel != 0) for(int i = 0; i < contextWindow.length; i++) contextWindow[i] = false;
	
	switch(contextModel){
		case 0:
			contextWindow[0] = false;
			break;
		case 1:
			if(y > 0){
				if(hasBeenSignificant(samples[z][y-1][x], bit)) contextWindow[0] = true;
			}
			break;
		case 2:
			if(x > 0){
				if(hasBeenSignificant(samples[z][y][x-1], bit)) contextWindow[0] = true;
			}
			break;
		case 3:
			if(y > 0){
				if(hasBeenSignificant(samples[z][y-1][x], bit)) contextWindow[0] = true;
			}
			if(x > 0){
				if(hasBeenSignificant(samples[z][y][x-1], bit)) contextWindow[1] = true;
			}
			break;
		case 4:
			if(x > 0){
				if(hasBeenSignificant(samples[z][y][x-1], bit)) contextWindow[0] = true;
			}
			if(y > 0){
				if(hasBeenSignificant(samples[z][y-1][x], bit)) contextWindow[1] = true;
				if(x > 0){
					if(hasBeenSignificant(samples[z][y-1][x-1], bit)) contextWindow[2] = true;
				}
			}
			break;
		case 6:
			if(y > 0){
				if(hasBeenSignificant(samples[z][y-1][x], bit)) contextWindow[0] = true;
			}
			break;
		case 7:
			if(x > 0){
				if(isSignificant(samples[z][y][x-1], bit)) contextWindow[0] = true;
			}
			break;
		case 8:
			if(y > 0){
				if(hasBeenSignificant(samples[z][y-1][x], bit)) contextWindow[0] = true;
			}
			if(x > 0){
				if(hasBeenSignificant(samples[z][y][x-1], bit)) contextWindow[1] = true;
			}
			break;
		case 9:
			if(x > 0){
				if(hasBeenSignificant(samples[z][y][x-1], bit)) contextWindow[0] = true;
			}
			if(y > 0){
				if(hasBeenSignificant(samples[z][y-1][x], bit)) contextWindow[1] = true;
				if(x > 0){
					if(hasBeenSignificant(samples[z][y-1][x-1], bit)) contextWindow[2] = true;
				}
			}
			break;
		case 10:
			//gain = 0.07
			//vertical dist = -1
			if(y > 0){
				if(hasBeenSignificant(samples[z][y-1][x], bit)) contextWindow[0] = true;
			}

			//gain = 0.06
			//horitzontal dist = -1
			if(x > 0){
				if(hasBeenSignificant(samples[z][y][x-1], bit)) contextWindow[1] = true;
			}
			
			//gain = 0.04
			//diagonal dist = -1
			if(y > 0 & x > 0){
				if(hasBeenSignificant(samples[z][y-1][x-1], bit)) contextWindow[2] = true;
			}
			
			if(y > 0 & x < samples[0].length - 1){
				if(hasBeenSignificant(samples[z][y-1][x+1], bit)) contextWindow[3] = true;
			}

			

			if(bit < 15){
				
				//horitzontal dist = -1
				if(x > 0 & y == 0){
					if(hasBeenSignificant(samples[z][y][x-1], bit+1)) contextWindow[4] = true;
				}
				
				
				//vertical dist = -1
				if(y > 0 & x == 0){
					if(hasBeenSignificant(samples[z][y-1][x], bit+1)) contextWindow[4] = true;
				}
				
				if(y > 0 & x > 0 & y < samples.length - 1 & x < samples[0].length - 1){
				
					if(hasBeenSignificant(samples[z][y][x-1], bit+1)) contextWindow[4] = true;
					if(hasBeenSignificant(samples[z][y-1][x], bit+1))contextWindow[4] = true;
					if(hasBeenSignificant(samples[z][y-1][x-1], bit+1)) contextWindow[5] = true;
					if(hasBeenSignificant(samples[z][y-1][x+1], bit+1)) contextWindow[5] = true;
					if(hasBeenSignificant(samples[z][y][x+1], bit+1)) contextWindow[4] = true;
					if(hasBeenSignificant(samples[z][y][x], bit+1)) contextWindow[6] = true;
					
				}	
				
			}
			
		break;	
		case 11:
			//gain = 0.07
			//vertical dist = -1
			if(y > 0){
				if(hasBeenSignificant(samples[z][y-1][x], bit)) contextWindow[0] = true;
			}

			//gain = 0.06
			//horitzontal dist = -1
			if(x > 0){
				if(hasBeenSignificant(samples[z][y][x-1], bit)) contextWindow[1] = true;
			}
			
			//gain = 0.04
			//diagonal dist = -1
			if(y > 0 & x > 0){
				if(hasBeenSignificant(samples[z][y-1][x-1], bit)) contextWindow[2] = true;
			}
			
			if(y > 0 & x < samples[0].length - 1){
				if(hasBeenSignificant(samples[z][y-1][x+1], bit)) contextWindow[3] = true;
			}

			if(y > 1){
				if(hasBeenSignificant(samples[z][y-2][x], bit)) contextWindow[4] = true;
			}
			
			if(y > 1 & x > 0 ){
				if(hasBeenSignificant(samples[z][y-2][x], bit)) contextWindow[4] = true;
				if(hasBeenSignificant(samples[z][y-2][x-1], bit)) contextWindow[4] = true;
			}
		
			if(y > 1 & x > 0 & x < samples[0].length - 1){
				if(hasBeenSignificant(samples[z][y-2][x], bit)) contextWindow[4] = true;
				if(hasBeenSignificant(samples[z][y-2][x-1], bit)) contextWindow[4] = true;
				if(hasBeenSignificant(samples[z][y-2][x+1], bit)) contextWindow[4] = true;
			}
				
			if(y > 1 & x > 0 & x < samples[0].length - 2){
				if(hasBeenSignificant(samples[z][y-2][x], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[z][y-2][x-1], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[z][y-2][x+1], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[z][y-2][x+2], bit)) contextWindow[5] = true;
			}
			
			if(y > 1 & x > 1 & x < samples[0].length - 2){
				if(hasBeenSignificant(samples[z][y-2][x], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[z][y-2][x-1], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[z][y-2][x-2], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[z][y-2][x+1], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[z][y-2][x+2], bit)) contextWindow[5] = true;
			}

			if(bit < 15){
				//horitzontal dist = -1
				if(x > 0 & y == 0){
					if(hasBeenSignificant(samples[z][y][x-1], bit+1)) contextWindow[6] = true;
				}
				
				
				//vertical dist = -1
				if(y > 0 & x == 0){
					if(isSignificant(samples[z][y-1][x], bit+1)) contextWindow[6] = true;
				}
				
				if(y > 0 & x > 0 & y < samples.length - 1 & x < samples[0].length - 1){
					if(hasBeenSignificant(samples[z][y][x-1], bit+1)) contextWindow[6] = true;
					if(hasBeenSignificant(samples[z][y-1][x], bit+1))contextWindow[6] = true;
					if(hasBeenSignificant(samples[z][y-1][x-1], bit+1)) contextWindow[7] = true;
					if(hasBeenSignificant(samples[z][y-1][x+1], bit+1)) contextWindow[7] = true;
					if(hasBeenSignificant(samples[z][y][x+1], bit+1)) contextWindow[6] = true;
					if(hasBeenSignificant(samples[z][y][x], bit+1)) contextWindow[8] = true;
				}	
				
				
			}
			
			if(bit < 14){
				
				if(hasBeenSignificant(samples[z][y][x], bit+2)){ 
					contextWindow[12] = true;
				}
				
				if(y > 1){
					if(hasBeenSignificant(samples[z][y-1][x], bit+2)){ 
						contextWindow[12] = true;
					}
				}
				
				if(x > 0){
					if(hasBeenSignificant(samples[z][y][x-1], bit+2)){ 
						contextWindow[12] = true;
					}
				}
				
				if(y > 0 & x > 0){
					if(hasBeenSignificant(samples[z][y-1][x-1], bit+2)) contextWindow[12] = true;
				}
				
				if(y > 0 & x < samples[0].length - 1){
					if(hasBeenSignificant(samples[z][y-1][x+1], bit+2)) contextWindow[12] = true;
				}
				
				
			}
			
			if(bit < 13){
				
				if(hasBeenSignificant(samples[z][y][x], bit+3)){ 
					contextWindow[13] = true;
				}
			}
			
			if(bit < 12){
				
				if(hasBeenSignificant(samples[z][y][x], bit+4)){ 
					contextWindow[14] = true;
				}
			}
			
			if(bit < 11){
				
				if(hasBeenSignificant(samples[z][y][x], bit+5)){ 
					contextWindow[15] = true;
				}
			}
			
		break;
		
		case 12:
			//gain = 0.07
			//vertical dist = -1
			if(y > 0){
				if(hasBeenSignificant(samples[z][y-1][x], bit)) contextWindow[0] = true;
			}

			//gain = 0.06
			//horitzontal dist = -1
			if(x > 0){
				if(hasBeenSignificant(samples[z][y][x-1], bit)) contextWindow[1] = true;
			}
			
			//gain = 0.04
			//diagonal dist = -1
			if(y > 0 & x > 0){
				if(hasBeenSignificant(samples[z][y-1][x-1], bit)) contextWindow[2] = true;
			}
			
			if(y > 0 & x < samples[0].length - 1){
				if(hasBeenSignificant(samples[z][y-1][x+1], bit)) contextWindow[3] = true;
			}

			if(y > 1){
				if(hasBeenSignificant(samples[z][y-2][x], bit)) contextWindow[4] = true;
			}
			
			if(y > 1 & x > 0 ){
				if(hasBeenSignificant(samples[z][y-2][x], bit)) contextWindow[4] = true;
				if(hasBeenSignificant(samples[z][y-2][x-1], bit)) contextWindow[4] = true;
			}
		
			if(y > 1 & x > 0 & x < samples[0].length - 1){
				if(hasBeenSignificant(samples[z][y-2][x], bit)) contextWindow[4] = true;
				if(hasBeenSignificant(samples[z][y-2][x-1], bit)) contextWindow[4] = true;
				if(hasBeenSignificant(samples[z][y-2][x+1], bit)) contextWindow[4] = true;
			}
				
			if(y > 1 & x > 0 & x < samples[0].length - 2){
				if(hasBeenSignificant(samples[z][y-2][x], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[z][y-2][x-1], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[z][y-2][x+1], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[z][y-2][x+2], bit)) contextWindow[5] = true;
			}
			
			if(y > 1 & x > 1 & x < samples[0].length - 2){
				if(hasBeenSignificant(samples[z][y-2][x], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[z][y-2][x-1], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[z][y-2][x-2], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[z][y-2][x+1], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[z][y-2][x+2], bit)) contextWindow[5] = true;
			}

			if(bit < 15){
				//horitzontal dist = -1
				if(x > 0 & y == 0){
					if(hasBeenSignificant(samples[z][y][x-1], bit+1)) contextWindow[6] = true;
				}
				
				
				//vertical dist = -1
				if(y > 0 & x == 0){
					if(isSignificant(samples[z][y-1][x], bit+1)) contextWindow[6] = true;
				}
				
				if(y > 0 & x > 0 & y < samples.length - 1 & x < samples[0].length - 1){
					if(hasBeenSignificant(samples[z][y][x-1], bit+1)) contextWindow[6] = true;
					if(hasBeenSignificant(samples[z][y-1][x], bit+1))contextWindow[6] = true;
					if(hasBeenSignificant(samples[z][y-1][x-1], bit+1)) contextWindow[7] = true;
					if(hasBeenSignificant(samples[z][y-1][x+1], bit+1)) contextWindow[7] = true;
					if(hasBeenSignificant(samples[z][y][x+1], bit+1)) contextWindow[6] = true;
					if(hasBeenSignificant(samples[z][y][x], bit+1)) contextWindow[8] = true;
				}	
				
				
			}
			
			
			if(bit < 14){
				
				if(hasBeenSignificant(samples[z][y][x], bit+2)){ 
					contextWindow[9] = true;
				}
				
				if(y > 0){
					if(hasBeenSignificant(samples[z][y-1][x], bit+2)){ 
						contextWindow[9] = true;
					}
				}
				
				if(x > 0){
					if(hasBeenSignificant(samples[z][y][x-1], bit+2)){ 
						contextWindow[9] = true;
					}
				}
				
				if(y > 0 & x > 0){
					if(hasBeenSignificant(samples[z][y-1][x-1], bit+2)) contextWindow[9] = true;
				}
				
				if(y > 0 & x < samples[0].length - 1){
					if(hasBeenSignificant(samples[z][y-1][x+1], bit+2)) contextWindow[9] = true;
				}
				
				
			}

			if(z > 0){
				if(hasBeenSignificant(samples[z-1][y][x], bit)){ 
					contextWindow[10] = true;
				}
			}
			
			if(z > 1){
				if(hasBeenSignificant(samples[z-2][y][x], bit)){ 
					contextWindow[11] = true;
				}
			}
			break;
		
		default:
			throw new Error();
	}
	
	int context = 0;
	for(int i = 0; i < contextWindow.length; i++){
		context += contextWindow[i] == true ? BIT_MASKS2[i] : 0;
	}
	
	if(contextModel != 0){
		context = context + (bit * BIT_MASKS2[contextWindow.length]);
	}

	return context;
}

public int getContext(int[][] samples, int[][] predictedSamplesPrevious, int[][]predictedSamplesPrevious2, int z, int y, int x, int bit){
	//initialitize contexts
	if(contextModel != 0) for(int i = 0; i < contextWindow.length; i++) contextWindow[i] = false;
	
	switch(contextModel){
		case 0:
			contextWindow[0] = false;
			break;
		case 1:
			if(y > 0){
				if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
			}
			break;
		case 2:
			if(x > 0){
				if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[0] = true;
			}
			break;
		case 3:
			if(y > 0){
				if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
			}
			if(x > 0){
				if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[1] = true;
			}
			break;
		case 4:
			if(x > 0){
				if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[0] = true;
			}
			if(y > 0){
				if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[1] = true;
				if(x > 0){
					if(hasBeenSignificant(samples[y-1][x-1], bit)) contextWindow[2] = true;
				}
			}
			break;
		case 5:
			if(y > 0){
				if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
			}
			if(x > 0){
				if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[1] = true;
			}
			if(z > 0){
				if(hasBeenSignificant(predictedSamplesPrevious[y][x], bit)) contextWindow[2] = true;
			}
			break;
		case 6:
			if(x > 0){
				if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[0] = true;
			}
			if(y > 0){
				if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[1] = true;
				if(x > 0){
					if(hasBeenSignificant(samples[y-1][x-1], bit)) contextWindow[2] = true;
				}
			}
			if(z > 0){
				if(hasBeenSignificant(predictedSamplesPrevious[y][x], bit)) contextWindow[3] = true;
			}
			break;
		case 7:
			//gain = 0.07
			//vertical dist = -1
			if(y > 0){
				if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
			}

			//gain = 0.06
			//horitzontal dist = -1
			if(x > 0){
				if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[1] = true;
			}
			
			//gain = 0.04
			//diagonal dist = -1
			if(y > 0 & x > 0){
				if(hasBeenSignificant(samples[y-1][x-1], bit)) contextWindow[2] = true;
			}
			
			if(y > 0 & x < samples[0].length - 1){
				if(hasBeenSignificant(samples[y-1][x+1], bit)) contextWindow[3] = true;
			}

			if(y > 1){
				if(hasBeenSignificant(samples[y-2][x], bit)) contextWindow[4] = true;
			}
			
			if(y > 1 & x > 0 ){
				if(hasBeenSignificant(samples[y-2][x], bit)) contextWindow[4] = true;
				if(hasBeenSignificant(samples[y-2][x-1], bit)) contextWindow[4] = true;
			}
		
			if(y > 1 & x > 0 & x < samples[0].length - 1){
				if(hasBeenSignificant(samples[y-2][x], bit)) contextWindow[4] = true;
				if(hasBeenSignificant(samples[y-2][x-1], bit)) contextWindow[4] = true;
				if(hasBeenSignificant(samples[y-2][x+1], bit)) contextWindow[4] = true;
			}
				
			if(y > 1 & x > 0 & x < samples[0].length - 2){
				if(hasBeenSignificant(samples[y-2][x], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[y-2][x-1], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[y-2][x+1], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[y-2][x+2], bit)) contextWindow[5] = true;
			}
			
			if(y > 1 & x > 1 & x < samples[0].length - 2){
				if(hasBeenSignificant(samples[y-2][x], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[y-2][x-1], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[y-2][x-2], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[y-2][x+1], bit)) contextWindow[5] = true;
				if(hasBeenSignificant(samples[y-2][x+2], bit)) contextWindow[5] = true;
			}

			if(bit < 15){
				//horitzontal dist = -1
				if(x > 0 & y == 0){
					if(hasBeenSignificant(samples[y][x-1], bit+1)) contextWindow[6] = true;
				}
				
				
				//vertical dist = -1
				if(y > 0 & x == 0){
					if(isSignificant(samples[y-1][x], bit+1)) contextWindow[6] = true;
				}
				
				if(y > 0 & x > 0 & y < samples.length - 1 & x < samples[0].length - 1){
					if(hasBeenSignificant(samples[y][x-1], bit+1)) contextWindow[6] = true;
					if(hasBeenSignificant(samples[y-1][x], bit+1))contextWindow[6] = true;
					if(hasBeenSignificant(samples[y-1][x-1], bit+1)) contextWindow[7] = true;
					if(hasBeenSignificant(samples[y-1][x+1], bit+1)) contextWindow[7] = true;
					if(hasBeenSignificant(samples[y][x+1], bit+1)) contextWindow[6] = true;
					if(hasBeenSignificant(samples[y][x], bit+1)) contextWindow[8] = true;
				}	
				
				
			}

			
			
			if(z > 0){
				//previous band dist = 0
				if(hasBeenSignificant(predictedSamplesPrevious[y][x], bit)) contextWindow[9] = true;
				//horitzontal dist = -1
				if(x > 0){
					if(hasBeenSignificant(predictedSamplesPrevious[y][x-1], bit)) contextWindow[10] = true;
				}
				//horitzontal dist = +1
				if(x < predictedSamplesPrevious[0].length - 1){
					if(hasBeenSignificant(predictedSamplesPrevious[y][x+1], bit)) contextWindow[10] = true;
				}
				//vertical dist = -1
				if(y > 0){
					if(hasBeenSignificant(predictedSamplesPrevious[y-1][x], bit)) contextWindow[11] = true;
				}
				//vertical dist = +1
				if(y < predictedSamplesPrevious.length - 1){
					if(hasBeenSignificant(predictedSamplesPrevious[y+1][x], bit)) contextWindow[11] = true;
				}
				//vertical dist = -1 i horitzontal dist = -1
				if(y > 0 & x > 0){
					if(hasBeenSignificant(predictedSamplesPrevious[y-1][x-1], bit)) contextWindow[12] = true;
				}
				//vertical dist = +1 i horitzontal dist = +1
				if(y < predictedSamplesPrevious.length - 1 & x < predictedSamplesPrevious[0].length - 1){
					if(hasBeenSignificant(predictedSamplesPrevious[y+1][x+1], bit)) contextWindow[12] = true;
				}
				//vertical dist = -1 i horitzontal dist = +1
				if(y > 0 & x < predictedSamplesPrevious[0].length - 1){
					if(hasBeenSignificant(predictedSamplesPrevious[y-1][x+1], bit)) contextWindow[13] = true;
				}
				//vertical dist = +1 i horitzontal dist = -1
				if(y < predictedSamplesPrevious.length - 1 & x > 0){
					if(hasBeenSignificant(predictedSamplesPrevious[y+1][x-1], bit)) contextWindow[13] = true;
				}
				
				
			}
			
			if(z > 1){
				//previous band dist = 0
				if(hasBeenSignificant(predictedSamplesPrevious2[y][x], bit)) contextWindow[14] = true;
				//horitzontal dist = -1
				if(x > 0){
					if(hasBeenSignificant(predictedSamplesPrevious2[y][x-1], bit)) contextWindow[14] = true;
				}
				//horitzontal dist = +1
				if(x < predictedSamplesPrevious2[0].length - 1){
					if(hasBeenSignificant(predictedSamplesPrevious2[y][x+1], bit)) contextWindow[14] = true;
				}
				//vertical dist = -1
				if(y > 0){
					if(hasBeenSignificant(predictedSamplesPrevious2[y-1][x], bit)) contextWindow[14] = true;
				}
				//vertical dist = +1
				if(y < predictedSamplesPrevious2.length - 1){
					if(hasBeenSignificant(predictedSamplesPrevious2[y+1][x], bit)) contextWindow[14] = true;
				}
				//vertical dist = -1 i horitzontal dist = -1
				if(y > 0 & x > 0){
					if(hasBeenSignificant(predictedSamplesPrevious2[y-1][x-1], bit)) contextWindow[14] = true;
				}
				//vertical dist = +1 i horitzontal dist = +1
				if(y < predictedSamplesPrevious2.length - 1 & x < predictedSamplesPrevious2[0].length - 1){
					if(hasBeenSignificant(predictedSamplesPrevious2[y+1][x+1], bit)) contextWindow[14] = true;
				}
				//vertical dist = -1 i horitzontal dist = +1
				if(y > 0 & x < predictedSamplesPrevious2[0].length - 1){
					if(hasBeenSignificant(predictedSamplesPrevious2[y-1][x+1], bit)) contextWindow[14] = true;
				}
				//vertical dist = +1 i horitzontal dist = -1
				if(y < predictedSamplesPrevious2.length - 1 & x > 0){
					if(hasBeenSignificant(predictedSamplesPrevious2[y+1][x-1], bit)) contextWindow[14] = true;
				}
				
			}
			break;

		case 8:
			
			if(y > 0){
				if(isSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
			}
			
			//gain = 0.08
			//horitzontal dist = -1
			if(x > 0){
				if(isSignificant(samples[y][x-1], bit)) contextWindow[1] = true;
			}
			
			if(x < samples[0].length - 1){
				if(isSignificant(samples[y][x+1], bit)) contextWindow[1] = true;
			}
			
			//gain = 0.04
			//diagonal dist = -1
			if(y > 0 & x > 0){
				if(isSignificant(samples[y-1][x-1], bit)) contextWindow[2] = true;
			}
			
			//gain = 0.04
			//diagonal dist = -1
			if(y > 0 & x < samples[0].length - 1){
				if(isSignificant(samples[y-1][x+1], bit)) contextWindow[2] = true;
			}
			
			
			if(y > 1){
				if(isSignificant(samples[y-2][x], bit)) contextWindow[3] = true;
			}
			
			if(y > 1 & x > 0){
				if(isSignificant(samples[y-2][x-1], bit)) contextWindow[3] = true;
			}
			
			if(y > 1 & x < samples[0].length - 1){
				if(isSignificant(samples[y-2][x+1], bit)) contextWindow[3] = true;
			}
			
			if(x > 1){
				if(isSignificant(samples[y][x-2], bit)) contextWindow[4] = true;
			}
			
			if(y > 0 & x > 1){
				if(isSignificant(samples[y-1][x-2], bit)) contextWindow[4] = true;
			}
			
			if(y > 1 & x > 1){
				if(isSignificant(samples[y-2][x-2], bit)) contextWindow[4] = true;
			}
			
			if(x < samples[0].length - 2){
				if(isSignificant(samples[y][x+2], bit)) contextWindow[5] = true;
			}
			
			if(y > 0 & x < samples[0].length - 2){
				if(isSignificant(samples[y-1][x+2], bit)) contextWindow[5] = true;
			}
			
			if(y > 1 & x < samples[0].length - 2){
				if(isSignificant(samples[y-2][x+2], bit)) contextWindow[5] = true;
			}
			
			
			if(y > 2){
				if(isSignificant(samples[y-3][x], bit)) contextWindow[6] = true;
			}
			
			if(y > 2 & x > 0){
				if(isSignificant(samples[y-3][x-1], bit)) contextWindow[6] = true;
			}
			
			if(y > 2 & x > 1){
				if(isSignificant(samples[y-3][x-2], bit)) contextWindow[6] = true;
			}
			
			if(y > 2 & x < samples[0].length - 1){
				if(isSignificant(samples[y-3][x+1], bit)) contextWindow[6] = true;
			}
			
			if(y > 2 & x < samples[0].length - 2){
				if(isSignificant(samples[y-3][x+2], bit)) contextWindow[6] = true;
			}
			
			
			if(x > 2){
				if(isSignificant(samples[y][x-3], bit)) contextWindow[7] = true;
			}
			
			if(y > 0 & x > 2){
				if(isSignificant(samples[y-1][x-3], bit)) contextWindow[7] = true;
			}
			
			if(y > 1 & x > 2){
				if(isSignificant(samples[y-2][x-3], bit)) contextWindow[7] = true;
			}
			
			if(y > 2 & x > 2){
				if(isSignificant(samples[y-3][x-3], bit)) contextWindow[7] = true;
			}
			
			
			if(x < samples[0].length - 3){
				if(isSignificant(samples[y][x+3], bit)) contextWindow[8] = true;
			}
			
			if(y > 0 & x < samples[0].length - 3){
				if(isSignificant(samples[y-1][x+3], bit)) contextWindow[8] = true;
			}
			
			if(y > 1 & x < samples[0].length - 3){
				if(isSignificant(samples[y-2][x+3], bit)) contextWindow[8] = true;
			}
			
			if(y > 2 & x < samples[0].length - 3){
				if(isSignificant(samples[y-3][x+3], bit)) contextWindow[8] = true;
			}
			
			if(bit < 15){
				
				if(isSignificant(samples[y][x], bit+1)){ 
					contextWindow[9] = true;
				}
				
				if(y > 1){
					if(isSignificant(samples[y-1][x], bit+1)){ 
						contextWindow[10] = true;
					}
				}
				
				if(x > 0){
					if(isSignificant(samples[y][x-1], bit+1)){ 
						contextWindow[10] = true;
					}
				}
				
				if(y > 0 & x > 0){
					if(isSignificant(samples[y-1][x-1], bit+1)) contextWindow[11] = true;
				}
				
				if(y > 0 & x < samples[0].length - 1){
					if(isSignificant(samples[y-1][x+1], bit+1)) contextWindow[11] = true;
				}
				
			}
			
			if(bit < 14){
				
				if(isSignificant(samples[y][x], bit+2)){ 
					contextWindow[12] = true;
				}
				
				if(y > 1){
					if(isSignificant(samples[y-1][x], bit+2)){ 
						contextWindow[12] = true;
					}
				}
				
				if(x > 0){
					if(isSignificant(samples[y][x-1], bit+2)){ 
						contextWindow[12] = true;
					}
				}
				
				if(y > 0 & x > 0){
					if(isSignificant(samples[y-1][x-1], bit+2)) contextWindow[12] = true;
				}
				
				if(y > 0 & x < samples[0].length - 1){
					if(isSignificant(samples[y-1][x+1], bit+2)) contextWindow[12] = true;
				}
				
				
			}
			
			if(bit < 13){
				
				if(isSignificant(samples[y][x], bit+3)){ 
					contextWindow[13] = true;
				}
			}
			
			if(bit < 12){
				
				if(isSignificant(samples[y][x], bit+4)){ 
					contextWindow[14] = true;
				}
			}
			
			if(bit < 11){
				
				if(isSignificant(samples[y][x], bit+5)){ 
					contextWindow[15] = true;
				}
			}

			
			
			if(isSignificant(predictedSamplesPrevious[y][x], bit)) contextWindow[16] = true;
			if(bit < 15) if(isSignificant(predictedSamplesPrevious[y][x], bit+1)) contextWindow[16] = true;
			
			
			if(y > 0){
				if(isSignificant(predictedSamplesPrevious[y-1][x], bit)) contextWindow[17] = true;
				if(bit < 15) if(isSignificant(predictedSamplesPrevious[y-1][x], bit+1)) contextWindow[17] = true;
			}
			
			
			//gain = 0.08
			//horitzontal dist = -1
			if(x > 0){
				if(isSignificant(predictedSamplesPrevious[y][x-1], bit)) contextWindow[18] = true;
				if(bit < 15) if(isSignificant(predictedSamplesPrevious[y][x-1], bit+1)) contextWindow[18] = true;
			}
			
			

			
			int context = 0;
			for(int i = 0; i < contextWindow.length; i++){
				context += contextWindow[i] == true ? BIT_MASKS2[i] : 0;
			}
			//if(contextModel != 0){
			//	context = context + (bit * BIT_MASKS2[contextWindow.length]);
			//}
			return context;
			//break;
			
		case 9:
			//gain = 0.07
			//vertical dist = -1
			if(y > 0){
				if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
			}

			//gain = 0.06
			//horitzontal dist = -1
			if(x > 0){
				if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[1] = true;
			}
			
			//gain = 0.04
			//diagonal dist = -1
			if(y > 0 & x > 0){
				if(hasBeenSignificant(samples[y-1][x-1], bit)) contextWindow[2] = true;
			}
			
			if(y > 0 & x < samples[0].length - 1){
				if(hasBeenSignificant(samples[y-1][x+1], bit)) contextWindow[3] = true;
			}

			

			if(bit < 15){
				
				
				//horitzontal dist = -1
				if(x > 0 & y == 0){
					if(hasBeenSignificant(samples[y][x-1], bit+1)) contextWindow[4] = true;
				}
				
				
				//vertical dist = -1
				if(y > 0 & x == 0){
					if(hasBeenSignificant(samples[y-1][x], bit+1)) contextWindow[4] = true;
				}
				
				if(y > 0 & x > 0 & y < samples.length - 1 & x < samples[0].length - 1){
					if(hasBeenSignificant(samples[y][x-1], bit+1)) contextWindow[4] = true;
					if(hasBeenSignificant(samples[y-1][x], bit+1))contextWindow[4] = true;
					if(hasBeenSignificant(samples[y-1][x-1], bit+1)) contextWindow[5] = true;
					if(hasBeenSignificant(samples[y-1][x+1], bit+1)) contextWindow[5] = true;
					if(hasBeenSignificant(samples[y][x+1], bit+1)) contextWindow[4] = true;
					if(hasBeenSignificant(samples[y][x], bit+1)) contextWindow[6] = true;
					
				}	
				
				
			}
			
			break;
		
		case 10:
			
			//gain = 0.07
			//vertical dist = -1
			
			if(y > 0){
				if(isSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
			}
			if(x > 0){
				if(isSignificant(samples[y][x-1], bit)) contextWindow[1] = true;
			}
			if(y > 0 & x > 0){
				if(isSignificant(samples[y-1][x-1], bit)) contextWindow[2] = true;
			}
			if(y > 0 & x < samples[0].length - 1){
				if(isSignificant(samples[y-1][x+1], bit)) contextWindow[3] = true;
			}
			
			if(bit < 15){
				if(isSignificant(samples[y][x], bit+1)) contextWindow[4] = true;
				
				//horitzontal dist = -1
				if(x > 0 & y == 0){
					if(isSignificant(samples[y][x-1], bit+1)) contextWindow[4] = true;
				}
				
				
				//vertical dist = -1
				if(y > 0 & x == 0){
					if(isSignificant(samples[y-1][x], bit+1)) contextWindow[4] = true;
				}
				
				//gain 0.1 bps
				if(y > 0 & x > 0 & y < samples.length - 1 & x < samples[0].length - 1){
					if(isSignificant(samples[y][x-1], bit+1)) contextWindow[4] = true;
					if(isSignificant(samples[y-1][x], bit+1))contextWindow[4] = true;
					if(isSignificant(samples[y-1][x-1], bit+1)) contextWindow[5] = true;
					if(isSignificant(samples[y-1][x+1], bit+1)) contextWindow[5] = true;
					if(isSignificant(samples[y+1][x-1], bit+1)) contextWindow[5] = true;
					if(isSignificant(samples[y+1][x+1], bit+1)) contextWindow[5] = true;
					if(isSignificant(samples[y][x+1], bit+1)) contextWindow[4] = true;
					if(isSignificant(samples[y][x], bit+1)) contextWindow[6] = true;
				}	
				
				
			}
			
			if(bit < 14){
				if(x > 0 & y == 0){
					if(isSignificant(samples[y][x-1], bit+2)) contextWindow[7] = true;
				}
				
				if(y > 0 & x == 0){
					if(isSignificant(samples[y-1][x], bit+2)) contextWindow[7] = true;
				}
				
				if(y > 0 & x > 0 & y < samples.length - 1 & x < samples[0].length - 1){
					if(isSignificant(samples[y][x-1], bit+2)) contextWindow[7] = true;
					if(isSignificant(samples[y-1][x], bit+2))contextWindow[7] = true;
					if(isSignificant(samples[y-1][x-1], bit+2)) contextWindow[8] = true;
					if(isSignificant(samples[y-1][x+1], bit+2)) contextWindow[8] = true;
					if(isSignificant(samples[y+1][x-1], bit+2)) contextWindow[8] = true;
					if(isSignificant(samples[y+1][x+1], bit+2)) contextWindow[8] = true;
					if(isSignificant(samples[y][x+1], bit+2)) contextWindow[7] = true;
					if(isSignificant(samples[y][x], bit+2)) contextWindow[9] = true;
				}	
				
				
			}
			
			if(bit < 14){
				if(isSignificant(samples[y][x], bit+2)) contextWindow[10] = true;
				
			}
			
			if(bit < 13){
				if(isSignificant(samples[y][x], bit+3)) contextWindow[11] = true;
			}
			
			if(bit < 12){
				if(isSignificant(samples[y][x], bit+4)) contextWindow[12] = true;
			}
			
			
			
			break;
		
		case 11:
			//gain = 0.07
			//vertical dist = -1
			if(y > 0){
				if(isSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
			}

			//gain = 0.06
			//horitzontal dist = -1
			if(x > 0){
				if(isSignificant(samples[y][x-1], bit)) contextWindow[1] = true;
			}
			
			//gain = 0.04
			//diagonal dist = -1
			if(y > 0 & x > 0){
				if(isSignificant(samples[y-1][x-1], bit)) contextWindow[2] = true;
			}
			
			if(y > 0 & x < samples[0].length - 1){
				if(isSignificant(samples[y-1][x+1], bit)) contextWindow[3] = true;
			}

			if(y > 1){
				if(isSignificant(samples[y-2][x], bit)) contextWindow[4] = true;
			}
			
			if(y > 1 & x > 0){
				if(isSignificant(samples[y-2][x-1], bit)) contextWindow[4] = true;
			}
			
			if(y > 1 & x < samples[0].length - 1){
				if(isSignificant(samples[y-2][x+1], bit)) contextWindow[4] = true;
			}
			

			
			
			break;

		case 12:
			//gain = 0.07
			//vertical dist = -1
			if(y > 0){
				if(isSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
			}

			//gain = 0.06
			//horitzontal dist = -1
			if(x > 0){
				if(isSignificant(samples[y][x-1], bit)) contextWindow[1] = true;
			}
			
			//gain = 0.04
			//diagonal dist = -1
			if(y > 0 & x > 0){
				if(isSignificant(samples[y-1][x-1], bit)) contextWindow[2] = true;
			}
			
			if(y > 0 & x < samples[0].length - 1){
				if(isSignificant(samples[y-1][x+1], bit)) contextWindow[3] = true;
			}

			if(y > 1){
				if(isSignificant(samples[y-2][x], bit)) contextWindow[4] = true;
			}
			
			if(y > 1 & x > 0){
				if(isSignificant(samples[y-2][x-1], bit)) contextWindow[4] = true;
			}
			
			if(y > 1 & x < samples[0].length - 1){
				if(isSignificant(samples[y-2][x+1], bit)) contextWindow[4] = true;
			}
			
			if(bit < 15){
				
				if(isSignificant(samples[y][x], bit+1)) contextWindow[5] = true;
				//horitzontal dist = -1
				if(x > 0){
					if(isSignificant(samples[y][x-1], bit+1)) contextWindow[6] = true;
				}
				if(x < samples[0].length - 1){
					if(isSignificant(samples[y][x+1], bit+1)) contextWindow[6] = true;
				}
				if(y > 0){
					if(isSignificant(samples[y-1][x], bit+1)) contextWindow[6] = true;
				}
				if(y < samples.length - 1){
					if(isSignificant(samples[y+1][x], bit+1)) contextWindow[6] = true;
				}
				
				if(x > 0 && y > 0){
					if(isSignificant(samples[y-1][x-1], bit+1)) contextWindow[7] = true;
				}
				if(x > 0 && y < samples.length - 1){
					if(isSignificant(samples[y+1][x-1], bit+1)) contextWindow[7] = true;
				}
				if(x < samples[0].length - 1 && y > 0){
					if(isSignificant(samples[y-1][x+1], bit+1)) contextWindow[7] = true;
				}
				if(x < samples[0].length - 1 && y < samples.length - 1){
					if(isSignificant(samples[y+1][x+1], bit+1)) contextWindow[7] = true;
				}

			}
			
			/*if(bit < 15){
							
				//horitzontal dist = -1
				if(x > 0 & y == 0){
					if(isSignificant(samples[y][x-1], bit+1)) contextWindow[5] = true;
				}
				
				
				//vertical dist = -1
				if(y > 0 & x == 0){
					if(isSignificant(samples[y-1][x], bit+1)) contextWindow[5] = true;
				}
				
				if(y > 0 & x > 0 & y < samples.length - 1 & x < samples[0].length - 1){
				
					if(isSignificant(samples[y][x-1], bit+1)) contextWindow[5] = true;
					if(isSignificant(samples[y-1][x], bit+1))contextWindow[5] = true;
					if(isSignificant(samples[y-1][x-1], bit+1)) contextWindow[6] = true;
					if(isSignificant(samples[y-1][x+1], bit+1)) contextWindow[6] = true;
					if(isSignificant(samples[y][x+1], bit+1)) contextWindow[5] = true;
					if(isSignificant(samples[y][x], bit+1)) contextWindow[7] = true;
					
				}	

			}*/
			break;
		case 13:
			//gain = 0.07
			//vertical dist = -1
			if(y > 0){
				if(isSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
			}

			//gain = 0.06
			//horitzontal dist = -1
			if(x > 0){
				if(isSignificant(samples[y][x-1], bit)) contextWindow[1] = true;
			}
			
			//gain = 0.04
			//diagonal dist = -1
			if(y > 0 & x > 0){
				if(isSignificant(samples[y-1][x-1], bit)) contextWindow[2] = true;
			}
			
			if(y > 0 & x < samples[0].length - 1){
				if(isSignificant(samples[y-1][x+1], bit)) contextWindow[3] = true;
			}

			

			if(bit < 15){
				
				
				//horitzontal dist = -1
				if(x > 0 & y == 0){
					if(isSignificant(samples[y][x-1], bit+1)) contextWindow[4] = true;
				}
				
				
				//vertical dist = -1
				if(y > 0 & x == 0){
					if(isSignificant(samples[y-1][x], bit+1)) contextWindow[4] = true;
				}
				
				if(y > 0 & x > 0 & y < samples.length - 1 & x < samples[0].length - 1){
					if(isSignificant(samples[y][x-1], bit+1)) contextWindow[4] = true;
					if(isSignificant(samples[y-1][x], bit+1))contextWindow[4] = true;
					if(isSignificant(samples[y-1][x-1], bit+1)) contextWindow[5] = true;
					if(isSignificant(samples[y-1][x+1], bit+1)) contextWindow[5] = true;
					if(isSignificant(samples[y+1][x-1], bit+1)) contextWindow[5] = true;
					if(isSignificant(samples[y+1][x+1], bit+1)) contextWindow[5] = true;
					if(isSignificant(samples[y][x+1], bit+1)) contextWindow[4] = true;
					if(isSignificant(samples[y][x], bit+1)) contextWindow[6] = true;
						
				}	
				
				
			}
			
			if(z > 0){
				//previous band dist = 0
				if(isSignificant(predictedSamplesPrevious[y][x], bit)) contextWindow[7] = true;
				//horitzontal dist = -1
				if(x > 0){
					if(isSignificant(predictedSamplesPrevious[y][x-1], bit)) contextWindow[8] = true;
				}
				//horitzontal dist = +1
				if(x < predictedSamplesPrevious[0].length - 1){
					if(isSignificant(predictedSamplesPrevious[y][x+1], bit)) contextWindow[8] = true;
				}
				//vertical dist = -1
				if(y > 0){
					if(isSignificant(predictedSamplesPrevious[y-1][x], bit)) contextWindow[8] = true;
				}
				//vertical dist = +1
				if(y < predictedSamplesPrevious.length - 1){
					if(isSignificant(predictedSamplesPrevious[y+1][x], bit)) contextWindow[8] = true;
				}
				//vertical dist = -1 i horitzontal dist = -1
				if(y > 0 & x > 0){
					if(isSignificant(predictedSamplesPrevious[y-1][x-1], bit)) contextWindow[9] = true;
				}
				//vertical dist = +1 i horitzontal dist = +1
				if(y < predictedSamplesPrevious.length - 1 & x < predictedSamplesPrevious[0].length - 1){
					if(isSignificant(predictedSamplesPrevious[y+1][x+1], bit)) contextWindow[9] = true;
				}
				//vertical dist = -1 i horitzontal dist = +1
				if(y > 0 & x < predictedSamplesPrevious[0].length - 1){
					if(isSignificant(predictedSamplesPrevious[y-1][x+1], bit)) contextWindow[9] = true;
				}
				//vertical dist = +1 i horitzontal dist = -1
				if(y < predictedSamplesPrevious.length - 1 & x > 0){
					if(isSignificant(predictedSamplesPrevious[y+1][x-1], bit)) contextWindow[9] = true;
				}
				
				
			}
			
			if(z > 1){
				//previous band dist = 0
				if(isSignificant(predictedSamplesPrevious2[y][x], bit)) contextWindow[10] = true;
				//horitzontal dist = -1
				if(x > 0){
					if(isSignificant(predictedSamplesPrevious2[y][x-1], bit)) contextWindow[10] = true;
				}
				//horitzontal dist = +1
				if(x < predictedSamplesPrevious2[0].length - 1){
					if(isSignificant(predictedSamplesPrevious2[y][x+1], bit)) contextWindow[10] = true;
				}
				//vertical dist = -1
				if(y > 0){
					if(isSignificant(predictedSamplesPrevious2[y-1][x], bit)) contextWindow[10] = true;
				}
				//vertical dist = +1
				if(y < predictedSamplesPrevious2.length - 1){
					if(isSignificant(predictedSamplesPrevious2[y+1][x], bit)) contextWindow[10] = true;
				}
				//vertical dist = -1 i horitzontal dist = -1
				if(y > 0 & x > 0){
					if(isSignificant(predictedSamplesPrevious2[y-1][x-1], bit)) contextWindow[10] = true;
				}
				//vertical dist = +1 i horitzontal dist = +1
				if(y < predictedSamplesPrevious2.length - 1 & x < predictedSamplesPrevious2[0].length - 1){
					if(isSignificant(predictedSamplesPrevious2[y+1][x+1], bit)) contextWindow[10] = true;
				}
				//vertical dist = -1 i horitzontal dist = +1
				if(y > 0 & x < predictedSamplesPrevious2[0].length - 1){
					if(isSignificant(predictedSamplesPrevious2[y-1][x+1], bit)) contextWindow[10] = true;
				}
				//vertical dist = +1 i horitzontal dist = -1
				if(y < predictedSamplesPrevious2.length - 1 & x > 0){
					if(isSignificant(predictedSamplesPrevious2[y+1][x-1], bit)) contextWindow[10] = true;
				}
			}
			break;
			
					
		default:
			throw new Error();
	}
	
	int context = 0;
	for(int i = 0; i < contextWindow.length; i++){
		context += contextWindow[i] == true ? BIT_MASKS2[i] : 0;
	}
	
	/*if(contextModel != 0){
		context = context + (bit * BIT_MASKS2[contextWindow.length]);
	}*/
	
	return context;
	
	
}


public int getContext(int[][] samples, int[][] predictedSamplesPrevious, int[][]predictedSamplesPrevious2, int z, int y, int x, int bit, int[][] samplesClasses){
	
	//initialitize contexts
	if(contextModel != 0) for(int i = 0; i < contextWindow.length; i++) contextWindow[i] = false;
	
	int verticalOffset = searchVerticalOffset(samples, y, x, samplesClasses);
	int leftOffset = searchLeftOffset(samples, y, x, samplesClasses);
	int rightOffset = searchRightOffset(samples, y, x, samplesClasses);
	//int rightOffset = 1;
	
	
	//if(leftOffset != 1)System.out.println(y+" "+x+" "+leftOffset+" "+verticalOffset);
	switch(contextModel){
		
		

		case 13:
			if(y > 0){
				if(isSignificant(samples[y-verticalOffset][x], bit)) contextWindow[0] = true;
			}
			if(x > 0){
				if(isSignificant(samples[y][x-1], bit)) contextWindow[1] = true;
			}
			if(y > 0 & x > 0){
				if(isSignificant(samples[y-verticalOffset][x-1], bit)) contextWindow[2] = true;
			}
			
			
			if(bit < 15){
				if(isSignificant(samples[y][x], bit+1)) contextWindow[3] = true;
				
				//horitzontal dist = -1
				if(x > 0){
					if(isSignificant(samples[y][x-1], bit+1)) contextWindow[4] = true;
				}
				
				
				//vertical dist = -1
				if(y > 0){
					if(isSignificant(samples[y-verticalOffset][x], bit+1)) contextWindow[4] = true;
				}
				
				//gain 0.1 bps
				if(y > 0 & x > 0 & y < samples.length - 1 & x < samples[0].length - 1){
					if(isSignificant(samples[y][x-1], bit+1)) contextWindow[4] = true;
					if(isSignificant(samples[y-verticalOffset][x], bit+1))contextWindow[4] = true;
					if(isSignificant(samples[y-verticalOffset][x-1], bit+1)) contextWindow[5] = true;
					if(isSignificant(samples[y-verticalOffset][x+1], bit+1)) contextWindow[5] = true;
					if(isSignificant(samples[y+1][x-1], bit+1)) contextWindow[5] = true;
					if(isSignificant(samples[y+1][x+1], bit+1)) contextWindow[5] = true;
					if(isSignificant(samples[y][x+1], bit+1)) contextWindow[4] = true;
					if(isSignificant(samples[y][x], bit+1)) contextWindow[6] = true;
				}	
				
				
			}
			
			if(bit < 14){
				if(x > 0 & y == 0){
					if(isSignificant(samples[y][x-1], bit+2)) contextWindow[7] = true;
				}
				
				if(y > 0 & x == 0){
					if(isSignificant(samples[y-verticalOffset][x], bit+2)) contextWindow[7] = true;
				}
				
				if(y > 0 & x > 0 & y < samples.length - 1 & x < samples[0].length - 1){
					if(isSignificant(samples[y][x-1], bit+2)) contextWindow[7] = true;
					if(isSignificant(samples[y-verticalOffset][x], bit+2))contextWindow[7] = true;
					if(isSignificant(samples[y-verticalOffset][x-1], bit+2)) contextWindow[8] = true;
					if(isSignificant(samples[y-verticalOffset][x+1], bit+2)) contextWindow[8] = true;
					if(isSignificant(samples[y+1][x-1], bit+2)) contextWindow[8] = true;
					if(isSignificant(samples[y+1][x+1], bit+2)) contextWindow[8] = true;
					if(isSignificant(samples[y][x+1], bit+2)) contextWindow[7] = true;
					if(isSignificant(samples[y][x], bit+2)) contextWindow[9] = true;
				}	
				
				
			}
			
			if(bit < 14){
				if(isSignificant(samples[y][x], bit+2)) contextWindow[10] = true;
				
			}
			
			if(bit < 13){
				if(isSignificant(samples[y][x], bit+3)) contextWindow[11] = true;
			}
			
			if(bit < 12){
				if(isSignificant(samples[y][x], bit+4)) contextWindow[12] = true;
			}
			
			break;
			
		
			
		default:
			throw new Error();
	}
	
	int context = 0;
	for(int i = 0; i < contextWindow.length; i++){
		context += contextWindow[i] == true ? BIT_MASKS2[i] : 0;
	}
	
	if(contextModel != 0){
		context = context + (bit * BIT_MASKS2[contextWindow.length]);
	}
	
	return context;
	
	
}


	private int searchVerticalOffset(int[][] samples, int y, int x, int[][] samplesClasses) {
		int inputClass = samplesClasses[y][x];
		int outputClass = -1;
		int offset = 0;
		int min = 999999;
		int minoffset = -1;
		//System.out.println(y+" "+x+" "+offset+" "+samplesClasses[y][x]+" "+outputClass);
		do {
			if(y == offset) {
				outputClass = inputClass;
				offset = minoffset;
			}else {
				offset++;
				outputClass = samplesClasses[y-offset][x];
				if(Math.abs(inputClass - outputClass) <= min) {
					min = outputClass;
					minoffset = offset;
				}
			}
			
			 
		}while(inputClass != outputClass);
		//System.out.println(y+" "+x+" "+offset+" "+samplesClasses[y][x]+" "+outputClass);
	return offset;
}



	private int searchLeftOffset(int[][] samples, int y, int x, int[][] samplesClasses) {
		int inputClass = samplesClasses[y][x];
		int outputClass = -1;
		int offset = 0;
		int min = 999999;
		int minoffset = -1;
		//System.out.println(y+" "+x+" "+offset+" "+samplesClasses[y][x]+" "+outputClass);
		do {
			if(x == offset) {
				outputClass = inputClass;
				offset = minoffset;
			}else {
				offset++;
				outputClass = samplesClasses[y][x-offset];
				if(Math.abs(inputClass - outputClass) <= min) {
					min = outputClass;
					minoffset = offset;
				}
			}
			
			 
		}while(inputClass != outputClass);
		//System.out.println(y+" "+x+" "+offset+" "+samplesClasses[y][x]+" "+outputClass);
	return offset;
}


	private int searchRightOffset(int[][] samples, int y, int x, int[][] samplesClasses) {
		int inputClass = samplesClasses[y][x];
		int outputClass = -1;
		int offset = 0;
		int min = 999999;
		int minoffset = -1;
		//System.out.println(y+" "+x+" "+offset+" "+samplesClasses[y][x]+" "+outputClass);
		do {
			if(samples[y].length >= offset+y) {
				outputClass = inputClass;
				offset = minoffset;
			}else {
				offset++;
				outputClass = samplesClasses[y+offset][x];
				if(Math.abs(inputClass - outputClass) <= min) {
					min = outputClass;
					minoffset = offset;
				}
			}
			
			 
		}while(inputClass != outputClass);
		//System.out.println(y+" "+x+" "+offset+" "+samplesClasses[y][x]+" "+outputClass);
	return offset;
}
	

	public int getContext(int[][] samples, int[][] predictedSamplesPrevious1, int z, int y, int x, int bit){
		
		//initialitize contexts
		if(contextModel != 0) for(int i = 0; i < contextWindow.length; i++) contextWindow[i] = false;
		
		switch(contextModel){
			case 0:
				contextWindow[0] = false;
				break;
			case 1:
				if(y > 0){
					if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
				}
				break;
			case 2:
				if(x > 0){
					if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[0] = true;
				}
				break;
			case 3:
				if(y > 0){
					if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
				}
				if(x > 0){
					if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[1] = true;
				}
				break;
			case 4:
				if(x > 0){
					if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[0] = true;
				}
				if(y > 0){
					if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[1] = true;
					if(x > 0){
						if(hasBeenSignificant(samples[y-1][x-1], bit)) contextWindow[2] = true;
					}
				}
				break;
			case 5:
				if(y > 0){
					if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
				}
				if(z > 0){
					if(hasBeenSignificant(predictedSamplesPrevious1[y][x], bit)) contextWindow[1] = true;
				}
				break;
			case 6:
				if(x > 0){
					if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[0] = true;
				}
				if(z > 0){
					if(hasBeenSignificant(predictedSamplesPrevious1[y][x], bit)) contextWindow[1] = true;
				}
				break;
			case 7:
				if(y > 0){
					if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
				}
				if(x > 0){
					if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[1] = true;
				}
				if(z > 0){
					if(hasBeenSignificant(predictedSamplesPrevious1[y][x], bit)) contextWindow[2] = true;
				}
				break;
			case 8:
				if(y > 0){
					if(hasBeenSignificant(samples[y-1][x], bit)) contextWindow[0] = true;
					if(x > 0){
							if(hasBeenSignificant(samples[y-1][x-1], bit)) contextWindow[2] = true;
					}
				}
				if(x > 0){
					if(hasBeenSignificant(samples[y][x-1], bit)) contextWindow[1] = true;
				}
				if(z > 0){
					if(hasBeenSignificant(predictedSamplesPrevious1[y][x], bit)) contextWindow[3] = true;
				}
				break;
			case 9:
				if(z > 0){
					if(hasBeenSignificant(predictedSamplesPrevious1[y][x], bit)) contextWindow[0] = true;
				}
				break;
			default:
				throw new Error();
		}
		
		int context = 0;
		for(int i = 0; i < contextWindow.length; i++){
			context += contextWindow[i] == true ? BIT_MASKS2[i] : 0;
		}
		if(contextModel != 0){
			context = context + (bit * BIT_MASKS2[contextWindow.length]);
		}
	
		return context;
	}

	/**
	 * returns true if the value i has bit significant before or in the bitplane bit.
	 * @param i
	 * @param bit
	 * @return
	 */
	private boolean hasBeenSignificant(int value, int bit) {
		for(int bitplane = 15; bitplane >= bit; bitplane--){
			if((value & BIT_MASKS2[bit]) != 0) return true;
		}
		return false;
	}
	
	/**
	 * returns true if the value i is significant in the current bitplane bit
	 * @param i
	 * @param bit
	 * @return
	 */
	private boolean isSignificant(int value, int bit) {
		if((value & BIT_MASKS2[bit]) != 0) return true;
		return false;
	}
	
	
	/**
	 * This function returns the number of contexts used for each bitplane. Note that CCSDS the number of bitplanes to be coded are 16.
	 * @return
	 */
	public int getNumberOfContexts(int MAXBITS){
		int numOfContexts = 0;
		switch(contextModel){
			case 0:
				numOfContexts = 1;
				contextWindow = new boolean[1];
				break;
			case 1:
				numOfContexts = 2+2;
				contextWindow = new boolean[1];
				break;
			case 2:
				numOfContexts = 2;
				contextWindow = new boolean[1];
				break;
			case 3:
				numOfContexts = 4;
				contextWindow = new boolean[2];
				break;
			case 4:
				contextWindow = new boolean[3];
				numOfContexts = 8;
				break;
			case 5:
				contextWindow = new boolean[3];
				numOfContexts = 8;
				break;
			case 6:
				numOfContexts = 16;
				contextWindow = new boolean[4];
				break;
			case 7:
				numOfContexts = 32768;
				contextWindow = new boolean[15];
				break;
			case 8:
				numOfContexts = 32768;
				contextWindow = new boolean[32];
				break;
			case 9:
				contextWindow = new boolean[7];
				numOfContexts = 128;
				break;
			case 10:
				contextWindow = new boolean[13];
				numOfContexts = 1 << 13;
				break;
			case 11:
				contextWindow = new boolean[16];
				numOfContexts = 1 << 16;
				break;
			case 12:
				contextWindow = new boolean[12];
				numOfContexts = 1 << 12;
				break;
			case 13:
				contextWindow = new boolean[14];
				numOfContexts = 1 << 14;
				break;
			default:
				throw new Error();
		}
		// TODO check this
		//this.numberOfContextsMagnitud = (numOfContexts*MAXBITS)+1;
		//this.numberOfContextsSign = 5;
		//numberOfContexts = numberOfContextsMagnitud + numberOfContextsSign;
		// TODO check this
		numberOfContexts = (numOfContexts*16)+1; 
		return numberOfContexts;
		
	}

	

	public int getContextSignificance(int[][] statusMapSignificance, int y, int x) {
		int contextSign = -1;
		int verticalCounter = 0;
		int horizontalCounter = 0;
		if(y == 0){
			if(x == 0){
				verticalCounter += statusMapSignificance[y+1][x];
				horizontalCounter += statusMapSignificance[y][x+1];
			}else{
				if(x == statusMapSignificance[y].length - 1){
					verticalCounter += statusMapSignificance[y+1][x];
					horizontalCounter += statusMapSignificance[y][x-1];
				}else{
					verticalCounter += statusMapSignificance[y+1][x];
					horizontalCounter += statusMapSignificance[y][x+1];
					horizontalCounter += statusMapSignificance[y][x-1];
				}
			}
		}else{
			if(y == statusMapSignificance.length - 1){
				if(x == 0){
					verticalCounter += statusMapSignificance[y-1][x];
					horizontalCounter += statusMapSignificance[y][x+1];
				}else{
					if(x == statusMapSignificance[y].length - 1){
						verticalCounter += statusMapSignificance[y-1][x];
						horizontalCounter += statusMapSignificance[y][x-1];
					}else{
						verticalCounter += statusMapSignificance[y-1][x];
						horizontalCounter += statusMapSignificance[y][x+1];
						horizontalCounter += statusMapSignificance[y][x-1];
					}
				}
			}else{
				if(x == 0){
					verticalCounter += statusMapSignificance[y-1][x];
					verticalCounter += statusMapSignificance[y+1][x];
					horizontalCounter += statusMapSignificance[y][x+1];
				}else{
					if(x == statusMapSignificance[y].length - 1){
						verticalCounter += statusMapSignificance[y-1][x];
						verticalCounter += statusMapSignificance[y+1][x];
						horizontalCounter += statusMapSignificance[y][x-1];
					}else{
						verticalCounter += statusMapSignificance[y-1][x];
						verticalCounter += statusMapSignificance[y+1][x];
						horizontalCounter += statusMapSignificance[y][x+1];
						horizontalCounter += statusMapSignificance[y][x-1];
					}
				}
				
			}
		}
		
	    if( (verticalCounter > 0 && horizontalCounter > 0) || (verticalCounter < 0 && horizontalCounter < 0)) contextSign = 0;
		else{
			if(verticalCounter == 0  && horizontalCounter != 0) contextSign = 1;
			else{
				if(verticalCounter != 0  && horizontalCounter == 0) contextSign = 2;
				else{
					contextSign = 3;
				}
			}
		}
		return (numberOfContextsMagnitud+verticalCounter+horizontalCounter);
	}
	
	public int getContextSign(int[][] statusMapSign, int y, int x) {
		int contextSign = -1;
		int verticalCounter = 0;
		int horizontalCounter = 0;
		if(y == 0){
			if(x == 0){
				verticalCounter += statusMapSign[y+1][x];
				horizontalCounter += statusMapSign[y][x+1];
			}else{
				if(x == statusMapSign[y].length - 1){
					verticalCounter += statusMapSign[y+1][x];
					horizontalCounter += statusMapSign[y][x-1];
				}else{
					verticalCounter += statusMapSign[y+1][x];
					horizontalCounter += statusMapSign[y][x+1];
					horizontalCounter += statusMapSign[y][x-1];
				}
			}
		}else{
			if(y == statusMapSign.length - 1){
				if(x == 0){
					verticalCounter += statusMapSign[y-1][x];
					horizontalCounter += statusMapSign[y][x+1];
				}else{
					if(x == statusMapSign[y].length - 1){
						verticalCounter += statusMapSign[y-1][x];
						horizontalCounter += statusMapSign[y][x-1];
					}else{
						verticalCounter += statusMapSign[y-1][x];
						horizontalCounter += statusMapSign[y][x+1];
						horizontalCounter += statusMapSign[y][x-1];
					}
				}
			}else{
				if(x == 0){
					verticalCounter += statusMapSign[y-1][x];
					verticalCounter += statusMapSign[y+1][x];
					horizontalCounter += statusMapSign[y][x+1];
				}else{
					if(x == statusMapSign[y].length - 1){
						verticalCounter += statusMapSign[y-1][x];
						verticalCounter += statusMapSign[y+1][x];
						horizontalCounter += statusMapSign[y][x-1];
					}else{
						verticalCounter += statusMapSign[y-1][x];
						verticalCounter += statusMapSign[y+1][x];
						horizontalCounter += statusMapSign[y][x+1];
						horizontalCounter += statusMapSign[y][x-1];
					}
				}
				
			}
		}
		
	    if( (verticalCounter > 0 && horizontalCounter > 0) || (verticalCounter < 0 && horizontalCounter < 0)) contextSign = 0;
		else{
			if(verticalCounter == 0  && horizontalCounter != 0) contextSign = 1;
			else{
				if(verticalCounter != 0  && horizontalCounter == 0) contextSign = 2;
				else{
					contextSign = 3;
				}
			}
		}
		return (numberOfContextsMagnitud+verticalCounter+horizontalCounter);
	}

	int getContextModel() {
		return(this.contextModel);
	}

}
