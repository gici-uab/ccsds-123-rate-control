package GiciEntropyCoder.InterleavedEntropycoder;

/**
 * @version 1.0
 * @author Juan Ramón Soler Cabrejas
 * 
 * This class extends the abstract class ProbTree, the purpose of this class
 * is to provide a default implementation ofthat class. This implementation uses
 * the maximum and minimun probabilitis and cutOffs for each bin used in the
 * NASA ICER Interleved entropy coder, it has 17 probabilities cutoffs, 18
 * if we count the maxprob/2 at the beginning of the array.
 */
class QuickProbTree extends ProbTree {
    private static int[] defaultCutOffs={32768,35298,37345,40503,43591,47480,50133,53645,55902,57755,58894,60437,62267,63613,64557,65134,65392,65536};
    private int MAXPROB = getDefaultMAXPROB();//65536;
    private int MIDPROB = getDefaultMIDPROB();//32768;
    
    /**
     * The constructor only asigns to BINNUM the default value 17. BINNUM
     * is the number of bins or containers used in the coder or decoder,
     * it is also the same as the component coders used and the number of
     * cuttoffs without counting the max/2 at the beginning.
     */
    protected QuickProbTree() {
        BINNUM=getDefaultBINNUM();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getCutOffs() {
        return defaultCutOffs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int maxProb() {
        return MAXPROB;
    } 
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int midProb() {
        return MIDPROB;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getBin(int p) {
        
        if (p<57755) {
            if (p<47480) {
                if (p<40503) {
                    if (p<37345) {
                        if (p<35298) {
                            if (p<MIDPROB) 
                                     return -1;
                            else     return  0;
                        }
                        else         return  1;
                    } else           return  2;
                } else {
                    if (p<43591)     return  3;
                    else             return  4;
                }
            } else {
                if (p<53645) {
                    if (p<50133)     return  5;
                    else             return  6;
                } else {
                    if (p<55902)     return  7;
                    else             return  8;
                }
            }
        } else {
            if (p<63613) {
                if (p<60437) {
                    if (p<58894)     return  9;
                    else             return 10;
                } else {
                    if (p<62267)     return 11;
                    else             return 12;
                }
            } else {
                if (p<65134) {
                    if (p<64557)     return 13;
                    else             return 14;
                } else {
                    if (p<65392)     return 15;
                    else {
                      if (p<MAXPROB) return 16;
                      else return -1;
                    }
                }
            }
        }
    }
}
