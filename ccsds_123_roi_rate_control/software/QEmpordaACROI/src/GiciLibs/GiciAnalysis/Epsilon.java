package GiciAnalysis;

/**
 * This class is used to compute the epsilons for single and double precision floating point numbers.
 * <p> Epsilon is the rounding error we can expect if we work with floating point numbers.
 *
 * @author Ian Blanes
 */
public class Epsilon {
	/**
	 * This is an utility class and shall not be constructed.
	 */
	protected Epsilon() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Half precision epsilon.
	 */
	private static HalfFloat hEpsilon = null;
	
	/**
	 * Single precision epsilon.
	 */
	private static float fEpsilon = 0;

	/**
	 * Double precision epsilon.
	 */
	private static double dEpsilon = 0;

	/**
	 * Have we computed it before?
	 */
	private static boolean doneHalf = false;
	
	/**
	 * Have we computed it before?
	 */
	private static boolean doneFloat = false;

	/**
	 * Have we computed it before?
	 */
	private static boolean doneDouble = false;

	/**
	 * @return single precision epsilon.
	 */
	public static HalfFloat getHalfFloatEpsilon() {
		if (doneHalf) {
			return hEpsilon;
		}

		float epsilon = 1.0f;
		float newEpsilon = 1.0f / 2.0f;

		while ((new HalfFloat(1.0f + newEpsilon)).compareTo(new HalfFloat (1.0f)) > 0) {
			epsilon = newEpsilon;
			newEpsilon /= 2.0f;
		}

		hEpsilon = new HalfFloat(epsilon);
		doneHalf = true;

		return hEpsilon;
	}
	
	/**
	 * @return single precision epsilon.
	 */
	public static float getFloatEpsilon() {
		if (doneFloat) {
			return fEpsilon;
		}

		float epsilon = 1.0f;
		float newEpsilon = 1.0f / 2.0f;

		while (1.0f + newEpsilon > 1.0f) {
			epsilon = newEpsilon;
			newEpsilon /= 2.0f;
		}

		fEpsilon = epsilon;
		doneFloat = true;

		return epsilon;
	}

	/**
	 * @return double precision epsilon.
	 */
	public static double getDoubleEpsilon() {
		if (doneDouble) {
			return dEpsilon;
		}

		double epsilon = 1.0;
		double newEpsilon = 1.0 / 2.0;

		while (1.0 + newEpsilon > 1.0) {
			epsilon = newEpsilon;
			newEpsilon /= 2.0;
		}

		dEpsilon = epsilon;
		doneDouble = true;

		return epsilon;
	}
}
