package emporda;


public class SDQ implements Quantizer {
	private int quantizationStep;
	private long acumulatedError = 0;
	
	public SDQ(int quantizationStep) {
			this.quantizationStep = quantizationStep + 1;
	}

	public int quantize(int residual, int t) {
		int sign = 1;
		int q_residual;
		if(residual < 0){
			sign = -1;
		}
		residual = Math.abs(residual);
		if(residual == 0) q_residual = 0;
		else q_residual=(int) (sign*Math.floor(residual/quantizationStep));
		acumulatedError = acumulatedError + Math.abs((residual - q_residual));	
		return q_residual;
	}
	
	public int dequantize(int q_residual){
		int residual;
		int sign = 1;
		if(q_residual < 0){
			sign = -1;
		}
		q_residual = Math.abs(q_residual);
		double offset = 0.5d;
		//=SIGNO(B52)*FLOOR.MATH(((ABS(B52)+1/2)*$B$3))
		if(q_residual == 0) residual = 0;
		else{
			residual = (int) Math.floor(((double)q_residual + (double)offset) * (double)quantizationStep);
			residual = sign * residual;
		}
	
		
		return residual;
	}
	public void setQuantizationStep(int quantisationStep){
		this.quantizationStep = quantisationStep + 1;
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
