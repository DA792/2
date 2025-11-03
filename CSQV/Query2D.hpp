/**
 *  @file Query2D.hpp
 *  @author Modified for 2D Range Query System
 *  
 *  Optimized 2D range query and verification system
 */

#ifndef QUERY2D_H
#define QUERY2D_H

#include "Node2D.hpp"
#include "Point2D.hpp"

/**
 *  Types of verification objects for 2D range queries.
 */
enum VObject2DType {V2D_LEAF, V2D_PRUNED, V2D_CONTAINER};

/**
 *  Base class for 2D verification objects.
 */
class VObject2D {
protected:
  VObject2DType type;
  
public:
  VObject2D(VObject2DType t) : type(t) {}
  virtual ~VObject2D() {}
  VObject2DType getType() const { return type; }
};

/**
 *  Verification object for 2D leaf nodes.
 */
class VLeaf2D : public VObject2D {
private:
  std::vector<Point2D> points;
  
public:
  VLeaf2D(const std::vector<Point2D> &points) 
  : VObject2D(V2D_LEAF), points(points) {}
  
  const std::vector<Point2D> &getPoints() const { return points; }
  size_t getSize() const { return points.size(); }
};

/**
 *  Verification object for pruned 2D internal nodes.
 */
class VPruned2D : public VObject2D {
private:
  Rectangle rect;
  hash_t hash;
  
public:
  VPruned2D(Rectangle r, hash_t h) 
  : VObject2D(V2D_PRUNED), rect(r), hash(h) {}
  
  Rectangle getRect() const { return rect; }
  hash_t getHash() const { return hash; }
};

/**
 *  Verification object for explored 2D internal nodes.
 */
class VContainer2D : public VObject2D {
private:
  std::vector<VObject2D*> children;
  
public:
  VContainer2D() : VObject2D(V2D_CONTAINER) {}
  
  void append(VObject2D *vo) { 
    if (vo) children.push_back(vo); 
  }
  
  VObject2D *get(size_t i) const { 
    return children.at(i); 
  }
  
  size_t size() const { 
    return children.size(); 
  }
  
  const std::vector<VObject2D*> &getChildren() const {
    return children;
  }
};

/**
 *  Result of 2D range query verification.
 */
class VResult2D {
private:
  Rectangle rect;           ///< Reconstructed MBR
  hash_t hash;             ///< Reconstructed hash
  std::vector<Point2D> points; ///< Query result points
  
public:
  VResult2D(Rectangle r, hash_t h, std::vector<Point2D> points) 
  : rect(r), hash(h), points(std::move(points)) {}
  
  Rectangle getRect() const { return rect; }
  hash_t getHash() const { return hash; }
  const std::vector<Point2D> &getPoints() const { return points; }
  size_t count() const { return points.size(); }
};

/**
 *  Query statistics for performance analysis.
 */
struct QueryStats2D {
  size_t nodes_visited;      ///< Number of nodes visited during query
  size_t nodes_pruned;       ///< Number of nodes pruned
  size_t points_examined;    ///< Total points examined
  size_t points_returned;    ///< Points that match the query
  double query_time_us;      ///< Query execution time in microseconds
  double verify_time_us;     ///< Verification time in microseconds
  
  QueryStats2D() : nodes_visited(0), nodes_pruned(0), points_examined(0), 
                   points_returned(0), query_time_us(0.0), verify_time_us(0.0) {}
};

/**
 *  Counts the number of points in a verification object.
 *  @param vo a 2D verification object
 *  @return the number of points
 */
size_t count_points_2d(VObject2D *vo);

/**
 *  Performs a 2D range query on the MR-tree.
 *  @param root the root of the 2D MR-tree
 *  @param query the query rectangle
 *  @param stats optional statistics collector
 *  @return verification object for the query
 */
VObject2D *range_query_2d(Node2D *root, const Rectangle &query, 
                          QueryStats2D *stats = nullptr);

/**
 *  Verifies a 2D range query result and reconstructs the tree root.
 *  @param vo verification object from the query
 *  @param query the original query rectangle
 *  @param stats optional statistics collector
 *  @return verification result with reconstructed information
 */
VResult2D *verify_2d(VObject2D *vo, const Rectangle &query,
                     QueryStats2D *stats = nullptr);

/**
 *  Performs a complete 2D range query with verification.
 *  This is a convenience function that combines query and verification.
 *  @param root the root of the 2D MR-tree
 *  @param query the query rectangle
 *  @param stats optional statistics collector
 *  @return verification result
 */
VResult2D *query_and_verify_2d(Node2D *root, const Rectangle &query,
                               QueryStats2D *stats = nullptr);

/**
 *  Frees memory used by a verification object.
 *  @param vo verification object to delete
 */
void delete_vo_2d(VObject2D *vo);

/**
 *  Prints query statistics.
 *  @param stats the statistics to print
 */
void print_query_stats_2d(const QueryStats2D &stats);

/**
 *  Loads query rectangles from a CSV file.
 *  Expected format: lx,ly,ux,uy,matching,fraction
 *  @param path path to the query file
 *  @return vector of query rectangles
 */
std::vector<Rectangle> load_queries_2d(const std::string &path);

/**
 *  Generates random query rectangles within a given MBR.
 *  @param mbr the bounding rectangle for generating queries
 *  @param num_queries number of queries to generate
 *  @param min_size minimum query size (as fraction of MBR)
 *  @param max_size maximum query size (as fraction of MBR)
 *  @return vector of random query rectangles
 */
std::vector<Rectangle> generate_random_queries_2d(const Rectangle &mbr,
                                                  size_t num_queries,
                                                  double min_size = 0.01,
                                                  double max_size = 0.1);

#endif
