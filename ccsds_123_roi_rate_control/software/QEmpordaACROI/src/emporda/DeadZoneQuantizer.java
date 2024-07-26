package emporda;

public class DeadZoneQuantizer implements Quantizer{
	
private int quantizationStep;
private long acumulatedError = 0;

	public DeadZoneQuantizer(int quantizationStep) {
			this.quantizationStep = quantizationStep;
	}

	public int quantize(int residual, int t) {
		int q_residual;
		q_residual=(int)(Math.signum(residual)*Math.floor(Math.abs(residual)/(float)quantizationStep));
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
