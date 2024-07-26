package GiciEntropyCoder.InterleavedEntropycoder;

import java.util.BitSet;

/**
 * @version 1.0
 * @author Juan Ramón Soler Cabrejas
 * 
 * The purpose of this class is to store a short value representing a short
 * amount (16 bits) of bits in a sequence, but having another short value,
 * length, to inform exactly what amount of bits are in use in value. Moreover
 * the important nextLenghtValue() method gives the class the ability of
 * get the next value needed while making a GolombCode from certain point
 * where it must increment length in addition to value in order to make
 * next entry of the GolombCode.
 */
class TruncatedBinary extends TreeNode implements Comparable {
   
    protected static final int USEDBITS = 9;
    protected static final int ALLOWEDLENGTH = 1<<USEDBITS;//512 (10 bits)
    protected static final int ALLOWEDVALUE = (1<<USEDBITS)-1;//511 (9 bits)
    
    private short value;
    private short length;
    
    /** 
     * Default constructor, giving parameters int value and int length,
     * a TruncatedBinary object is initialized using them if they are in the
     * allowed ranges, if they are not an Exception is launched to indicate it.
     * @param value Value to be stored, it must be positive and less than
     *        Short.MAX_VALUE and (2^Legth)-1
     * @param length Legth used to store the value, it must be less than
     *        Short.MAX_VALUE.
     */
    public TruncatedBinary(int value,int length) {

        if (value>ALLOWEDVALUE) throw new RuntimeException("int value parameter "+value+">"+ALLOWEDVALUE+" (allowed value)");
        if (length>ALLOWEDLENGTH) throw new RuntimeException("length value parameter "+length+">"+ALLOWEDLENGTH+" (allowed length)");
        if (value<0) throw new RuntimeException("The value property has become lower than 0");
        if (length<0) throw new RuntimeException("The length property has become lower than 0");
        if (value>(Math.pow(2,length)-1)) throw new RuntimeException("The value property is too hight for this length");
 
        this.value = (short) value;
        this.length = (short) length;
    }
    
    /**
     * Test if this TruncatedBinary is equal to Object o, the first condition
     * is o is an instance of TruncatedBinary, the second is o and this
     * Truncated Binary have the same value and length properties.
     * @param o to be tested like TruncatedBinary.
     * @return boolean, true if both contions are true, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof TruncatedBinary) {
            TruncatedBinary e=(TruncatedBinary)o;
            if (length==e.length && value==e.value) return true;
        }
        return false;
    }

    /**
     * It just calculate a hashCode for this TruncatedBinary object
     * and return an int.
     * @return int, the hash code of the TruncatedBinary object.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.value;
        hash = 37 * hash + this.length;
        return hash;
    }
    
    /**
     * This is the copy constructor to create a new TruncatedBinary from the
     * one parameter c indicates, it copies value and length properties.
     * @param c to be copied.
     */
    public TruncatedBinary(TruncatedBinary c) {
        
        this.value = c.value;
        this.length = c.length;
    }
    
    /**
     * This method is used to validate value and length properties remains in
     * the allowed ranges after the operations carried by some others methods
     * are over, an exception is launched if value or length properties goes
     * out of the allowed ranges for TruncatedBinary objects.
     * The allowed range for value and length are positive shorts,
     * and value keeps a number with a binary representation no longer
     * than length.
     */
    private void validateData() {
        
        if (value>ALLOWEDVALUE) throw new RuntimeException("The value property "+value+">"+ALLOWEDVALUE+" (allowed value)");
        if (length>ALLOWEDLENGTH) throw new RuntimeException("The length property "+length+">"+ALLOWEDLENGTH+" (allowed length)");
        if (value<0) throw new RuntimeException("The value property has become lower than 0");
        if (length<0) throw new RuntimeException("The length property has become lower than 0");
        if (value>(Math.pow(2,length)-1)) throw new RuntimeException("The value property is too hight for this length");
    }
    
    /**
     * This method just returns the length property like a short.
     * @return short, length property like a short.
     */
    public short getlength() {
        return length;
    }
    
    /**
     * This method just returns the value property like a short.
     * @return short, value property like a short
     */
    public short getValue() {
        return value;
    }
    
    /**
     * This important method is used to extend by 1 the length of a GolombCode
     * entry when this code reaches the point of increment beyond (2^c)-m codes
     * has been generated. At the former point value is incremented and shifted
     * left by 1 position also. An exception is launched if value or length
     * properties goes out of the allowed ranges for TruncatedBinary objects
     * taking into account value and length are positive shorts and value keeps
     * a number with a binary representation no longer than length.
     */
    public void nextLenghtValue() {
        if (getHighBit()==true) throw new RuntimeException("The value is too "
                + "high to promote the length in Golomb context.");
        length++;
        value++;
        value=(short)(value<<1);
        validateData();
    }
    
    /**
     * This method shift left the value property 1 position and increments
     * length by 1 also. The digit shifted in is 0 o 1 depending of char c
     * parameter. Parameter c can be the character '0' or '1', others will
     * cause an Exception indicating an abnormal use of this method.
     * An exception is launched if value or length properties goes out
     * of the allowed ranges for TruncatedBinary objects taking into account
     * value and length are positive shorts and value keeps a number with
     * a binary representation no longer than length.
     * @param c the char must be '0' or '1'.
     */
    public void shiftC(char c) {
        if (c!='0' && c!='1') throw new RuntimeException("Character c must be 0 or 1.");
        
        int b=Character.digit(c,2);
        length++;
        value=(short)(value<<1);
        value+=b;
        validateData();
    }
    
    /**
     * This method shift left the value property n positions, each position
     * shifted increments length by 1 also, the digit shifted in is not
     * allways 0, instead is the digit already there at the right position
     * from the begining.
     * The n positions to be shifted only can be a number from 2 to 9.
     * An exception is launched if value or length properties goes out of the
     * allowed ranges for TruncatedBinary objects taking into account value and
     * length are positive shorts and value keeps a number with a binary
     * representation no longer than length.
     * @param n positions to shift left the right bit, it must be 2-9.
     */
    public void shiftRightBit(int n) {
        if (n>9 || n<2) throw new RuntimeException("Number of shift positions n must be 2-9.");
        int rightBit = value & 0x01;
        length+=n;
        value=(short)(value<<n);
        value+=(rightBit<<n)-rightBit;
        validateData();
    }
    
    /**
     * This method just increments the value property of this TruncatedBinary.
     * An Exception is launched if value goes beyond the maximum allowed value
     * taking into account value and length are positive shorts and value keeps
     * a number with a binary representation no longer than length.
     */
    public void incValue() {
        value++;
        validateData();
    }
      
    /**
     * This method adds the length property number of bits contained in
     * the value property to a BitSet bs starting at the position pos of bs.
     * @param bs BitSet where the TruncatedBinary will be copied in.
     * @param pos position of the BitSet to start writing in.
     */
    public void addToBitSet(BitSet bs,int pos) {
        if (bs==null) throw new RuntimeException("BitSet bs can not be null.");
        if (pos<0) throw new RuntimeException("BitSet position to write can not be negative.");
        if (pos+this.length>Integer.MAX_VALUE)
         throw new RuntimeException("Overflow writing to BitSet bs from position pos.");
        int val=this.value;
        int len=this.length;
        while (len>0) {
            len--;
            bs.set(pos+len,((val&0x01)==0)?false:true);
            val>>=1;
        }
    }
    
    /**
     * This method gets a String of 0 and 1 of length equal to this
     * BinaryString object legth property and representing de value
     * property of this BinaryString object.
     * @return String, with the binary representation of this TruncatedBinary.
     */
    public String getBinaryString() {
        char[] output=new char[length];
        boolean b; int pos=0;
        TruncatedBinary out=new TruncatedBinary(this);
        while (out.length>0) {
            b=out.removeHighBit();
            output[pos]=b?'1':'0';
            pos++;
        }
        return new String(output);
    }
    
    /**
     * This method gets and remove the High Bit of the TruncatedBinary object.
     * An exception is launched if value or length properties goes out of the
     * allowed ranges for TruncatedBinary objects taking into account value
     * and length are positive shorts and value keeps a number with a binary
     * representation no longer than length.
     * @return boolean, true if high bit is 1 and false if it is 0.
     */
    public boolean removeHighBit() {
        long mask;
        long leng=length;
        if (leng>31) mask=0;
        else mask=(1<<(leng-1));
        boolean ret=((value&mask)==0)?false:true;
        value&=~mask;
        length--;
        validateData();    
        return ret;    
    }
    
    /**
     * This method gets the High Bit of the TruncatedBinary object.
     * An exception is launched if value or length properties goes out of the
     * allowed ranges for TruncatedBinary objects taking into account value
     * and length are positive shorts and value keeps a number with a binary
     * representation no longer than length.
     * @return boolean, true if high bit is 1 and false if it is 0.
     */   
    public boolean getHighBit() {
        long length2=this.length;
        int value2=this.value;
        long mask;
        if (length2>31) mask=0;
        else mask=(1<<(length2-1));  
        boolean ret=((value2&mask)==0)?false:true;
        validateData();
        return ret;    
    } 
    
    /**
     * This method returns true if the TruncatedBinary object owner
     * of the method is a prefix of the parameter tb.
     * @return boolean, true if this TruncatedBinary is a prefix
     *         of tb, and false otherwise.
     */
    public boolean isPrefix(TruncatedBinary tb) {
        TruncatedBinary temp=new TruncatedBinary(tb);
        while (temp.length>length) temp.removeLowBit();
        if (temp.length==length && temp.value==value) return true;
        return false;
    }
    
    /**
     * This method gets and remove the Low Bit of the TruncatedBinary object.
     * An exception is launched if value or length properties goes out of the
     * allowed ranges for TruncatedBinary objects taking into account value
     * and length are positive shorts and value keeps a number with a binary
     * representation no longer than length.
     * @return boolean, true if low bit is 1 and false if it is 0.
     */
    public boolean removeLowBit() {
        boolean b=getLowBit();
        value>>=1;
        length--;
        validateData();
        return b;
    }
    
    /**
     * This method gets the Low Bit of the TruncatedBinary object
     * without changing it.
     * @return boolean, true if low bit is 1 and false if it is 0.
     */
    public boolean getLowBit() {
        boolean b=((value&0x01)==0)?false:true;
        return b;
    }
    
    /**
     * This method counts the number of 1 a TruncatedBinary has and
     * returns it without changing the TruncatedBinary object.
     * @return int, the number of ones in the truncated binary.
     */
    public int numberOfOnes() {
        int ones=0;
        int val=this.value;
        int len=this.length;
        while (len>0) {
            len--;
            ones+=val&0x01;
            val>>=1;
        }
        return ones;
    }
    
    /**
     * This is the method to compare two TruncatedBinary objects,
     * It compares firsts by value, from low to high values, and
     * second by length, from short to long. An special case arise if
     * two values 0 and 1 are compared, in this case the comparison
     * starts taking into acount length and later value. As always
     * in methods compareTo -1,0,1 are returned to indicate this Object
     * is less, equal or higher than parameter Object o. A RuntimeException
     * is generated if Object o is not instance of Class TruncatedBinary.
     * @param o Object instance of TruncatedBinary to be compared with this one.
     * @return int, with values (-1,0,1), meaning:
     *               -1: this TruncatedBinary is lower than TruncatedBinary o.
     *                1: this TruncatedBinary is higher than TruncatedBinary o.
     *                0: this TruncatedBinary and TruncatedBinary o are equal.
     */
    @Override
    public int compareTo(Object o) {
        if (!(o instanceof TruncatedBinary))
         throw new RuntimeException("Object o must be instance of TruncatedBinary.");
        TruncatedBinary tb=(TruncatedBinary)o;
        if ((tb.value==0 && value==1) ||
                (tb.value==1 && value==0)) {
            if     (length<tb.length) return -1;
            else if (length>tb.length) return 1;
            else if (value<tb.value)  return -1;
            else if (value>tb.value)  return  1;
            else return 0;
        }
        if      (value<tb.value)      return -1;
        else if (value>tb.value)      return  1;
        else if (length<tb.length)    return -1;
        else if (length>tb.length)    return  1;
        else return 0;        
    }
}
