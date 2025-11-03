/**
 *  @file Test2DIndex.cpp
 *  @author Modified for 2D Range Query System
 *  
 *  Test program for 2D tree construction performance
 */

#include "Point2D.hpp"
#include "Node2D.hpp"
#include <iostream>
#include <chrono>
#include <iomanip>

using namespace std::chrono;

void print_usage(const char* program_name) {
  std::cout << "Usage: " << program_name << " <data_file> <capacity>" << std::endl;
  std::cout << "  data_file: CSV file with format ID,Year,Month,Day,Time,x,y" << std::endl;
  std::cout << "  capacity: Maximum number of points per leaf node" << std::endl;
}

int main(int argc, char const *argv[]) {
  if (argc < 3) {
    print_usage(argv[0]);
    return 1;
  }
  
  std::string data_file = argv[1];
  size_t capacity = std::stoul(argv[2]);
  
  std::cout << "=== 2D Tree Construction Test ===" << std::endl;
  std::cout << "Data file: " << data_file << std::endl;
  std::cout << "Capacity: " << capacity << std::endl << std::endl;
  
  // Load data points
  std::cout << "Loading data points..." << std::endl;
  auto load_start = high_resolution_clock::now();
  std::vector<Point2D> points = load_points_file(data_file);
  auto load_end = high_resolution_clock::now();
  
  if (points.empty()) {
    std::cerr << "Error: No points loaded from data file" << std::endl;
    return 1;
  }
  
  auto load_time = duration_cast<microseconds>(load_end - load_start);
  std::cout << "Loaded " << points.size() << " points in " 
            << load_time.count() << " μs" << std::endl;
  
  // Compute data statistics
  Rectangle data_mbr = compute_mbr(points);
  std::cout << "Data MBR: (" << data_mbr.lx << ", " << data_mbr.ly 
            << ") to (" << data_mbr.ux << ", " << data_mbr.uy << ")" << std::endl;
  
  int32_t width = data_mbr.ux - data_mbr.lx;
  int32_t height = data_mbr.uy - data_mbr.ly;
  std::cout << "Data dimensions: " << width << " x " << height << std::endl << std::endl;
  
  // Build 2D MR-tree
  std::cout << "Building 2D MR-tree..." << std::endl;
  auto build_start = high_resolution_clock::now();
  Node2D *root = build_2d_tree(points, capacity);
  auto build_end = high_resolution_clock::now();
  
  if (!root) {
    std::cerr << "Error: Failed to build tree" << std::endl;
    return 1;
  }
  
  auto build_time = duration_cast<microseconds>(build_end - build_start);
  
  // Print construction results
  std::cout << std::endl << "=== Construction Results ===" << std::endl;
  std::cout << "Construction time: " << build_time.count() << " μs" << std::endl;
  std::cout << "Construction time: " << std::fixed << std::setprecision(2)
            << (double)build_time.count() / 1000.0 << " ms" << std::endl;
  
  print_2d_tree_stats(root);
  
  // Calculate additional statistics
  int leaves = count_2d_leaves(root);
  int height = height_2d_tree(root);
  double avg_points_per_leaf = (double)points.size() / leaves;
  double tree_utilization = avg_points_per_leaf / capacity;
  
  std::cout << std::endl << "=== Additional Statistics ===" << std::endl;
  std::cout << "Average points per leaf: " << std::fixed << std::setprecision(2)
            << avg_points_per_leaf << std::endl;
  std::cout << "Tree utilization: " << std::fixed << std::setprecision(2)
            << tree_utilization * 100 << "%" << std::endl;
  std::cout << "Points per microsecond: " << std::fixed << std::setprecision(2)
            << (double)points.size() / build_time.count() << std::endl;
  
  // Memory usage estimation
  size_t estimated_memory = 0;
  estimated_memory += points.size() * sizeof(Point2D);  // Points in leaves
  estimated_memory += leaves * sizeof(LeafNode2D);      // Leaf nodes
  // Rough estimate for internal nodes (depends on tree structure)
  estimated_memory += (leaves / capacity + 1) * sizeof(IntNode2D) * height;
  
  std::cout << "Estimated memory usage: " << std::fixed << std::setprecision(2)
            << (double)estimated_memory / (1024 * 1024) << " MB" << std::endl;
  
  // Test a simple query to verify tree correctness
  std::cout << std::endl << "=== Correctness Test ===" << std::endl;
  
  // Create a small query rectangle in the center of the data
  int32_t center_x = (data_mbr.lx + data_mbr.ux) / 2;
  int32_t center_y = (data_mbr.ly + data_mbr.uy) / 2;
  int32_t query_size = std::min(width, height) / 10;  // 10% of smaller dimension
  
  Rectangle test_query = {
    center_x - query_size/2,
    center_y - query_size/2,
    center_x + query_size/2,
    center_y + query_size/2
  };
  
  std::cout << "Test query: (" << test_query.lx << ", " << test_query.ly 
            << ") to (" << test_query.ux << ", " << test_query.uy << ")" << std::endl;
  
  // Count points using brute force
  size_t brute_force_count = count_in_range(points, test_query);
  std::cout << "Brute force result: " << brute_force_count << " points" << std::endl;
  
  // Count points using tree query (without full verification for speed)
  auto query_start = high_resolution_clock::now();
  VObject2D *vo = range_query_2d(root, test_query);
  size_t tree_count = count_points_2d(vo);
  auto query_end = high_resolution_clock::now();
  
  auto query_time = duration_cast<microseconds>(query_end - query_start);
  std::cout << "Tree query result: " << tree_count << " points" << std::endl;
  std::cout << "Query time: " << query_time.count() << " μs" << std::endl;
  
  if (brute_force_count == tree_count) {
    std::cout << "✓ Correctness test PASSED" << std::endl;
  } else {
    std::cout << "✗ Correctness test FAILED" << std::endl;
  }
  
  // Clean up
  delete_vo_2d(vo);
  delete_2d_tree(root);
  
  std::cout << std::endl << "Tree construction test completed!" << std::endl;
  return 0;
}
