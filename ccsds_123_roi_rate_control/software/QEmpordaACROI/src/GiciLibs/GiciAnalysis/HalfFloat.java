package GiciAnalysis;

import java.io.Serializable;


/**
 * This class implements the IEEE 754 16-bit floating point format.
 * Do not expect exact compliance. Note: Most of the work is done by upclassing to Float.
 * 
 * Might be still quite buggy.
 * 
 * @author Ian
 */
public class HalfFloat implements Serializable, Comparable<HalfFloat> {

	/**
	 * The only non-static field of this class.
	 */
	private final short value;
	
	// Masks and stuff
	//private static short signMask = (short) 0x8000;
	private static short exponentMask = (short) 0x7c00;
	private static short significantMask = (short) 0x03FF;
	
	private static int signOffset = 15;
	private static int exponentOffset = 10;
	private static int significantOffset = 0;

	/**
	 * Serialization information.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Maximum exponent a finite HalfFloat variable may have.
	 */ 
	public static final int MAX_EXPONENT = 15;
	
	/**
	 * A constant holding the largest positive finite value of type HalfFloat.
	 */
	public static final HalfFloat MAX_VALUE = new HalfFloat(0x7bff);
	
	/**
	 * Minimum exponent a normalized float variable may have.
	 */
	public static final int MIN_EXPONENT = -15;
	
	/**
	 * A constant holding the smallest positive normal value of type HalfFloat, 2^-14.
	 */
	public static final HalfFloat MIN_NORMAL = new HalfFloat(0x0400);
	
	/**
	 * A constant holding the smallest positive nonzero value of type HalfFloat, 2^-24.
	 */
	public static final HalfFloat MIN_VALUE = new HalfFloat(0x0001);
	
	/**
	 * A constant holding a Not-a-Number (NaN) value of type HalfFloat.
	 */
	public static final HalfFloat NaN = new HalfFloat(0x7e00);
	
	/**
	 * A constant holding the negative infinity of type HalfFloat.
	 */ 
	public static final HalfFloat NEGATIVE_INFINITY = new HalfFloat(0xfc00);
	
	/**
	 * A constant holding the positive infinity of type HalfFloat.
	 */ 
	public static final HalfFloat POSITIVE_INFINITY = new HalfFloat(0x7c00);
	
	/**
	 * The number of bits used to represent a half precision float value.
	 */ 
	public static final int SIZE = 16;
	// The Class instance representing the primitive type float.
	//static Class<Float> TYPE

	protected HalfFloat(final int value) {
		
		assert ((value & 0xffff) == value);
		
		this.value = (short) value;
	}
	
	/**
	 * Constructs a newly allocated HalfFloat object that represents the argument converted to type float.
	 */
	public HalfFloat(double value) {
		if (Double.isNaN(value)) {
			this.value = NaN.value;
		} else if (Double.isInfinite(value) || Math.abs(value) > MAX_VALUE.doubleValue()) {
			if (value > 0) {
				this.value = POSITIVE_INFINITY.value;
			} else {
				this.value = NEGATIVE_INFINITY.value;
			}
		} else {


			final long v = Double.doubleToRawLongBits(value);
			
			long sign = (v >> 63) & 0x1;
			//sign = 1 - 2 * sign;
			long expo = (v >> 52) & 0x07FF;

			final int exponentOffsetShift = 52 /* fractional part */ + 1023 /* exponent */;
			
			expo -= exponentOffsetShift;

			long mant = v & 0xfffffffffffffL;
			
			if (expo != -exponentOffsetShift) {
				// Add the implicit one to the not-subnormalized numbers
				mant = (mant | (1L << 52));
			} else {
				expo += 1;
			}			
			
			if (expo > -37) {
				// This does not fit in a HalfFloat, so lets fill an Infinity				
				if (sign == 0) {
					this.value = POSITIVE_INFINITY.value;
				} else {
					this.value = NEGATIVE_INFINITY.value;
				}
			} else {				
				// Reduce mantissas' precision 
				mant = mant >> (53 - 11);
				expo += (53 - 11);
				
				long subnormalShift = (- 10 - 15) - expo + 1;  
				
				if (subnormalShift > 0) {
					// sub-normalize numbers
					mant = mant >> subnormalShift;
					
					expo = -25;
				} 
				
				expo += 10;
				
				long fsign = sign << signOffset;
				long fexpo = ( (expo + 15) & 0x1f ) << exponentOffset;
				long fmant = mant & significantMask;
				
				this.value = (short) (fsign | fexpo | fmant);
			}			
		}
		
	}
	
	/**
	 * Constructs a newly allocated HalfFloat object that represents the primitive float argument.
	 */ 
	public HalfFloat(float value) {
		this((double) value);
	}
	
	/**
	 * Constructs a newly allocated HalfFloat object that represents the floating-point value of type float represented by the string.
	 */
	public HalfFloat(String s) {
		// Could be better
		this((new Double(s)).doubleValue());
	}

	/**
	 * Returns the value of this Float as a byte (by casting to a byte).
	 */ 
	public byte byteValue() {
		return (new Float(this.floatValue())).byteValue();
	}

	// Compares the two specified float values.
	// static int compare(float f1, float f2) {}

	/**
	 * Compares two Float objects numerically.
	 */
	public int compareTo(HalfFloat anotherHalfFloat) {
		Float a = new Float(this.floatValue());
		Float b = new Float(anotherHalfFloat.floatValue());
		
		return a.compareTo(b);
	}
	
	/**
	 * Returns the double value of this Float object.
	 */
	public double doubleValue() {
		return floatValue();
	}
	
	/**
	 * Compares this object against the specified object.
	 */ 
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		try {
			return halfFloatToShortBits(this) == halfFloatToShortBits((HalfFloat) obj); 
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	/**
	 * Returns a representation of the specified floating-point value according to the IEEE 754 floating-point "single format" bit layout.
	 */
	public static short halfFloatToShortBits(final HalfFloat value) {
		return halfFloatToRawShortBits(value.isNaN()? NaN: value);
	}
	
	/**
	 * Returns a representation of the specified floating-point value according to the IEEE 754 floating-point "single format" bit layout, preserving Not-a-Number (NaN) values.
	 */
	public static short halfFloatToRawShortBits(final HalfFloat value) {
		return value.value;
	}
	
	/**
	 * Returns the float value of this HalfFloat object.
	 */ 
	public float floatValue() {
		int sign = (value >> signOffset) & 0x1;
		int exponent = (value & exponentMask) >> exponentOffset;
		int significant = (value & significantMask) >> significantOffset;

		sign = 1 - 2 * sign;
		
		if (isNaN()) {
			return Float.NaN;
		} else if (isInfinite()) {
			if (sign > 0)
				return Float.POSITIVE_INFINITY;
			else
				return Float.NEGATIVE_INFINITY;
		}
		
		if (exponent == 0 && significant == 0) {
			// We got a zero
			if (sign > 0)
				return 0f;
			else 
				return -0f;
		}
		
		if (exponent != 0) {
			// Add the implicit one to the not-subnormalized numbers
			significant = (significant | (1 << exponentOffset));
		} else {
			exponent += 1;
		}
		
		exponent += -15 - 10;

		
		// Correct for sign;
		significant *= sign;

		return (float)significant * (float)Math.pow(2.0, exponent);
	}
	
	/**
	 * Returns a hash code for this HalfFloat object.
	 */
	public int hashCode() {
		return halfFloatToShortBits(this);
	}
	
	// Returns the float value corresponding to a given bit representation.
	public static HalfFloat shortBitsToFloat(final short bits) {
		// remove sign extension
		int intbits = ((int) bits) & 0xffff; 
		return new HalfFloat(intbits);
	}
	
	// Returns the value of this Float as an int (by casting to type int).
	public int intValue() {
		return (new Float(this.floatValue())).intValue();
	}
	
	// Returns true if this Float value is infinitely large in magnitude, false otherwise.
	public boolean isInfinite() {
		return (value & POSITIVE_INFINITY.value) == POSITIVE_INFINITY.value && (value & significantMask) == 0;
	}
	
	// Returns true if the specified number is infinitely large in magnitude, false otherwise.
	//public static boolean isInfinite(HalfFloat v) {}
	
	/**
	 * Returns true if this Float value is a Not-a-Number (NaN), false otherwise.
	 */
	public boolean isNaN() {
		return (value & POSITIVE_INFINITY.value) == POSITIVE_INFINITY.value && (value & significantMask) != 0;
	}
	
	// Returns true if the specified number is a Not-a-Number (NaN) value, false otherwise.
	//static boolean isNaN(float v) {}

	public boolean isNormal() {
		return ((value & exponentMask) >> exponentOffset) != 0 || (value & significantMask) == 0;
	}
	
	// Returns value of this Float as a long (by casting to type long).
	public long longValue() {
		return (new Float(this.floatValue())).longValue();
	}
	
	// Returns a new float initialized to the value represented by the specified String, as performed by the valueOf method of class Float.
	//static float parseFloat(String s) {}
	
	// Returns the value of this Float as a short (by casting to a short).
	public short shortValue() {
		return (new Float(this.floatValue())).shortValue();
	}
	
	// Returns a hexadecimal string representation of the float argument.
	public static String toHexString(HalfFloat f) {
		return Float.toHexString(new Float(f.floatValue()));
	}
	
	// Returns a string representation of this Float object.
	public String toString() {
		return (new Float(this.floatValue())).toString();
	}
	
//	// Returns a string representation of the float argument.
//	public static String toString(HalfFloat f) {
//		return null;
//	}
//	
//	// Returns a Float instance representing the specified float value.
//	public static HalfFloat valueOf(HalfFloat f) {
//		return null;
//
//	}
//	
//	// Returns a Float object holding the float value represented by the argument string s.
//	public static HalfFloat valueOf(String s) {
//		return null;
//
//	}

}
