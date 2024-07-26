package emporda;

public class URQMAX implements Quantizer {
	private int quantizationStep;
	private long acumulatedError = 0;
	
	public URQMAX(int quantizationStep) {
			this.quantizationStep = quantizationStep;
	}

/*
	
	public int quantize(int residual, int t) {
		int q_residual;
		q_residual = (int) (Math.abs(residual) + quantizationStep) / (2*quantizationStep+1);
		q_residual = (int) (Math.signum(residual) * q_residual);
		acumulatedError = acumulatedError + Math.abs((residual - q_residual));
		return q_residual;
	}
	
	public int dequantize(int q_residual){	
		int residual;
		residual = q_residual  * ( 2 * quantizationStep + 1);
		return residual;
	}
	*/
	public int quantize(int residual, int t) {
		int q_residual;
	
		if(residual >= 0){
			q_residual=  (residual + quantizationStep) / (2*quantizationStep+1);
		}else{
			q_residual=  (residual - quantizationStep) / (2*quantizationStep+1);
		}
		
		 
		acumulatedError = acumulatedError + Math.abs((residual - q_residual));
		//if(residual != q_residual)System.out.println("C: "+residual+" "+q_residual);
		return q_residual;
	}
	
	public int dequantize(int q_residual){
		int residual;
		//residual= (int) (Math.signum(q_residual) * (Math.abs(q_residual) + 1));
		//residual = ((Math.abs(q_residual))  * ( 2 * quantizationStep + 1)) - quantizationStep;
		//residual = (int) (Math.signum(q_residual)*residual);
		residual = ((Math.abs(q_residual))  * ( 2 * quantizationStep + 1)) - quantizationStep;
		
		residual = (int) (Math.signum(q_residual)*residual);
		//if(residual != q_residual)System.out.println("D: "+residual+" "+q_residual);
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
