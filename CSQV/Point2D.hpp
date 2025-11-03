/**
 *  @file Point2D.hpp
 *  @author Modified for 2D Range Query System
 *  
 *  Simplified data structure for 2D range queries with verification
 */

#ifndef POINT2D_H
#define POINT2D_H

#include "Config.hpp"
#include "Geometry.hpp"
#include "Hash.hpp"
#include "Buffer.hpp"

/**
 *  A simplified record containing only 2D coordinates and an ID.
 *  This is optimized for pure 2D range queries.
 */
struct Point2D {
  uint32_t id;        ///< Unique identifier for the point
  Point loc;          ///< The 2D location coordinates
  #ifdef Z_INDEX
  uint_fast64_t z_index; ///< Morton index of the location for spatial ordering
  #endif

  /**
   *  Default constructor
   */
  Point2D() : id(0), loc({0, 0}) {
    #ifdef Z_INDEX
    z_index = 0;
    #endif
  }

  /**
   *  Constructor with parameters
   */
  Point2D(uint32_t _id, int32_t x, int32_t y) : id(_id), loc({x, y}) {
    #ifdef Z_INDEX
    // Compute Morton index for spatial ordering
    z_index = morton2D_encode(x, y);
    #endif
  }

  /**
   *  "Strictly-less than" operator for point comparison.
   *  Uses Morton ordering if enabled, otherwise lexicographic ordering.
   */
  bool operator<(const Point2D &other) const {
    #ifdef Z_INDEX
    return z_index < other.z_index;
    #else
    return loc < other.loc;
    #endif
  }

  /**
   *  Equality operator
   */
  bool operator==(const Point2D &other) const {
    return id == other.id && loc.x == other.loc.x && loc.y == other.loc.y;
  }
};

/**
 *  Checks if a given point is inside the query rectangle.
 *  @param p the point
 *  @param q the query rectangle
 *  @return true if and only if the point is inside the rectangle
 */
static inline bool contains(const Point2D &p, const Rectangle &q) {
  return (q.lx <= p.loc.x && p.loc.x <= q.ux &&
          q.ly <= p.loc.y && p.loc.y <= q.uy);
}

/**
 *  Counts the number of points that fall within a query rectangle.
 *  @param points list of 2D points
 *  @param q the query rectangle
 *  @return the number of points inside the rectangle
 */
static inline size_t count_in_range(const std::vector<Point2D> &points,
  const Rectangle &q) {
  size_t count = 0;
  for (const Point2D &p : points) {
    if (contains(p, q)) count++;
  }
  return count;
}

/**
 *  Returns all points that fall within a query rectangle.
 *  @param points list of 2D points
 *  @param q the query rectangle
 *  @return vector of points inside the rectangle
 */
static inline std::vector<Point2D> range_query(const std::vector<Point2D> &points,
  const Rectangle &q) {
  std::vector<Point2D> result;
  for (const Point2D &p : points) {
    if (contains(p, q)) {
      result.push_back(p);
    }
  }
  return result;
}

/**
 *  Parses a CSV file and creates a list of 2D points.
 *  Expected format: ID,Year,Month,Day,Time,x,y
 *  Only extracts ID, x, y columns for 2D range queries.
 *  @param path full path of the input file
 *  @return a list of 2D points parsed from the input file
 */
std::vector<Point2D> load_points_file(const std::string &path);

/**
 *  Inserts a 2D point into a buffer for hashing.
 *  @param buf the buffer
 *  @param p the 2D point
 */
static inline void put_point2d(Buffer &buf, const Point2D &p) {
  buf.put(p.id).put(p.loc.x).put(p.loc.y);
}

/**
 *  Computes the minimum bounding rectangle of a list of 2D points.
 *  @param points list of 2D points
 *  @return the minimum bounding rectangle
 */
static inline Rectangle compute_mbr(const std::vector<Point2D> &points) {
  if (points.empty()) return EMPTY_RECT;
  
  Rectangle mbr = EMPTY_RECT;
  for (const Point2D &p : points) {
    mbr = enlarge(mbr, p.loc);
  }
  return mbr;
}

// Morton encoding function declaration (to be implemented)
#ifdef Z_INDEX
uint_fast64_t morton2D_encode(int32_t x, int32_t y);
#endif

#endif
