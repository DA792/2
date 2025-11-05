/**
 *  @file QueryGenMultiple.cpp
 *  @author Modified for 2D Range Query System
 *  
 *  Generates multiple sets of 2D range queries with different selectivity levels
 */

#include "Point2D.hpp"
#include "Query2D.hpp"
#include <iostream>
#include <fstream>
#include <iomanip>
#include <cmath>
#include <random>
#include <sstream>

void print_usage(const char* program_name) {
  std::cout << "Usage: " << program_name << " <data_file> <output_prefix> <num_queries_per_level>" << std::endl;
  std::cout << "  data_file: CSV file with 2D points" << std::endl;
  std::cout << "  output_prefix: Prefix for output CSV files" << std::endl;
  std::cout << "  num_queries_per_level: Number of queries to generate for each selectivity level" << std::endl;
  std::cout << std::endl;
  std::cout << "Generates query sets with selectivity levels: 0.0001, 0.001, 0.01, 0.1" << std::endl;
  std::cout << "Output files: <prefix>_sel_0.0001.csv, <prefix>_sel_0.001.csv, etc." << std::endl;
}

/**
 *  Calculates the area-based selectivity of a query rectangle
 */
double calculate_area_selectivity(const Rectangle &query, const Rectangle &data_mbr) {
  int64_t query_area = (int64_t)(query.ux - query.lx) * (query.uy - query.ly);
  int64_t data_area = (int64_t)(data_mbr.ux - data_mbr.lx) * (data_mbr.uy - data_mbr.ly);
  return (double)query_area / data_area;
}

/**
 *  Generates queries for a specific selectivity level
 */
std::vector<Rectangle> generate_queries_by_selectivity(const Rectangle &data_mbr,
                                                      size_t num_queries,
                                                      double target_selectivity) {
  std::vector<Rectangle> queries;
  queries.reserve(num_queries);
  
  std::random_device rd;
  std::mt19937 gen(rd());
  
  int32_t data_width = data_mbr.ux - data_mbr.lx;
  int32_t data_height = data_mbr.uy - data_mbr.ly;
  
  // Calculate target query dimensions based on selectivity
  // selectivity = (query_width * query_height) / (data_width * data_height)
  // Assuming square queries for simplicity: query_width = query_height
  double side_ratio = std::sqrt(target_selectivity);
  int32_t target_width = static_cast<int32_t>(data_width * side_ratio);
  int32_t target_height = static_cast<int32_t>(data_height * side_ratio);
  
  // Add some variation (±20%) to avoid all queries being exactly the same size
  std::uniform_real_distribution<double> size_variation(0.8, 1.2);
  
  // Distributions for random positioning
  std::uniform_int_distribution<int32_t> x_dist(data_mbr.lx, 
                                               std::max(data_mbr.lx, data_mbr.ux - target_width));
  std::uniform_int_distribution<int32_t> y_dist(data_mbr.ly, 
                                               std::max(data_mbr.ly, data_mbr.uy - target_height));
  
  for (size_t i = 0; i < num_queries; i++) {
    // Apply size variation
    double variation = size_variation(gen);
    int32_t query_width = static_cast<int32_t>(target_width * variation);
    int32_t query_height = static_cast<int32_t>(target_height * variation);
    
    // Ensure minimum size
    query_width = std::max(query_width, 1);
    query_height = std::max(query_height, 1);
    
    // Generate random position
    int32_t max_x = std::max(data_mbr.lx, data_mbr.ux - query_width);
    int32_t max_y = std::max(data_mbr.ly, data_mbr.uy - query_height);
    
    std::uniform_int_distribution<int32_t> pos_x_dist(data_mbr.lx, max_x);
    std::uniform_int_distribution<int32_t> pos_y_dist(data_mbr.ly, max_y);
    
    int32_t lx = pos_x_dist(gen);
    int32_t ly = pos_y_dist(gen);
    int32_t ux = std::min(data_mbr.ux, lx + query_width);
    int32_t uy = std::min(data_mbr.uy, ly + query_height);
    
    queries.push_back({lx, ly, ux, uy});
  }
  
  return queries;
}

/**
 *  Writes queries to CSV file with statistics
 */
void write_query_file(const std::string &filename,
                     const std::vector<Rectangle> &queries,
                     const std::vector<Point2D> &points,
                     const Rectangle &data_mbr,
                     double target_selectivity) {
  std::ofstream out(filename);
  if (!out.is_open()) {
    std::cerr << "Error: Cannot open output file " << filename << std::endl;
    return;
  }
  
  // Write header
  out << "lx,ly,ux,uy,matching,point_fraction,area_selectivity" << std::endl;
  
  size_t total_matching = 0;
  double total_area_selectivity = 0.0;
  
  for (const Rectangle &query : queries) {
    // Count matching points
    size_t matching = count_in_range(points, query);
    double point_fraction = (double)matching / points.size();
    
    // Calculate area-based selectivity
    double area_selectivity = calculate_area_selectivity(query, data_mbr);
    
    total_matching += matching;
    total_area_selectivity += area_selectivity;
    
    // Write to file
    out << query.lx << "," << query.ly << "," << query.ux << "," << query.uy 
        << "," << matching << "," << std::fixed << std::setprecision(6) << point_fraction
        << "," << std::fixed << std::setprecision(6) << area_selectivity << std::endl;
  }
  
  out.close();
  
  // Print statistics
  double avg_matching = (double)total_matching / queries.size();
  double avg_point_fraction = avg_matching / points.size();
  double avg_area_selectivity = total_area_selectivity / queries.size();
  
  std::cout << "  File: " << filename << std::endl;
  std::cout << "  Target area selectivity: " << std::fixed << std::setprecision(4) << target_selectivity << std::endl;
  std::cout << "  Actual avg area selectivity: " << std::fixed << std::setprecision(4) << avg_area_selectivity << std::endl;
  std::cout << "  Avg matching points: " << std::fixed << std::setprecision(2) << avg_matching << std::endl;
  std::cout << "  Avg point fraction: " << std::fixed << std::setprecision(4) << avg_point_fraction * 100 << "%" << std::endl;
}

int main(int argc, char const *argv[]) {
  if (argc < 4) {
    print_usage(argv[0]);
    return 1;
  }
  
  std::string data_file = argv[1];
  std::string output_prefix = argv[2];
  size_t num_queries_per_level = std::stoul(argv[3]);
  
  // Define selectivity levels
  std::vector<double> selectivity_levels = {0.0001, 0.001, 0.01, 0.1};
  
  std::cout << "=== Multiple Selectivity Query Generator ===" << std::endl;
  std::cout << "Data file: " << data_file << std::endl;
  std::cout << "Output prefix: " << output_prefix << std::endl;
  std::cout << "Queries per level: " << num_queries_per_level << std::endl;
  std::cout << "Selectivity levels: ";
  for (size_t i = 0; i < selectivity_levels.size(); i++) {
    std::cout << selectivity_levels[i];
    if (i < selectivity_levels.size() - 1) std::cout << ", ";
  }
  std::cout << std::endl << std::endl;
  
  // Load data points
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
  
  int64_t data_area = (int64_t)(data_mbr.ux - data_mbr.lx) * (data_mbr.uy - data_mbr.ly);
  std::cout << "Data area: " << data_area << std::endl << std::endl;
  
  // Generate queries for each selectivity level
  for (double selectivity : selectivity_levels) {
    std::cout << "Generating queries for selectivity " << selectivity << "..." << std::endl;
    
    // Generate queries
    std::vector<Rectangle> queries = generate_queries_by_selectivity(data_mbr, 
                                                                    num_queries_per_level, 
                                                                    selectivity);
    
    // Create output filename
    std::ostringstream filename_stream;
    filename_stream << output_prefix << "_sel_" << std::fixed << std::setprecision(4) << selectivity << ".csv";
    std::string filename = filename_stream.str();
    
    // Write to file
    write_query_file(filename, queries, points, data_mbr, selectivity);
    std::cout << std::endl;
  }
  
  std::cout << "Query generation completed successfully!" << std::endl;
  std::cout << "Generated " << selectivity_levels.size() << " query sets with " 
            << num_queries_per_level << " queries each." << std::endl;
  
  return 0;
}
