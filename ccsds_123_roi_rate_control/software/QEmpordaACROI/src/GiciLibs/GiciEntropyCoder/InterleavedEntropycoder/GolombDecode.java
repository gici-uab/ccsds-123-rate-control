package GiciEntropyCoder.InterleavedEntropycoder;

/**
 * @version 1.0
 * @author Juan Ramón Soler Cabrejas
 * 
 * The purpose of this class is to provide a way to build a GolombDecode
 * instance from his main parameter, m. It also give services to obtain the
 * inverse instance of GolombCode, the entropy of this inverse Golomb code tree
 * and the redundancy for any legal probability in the range [0,1], for this
 * inverse Golomb code or directly for another inverse Golomb code given m.
 */
class GolombDecode extends ComponentCode {
    
    /**
     * Constructor of the GolombDecode class, it builds the code binary tree
     * structure from the main inverse Golomb parameter m.
     * @param m int, main inverse Golomb parameter m, it must be in the range
     *                [2,512]
     */
    public GolombDecode(int m) {
        
        super(m);      
        TruncatedBinary tb = new TruncatedBinary(0,m);
        coder[0].setSon(tb,1);
        coder[0].setSon(coder[1],0);
        short c = (short) Math.ceil(Math.log(m)/Math.log(2));
        short cBitsElements = (short) ((1<<c)-m);
        boolean rice=(cBitsElements==0)?true:false;
        
        if (rice) {
            cBitsElements=(short)m;
            c++;
        }
        
        int idx;
        int parent,jmax=2;
        for (int i=1;i<c-1;i++) {
            for(int j=0;j<jmax;j++) {
                idx=j+jmax;
                parent=idx>>1;
                coder[parent].setSon(coder[idx],idx & 0x01);
            }
            jmax=jmax<<1;

        }
          
        int cLevel = 1<<(c-2);
        for (int i=0;i<cBitsElements;i++) {
            tb = new TruncatedBinary(1,i+1);
            coder[cLevel+(i>>1)].setSon(tb,i & 0x01);
        }
    
        if (!rice) {
            int cNext = cLevel+(cBitsElements>>1);
            int cLast = 1<<(c-1);
            idx = cLast;
            int lengthCount=cBitsElements;
            for (int i=cNext;i<cLast;i++) {
                for (int j=0;j<2;j++) {
                    if (coder[i].getSon(j)==null) {    
                        coder[i].setSon(coder[idx],j);     
                        for (int k=0;k<2;k++) {
                            lengthCount++;
                            tb = new TruncatedBinary(1,lengthCount);
                            coder[idx].setSon(tb,k);
                        }
                        idx++;
                    }
                }
            }
        }
    }

    /**
     * It calculates the entropy of this inverse Golomb code tree from property
     * m following the general formula given in the documentation.
     * @return Double, the entropy of this Golomb code
     */
    public Double getEntropy() {
        Double entrop=Math.ceil(Math.log(m)/Math.log(2));
        int c=entrop.intValue();
        entrop/=2;
        entrop+=new Double(m)/(1<<c);
        return entrop;
    }
    
    /**
     * This static method calculates the redundancy of a suposed inverse Golomb
     * code from parameter m following the formula given in the documentation
     * for any probability in the range [0,1].
     * @param prob Double, probability in the range [0,1]
     * @param m int, main inverse Golomb parameter m, it is not compulsory m
     *                is in the range [2,512] because a GolombDecode instance is
     *                not really made and a general formula is used instead.
     * @return Double, redundancy of a suposed inverse Golomb code with
     *                 parameter m
     */
    protected static Double getRedundancy(Double prob,int m) {

        Double p2=1-prob;
        Double temp=Math.ceil(Math.log(m)/Math.log(2));
        int c=temp.intValue();
        int ones,ceros;
        int outLenght=1;
        Double lS=0.0;
        Double lE=0.0;
        if ((1<<c)-m==0) c++;
        TruncatedBinary tb=new TruncatedBinary(0,c);
        for (int i=0;i<m;i++) {
            ones=tb.numberOfOnes();
            ceros=tb.getlength()-ones;
            temp=Math.pow(prob,ceros)*Math.pow(p2,ones);
            lS+=temp*outLenght;
            outLenght++;
            lE+=temp*tb.getlength();
            if (i+1!=(1<<c)-m) tb.incValue();
            else if (i+1!=m) tb.nextLenghtValue();
        }
        lS+=p2*m;
        lE+=p2;     
        return (lS/lE)-getEntropyFromProb(prob);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Double getRedundancy(Double prob) {
        return getRedundancy(prob,m);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public GolombCode inverseCoder() {
        return new GolombCode(m);
    }
}
