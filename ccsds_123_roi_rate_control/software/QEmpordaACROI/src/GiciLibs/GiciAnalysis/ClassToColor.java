package GiciAnalysis;

import java.awt.Color;
import java.util.concurrent.Callable;

import GiciException.WarningException;

/**
 * Converts a classification to three component color image
 * @author Gici
 *
 */

public class ClassToColor implements Callable<float[][][]> {

	final String[] colorStrings = {
			"#FF0000",		"#00FF00",		"#0000FF",		"#FF00FF",		"#00FFFF",
			"#FFFF00",		"#000000",		"#70DB93",		"#5C3317",		"#9F5F9F",
			"#B5A642",		"#D9D919",		"#A62A2A",		"#8C7853",		"#A67D3D",
			"#5F9F9F",		"#D98719",		"#B87333",		"#FF7F00",		"#42426F",
			"#5C4033",		"#2F4F2F",		"#4A766E",		"#4F4F2F",		"#9932CD",
			"#871F78",		"#6B238E",		"#2F4F4F",		"#97694F",		"#7093DB",
			"#855E42",		"#545454",		"#856363",		"#D19275",		"#8E2323",
			"#F5CCB0",		"#238E23",		"#CD7F32",		"#DBDB70",		"#C0C0C0",
			"#527F76",		"#93DB70",		"#215E21",		"#4E2F2F",		"#9F9F5F",
			"#C0D9D9",		"#A8A8A8",		"#8F8FBD",		"#E9C2A6",		"#32CD32",
			"#E47833",		"#8E236B",		"#32CD99",		"#3232CD",		"#6B8E23",
			"#EAEAAE",		"#9370DB",		"#426F42",		"#7F00FF",		"#7FFF00",
			"#70DBDB",		"#DB7093",		"#A68064",		"#2F2F4F",		"#23238E",
			"#4D4DFF",		"#FF6EC7",		"#00009C",		"#EBC79E",		"#CFB53B",
			"#FF7F00",		"#FF2400",		"#DB70DB",		"#8FBC8F",		"#BC8F8F",
			"#EAADEA",		"#D9D9F3",		"#5959AB",		"#6F4242",		"#8C1717",
			"#238E68",		"#6B4226",		"#8E6B23",		"#E6E8FA",		"#3299CC",
			"#007FFF",		"#FF1CAE",		"#00FF7F",		"#236B8E",		"#38B0DE",
			"#DB9370",		"#D8BFD8",		"#ADEAEA",		"#5C4033",		"#CDCDCD",
			"#4F2F4F",		"#CC3299",		"#D8D8BF",		"#99CC32", };

	final float[][] colors;
	
	final int [][] c;
	
	private float[][] convertColors(float scale) {
		/* convert colors */

		float[][] colors = new float[colorStrings.length][];
		
		for (int i = 0; i < colorStrings.length; i++) {
			Color color = Color.decode(colorStrings[i]);			
			colors[i] = color.getRGBColorComponents(null);
			
			/* perhaps some scaling here ? */
			for (int j = 0; j < colors[i].length; j++) {
				colors[i][j] *= scale;
			}
		}
		
		return colors;
	}
	
	public ClassToColor (final int [] c, int y, int x, float scale) {
		int[][] nc = new int[y][x];
		
		for (int i = 0; i < y; i++) {
			for (int j = 0; j < x; j++) {
				nc[i][j] = c[x * i + j];
			}
		}
		
		this.c = nc;
		assert (this.c.length > 0 && this.c.length == this.c[0].length);
		
		this.colors = convertColors(scale);
	}
	
	public ClassToColor (final int [][] c, float scale) {
		this.c = c;
		assert (this.c.length > 0 && this.c.length == this.c[0].length);
	
		this.colors = convertColors(scale);
	}


	public float[][][] call() throws Exception {
		float[][][] r = new float[3][c.length][c[0].length]; 

		for (int y = 0; y < c.length; y++) {
			for (int x = 0; x < c[y].length; x++) {
				if (c[y][x] >= colors.length)
					throw new WarningException("To many classes for to few colors");
				
				assert (c[y][x] >= 0);
				
				r[0][y][x] = colors[c[y][x]][0];
				r[1][y][x] = colors[c[y][x]][1];
				r[2][y][x] = colors[c[y][x]][2];
			}
		}

		return r;
	}
}
