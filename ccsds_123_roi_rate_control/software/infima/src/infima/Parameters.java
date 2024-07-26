/**
 * Copyright (C) 2013 - Francesc Auli-Llinas
 *
 * This program is distributed under the BOI License.
 * This program is distributed in the hope that it will be useful, but without any warranty; without even the implied warranty of merchantability or fitness for a particular purpose.
 * You should have received a copy of the BOI License along with this program. If not, see <http://www.deic.uab.cat/~francesc/software/license/>.
 */
package infima;


/**
 * This object manipulates the program's parameters, parsing and checking them.<br>
 *
 * Usage: objects of this class are not allowed. All functions and fields of the class are static. The function <code>parseArguments</code> parses the program's arguments and sets the corresponding variables. Then the variables can be checked through the <code>checkParameters</code> function. The <code>get</code> functions returns the parameters.<br>
 *
 * Multithreading support: the class must be manipulated by a single thread since there are no mutual exclusion mechanisms to avoid conflicts when setting the class fields.
 *
 * @author Francesc Auli-Llinas
 * @version 1.0
 */
public final class Parameters{

	/**
	 * Original image file.
	 * <p>
	 * The format of the image file must be either P5 (grayscale) / P6 (color) from the PNM family, or raw.
	 */
	private static String fileOriginal = "";

	/**
	 * Image to be compared with the original (when applicable).
	 * <p>
	 * The format of the image files must be either P5 (grayscale) / P6 (color) from the PNM family, or raw.
	 */
	private static String fileCompare = "";

	/**
	 * Geometry of the input image/s.
	 * <p>
	 * Nine integers meaning zSize ySzie xSize sampleType signedType bitDepth byteOrder dataOrder componentsRGB.
	 */
	private static int[] geometry = null;

	/**
	 * Metrics to compute and print.
	 * <p>
	 * When the program in single file mode, the metrics are:
	 * <ul>
	 * <li>0: all</li>
	 * <li>1: min</li>
	 * <li>2: max</li>
	 * <li>3: average</li>
	 * <li>4: range center</li>
	 * <li>5: energy</li>
	 * </ul>
	 * and when the program compares two images (two files mode), the metrics are:
	 * <ul>
	 * <li>0: all</li>
	 * <li>1: mean absolute error (MAE)</li>
	 * <li>2: peak absolute error (PAE)</li>
	 * <li>3: mean squared error (MSE)</li>
	 * <li>4: root mean square error (RMSE)</li>
	 * <li>5: mean error (ME)</li>
	 * <li>6: signal to noise ratio (SNR)</li>
	 * <li>7: peak signal to noise ratio (PSNR)</li>
	 * <li>8: equality</li>
	 * </ul>
	 */
	private static int[] metrics = {0};

	/**
	 * Printing format.
	 * <p>
	 * If 0 a long format will be used, 1 will employ a format like M1 M2 M3 ..., where M1 indicates the first metric specified.
	 */
	private static int format = 0;

	/**
	 * Print the totals (average of all components, when applicable).
	 * <p>
	 * 0 does not show the totals, 1 prints the metrics for the totals, and 2 prints the metrics only for the components and the totals
	 */
	private static int totals = 1;

	/**
	 * Number of threads executed in parallel to perform the operations. Each thread computes the entropy for a data bunch of the image.
	 * <p>
	 * In general, two threads achieve high performance.
	 */
	private static int threadsNumber = Runtime.getRuntime().availableProcessors() / 2 < 2 ? 2: Runtime.getRuntime().availableProcessors() / 2;

	/**
	 * Flags to know whether the parameter is given in the program's arguments or not.
	 * <p>
	 * Each parameters is an index of the array. True indicates that is given the program's arguments.
	 */
	private static boolean[] parametersGiven = new boolean[6];

	/**
	 * Help of the program.
	 */
	private static String help = "" +
		"INFIMA (version " + Infima.INFIMA_VERSION + ")\n" +
		"\n" +
		"Copyright (C) 2013 - Francesc Auli-Llinas\n" +
		"This program is distributed under the BOI License (http://www.deic.uab.es/~francesc/software/license/).\n" +
		"This program is distributed in the hope that it will be useful, but without any warranty; without even the implied warranty of merchantability or fitness for a particular purpose.\n" +
		"Please, report bugs, comments, or suggestions to: boi@deic.uab.cat\n" +
		"\n" +
		"Synopsis: INFIMA computes information metrics of one or two images. When only one image is specified in the -i parameter, the program computes metrics such as the energy, center range, etc. of that image. When two images are specified, the program computes comparison metrics such as mean squared error, peak signal to noise ratio, etc. See parameter -m for a complete list of available metrics. The results are displayed in differents forms as specified via the -f and -t parameters.\n" +
		"\n" +
		"Program's parameters:\n" +
		"\n\"-i fileOriginal [fileCompare]\"\n" +
			"  Input file/s. If only one file is specified, the program enters in single file mode and computes some metrics from the image. When two files are provided, the program enters in two files mode and compares the images employing some metrics. When in two files mode, the first file is the original image, and the second is the image to be compared with the original. The format of the file/s must be P5 (grayscale) / P6 (color) of the PNM family, or raw. This parameter is mandatory.\n" +
		"\n\"-ig zSize ySzie xSize sampleType signedType bitDepth byteOrder dataOrder componentsRGB\"\n" +
			"  Geometry of the input file/s when their format is raw (i.e., files with extensions .raw or .img). When in two files mode, the geometry of both files is assumed to be the same. Each argument is an integer meaning the following:\n" +
			"    zSize: number of components\n" +
			"    ySize: number of rows\n" +
			"    xSize: number of columns\n" +
			"    sampleType:\n" +
				"      0 for boolean (1 byte)\n" +
				"      1 for unsigned integer (1 byte)\n" +
				"      2 for unsigned integer (2 bytes)\n" +
				"      3 for signed integer (2 bytes)\n" +
				"      4 for signed integer (4 bytes)\n" +
				"      5 for signed integer (8 bytes)\n" +
				"      6 for float (4 bytes)\n" +
				"      7 for double (8 bytes)\n" +
			"    signedType: 0 indicates unsigned data, 1 otherwise\n" +
			"    bitDepth: bit depth --it may be equal or lower than the maximum allowed by the sampleType--\n" +
			"    byteOrder: 0 indicates big endian, 1 little endian\n" +
			"    dataOrder: 0 indicates that the data are organized first as rows, then columns and then components (i.e., the outer loop reads the components), 1 indicates that the data are organized as components, rows and columns (i.e., the inner loop reads the components)\n" +
			"    componentsRGB: 1 indicates that the first three components of the image correspond to the red, green, and blue channels, 0 otherwise\n" +
			"  Important note: if this parameter is not specified and the file format is raw, then the geometry is extracted from the name of the file, which must contain a sequence like *.zSize_ySzie_xSize_sampleType_signedType_bitDepth_byteOrder_dataOrder_componentsRGB.*\n" +
		"\n\"-m metric1 [metric2] [...]\"\n" +
			"  Metrics to compute and print (" + Parameters.metrics[0] + " by default). If this parameter is given, at least one metric has to be specified. Supported metrics when the program is in single file mode are:\n" +
			"    0: computes and prints all metrics below\n" +
			"    1: min\n" +
			"    2: max\n" +
			"    3: range center\n" +
			"    4: average\n" +
			"    5: energy\n" +
			"  whereas supported metrics when the program is in two files mode are:\n" +
			"    0: computes and prints all metrics below\n" +
			"    1: mean absolute error (MAE)\n" +
			"    2: peak absolute error (PAE)\n" +
			"    3: mean squared error (MSE)\n" +
			"    4: root mean square error (RMSE)\n" +
			"    5: mean error (ME)\n" +
			"    6: signal to noise ratio (SNR)\n" +
			"    7: peak signal to noise ratio (PSNR)\n" +
			"    8: equality\n" +
		"\n\"-f format\"\n" +
			"  Format employed to print the results (" + Parameters.format + " by default). Valid formats are:\n" +
			"    0: long format (self-explanatory)\n" +
			"    1: short format, in which each line corresponds to the results of one component, being the first field the component number and then each metric is separated by a space\n" +
		"\n\"-t totals\"\n" +
			"  Indicates whether to print the totals (average metric for all components of the image) or not (" + Parameters.totals + " by default). Valid methods are:\n" +
			"    0: prints the metrics per component and does not print the totals\n" +
			"    1: only prints the totals\n" +
			"    2: prints the metrics per component and the totals\n" +
			"    3: only prints information about the file/s, without printing any metric\n" +
		"\n\"-tn threadsNumber\"\n" +
			"  Number of threads employed. Each thread processes a bunch of the image (" + Parameters.threadsNumber + " by default).\n" +
		"\n\"-h\"\n" +
			"  Prints this help and exits.";


	/**
	 * Parses the program arguments and sets them in the class variables.
	 *
	 * @param args an array of strings containing the program's parameters
	 * @throws Exception when some problem reading the parameters occurs
	 */
	public static void parseArguments(String[] args) throws Exception{
		if(args.length == 0){
			System.out.println(help);
			System.exit(0);
		}

		int argIndex = 0;
		while(argIndex < args.length){
			if(args[argIndex].equalsIgnoreCase("-i")){
				if(parametersGiven[0]){
					throw new Exception("Parameter -i is given twice.");
				}else{
					parametersGiven[0] = true;
				}
				if(args.length <= argIndex + 1){
					throw new Exception("Parameter -i takes one or two arguments. Use -h to show help.");
				}
				argIndex++;
				fileOriginal = args[argIndex];
				if((args.length > argIndex + 1) && (!args[argIndex + 1].startsWith("-"))){
					argIndex++;
					fileCompare = args[argIndex];
				}

			}else if(args[argIndex].equalsIgnoreCase("-ig")){
				if(parametersGiven[1]){
					throw new Exception("Parameter -ig is given twice.");
				}else{
					parametersGiven[1] = true;
				}
				if(args.length <= argIndex + 9){
					throw new Exception("Parameter -ig takes nine arguments. Use -h to show help.");
				}
				geometry = new int[9];
				for(int a = 0; a < 9; a++){
					argIndex++;
					geometry[a] = Integer.parseInt(args[argIndex]);
				}

			}else if(args[argIndex].equalsIgnoreCase("-m")){
				if(parametersGiven[2]){
					throw new Exception("Parameter -m is given twice.");
				}else{
					parametersGiven[2] = true;
				}
				if(args.length <= argIndex + 1){
					throw new Exception("Parameter -m takes one argument, at least. Use -h to show help.");
				}
				argIndex++;
				int[] m = new int[args.length - argIndex];
				m[0] = Integer.parseInt(args[argIndex]);
				int numMetrics = 1;
				while((args.length > argIndex + 1) && (!args[argIndex + 1].startsWith("-"))){
					argIndex++;
					m[numMetrics] = Integer.parseInt(args[argIndex]);
					numMetrics++;
				}
				metrics = new int[numMetrics];
				System.arraycopy(m, 0, metrics, 0, numMetrics);

			}else if(args[argIndex].equalsIgnoreCase("-f")){
				if(parametersGiven[3]){
					throw new Exception("Parameter -f is given twice.");
				}else{
					parametersGiven[3] = true;
				}
				if(args.length <= argIndex + 1){
					throw new Exception("Parameter -f takes one argument. Use -h to show help.");
				}
				argIndex++;
				format = Integer.parseInt(args[argIndex]);

			}else if(args[argIndex].equalsIgnoreCase("-t")){
				if(parametersGiven[4]){
					throw new Exception("Parameter -t is given twice.");
				}else{
					parametersGiven[4] = true;
				}
				if(args.length <= argIndex + 1){
					throw new Exception("Parameter -t takes one argument. Use -h to show help.");
				}
				argIndex++;
				totals = Integer.parseInt(args[argIndex]);

			}else if(args[argIndex].equalsIgnoreCase("-tn")){
				if(parametersGiven[5]){
					throw new Exception("Parameter -tn is given twice.");
				}else{
					parametersGiven[5] = true;
				}
				if(args.length <= argIndex + 1){
					throw new Exception("Parameter -tn takes one argument. Use -h to show help.");
				}
				argIndex++;
				threadsNumber = Integer.parseInt(args[argIndex]);

			}else if(args[argIndex].equalsIgnoreCase("-h")){
				System.out.println(help);
				System.exit(0);

			}else{
				throw new Exception("Parameter " + args[argIndex] +  " is not recognized. Use -h to show help.");
			}
			argIndex++;
		}
	}

	/**
	 * Checks the program's parameters.
	 *
	 * @throws Exception when some parameter is incorrect
	 */
	public static void checkParameters() throws Exception{
		//-i is mandatory
		if(fileOriginal.equalsIgnoreCase("")){
			throw new Exception("Parameter -i is mandatory. Use -h to show help.");
		}
	}

	/**
	 * @return {@link #fileOriginal}
	 */
	public static String getFileOriginal(){
		return(fileOriginal);
	}

	/**
	 * @return {@link #fileCompare}
	 */
	public static String getFileCompare(){
		return(fileCompare);
	}

	/**
	 * @return {@link #geometry}
	 */
	public static int[] getGeometry(){
		return(geometry);
	}

	/**
	 * @return {@link #metrics}
	 */
	public static int[] getMetrics(){
		return(metrics);
	}

	/**
	 * @return {@link #format}
	 */
	public static int getFormat(){
		return(format);
	}

	/**
	 * @return {@link #totals}
	 */
	public static int getTotals(){
		return(totals);
	}

	/**
	 * @return {@link #threadsNumber}
	 */
	public static int getThreadsNumber(){
		return(threadsNumber);
	}
}
