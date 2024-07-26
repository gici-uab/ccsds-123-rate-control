package GiciEntropyCoder.InterleavedEntropycoder;

import java.util.Arrays;

/**
 * @version 1.0
 * @author Juan Ramón Soler Cabrejas
 * 
 * The purpose of this class is to provide alternative methods to configure out
 * of default values the number of bins or containers and the probability
 * cutOffs, which define the rangs of aplicability of each container and the rang
 * for the associated component codes.
 */
class CustomProbTree extends ProbTree {
    
    private int MAXPROB;
    private int MIDPROB;
    private int[] probCutoffs;
    
    /**
     * This is the constructor, it receives an int array with the cuttOffs
     * without the half maximum probability included because it will be added
     * automatically to the stored array.
     * @param probs int[], The probability cutOffs, there can not be repeated
     *                     elements in the array but it can be out of order,
     *                     it will be ordered automatically. The half maximum
     *                     value in the array needs to be lower than the minimum
     *                     in order to construct properly the internal cutOffs.
     *                     Probability array length must be in range [1,512], if
     *                     higher than 512 an exception will be thrown.
     */
    CustomProbTree(int[] probs) {
        if (probs==null) throw new RuntimeException("Probability array must not be null");
        if (probs.length>(1<<9))
            throw new RuntimeException("Probability array length must not be greater 512");
        BINNUM=probs.length;
        Arrays.sort(probs);
        for (int i=0;i<probs.length-1;i++) {
            if (probs[i]==probs[i+1]) throw new RuntimeException("There are cutOffs with equal value in probs array at index: "+i);
        }
        probCutoffs = new int[probs.length+1];
        System.arraycopy(probs, 0, probCutoffs, 1, probs.length);
        MAXPROB=probCutoffs[probCutoffs.length-1];
        MIDPROB=MAXPROB>>1;
        probCutoffs[0]=MIDPROB;
        if (probCutoffs[0]>=probCutoffs[1])
            throw new RuntimeException("MIDPROB cutOff must be less than lower element in probs array parameter");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getCutOffs() {
        return Arrays.copyOfRange(probCutoffs,0,probCutoffs.length);
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
        if (p>=MAXPROB || p<MIDPROB) return -1;
        int bin=Arrays.binarySearch(probCutoffs,p);
        return (bin<0)?Math.abs(bin+2):bin; 
    }
}
