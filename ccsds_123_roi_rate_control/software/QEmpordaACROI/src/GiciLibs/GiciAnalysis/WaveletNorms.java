package GiciAnalysis;

public class WaveletNorms {
	// 5/3
	static private final double[] LP5_3 = {1.22474487139159, 1.6583123951777, 2.31840462387393, 3.26917420765551, 4.61992965314408, 6.53237131522696, 9.23774526061419, 13.063995129745, 18.4752262333916, 26.127896819061, 36.9504194305525, 52.2557819580463};
	static private final double[] HP5_3 = {0.847791247890659, 0.960143218483576, 1.25934010497562, 1.74441071711911, 2.45387130367507, 3.46565176950888, 4.89952763985979, 6.92839704021608, 9.79802749401314, 13.8564306871113, 19.5959265076536, 27.7128159494245};
	// 9/7
	static private final double[] LP9_7 = {1.40210816792974,2.03037185608179,2.90116255627856,4.1152851751758,5.82451086377281,8.23875993457252,11.6519546479211,16.4785606470644,23.3042776444607,32.957251561374};
	static private final double[] HP9_7 = {0.721261382508077,0.983471304122789,1.44196240413945,2.07376041967166,2.94732487653391,4.17358945892957,5.90430232755241,8.35063902078243,11.8098328516124,16.7017127554297};


	public static double[] getNorms1D(final int waveletFilter, final int levels, final int components) {
		final double[] LP;
		final double[] HP;

		switch (waveletFilter) {
		case 1: // 5/3 isonorm
			LP = LP5_3;
			HP = HP5_3;
			break;
		case 2: // 9/7 isonorm
			LP = LP9_7;
			HP = HP9_7;
			break;
		default:
			throw new Error("Norms are not available for this wavelet transform.");
		}

		if (levels < 1 || levels > LP.length) {
			throw new Error("Norms are not available for this many levels of this wavelet transform.");
		}

		final double[] result = new double[components];
		int resultIndex = result.length - 1;

		int currentComponents = components;

		for (int l = 0; l < levels; l++) {
			int currentHighPassSize = currentComponents / 2; 
			currentComponents -= currentHighPassSize; 

			for (int i = 0; i < currentHighPassSize; i++) {
				result[resultIndex--] = HP[l];
			}
		}

		for (int i = 0; i < currentComponents; i++) {
			result[resultIndex--] = LP[levels - 1];
		}

		return result;
	}

}
