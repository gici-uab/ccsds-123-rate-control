package GiciAnalysis;

import GiciException.WarningException;

public class ConsoleComparisonMap {
	public static String compare(float[][][] image1, float[][][] image2, boolean showOrigin) throws WarningException {		
		//Size set
		int zSize1 = image1.length;
		int ySize1 = image1[0].length;
		int xSize1 = image1[0][0].length;

		int zSize2 = image2.length;
		int ySize2 = image2[0].length;
		int xSize2 = image2[0][0].length;

		//Check if images have same sizes
		if((zSize1 != zSize2) || (ySize1 != ySize2) || (xSize1 != xSize2)){
			throw new WarningException("Images must have the same size to perform the comparison.");
		}

		// Result is 80 columns wide (including a 2 character frame) and as many rows as it needs
		int horizontalBins = 78;
		int pixelsPerBin = (xSize1 + horizontalBins - 1) / horizontalBins;
		int verticalBins = (ySize1 + pixelsPerBin - 1) / pixelsPerBin;
		horizontalBins = (xSize1 + pixelsPerBin - 1) / pixelsPerBin;
		
		// Find the differences (right now only accumulates spectrally)
		float[][] differences = new float[ySize1][xSize1];
		int[][] origins = new int[ySize1][xSize1];
				
		for (int i = 0; i < ySize1; i++) {
			for (int j = 0; j < xSize1; j++) {
				float error = Float.NEGATIVE_INFINITY;
				int origin = -1;
				
				for (int k = 0; k < zSize1; k++) {
					float d = Math.abs(image1[k][i][j] - image2[k][i][j]);
					
					if (error < d) {
						error = d;
						origin = k;
					}					
				}
				
				assert (error >= 0);
				
				differences[i][j] = error;
				origins[i][j] = origin;
			}
		}
		
		// Reduce size to horizontalBins x verticalBins
		float[][] values = new float[verticalBins][horizontalBins];
		int[][] binnedOrigins = new int[verticalBins][horizontalBins];
		

		for (int i = 0; i < verticalBins; i++) {
			for (int j = 0; j < horizontalBins; j++) {
				values[i][j] = Float.NEGATIVE_INFINITY;
				binnedOrigins[i][j] = -1;
			}
		}
		
		for (int i = 0; i < ySize1; i++) {
			for (int j = 0; j < xSize1; j++) {
				if (values[i / pixelsPerBin][j / pixelsPerBin] < differences[i][j]) {
					values[i / pixelsPerBin][j / pixelsPerBin] = differences[i][j];
					binnedOrigins[i / pixelsPerBin][j / pixelsPerBin] = origins[i][j];
				}
			}
		}	

		float min = Float.POSITIVE_INFINITY;
		float max = Float.NEGATIVE_INFINITY;
		
		for (int i = 0; i < verticalBins; i++) {
			for (int j = 0; j < horizontalBins; j++) {
				float v = values[i][j];
				min = Math.min(min, v);
				max = Math.max(max, v);	
			}
		}
			
		if (! showOrigin) {
			binnedOrigins = null;
		}
		
		return "pixelsPerBin = " + pixelsPerBin + "^2\n"
			+ map(values, min, max, binnedOrigins);
	}
	
	private static String repeat(char c, int count) {
		char[] r = new char[count];
		
		for (int i = 0; i < count; i++) {
			r[i] = c;
		}
		
		return new String(r);
	}

	
	public static String map(float[][] values, float min, float max, int[][] binnedOrigins) {
		
		int originPadding = 0;
		
		if (binnedOrigins != null) {
			originPadding = 2;
		}
		
		char[] symbols = {' ', '.', '+', 'x', 'X'}; 
				
		// Values below this threshold (included) are in this interval or less.
		float[] cuttingOffsets = new float[symbols.length];
			
		for (int i = 0; i < symbols.length; i++) {
			cuttingOffsets[i] = (float) Math.ceil((min + (max - min) * i / (symbols.length - 1)));
		}
		
		cuttingOffsets[0] = min;
		cuttingOffsets[symbols.length - 1] = max;
		
		// Trim intervals and display them
		String result = "ASCII map\n\t'" + symbols[0] + "' = [" + min + ", " + min + "]";
		
		for (int i = 1; i < symbols.length; i++) {
			float trimmedMin = Float.POSITIVE_INFINITY;
			float trimmedMax = Float.NEGATIVE_INFINITY;
			
			for (int y = 0; y < values.length; y++) {
				for (int x = 0; x < values[y].length; x++) {
					
					int pos;
					
					for (pos = 0; pos < cuttingOffsets.length; pos++) {
						if (values[y][x] <= cuttingOffsets[pos])
							break;
					}
					
					if (pos == i) {
						// Value is in this interval
						trimmedMin = Math.min(trimmedMin, values[y][x]);
						trimmedMax = Math.max(trimmedMax, values[y][x]);
					}
				}
			}
			
			//result += "\n\t'" + symbols[i] + "' = [" + realMin + ", " + realMax + "]";
			result += "\n\t'" + symbols[i] + "' = [" + trimmedMin + ", " + trimmedMax + "]"
				+ " C (" + cuttingOffsets[i - 1] + ", " + cuttingOffsets[i] + "]";
		}
		
		result += "\n/" + repeat('-', (values.length > 0? values[0].length : 0)) + "\\\n";
		
		int skip = 0;
		
		for (int i = 0; i < values.length; i++) {
			char[] r = new char[values[i].length * (1 + originPadding)];
			boolean empty = true;
			
			for (int j = 0; j < values[i].length; j++) {
				if (values[i][j] == min) {
					r[j] = symbols[0];
				} else {
					empty = false;
					
					assert (min < max);
//					int pos = (int)Math.ceil((values[i][j] - min) / (max - min) * (symbols.length - 1));
//					r[j] = symbols[Math.max(Math.min(pos + 1, symbols.length - 1), 1)];
					int pos;
					
					for (pos = 0; pos < cuttingOffsets.length; pos++) {
						if (values[i][j] <= cuttingOffsets[pos])
							break;
					}
					
					if (pos >= cuttingOffsets.length) {
						System.err.println("xx " + min + " " + max + " " + values[i][j]);
					}
					
					r[j * (1 + originPadding)] = symbols[pos];
					if (binnedOrigins != null) {
						String s = "          " + Integer.toString(binnedOrigins[i][j]);
						
						for (int k = 0; k < originPadding; k++) {
							r[j * (1 + originPadding) + k + 1] = s.charAt(s.length() - originPadding + k);
						}
					}
				}

			}
			
			if (! empty) {
				if (skip > 1) {
					result += "->" + skip + " lines skipped\n";
					skip = 0;
				} else if (skip == 1) {
					result += "|" + repeat(symbols[0], values[i].length) + "|\n";
					skip = 0;
				}
				
				result += "|" + new String(r) + "|\n";
			} else {
				skip++;
			}
		}
		
		if (skip > 1) {
			result += "->" + skip + " lines skipped\n";
			skip = 0;
		} else if (skip == 1) {
			result += "|" + repeat(symbols[0], (values.length > 0? values[values.length - 1].length : 0)) + "|\n";
			skip = 0;
		}
		
		result += "\\" + repeat('-', (values.length > 0? values[values.length - 1].length : 0)) + "/\n";
		
		return result;
	}
}

