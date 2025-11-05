package com.mrtree.node;

import com.mrtree.geometry.Point2D;
import com.mrtree.geometry.Rectangle;
import com.mrtree.util.HashUtil;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Builder for 2D MR-tree using bulk-loading algorithm.
 * Java equivalent of the C++ tree building functions.
 * 
 * @author Java port of CSQV MR-tree
 */
public class TreeBuilder2D {
    
    /**
     * Build a 2D MR-tree from a list of points using bulk-loading.
     * @param points list of 2D points
     * @param capacity page capacity (max points per leaf)
     * @return root node of the 2D tree
     */
    public static Node2D buildTree(List<Point2D> points, int capacity) {
        if (points == null || points.isEmpty()) {
            return null;
        }
        
        // Sort points for spatial locality
        List<Point2D> sortedPoints = new ArrayList<>(points);
        Collections.sort(sortedPoints);
        
        // Create leaf nodes by splitting points into chunks
        List<Node2D> currentLevel = new ArrayList<>();
        
        for (int i = 0; i < sortedPoints.size(); i += capacity) {
            int end = Math.min(sortedPoints.size(), i + capacity);
            List<Point2D> chunk = sortedPoints.subList(i, end);
            currentLevel.add(makeLeaf(chunk));
        }
        
        // Build internal levels bottom-up
        while (currentLevel.size() > 1) {
            List<Node2D> nextLevel = new ArrayList<>();
            
            for (int i = 0; i < currentLevel.size(); i += capacity) {
                int end = Math.min(currentLevel.size(), i + capacity);
                List<Node2D> chunk = currentLevel.subList(i, end);
                nextLevel.add(makeInternal(chunk));
            }
            
            currentLevel = nextLevel;
        }
        
        return currentLevel.isEmpty() ? null : currentLevel.get(0);
    }
    
    /**
     * Create a new leaf node from a list of points.
     * @param points the list of 2D points
     * @return a leaf node for the 2D MR-tree
     */
    public static LeafNode2D makeLeaf(List<Point2D> points) {
        if (points.isEmpty()) {
            return new LeafNode2D(Rectangle.empty(), new byte[HashUtil.HASH_LENGTH], 
                                 new ArrayList<>());
        }
        
        // Compute MBR of all points
        Rectangle rect = computeMBR(points);
        
        // Create buffer for hashing
        HashUtil.HashBuffer buffer = HashUtil.createBuffer();
        for (Point2D point : points) {
            buffer.putInt(point.id)
                  .putInt(point.getX())
                  .putInt(point.getY());
        }
        
        // Compute hash
        byte[] hash = buffer.hash();
        
        return new LeafNode2D(rect, hash, points);
    }
    
    /**
     * Create a new internal node from child nodes.
     * @param children the list of child nodes
     * @return an internal node for the 2D MR-tree
     */
    public static InternalNode2D makeInternal(List<Node2D> children) {
        if (children.isEmpty()) {
            return new InternalNode2D(Rectangle.empty(), new byte[HashUtil.HASH_LENGTH], 
                                     new ArrayList<>());
        }
        
        // Compute MBR of all children
        Rectangle rect = Rectangle.empty();
        HashUtil.HashBuffer buffer = HashUtil.createBuffer();
        
        for (Node2D child : children) {
            Rectangle childRect = child.getRect();
            byte[] childHash = child.getHash();
            
            rect = rect.enlarge(childRect);
            
            // Add child's rectangle and hash to buffer
            buffer.putInt(childRect.lx)
                  .putInt(childRect.ly)
                  .putInt(childRect.ux)
                  .putInt(childRect.uy)
                  .putBytes(childHash);
        }
        
        // Compute hash
        byte[] hash = buffer.hash();
        
        return new InternalNode2D(rect, hash, children);
    }
    
    /**
     * Compute the minimum bounding rectangle of a list of 2D points.
     * @param points list of 2D points
     * @return the minimum bounding rectangle
     */
    private static Rectangle computeMBR(List<Point2D> points) {
        if (points.isEmpty()) {
            return Rectangle.empty();
        }
        
        Rectangle mbr = Rectangle.empty();
        for (Point2D point : points) {
            mbr = mbr.enlarge(point.location);
        }
        return mbr;
    }
    
    /**
     * Count the number of leaf nodes in the tree.
     * @param root root node of the tree
     * @return number of leaf nodes
     */
    public static int countLeaves(Node2D root) {
        if (root == null) {
            return 0;
        }
        
        if (root.isLeaf()) {
            return 1;
        }
        
        int count = 0;
        InternalNode2D internal = (InternalNode2D) root;
        for (Node2D child : internal.getChildren()) {
            count += countLeaves(child);
        }
        
        return count;
    }
    
    /**
     * Compute the height of the tree.
     * @param root root node of the tree
     * @return height of the tree
     */
    public static int computeHeight(Node2D root) {
        if (root == null) {
            return 0;
        }
        
        if (root.isLeaf()) {
            return 1;
        }
        
        int maxHeight = 0;
        InternalNode2D internal = (InternalNode2D) root;
        for (Node2D child : internal.getChildren()) {
            maxHeight = Math.max(maxHeight, computeHeight(child));
        }
        
        return maxHeight + 1;
    }
    
    /**
     * Print statistics about the 2D tree.
     * @param root root node of the tree
     */
    public static void printTreeStats(Node2D root) {
        if (root == null) {
            System.out.println("Tree is empty");
            return;
        }
        
        System.out.println("2D Tree Statistics:");
        System.out.println("  Height: " + computeHeight(root));
        System.out.println("  Leaves: " + countLeaves(root));
        
        Rectangle mbr = root.getRect();
        System.out.printf("  MBR: (%d, %d) to (%d, %d)%n", 
                         mbr.lx, mbr.ly, mbr.ux, mbr.uy);
    }
}
