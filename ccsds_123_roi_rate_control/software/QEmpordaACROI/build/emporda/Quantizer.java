package emporda;

public interface Quantizer {
	int quantize(int residual, int t);
	int dequantize(int q_residual);
	void setQuantizationStep(int quantisationStep);
	void setMax(int max);
	int getQuantizationStep();
	void updateLambda(double lambda);
	long getAcumulatedError();
	int setWindowsize();
}
