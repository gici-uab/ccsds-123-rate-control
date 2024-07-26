package emporda;

import java.util.List;

import GiciContextModel.ContextModelling;
import GiciContextModel.ContextProbability;
import GiciEntropyCoder.ArithmeticCoder.ArithmeticCoderFLW;
import GiciEntropyCoder.Interface.EntropyCoder;

/**
 * This is strucutre contains the data line information necessary to predict the next data line according to the previous predicted data.
 **/
public class DataLine {
	
	private int bands;
	private int rows;
	private int columns;
	private int line;
	private int[][][] data = null;
	public int[][][] PredictedData = null;
	private double rate = 0;
	private long error = 0;
	private List<Integer> qStepsList;
	private Predictor predictor;
	private ContextModelling contextModelling; 
	private ContextProbability contextProbability;
	private ArithmeticCoderFLW arithmeticCoderFLW;
	
	public DataLine(int bands, int rows, int columns, int line, int qstep, ContextModelling contextModelling, ContextProbability contextProbability, EntropyCoder ec) {
		this.bands = bands;
		this.rows = rows;
		this.columns = columns;
		this.line = line;
		this.qStepsList.add(qstep);
		this.data = new int[bands][rows][columns];
		this.PredictedData = new int[bands][rows][columns];
		
		for(int band = 0; band < bands; band++){
		for(int row = 0; row < rows; row++){
		for(int column = 0; column < columns; column++){	
			this.data[band][row][column] = 0;	
			
		}}}
	}
	
	/**
	 * returns the line
	 */
	public int getLine(){
		return(line);
	}
	
	/**
	 * returns the line
	 */
	public int getQstep(){
		return(qStepsList.get(qStepsList.size()-1));
	}
	
	/**
	 * put Predicted Data in the specific position z and x, y always is y=1
	 * @param value
	 * @param z
	 * @param x
	 */
	public void putPredictedData(int value, int z, int x){
		this.PredictedData[z][1][x] = value;
	}

	/**
	 * put line data
	 * @param data
	 */
	public void putPreviousData(int [][][] line){
		for(int band = 0; band < bands; band++){
		for(int column = 0; column < columns; column++){
			this.data[band][0][column] = line[band][0][column];	
		}}
	}
	
	/**
	 * put line data
	 * @param data
	 */
	public void putData(int [][][] line){
		for(int band = 0; band < bands; band++){
		for(int column = 0; column < columns; column++){
			this.data[band][1][column] = line[band][0][column];	
		}}
	}
	
	/**
	 * Returns the predictor used to predict this line
	 * @return
	 */
	public Predictor getPredictor(){
		return(predictor);
	}
	
	/**
	 * Return the acumulated rate
	 * @return
	 */
	public double getRate(){
		return(rate);
	}
	
	
	/**
	 * Insert the rate of the current predicted line quantized with the #qstep value. The rate is acumulated.
	 * @param rate
	 */
	public void putRate(double rate){
		this.rate += rate;
	}
	
	/**
	 * Return the acumulated error
	 * @return
	 */
	public long getError(){
		return(error);
	}
	
	/**
	 * Insert the error of the current predicted line quantized with the #qstep value. The error is acumulated.
	 * @param error
	 */
	public void putError(long error){
		this.error += error;
	}
	
	/**
	 * Insert a qstep element int the qStepsList
	 * @param qstep
	 */
	public void putQstep(int qstep){
		qStepsList.add(qstep);
	}
	
	/**
	 * Returns the twoLines structure data
	 * @return twoLines
	 */
	public int[][][] getData(){
		return(this.data);
	}
	
	/**
	 * Return the ContextProbability
	 * @return
	 */
	public ContextProbability getContextProbability(){
		return(contextProbability);
	}
	
	/**
	 * Return the ArithmeticCoderFLW
	 * @return
	 */
	public ArithmeticCoderFLW getArithmeticCoderFLW(){
		return(arithmeticCoderFLW);
	}
	
	/**
	 * Return the ContextModelling
	 * @return
	 */
	public ContextModelling getContextModelling(){
		return(contextModelling);
	}
	
}
