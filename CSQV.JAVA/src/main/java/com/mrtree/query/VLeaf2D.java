package com.mrtree.query;

import com.mrtree.geometry.Point2D;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Verification object for 2D leaf nodes.
 * Java equivalent of the C++ VLeaf2D class.
 * 
 * @author Java port of CSQV MR-tree
 */
public class VLeaf2D extends VObject2D {
    private final List<Point2D> points;
    
    /**
     * Constructor for leaf verification object.
     * @param points list of 2D points
     */
    public VLeaf2D(List<Point2D> points) {
        super(VObjectType.LEAF);
        this.points = new ArrayList<>(points); // Defensive copy
    }
    
    /**
     * Get the list of points.
     * @return unmodifiable list of points
     */
    public List<Point2D> getPoints() {
        return Collections.unmodifiableList(points);
    }
    
    /**
     * Get the number of points.
     * @return number of points
     */
    public int getSize() {
        return points.size();
    }
    
    /**
     * Check if the verification object is empty.
     * @return true if no points
     */
    public boolean isEmpty() {
        return points.isEmpty();
    }
    
    /**
     * String representation.
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("VLeaf2D[points=%d]", points.size());
    }
}
