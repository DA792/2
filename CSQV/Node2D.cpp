/**
 *  @file Node2D.cpp
 *  @author Modified for 2D Range Query System
 */

#include "Node2D.hpp"
#include <algorithm>
#include <queue>
#include <iostream>

/**
 *  Defines the number of chunks when splitting elements into groups.
 */
#define N_PARTS_2D(n, k) (((n) / (k)) + (((n) % (k)) != 0))

/**
 *  Size of an entry in internal nodes (rectangle + hash).
 */
#define ENTRY_SIZE_2D (4*sizeof(int32_t) + SHA256_DIGEST_LENGTH)

/**
 *  Creates a new 2D leaf node from a list of points.
 */
LeafNode2D *make_leaf_2d(std::vector<Point2D> points) {
  if (points.empty()) {
    return new LeafNode2D(EMPTY_RECT, hash_t{}, std::vector<Point2D>());
  }
  
  // Compute MBR of all points
  Rectangle rect = compute_mbr(points);
  
  // Create buffer for hashing
  Buffer buf(points.size() * (sizeof(uint32_t) + 2 * sizeof(int32_t)));
  for (const Point2D &p : points) {
    put_point2d(buf, p);
  }
  
  // Compute hash
  hash_t hash = sha256(buf);
  
  return new LeafNode2D(rect, hash, std::move(points));
}

/**
 *  Creates a new 2D internal node from child nodes.
 */
IntNode2D *make_internal_2d(std::vector<Node2D*> children) {
  if (children.empty()) {
    return new IntNode2D(EMPTY_RECT, hash_t{}, std::vector<Node2D*>());
  }
  
  // Compute MBR of all children
  Rectangle rect = EMPTY_RECT;
  Buffer buf(children.size() * ENTRY_SIZE_2D);
  
  for (Node2D *child : children) {
    Rectangle child_rect = child->getRect();
    hash_t child_hash = child->getHash();
    
    rect = enlarge(rect, child_rect);
    
    // Add child's rectangle and hash to buffer
    buf.put(child_rect.lx).put(child_rect.ly)
       .put(child_rect.ux).put(child_rect.uy)
       .put_bytes(child_hash.data(), child_hash.size());
  }
  
  // Compute hash
  hash_t hash = sha256(buf);
  
  return new IntNode2D(rect, hash, std::move(children));
}

/**
 *  Builds a 2D MR-tree using bulk-loading algorithm.
 */
Node2D *build_2d_tree(std::vector<Point2D> &points, size_t capacity) {
  if (points.empty()) {
    return nullptr;
  }
  
  // Sort points for spatial locality
  std::sort(points.begin(), points.end());
  
  // Create leaf nodes by splitting points into chunks
  std::vector<Node2D*> current_level;
  current_level.reserve(N_PARTS_2D(points.size(), capacity));
  
  for (size_t i = 0; i < points.size(); i += capacity) {
    size_t end = std::min(points.size(), i + capacity);
    std::vector<Point2D> chunk(points.begin() + i, points.begin() + end);
    current_level.push_back(make_leaf_2d(std::move(chunk)));
  }
  
  // Build internal levels bottom-up
  while (current_level.size() > 1) {
    std::vector<Node2D*> next_level;
    next_level.reserve(N_PARTS_2D(current_level.size(), capacity));
    
    for (size_t i = 0; i < current_level.size(); i += capacity) {
      size_t end = std::min(current_level.size(), i + capacity);
      std::vector<Node2D*> chunk(current_level.begin() + i, 
                                 current_level.begin() + end);
      next_level.push_back(make_internal_2d(std::move(chunk)));
    }
    
    current_level = std::move(next_level);
  }
  
  return current_level.empty() ? nullptr : current_level[0];
}

/**
 *  Frees memory occupied by a 2D MR-tree.
 */
void delete_2d_tree(Node2D *root) {
  if (!root) return;
  
  std::queue<Node2D*> queue;
  queue.push(root);
  
  while (!queue.empty()) {
    Node2D *node = queue.front();
    queue.pop();
    
    if (node->getType() == N2D_INT) {
      IntNode2D *internal = static_cast<IntNode2D*>(node);
      for (Node2D *child : internal->getChildren()) {
        queue.push(child);
      }
    }
    
    delete node;
  }
}

/**
 *  Counts leaf nodes in the 2D tree.
 */
int count_2d_leaves(Node2D *root) {
  if (!root) return 0;
  
  if (root->getType() == N2D_LEAF) {
    return 1;
  }
  
  int count = 0;
  IntNode2D *internal = static_cast<IntNode2D*>(root);
  for (Node2D *child : internal->getChildren()) {
    count += count_2d_leaves(child);
  }
  
  return count;
}

/**
 *  Computes height of the 2D tree.
 */
int height_2d_tree(Node2D *root) {
  if (!root) return 0;
  
  if (root->getType() == N2D_LEAF) {
    return 1;
  }
  
  int max_height = 0;
  IntNode2D *internal = static_cast<IntNode2D*>(root);
  for (Node2D *child : internal->getChildren()) {
    max_height = std::max(max_height, height_2d_tree(child));
  }
  
  return max_height + 1;
}

/**
 *  Prints statistics about the 2D tree.
 */
void print_2d_tree_stats(Node2D *root) {
  if (!root) {
    std::cout << "Tree is empty" << std::endl;
    return;
  }
  
  std::cout << "2D Tree Statistics:" << std::endl;
  std::cout << "  Height: " << height_2d_tree(root) << std::endl;
  std::cout << "  Leaves: " << count_2d_leaves(root) << std::endl;
  
  Rectangle mbr = root->getRect();
  std::cout << "  MBR: (" << mbr.lx << ", " << mbr.ly << ") to ("
            << mbr.ux << ", " << mbr.uy << ")" << std::endl;
}
