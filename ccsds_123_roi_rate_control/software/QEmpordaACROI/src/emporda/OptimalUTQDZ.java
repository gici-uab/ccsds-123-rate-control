package emporda;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


public class OptimalUTQDZ implements Quantizer{
	private int quantizationStep;
	private double z;
	private String file1 = "optimalReconstruction_geo_qs";
	private String file2 = "qResidual_geo_qs";
	private RandomAccessFile optRec;
	private RandomAccessFile qRes;
	private List<Integer> optReconstruction = new ArrayList<Integer>();
	private List<Integer> qResidual = new ArrayList<Integer>();
	private int min_qResidual;
	private int LUTsize;
	private long acumulatedError = 0;
	
	public OptimalUTQDZ(int quantizationStep) {
		this.quantizationStep = quantizationStep;
		z = (quantizationStep + 1)/ (float)quantizationStep;
		try{
			optRec = new RandomAccessFile(file1+String.valueOf(quantizationStep)+".txt", "r");
			optRec.seek(0);
			qRes = new RandomAccessFile(file2+String.valueOf(quantizationStep)+".txt", "r");
			qRes.seek(0);
			String line;
			
			  while ((line = optRec.readLine()) != null) {
				  optReconstruction.add(Integer.parseInt(line));
				  qResidual.add(Integer.parseInt(qRes.readLine()));
			  }
			  
			min_qResidual = qResidual.get(0);
			LUTsize = optReconstruction.size();
				
		}catch(FileNotFoundException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}catch(IOException e) {
			throw new Error("Unexpected exception ocurred "+e.getMessage());
		}
	}
	public int quantize(int residual, int t) {
		int q_residual;
		q_residual=(int)(Math.signum(residual)*Math.max(0, Math.floor(Math.abs(residual)/(float)quantizationStep - z/(float)2 + 1 )));
		return q_residual;
	}		
	public int dequantize(int q_residual){
		int residual;
		
		if (q_residual == 0) residual = 0;
		else {
			int index = q_residual - min_qResidual;
			if (index >= 0 && index < LUTsize) residual = optReconstruction.get(index);
			else {
				residual = (int) (Math.signum(q_residual)*Math.ceil((Math.abs(q_residual) + z/(float)2 - 1)*quantizationStep + quantizationStep/(float)2));
			}
		}
		acumulatedError = acumulatedError + Math.abs((residual - q_residual));
		return residual;
	}
	public void setQuantizationStep(int quantizationStep){
		this.quantizationStep = quantizationStep;
		z = (quantizationStep + 1)/ (float)quantizationStep;
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
