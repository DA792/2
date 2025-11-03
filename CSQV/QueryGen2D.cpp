/**
 *  @file QueryGen2D.cpp
 *  @author Modified for 2D Range Query System
 *  
 *  Generates random 2D range queries for testing
 */

#include "Point2D.hpp"
#include "Query2D.hpp"
#include <iostream>
#include <fstream>
#include <iomanip>

void print_usage(const char* program_name) {
  std::cout << "Usage: " << program_name << " <data_file> <query_file> <num_queries> [min_size] [max_size]" << std::endl;
  std::cout << "  data_file: CSV file with 2D points" << std::endl;
  std::cout << "  query_file: Output CSV file for generated queries" << std::endl;
  std::cout << "  num_queries: Number of queries to generate" << std::endl;
  std::cout << "  min_size: Minimum query size as fraction of data MBR (default: 0.01)" << std::endl;
  std::cout << "  max_size: Maximum query size as fraction of data MBR (default: 0.1)" << std::endl;
}

int main(int argc, char const *argv[]) {
  if (argc < 4) {
    print_usage(argv[0]);
    return 1;
  }
  
  std::string data_file = argv[1];
  std::string query_file = argv[2];
  size_t num_queries = std::stoul(argv[3]);
  
  double min_size = (argc > 4) ? std::stod(argv[4]) : 0.01;
  double max_size = (argc > 5) ? std::stod(argv[5]) : 0.1;
  
  std::cout << "=== 2D Query Generator ===" << std::endl;
  std::cout << "Data file: " << data_file << std::endl;
  std::cout << "Query file: " << query_file << std::endl;
  std::cout << "Number of queries: " << num_queries << std::endl;
  std::cout << "Query size range: " << min_size << " - " << max_size << std::endl << std::endl;
  
  // Load data points to compute MBR
  std::cout << "Loading data points..." << std::endl;
  std::vector<Point2D> points = load_points_file(data_file);
  
  if (points.empty()) {
    std::cerr << "Error: No points loaded from data file" << std::endl;
    return 1;
  }
  
  // Compute MBR of the dataset
  Rectangle data_mbr = compute_mbr(points);
  std::cout << "Data MBR: (" << data_mbr.lx << ", " << data_mbr.ly 
            << ") to (" << data_mbr.ux << ", " << data_mbr.uy << ")" << std::endl;
  
  // Generate random queries
  std::cout << "Generating " << num_queries << " random queries..." << std::endl;
  std::vector<Rectangle> queries = generate_random_queries_2d(data_mbr, num_queries, 
                                                              min_size, max_size);
  
  // Open output file
  std::ofstream out(query_file);
  if (!out.is_open()) {
    std::cerr << "Error: Cannot open output file " << query_file << std::endl;
    return 1;
  }
  
  // Write header
  out << "lx,ly,ux,uy,matching,fraction" << std::endl;
  
  // Process each query and compute statistics
  std::cout << "Computing query statistics..." << std::endl;
  
  for (size_t i = 0; i < queries.size(); i++) {
    const Rectangle &query = queries[i];
    
    // Count matching points
    size_t matching = count_in_range(points, query);
    double fraction = (double)matching / points.size();
    
    // Write to file
    out << query.lx << "," << query.ly << "," << query.ux << "," << query.uy 
        << "," << matching << "," << std::fixed << std::setprecision(6) << fraction << std::endl;
    
    // Print progress
    if ((i + 1) % 1000 == 0 || i == queries.size() - 1) {
      std::cout << "Processed " << (i + 1) << "/" << queries.size() 
                << " queries" << std::endl;
    }
  }
  
  out.close();
  
  // Print summary statistics
  std::cout << std::endl << "=== Generation Summary ===" << std::endl;
  std::cout << "Generated " << queries.size() << " queries" << std::endl;
  std::cout << "Output written to: " << query_file << std::endl;
  
  // Compute some basic statistics
  size_t total_matching = 0;
  size_t min_matching = SIZE_MAX;
  size_t max_matching = 0;
  
  for (const Rectangle &query : queries) {
    size_t matching = count_in_range(points, query);
    total_matching += matching;
    min_matching = std::min(min_matching, matching);
    max_matching = std::max(max_matching, matching);
  }
  
  double avg_matching = (double)total_matching / queries.size();
  double avg_fraction = avg_matching / points.size();
  
  std::cout << "Average matching points: " << std::fixed << std::setprecision(2) 
            << avg_matching << std::endl;
  std::cout << "Average selectivity: " << std::fixed << std::setprecision(4) 
            << avg_fraction * 100 << "%" << std::endl;
  std::cout << "Min matching points: " << min_matching << std::endl;
  std::cout << "Max matching points: " << max_matching << std::endl;
  
  std::cout << std::endl << "Query generation completed successfully!" << std::endl;
  return 0;
}
