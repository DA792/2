package com.mrtree.node;

import com.mrtree.geometry.Rectangle;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Internal node for 2D MR-tree.
 * Contains a list of child nodes.
 * Java equivalent of the C++ IntNode2D class.
 * 
 * @author Java port of CSQV MR-tree
 */
public class InternalNode2D extends Node2D {
    private final List<Node2D> children; // List of child nodes
    
    /**
     * Constructor for internal node.
     * @param rect the node rectangle
     * @param hash the hash value of the node
     * @param children list of child nodes
     */
    public InternalNode2D(Rectangle rect, byte[] hash, List<Node2D> children) {
        super(NodeType.INTERNAL, rect, hash);
        this.children = new ArrayList<>(children); // Defensive copy
    }
    
    /**
     * Get the list of children of the node.
     * @return unmodifiable list of children
     */
    public List<Node2D> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    /**
     * Get the number of children.
     * @return number of children
     */
    @Override
    public int size() {
        return children.size();
    }
    
    /**
     * Check if the internal node has no children.
     * @return true if node has no children
     */
    public boolean isEmpty() {
        return children.isEmpty();
    }
    
    /**
     * Get a specific child by index.
     * @param index the index of the child
     * @return the child at the specified index
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public Node2D getChild(int index) {
        return children.get(index);
    }
    
    /**
     * String representation of the internal node.
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("InternalNode2D[rect=%s, children=%d]", rect, children.size());
    }
}
