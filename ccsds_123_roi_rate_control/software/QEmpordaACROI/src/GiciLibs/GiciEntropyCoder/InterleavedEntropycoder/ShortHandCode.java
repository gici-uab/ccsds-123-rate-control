package GiciEntropyCoder.InterleavedEntropycoder;

import java.util.Arrays;

/**
 * @version 1.0
 * @author Juan Ramón Soler Cabrejas
 * 
 * The purpose of this class is the building of component codes from an
 * string in Short Hand notation, it has other functions like the processing
 * of inverse codes, the calculation of entropy of a tree or redundancy of
 * a code of this kind. It also has some checks and keeps all the Short Hand
 * Codes used in the NASA ICER Interleaved Entropy Coder and gives an easy way
 * to get the inverse of one of such codes.
 */
class ShortHandCode extends ComponentCode {
    
    TreeNode[] flushBits1;
    private String shortHandString;
    int id;
       
    /*
     * Private Constructor to prepare space to work with inside other
     * methods in this class. It really does not make the code tree.
     */
    private ShortHandCode(int m,int id) {
        super(m);
        this.id=id;
    }
    
    /**
     * The public constructor to instance the class from a string, it also has
     * an 'id' mainly used to identify the code and facilitate the identification
     * of the stored inverse in the case of the NASA ICER Interleaved entropy
     * coder Short Hand notation codes.
     * @param shortHandTree the string in short hand notation from where the
     *                      code has to be red.
     * @param id the 'id' of the code, the codes from ICER uses the rang 1 to 7
     *           and the inverses -1 to -7. The 0 is used by convention by
     *           every other coder, but it is not manadatory.
     */
    public ShortHandCode(String shortHandTree,int id) {
           
        super(shortHandTree);
        this.id=id;
        shortHandString=shortHandTree;
        TruncatedBinary tb=null;
        char c;
        int node=0;
        int nodeSon=0;
        int coma=0;
        boolean formerBinary=false;
        int[] parents = new int[m];
        char former='(';
        for (int idx=1;idx<shortHandTree.length();idx++) {
            c=shortHandTree.charAt(idx);
            if (c!='('&&c!=')'&&c!=','&&!Character.isDigit(c))
                throw new RuntimeException("The string contains a not allowed char for a Short Hand notation.");
            switch (c) {
                case '(':
                    nodeSon++;
                    coder[node].setSon(coder[nodeSon],coma);
                    parents[nodeSon]=node;
                    node = nodeSon;         
                    coma=0;
                    formerBinary=false;
                    break;
                case ')':
                    if (tb!=null) {
                        coder[node].setSon(tb,1);
                        tb=null;
                    } else node=parents[node];
                    coma=0;
                    formerBinary=false;
                    break;
                case ',':
                    if (tb!=null) {
                        coder[node].setSon(tb,0);
                        tb=null;     
                     } else node=parents[node];
                    coma=1;
                    formerBinary=false;
                    break;
                case '0':
                case '1':
                    if (!Character.isDigit(former)) tb = new TruncatedBinary(0,0);
                    tb.shiftC(c);
                    formerBinary=true;
                    break;
                default:
                    if (!Character.isDigit(c))
                        throw new RuntimeException("ShortHand tree notation is"
                           + "wrong at position "+idx+". A digit was expected");
                    if (formerBinary) tb.shiftRightBit(Character.digit(c,10)-1);
                    else throw new RuntimeException("ShortHand tree notation is"
                           + "wrong at position "+idx+". A binary digit is"
                           + " needed before a non binary.");
                    formerBinary=false;
            }
            switch (former) {
                case ',':
                case '(': if (c!='(' &&  !Character.isDigit(c))
                           throw new RuntimeException("ShortHand Tree notation "
                           +"is wrong at position "+idx+" ( or digit expected.");
                          break;
                case ')': if (c!=',' &&  c!=')')
                           throw new RuntimeException("ShortHand Tree notation"
                           +" is wrong at position "+idx+" , or ) expected.");
                          break;
                default : if (c!=',' &&  c!=')' &&  !Character.isDigit(c))
                           throw new RuntimeException("ShortHand Tree notation "
                           +"is wrong at position "+idx+" , or ) or digit expected.");
                          break;
            }
            former=c;
        }
         
        fillFlushBitsMap(coder[0]);
        if (testPrefix()==false)
            throw new RuntimeException("This is not a Prefix Code.");
    }
   
    /**
     * This method checks if the routes which a code uses form a prefix code,
     * looking for routes which are prefixes of some other routes.
     * @param routes The TruncatedBinary objects representing distict routes to
     *               distinct leafs in a tree.
     * @return boolean True if someone of the TruncatedBinary objects in routes
     *                 is prefix of some other, two identical objects are not
     *                 considered to be prefixes among them. False otherwise.
     */
    private boolean isPrefixCode(TruncatedBinary[] routes) {
        for (int i=0;i<routes.length;i++) {
            for (int j=0;j<routes.length;j++) {
                if (i!=j && routes[i].isPrefix(routes[j])) return false;
            }
        }
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    boolean getFlushBit(TreeNode act) {
        for (int i=0;i<flushBits1.length;i++) {
            if (flushBits1[i]==act) return true;
        }
        return false;
    }
    
    /**
     * This methods fills de property flushBits1, this is an array of TreeNode
     * references to the TreeNode part of this same coder tree, this references
     * informs about nodes which have a flush bit to be aplied different from
     * the 0, this is a 1, this is very uncommon.
     * @param act TreeNode, the TreeNode instance
     */
    private void fillFlushBitsMap(TreeNode act) {
        
        class RecursiveGetLeafs {
            
             int ones=0;
            
             int init(TreeNode act) {
                return recursive(act);
             }
             
             int recursive(TreeNode act) {
                int size[]={0,0};
                TruncatedBinary tb;
                for (int i=0;i<2;i++) {
                    TreeNode next=act.getSon(i);
                    if (next instanceof TruncatedBinary) {
                        tb=(TruncatedBinary)next;
                        size[i]=tb.getlength();
                    } else {
                        size[i]=recursive(next);
                    }
                }
                if (size[1]<size[0]) {
                    flushBits1[ones]=act;
                    ones++;
                    return size[1];
                }
                else return size[0];
             }
        }
        flushBits1=new TreeNode[m];
        RecursiveGetLeafs rec=new RecursiveGetLeafs();
        rec.init(act);
        flushBits1=Arrays.copyOf(flushBits1,rec.ones);
    }
    
    /**
     * It calculate the routes of this ShortHandCode object and checks if the
     * routes which a code uses form a prefix code, looking for routes which
     * are prefixes of some other routes.
     * @return boolean, True if someone of the routes is prefix of some other,
     *                  two identical objects are not considered to be prefixes
     *                  among them. False otherwise.
     */
    final protected boolean testPrefix() {
        ShortHandCode inverse=getInversInstance();
        TruncatedBinary[] leafs=getLeafs(inverse.coder[0]);
        return isPrefixCode(leafs);
    }
        
    /**
     * This method returns all routes to leafs or TruncatedBinary instances from
     * a TreeNode object inside a coder tree, normally the root of the tree.
     * @param act a TreeNode object of a coder tree where the search of leafs
     *            is started from.
     * @return TruncatedBinary[], the route to every leaf as an array of
     *                            TruncatedBinary objects.
     */
    private TruncatedBinary[] getLeafs(TreeNode act) {
        
        class RecursiveGetLeafs {
             int leafPos=0;
             TruncatedBinary[] leaf=new TruncatedBinary[m+1];
             
             TruncatedBinary[] init(TreeNode act) {
                return recursive(act);
             }
             
             TruncatedBinary[] recursive(TreeNode act) {
                for (int i=0;i<2;i++) {
                    TreeNode next=act.getSon(i);
                    if (next instanceof TruncatedBinary) {
                        leaf[leafPos]=new TruncatedBinary((TruncatedBinary)next);
                        leafPos++;
                    } else {
                        leaf=recursive(next);
                    }

                }
                return leaf;
             }
        }
        RecursiveGetLeafs rec=new RecursiveGetLeafs();
        return rec.init(act);
    }
    
    /**
     * This method returns the Short Hand notation string representing this
     * code tree.
     * @return String, the Short Hand string representing this code tree.
     */
    public String getShortHandString() {
        return shortHandString;
    }
    
    /**
     * This method returns the entropy of this code tree taking into account all
     * the routes this tree has and the probabilities each of them has.
     * @return Double, the entropy of this code tree. 
     */
    @Override
    public Double getEntropy() {

        Double temp;
        Double entrop=0.0;
        boolean tbNum=false;
        char c;
        char former='(';
        int deep=1;
        for (int idx=1;idx<shortHandString.length();idx++) {
            c=shortHandString.charAt(idx);
            if (c!='('&&c!=')'&&c!=','&&!Character.isDigit(c)) 
                throw new RuntimeException("The string contains a not allowed char for a Short Hand notation.");
            switch (c) {
                case '(':
                    deep++;
                    break;
                case ')':
                    if (tbNum) {
                        temp=(1.0/(1<<deep));
                        temp=temp*(Math.log(temp)/Math.log(2));
                        entrop+=temp;
                        tbNum=false;
                    }
                    deep--;
                    break;
                case ',':
                    if (tbNum) {
                        temp=(1.0/(1<<deep));
                        temp=temp*(Math.log(temp)/Math.log(2));
                        entrop+=temp;                 
                        tbNum=false;
                    }
                    break;
                case '0':
                case '1':
                    if (!Character.isDigit(former)) tbNum=true;
                    break;
            }
            former=c;
        }
        return -entrop;
    }
    
    /**
     * This static method gets the redundancy a code represented by a Short Hand
     * notation string would have encoding a bit with certain probability.
     * @param prob the probability of the bit, the normal use of the function
     *             demands prob to be in the range 0.5 - 1.0 .
     * @param shortHandString the Short Hand string representing the code.
     * @return Double, the redundancy given the parameters. 
     */
    protected static Double getRedundancy(Double prob,String shortHandString) {
        
        Double temp,p2=1-prob;
        Double lS=0.0;
        Double lE=0.0;
        int ones,ceros;
        TruncatedBinary tb=null;
        TruncatedBinary tbRoute=new TruncatedBinary(0,1);
        char former='(';
        char c;
        for (int idx=1;idx<shortHandString.length();idx++) {
            c=shortHandString.charAt(idx);
            if (c!='('&&c!=')'&&c!=','&&!Character.isDigit(c)) 
                throw new RuntimeException("The string contains a not allowed char for a Short Hand notation.");
            switch (c) {
                case '(':
                    tbRoute.shiftC('0');
                    break;
                case ')':
                case ',':
                    if (tb!=null) {
                        ones=tbRoute.numberOfOnes();
                        ceros=tbRoute.getlength()-ones;
                        temp=Math.pow(prob,ceros)*Math.pow(p2,ones);
                        lS+=temp*tb.getlength();
                        lE+=temp*tbRoute.getlength();
                        tb=null;
                    }
                    if (c==')') tbRoute.removeLowBit();
                    else tbRoute.incValue();
                    break;
                case '0':
                case '1':
                    if (!Character.isDigit(former)) tb = new TruncatedBinary(0,0);
                    tb.shiftC(c);
                    break;
                default: tb.shiftRightBit(Character.digit(c,10)-1);
            }
            former=c;
        }     
        return (lS/lE)-getEntropyFromProb(prob);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Double getRedundancy(Double prob) {
        return getRedundancy(prob,shortHandString);
    }
    
    /**
     * This static method give a Short Hand code tree given an identifier, but
     * only if the identifier is a number from 1 to 7 or -1 to -7, where the
     * negatives are inverses of the positives. This codes are the ones used by
     * default in the NASA ICER Interleaved entropy coder.
     * @param idx the identifier to get the code, it has to be in the ranges 
     *            from 1 to 7 or -1 to -7,others will throw a RuntimeException.
     * @return ShortHandCode, the ShortHandCode instance given the identifier.
     */
    public static ShortHandCode getShortHandCode(int idx) {//negative are decoders, positive coders, 0 copy coder
        if (idx<-7 || idx>7) throw new RuntimeException("Coder by index does not exist, please idx between -7 and 7.");
        switch (idx) {        
            case  1: return new ShortHandCode("(((((130,05),031),001),10),(01,(110,(14,041))))",idx);
            case  2: return new ShortHandCode("((((110,(14,00101)),03),10),(01,((130,00100),0011)))",idx);
            case  3: return new ShortHandCode("(((00,110),01),(10,13))",idx);
            case  4: return new ShortHandCode("((1,(03,0101)),((((0010,01100),013),0100),(0011,01101)))",idx);
            case  5: return new ShortHandCode("(((((00,130),100),101),(110,14)),01)",idx);
            case  6: return new ShortHandCode("(((0,100),(101,140)),((110,15),130))",idx);
            case  7: return new ShortHandCode("((((0,(130,140)),100),101),(110,15))",idx);
            case -1: return new ShortHandCode("(((((041,14),031),001),10),(01,(110,(05,130))))",idx);
            case -2: return new ShortHandCode("(((001,((1101,0311),13)),10),(01,(04,(1100,0310))))",idx);
            case -3: return new ShortHandCode("((03,01),(10,(001,11)))",idx);
            case -4: return new ShortHandCode("(((010,(104,110)),((101,011),((1031,13),1001))),00)",idx);
            case -5: return new ShortHandCode("((05,1),((031,001),(010,(041,011))))",idx);
            case -6: return new ShortHandCode("(03,((001,010),(100,(11,(011,101)))))",idx);
            case -7: return new ShortHandCode("(04,((001,01),(10,(0310,(0311,11)))))",idx);
            default: throw new RuntimeException("Coder by index are not cataloged, user defined ShortHandCode.");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ShortHandCode inverseCoder() {
        if (id>=-7 && id<=7 && id!=0) {
            return getShortHandCode(-id);
        } else return getInversInstance();    
    }
    
    /**
     * This method calculates and returns the inverse of this ShortHandCode
     * instance given it is not a NASA ICER Short Hand code cataloged one.
     * @return ShortHandCode, the inverse of this ShortHandCode instance. 
     */
    private ShortHandCode getInversInstance() {
        
        ShortHandCode ret=new ShortHandCode(this.m,-this.id);
        
        class TruncatedBinaryPair implements Comparable {
            TruncatedBinary tbRoute;
            TruncatedBinary tbLeaf;
            
            @Override
            public int compareTo(Object o) {
                TruncatedBinaryPair tbp=(TruncatedBinaryPair)o;
                return tbRoute.compareTo(tbp.tbRoute);
            }
        }
        
        TruncatedBinaryPair[] tbs = new TruncatedBinaryPair[m+1];
        
        int tbsPos=0;
        TruncatedBinary tb=null;
        TruncatedBinary tbRoute=new TruncatedBinary(0,1);
        char former='(';
        char c;
        for (int idx=1;idx<shortHandString.length();idx++) {
            c=shortHandString.charAt(idx);
            if (c!='('&&c!=')'&&c!=','&&!Character.isDigit(c))
                throw new RuntimeException("The string contains a not allowed char for a Short Hand notation.");
            switch (c) {
                case '(':
                    tbRoute.shiftC('0');
                    break;
                case ')':
                case ',':
                    if (tb!=null) {
                        tbs[tbsPos]=new TruncatedBinaryPair();
                        tbs[tbsPos].tbLeaf=new TruncatedBinary(tbRoute);
                        tbs[tbsPos].tbRoute=new TruncatedBinary(tb);
                        tbsPos++;
                        tb=null;
                    }
                    if (c==')') tbRoute.removeLowBit();
                    else tbRoute.incValue();
                    break;
                case '0':
                case '1':
                    if (!Character.isDigit(former)) tb = new TruncatedBinary(0,0);
                    tb.shiftC(c);
                    break;
                default: tb.shiftRightBit(Character.digit(c,10)-1);
            }
            former=c;
        }
        Arrays.sort(tbs); 
        TruncatedBinary[] routes=new TruncatedBinary[tbs.length];
        for (int i=0;i<tbs.length;i++) { routes[i]=tbs[i].tbRoute; } 
        if (isPrefixCode(routes)==false)
            throw new RuntimeException("The inverse is not a prefix code.");
  
        boolean b;
        int codersIdx=0;
        for (int i=0;i<tbs.length;i++) {     
            TreeNode next,act=ret.coder[0];
            while (tbs[i].tbRoute.getlength()>0) {
                b=tbs[i].tbRoute.removeHighBit();
                next=act.getSon(b?1:0);
                if (next==null) {
                    if (tbs[i].tbRoute.getlength()==0) {
                        act.setSon(tbs[i].tbLeaf,b?1:0);
                    } else  {
                        codersIdx++;
                        act.setSon(ret.coder[codersIdx],b?1:0);
                        act=ret.coder[codersIdx];   
                    }
                } else act=next;
            }         
        }
        ret.shortHandString=makeString(ret.coder[0]);
        return ret;
    }
    
    /**
     * This static method reconstruct and return a Short Hand notation string
     * from an already made ShortHandCode tree.
     * @param act a TreeNode object of a coder tree, normally the root of the
     *            own code tree, if anything diferent from the root is used
     *            a partial string from that point will be generated.
     * @return String, Short Hand notation string from the already made
     *                 ShortHandCode tree.
     */
    protected static String makeString(TreeNode act) {
        String temp;
        String out=new String();
        out=out+'(';
        for (int i=0;i<2;i++) {
            TreeNode next=act.getSon(i);
            if (next instanceof TruncatedBinary) {
                temp=((TruncatedBinary)next).getBinaryString(); 
            } else {
                temp=makeString(next);          
            }
            out=out+temp;
            if (i==0) out=out+',';
        }
        out=out+')';
        return out;
    }
}
