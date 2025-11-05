package com.mrtree.node;

import com.mrtree.geometry.Rectangle;

/**
 * Abstract base class for 2D MR-tree nodes.
 * Java equivalent of the C++ Node2D class.
 * 
 * @author Java port of CSQV MR-tree
 */
public abstract class Node2D {
    protected final NodeType type;      // Node type indicator
    protected final Rectangle rect;     // Bounding rectangle
    protected final byte[] hash;        // SHA-256 digest of the node
    
    /**
     * Constructor for 2D tree node.
     * @param type the node type
     * @param rect the MBR of the node
     * @param hash the hash value of the node
     */
    protected Node2D(NodeType type, Rectangle rect, byte[] hash) {
        this.type = type;
        this.rect = rect;
        this.hash = hash.clone(); // Defensive copy
    }
    
    /**
     * Get the type of the node.
     * @return node type
     */
    public NodeType getType() {
        return type;
    }
    
    /**
     * Get the MBR of the node.
     * @return bounding rectangle
     */
    public Rectangle getRect() {
        return rect;
    }
    
    /**
     * Get the digest of the node.
     * @return node hash (defensive copy)
     */
    public byte[] getHash() {
        return hash.clone();
    }
    
    /**
     * Check if this is a leaf node.
     * @return true if leaf node
     */
    public boolean isLeaf() {
        return type == NodeType.LEAF;
    }
    
    /**
     * Check if this is an internal node.
     * @return true if internal node
     */
    public boolean isInternal() {
        return type == NodeType.INTERNAL;
    }
    
    /**
     * Get the number of entries in this node.
     * @return number of entries
     */
    public abstract int size();
    
    /**
     * String representation of the node.
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("%s[type=%s, rect=%s, size=%d]", 
                           getClass().getSimpleName(), type, rect, size());
    }
}
