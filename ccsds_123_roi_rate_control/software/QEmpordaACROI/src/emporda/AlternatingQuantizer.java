package emporda;

public class AlternatingQuantizer implements Quantizer {
	private int quantizationStep;
	private long acumulatedError = 0;

	public AlternatingQuantizer(int quantizationStep) {
		this.quantizationStep = quantizationStep;
	}
	public int quantize(int residual, int t) {
		int q_residual;
		q_residual = t % 2 == 0 ? (int)Math.ceil(residual / (float) quantizationStep - 0.5f): (int)Math.floor(residual/(float) quantizationStep + 0.5f);
		acumulatedError = acumulatedError + Math.abs((residual - q_residual));
		return q_residual;
	}
	
	public int dequantize(int q_residual){
		int residual;
		residual= q_residual * quantizationStep;
		return residual;
	}
	public void setQuantizationStep(int quantizationStep){
		this.quantizationStep = quantizationStep;
		this.acumulatedError = 0;
	}
	public int getQuantizationStep(){
		return quantizationStep;
	}
	public void updateLambda(double lambda){
		// Nothing to do here
	}
	
	@Override
	public long getAcumulatedError() {
		
		return acumulatedError;
	}
	@Override
	public void setMax(int max) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int setWindowsize() {
		// TODO Auto-generated method stub
		return 0;
	}
}
