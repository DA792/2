package com.mrtree.query;

/**
 * Base class for 2D verification objects.
 * Java equivalent of the C++ VObject2D class.
 * 
 * @author Java port of CSQV MR-tree
 */
public abstract class VObject2D {
    protected final VObjectType type;
    
    /**
     * Constructor for verification object.
     * @param type the verification object type
     */
    protected VObject2D(VObjectType type) {
        this.type = type;
    }
    
    /**
     * Get the type of the verification object.
     * @return verification object type
     */
    public VObjectType getType() {
        return type;
    }
    
    /**
     * Check if this is a leaf verification object.
     * @return true if leaf verification object
     */
    public boolean isLeaf() {
        return type == VObjectType.LEAF;
    }
    
    /**
     * Check if this is a pruned verification object.
     * @return true if pruned verification object
     */
    public boolean isPruned() {
        return type == VObjectType.PRUNED;
    }
    
    /**
     * Check if this is a container verification object.
     * @return true if container verification object
     */
    public boolean isContainer() {
        return type == VObjectType.CONTAINER;
    }
}
