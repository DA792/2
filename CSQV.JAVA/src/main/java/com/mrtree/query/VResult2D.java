package com.mrtree.query;

import com.mrtree.geometry.Point2D;
import com.mrtree.geometry.Rectangle;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Result of 2D range query verification.
 * Java equivalent of the C++ VResult2D class.
 * 
 * @author Java port of CSQV MR-tree
 */
public class VResult2D {
    private final Rectangle rect;           // Reconstructed MBR
    private final byte[] hash;             // Reconstructed hash
    private final List<Point2D> points;   // Query result points
    
    /**
     * Constructor for verification result.
     * @param rect reconstructed MBR
     * @param hash reconstructed hash
     * @param points query result points
     */
    public VResult2D(Rectangle rect, byte[] hash, List<Point2D> points) {
        this.rect = rect;
        this.hash = hash.clone(); // Defensive copy
        this.points = new ArrayList<>(points); // Defensive copy
    }
    
    /**
     * Get the reconstructed rectangle.
     * @return rectangle
     */
    public Rectangle getRect() {
        return rect;
    }
    
    /**
     * Get the reconstructed hash.
     * @return hash (defensive copy)
     */
    public byte[] getHash() {
        return hash.clone();
    }
    
    /**
     * Get the query result points.
     * @return unmodifiable list of points
     */
    public List<Point2D> getPoints() {
        return Collections.unmodifiableList(points);
    }
    
    /**
     * Get the number of result points.
     * @return number of points
     */
    public int count() {
        return points.size();
    }
    
    /**
     * Check if the result is empty.
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
        return String.format("VResult2D[rect=%s, points=%d]", rect, points.size());
    }
}
