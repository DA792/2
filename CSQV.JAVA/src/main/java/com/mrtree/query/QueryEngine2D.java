package com.mrtree.query;

import com.mrtree.geometry.Point2D;
import com.mrtree.geometry.Rectangle;
import com.mrtree.node.Node2D;
import com.mrtree.node.LeafNode2D;
import com.mrtree.node.InternalNode2D;
import com.mrtree.node.TreeBuilder2D;
import com.mrtree.util.HashUtil;
import java.util.List;
import java.util.ArrayList;

/**
 * 2D range query and verification engine.
 * Java equivalent of the C++ Query2D functionality.
 * 
 * @author Java port of CSQV MR-tree
 */
public class QueryEngine2D {
    
    /**
     * Perform a 2D range query on the MR-tree.
     * @param root the root of the 2D MR-tree
     * @param query the query rectangle
     * @param stats optional statistics collector
     * @return verification object for the query
     */
    public static VObject2D rangeQuery(Node2D root, Rectangle query, QueryStats2D stats) {
        if (root == null) {
            return null;
        }
        
        if (stats != null) {
            stats.nodesVisited++;
        }
        
        // If this is a leaf node, return all its points
        if (root.isLeaf()) {
            LeafNode2D leaf = (LeafNode2D) root;
            if (stats != null) {
                stats.pointsExamined += leaf.size();
            }
            return new VLeaf2D(leaf.getPoints());
        }
        
        // For internal nodes, check if MBR intersects with query
        Rectangle nodeRect = root.getRect();
        if (!nodeRect.intersects(query)) {
            // No intersection - prune this subtree
            if (stats != null) {
                stats.nodesPruned++;
            }
            return new VPruned2D(nodeRect, root.getHash());
        }
        
        // Intersection found - explore children
        VContainer2D container = new VContainer2D();
        InternalNode2D internal = (InternalNode2D) root;
        
        for (Node2D child : internal.getChildren()) {
            VObject2D childVo = rangeQuery(child, query, stats);
            container.append(childVo);
        }
        
        return container;
    }
    
    /**
     * Verify a 2D range query result.
     * @param vo verification object from the query
     * @param query the original query rectangle
     * @param stats optional statistics collector
     * @return verification result with reconstructed information
     */
    public static VResult2D verify(VObject2D vo, Rectangle query, QueryStats2D stats) {
        if (vo == null) {
            return null;
        }
        
        switch (vo.getType()) {
            case LEAF:
                return verifyLeaf((VLeaf2D) vo, query, stats);
                
            case PRUNED:
                return verifyPruned((VPruned2D) vo, query, stats);
                
            case CONTAINER:
                return verifyContainer((VContainer2D) vo, query, stats);
                
            default:
                throw new IllegalArgumentException("Unknown verification object type: " + vo.getType());
        }
    }
    
    /**
     * Verify a leaf verification object.
     */
    private static VResult2D verifyLeaf(VLeaf2D leaf, Rectangle query, QueryStats2D stats) {
        List<Point2D> allPoints = leaf.getPoints();
        
        // Filter points that match the query
        List<Point2D> matchingPoints = new ArrayList<>();
        Rectangle leafMbr = Rectangle.empty();
        HashUtil.HashBuffer buffer = HashUtil.createBuffer();
        
        for (Point2D point : allPoints) {
            leafMbr = leafMbr.enlarge(point.location);
            buffer.putInt(point.id)
                  .putInt(point.getX())
                  .putInt(point.getY());
            
            if (point.isInside(query)) {
                matchingPoints.add(point);
                if (stats != null) {
                    stats.pointsReturned++;
                }
            }
        }
        
        byte[] leafHash = buffer.hash();
        return new VResult2D(leafMbr, leafHash, matchingPoints);
    }
    
    /**
     * Verify a pruned verification object.
     */
    private static VResult2D verifyPruned(VPruned2D pruned, Rectangle query, QueryStats2D stats) {
        // Use provided MBR and hash for pruned nodes
        return new VResult2D(pruned.getRect(), pruned.getHash(), new ArrayList<>());
    }
    
    /**
     * Verify a container verification object.
     */
    private static VResult2D verifyContainer(VContainer2D container, Rectangle query, QueryStats2D stats) {
        List<Point2D> allMatchingPoints = new ArrayList<>();
        Rectangle combinedMbr = Rectangle.empty();
        HashUtil.HashBuffer buffer = HashUtil.createBuffer();
        
        for (VObject2D child : container.getChildren()) {
            VResult2D childResult = verify(child, query, stats);
            
            // Collect matching points
            allMatchingPoints.addAll(childResult.getPoints());
            
            // Update combined MBR and hash buffer
            Rectangle childRect = childResult.getRect();
            byte[] childHash = childResult.getHash();
            
            combinedMbr = combinedMbr.enlarge(childRect);
            buffer.putInt(childRect.lx)
                  .putInt(childRect.ly)
                  .putInt(childRect.ux)
                  .putInt(childRect.uy)
                  .putBytes(childHash);
        }
        
        byte[] combinedHash = buffer.hash();
        return new VResult2D(combinedMbr, combinedHash, allMatchingPoints);
    }
    
    /**
     * Perform complete 2D range query with verification.
     * @param root the root of the 2D MR-tree
     * @param query the query rectangle
     * @param stats optional statistics collector
     * @return verification result
     */
    public static VResult2D queryAndVerify(Node2D root, Rectangle query, QueryStats2D stats) {
        if (stats != null) {
            stats.nodesVisited = 0;
            stats.nodesPruned = 0;
            stats.pointsExamined = 0;
            stats.pointsReturned = 0;
        }
        
        // Perform query
        long queryStart = System.nanoTime();
        VObject2D vo = rangeQuery(root, query, stats);
        long queryEnd = System.nanoTime();
        
        if (stats != null) {
            stats.queryTimeNs = queryEnd - queryStart;
        }
        
        // Perform verification
        long verifyStart = System.nanoTime();
        VResult2D result = verify(vo, query, stats);
        long verifyEnd = System.nanoTime();
        
        if (stats != null) {
            stats.verifyTimeNs = verifyEnd - verifyStart;
        }
        
        return result;
    }
    
    /**
     * Count the number of points in a verification object.
     * @param vo verification object
     * @return number of points
     */
    public static long countPoints(VObject2D vo) {
        if (vo == null) {
            return 0;
        }
        
        switch (vo.getType()) {
            case LEAF:
                return ((VLeaf2D) vo).getSize();
                
            case PRUNED:
                return 0; // Pruned nodes don't contribute points
                
            case CONTAINER:
                long total = 0;
                VContainer2D container = (VContainer2D) vo;
                for (VObject2D child : container.getChildren()) {
                    total += countPoints(child);
                }
                return total;
                
            default:
                return 0;
        }
    }
}
