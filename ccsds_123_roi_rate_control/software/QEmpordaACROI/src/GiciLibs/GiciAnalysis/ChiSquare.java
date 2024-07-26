/**
 * @file ChiSquare.java
 * @author Miguel Hernández Cabronero <mhernandez@deic.uab.cat>
 * @date 08/02/2012
 * @version 1.0
 *
 * @brief Calculate Chi-square probabilities and critical values,
 * based on the code in http://www.swogstat.org/stat/public/chisq_calculator.htm
 */

package GiciAnalysis;

public class ChiSquare {
    /* Max value to represent exp(x) */
    private static double BIGX = 20.0;
    /* Accuracy of critchi approximation */
    static double CHI_EPSILON = 0.00000001;
    /* Maximum chi-square value */
    static double CHI_MAX = 99999.0;

    /**
     * Calculate and return 1 - p, where
     *                  p = P(Chi^2(n) <= v),                           (1)
     * n are the degrees of freedom and v is the critical value.
     *
     * @param degreesOfFreedom number of degrees of freedom, n in (1).
     * @param value critical value, v in (1)
     * @return 1-p, p in (1). Unless dof <= 0, then -1 is returned
     */
    public static double getSignificanceLevel(int degreesOfFreedom, double value) {
//         function pochisq(x, df)
        double a;
        double y = 0;
        double s;
        double e;
        double c;
        double z;
        double x = value;
        int df = degreesOfFreedom;
        boolean even = (df % 2 == 0 ? true : false);
        double LOG_SQRT_PI = 0.5723649429247000870717135; /* log(sqrt(pi)) */
        double I_SQRT_PI = 0.5641895835477562869480795;   /* 1 / sqrt(pi) */

        if (degreesOfFreedom <= 0) {
            // throw new IllegalArgumentException("Degrees of freedom cannot be <= 0");
            return -1;
        }
        if (x <= 0.0 || df < 1) {
            return 1.0;
        }

        a = 0.5 * x;
        if (df > 1) {
            y = ex(-a);
        }
        s = (even ? y : (2.0 * poz(-Math.sqrt(x))));
        if (df > 2) {
            x = 0.5 * (df - 1.0);
            z = (even ? 1.0 : 0.5);
            if (a > BIGX) {
                e = (even ? 0.0 : LOG_SQRT_PI);
                c = Math.log(a);
                while (z <= x) {
                    e = Math.log(z) + e;
                    s += ex(c * z - a - e);
                    z += 1.0;
                }
                return s;
            } else {
                e = (even ? 1.0 : (I_SQRT_PI / Math.sqrt(a)));
                c = 0.0;
                while (z <= x) {
                    e = e * (a / z);
                    c = c + e;
                    z += 1.0;
                }
                return c * y + s;
            }
        } else {
            return s;
        }
    }


    /**
     * Calculate the critical value for the given degrees of freedom and significance
     * level.
     *
     * @param degreesOfFreedom number of degrees of freedom. Should be n = k - 1 - d
     * if there are k possible outcomes, and d parameters of the probability distribution
     * used for the null hypothesis H_0 have been stimated.
     * @param significanceLevel level of significance for which the critical value
     * is calculated. Usually, this level will lay between 0.05 and 0.01.
     * @return the critical value for d freedom degrees and significance level a,
     * that is, the value V so that
     *                  P(Chi^2(n) <= V) = 1 - a
     */
    public static double getCriticalValue(int degreesOfFreedom, double significanceLevel) {
        //     function critchi(p, df) {
        double p = significanceLevel;
        double df = degreesOfFreedom;
        double minchisq = 0.0;
        double maxchisq = CHI_MAX;
        double chisqval;
        if (degreesOfFreedom <= 0) {
            throw new IllegalArgumentException("Degrees of freedom cannot be <= 0");
        }
        if (significanceLevel < 0.0 || significanceLevel > 1.0) {
            throw new IllegalArgumentException("Significance level = " + significanceLevel + " not in [0,1]");
        }
        if (p <= 0.0) {
            return maxchisq;
        } else {
            if (p >= 1.0) {
                return 0.0;
            }
        }

        chisqval = df / Math.sqrt(p);    /* fair first value */
        while ((maxchisq - minchisq) > CHI_EPSILON) {
            if (getSignificanceLevel((int) Math.round(df), chisqval) < p) {
                maxchisq = chisqval;
            } else {
                minchisq = chisqval;
            }
            chisqval = (maxchisq + minchisq) * 0.5;
        }
        return chisqval;
    }

    private static double ex(double x) {
        return (x < -BIGX) ? 0.0 : Math.exp(x);
    }

    private static double poz(double z) {
        /*
         * The following JavaScript functions for calculating normal and
         * chi-square probabilities and critical values were adapted by
         * John Walker from C implementations
         * written by Gary Perlman of Wang Institute, Tyngsboro, MA
         * 01879.  Both the original C code and this JavaScript edition
         * are in the public domain.
         */
        //     function poz(z) {
        double y;
        double x;
        double w;
        /* Maximum meaningful z value */
        double Z_MAX = 6.0;

        if (z == 0.0) {
            x = 0.0;
        } else {
            y = 0.5 * Math.abs(z);
            if (y >= (Z_MAX * 0.5)) {
                x = 1.0;
            } else if (y < 1.0) {
                w = y * y;
                x = ((((((((0.000124818987 * w
                         - 0.001075204047) * w + 0.005198775019) * w
                         - 0.019198292004) * w + 0.059054035642) * w
                         - 0.151968751364) * w + 0.319152932694) * w
                         - 0.531923007300) * w + 0.797884560593) * y * 2.0;
            } else {
                y -= 2.0;
                x = (((((((((((((-0.000045255659 * y
                               + 0.000152529290) * y - 0.000019538132) * y
                               - 0.000676904986) * y + 0.001390604284) * y
                               - 0.000794620820) * y - 0.002034254874) * y
                               + 0.006549791214) * y - 0.010557625006) * y
                               + 0.011630447319) * y - 0.009279453341) * y
                               + 0.005353579108) * y - 0.002141268741) * y
                               + 0.000535310849) * y + 0.999936657524;
            }
        }

        return z > 0.0 ? ((x + 1.0) * 0.5) : ((1.0 - x) * 0.5);
    }

    public static void main(String[] argv) throws Exception {
        ChiSquare cs = new ChiSquare();
        int decimalPlaces = 4;

        // Array of (dof, cv, expected) values for significance level
        double[][] significanceLevels = {
            // dof = 1
            {1, 0, 1},
            {1, 0.1, 0.7518},
            {1, 2, 0.1573},
            {1, 3, 0.0833},
            {1, 10, 0.0016},
            {1, 100, 0},

            // dof = 255
            {255, 0, 1},
            {255, 10, 1},
            {255, 200, 0.9954},
            {255, 250, 0.5766},
            {255, 300, 0.0277},
            {255, 500, 0},
            {255, 10000, 0},

            // dof = 65535
            {65535, 0, 1},
            {65535, 100, 1},
            {65535, 1000, 1},
            {65535, 10000, 1},
            {65535, 20000, 1},
            {65535, 30000, 1},
            {65535, 40000, 1},
            {65535, 50000, 1},
            {65535, 60000, 1},
            {65535, 65000, 0.9306},
            {65535, 65500, 0.5378},
            {65535, 65900, 0.1567},
            {65535, 70000, 0}
        };
        System.out.println("** Testing " + significanceLevels.length +
                           " significance level calculation examples...");
        for (int i = 0; i < significanceLevels.length; i++) {
            int dof =  (int) Math.round(significanceLevels[i][0]);
            double value = significanceLevels[i][1];
            double expected = significanceLevels[i][2];

            if (compareValues(cs.getSignificanceLevel(dof, value), expected, decimalPlaces) == false) {
                throw new Exception("Wrong significance level calculation for\n" +
                                    "\rdof = " + dof + ",\n" +
                                    "\rvalue = " + value + ",\n" +
                                    "\robtained = " + cs.getSignificanceLevel(dof, value) + ",\n" +
                                    "\rexpected = " + expected);
            }
        }
        System.out.println("Ok!");


        decimalPlaces = 0;
        double[][] criticalValues = {
            // dof = 1
            {1, 0.99, 0.0002},
            {1, 0.95, 0.0039},
            {1, 0.5, 0.4549},
            {1, 0.05, 3.841},
            {1, 0.01, 6.635},

            // dof = 255
            {255, 0.99 , 205},
            {255, 0.95 , 219},
            {255, 0.5  , 254},
            {255, 0.05 , 293},
            {255, 0.01 , 310},

            // dof = 65535
            {65535, 0.99, 64696},
            {65535, 0.95, 64941},
            {65535, 0.5 , 65534},
            {65535, 0.05, 66132},
            {65535, 0.01, 66380}
        };
        System.out.println("** Testing " + criticalValues.length +
                           " critical value calculation examples...");
        for (int i = 0; i < criticalValues.length; i++) {
            int dof = (int) Math.round(criticalValues[i][0]);
            double significance = criticalValues[i][1];
            double expected = criticalValues[i][2];

            if (compareValues(cs.getCriticalValue(dof, significance), expected, decimalPlaces) == false) {
                throw new Exception("Wrong critical value calculation for\n" +
                                    "\rdof = " + dof + ",\n" +
                                    "\rsignificance = " + significance+ ",\n" +
                                    "\robtained = " + cs.getCriticalValue(dof, significance) + ",\n" +
                                    "\rexpected = " + expected);
            }
        }
        System.out.println("Ok!");
    }

    /**
     * check that the two values are equal at least up to the number
     * of decimal places
     *
     * @param val1 first value to compare
     * @param val2 second value to compare
     */
    private static boolean compareValues(double val1, double val2, int decimalPlaces) {
       double factor = Math.pow(10, decimalPlaces);

       return (Math.round(val1*factor) == Math.round(val2*factor));
    }
}
