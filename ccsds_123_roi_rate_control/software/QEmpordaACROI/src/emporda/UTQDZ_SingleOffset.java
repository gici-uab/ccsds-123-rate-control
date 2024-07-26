package emporda;

public class UTQDZ_SingleOffset implements Quantizer{
	
	private int quantizationStep;
	private double z;
	private double lambda;
	private double alpha;
	private double delta;
	private double offset;
	private long acumulatedError = 0;
		
		public UTQDZ_SingleOffset(int quantizationStep) {
				this.quantizationStep = quantizationStep;
//				alpha = quantizationStep/(float)lambda;
//				delta = 1 - (alpha*Math.exp(-alpha))/(float)(1-Math.exp(-alpha));
//				offset = delta*lambda;
				offset = quantizationStep*4/9f;
				z = (quantizationStep + 1)/ (float)quantizationStep;
		}
		public int quantize(int residual, int t) {
			int q_residual;
			q_residual=(int)(Math.signum(residual)*Math.max(0, Math.floor(Math.abs(residual)/(float)quantizationStep - z/(float)2 + 1 )));
			acumulatedError = acumulatedError + Math.abs((residual - q_residual));
			return q_residual;
		}		
		public int dequantize(int q_residual){
			int residual;
			if (q_residual == 0) residual = 0;
			else residual = (int) (Math.signum(q_residual)*Math.floor((Math.abs(q_residual) + z/(float)2 - 1)*quantizationStep + offset));	
			return residual;
		}
		public void setQuantizationStep(int quantizationStep){
			this.quantizationStep = quantizationStep;
//			alpha = quantizationStep/(float)lambda;
//			delta = 1 - (alpha*Math.exp(-alpha))/(float)(1-Math.exp(-alpha));
//			offset = delta*lambda;
			offset = quantizationStep*4/9f;
			z = (quantizationStep + 1)/ (float)quantizationStep;
			acumulatedError = 0;
		}
		public int getQuantizationStep(){
			return quantizationStep;
		}
		public void updateLambda(double lambda){
//			this.lambda = lambda;
//			alpha = quantizationStep/(float)lambda;
//			delta = 1 - (alpha*Math.exp(-alpha))/(float)(1-Math.exp(-alpha));
//			offset = delta*lambda;
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
