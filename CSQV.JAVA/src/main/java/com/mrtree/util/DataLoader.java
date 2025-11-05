package com.mrtree.util;

import com.mrtree.geometry.Point2D;
import com.mrtree.geometry.Rectangle;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Utility for loading data points and generating queries.
 * Java equivalent of the C++ data loading functionality.
 * 
 * @author Java port of CSQV MR-tree
 */
public class DataLoader {
    
    /**
     * Load 2D points from CSV file.
     * Expected format: x,y (simple format) or ID,Year,Month,Day,Time,x,y (full format)
     * @param filePath path to the CSV file
     * @return list of 2D points
     * @throws IOException if file cannot be read
     */
    public static List<Point2D> loadPointsFromFile(String filePath) throws IOException {
        List<Point2D> points = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int pointId = 1;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                String[] parts = line.split(",");
                
                // Skip header line if it contains non-numeric data
                if (isFirstLine && !isNumeric(parts[0])) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;
                
                try {
                    if (parts.length == 2) {
                        // Simple x,y format
                        int x = Integer.parseInt(parts[0].trim());
                        int y = Integer.parseInt(parts[1].trim());
                        points.add(new Point2D(pointId++, x, y));
                    } else if (parts.length >= 7) {
                        // Full format: ID,Year,Month,Day,Time,x,y
                        int x = Integer.parseInt(parts[5].trim());
                        int y = Integer.parseInt(parts[6].trim());
                        points.add(new Point2D(pointId++, x, y));
                    } else {
                        System.err.println("Skipping invalid line: " + line);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid line: " + line);
                }
            }
        }
        
        System.out.println("Loaded " + points.size() + " 2D points from " + filePath);
        return points;
    }
    
    /**
     * Load query rectangles from CSV file.
     * Expected format: lx,ly,ux,uy,matching,fraction
     * @param filePath path to the query file
     * @return list of query rectangles
     * @throws IOException if file cannot be read
     */
    public static List<Rectangle> loadQueriesFromFile(String filePath) throws IOException {
        List<Rectangle> queries = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                // Skip header line
                if (isFirstLine && line.contains("lx")) {
                    isFirstLine = false;
                    continue;
                }
                isFirstLine = false;
                
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    try {
                        int lx = Integer.parseInt(parts[0].trim());
                        int ly = Integer.parseInt(parts[1].trim());
                        int ux = Integer.parseInt(parts[2].trim());
                        int uy = Integer.parseInt(parts[3].trim());
                        queries.add(new Rectangle(lx, ly, ux, uy));
                    } catch (NumberFormatException e) {
                        System.err.println("Skipping invalid query line: " + line);
                    }
                }
            }
        }
        
        System.out.println("Loaded " + queries.size() + " queries from " + filePath);
        return queries;
    }
    
    /**
     * Generate random query rectangles within a given MBR.
     * @param mbr the bounding rectangle for generating queries
     * @param numQueries number of queries to generate
     * @param minSizeFraction minimum query size (as fraction of MBR)
     * @param maxSizeFraction maximum query size (as fraction of MBR)
     * @return list of random query rectangles
     */
    public static List<Rectangle> generateRandomQueries(Rectangle mbr, int numQueries,
                                                       double minSizeFraction, double maxSizeFraction) {
        List<Rectangle> queries = new ArrayList<>();
        Random random = new Random();
        
        int width = mbr.ux - mbr.lx;
        int height = mbr.uy - mbr.ly;
        
        for (int i = 0; i < numQueries; i++) {
            // Generate random lower-left corner
            int lx = random.nextInt(width) + mbr.lx;
            int ly = random.nextInt(height) + mbr.ly;
            
            // Generate random size
            double sizeFactor = minSizeFraction + 
                               random.nextDouble() * (maxSizeFraction - minSizeFraction);
            int queryWidth = (int) (width * sizeFactor);
            int queryHeight = (int) (height * sizeFactor);
            
            // Ensure query stays within MBR
            int ux = Math.min(mbr.ux, lx + queryWidth);
            int uy = Math.min(mbr.uy, ly + queryHeight);
            
            queries.add(new Rectangle(lx, ly, ux, uy));
        }
        
        return queries;
    }
    
    /**
     * Compute the minimum bounding rectangle of a list of points.
     * @param points list of 2D points
     * @return minimum bounding rectangle
     */
    public static Rectangle computeMBR(List<Point2D> points) {
        if (points.isEmpty()) {
            return Rectangle.empty();
        }
        
        Rectangle mbr = Rectangle.empty();
        for (Point2D point : points) {
            mbr = mbr.enlarge(point.location);
        }
        return mbr;
    }
    
    /**
     * Count points that fall within a query rectangle.
     * @param points list of points
     * @param query query rectangle
     * @return number of matching points
     */
    public static long countPointsInRange(List<Point2D> points, Rectangle query) {
        return points.stream()
                    .mapToLong(point -> point.isInside(query) ? 1 : 0)
                    .sum();
    }
    
    /**
     * Check if a string represents a numeric value.
     * @param str string to check
     * @return true if numeric
     */
    private static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
