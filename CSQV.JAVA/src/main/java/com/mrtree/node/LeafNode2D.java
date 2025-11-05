package com.mrtree.node;

import com.mrtree.geometry.Point2D;
import com.mrtree.geometry.Rectangle;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Leaf node for 2D MR-tree.
 * Contains a list of 2D points.
 * Java equivalent of the C++ LeafNode2D class.
 * 
 * @author Java port of CSQV MR-tree
 */
public class LeafNode2D extends Node2D {
    private final List<Point2D> points; // List of 2D points in this leaf
    
    /**
     * Constructor for leaf node.
     * @param rect the node rectangle
     * @param hash the hash value of the node
     * @param points list of 2D points to be stored
     */
    public LeafNode2D(Rectangle rect, byte[] hash, List<Point2D> points) {
        super(NodeType.LEAF, rect, hash);
        this.points = new ArrayList<>(points); // Defensive copy
    }
    
    /**
     * Get the list of 2D points contained in the node.
     * @return unmodifiable list of points
     */
    public List<Point2D> getPoints() {
        return Collections.unmodifiableList(points);
    }
    
    /**
     * Get the number of points in this leaf.
     * @return number of points
     */
    @Override
    public int size() {
        return points.size();
    }
    
    /**
     * Check if the leaf is empty.
     * @return true if leaf contains no points
     */
    public boolean isEmpty() {
        return points.isEmpty();
    }
    
    /**
     * Get a specific point by index.
     * @param index the index of the point
     * @return the point at the specified index
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public Point2D getPoint(int index) {
        return points.get(index);
    }
    
    /**
     * String representation of the leaf node.
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("LeafNode2D[rect=%s, points=%d]", rect, points.size());
    }
}
