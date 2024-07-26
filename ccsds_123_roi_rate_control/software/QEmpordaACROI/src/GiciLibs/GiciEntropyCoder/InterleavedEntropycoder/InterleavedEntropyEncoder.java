package GiciEntropyCoder.InterleavedEntropycoder;

import java.util.Arrays;
import java.util.BitSet;

/**
 * @version 1.0
 * @author Juan Ramón Soler Cabrejas
 * 
 * This public class is the responsible of everithing related with the encoder,
 * it has constructors to create an encoder instance, like ICER one or diferent,
 * and methods to encode an input with the created encoder, given a loaded input
 * or bit by bit. This class has an important automatic configurator for the
 * encoder which from a reduce set of initial component codes can work to get a
 * nearly optimal code with less or more desired component coders, from 2 to 512
 * , this automatic method even has improved the NASA ICER component coders for
 * Interleaved entropy coders with 17 bins. It also has a method to change a
 * configuration parameter from an already created instance of this class, the
 * circular buffer size, and a couple of methods to show renundancy graphics
 * given probability for the current configurated encoder, for the whole range
 * [0.5,1.0] or the range under certain specified bin or container.
 */
public class InterleavedEntropyEncoder extends InterleavedEntropyCoder {
    
    CircularQueue cq=new CircularQueue(circBufferSize);
    
    /**
     * This constructor builds an InterleavedEntropyEncoder instance with a
     * given circular queue size and custom probability cuttoffs. The coders
     * used by the constructor by default are the ones in Nasa ICER Interleaved
     * entropy coder. If the length of customBinsCutoffs is not equal to 17, the
     * number of component coders in ICER, then more or less components codes
     * from ICER will be taken. If length of customBinsCutoffs is under 17,
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
    public InterleavedEntropyEncoder(int circBufferSize,int[] customBinsCutoffs) { 
    	super(circBufferSize,customBinsCutoffs);  
    	codersInit();
    }
    
    /**
     * This constructor builds an InterleavedEntropyEncoder instance with a
     * given circular queue size and default ICER probability cuttoffs and
     * component coders. The coders and cutoffs used by the constructor by
     * default are the ones in Nasa ICER Interleaved entropy coder.
     * @param circBufferSize int, the desired circular buffer size
     */
    public InterleavedEntropyEncoder(int circBufferSize) { 
        this(circBufferSize,null);  
    }
    
    /**
     * This constructor builds an InterleavedEntropyEncoder instance with a
     * given custom probability cuttoffs. The coders used by the constructor by
     * default are the ones in Nasa ICER Interleaved entropy coder. If the
     * length of customBinsCutoffs is not equal to 17, the number of component
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
    public InterleavedEntropyEncoder(int[] customBinsCutoffs) { 
        this(defaultCIRCBUFFERSIZE,customBinsCutoffs);  
    }
    
    /**
     * This constructor builds an InterleavedEntropyEncoder instance with
     * default ICER probability cuttoffs, component coders and circular buffer
     * size. The coders and cutoffs used by the constructor by
     * default are the ones in Nasa ICER Interleaved entropy coder.
     * The circular buffer size is the one used in ICER as well.
     */
    public InterleavedEntropyEncoder() { 
    	
        this(defaultCIRCBUFFERSIZE,null);  
    }
    
    /**
     * This constructor builds an InterleavedEntropyEncoder instance with a
     * given circular queue size and custom probability cuttoffs, this
     * constructor is also given the input data to be encoded and probability
     * to encode it with, because input data comes in a BitSet, input size will
     * be equal to the length of probabilities array, which determines how many
     * bits of input will be used for encoding if we call encodeInput().
     * The coders used by the constructor by default are the ones in Nasa ICER
     * Interleaved entropy coder. If the length of customBinsCutoffs is not
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
    public InterleavedEntropyEncoder(BitSet input,int[] probabilities,int circBufferSize,int[] customBinsCutoffs) {
        super(input,probabilities,circBufferSize,customBinsCutoffs);
        codersInit();
    }
    
    /**
     * This constructor builds an InterleavedEntropyEncoder instance with a
     * given circular queue size and default ICER probability cuttoffs and
     * component coders, this constructor is also given the input data to be
     * encoded and probability to encode it with, because input data comes in
     * a BitSet, length of probabilities determines how many bits of input will
     * be used for the encoding process if we call encodeInput().
     * The coders and cutoffs used by the constructor by default are the ones
     * in Nasa ICER Interleaved entropy coder.
     * @param input BitSet, where the input bits to encode are
     * @param probabilities int[], array to encode each bit with, the length of
     *                             probabilities determines how many bits of
     *                             input will be used.
     * @param circBufferSize int, the desired circular buffer size
     */
    public InterleavedEntropyEncoder(BitSet input,int[] probabilities,int circBufferSize) {
        this(input,probabilities,circBufferSize,null);
    }
    
    /**
     * This constructor builds an InterleavedEntropyEncoder instance with custom
     * probability cuttoffs, for circular buffer size is used the default, the
     * constructor is also given the input data to be encoded and probability
     * to encode it with, because input data comes in a BitSet, input size will
     * be equal to the length of probabilities array, which determines how many
     * bits of input will be used for encoding if we call encodeInput().
     * The coders used by the constructor by default are the ones in Nasa ICER
     * Interleaved entropy coder. If the length of customBinsCutoffs is not
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
    public InterleavedEntropyEncoder(BitSet input,int[] probabilities,int[] customBinsCutoffs) {
        this(input,probabilities,defaultCIRCBUFFERSIZE,customBinsCutoffs);
    }
    
    /**
     * This constructor is given the input data to be encoded and probability to
     * encode it with, because input data comes in a BitSet, input size will
     * be equal to the length of probabilities array, which determines how many
     * bits of input will be used for encoding if we call encodeInput().
     * This constructor builds an InterleavedEntropyEncoder instance with
     * default ICER probability cuttoffs, component coders and circular buffer
     * size. The coders and cutoffs used by the constructor by default are the
     * ones in Nasa ICER Interleaved entropy coder. The circular buffer size is
     * the one used in ICER as well.
     * @param input BitSet, where the input bits to encode are
     * @param probabilities int[], array to encode each bit with, the length of
     *                              probabilities determines how many bits of
     *                              input will be used.
     */
    public InterleavedEntropyEncoder(BitSet input,int[] probabilities) {
        this(input,probabilities,defaultCIRCBUFFERSIZE,null);
    }   
      
    /**
     * This method reads a bit from an external input of bits and a probability
     * 'p' for that bit, this input is not necessarily the input BitSet property,
     * this method can be called without filling first input and probabilities
     * arrays from the constructors or 'setInput(BitSet input,int[] probabilities)'
     * method. Then any length input can be read this way, the only condition is
     * when all input has been red it must been called 'emptyAll()' from this
     * class, to flush out the last bits to the output. After that the output
     * can be returned with 'BitSet getOutput()' method. If the output is
     * too long for a BitSet 'BitSet getOutput()' method can be called before it
     * is full, (aprox. 245 MB), you can keep then the partial output and output
     * property becomes empty to continue the encoding process.
     * @param b boolean, it represents the bit to be encoded
     * @param p int, the probability to encode the bit with
     * @throws RuntimeException 
     */
    public void encodeBitProb(boolean b, int p) throws RuntimeException {
    	if (p>probTree.maxProb()||p<0) throw new RuntimeException("p is out of range");
        
       if (p<probTree.midProb()) {
           p+=((probTree.midProb()-p)<<1);
           b^=true;
       }
       
       int bin=probTree.getBin(p);
       if (bin==-1 && p==probTree.maxProb()) {
           if (b){
        	   throw new RuntimeException("Inconsistent probabilistic model");
           }
           else return;  
       }
               
       if (isNodeNull(bin)) {
           if (cq.fullQueue()) {
                flushBits();
                emptyHeadRun();
           }
           setNodeByBin(cq.shiftInQueueElement(),bin);
           cq.setLast(coders[bin].coder[0],bin);
       }
       nextTreeEntry(getNodeByBin(bin),b);
       emptyHeadRun();
   }
   
   /**
    * When all input has been red this method must been called to empty all
    * the partial or full codewords in the encoder circular queue. When this
    * is done all codewords from head to tail of the queue will be flush out,
    * if they are partial codewors the flush bit '0' will be added to them
    * until they become full output codewords.
    */
   public void terminate() {
       while (!cq.emptyQueue()) {
           flushBits();
           emptyHeadRun();
       }
   } 
   
   /**
    * This method flush out every complete output codeword from the head of
    * the circular queue until it finds a not complete one.
    */
   private void emptyHeadRun() {
       TruncatedBinary tb;
       while ((tb=cq.getFirstIfLeaf())!=null) {
           writeOut(tb);
           cq.shiftOutQueueElement();
       } 
   }
   
   /**
    * This method writes the content of a complete output codeword to the
    * output BitSet if there is space enough, if not it throws an exception.
    * @param num TruncatedBinary, the complete output codeword
    * @throws RuntimeException, thrown if output becomes full
    */
   private void writeOut(TruncatedBinary num) throws RuntimeException {
       if (usedOutput+num.getlength()>Integer.MAX_VALUE)
        throw new RuntimeException("Property usedOutput overflow");
       num.addToBitSet(output,usedOutput);
       usedOutput+=num.getlength(); 
   }
   
   /**
    * This method moves from a code binary tree TreeNode to another given a bit
    * represented by 'b', so the son '0' or '1' of 'n.entry' is selected and
    * returned a Node, this is a class with a TreeNode, (son), and a code number
    * indicating the bin and thus the associated component code where the
    * TreeNode is placed.
    * A TreeNode can be another TreeNode with two other sons or a leaf, this is
    * a TruncatedBinary with a complete output codeword.
    * @param n Node, the Node with the TreeNode instance of the father inside
    * @param b boolean, the bit to select the son from the father
    * @return Node, the son Node instance, complete output codeword or not
    */
   private Node nextTreeEntry(Node n,boolean b) {
       n.entry=n.entry.getSon(b?1:0);
       if (cq.isLeaf(n)) clearNodeByBin(n.code);
       return n;
   }
   
   /** 
    * This method adds some bits '0' known as flush bits until the codeword at
    * the head of the circular queue becomes a complete output codeword.
    */
   private void flushBits() {
       boolean flushBit;
       Node n=cq.getFirstNode();
       while (!cq.isLeaf(n)) {
           flushBit=coders[n.code].getFlushBit(n.entry);    
           n=nextTreeEntry(n,flushBit);
           n=cq.getFirstNode();
       }
   }
   
   /**
    * This method change the circular queue size of the encoder.
    * This method must not be used with an started encoding process done bit by
    * bit with 'readBit' method or a wrong encoding result will be obtained.
    * @param size int, the new size for the encoder circular queue
    */
   @Override
   public void setCircBufferSize(int size) {
       super.setCircBufferSize(size);
       cq=new CircularQueue(size);
   }
   
   /**
    * This method encodes the input BitSet property for a length equal to the
    * length of probabilities property array, if probabilities array is null or
    * empty it throws an exception, if input BitSet is empty it throws as well.
    * @return BitSet, the output BitSet property after the encoding of input
    */
   public BitSet encodeInput() {//Aquí obtendrías mayor eficiencia con un iterador ??
       if (input==null) throw new RuntimeException("The input BitSet can not be null");
       if (Integer.MAX_VALUE<getProbabilitiesLength()) throw new RuntimeException("The input BitSet can not contain probabilities length bits.");
       if (getProbabilitiesLength()==0) throw new RuntimeException("This method can not be used without probabilities.");
       boolean b; int p;
       for (int i=0;i<getProbabilitiesLength();i++) {
           b=input.get(i); p=probabilities[i];
           try { encodeBitProb(b,p); }
           catch (RuntimeException e) {
             if (e.getMessage()==null) throw e;
             switch (e.getMessage()) {
               case "p out of range":
                 throw new RuntimeException("Invalid value suplied by Probabilistic"
                 +" model at position "+i+". Value has to be beetwen 0 and "+probTree.maxProb(),e);
               case "Inconsistent probabilistic model":
                 throw new RuntimeException("Data and Probabilistic model are inconsi"
                 +"stent at position "+i+". The probability of 0 is "+p/probTree.maxProb()
                 +" and there is a "+((b==false)?0:1)+" at that position.",e);
               default: throw e;
             }
           }  
       }         
       terminate();
       return output;
   }
      
   /**
    * This method shows a graphic of redundancy given probability for a
    * particular component code of this encoder associated to a bin, if the
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
       double[][] data=getRedundancyData(bin,pNorm);
       String dataLabel;
       if (pNorm) dataLabel="Probability of zero";
       else dataLabel="Probability of zero over "+probTree.maxProb();
       //renderGraphic(data,true,1,"IEC Redundancy at bin "+bin+"\n"+n,dataLabel,"Redundancy (bits/source bit)","Redundancy as a function of probability of zero");      
   }
   
   /**
    * This method shows a graphic of redundancy given probability for this
    * encoder.
    * @param pNorm boolean, if true probabilities will be shown form the range
    *                        [0.5,1.0], if false from the range
    *                        [midprob,maxprob], (integers from bins cutOffs)
    */
   public void showRedundancyGraphic(boolean pNorm) {
       double[][] data=getRedundancyData(pNorm);
       String dataLabel;
       if (pNorm) dataLabel="Probability of zero";
       else dataLabel="Probability of zero over "+probTree.maxProb();
       //renderGraphic(data,true,1,"IEC Redundancy",dataLabel,"Redundancy (bits/source bit)","Redundancy as a function of probability of zero");
   }
   
   /**
    * This method calculate a near optimal encoder using only several GolombCode
    * codes and the copy code, the number of final component codes will
    * be the desiredCoders parameter. At last a graphic with two axis will be
    * shown the horizontal one will show the number of components coders of each
    * posible encoder, the vertical the mean redundancy for each encoder.
    * @param desiredCoders int, the number of final component codes, it must be
    *                      at least 2 or an exception will be thrown
    * @return InterleavedEntropyEncoder, the calculated encoder
    */
   public static InterleavedEntropyEncoder getInstanceByNumCoders(int desiredCoders) {
       ComponentCode[] partialCodes=new ComponentCode[1];
       partialCodes[0]=new ComponentCode();
       return getInstanceByRedundancy(desiredCoders,partialCodes);
   }
   
   /**
    * This method calculate a near optimal encoder adding only several
    * GolombCode codes to the 8 first component codes used by the Nasa ICER
    * Interleaved entropy coder, the number of final component codes will
    * be the desiredCoders parameter. At last a graphic with two axis will be
    * shown the horizontal one will show the number of components coders of each
    * posible encoder, the vertical the mean redundancy for each encoder.
    * @param desiredCoders int, the number of final component codes, it can be
    *                       more or less than 8 but it must be at least 2 or an
    *                       exception will be thrown
    * @return InterleavedEntropyEncoder, the calculated encoder
    */
   public static InterleavedEntropyEncoder getInstanceByNumCodersFromIcer(int desiredCoders) {
       
       ComponentCode[] partialCodes=new ComponentCode[8];
       partialCodes[0]=new ComponentCode();
       for (int i=1;i<=7;i++) {
           partialCodes[i]=ShortHandCode.getShortHandCode(i);
       }
       return getInstanceByRedundancy(desiredCoders,partialCodes);
   }
   
   /**
    * This method calculate a near optimal encoder adding only several
    * GolombCode codes to partialCoders array codes, a parameter with some
    * initial component codes, the number of final component codes will
    * be the desiredCoders parameter. At last a graphic with two axis will be
    * shown the horizontal one will show the number of components coders of each
    * posible encoder, the vertical the mean redundancy for each encoder.
    * @param desiredCoders int, the number of final component codes, it can be
    *                      more or less than the initial number of component
    *                      codes but it must be at least 2 or an exception
    *                      will be thrown
    * @param partialCoders ComponentCode[], a parameter with some initial
    *                      component codes
    * @return InterleavedEntropyEncoder, the calculated encoder
    */
   public static InterleavedEntropyEncoder getInstanceByNumCoderFromPartialCodes(int desiredCoders,ComponentCode[] partialCoders) {
       return getInstanceByRedundancy(desiredCoders,partialCoders);
   }
      
   /**
    * {@inheritDoc}
    */
   @Override
   public void setInput(BitSet input,int[] probabilities) {
       super.setInput(input,probabilities);
       terminate();
   }
   
   /**
    * This method calculate a near optimal encoder adding only several
    * GolombCode codes to partialCoders array codes, a parameter with some
    * initial component codes, the number of final component codes will
    * be the desiredCoders parameter. At last a graphic with two axis will be
    * shown the horizontal one will show the number of components coders of each
    * posible encoder, the vertical the mean redundancy for each encoder.
    * @param desiredCoders int, the number of final component codes, it can be
    *                      more or less than the initial number of component
    *                      codes but it must be at least 2 or an exception
    *                      will be thrown
    * @param partialCodes ComponentCode[], a parameter with some initial
    *                     component codes
    * @return InterleavedEntropyEncoder, the calculated encoder
    */
   private static InterleavedEntropyEncoder getInstanceByRedundancy(int desiredCoders,ComponentCode[] partialCodes) {
       
       if (desiredCoders<2) throw new RuntimeException("Parameter desiredCoders must be at least 2");
       
       class CoderData {
           int[] customBinsCutoffs=new int[1024];
           ComponentCode[] iecCcs=new ComponentCode[1024];
           
           class RetData {
               int[] destOffSets;
               ComponentCode[] destCoders;
               
               RetData (ComponentCode[] cod, int[] off) {
                   destCoders=cod;
                   destOffSets=off;
               }
           }
           
           RetData deletePosAndUpdateArrays(int out,ComponentCode[] codersOrig, int[] offsetsOrig) {
              
              int[] offsetsDest = new int[offsetsOrig.length-1];
              ComponentCode[] codersDest = new ComponentCode[codersOrig.length-1];

              for (int i=0;i<out;i++) {//Ponía manual array copy
                  codersDest[i]=codersOrig[i];
                  offsetsDest[i]=offsetsOrig[i];

              }
              for (int i=out;i<codersDest.length;i++) {
                  codersDest[i]=codersOrig[i+1];
                  offsetsDest[i]=offsetsOrig[i+1];
              }

              if (out==offsetsDest.length) offsetsDest[out-1]=ProbTree.getDefaultMAXPROB();
              else if (out!=0) offsetsDest[out-1]=joinCoderPoint(offsetsDest[out-1],codersDest[out-1],codersDest[out]);
              
              return new RetData(codersDest,offsetsDest);
           }
           
           Double getDifRedundancy(int out) {

            int binOrig,bin;
            Double redOrig=0.0;
            Double red=0.0;
            ProbTree ptOrig=ProbTree.getInstance(customBinsCutoffs);
            
 
            RetData rd=deletePosAndUpdateArrays(out,iecCcs,customBinsCutoffs);
            ComponentCode[] cods=rd.destCoders;
            int[] cutOffs=rd.destOffSets;
            
            ProbTree pt=ProbTree.getInstance(cutOffs);
            int limit1=(out!=0)?customBinsCutoffs[out-1]:ProbTree.getDefaultMIDPROB();

            for (int i=limit1;i<customBinsCutoffs[out];i++) {
                bin=pt.getBin(i);
                binOrig=ptOrig.getBin(i);
                red+=cods[bin].getRedundancy(new Double(i)/pt.maxProb());
                redOrig+=iecCcs[binOrig].getRedundancy(new Double(i)/pt.maxProb());
            }
            return red-redOrig;
           }
           
           public Double eliminateWorseCoder() {
       
            int worse=-1;
            Double temp,bestRedundancy=Double.MAX_VALUE;

            for (int i=0;i<iecCcs.length;i++) {
                if ((temp=getDifRedundancy(i))<bestRedundancy) {
                    bestRedundancy=temp;
                    worse=i;
                }
            }
          
            RetData rd=deletePosAndUpdateArrays(worse,iecCcs,customBinsCutoffs);
            iecCcs=rd.destCoders;
            customBinsCutoffs=rd.destOffSets;
            return bestRedundancy;
        }
           
        private int fullJoinCoderPoint(int prob,ComponentCode c1,int m2) {

            while (prob<ProbTree.getDefaultMAXPROB()) {
                if (c1.getRedundancy(new Double(prob)/ProbTree.getDefaultMAXPROB())
                <GolombCode.getRedundancy(new Double(prob)/ProbTree.getDefaultMAXPROB(),m2) &&
                c1.getRedundancy(new Double(prob+1)/ProbTree.getDefaultMAXPROB())
                >GolombCode.getRedundancy(new Double(prob+1)/ProbTree.getDefaultMAXPROB(),m2))
                    break;
                    prob++; 
            }        
            return prob;
        }

        private int joinCoderPoint(int prob,ComponentCode c1,ComponentCode c2) {

            while (prob<ProbTree.getDefaultMAXPROB() &&
                c1.getRedundancy(new Double(prob)/ProbTree.getDefaultMAXPROB())
                <c2.getRedundancy(new Double(prob)/ProbTree.getDefaultMAXPROB())) {         
                    prob++; 
            }    
            return prob;
        }

        private int joinCoderPoint(int prob,ComponentCode c1,int m2) {

            while (prob<ProbTree.getDefaultMAXPROB() &&
                c1.getRedundancy(new Double(prob)/ProbTree.getDefaultMAXPROB())
                <GolombCode.getRedundancy(new Double(prob)/ProbTree.getDefaultMAXPROB(),m2)) {         
                    prob++; 
            }        
            return prob;
        }
       }
       
       if (partialCodes==null)
           throw new RuntimeException("Parameter partialCodes can not be null.");
       
       int m2=2;
       InterleavedEntropyEncoder iec=new InterleavedEntropyEncoder();
       int bins=partialCodes.length;
       CoderData cd=new CoderData();
       
       System.arraycopy(partialCodes,0,cd.iecCcs,0,bins);
       
       int prob;
       if (partialCodes.length>1) {
           cd.customBinsCutoffs[0]=cd.joinCoderPoint(ProbTree.getDefaultMIDPROB(),
            partialCodes[0],partialCodes[1]);
           for (int i=1;i<bins-1;i++) {
               cd.customBinsCutoffs[i]=cd.joinCoderPoint(cd.customBinsCutoffs[i-1],
                cd.iecCcs[i],cd.iecCcs[i+1]);    
           }
           while ((prob=cd.fullJoinCoderPoint(cd.customBinsCutoffs[bins-2],cd.iecCcs[bins-1],m2))
                   ==ProbTree.getDefaultMAXPROB()) {
               if (m2==512) break; m2++; }
       } else {
           while ((prob=cd.fullJoinCoderPoint(ProbTree.getDefaultMIDPROB(),partialCodes[0],m2))
                   ==ProbTree.getDefaultMAXPROB()) {
               if (m2==512) break; m2++; }   
       }
       ComponentCode act=new GolombCode(m2);
       cd.customBinsCutoffs[bins-1]=prob;
       
       while ((prob=cd.joinCoderPoint(prob,act,m2+1))
          <ProbTree.getDefaultMAXPROB()&&m2<512) {
           
           if (prob==cd.customBinsCutoffs[bins-1]) {
               m2++;
               act=new GolombCode(m2);
               continue;
           } 
   
           if (bins>1023) throw new RuntimeException("It is impossible to achie"
            +"ve the desired maximum redundancy with 1024 component coders.");
           
           cd.customBinsCutoffs[bins]=prob;
           cd.iecCcs[bins]=act;
           bins++;
           m2++;
           act=new GolombCode(m2);
       }
       
       if (prob<ProbTree.getDefaultMAXPROB()) {
           cd.iecCcs[bins]=act;
           cd.customBinsCutoffs[bins]=ProbTree.getDefaultMAXPROB();
           bins++;
       }
       cd.customBinsCutoffs=Arrays.copyOf(cd.customBinsCutoffs,bins);
       cd.iecCcs=Arrays.copyOf(cd.iecCcs,bins);
       
       int limit1,limit2;
       ProbTree pt=ProbTree.getInstance(cd.customBinsCutoffs);
       limit2=pt.maxProb();
       limit1=pt.midProb();
       Double totalRed=0.0;
       int bin;
       for (int i=limit1;i<=limit2;i++) {
           bin=pt.getBin(i);
           if (bin==-1) bin=pt.getBin(i-1);
            totalRed+=cd.iecCcs[bin].getRedundancy(new Double(i)/pt.maxProb());
       }

       int[] outCutOffs=Arrays.copyOf(cd.customBinsCutoffs,cd.customBinsCutoffs.length);
       ComponentCode[] outCoders=Arrays.copyOf(cd.iecCcs,cd.iecCcs.length);
           
       int i2=21;
       int num=(limit2-limit1)+1;
       Double finalRedundancy=0.0;
       double[][] data=new double[2][num];
       while (cd.iecCcs.length>2) {  
           System.out.println("Automatic Configuration current components #: "
                              +cd.iecCcs.length+" mean Redundancy: "
                              +totalRed/num);
           totalRed+=cd.eliminateWorseCoder();
           if (cd.iecCcs.length==desiredCoders) {
               outCutOffs=Arrays.copyOf(cd.customBinsCutoffs,cd.customBinsCutoffs.length);
               outCoders=Arrays.copyOf(cd.iecCcs,cd.iecCcs.length);
               finalRedundancy=totalRed/num;
           }  
           if (i2>0 && cd.iecCcs.length<=i2) {
               i2--;
               data[0][i2]=cd.iecCcs.length;
               data[1][i2]=totalRed/num;
           }
       }
           
       System.out.println("Automatic configuration of components done with "
                          +outCoders.length+" components coders. Final "
                          +"Redundancy: "+finalRedundancy);
       //renderGraphic(data,false,4,"IEC Redundancy given amount of codes","Probability of zero","Redundancy (bits/source bit)","Redundancy as a function of probability of zero");
       iec.setBinsProbsAndCoders(outCutOffs,outCoders);
       return iec;
   }
   
   /**
    * This method initializes de internal component codes following the Nasa
    * ICER Interleaved entropy coder specification but respecting the limit of
    * number of components, truncating if the current number of containers is
    * not enough.
    */
   private void codersInit() {
          
       ComponentCode[] cod= new ComponentCode[ProbTree.getDefaultBINNUM()];
       cod[0] = new ComponentCode();
       for (int i=1;i<8;i++) cod[i] = ShortHandCode.getShortHandCode(i);
       cod[8] = new GolombCode(5);
       cod[9] = new GolombCode(6);
       cod[10] = new GolombCode(7);
       cod[11] = new GolombCode(11); 
       cod[12] = new GolombCode(17);
       cod[13] = new GolombCode(31);
       cod[14] = new GolombCode(70);
       cod[15] = new GolombCode(200);
       cod[16] = new GolombCode(512);
       System.arraycopy(cod,0,coders,0,Math.min(ProbTree.getDefaultBINNUM(),probTree.getBINNUM()));
   }
}
