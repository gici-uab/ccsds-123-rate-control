package GiciEntropyCoder.InterleavedEntropycoder;

/**
 * @version 1.0
 * @author Juan Ramón Soler Cabrejas
 * 
 * The purpose of this class is to provide a way to build a GolombCode instance
 * from his main parameter, m. It also give services to obtain the inverse
 * instance of GolombDecode, the entropy of this Golomb code tree and the
 * redundancy for any legal probability in the range [0,1], for this Golomb code
 * or directly for another Golomb code given m.
 */
class GolombCode extends ComponentCode {

    /**
     * Constructor of the GolombCode class, it builds the code binary tree
     * structure from the main Golomb parameter m.
     * @param m int, main Golomb parameter m, it must be in the range [2,512]
     */
    public GolombCode(int m) {
        
        super(m);
        int i;

        short c = (short) Math.ceil(Math.log(m)/Math.log(2));
        short cBitsElements = (short) ((1<<c)-m);
        boolean rice=(cBitsElements==0)?true:false;
        
        if (rice) {
            cBitsElements=(short)m;
            c++;
        }
        
        for (i=1;i<m;i++) {
            coder[i-1].setSon(coder[i],0);
        }
        
        TruncatedBinary tb = new TruncatedBinary(0,c);
        
        for (i=0;i<cBitsElements-1;i++) {
            coder[i].setSon(tb,1);
            tb = new TruncatedBinary(tb);
            tb.incValue();
        }
        coder[i].setSon(tb,1);
        
        if (!rice) {
            tb = new TruncatedBinary(tb);
            tb.nextLenghtValue();
            for (i=cBitsElements;i<m-1;i++) {
                coder[i].setSon(tb,1);
                tb = new TruncatedBinary(tb);
                tb.incValue();
            }
            coder[i].setSon(tb,1);
        }
        coder[i].setSon(new TruncatedBinary(1,1),0);        
    }
    
    /**
     * It calculates the entropy of this Golomb code tree from property m
     * following the general formula given in the documentation.
     * @return Double, the entropy of this Golomb code
     */
    @Override
    public Double getEntropy() {
        Double entrop=new Double(m);
        Double denom=Math.pow(2,m-1);
        entrop/=denom;
        for (Double i=1.0;i<m;i++) {
            entrop+=i/Math.pow(2,i);
        }
        return entrop;
    }
    
    /**
     * This static method calculates the redundancy of a suposed Golomb Code
     * from parameter m following the formula given in the documentation for
     * any probability in the range [0,1].
     * @param prob Double, probability in the range [0,1]
     * @param m int, main Golomb parameter m, it is not compulsory m is in
     *                the range [2,512] because a GolombCode instance is not
     *                really made and a general formula is used instead.
     * @return Double, redundancy of a suposed Golomb Code with parameter m
     */
    protected static Double getRedundancy(Double prob, int m) {
        Double red=Math.ceil(Math.log(m)/Math.log(2));
        int c=red.intValue();
        red=Math.pow(prob,(1<<c)-m)+(c*(1-Math.pow(prob,m)));
        Double denom=1.0;
        for (int i=1;i<m;i++) {
            denom+=Math.pow(prob,i);
        }
        red/=denom;
        red-=getEntropyFromProb(prob);
        return red;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Double getRedundancy(Double prob) {
        return getRedundancy(prob,m);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public GolombDecode inverseCoder() {
        return new GolombDecode(m);
    }
}
