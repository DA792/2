/**
 *  @file Node2D.hpp
 *  @author Modified for 2D Range Query System
 *  
 *  Optimized Merkle R-tree nodes for 2D range queries
 */

#ifndef NODE2D_H
#define NODE2D_H

#include "Geometry.hpp"
#include "Hash.hpp"
#include "Point2D.hpp"

/**
 *  This enum defines the kind of a MR-tree node for 2D points.
 */
enum Node2DType {N2D_LEAF, N2D_INT};

/**
 *  This class represents a generic node of the 2D MR-tree.
 *  Optimized for 2D range queries with minimal overhead.
 */
class Node2D {
private:
  Node2DType type;    ///< Node type indicator
  Rectangle rect;     ///< Bounding rectangle
  hash_t hash;        ///< The digest of the node
  
public:
  /**
   *  Constructor for 2D tree node.
   *  @param t the node type
   *  @param r the MBR of the node
   *  @param h the hash value of the node
   */
  Node2D(Node2DType t, Rectangle r, hash_t h) : type(t), rect(r), hash(h) {}

  /**
   *  Virtual destructor.
   */
  virtual ~Node2D() {}

  /**
   *  Returns the type of the node.
   */
  Node2DType getType() const { return type; }

  /**
   *  Returns the MBR of the node.
   */
  Rectangle getRect() const { return rect; }

  /**
   *  Returns the digest of the node.
   */
  hash_t getHash() const { return hash; }
};

/**
 *  Leaf node for 2D points.
 */
class LeafNode2D : public Node2D {
private:
  std::vector<Point2D> points; ///< List of 2D points in this leaf
  
public:
  /**
   *  Constructs a new 2D leaf node.
   *  @param r the node rectangle
   *  @param h the hash value of the node
   *  @param points list of 2D points to be stored
   */
  LeafNode2D(Rectangle r, hash_t h, std::vector<Point2D> points)
  : Node2D(N2D_LEAF, r, h), points(std::move(points)) {}

  /**
   *  Returns the list of 2D points contained in the node.
   */
  const std::vector<Point2D> &getPoints() const { return points; }
  
  /**
   *  Returns the number of points in this leaf.
   */
  size_t size() const { return points.size(); }
};

/**
 *  Internal node for 2D tree.
 */
class IntNode2D : public Node2D {
private:
  std::vector<Node2D*> children; ///< List of child nodes
  
public:
  /**
   *  Constructs a new 2D internal node.
   *  @param r the node rectangle
   *  @param h the hash value of the node
   *  @param children list of child nodes
   */
  IntNode2D(Rectangle r, hash_t h, std::vector<Node2D*> children)
  : Node2D(N2D_INT, r, h), children(std::move(children)) {}

  /**
   *  Returns the list of children of the node.
   */
  const std::vector<Node2D*> &getChildren() const { return children; }
  
  /**
   *  Returns the number of children.
   */
  size_t size() const { return children.size(); }
};

/**
 *  Creates a new 2D leaf node from a list of points.
 *  @param points the list of 2D points
 *  @return a leaf node for the 2D MR-tree
 */
LeafNode2D *make_leaf_2d(std::vector<Point2D> points);

/**
 *  Creates a new 2D internal node from a list of child nodes.
 *  @param children the list of child nodes
 *  @return an internal node for the 2D MR-tree
 */
IntNode2D *make_internal_2d(std::vector<Node2D*> children);

/**
 *  Builds a 2D MR-tree from a list of points using bulk-loading.
 *  @param points list of 2D points
 *  @param capacity page capacity
 *  @return pointer to the root node of the 2D tree
 */
Node2D *build_2d_tree(std::vector<Point2D> &points, size_t capacity);

/**
 *  Frees the memory occupied by a 2D MR-tree.
 *  @param root pointer to the tree root
 */
void delete_2d_tree(Node2D *root);

/**
 *  Counts the number of leaf nodes in the 2D tree.
 *  @param root pointer to the tree root
 *  @return the number of leaves
 */
int count_2d_leaves(Node2D *root);

/**
 *  Computes the height of the 2D tree.
 *  @param root pointer to the tree root
 *  @return the height of the tree
 */
int height_2d_tree(Node2D *root);

/**
 *  Prints statistics about the 2D tree.
 *  @param root pointer to the tree root
 */
void print_2d_tree_stats(Node2D *root);

#endif
