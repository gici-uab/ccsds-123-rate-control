package emporda;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class TWOSDQ implements Quantizer {
	private int PAE;
	private long acumulatedError = 0;
	private int maxline = 0;
	private float alpha = 0.5f;
	private long counter = 0;
	private FileWriter fwd = null;
	private PrintWriter pwd = null;
	private FileWriter fwc = null;
	private PrintWriter pwc = null;
	private long count = 0;
	
	
	public TWOSDQ(int quantizationStep) throws IOException {
			this.PAE = quantizationStep;
			fwd = new FileWriter("dlog.data",true);
			pwd = new PrintWriter(fwd);
			fwc = new FileWriter("clog.data",true);
			pwc = new PrintWriter(fwc);
	
	}

	/*public int quantize(int residual, int t) {

		int q_residual;
		int DeltaH = PAE;
		int DeltaL = PAE;
		if(DeltaL < 1) DeltaL = 1;
		int sign = 1;
		
		if(residual < 0){
			sign = -1;
		}
		
		maxline = (int)Math.floor( ((double)(DeltaL + 1) * 3) / 2);
		if(DeltaH + 1 > 1){
			residual = Math.abs(residual);
			if( residual < maxline){
				q_residual = (residual / (DeltaL + 1));
			}else{
				int A = (int) Math.floor( (double) (3 * (DeltaL + 1)) / (double) (2 * (DeltaL + 1)) );
				int B = (int) Math.floor((double)(residual - Math.floor( ((double)(DeltaL + 1) * 3) / 2) )/ (double)(DeltaL + 1));
				q_residual =  A + B;
			}
			
			q_residual = sign * q_residual;
		}else{
			q_residual = residual;
		}
		
		return q_residual;
	}
	
	public int dequantize(int q_residual){
		int residual;
		int DeltaH = PAE;
		int DeltaL = PAE;
		if(DeltaL < 1) DeltaL = 1;
		
		int sign = 1;
		
		if(q_residual < 0){
			sign = -1;
		}
		
		maxline = (int)Math.floor( ((double)(DeltaL + 1) * 3) / 2);
		if(DeltaH + 1 > 1){
			q_residual = Math.abs(q_residual);
			if(q_residual < (maxline  / (DeltaL+1))){
				residual =  q_residual * (DeltaL+1);
			}else{
				residual = (maxline) + ((q_residual - (maxline  / (DeltaL + 1))) * (DeltaH + 1));
			}
			residual = sign * residual;
		}else{
			residual = q_residual;
		}
		
		return residual;
		
	}*/
	
	public int quantize(int residual, int t) {

		int q_residual;
		int DeltaH = PAE * 2;
		int DeltaL = PAE * 2;
		if(DeltaL < 1) DeltaL = 1;
		int sign = 1;
		int A = -1;
		int B = 0;
		if(residual < 0){
			sign = -1;
		}
		
		maxline = (int)Math.floor( ((double)(DeltaL + 1) * 3) / 2);
		residual = Math.abs(residual);
		if( residual < maxline){
			q_residual = ((residual + PAE) / (DeltaL + 1));
		}else{
			//int A = 0;
			
			if(DeltaL == 1){
				A = (int) Math.floor( (double) (3 * (DeltaL + 1)) / (double) (2 * (DeltaL + 1)) );
			}else{
				A = (int)Math.ceil((double)maxline / (double)DeltaL); // PAE ok pero rate no
				A = (int)((double)(maxline + PAE) / (double)(DeltaL + 1)); //-->el rate ok pero no el PAE
			}
			//A = 2;
			
			B = (int) Math.floor((residual + PAE - maxline)/ (DeltaL + 1));
			//System.out.println("maxline: "+maxline+" DeltaL: "+DeltaL+" A: "+A+" B: "+B);
			q_residual =  A + B;
		}
		
		q_residual = sign * q_residual;
		
		int residualdequantized = dequantize(q_residual);
		residual = sign * residual;
		int error = Math.abs(residualdequantized-residual);
		if(error > PAE) System.out.println("!!!!!count: "+ count+" maxline: "+maxline+" A: "+A+" DeltaL: "+DeltaL+" error: "+error+" residual: "+residual+" q_residual: "+q_residual+" residualdequantized: "+residualdequantized);
		count++;
		//if(Math.abs(residualdequantized-residual)<=PAE) System.out.println("maxline: "+maxline+" DeltaL: "+DeltaL+" residual: "+residual+" residualdequantized: "+residualdequantized);
		return q_residual;
	}

	public int dequantize(int q_residual){
		int A = -1;
		int residual;
		int DeltaH = PAE * 2;
		int DeltaL = PAE * 2;
		if(DeltaL < 1) DeltaL = 1;
		
		int sign = 1;
		
		if(q_residual < 0){
			sign = -1;
		}
		
		maxline = (int)Math.floor( ((double)(DeltaL + 1) * 3) / 2);
		q_residual = Math.abs(q_residual);
		if(q_residual < ((double)(maxline + PAE) / (double)(DeltaL+1))){
			residual =  q_residual * (DeltaL+1);
		}else{
			if(DeltaL == 1){
				A = (int) Math.floor( (double) (3 * (DeltaL + 1)) / (double) (2 * (DeltaL + 1)) );
			}else{
				A = (int)Math.ceil((double)maxline / (double)DeltaL);
				A = (int)((double)(maxline + PAE) / (double)(DeltaL + 1));
			}
			
			residual = (maxline + (q_residual - A) * ( DeltaL + 1 ) );
		}
		residual = sign * residual;
		
		return residual;
		
	}
	
	/*UQ en 2 steps
	 * public int quantize(int residual, int t) {

		int q_residual;
		int DeltaH = PAE;
		int DeltaL = PAE;
		
		int sign = 1;
		
		if(residual < 0){
			sign = -1;
		}
		
		maxline = (int)Math.floor( ((double)(DeltaL + 1) * 3) / 2);
		if(DeltaL > 0){
			residual = Math.abs(residual);
			if( residual < maxline){
				q_residual = (residual / (DeltaL + 1));
				System.out.println(residual+" "+q_residual+" "+(maxline/ (DeltaL+1)));
			}else{
				//int A = (int) Math.floor( (double) (3 * (DeltaL + 1)) / (double) (2 * (DeltaL + 1)) );
				//int A = (int)Math.ceil(maxline / DeltaL);
				int A = 0;
				if(DeltaL == 1){
					A = (int) Math.floor( (double) (3 * (DeltaL + 1)) / (double) (2 * (DeltaL + 1)) );
				}else{
					A = (int)Math.ceil(maxline / DeltaL);
				}
				int B = (int) Math.floor((double)(residual - Math.floor( ((double)(DeltaL + 1) * 3) / 2) )/ (double)(DeltaL + 1));
				q_residual =  A + B;
			}
			
			q_residual = sign * q_residual;
		}else{
			q_residual = residual;
		}
		
		return q_residual;
	}
	
	public int dequantize(int q_residual){
		int residual;
		int DeltaH = PAE;
		int DeltaL = PAE;
		
		int sign = 1;
		
		if(q_residual < 0){
			sign = -1;
		}
		double offset = 0.0d;
		
		maxline = (int)Math.floor( ((double)(DeltaL + 1) * 3) / 2);
		if(q_residual == 0) residual = 0;
		else{
			q_residual = Math.abs(q_residual);
			if(q_residual < (maxline  / (DeltaL+1))){
				residual =  (int)(((double)q_residual+offset) * (double)(DeltaL+1));
			}else{
				int A = (int)Math.ceil((((double)maxline  / (double)(DeltaL+1) + offset) * (double)(DeltaL+1)));
				residual = (A) + (int)Math.floor(( ((double)q_residual + (double)offset - (double)(A  / (DeltaL + 1))) ) * (double)(DeltaH + 1));
			}
			residual = sign * residual;
		}
			
		return residual;
		
	}*/
	

	
	public void setQuantizationStep(int quantisationStep){
		this.PAE = quantisationStep;
		this.acumulatedError = 0;
	}
	public int getQuantizationStep(){
		return PAE;
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
		maxline = max;
		
	}

	@Override
	public int setWindowsize() {
		pwc.flush();
		pwc.close();
		pwd.flush();
		pwd.close();
		return 0;
	}
	
}
