package com.mrtree.geometry;

import com.mrtree.util.MortonEncoder;

/**
 * A simplified record containing only 2D coordinates and an ID.
 * This is optimized for pure 2D range queries.
 * Java equivalent of the C++ Point2D struct.
 * 
 * @author Java port of CSQV MR-tree
 */
public class Point2D implements Comparable<Point2D> {
    public final int id;           // Unique identifier for the point
    public final Point location;   // The 2D location coordinates
    public final long zIndex;      // Morton index for spatial ordering
    
    private static final boolean USE_Z_INDEX = true; // Configuration flag
    
    /**
     * Default constructor.
     */
    public Point2D() {
        this.id = 0;
        this.location = new Point(0, 0);
        this.zIndex = USE_Z_INDEX ? MortonEncoder.encode(0, 0) : 0;
    }
    
    /**
     * Constructor with parameters.
     * @param id unique identifier
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public Point2D(int id, int x, int y) {
        this.id = id;
        this.location = new Point(x, y);
        this.zIndex = USE_Z_INDEX ? MortonEncoder.encode(x, y) : 0;
    }
    
    /**
     * Constructor with Point.
     * @param id unique identifier
     * @param location the point location
     */
    public Point2D(int id, Point location) {
        this.id = id;
        this.location = location;
        this.zIndex = USE_Z_INDEX ? MortonEncoder.encode(location.x, location.y) : 0;
    }
    
    /**
     * Compare points for sorting.
     * Uses Morton ordering if enabled, otherwise lexicographic ordering.
     * @param other the point to compare with
     * @return comparison result
     */
    @Override
    public int compareTo(Point2D other) {
        if (USE_Z_INDEX) {
            return Long.compare(this.zIndex, other.zIndex);
        } else {
            return this.location.compareTo(other.location);
        }
    }
    
    /**
     * Check if this point is inside the query rectangle.
     * @param query the query rectangle
     * @return true if point is inside rectangle
     */
    public boolean isInside(Rectangle query) {
        return query.contains(this.location);
    }
    
    /**
     * Get the x-coordinate.
     * @return x-coordinate
     */
    public int getX() {
        return location.x;
    }
    
    /**
     * Get the y-coordinate.
     * @return y-coordinate
     */
    public int getY() {
        return location.y;
    }
    
    /**
     * Check if this point equals another point.
     * @param obj the object to compare with
     * @return true if points are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Point2D point2D = (Point2D) obj;
        return id == point2D.id && location.equals(point2D.location);
    }
    
    /**
     * Hash code for the point.
     * @return hash code
     */
    @Override
    public int hashCode() {
        return 31 * id + location.hashCode();
    }
    
    /**
     * String representation of the point.
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("Point2D[id=%d, loc=%s, z=%d]", id, location, zIndex);
    }
}
