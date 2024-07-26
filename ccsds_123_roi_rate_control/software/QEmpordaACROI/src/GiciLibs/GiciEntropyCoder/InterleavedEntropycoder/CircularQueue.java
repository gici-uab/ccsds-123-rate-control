package GiciEntropyCoder.InterleavedEntropycoder;

/**
 * @version 1.0
 * @author Juan Ramón Soler Cabrejas
 * 
 * This class inherits from BinNodeBuffer, which provides the basic buffering
 * services CircularQueue class extends, so uses the same Node class with two
 * parts, a TreeNode reference and an int code representing the bin or container
 * in this case, with an associated code tree where the TreeNode instance
 * belongs.
 * The purpose of this class is to keep a buffer with a behavior of a circular
 * queue, this is very useful for the InterleavedEntropyEncoder instance to
 * keep in memory an amount of partial input code words and complete output
 * codewords that can not be emited to the output because they are not yet
 * complete output codewords or they are not yet at the first position of the
 * queue. This wait of every code word until be completed and at the head of the
 * queue is necesary and a central part of an InterleavedEntropyEncoder.
 * Separated bits from the input with similar probabilities to be 0 and
 * belonging to the same bin are joined to form compact output codewords, the
 * wait to be on the queue head allows the decoder to know what bit go where
 * because it can reproduce when the coder queue must be full and it generates
 * an output codeword for same container input bits. 
 * 
 */
class CircularQueue extends BinNodeBuffer {
    
    
    private int first = 0;
    private int last = -1;
    private int numElements = 0;

    /**
     * This is the constructor of the class CircularQueue, given a size for
     * itself it initializes the circular queue buffer.
     * @param size int, the size for the circular queue
     */
    public CircularQueue(int size) {
        super(size);
        initBinNodeBuffer();
    }
    
    /**
     * This method inform about the emptiness of the circular queue.
     * @return boolean true if queue is empty, false otherwise
     */
    protected boolean emptyQueue() {
        return (numElements==0);
    }
   
    /**
     * This method inform about the fullness of the circular queue.
     * @return boolean true if queue is full, false otherwise
     */
    protected boolean fullQueue() {
        return (numElements==getBufferSize());
    }
    
    /**
     * This method adds or prepare a new position at the last postion of the
     * circular queue and returns the container contained at that position.
     * This way the caller of this function can full the container after the
     * call to this method.
     * @return, Node the container node at the last position of circular queue
     */
    protected Node shiftInQueueElement() { 
        last = (++last)%getBufferSize();
        if (numElements==getBufferSize())
            throw new RuntimeException("CircularQueue buffer overflow");
        numElements++;
        return getNodeByBin(last);   
    }
   
    /**
     * This method removes the first position of the circular queue.
     */
    protected void shiftOutQueueElement() {
       if (numElements==0)
            throw new RuntimeException("CircularQueue buffer underflow");
       numElements--;    
       if (numElements==0) {
            first=0;
            last=-1;
       } else first = (++first)%getBufferSize();
    }
    
    /**
     * This method writes a Node data by its parts into the last postion of
     * the circular queue. The node parts are the same declared in the ascendant
     * class BinNodeBuffer and, in this case it is stored a reference to
     * TreeNode, which represents a partial input codeword or even a complete
     * output codeword if a TruncatedBinary instance is passed, and an int, the
     * bin or container associated with the coder this TreeNode belongs to.
     * @param tn TreeNode, a pure TreeNode or TruncatedBinary
     * @param bin int, bin or container associated with the coder this
     *                  TreeNode belongs to
     */
    protected void setLast(TreeNode tn,int bin) {
        setNodeByBin(tn,bin,last);
    }   
    
    /**
     * This method informs about a Node is a complete output codeword or
     * TruncatedBinary or an uncomplete input codeword or TreeNode instance.
     * @param n Node, the node to inform about
     * @return boolean true means complete output codeword
     *                 false means uncomplete input codeword
     */
    protected boolean isLeaf(Node n) {
        return (n.entry instanceof TruncatedBinary);
    }
       
    /**
     * This method gets back the first node in the circular queue.
     * @return Node the first node in the circular queue
     */
    protected Node getFirstNode() {
        return (getNodeByBin(first));
    }
       
    /**
     * This method gets back the first node in the circular queue, but only if
     * it is a complete output codeword.
     * @return TruncatedBinary first node in circular queue if it is a complete
     *                         output codeword, null otherwise
     */
    protected TruncatedBinary getFirstIfLeaf() {
        if (numElements==0) return null;
        Node n=getNodeByBin(first);
        if (n.entry instanceof TruncatedBinary) {
            return ((TruncatedBinary)n.entry);
        }
        return null;
            
    }
}
