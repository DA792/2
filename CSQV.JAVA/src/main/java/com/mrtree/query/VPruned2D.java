package com.mrtree.query;

import com.mrtree.geometry.Rectangle;

/**
 * Verification object for pruned 2D internal nodes.
 * Java equivalent of the C++ VPruned2D class.
 * 
 * @author Java port of CSQV MR-tree
 */
public class VPruned2D extends VObject2D {
    private final Rectangle rect;
    private final byte[] hash;
    
    /**
     * Constructor for pruned verification object.
     * @param rect the rectangle of the pruned node
     * @param hash the hash of the pruned node
     */
    public VPruned2D(Rectangle rect, byte[] hash) {
        super(VObjectType.PRUNED);
        this.rect = rect;
        this.hash = hash.clone(); // Defensive copy
    }
    
    /**
     * Get the rectangle.
     * @return rectangle
     */
    public Rectangle getRect() {
        return rect;
    }
    
    /**
     * Get the hash.
     * @return hash (defensive copy)
     */
    public byte[] getHash() {
        return hash.clone();
    }
    
    /**
     * String representation.
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("VPruned2D[rect=%s]", rect);
    }
}
