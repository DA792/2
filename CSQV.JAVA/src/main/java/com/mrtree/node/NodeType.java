package com.mrtree.node;

/**
 * Enumeration for MR-tree node types.
 * Java equivalent of the C++ Node2DType enum.
 * 
 * @author Java port of CSQV MR-tree
 */
public enum NodeType {
    LEAF,     // Leaf node containing data points
    INTERNAL  // Internal node containing child nodes
}
