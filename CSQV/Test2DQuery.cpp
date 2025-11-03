/**
 *  @file Test2DQuery.cpp
 *  @author Modified for 2D Range Query System
 *  
 *  Test program for 2D range queries with verification
 */

#include "Point2D.hpp"
#include "Node2D.hpp"
#include "Query2D.hpp"
#include <iostream>
#include <chrono>
#include <iomanip>

using namespace std::chrono;

void print_usage(const char* program_name) {
  std::cout << "Usage: " << program_name << " <data_file> <query_file> <capacity>" << std::endl;
  std::cout << "  data_file: CSV file with format ID,Year,Month,Day,Time,x,y" << std::endl;
  std::cout << "  query_file: CSV file with format lx,ly,ux,uy,matching,fraction" << std::endl;
  std::cout << "  capacity: Maximum number of points per leaf node" << std::endl;
}

int main(int argc, char const *argv[]) {
  if (argc < 4) {
    print_usage(argv[0]);
    return 1;
  }
  
  std::string data_file = argv[1];
  std::string query_file = argv[2];
  size_t capacity = std::stoul(argv[3]);
  
  std::cout << "=== 2D Range Query System Test ===" << std::endl;
  std::cout << "Data file: " << data_file << std::endl;
  std::cout << "Query file: " << query_file << std::endl;
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
  
  std::cout << "Loaded " << points.size() << " points in " 
            << duration_cast<milliseconds>(load_end - load_start).count() 
            << " ms" << std::endl << std::endl;
  
  // Build 2D MR-tree
  std::cout << "Building 2D MR-tree..." << std::endl;
  auto build_start = high_resolution_clock::now();
  Node2D *root = build_2d_tree(points, capacity);
  auto build_end = high_resolution_clock::now();
  
  if (!root) {
    std::cerr << "Error: Failed to build tree" << std::endl;
    return 1;
  }
  
  std::cout << "Tree built in " 
            << duration_cast<milliseconds>(build_end - build_start).count() 
            << " ms" << std::endl;
  print_2d_tree_stats(root);
  std::cout << std::endl;
  
  // Load queries
  std::cout << "Loading queries..." << std::endl;
  std::vector<Rectangle> queries = load_queries_2d(query_file);
  
  if (queries.empty()) {
    std::cerr << "Error: No queries loaded" << std::endl;
    delete_2d_tree(root);
    return 1;
  }
  
  std::cout << std::endl;
  
  // Execute queries
  std::cout << "Executing queries..." << std::endl;
  
  QueryStats2D total_stats;
  size_t total_points_returned = 0;
  
  for (size_t i = 0; i < queries.size(); i++) {
    QueryStats2D query_stats;
    
    VResult2D *result = query_and_verify_2d(root, queries[i], &query_stats);
    
    if (result) {
      total_points_returned += result->count();
      total_stats.nodes_visited += query_stats.nodes_visited;
      total_stats.nodes_pruned += query_stats.nodes_pruned;
      total_stats.points_examined += query_stats.points_examined;
      total_stats.points_returned += query_stats.points_returned;
      total_stats.query_time_us += query_stats.query_time_us;
      total_stats.verify_time_us += query_stats.verify_time_us;
      
      delete result;
    }
    
    // Print progress every 100 queries
    if ((i + 1) % 100 == 0 || i == queries.size() - 1) {
      std::cout << "Processed " << (i + 1) << "/" << queries.size() 
                << " queries" << std::endl;
    }
  }
  
  // Print summary statistics
  std::cout << std::endl << "=== Summary Statistics ===" << std::endl;
  std::cout << "Number of queries: " << queries.size() << std::endl;
  std::cout << "Average nodes visited: " << std::fixed << std::setprecision(2)
            << (double)total_stats.nodes_visited / queries.size() << std::endl;
  std::cout << "Average nodes pruned: " << std::fixed << std::setprecision(2)
            << (double)total_stats.nodes_pruned / queries.size() << std::endl;
  std::cout << "Average points examined: " << std::fixed << std::setprecision(2)
            << (double)total_stats.points_examined / queries.size() << std::endl;
  std::cout << "Average points returned: " << std::fixed << std::setprecision(2)
            << (double)total_stats.points_returned / queries.size() << std::endl;
  std::cout << "Average query time: " << std::fixed << std::setprecision(2)
            << total_stats.query_time_us / queries.size() << " μs" << std::endl;
  std::cout << "Average verification time: " << std::fixed << std::setprecision(2)
            << total_stats.verify_time_us / queries.size() << " μs" << std::endl;
  std::cout << "Average total time: " << std::fixed << std::setprecision(2)
            << (total_stats.query_time_us + total_stats.verify_time_us) / queries.size() 
            << " μs" << std::endl;
  
  // Calculate pruning efficiency
  double pruning_ratio = (double)total_stats.nodes_pruned / 
                        (total_stats.nodes_visited + total_stats.nodes_pruned);
  std::cout << "Pruning efficiency: " << std::fixed << std::setprecision(2)
            << pruning_ratio * 100 << "%" << std::endl;
  
  // Clean up
  delete_2d_tree(root);
  
  std::cout << std::endl << "Test completed successfully!" << std::endl;
  return 0;
}
