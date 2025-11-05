package com.mrtree.query;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Verification object for explored 2D internal nodes.
 * Java equivalent of the C++ VContainer2D class.
 * 
 * @author Java port of CSQV MR-tree
 */
public class VContainer2D extends VObject2D {
    private final List<VObject2D> children;
    
    /**
     * Constructor for container verification object.
     */
    public VContainer2D() {
        super(VObjectType.CONTAINER);
        this.children = new ArrayList<>();
    }
    
    /**
     * Add a verification object to the container.
     * @param vo verification object to add
     */
    public void append(VObject2D vo) {
        if (vo != null) {
            children.add(vo);
        }
    }
    
    /**
     * Get a verification object by index.
     * @param index the index
     * @return verification object at the specified index
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public VObject2D get(int index) {
        return children.get(index);
    }
    
    /**
     * Get the number of children.
     * @return number of children
     */
    public int size() {
        return children.size();
    }
    
    /**
     * Check if the container is empty.
     * @return true if no children
     */
    public boolean isEmpty() {
        return children.isEmpty();
    }
    
    /**
     * Get all children.
     * @return unmodifiable list of children
     */
    public List<VObject2D> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    /**
     * String representation.
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("VContainer2D[children=%d]", children.size());
    }
}
