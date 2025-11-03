/**
 *  @file Query2D.cpp
 *  @author Modified for 2D Range Query System
 */

#include "Query2D.hpp"
#include "csv.hpp"
#include <chrono>
#include <iostream>
#include <fstream>
#include <random>

using namespace std::chrono;

/**
 *  Counts the number of points in a verification object.
 */
size_t count_points_2d(VObject2D *vo) {
  if (!vo) return 0;
  
  switch (vo->getType()) {
    case V2D_LEAF:
      return static_cast<VLeaf2D*>(vo)->getSize();
      
    case V2D_PRUNED:
      return 0; // Pruned nodes don't contribute points
      
    case V2D_CONTAINER: {
      size_t total = 0;
      VContainer2D *container = static_cast<VContainer2D*>(vo);
      for (size_t i = 0; i < container->size(); i++) {
        total += count_points_2d(container->get(i));
      }
      return total;
    }
  }
  
  return 0;
}

/**
 *  Performs a 2D range query on the MR-tree.
 */
VObject2D *range_query_2d(Node2D *root, const Rectangle &query, 
                          QueryStats2D *stats) {
  if (!root) return nullptr;
  
  if (stats) stats->nodes_visited++;
  
  // If this is a leaf node, return all its points
  if (root->getType() == N2D_LEAF) {
    LeafNode2D *leaf = static_cast<LeafNode2D*>(root);
    if (stats) stats->points_examined += leaf->size();
    return new VLeaf2D(leaf->getPoints());
  }
  
  // For internal nodes, check if MBR intersects with query
  Rectangle node_rect = root->getRect();
  if (!intersect(node_rect, query)) {
    // No intersection - prune this subtree
    if (stats) stats->nodes_pruned++;
    return new VPruned2D(node_rect, root->getHash());
  }
  
  // Intersection found - explore children
  VContainer2D *container = new VContainer2D();
  IntNode2D *internal = static_cast<IntNode2D*>(root);
  
  for (Node2D *child : internal->getChildren()) {
    VObject2D *child_vo = range_query_2d(child, query, stats);
    container->append(child_vo);
  }
  
  return container;
}

/**
 *  Verifies a 2D range query result.
 */
VResult2D *verify_2d(VObject2D *vo, const Rectangle &query,
                     QueryStats2D *stats) {
  if (!vo) return nullptr;
  
  switch (vo->getType()) {
    case V2D_LEAF: {
      // Reconstruct leaf node
      VLeaf2D *leaf = static_cast<VLeaf2D*>(vo);
      const std::vector<Point2D> &all_points = leaf->getPoints();
      
      // Filter points that match the query
      std::vector<Point2D> matching_points;
      Rectangle leaf_mbr = EMPTY_RECT;
      Buffer buf(all_points.size() * (sizeof(uint32_t) + 2 * sizeof(int32_t)));
      
      for (const Point2D &p : all_points) {
        leaf_mbr = enlarge(leaf_mbr, p.loc);
        put_point2d(buf, p);
        
        if (contains(p, query)) {
          matching_points.push_back(p);
          if (stats) stats->points_returned++;
        }
      }
      
      hash_t leaf_hash = sha256(buf);
      return new VResult2D(leaf_mbr, leaf_hash, std::move(matching_points));
    }
    
    case V2D_PRUNED: {
      // Use provided MBR and hash for pruned nodes
      VPruned2D *pruned = static_cast<VPruned2D*>(vo);
      return new VResult2D(pruned->getRect(), pruned->getHash(), 
                          std::vector<Point2D>());
    }
    
    case V2D_CONTAINER: {
      // Reconstruct internal node
      VContainer2D *container = static_cast<VContainer2D*>(vo);
      std::vector<Point2D> all_matching_points;
      Rectangle combined_mbr = EMPTY_RECT;
      Buffer buf(container->size() * (4*sizeof(int32_t) + SHA256_DIGEST_LENGTH));
      
      for (size_t i = 0; i < container->size(); i++) {
        VResult2D *child_result = verify_2d(container->get(i), query, stats);
        
        // Collect matching points
        const std::vector<Point2D> &child_points = child_result->getPoints();
        all_matching_points.insert(all_matching_points.end(),
                                  child_points.begin(), child_points.end());
        
        // Update combined MBR and hash buffer
        Rectangle child_rect = child_result->getRect();
        hash_t child_hash = child_result->getHash();
        
        combined_mbr = enlarge(combined_mbr, child_rect);
        buf.put(child_rect.lx).put(child_rect.ly)
           .put(child_rect.ux).put(child_rect.uy)
           .put_bytes(child_hash.data(), child_hash.size());
        
        delete child_result;
      }
      
      hash_t combined_hash = sha256(buf);
      return new VResult2D(combined_mbr, combined_hash, 
                          std::move(all_matching_points));
    }
  }
  
  return nullptr;
}

/**
 *  Performs complete 2D range query with verification.
 */
VResult2D *query_and_verify_2d(Node2D *root, const Rectangle &query,
                               QueryStats2D *stats) {
  if (stats) {
    stats->nodes_visited = 0;
    stats->nodes_pruned = 0;
    stats->points_examined = 0;
    stats->points_returned = 0;
  }
  
  // Perform query
  auto query_start = high_resolution_clock::now();
  VObject2D *vo = range_query_2d(root, query, stats);
  auto query_end = high_resolution_clock::now();
  
  if (stats) {
    stats->query_time_us = duration_cast<microseconds>(query_end - query_start).count();
  }
  
  // Perform verification
  auto verify_start = high_resolution_clock::now();
  VResult2D *result = verify_2d(vo, query, stats);
  auto verify_end = high_resolution_clock::now();
  
  if (stats) {
    stats->verify_time_us = duration_cast<microseconds>(verify_end - verify_start).count();
  }
  
  // Clean up verification object
  delete_vo_2d(vo);
  
  return result;
}

/**
 *  Frees memory used by a verification object.
 */
void delete_vo_2d(VObject2D *vo) {
  if (!vo) return;
  
  if (vo->getType() == V2D_CONTAINER) {
    VContainer2D *container = static_cast<VContainer2D*>(vo);
    for (size_t i = 0; i < container->size(); i++) {
      delete_vo_2d(container->get(i));
    }
  }
  
  delete vo;
}

/**
 *  Prints query statistics.
 */
void print_query_stats_2d(const QueryStats2D &stats) {
  std::cout << "Query Statistics:" << std::endl;
  std::cout << "  Nodes visited: " << stats.nodes_visited << std::endl;
  std::cout << "  Nodes pruned: " << stats.nodes_pruned << std::endl;
  std::cout << "  Points examined: " << stats.points_examined << std::endl;
  std::cout << "  Points returned: " << stats.points_returned << std::endl;
  std::cout << "  Query time: " << stats.query_time_us << " μs" << std::endl;
  std::cout << "  Verification time: " << stats.verify_time_us << " μs" << std::endl;
  std::cout << "  Total time: " << (stats.query_time_us + stats.verify_time_us) << " μs" << std::endl;
}

/**
 *  Loads query rectangles from CSV file.
 */
std::vector<Rectangle> load_queries_2d(const std::string &path) {
  std::vector<Rectangle> queries;
  
  try {
    csv::CSVReader reader(path);
    
    for (csv::CSVRow& row : reader) {
      int32_t lx = row[0].get<int32_t>();
      int32_t ly = row[1].get<int32_t>();
      int32_t ux = row[2].get<int32_t>();
      int32_t uy = row[3].get<int32_t>();
      
      queries.push_back({lx, ly, ux, uy});
    }
    
    std::cout << "Loaded " << queries.size() << " queries from " << path << std::endl;
    
  } catch (const std::exception& e) {
    std::cerr << "Error loading queries: " << e.what() << std::endl;
  }
  
  return queries;
}

/**
 *  Generates random query rectangles.
 */
std::vector<Rectangle> generate_random_queries_2d(const Rectangle &mbr,
                                                  size_t num_queries,
                                                  double min_size,
                                                  double max_size) {
  std::vector<Rectangle> queries;
  queries.reserve(num_queries);
  
  std::random_device rd;
  std::mt19937 gen(rd());
  
  int32_t width = mbr.ux - mbr.lx;
  int32_t height = mbr.uy - mbr.ly;
  
  std::uniform_int_distribution<int32_t> x_dist(mbr.lx, mbr.ux);
  std::uniform_int_distribution<int32_t> y_dist(mbr.ly, mbr.uy);
  std::uniform_real_distribution<double> size_dist(min_size, max_size);
  
  for (size_t i = 0; i < num_queries; i++) {
    // Generate random lower-left corner
    int32_t lx = x_dist(gen);
    int32_t ly = y_dist(gen);
    
    // Generate random size
    double size_factor = size_dist(gen);
    int32_t query_width = static_cast<int32_t>(width * size_factor);
    int32_t query_height = static_cast<int32_t>(height * size_factor);
    
    // Ensure query stays within MBR
    int32_t ux = std::min(mbr.ux, lx + query_width);
    int32_t uy = std::min(mbr.uy, ly + query_height);
    
    queries.push_back({lx, ly, ux, uy});
  }
  
  return queries;
}
