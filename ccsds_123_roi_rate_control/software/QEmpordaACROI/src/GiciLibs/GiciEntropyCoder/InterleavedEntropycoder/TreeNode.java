package GiciEntropyCoder.InterleavedEntropycoder;

/**
 * @version 1.0
 * @author Juan Ramón Soler Cabrejas
 * 
 * The purpose of this Class is just to act like ancestor of TruncatedBinary in
 * order to act like the basic brick for binary trees to keep component coders.
 * This way the nodes made under this class can be intermediate nodes of a
 * binary tree and point to other node of TreeNode class or point to leaf
 * nodes of the binary tree, that is, a TruncatedBinary instance. The class
 * keeps two treenode references in an array to be able to make binary trees.
 */
class TreeNode {
    private TreeNode[] sons;
    
    /**
     * Constructor to make a TreeNode.
     */
    protected TreeNode() {
        sons=new TreeNode[2];
    }
    
    /**
     * It returns de left or right son of this binary tree node.
     * @param pos 0 for left branch, 1 for right. The selected branch
     *            referes to the TreeNode object which will be returned.
     * @return TreeNode, the returned TreeNode object.
     */
    protected TreeNode getSon(int pos) {
        if (pos<0||pos>1) throw new RuntimeException("Parameter pos must be 0 or 1.");
        return sons[pos];
    }
    
    /**
     * It set up de left or right son of this binary tree node.
     * @param tb The TreeNode object which has to be referenced by this one.
     * @param pos 0 for left branch, 1 for right. The selected branch will
     *            refere to tb.
     */
    protected void setSon(TreeNode tb,int pos) {
        if (pos<0||pos>1) throw new RuntimeException("Parameter pos must be 0 or 1.");
        if (tb==null) throw new RuntimeException("Parameter tb can not be null.");
        sons[pos]=tb;
    }
}
