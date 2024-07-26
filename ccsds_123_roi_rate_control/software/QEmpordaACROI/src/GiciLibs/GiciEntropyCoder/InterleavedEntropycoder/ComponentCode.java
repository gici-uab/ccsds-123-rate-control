package GiciEntropyCoder.InterleavedEntropycoder;

/**
 * @version 1.0
 * @author Juan Ramón Soler Cabrejas
 * 
 * The purpose of this public class is to be the ancestor of the distinct kinds
 * of Component Codes which can be used, this is the GolombCode, parametrized
 * with m, his inverse tree, called GolombDecode, also dependent of m, and the
 * ShortHandCode, another prefix code not in the Golomb family and specified
 * by an String using the short hand notation. At the same time this class
 * provides other services like a quick constructor of the copy code, also
 * a posible ComponentCode and equivalent to ShortHandCode("(0,1)"). The class
 * also keeps the common data of all component codes and has some methods useful
 * for all of them, to prepare data for their constructors, check common
 * requisites, get a simple entropy from a probability and make visible some
 * methods of his descendants to the exterior of the paquet.
 */
public class ComponentCode {
    
    short m;  
    TreeNode[] coder;
       
    /**
     * The Copy Code constructor, this code leaves things like he gets, a 0 is
     * transformed to a 0 and a 1 to a 1.
     */
    ComponentCode() {
        init(1);
        coder[0].setSon(new TruncatedBinary(0,1),0);
        coder[0].setSon(new TruncatedBinary(1,1),1);
    }
    
    /**
     * This constructor check some aspects about the validity of a string
     * suposed to be in Short Hand notation, after this initialize the coder
     * space needed to keep a ShortHandCode instance counting how many chars '('
     * he can find. This constructor is called by ShortHandCode constructor
     * instance before anything else because that constructor does not know in
     * advance the needed space to keep the code tree structure.
     * @param shortHandTree the String to check space and validity conditions
     */
    ComponentCode(String shortHandTree) {
        int count=getCharCount(shortHandTree,'(');
        if (count<1) {
            throw new RuntimeException("ShortHand Tree notation is wrong or has"
                    + " not any node. There are not any ( symbol.");
        }
        if (count!=getCharCount(shortHandTree,')'))
            throw new RuntimeException("ShortHand Tree notation is wrong. "
                    + "Number of ( and ) does not match.");
        if (count!=getCharCount(shortHandTree,','))
            throw new RuntimeException("ShortHand Tree notation is wrong. "
                    + "Number of ( and , does not match."); 
        init(count);
    }
    
    /**
     * This constructor is called at first part of specialized constructors for
     * GolombCode and GolombDecode classes, this is done to check common
     * validity conditions and initialize the needed structures for the code,
     * this is the code tree and the parameter m.
     * @param m int, the m parameter of a GolombCode or GolombDecode
     */
    ComponentCode(int m) {
        if (m>512) throw new RuntimeException("Maximum m value allowed is 512");
        if (m<2) throw new RuntimeException("Minimum m value allowed is 2, "
                + "codes with m=1 has not sense");
        init(m);
    }
    
    /**
     * This method is the one who initializes the espace needed for any kind of
     * prefix code given m TreeNode's needed in his tree structure. For Golomb
     * codes family this is also his normal parameter, for the rest is just the
     * amount of needed space.
     * @param m int, the amount of TreeNode's nodes needed by the code tree
     */
    private void init(int m) {
        this.m=(short)m;
        coder = new TreeNode[m];
        for (int i=0;i<m;i++) {
            coder[i]=new TreeNode();
        }
    }
    
    /**
     * This method just returns the property m of any ComponentCode instance,
     * this is always the space in use or number of TreeNode's needed in his
     * tree structure. For Golomb code family is also the characteristic
     * parameter.
     * @return short, the m property, amount of space in use by the coder tree
     */
    short getM() {
        return m;
    }
    
    /**
     * This method counts the number of ocurrences of a char in a String
     * @param shortHandTree a suposed ShortHand notation string
     * @param c a char to be searched in the string
     * @return int, the number of ocurrences of the char in the string
     */
    final int getCharCount(String shortHandTree,char c) {
        int idx=0;
        int count=0;
        while ((idx=shortHandTree.indexOf(c,idx))!=-1) {
            count++;
            idx++;
        }
        return count;
    }
    
    /**
     * This method returns the redundancy this code have encoding a bit with
     * certain probability.
     * @param prob the probability of the bit, the normal use of the function
     *              demands prob to be in the range 0.5 - 1.0 but any
     *              probability in the range [0,1] is allowed.
     * @return Double, the redundancy given the probability prob. 
     */
    Double getRedundancy(Double prob) {
        return 1.0-getEntropyFromProb(prob);
    }
    
    /**
     * This method returns the flush bit for a node of a Tree, it is normally
     * 0 but it is overriden in ShortHandCode and could be not in the case this
     * object is an instance of ShortHandCode.
     */
    boolean getFlushBit(TreeNode act) {
        return false;
    }
    
    /**
     * This method returns the entropy of a copy code, but if the ComponentCode
     * instance is also instance of GolombCode, GolombDecode or ShortHandCode
     * then returns the entropy using their override method.
     * @return Double, the entropy of the code
     */
    public Double getEntropy() {
        return 1.0;
    }
    
    /**
     * This static method allows to calculate the entropy of a probability with
     * its complementary probability.
     * @param p1 Double, the probability
     * @return Double, the entropy
     */
    static public Double getEntropyFromProb(Double p1) {
        Double p2=1.0-p1;
        Double log2=Math.log(2);
        Double logP1,logP2;
        if (p1==0.0) logP1=0.0;
        else logP1=Math.log(p1);
        if (p2==0.0) logP2=0.0;
        else logP2=Math.log(p2);
        return Math.abs(-((p1*(logP1/log2))+(p2*(logP2/log2))));  
    }
    
    /**
     * This method returns the inverse of this ComponentCode instance.
     * @return ComponentCode, the inverse of this ComponentCode instance.
     */
    public ComponentCode inverseCoder() {
        return new ComponentCode();
    }
    
    /**
     * This static method makes visible the constructor of the copy code and
     * allows to get a copy code ComponentCode instance from outside the API.
     * @return ComponentCode, a copy code ComponentCode instance
     */
    public static ComponentCode getCopyCode() {
        return new ComponentCode();
    }
   
    /**
     * This static method makes visible the constructor of the ShortHandCode
     * class and allows to get a ComponentCode instance of ShortHandCode from 
     * outside the API with a Short Hand notation string.
     * @param shortHandString the Short Hand notation string
     * @return ComponentCode, instance of ShortHandCode
     */
    public static ComponentCode getShortHandCode(String shortHandString) {
        return new ShortHandCode(shortHandString,0);
    }
    
    /**
     * This static method makes visible the constructor of the ShortHandCode
     * class and allows to get a ComponentCode instance of ShortHandCode from
     * outside the API, the method returns the inverse code to the one specified
     * by the Short Hand notation string.
     * @param shortHandString the Short Hand notation string to invert
     * @return ComponentCode, the inverse instance of ShortHandCode
     */
    public static ComponentCode getInverseShortHandCode(String shortHandString) {
        return new ShortHandCode(shortHandString,0).inverseCoder();
    }
    
    /**
     * This static method makes visible to outside the API the static method of
     * ShortHandCode class which allows to get a ComponentCode instance of
     * ShortHandCode from an 'id' number in the rangs 1 to 7 or -1 to -7, this
     * are ShortHandCode instances used by the default NASA ICER
     * Interleaved entropy coder, [1,7] are normally used in the ICER coder and
     * the inverses coders [-7,-1] in ICER decoder, but you can play.
     * @param id int, the identification number in the rangs 1 to 7 or -1 to -7
     *                 The negatives are the inverses of the positives.
     *                 For example 1 is the first catalogued code and -1 is his
     *                 inverse.
     * @return ComponentCode, instance of a catalogued ShortHandCode
     */
    public static ComponentCode getICERShortHandCode(int id) {
        return ShortHandCode.getShortHandCode(id);
    }
   
    /**
     * This static method makes visible the constructor of the GolombCode class
     * and allows to get a ComponentCode instance of GolombCode from 
     * outside the API given a parameter m.
     * @param m int, the parameter m, it must be between 2 and 512 included
     * @return ComponentCode, instance of GolombCode with parameter m
     */
    public static ComponentCode getGolombCode(int m) {
        return new GolombCode(m);
    }
   
    /**
     * This static method makes visible the constructor of the GolombDecode
     * class and allows to get a ComponentCode instance of GolombDecode from 
     * outside the API given a parameter m.
     * @param m int, the parameter m, it must be between 2 and 512 included
     * @return ComponentCode, instance of GolombDecode with parameter m
     */
    public static ComponentCode getGolombDecode(int m) {
        return new GolombDecode(m);
    }
    
    /**
     * This static method returns all the not Golomb family NASA ICER
     * Interleaved entropy codes, ordered and in an array of ComponentCode,
     * this can be useful in order to design another coder starting from here.
     * @return ComponentCode[], an ordered array with all the not Golomb family
     *                          NASA ICER Interleaved entropy codes
     */
    public static ComponentCode[] getICERInitialCodes() {
        ComponentCode[] ret = new ComponentCode[8];
        ret[0]=new ComponentCode();
        for (int i=1;i<8;i++) ret[i] = ShortHandCode.getShortHandCode(i);
        return ret;
    }
    
    /**
     * This static method returns all the not Golomb family NASA ICER
     * Interleaved entropy inverse codes, ordered and in an array of
     * ComponentCode, this can be useful in order to design another decoder
     * starting from here.
     * @return ComponentCode[], an ordered array with all the not Golomb family
     *                          NASA ICER Interleaved entropy inverse codes
     */
    public static ComponentCode[] getICERInverseInitialCodes() {
        ComponentCode[] ret = new ComponentCode[8];
        ret[0]=new ComponentCode();
        for (int i=1;i<8;i++) ret[i] = ShortHandCode.getShortHandCode(-i);
        return ret;
    }
}
