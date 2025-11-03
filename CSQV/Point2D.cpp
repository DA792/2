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
 *  Expected format: x,y (simple 2-column format)
 *  Generates sequential IDs for points.
 */
std::vector<Point2D> load_points_file(const std::string &path) {
  std::vector<Point2D> points;
  
  try {
    csv::CSVReader reader(path);
    uint32_t id = 0;
    
    for (csv::CSVRow& row : reader) {
      // Extract x, y columns (positions 0, 1)
      int32_t x = row[0].get<int32_t>();
      int32_t y = row[1].get<int32_t>();
      
      // Use sequential ID
      Point2D point(id++, x, y);
      points.push_back(point);
    }
    
    std::cout << "Loaded " << points.size() << " 2D points from " << path << std::endl;
    
  } catch (const std::exception& e) {
    std::cerr << "Error loading points file: " << e.what() << std::endl;
  }
  
  return points;
}
