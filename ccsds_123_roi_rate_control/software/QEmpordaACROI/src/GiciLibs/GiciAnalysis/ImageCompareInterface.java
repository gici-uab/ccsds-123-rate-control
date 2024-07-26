package GiciAnalysis;

public interface ImageCompareInterface {
	/**
	 * @return mae definition in this class
	 */
	public abstract double[] getMAE();

	/**
	 * @return totalMAE definition in this class
	 */
	public abstract double getTotalMAE();

	/**
	 * @return pae definition in this class
	 */
	public abstract double[] getPAE();

	/**
	 * @return totalPAE definition in this class
	 */
	public abstract double getTotalPAE();

	/**
	 * @return mse definition in this class
	 */
	public abstract double[] getMSE();

	/**
	 * @return totalMSE definition in this class
	 */
	public abstract double getTotalMSE();

	/**
	 * @return rmse definition in this class
	 */
	public abstract double[] getRMSE();

	/**
	 * @return totalRMSE definition in this class
	 */
	public abstract double getTotalRMSE();

	/**
	 * @return me definition in this class
	 */
	public abstract double[] getME();

	/**
	 * @return totalME definition in this class
	 */
	public abstract double getTotalME();

	/**
	 * @return snr definition in this class
	 */
	public abstract double[] getSNR();

	/**
	 * @return totalSNR definition in this class
	 */
	public abstract double getTotalSNR();

	/**
	 * @return psnr definition in this class
	 */
	public abstract double[] getPSNR();

	/**
	 * @return totalPSNR definition in this class
	 */
	public abstract double getTotalPSNR();

	/**
	 * @return psnr definition in this class
	 */
	public abstract double[] getPSNRSALOMON();

	/**
	 * @return totalPSNR definition in this class
	 */
	public abstract double getTotalPSNRSALOMON();

	/**
	 * @return equal definition in this class
	 */
	public abstract boolean[] getEQUAL();

	/**
	 * @return totalEQUAL definition in this class
	 */
	public abstract boolean getTotalEQUAL();

	/**
	 * @return snr definition in this class
	 */
	public abstract double[] getSNRVAR();

	/**
	 * @return totalSNR definition in this class
	 */
	public abstract double getTotalSNRVAR();

	/**
	 * @return covariance definition in this class
	 */
	public abstract double[] getCovariance();

	/**
	 * @return ssim definition in this class
	 */
	public abstract double[] getSSIM();

	/**
	 * @return totalSSIM definition in this class
	 */
	public abstract double getTotalSSIM();

	public abstract double[] getRRMSE();

	public abstract double getTotalRRMSE();

	public abstract double[] getNMSE();

	public abstract double getTotalNMSE();

	public abstract double[] getPSNRNC();

	public abstract double getTotalPSNRNC();

}