package emporda;

public class UTQDZ_NURQ implements Quantizer{
private int quantizationStep;
private double p;
private double z;
private double c = 0.5;
private long acumulatedError = 0;
	
	public UTQDZ_NURQ(int quantizationStep) {
			this.quantizationStep = quantizationStep;
			z=((quantizationStep+1)/(float)quantizationStep)/2f;
			p = z - c;
	}

	public int quantize(int residual, int t) {
		int q_residual;
		q_residual=(int)(Math.signum(residual)*Math.max(0, Math.floor(Math.abs(residual)/(float)quantizationStep - z + 1 )));
		acumulatedError = acumulatedError + Math.abs((residual - q_residual));
		return q_residual;
	}
	
	public int dequantize(int q_residual){
		int residual;
		residual= (int)(Math.signum(q_residual)*Math.floor((Math.abs(q_residual)+p)*quantizationStep));
		return residual;
	}
	public void setQuantizationStep(int quantisationStep){
		this.quantizationStep = quantisationStep;
		z=((quantizationStep+1)/(float)quantizationStep)/2f;
		p = z - c;
		acumulatedError = 0;
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
