package GiciEntropyCoder.InterleavedEntropycoder;

import java.util.Arrays;
import java.util.BitSet;

/**
 * @version 1.0
 * @author Juan Ramón Soler Cabrejas
 * 
 * This public class is the responsible of everything related with the decoder,
 * it has constructors to create a decoder instance, by default or with custom
 * circular queue size and/or custom bin cutoffs. It can be used as well in two
 * manners, like the encoder, loading an input with setInput method and using
 * decodeInput after that, or directly 'bit to bit' passing probabilities to
 * readCodeWord method, this last case requires periodic load of input with
 * addInputBit(boolean b) method or an exception of lack of input can be thrown
 * from readCodeWord method. The last also requires to get the output and clear
 * it from the interface each Integer.MaxValue times or a output overflow may
 * throw an Exception. You can also visualize the same interesting graphics
 * about redundancy like in the encoder but this is not so interesting.
 * Finally one very important method this class has is 'getInstanceMirrorCoder',
 * from an InterleavedEntropyEncoder instance it wil configure automatically the
 * decoder an return a proper decoder instance to work with, even if you use in
 * the encoder a non catalogued ShortHandCode, each inverse tree will be
 * calculated.
 */
public class InterleavedEntropyDecoder extends InterleavedEntropyCoder {
  
    int codeWord=0;
    int inputRead=0;
    int inputWrite=0;
    private static final int MAX=Integer.MAX_VALUE;//used for testing
    
    /**
     * This constructor builds an InterleavedEntropyDecoder instance with a
     * given circular queue size and custom probability cuttoffs. The coders
     * used by the constructor by default are the inverse ones in Nasa ICER
     * Interleaved entropy coder. If the length of customBinsCutoffs is not equal
     * to 17, the number of component coders in ICER, then more or less components
     * codes from ICER will be taken. If length of customBinsCutoffs is under 17,
     * the ICER component codes will be truncated and, if it is over 17 they
     * will be extended with copy or transparent codes and the encoder will need
     * further configuration after calling this constructor to work properly.
     * @param circBufferSize int, the desired circular buffer size
     * @param customBinsCutoffs int[], array with every probability cutOff, if
     *        it is null or empty the default values will be used instead, it
     *        must not include the equivalent probability to 0.5/1.0 because it
     *        is added internally. The minimum probability in the array must be
     *        higher than half the maximum probability in the array or an
     *        exception will be thrown.
     */
    public InterleavedEntropyDecoder(int circBufferSize,int[] customBinsCutoffs) {
        
        super(circBufferSize,customBinsCutoffs);
        input=new BitSet();
        codersInit();
    }
    
    /**
     * This constructor builds an InterleavedEntropyDecoder instance with a
     * given circular queue size and default ICER probability cuttoffs and
     * inverse component coders. The coders and cutoffs used by the constructor
     * by default are the ones in Nasa ICER Interleaved entropy coder.
     * @param circBufferSize int, the desired circular buffer size
     */
    public InterleavedEntropyDecoder(int circBufferSize) {
        this(circBufferSize,null);
    }
    
    /**
     * This constructor builds an InterleavedEntropyDecoder instance with a
     * given custom probability cuttoffs. The coders used by the constructor by
     * default are the inverse ones in Nasa ICER Interleaved entropy coder. If
     * the length of customBinsCutoffs is not equal to 17, the number of component
     * coders in ICER, then more or less components codes from ICER will be
     * taken. If length of customBinsCutoffs is under 17, the ICER component
     * codes will be truncated and, if it is over 17 they will be extended with
     * copy or transparent codes and the encoder will need further configuration
     * after calling this constructor to work properly.
     * @param customBinsCutoffs int[], array with every probability cutOff, if
     *        it is null or empty the default values will be used instead, it
     *        must not include the equivalent probability to 0.5/1.0 because it
     *        is added internally. The minimum probability in the array must be
     *        higher than half the maximum probability in the array or an
     *        exception will be thrown.
     */
    public InterleavedEntropyDecoder(int[] customBinsCutoffs) {
        this(defaultCIRCBUFFERSIZE,customBinsCutoffs);
    }
    
    /**
     * This constructor builds an InterleavedEntropyDecoder instance with
     * default ICER probability cuttoffs, inverse component coders and circular
     * buffer size. The coders and cutoffs used by the constructor by
     * default are the ones in Nasa ICER Interleaved entropy coder.
     * The circular buffer size is the one used in ICER as well.
     */
    public InterleavedEntropyDecoder() {
        this(defaultCIRCBUFFERSIZE,null);
    }
    
    /**
     * This constructor builds an InterleavedEntropyDecoder instance with a
     * given circular queue size and custom probability cuttoffs, this
     * constructor is also given the input data to be encoded and probability
     * to encode it with, because input data comes in a BitSet, input size will
     * be equal to the length of probabilities array, which determines how many
     * bits of input will be used for encoding if we call encodeInput().
     * The coders used by the constructor by default are the inverse ones in Nasa
     * ICER Interleaved entropy coder. If the length of customBinsCutoffs is not
     * equal to 17, the number of component coders in ICER, then more or less
     * components codes from ICER will be taken. If length of customBinsCutoffs
     * is under 17, the ICER component codes will be truncated and, if it is
     * over 17 they will be extended with copy or transparent codes and the
     * encoder will need further configuration after calling this constructor
     * to work properly. 
     * @param input BitSet, where the input bits to encode are
     * @param probabilities int[], array to encode each bit with, the length of
     *                              probabilities determines how many bits of
     *                              input will be used.
     * @param circBufferSize int, the desired circular buffer size
     * @param customBinsCutoffs int[], array with every probability cutOff, if
     *        it is null or empty the default values will be used instead, it
     *        must not include the equivalent probability to 0.5/1.0 because it
     *        is added internally. The minimum probability in the array must be
     *        higher than half the maximum probability in the array or an
     *        exception will be thrown
     */
    public InterleavedEntropyDecoder(BitSet input,int[] probabilities,int circBufferSize,int[] customBinsCutoffs) {
        
        super(input,probabilities,circBufferSize,customBinsCutoffs);
        inputWrite=MAX-1; 
        codersInit();
    }
    
    /**
     * This constructor builds an InterleavedEntropyDecoder instance with a
     * given circular queue size and default ICER probability cuttoffs and
     * inverse component coders, this constructor is also given the input data to
     * be encoded and probability to encode it with, because input data comes in
     * a BitSet, length of probabilities determines how many bits of input will
     * be used for the encoding process if we call encodeInput().
     * The inverse coders and cutoffs used by the constructor by default are the
     * ones in Nasa ICER Interleaved entropy coder.
     * @param input BitSet, where the input bits to encode are
     * @param probabilities int[], array to encode each bit with, the length of
     *                              probabilities determines how many bits of
     *                              input will be used.
     * @param circBufferSize int, the desired circular buffer size
     */
    public InterleavedEntropyDecoder(BitSet input,int[] probabilities,int circBufferSize) {
        this(input,probabilities,circBufferSize,null);
    }
    
    /**
     * This constructor builds an InterleavedEntropyDecoder instance with custom
     * probability cuttoffs, for circular buffer size is used the default, the
     * constructor is also given the input data to be encoded and probability
     * to encode it with, because input data comes in a BitSet, input size will
     * be equal to the length of probabilities array, which determines how many
     * bits of input will be used for encoding if we call encodeInput().
     * The coders used by the constructor by default are the inverse ones in Nasa
     * ICER Interleaved entropy coder. If the length of customBinsCutoffs is not
     * equal to 17, the number of component coders in ICER, then more or less
     * components codes from ICER will be taken. If length of customBinsCutoffs
     * is under 17, the ICER component codes will be truncated and, if it is
     * over 17 they will be extended with copy or transparent codes and the
     * encoder will need further configuration after calling this constructor
     * to work properly. 
     * @param input BitSet, where the input bits to encode are
     * @param probabilities int[], array to encode each bit with, the length of
     *                              probabilities determines how many bits of
     *                              input will be used.
     * @param customBinsCutoffs int[], array with every probability cutOff, if
     *        it is null or empty the default values will be used instead, it
     *        must not include the equivalent probability to 0.5/1.0 because it
     *        is added internally. The minimum probability in the array must be
     *        higher than half the maximum probability in the array or an
     *        exception will be thrown
     */
    public InterleavedEntropyDecoder(BitSet input,int[] probabilities,int[] customBinsCutoffs) {
        this(input,probabilities,defaultCIRCBUFFERSIZE,customBinsCutoffs);
    }
    
    /**
     * This constructor is given the input data to be encoded and probability to
     * encode it with, because input data comes in a BitSet, input size will
     * be equal to the length of probabilities array, which determines how many
     * bits of input will be used for encoding if we call encodeInput().
     * This constructor builds an InterleavedEntropyEncoder instance with
     * default ICER probability cuttoffs, inverse component coders and circular
     * buffer size. The inverse coders and cutoffs used by the constructor by
     * default are the ones in Nasa ICER Interleaved entropy coder. The circular
     * buffer size is the one used in ICER as well.
     * @param input BitSet, where the input bits to encode are
     * @param probabilities int[], array to encode each bit with, the length of
     *                              probabilities determines how many bits of
     *                              input will be used.
     */
    public InterleavedEntropyDecoder(BitSet input,int[] probabilities) {
        this(input,probabilities,defaultCIRCBUFFERSIZE,null);
    }
    
   /**
    * This method decodes the input BitSet property calling 'readCodeWord'
    * method a number of times equal to the length of probabilities property
    * array, if probabilities array is null or empty it throws an exception,
    * if input BitSet is empty it throws as well.
    * @return BitSet, the output BitSet property after the encoding of input
    */
    public BitSet decodeInput() {
        if (getProbabilitiesLength()==0) throw new RuntimeException("This method can not be used without probabilities.");
        if (input==null) throw new RuntimeException("The input property can not be null.");
        int p;
        for (int i=0;i<getProbabilitiesLength();i++) {//Aquí obtendrías mayor eficiencia con un iterador ??
            p=probabilities[i];
            try { readCodeWord(p); }
            catch (RuntimeException e) {
              if (e.getMessage()==null) throw e;
              switch (e.getMessage()) {
                case "p out of range":
                  throw new RuntimeException("Invalid value suplied by Probabilistic"
                  +" model at position "+i+". Value has to be beetwen 0 and "+probTree.maxProb(),e);
                case "Inconsistent probabilistic model":
                  throw new RuntimeException("Data and Probabilistic model are incons"
                  +"istent at position "+i+". The probability of 0 is "+p/probTree.maxProb()
                  +" and there is a "+((p==probTree.maxProb())?1:0)+" at that position.",e); 
                default: throw e;
              }
            }
        }
        return output;
    }
    
    /**
     * This method reads a complete codeWord from the input with an unique
     * probability 'p', this can be done because encoder left at his output
     * complete codewords made from the same probability. A better explanation
     * of what this method does exactly can be found in the project documents.
     * @param p int, the probability from probabilities array, with a length
     *                equal to the input length encoder had.
     * @throws RuntimeException if there are not enough bits in input for the
     *         probabilities, 'p' is out of valid range, the probabilistic model
     *         in each 'p' is inconsistent with input or it is found an
     *         output BitSet overflow
     */
    public int readCodeWord(int p) throws RuntimeException {
       
       if (inputWrite==MAX) throw new RuntimeException("inputWrite property overflow");
       if (p>probTree.maxProb()||p<0) throw new RuntimeException("p out of range");
        
       boolean inverse=false; 
       if (p<probTree.midProb()) {
           p+=((probTree.midProb()-p)<<1);
           inverse=true;
       }
       
       int bin=probTree.getBin(p);
       Node n = getNodeByBin(bin);
       
       if (bin==-1 && p==probTree.maxProb()) {
           if (input.get(inputRead)^inverse) {
               throw new RuntimeException("Inconsistent probabilistic model");
           }
           else {
               output.set(usedOutput,inverse);
               if (usedOutput==MAX) throw new RuntimeException("Output BitSet overflow");
               usedOutput++;
           }  
       }
       
       if (n.code==-1 || codeWord-n.code>circBufferSize) {      
           n.entry=coders[bin].coder[0];
           reScaleCodeWords();
           n.code=codeWord;
           if (codeWord==MAX) throw new RuntimeException("codeWord property overflow");
           codeWord++;
       }
       
       while (!(n.entry instanceof TruncatedBinary)) {
           n.entry=n.entry.getSon(input.get(inputRead)?1:0);
           if (n.entry instanceof TruncatedBinary)
               n.entry=new TruncatedBinary((TruncatedBinary)n.entry);
           if (inputRead==MAX) makeInputSpace();
           if (inputRead==MAX) throw new RuntimeException("inputRead property overflow");
           inputRead++;
       }
       
       TruncatedBinary tb = (TruncatedBinary)n.entry;
       output.set(usedOutput,inverse^tb.removeHighBit());
       if (tb.getlength()==0) n.code=-1;
       if (usedOutput==MAX) throw new RuntimeException("Output BitSet overflow");
       usedOutput++;
       return inputWrite-inputRead;
   }
    
   /**
    * This method adds a block of bits to the input in order 'readCodeWord'
    * has enough input ready if we do not use 'setInput' and 'decodeInput'
    * methods, because we do not know in advance how many bits 'readCodeWord'
    * method will need at the next call the maximum bits we think in the worse
    * case, the longer codeword we can get is 512 bits length so we can read
    * blocks of 512 bits unless input data to be charged is not so long already
    * because we are ending the external input. If there is no space in input
    * to write the block 'b' inside, none bit will be charged.
    * @param b boolean[], the BitSet to charge bits from, its length is in
    *                      range [1,512]
    * @return boolean, true if block 'b' has been charged, false otherwise
    */
   public boolean addInputBitBlock(boolean[] b) {
       long pos=inputWrite;
       if (b.length<1) throw new RuntimeException("At least 1 bit must be charged");
       if (b.length>512) throw new RuntimeException("No more than 512 bits must be charged");
       pos+=b.length;
       if (pos>MAX-1) {
           makeInputSpace();
           pos=inputWrite;
           pos+=b.length;
       }
       if (pos>MAX-1) return false;
       for (int i=0;i<b.length;i++) {
           input.set(inputWrite,b[i]);
           inputWrite++;
       }
       return true;
   }
   
   /**
    * This method re-initializes the decoder so can start the decoding of
    * another input without getting and configuring a new decoder intance.
    */
   public void reInitializeDecoder() {
       codeWord=0;
       inputRead=0;
       inputWrite=0;
       input.clear();
       initBinNodeBuffer();
   }
      
   /**
    * {@inheritDoc}
    */
   @Override
   public void setInput(BitSet input,int[] probabilities) {
       super.setInput(input,probabilities);
       codeWord=0;
       inputRead=0;
       inputWrite=MAX-1;
       initBinNodeBuffer();
   }
   
   /**
    * This method shows a graphic of redundancy given probability for a
    * particular component code of this decoder associated to a bin, if the
    * component code associated to the bin does not exist it throws an exception.
    * @param bin int, the bin or container
    * @param pNorm boolean, if true probabilities will be shown form the range
    *                        [0.5,1.0], if false from the range
    *                        [midprob,maxprob], (integers from bins cutOffs)
    */
   public void showRedundancyGraphic(int bin,boolean pNorm) {
    
       if (bin<0 || bin>coders.length-1) throw new RuntimeException("This bin is not in ComponentCode array");
       String n;
       if (coders[bin] instanceof GolombCode) n="G("+coders[bin].m+")";
       else if (coders[bin] instanceof GolombDecode) n="-G("+coders[bin].m+")";
       else if (coders[bin] instanceof ShortHandCode) {
           ShortHandCode shc=(ShortHandCode)coders[bin];
           n=shc.getShortHandString();
       } else n="(0,1) copy code";
       double[][] data=super.getRedundancyData(bin,pNorm);
       String dataLabel;
       if (pNorm) dataLabel="Probability of zero";
       else dataLabel="Probability of zero over "+probTree.maxProb();
       //renderGraphic(data,true,1,"Inverse IEC Redundancy at bin "+bin+"\n"+n,dataLabel,"Redundancy (bits/source bit)","Redundancy as a function of probability of zero");      
   }
   
   /**
    * This method shows a graphic of redundancy given probability for this
    * decoder.
    * @param pNorm boolean, if true probabilities will be shown form the range
    *                        [0.5,1.0], if false from the range
    *                        [midprob,maxprob], (integers from bins cutOffs)
    */
   public void showRedundancyGraphic(boolean pNorm) {
       double[][] data=super.getRedundancyData(pNorm);
       String dataLabel;
       if (pNorm) dataLabel="Probability of zero";
       else dataLabel="Probability of zero over "+probTree.maxProb();
       //renderGraphic(data,true,1,"Inverse IEC Redundancy",dataLabel,"Redundancy (bits/source bit)","Redundancy as a function of probability of zero");
   }
   
   /**
    * This method wil configure de decoder automatically from an
    * InterleavedEntropyEncoder instance and return a proper decoder instance to
    * work with that encoder, even if you use in the encoder a non catalogued
    * ShortHandCode, each inverse tree will be calculated with this method.
    * @param iec InterleavedEntropyEncoder, the instance to invert or mirror
    * @return InterleavedEntropyDecoder, the configured decoder instance
    */
   public static InterleavedEntropyDecoder getInstanceMirrorCoder(InterleavedEntropyEncoder iec) {
       
       if (iec==null) throw new RuntimeException("Parameter iec can not be null");
       InterleavedEntropyDecoder ied=new InterleavedEntropyDecoder();
       ComponentCode[] iedCcs=new ComponentCode[iec.coders.length];
       for (int i=0;i<iec.coders.length;i++) {
           iedCcs[i]=iec.coders[i].inverseCoder();
       }
       ied.setBinsProbsAndCoders(Arrays.copyOfRange(iec.probTree.getCutOffs(),1,iec.probTree.getBINNUM()+1),iedCcs);
       ied.circBufferSize=iec.circBufferSize;
       ied.initBinNodeBuffer();
       ied.setInput(iec.getOutput(),iec.getProbabilities());
       return ied;
   }
    
   /**
    * This method make input space when input becomes full moving data to be
    * read to the begining of the input BitSet, it is an easy alternative to
    * a circular queue, because the especial features of BitSet's this is quick
    * as well. Moves the bits between inputRead and inputWrite to 0 and
    * inputWrite-inputRead;
    */
   private void makeInputSpace() {
       input=input.get(inputRead,inputWrite);
       inputWrite-=inputRead;
       inputRead=0;
   } 
   
   /**
    * This method reduce codeWord number property and all codewords numbers
    * stored in the buffer of size equal to the number of bins or containers,
    * it reduce all of them in the same amount so it avoids an Integer overflow
    * without any other undesired effect.
    */
   private void reScaleCodeWords() {
       if (codeWord==MAX) {
           Node n;
           int min=MAX;
           for (int i=0;i<getBufferSize();i++) {
               n=getNodeByBin(i);
               if (codeWord-n.code>circBufferSize) {
                   setCodeByBin(-1,i);
               } else min=(n.code<min)?n.code:min;
           }
           for (int i=0;i<getBufferSize();i++) {
               n=getNodeByBin(i);
               setCodeByBin(n.code-min,i);
           }
           codeWord-=min;
           if (codeWord==MAX) throw new RuntimeException("Abnormal behavior at function.");
       }
   }
   
   /**
    * This method initializes de internal inverse component codes following the
    * Nasa ICER Interleaved entropy inverse coder specification but respecting
    * the limit of number of components, truncating if the current number of
    * containers is not enough.
    */
   private void codersInit() {  
        ComponentCode[] cod= new ComponentCode[ProbTree.getDefaultBINNUM()];
        cod[0] = new ComponentCode();
        for (int i=1;i<8;i++) cod[i] = ShortHandCode.getShortHandCode(-i);
        cod[8] = new GolombDecode(5);
        cod[9] = new GolombDecode(6);
        cod[10] = new GolombDecode(7);
        cod[11] = new GolombDecode(11);
        cod[12] = new GolombDecode(17);        
        cod[13] = new GolombDecode(31);      
        cod[14] = new GolombDecode(70);
        cod[15] = new GolombDecode(200);
        cod[16] = new GolombDecode(512);
        System.arraycopy(cod,0,coders,0,Math.min(ProbTree.getDefaultBINNUM(),probTree.getBINNUM()));
        initBinNodeBuffer();
    }
}
