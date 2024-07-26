package GiciEntropyCoder.InterleavedEntropycoder;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;
import java.util.BitSet;
//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartFrame;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.chart.plot.XYPlot;
//import org.jfree.chart.renderer.xy.XYItemRenderer;
//import org.jfree.data.xy.DefaultXYDataset;

/**
 * @version 1.0
 * @author Juan Ramón Soler Cabrejas
 * 
 * This is possibly the main class of this Interleaved Entropy Coder and study
 * extensions project. Although is not directly visible out of the API, all of
 * its public methods can by used from the API interface because the two public
 * classes InterleavedEntropyEncoder and InterleavedEntropyDecoder extends this
 * class. Because this class extends BinNodeBuffer InterleavedEntropyEncoder and
 * InterleavedEntropyDecoder inherits indirectly BinNodeBuffer as well and they
 * has an internal buffer they use for diferent purposes, see that classes.
 * However the main purpose of this class is keeping all the common properties
 * and methods its descendants also need.
 */
class InterleavedEntropyCoder extends BinNodeBuffer {
    
   static int defaultCIRCBUFFERSIZE = 2048;
   int circBufferSize;
   BitSet input;
   int[] probabilities;
   BitSet output = new BitSet();
   int usedOutput = 0;
   ComponentCode[] coders;
   ProbTree probTree;
   
   /**
    * This constructor initializes at once the circular buffer given a parameter
    * and custom bins probability cutoffs given another, circular buffer size
    * allowed range is [2,2^14] and the probability cutoffs array can be null or
    * has a length in the range [2,256]. Additionally the input and the
    * associated probabilities for each bit can be loaded from the beginning,
    * neither input BitSet or probabilities array can be null, the length of
    * probabilities determines how many bits of input will be used.
    * @param input BitSet, the input to be encoded or decoded
    * @param probabilities int[], array of probabilities for each bit in the
    *                            original input. The length of probabilities
    *                            determines how many bits of input will be used.
    * @param circBufferSize int, circular buffer size in the range [2,2^14]
    * @param customBinsCutoffs int[], array of probability cutoffs for bins,
    *                           it can be null and default cutoffs will be used
    *                           or, if it is not null its length must be in the
    *                           range [2,256]                                       
    */
   InterleavedEntropyCoder(BitSet input,int[] probabilities,int circBufferSize,int[] customBinsCutoffs) {   
       this(circBufferSize,customBinsCutoffs);
       if (input==null) throw new RuntimeException("BitSet input can not be null");
       if (probabilities==null) throw new RuntimeException("The probabilities array can not be null");
       this.input=input;
       this.probabilities=probabilities;
       
   }
   
   /**
    * This constructor initializes at once the circular buffer given a parameter
    * and custom bins probability cutoffs given another, circular buffer size
    * allowed range is [2,2^14] and the probability cutoffs array can be null or
    * has a length in the range [2,256].
    * @param circBufferSize int, circular buffer size in the range [2,2^14]
    * @param customBinsCutoffs int[], array of probability cutoffs for bins,
    *                           it can be null and default cutoffs will be used
    *                           or, if it is not null its length must be in the
    *                           range [2,256]  
    */
   InterleavedEntropyCoder(int circBufferSize,int[] customBinsCutoffs) {
       super(customBinsCutoffs);
       if (customBinsCutoffs!=null && (customBinsCutoffs.length>(1<<8)||customBinsCutoffs.length<2))
           throw new RuntimeException("The allowed lenght range for customBinsCutoffs array is [2,256]");
       if (circBufferSize>(1<<14)||circBufferSize<1)
           throw new RuntimeException((1<<14)+" is the highter allowed value for circBufferSize and 1 the minimum");
       this.circBufferSize=circBufferSize;
       this.input=null;
       this.probabilities=null;
       probTree = ProbTree.getInstance(customBinsCutoffs);
       coders = new ComponentCode[probTree.getBINNUM()];
       for (int i=0;i<probTree.getBINNUM();i++) {
           coders[i]=new ComponentCode();
       }
   }
   
   /**
    * This method sets the input to be used by the encoder or the decoder, input
    * BitSet and probabilities array must be not null, length of probabilities
    * array informs about how many bits from input are to be encoded when
    * calling encodeInput at InterleavedEntropyEncoder class, at
    * InterleavedEntropyDecoder class length of probabilities array informs
    * about how many original input bits it must recover decoding its own input.
    * @param input BitSet, the input to be encoded or decoded
    * @param probabilities int[], array of probabilities for each bit in the
    *                            original input. The length of probabilities
    *                            determines how many bits of input will be used.
    */
   public void setInput(BitSet input,int[] probabilities) {
       if (input==null) throw new RuntimeException("BitSet input can not be null");
       this.input=input;
       this.probabilities=probabilities;
       clearOutput();
   }
   
   /**
    * This method returns the length of probability array or 0 if the array
    * references null.
    * @return int, length of probability array or 0 if the array references null
    */

   public long getProbabilitiesLength() {
        if (probabilities==null) return 0;
        return probabilities.length;
   }

   /**
    * This method returns an array of ComponentCode instances with all the 
    * component codes currently in use by this encoder or decoder.
    * @return ComponentCode[], codes currently in use by this encoder or decoder
    */
   public ComponentCode[] getCodes() {
       return Arrays.copyOf(coders,coders.length);
   }
   
   /**
    * This method returns an array of ComponentCode instances with the 
    * component codes currently in use by this encoder or decoder but truncated
    * its number to the parameter num, if num is higher than the length of the 
    * component codes currently in use an exception is thrown.
    * @param num int, the number of component codes to be returned
    * @return ComponentCode[], codes currently in use by this encoder or decoder
    *                          truncated by parameter num
    */
   public ComponentCode[] getTruncCodes(int num) {
       if (num>coders.length) throw new RuntimeException("This method can not be used to get more codes than there are.");
       ComponentCode[] cods=Arrays.copyOf(coders,num);
       return cods;
   }
   
   /**
    * This method returns an array of integers with the cutoffs currently in use
    * by this encoder or decoder but truncated its number to the parameter num,
    * if num is higher than the length of the current cutoffs in use an
    * exception is thrown.
    * The first stored cutoff equal to half the maximum probability is not
    * returned, so the returned array is ready to be used like cutoffs for a new
    * coder or decoder if the minimum value in the array is higher than half de
    * maximum, if not values have to be added before using it.
    * @param num int, the number of cutoffs probabilities to be returned
    * @return int[], cutoffs currently in use by this encoder or decoder
    *                truncated by parameter num
    */
   public int[] getTruncCutOffs(int num) {
       if (num>coders.length) throw new RuntimeException("This method can not be used to get more cutoffs than there are.");
       int[] cutoffs=Arrays.copyOfRange(probTree.getCutOffs(),1,num+1);
       return cutoffs;
   } 
   
   /**
    * This method change the circular queue size of the encoder and decoder.
    * This method must not be used with an started encoding process done bit by
    * bit with 'readBit' or 'readCodeWord' method or a wrong encoding result
    * will be obtained.
    * @param size int, the new size for the encoder circular queue
    */
   public void setCircBufferSize(int size) {
       if (size>(1<<14)||size<1)
           throw new RuntimeException((1<<14)+" is the highter allowed value for circBufferSize and 1 the minimum");
       this.circBufferSize=size;
   }
   
   /**
    * This method assigns a new copy code to the indicated position pos in the
    * component coders property array. A copy code is a transparent code which
    * copy to the output everything it gets from the input. A Short Hand
    * notation string representing a copy code would be (0,1).
    * @param pos int, the position
    */
   public void setCopyCode(int pos) {
       if (pos<0||pos>coders.length-1) throw new RuntimeException("There is not such position in coders array.");
       coders[pos]=new ComponentCode();
   }
   
   /**
    * This method assigns a ShortHandCode instance to the indicated position pos
    * in the component coders property array.
    * @param shortHandString String, Short Hand notation string describing the
    *                        ShortHandCode instance to assign to position pos.
    * @param pos int, the position
    */
   public void setShortHandCode(String shortHandString,int pos) {
       if (pos<0||pos>coders.length-1)
           throw new RuntimeException("There is not such position in coders array.");
       coders[pos]=new ShortHandCode(shortHandString,0);
   }
   
   /**
    * This method assigns a ShortHandCode inverse instance to the indicated
    * position pos in the component coders property array.
    * @param shortHandString String, Short Hand notation string describing the
    *                        inverse of the ShortHandCode instance to assign
    *                        to position pos.
    * @param pos int, the position
    */
   public void setInverseShortHandCode(String shortHandString,int pos) {
       if (pos<0||pos>coders.length-1)
           throw new RuntimeException("There is not such position in coders array.");
       coders[pos]=new ShortHandCode(shortHandString,0).inverseCoder();
   }
   
   /**
    * This method assigns a NASA ICER catalogued ShortHandCode instance to the
    * indicated position pos in the component coders property array, the allowed
    * ranges are [1,7] for the codes normally used in the ICER coder and
    * [-7,-1] for the codes used at ICER decoder, but you can play.
    * @param id int, the identification number in the catalog of the catalogued
    *                ShortHandCode. The allowed ranges are [1,7] for the codes
    *                used in the ICER coder and [-7,-1] for the codes used at
    *                ICER decoder, where, for example, '-1' is inverses of '1'
    * @param pos int, the position
    */
   public void setICERShortHandCode(int id,int pos) {
       coders[pos]=ShortHandCode.getShortHandCode(id);
   }
   
   /**
    * This method assigns a GolombCode instance with characteristic parameter m
    * to the indicated position pos in the component coders property array.
    * @param m int, characteristic Golomb parameter m, range [2,512]
    * @param pos int, the position
    */
   public void setGolombCode(int m,int pos) {
       if (pos<0||pos>coders.length-1) throw new RuntimeException("There is not such position in coders array.");
       coders[pos]=new GolombCode(m);
   }
   
   /**
    * This method assigns a GolombDecode instance with characteristic parameter
    * m to the indicated position pos in the component coders property array.
    * @param m int, characteristic Golomb parameter m, range [2,512]
    * @param pos int, the position
    */
   public void setGolombDecode(int m,int pos) {
       if (pos<0||pos>coders.length-1) throw new RuntimeException("There is not such position in coders array.");
       coders[pos]=new GolombDecode(m);
   }
   
   /**
    * This method prints the mean redundancy calculated taking into account all
    * redundancies for probabilities in the range [0.5,1.0].
    */
   public void showMeanRedundancy() {
       double[][] data=getRedundancyData(false);
       Double mean=getMeanRedundancy(data);
       System.out.println("The exact redundacy mean of this coder is: "+mean);
   }
   
   /**
    * This method prints the circular buffer size used by the encoder, in case
    * of the decoder it does not use the buffer but it uses the value as well.
    */
   public void showCircBufferSize() {
       System.out.println("The circular buffer size is: "+circBufferSize);
   }
   
   /**
    * This method prints the number of bins or containers in use by the encoder
    * or the decoder. This is also the number of component coders, and cutoffs
    * if we do not take into account the half maximum cutoff stored at first
    * position of its array.
    */
   public void showContainers() {
       System.out.println("The amount of containers is: "+probTree.getBINNUM());
   }
   
   /**
    * This method returns the number of bins or containers in use by the encoder
    * or the decoder. This is also the number of component coders, and cutoffs
    * if we do not take into account the half maximum cutoff stored at first
    * position of its array.
    */
   public int getContainersAmount() {
       return probTree.getBINNUM();
   }  
   
   /**
    * This method returns the internal probabilities array in use by the encoder
    * or the decoder if it is set, if it is not it returns null.
    * @return, int[], the internal stored probability model array
    */
   public int[] getProbabilities() {
       return probabilities;
   }
  
   /**
    * This method returns a copy of the acumulated internal output BitSet
    * generated by the encoding or decoding process.
    * @return SizedBitSet, acumulated internal output BitSet copy
    */
   public BitSet getOutput() {
       return (BitSet)output.clone();
   }
   
   /**
    * This method returns the size of the acumulated internal output BitSet
    * generated by the encoding or decoding process.
    * @return int, acumulated internal output BitSet size
    */
   public int getOutputSize() {
       return usedOutput;
   }
   
   /**
    * This methods cleans output BitSet and the variable controlling its size
    * so the encoding or decoding process can continue beyond the BitSet
    * capacity limit, (aprox 245MB). The normal use of this is after yoy get
    * and keep the output form the interface.
    */
   public void clearOutput() {
       output.clear();
       usedOutput = 0;
   }
   
   /**
    * This method prints a summary of containers(bins), ranges of probability
    * for each container extracted from the internal stored cutOffs, and
    * component coder for each container, it is a complete internal state of
    * the coder or decoder.
    */
   public void showCodersAndProbRanges() {
       System.out.println("The assigned probability intervals and component codes for each container or bin is: ");
       if (probTree.getCutOffs().length!=coders.length+1) throw new RuntimeException("Diferent amount of coders and cutOffs.");
       int[] cutOffs=new int[probTree.getCutOffs().length];
       System.arraycopy(probTree.getCutOffs(),0,cutOffs,0,probTree.getCutOffs().length);
       for (int i=0;i<coders.length;i++) {
           if (coders[i] instanceof ShortHandCode)
               System.out.println("Bin "+i+": ["+cutOffs[i]+","+cutOffs[i+1]+")"+
                       " : ShortHand Code: "+
                       ((ShortHandCode)coders[i]).getShortHandString());
           else if (coders[i] instanceof GolombCode)
               System.out.println("Bin "+i+": ["+cutOffs[i]+","+cutOffs[i+1]+")"+
                       " : Golomb Code("+coders[i].getM()+")");
           else if (coders[i] instanceof GolombDecode)
               System.out.println("Bin "+i+": ["+cutOffs[i]+","+cutOffs[i+1]+")"+
                       " : Golomb Decode("+coders[i].getM()+")");
           else System.out.println("Bin "+i+": ["+cutOffs[i]+","+cutOffs[i+1]+")"+
                       " : Transparent or copy Code");
       }
   }
   
   /**
    * This method reconfigure all the important aspects of the coder or decoder
    * in a simple way from an array of custom cutOffs and another of component
    * codes, if the lengths of both do not match it throws an exception.
    * @param customBinsCutoffs int[], array of custom cutOffs not including the
    *                           half maximum probability equivalent to the 0.5
    *                           probability. This probability will be added
    *                           internally and must be the minimum in the
    *                           internal array so the minimum in customBinsCutoffs
    *                           must be over half the maximum. Be carefull the
    *                           length of the array is in range [1,512], or an
    *                           exception will be thrown.
    * @param ccs ComponentCode[], array of component coders with the same
    *                              length than customBinsCutoffs. Be carefull
    *                              the length of the array is in range [1,512],
    *                              or an exception will be thrown.
    */
   public void setBinsProbsAndCoders(int[] customBinsCutoffs,ComponentCode[] ccs) {
       if (customBinsCutoffs!=null) {
           setNewBinNodeBuffer(customBinsCutoffs);
           probTree = ProbTree.getInstance(customBinsCutoffs);
       }
       if (ccs!=null) {
           coders = new ComponentCode[ccs.length];
           System.arraycopy(ccs,0,coders,0,ccs.length); 
       }   
       if (coders.length!=probTree.getBINNUM())
           throw new RuntimeException("Number of bins and coders are diferent.");  
   }
   
   /**
    * This method is equivalent to the use of:
    *       setBinsProbsAndCoders(getTruncCutOffs(n),getTruncCodes(n))
    * If a problem occurs with the cutoffs, like a too high minimum probability,
    * an exception will be thrown.
    * @param n int, the n where the trunc will be made
    */
   public void truncBinsProbsAndCoders(int n) {
       setBinsProbsAndCoders(getTruncCutOffs(n),getTruncCodes(n));
   }
     
   /**
    * This method returns de theoretical mean redundancy of input, given the
    * bins its bits belong.
    * @return Double, theoretical mean redundancy of input
    */
   public Double getRealRedundancy() {
       if (getProbabilitiesLength()==0) throw new RuntimeException("There is not loaded data.");
       int bin;
       Double ret=0.0; Double temp;
       for (int i=0;i<getProbabilitiesLength();i++) {
           bin=probTree.getBin(probabilities[i]);
           temp=new Double(probabilities[i])/probTree.maxProb();
           ret+=coders[bin].getRedundancy(temp);
       }

       return ret/getProbabilitiesLength();
   }
   
   /**
    * This method prepares the data in a 2 dimensions array of Doubles with
    * all the Redundancies of all possible probabilities under a given bin.
    * @param bin int, the bin or container
    * @param pNorm boolean, it affects how data is prepared, a true will use
    *              probabilities in the range [0.5,1.0] and a false will use
    *              the range [midprob,maxprob] (integer to Doubles)
    * @return double[][], the data structure with all redundancies for every
    *                     probability under the given bin or container
    */
   double[][] getRedundancyData(int bin,boolean pNorm) {
       int[] cutOffs=probTree.getCutOffs();
       double[][] data=new double [2][cutOffs[bin+1]-cutOffs[bin]];
       for (int i=cutOffs[bin];i<cutOffs[bin+1];i++) {
           prepareData(data,bin,i,pNorm,cutOffs[bin]);
       }
       return data;
   }
   
   /**
    * This method prepares the data in a 2 dimensions array of Doubles with
    * all the Redundancies of all possible probabilities in range [0.5,1.0].
    * @param pNorm boolean, it affects how data is prepared, a true will use
    *              probabilities in the range [0.5,1] and a false will use the
    *              range [midprob,maxprob] (integers to Doubles)
    * @return double[][], the data structure with all redundancies for every
    *                     probability in the range [0.5,1.0]
    */
   double[][] getRedundancyData(boolean pNorm) {

       double[][] data=new double [2][(probTree.maxProb()-probTree.midProb())+1];
       int bin;
       for (int i=probTree.midProb();i<=probTree.maxProb();i++) {
           bin=probTree.getBin(i);
           if (bin==-1) bin=probTree.getBin(i-1);
           prepareData(data,bin,i,pNorm,probTree.midProb());
       }
       return data;
   }
   
   /**
    * This method just fills the double[][] structure for every position
    * taking into acount the format to show the probabilities indicated
    * by the parameter pNorm.
    * It is used by:
    *         getRedundancyData(int bin,boolean pNorm)
    *         double[][] getRedundancyData(boolean pNorm)
    * Its main purpose is the call of getRedundancy(Double) for every
    * probability and the use of boolean pNorm to select the format of the
    * recorded probability, see pNorm parameter information.
    * @param data double[][], the structure to leave each result
    * @param bin int, the bin or container needed to use the proper component
    *                 coder in order to calculate the redundancy for param 'i'
    * @param i int, one probability in the range [midprob,maxprob]
    * @param pNorm boolean, it affects how data is prepared, a true will show
    *              probabilities in the range [0.5,1] and a false will use the
    *              range [midprob,maxprob] (integers to Doubles)
    * @param initProb int, the initial probability to start calculating and
    *                 keeping the redundancies in the data structure
    */
   void prepareData(double[][] data,int bin,int i,boolean pNorm,int initProb) {

       int i2=i-initProb;
       data[0][i2]=i;
       if (pNorm) {
           data[0][i2]/=probTree.maxProb();
           data[1][i2]=coders[bin].getRedundancy(data[0][i2]);
       } else {  
           data[1][i2]=coders[bin].getRedundancy(data[0][i2]/probTree.maxProb());
       }   
   }
   
   /**
    * This method calculates the mean redundancy of all stored values in the
    * parameter data
    * @param data double[][], all redundancies are in data[1], data[0] is for
    *             the probabilities
    * @return Double, the mean redundancy along all possible probabilities
    */
   static protected Double getMeanRedundancy(double[][] data) {
       Double mean=0.0;
       for (int i=0;i<data[1].length;i++) {
           mean+=data[1][i];
       }
       mean/=data[1].length;
       return mean;
   }
   
   /**
    * This method is the general one to show any graphic needed at the project,
    * it is a scattered plot using the JFreeChart jar file.
    * @param data double[][], data with probabilities and redundancies, all
    *                          redundancies are in data[1], data[0] is for
    *                          probabilities
    * @param showMean boolean, if true the mean redundancy will be calculated
    *                           and shown in the graphic like a dotted line
    * @param dotSize int, it selects the size of the dots shown in the plot
    * @param title String, title of the graphic to be shown over it
    * @param dataLabel String, label of data to be shown (horizontal axis)
    * @param valueLabel String, label of values to be shown (vertical axis)
    * @param frameLabel String, label to be shown on the window frame
    */
   /*static void renderGraphic(double[][] data,boolean showMean,int dotSize,String title,String dataLabel,String valueLabel,String frameLabel) {

       DefaultXYDataset dataset = new DefaultXYDataset();
       Comparable seriesKey="Redundancy";
       dataset.addSeries(seriesKey,data);

       if (showMean) {
           Double sparseLine=0.01*data[1].length;
           int length=data[1].length/sparseLine.intValue();
           Double max=0.0;
           Double min=Double.MAX_VALUE;
           for (int i=0;i<data[0].length;i++) {
               max=Math.max(max,data[0][i]);
               min=Math.min(min,data[0][i]);
           }
           Double step=Math.pow(new Double(length)/(max-min),-1.0);
           double[][] meanLine=new double[2][length];
           Double mean=getMeanRedundancy(data);
           for (int i=0;i<length;i++) {
               meanLine[0][i]=min+(step*i);
               meanLine[1][i]=mean;
           }
           Comparable meanKey="Mean";
           dataset.addSeries(meanKey,meanLine);
       }
       
       //We create Graphic with JFreeChart
       JFreeChart chart = ChartFactory.createScatterPlot(title,dataLabel,
        valueLabel,dataset,PlotOrientation.VERTICAL,true,true,false);
       //false,true,false);//Leyenda=name of each serie,tooltips,URLs
       
       //Change Shape to 1x1 red pixel circle
       Shape shape  = new Ellipse2D.Double(0,0,dotSize,dotSize);
       XYPlot xyPlot = (XYPlot) chart.getPlot();
       
       float a;//=xyPlot.getBackgroundImageAlpha();
       a=new Float(0.6);
       xyPlot.setBackgroundAlpha(a);
       
       XYItemRenderer renderer = xyPlot.getRenderer();
       renderer.setBasePaint(Color.red);
       renderer.setBaseShape(shape);
       
       //Set shape for all data
       for (int i=0;i<data[0].length;i++) {
           renderer.setSeriesShape(i,shape);
           //renderer.setSeriesPaint(i,Color current);
       }
       //A frame to show it
       ChartFrame frame = new ChartFrame(frameLabel, chart);
       frame.pack();
       frame.setVisible(true); 
   }*/
}
