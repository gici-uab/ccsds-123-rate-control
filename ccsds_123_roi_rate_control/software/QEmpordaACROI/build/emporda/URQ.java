package emporda;


public class URQ implements Quantizer {
	private int quantizationStep;
	private long acumulatedError = 0;
	
	public URQ(int quantizationStep) {
			this.quantizationStep = quantizationStep;
	}

	public int quantize(int residual, int t) {
		int sign = 1;
		int q_residual;
		if(residual < 0){
			sign = -1;
		}
		q_residual=sign*(int)Math.floor((Math.abs((double)residual)+(((double)quantizationStep - 1d)/2d))/(double)quantizationStep);
		acumulatedError = acumulatedError + Math.abs((residual - q_residual));	
		return q_residual;
	}
	
	public int dequantize(int q_residual){
		int residual;
		residual= q_residual * quantizationStep;

		return residual;
	}
	public void setQuantizationStep(int quantisationStep){
		this.quantizationStep = quantisationStep;
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
