package uk.ac.le.sppg.superdarn.fitData;


import java.io.*;

/**
 * @author Nigel Wade
 */
public class RangeData implements Serializable {

    private static final long serialVersionUID = 0x525350504700001CL;

    /**
     * velocity (v)
     */
    public double velocity;                // velocity (v)
    /**
     * velocity error (v_err)
     */
    public double velocityErr;             // velocity error (v_err)
    /**
     * lag0 power (p_0)
     */
    public double lag0Power;               // lag0 power (p_0)
    /**
     * lambda power (p_l)
     */
    public double lambdaPower;             // lambda power (p_l)
    /**
     * lamba power error (p_l_err)
     */
    public double lambaPowerErr;           // lamba power error (p_l_err)
    /**
     * sigma power (p_s)
     */
    public double sigmaPower;              // sigma power (p_s)
    /**
     * sigma power error (p_s_err)
     */ 
    public double sigmaPowerErr;           // sigma power error (p_s_err)
    /**
     * lambda spectral width (w_l)
     */
    public double lambdaSpectralWidth;     // lambda spectral width (w_l)
    /**
     * lambda spectral width error (w_l_err)
     */
    public double lambdaSpectralWidthErr;  // lambda spectral width error (w_l_err)
    /**
     * sigma spectral width (w_s)
     */
    public double sigmaSpectralWidth;      // sigma spectral width (w_s)
    /**
     * sigma spectral width power (w_s_err)
     */
    public double sigmaSpectralWidthErr;   // sigma spectral width power (w_s_err)
    /**
     * phase angle (phi0)
     */
    public double phaseAngle;              // phase angle (phi0)
    /**
     * phase angle error (phi_err)
     */
    public double phaseAngleErr;           // phase angle error (phi_err)
    /**
     * standard deviation of of the lambda fit (sdev_l)
     */
    public double sdevLambdaFit;           // st dev of of the lambda fit (sdev_l)
    /**
     * standard deviation of the sigma fit (sdev_s)
     */
    public double sdevSigmaFit;            // st dev of the sigma fit (sdev_s)
    /**
     * standard deviation of the phase fit (sdev_phi)
     */
    public double sdevPhaseFit;            // st dev of the phase fit (sdev_phi)
    /**
     * quality flag (qflg)
     */
    public int qualityFlag;                // quality flag (qflg)
    /**
     * ground scatter flag (gsct)
     */
    public boolean groundScatterFlag;      // ground scatter flag (gsct)
    /**
     * number of good lags in the fit (nump)
     */
    public int numberGoodLags;             // number of good lags in the fit (nump)
	/**
	 * @return
	 */
	public boolean isGroundScatterFlag() {
		return groundScatterFlag;
	}

	/**
	 * @return
	 */
	public double getLag0Power() {
		return lag0Power;
	}

	/**
	 * @return
	 */
	public double getLambaPowerErr() {
		return lambaPowerErr;
	}

	/**
	 * @return
	 */
	public double getLambdaPower() {
		return lambdaPower;
	}

	/**
	 * @return
	 */
	public double getLambdaSpectralWidth() {
		return lambdaSpectralWidth;
	}

	/**
	 * @return
	 */
	public double getLambdaSpectralWidthErr() {
		return lambdaSpectralWidthErr;
	}

	/**
	 * @return
	 */
	public int getNumberGoodLags() {
		return numberGoodLags;
	}

	/**
	 * @return
	 */
	public double getPhaseAngle() {
		return phaseAngle;
	}

	/**
	 * @return
	 */
	public double getPhaseAngleErr() {
		return phaseAngleErr;
	}

	/**
	 * @return
	 */
	public int getQualityFlag() {
		return qualityFlag;
	}

	/**
	 * @return
	 */
	public double getSdevLambdaFit() {
		return sdevLambdaFit;
	}

	/**
	 * @return
	 */
	public double getSdevPhaseFit() {
		return sdevPhaseFit;
	}

	/**
	 * @return
	 */
	public double getSdevSigmaFit() {
		return sdevSigmaFit;
	}

	/**
	 * @return
	 */
	public double getSigmaPower() {
		return sigmaPower;
	}

	/**
	 * @return
	 */
	public double getSigmaPowerErr() {
		return sigmaPowerErr;
	}

	/**
	 * @return
	 */
	public double getSigmaSpectralWidth() {
		return sigmaSpectralWidth;
	}

	/**
	 * @return
	 */
	public double getSigmaSpectralWidthErr() {
		return sigmaSpectralWidthErr;
	}

	/**
	 * @return
	 */
	public double getVelocity() {
		return velocity;
	}

	/**
	 * @return
	 */
	public double getVelocityErr() {
		return velocityErr;
	}

}
