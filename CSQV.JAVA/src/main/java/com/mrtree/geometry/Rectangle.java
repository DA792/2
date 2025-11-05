package com.mrtree.geometry;

/**
 * A rectangle is represented by the coordinates of its lower-left
 * and upper-right vertices.
 * Java equivalent of the C++ Rectangle struct.
 * 
 * @author Java port of CSQV MR-tree
 */
public class Rectangle {
    public final int lx; // The x-coordinate of the lower-left vertex
    public final int ly; // The y-coordinate of the lower-left vertex
    public final int ux; // The x-coordinate of the upper-right vertex
    public final int uy; // The y-coordinate of the upper-right vertex
    
    /**
     * Constructor for rectangle.
     * @param lx lower-left x coordinate
     * @param ly lower-left y coordinate
     * @param ux upper-right x coordinate
     * @param uy upper-right y coordinate
     */
    public Rectangle(int lx, int ly, int ux, int uy) {
        this.lx = lx;
        this.ly = ly;
        this.ux = ux;
        this.uy = uy;
    }
    
    /**
     * Create an empty rectangle (used for initialization).
     * @return empty rectangle with inverted coordinates
     */
    public static Rectangle empty() {
        return new Rectangle(Integer.MAX_VALUE, Integer.MAX_VALUE, 
                           Integer.MIN_VALUE, Integer.MIN_VALUE);
    }
    
    /**
     * Check if the given point is inside this rectangle.
     * @param p the point to check
     * @return true if point is inside rectangle
     */
    public boolean contains(Point p) {
        return (lx <= p.x && p.x <= ux && ly <= p.y && p.y <= uy);
    }
    
    /**
     * Check if this rectangle intersects with another rectangle.
     * @param other the other rectangle
     * @return true if rectangles intersect
     */
    public boolean intersects(Rectangle other) {
        boolean above = (this.ly >= other.uy);  // this is above other
        boolean below = (this.uy <= other.ly);  // this is below other
        boolean left = (this.ux <= other.lx);   // this is to the left of other
        boolean right = (this.lx >= other.ux);  // this is to the right of other
        return !(above || below || left || right);
    }
    
    /**
     * Enlarge this rectangle to include the given point.
     * @param p the point to include
     * @return new rectangle that includes the point
     */
    public Rectangle enlarge(Point p) {
        return new Rectangle(
            Math.min(this.lx, p.x),
            Math.min(this.ly, p.y),
            Math.max(this.ux, p.x),
            Math.max(this.uy, p.y)
        );
    }
    
    /**
     * Enlarge this rectangle to include another rectangle.
     * @param other the rectangle to include
     * @return new rectangle that includes both rectangles
     */
    public Rectangle enlarge(Rectangle other) {
        return new Rectangle(
            Math.min(this.lx, other.lx),
            Math.min(this.ly, other.ly),
            Math.max(this.ux, other.ux),
            Math.max(this.uy, other.uy)
        );
    }
    
    /**
     * Get the area of the rectangle.
     * @return area of the rectangle
     */
    public long area() {
        if (lx > ux || ly > uy) return 0; // Invalid rectangle
        return (long)(ux - lx) * (uy - ly);
    }
    
    /**
     * Check if this rectangle is valid (not empty/inverted).
     * @return true if rectangle is valid
     */
    public boolean isValid() {
        return lx <= ux && ly <= uy;
    }
    
    /**
     * Check if this rectangle equals another rectangle.
     * @param obj the object to compare with
     * @return true if rectangles are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Rectangle rect = (Rectangle) obj;
        return lx == rect.lx && ly == rect.ly && ux == rect.ux && uy == rect.uy;
    }
    
    /**
     * Hash code for the rectangle.
     * @return hash code
     */
    @Override
    public int hashCode() {
        int result = lx;
        result = 31 * result + ly;
        result = 31 * result + ux;
        result = 31 * result + uy;
        return result;
    }
    
    /**
     * String representation of the rectangle.
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("Rectangle[(%d,%d) to (%d,%d)]", lx, ly, ux, uy);
    }
}
