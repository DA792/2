/**
 *  @file Point2D.cpp
 *  @author Modified for 2D Range Query System
 */

#include "Point2D.hpp"
#include "csv.hpp"
#include <iostream>
#include <fstream>

#ifdef Z_INDEX
#include "libmorton/morton.h"
#endif

/**
 *  Morton encoding function for 2D coordinates.
 */
#ifdef Z_INDEX
uint_fast64_t morton2D_encode(int32_t x, int32_t y) {
  // Convert to unsigned for Morton encoding
  uint32_t ux = static_cast<uint32_t>(x);
  uint32_t uy = static_cast<uint32_t>(y);
  return libmorton::morton2D_64_encode(ux, uy);
}
#endif

/**
 *  Parses a CSV file and creates a list of 2D points.
 *  Expected format: ID,Year,Month,Day,Time,x,y
 *  Only extracts ID, x, y columns for 2D range queries.
 */
std::vector<Point2D> load_points_file(const std::string &path) {
  std::vector<Point2D> points;
  
  try {
    csv::CSVReader reader(path);
    
    for (csv::CSVRow& row : reader) {
      // Extract ID, x, y columns (assuming they are at positions 0, 5, 6)
      std::string id_str = row[0].get<std::string>();
      int32_t x = row[5].get<int32_t>();
      int32_t y = row[6].get<int32_t>();
      
      // Convert ID string to hash for unique identifier
      uint32_t id = std::hash<std::string>{}(id_str) % UINT32_MAX;
      
      Point2D point(id, x, y);
      points.push_back(point);
    }
    
    std::cout << "Loaded " << points.size() << " 2D points from " << path << std::endl;
    
  } catch (const std::exception& e) {
    std::cerr << "Error loading points file: " << e.what() << std::endl;
  }
  
  return points;
}
