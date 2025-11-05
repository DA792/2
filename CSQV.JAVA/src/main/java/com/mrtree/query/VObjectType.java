package com.mrtree.query;

/**
 * Types of verification objects for 2D range queries.
 * Java equivalent of the C++ VObject2DType enum.
 * 
 * @author Java port of CSQV MR-tree
 */
public enum VObjectType {
    LEAF,      // Verification object for leaf nodes
    PRUNED,    // Verification object for pruned internal nodes
    CONTAINER  // Verification object for explored internal nodes
}
