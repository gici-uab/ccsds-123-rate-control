package GiciEntropyCoder.InterleavedEntropycoder;
/**
 * @version 1.0
 * @author Juan Ramón Soler Cabrejas
 * 
 * Public abstract class with the purpose of act like a factory of objects
 * of his derivatives clases QuickProbTree and CustomProbTree from a common
 * interface and it allows to view both clases like the same for the rest of
 * clases of the paquet at outside the paquet. It also obligues its derivatives
 * to have certain methods and it keeps som default values.
 * 
 * However we can say the main purpose of this public class which is visible
 * outside the API, and its descendants classes, is to associate in a
 * configurable manner probabilities in the range [0.5,1) with a bin or
 * container associated with them.
 */
public abstract class ProbTree {
    static int defaultBINNUM=17;
    static int defaultMAXPROB = 65536;
    static int defaultMIDPROB = 32768;
    int BINNUM;
    
    /**
     * This Static method acts like a factory of objects of the class
     * QuickProbTree, but returning it like a ProbTree.
     * @return ProbTree, the ProbTree instance of QuickProbTree.
     */
    public static ProbTree getInstance() {
        return new QuickProbTree();
    }
    /**
     * This Static method acts like a factory of objects of the class
     * QuickProbTree and CustomProbTree, depending of the contents of his
     * parameter, but it always returns them like a ProbTree instance.
     * @param probs int[], if it is null it makes a QuickProbTree
     *              if it is not null it makes a CustomProbTree, when we
     *              use this option we must not include the half maximum
     *              probability like the first cuttoff, so the number of
     *              elements in the array must be equal to the number of bins
     *              or containers, and also to the number of component coders.
     * @return ProbTree, it returns ProbTree object instance of
     *                   QuickProbTree or CustomProbTree
     */
    public static ProbTree getInstance(int[] probs) {
        if (probs==null || probs.length==0) return new QuickProbTree();
        else                          return new CustomProbTree(probs);
    }

    /**
     * This static method returns the default value for BINNUM, this is 17.
     * BINNUM is the number of containers of an Interleaved Entropy Coder, also
     * related with the num of components coders and the number of probability
     * cuttoffs.
     * @return int, 17
     */
    public static int getDefaultBINNUM() {
        return defaultBINNUM;
    }
    
    /**
     * This static method returns the default value for MAXPROB, this is 65536.
     * MAXPROB is the maximum probability cuttoff in the cutoffs array, this
     * is suposed to be the first beyond the higher bin because it is
     * equivalent to a probability of 1. If CustomProbTree is under use the
     * maximum probability can be diferent from this one.
     * @return int, the default maximum probability
     */
    public static int getDefaultMAXPROB() {
        return defaultMAXPROB;
    }
    
    /**
     * This static method returns the default value for MIDPROB, this is 32768.
     * MIDPROB is half the maximum probability cuttoff in the cutoffs array which
     * is suposed to be the first beyond the higher bin because it is
     * equivalent to a probability of 1, so MIDPROB is equivalent to 1/2.
     * If CustomProbTree is under use the midprob probability can be diferent
     * from this one.
     * @return int, the default half maximum probability
     */
    public static int getDefaultMIDPROB() {
        return defaultMIDPROB;
    }
    
    /**
     * This method returns the actual BINNUM, the default if a QuickProbTree
     * class instance is under use, or can be diferent if we are using
     * CustomProbTree. BINNUM is the number of containers of an Interleaved
     * Entropy Coder, also related with the num of components coders and the
     * number of probability cuttoffs.
     * @return int, the actual BINNUM, number of containers or bins
     */
    public int getBINNUM() {
        return BINNUM;
    }
    /**
     * This method returns the cuttoffs array, including the half maximum
     * probability cuttoff.
     * @return  int[], returns the cuttoffs array including the half maximum
     *                 probability cuttoff at first position of the array
     */
    public abstract int[] getCutOffs();
    
    /**
     * This method returns the actual maximum probability, the default if a
     * QuickProbTree class instance is under use or can be diferent if we are
     * using CustomProbTree. MAXPROB is the maximum probability cuttoff in the
     * cutoffs array, this is the last in the stored array and is suposed to be
     * the first beyond the higher bin because it is equivalent to a probability
     * of 1 in a 0-1 scale.
     * @return int, the actual MAXPROB
     */
    public abstract int maxProb();
    
    /**
     * This method returns the actual half maximum probability, the default if a
     * QuickProbTree class instance is under use or can be diferent if we are
     * using CustomProbTree. MIDPROB is the half maximum probability cuttoff in
     * the cutoffs array, this is suposed to be the first of the stored array
     * and it is equivalent to a probability of 1/2 in a 0-1 scale. Remember
     * when initializing a ProbTree with not default cutOffs, MidProb must not
     * be in the cutOffs array passed to the getInstance method, it will be
     * calculated and added internally.
     * @return int, the actual MIDPROB
     */
    public abstract int midProb();
    
    /**
     * This method returns the number of bin or container associated with
     * certain probability p, taking into account the internal probability
     * stored cutOffs, implicitly or explicitly.
     * @param p int, the probability we want to know the bin about
     * @return int, the number which identify the bin or container, from 0 to
     *              BINNUM-1
     */
    public abstract int getBin(int p);
}
