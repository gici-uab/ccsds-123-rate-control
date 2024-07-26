package GiciEntropyCoder.InterleavedEntropycoder;

/**
 * @version 1.0
 * @author Juan Ramón Soler Cabrejas
 * 
 * The purpose of this class is double, InterleavedEntropyCoder extends this
 * class and InterleavedEntropyDecoder and InterleavedEntropyCoder extends
 * the former. When this class is been inherited by de Encoder it is used to
 * store references to points in the Circular queue of output codes, when that
 * codes are partial. When this class is been inherited by the Decoder it is
 * used to store complete input codewords, but this codes need to wait there
 * until they are old enough so we can be sure about their bits are flush bits
 * or not.
 */
class BinNodeBuffer {
    
    /**
     * This subclass Node is used to define a node in the buffer, all the
     * descendants of BinNodeBuffer will inherit this definition this way.
     */
    class Node {
       /**  Each node of the class has a TreeNode reference to a node in a
        * coder. In the InterleavedEntropyEncoder this references to a normal
        * TreeNode if it is keeping a partial code or a TruncatedBinary if it
        * is keeping a complete one. In the InterleavedEntropyDecoder it
        * always references a TruncatedBinary.
        */
        TreeNode entry;
        /**  Each node of the class has also a code. This keeps the bin of code
         * where the TreeNode field is referencing when this is been used by
         * the class InterleavedEntropyEncoder. When it is been used by
         * InterleavedEntropyDecoder class this code is used like a codeword
         * number asigned when the output codeword is generated, so it is a date
         * of birth to know when a codeword is old enough to be sent to output.
         */
        int code=-1;
            
        Node() {}
        /**
         * This is the constructor of a node.
         * @param entry TreeNode or his descendant TruncatedBinary
         * @param code int, meaning the bin or the codeword date of birth
         */
        Node(TreeNode entry,int code) {
            this.entry=entry;
            this.code=code;
        }
    }
    
    int bufferSize;
    Node[] partCoder;
    
    /**
     * This is a constructor of the class BinNodeBuffer, it has a parameter but
     * the only thing it reads from it is the size. It reserves space equal to
     * his size for nodes and its assigns its size to an internal property.
     * @param array int[], if the array passed like parameter is null it makes
     *                      space for Nodes equal to the default amount of BINS.
     *                      If this parameter is not null then it reserves space
     *                      for an amount of nodes equal to the length of array.
     */
    BinNodeBuffer(int[] array) {
        setNewBinNodeBuffer(array);
    }
 
    /**
     * This is the default constructor of BinNodeBuffer class. It reserves space
     * for nodes and stores the size reserved in an independent variable.
     * @param size int, the size to reserve and store
     */

    BinNodeBuffer(int size) {
        bufferSize=size;
        partCoder = new Node[bufferSize];
    }

    /**
     * This method sets a new internal buffer and size property, it has a
     * parameter but the only thing it reads from it is the size. It reserves
     * space equal to his size for nodes and its assigns its size to an internal
     * property.
     * @param array int[], if the array passed like parameter is null it makes
     *                      space for Nodes equal to the default amount of BINS.
     *                      If this parameter is not null then it reserves space
     *                      for an amount of nodes equal to the length of array.
     */
    final void setNewBinNodeBuffer(int[] array){
        int size=ProbTree.getDefaultBINNUM();
        if (array!=null) size=array.length; 
        bufferSize=size;
        partCoder = new Node[bufferSize];
    }
   
    /**
     * This method initializes the contents of the internal buffer of nodes,
     * creating a new Node for each position of the buffer. It is separated from
     * the constructors because some uses of this buffer needs to be initialized
     * and others not.
     */
    void initBinNodeBuffer() {
        for(int i=0;i<bufferSize;i++)
            partCoder[i]=new Node();
    }
    
    /**
     * This method returns the buffer size.
     * @return int the buffer size
     */
    int getBufferSize() {
        return bufferSize;
    }
    
    /**
     * This method returns the Node in the buffer at the specified position.
     * @param pos int, the position
     * @return Node the node
     */
    Node getNodeByBin(int pos) {
        return partCoder[pos];
    }
    
    /**
     * This method looks if some position of the buffer has an assigned Node.
     * @param pos int, the postion
     * @return boolean true if the position references null, false otherwise
     */
    boolean isNodeNull(int pos) {
        return partCoder[pos]==null;
    }
    
    /**
     * This method assigns a Node to the specified position, by Node parts.
     * @param tn TreeNode, part of the Node to be assigned to position pos
     * @param code int, part of the Node to be assigned to position pos
     * @param pos int, the position
     */
    void setNodeByBin(TreeNode tn,int code,int pos) {
        partCoder[pos].entry=tn;
        partCoder[pos].code=code;
    }
    
    /**
     * This method assigns a Node to the specified position.
     * @param n Node, the node to be assigned
     * @param pos int, the position
     */
    void setNodeByBin(Node n,int pos) {
        partCoder[pos]=n;
    }
    
    /**
     * This method assigns the code part of a Node to the Node in buffer at
     * the specified position.
     * @param code int, the code to be assigned
     * @param pos int, the position
     */
    void setCodeByBin(int code,int pos) {
        partCoder[pos].code=code;
    }
    
    /**
     * This method clears or assigns null to the specified position of buffer.
     * @param pos int, the position 
     */
    void clearNodeByBin(int pos) {
        partCoder[pos]=null;
    }
}
