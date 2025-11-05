package com.mrtree.geometry;

/**
 * A point is represented by two integer coordinates.
 * Java equivalent of the C++ Point struct.
 * 
 * @author Java port of CSQV MR-tree
 */
public class Point implements Comparable<Point> {
    public final int x; // The x-coordinate of the point
    public final int y; // The y-coordinate of the point
    
    /**
     * Default constructor creating point at origin.
     */
    public Point() {
        this.x = 0;
        this.y = 0;
    }
    
    /**
     * Constructor with coordinates.
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Lexicographic comparison of points.
     * @param other the point to compare with
     * @return negative if this < other, 0 if equal, positive if this > other
     */
    @Override
    public int compareTo(Point other) {
        if (this.x != other.x) {
            return Integer.compare(this.x, other.x);
        }
        return Integer.compare(this.y, other.y);
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
        Point point = (Point) obj;
        return x == point.x && y == point.y;
    }
    
    /**
     * Hash code for the point.
     * @return hash code
     */
    @Override
    public int hashCode() {
        return 31 * x + y;
    }
    
    /**
     * String representation of the point.
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("Point(%d, %d)", x, y);
    }
}
