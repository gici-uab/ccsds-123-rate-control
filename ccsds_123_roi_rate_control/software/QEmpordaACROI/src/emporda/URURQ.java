package emporda;

public class URURQ implements Quantizer {
	
	private int quantizationStep;
	private long acumulatedError = 0;
	
	public URURQ(int quantizationStep) {
			this.quantizationStep = quantizationStep;
	}

	public int quantize(int residual, int t) {
		int q_residual;
		
		q_residual=(int)(Math.signum(residual)*Math.abs(residual)/(float)quantizationStep);
		acumulatedError = acumulatedError + Math.abs((residual - q_residual));
		return q_residual;
	}
	
	public int dequantize(int q_residual){
		int residual;
		residual= q_residual * quantizationStep;
		//System.out.println("D: "+residual+" "+q_residual);
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
