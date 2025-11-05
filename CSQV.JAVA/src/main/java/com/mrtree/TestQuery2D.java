package com.mrtree;

import com.mrtree.geometry.Point2D;
import com.mrtree.geometry.Rectangle;
import com.mrtree.node.Node2D;
import com.mrtree.node.TreeBuilder2D;
import com.mrtree.query.QueryEngine2D;
import com.mrtree.query.QueryStats2D;
import com.mrtree.query.VResult2D;
import com.mrtree.util.DataLoader;
import java.util.List;

/**
 * Test program for 2D range queries with verification.
 * Java equivalent of the C++ Test2DQuery program.
 * 
 * @author Java port of CSQV MR-tree
 */
public class TestQuery2D {
    
    public static void main(String[] args) {
        if (args.length < 3) {
            printUsage();
            return;
        }
        
        String dataFile = args[0];
        String queryFile = args[1];
        int capacity = Integer.parseInt(args[2]);
        
        System.out.println("=== 2D Range Query System Test ===");
        System.out.println("Data file: " + dataFile);
        System.out.println("Query file: " + queryFile);
        System.out.println("Capacity: " + capacity);
        System.out.println();
        
        try {
            // Load data points
            System.out.println("Loading data points...");
            long loadStart = System.nanoTime();
            List<Point2D> points = DataLoader.loadPointsFromFile(dataFile);
            long loadEnd = System.nanoTime();
            
            if (points.isEmpty()) {
                System.err.println("Error: No points loaded from data file");
                return;
            }
            
            double loadTimeMs = (loadEnd - loadStart) / 1_000_000.0;
            System.out.printf("Loaded %d points in %.2f ms%n", points.size(), loadTimeMs);
            System.out.println();
            
            // Build 2D MR-tree
            System.out.println("Building 2D MR-tree...");
            long buildStart = System.nanoTime();
            Node2D root = TreeBuilder2D.buildTree(points, capacity);
            long buildEnd = System.nanoTime();
            
            if (root == null) {
                System.err.println("Error: Failed to build tree");
                return;
            }
            
            double buildTimeMs = (buildEnd - buildStart) / 1_000_000.0;
            System.out.printf("Tree built in %.2f ms%n", buildTimeMs);
            TreeBuilder2D.printTreeStats(root);
            System.out.println();
            
            // Load queries
            System.out.println("Loading queries...");
            List<Rectangle> queries = DataLoader.loadQueriesFromFile(queryFile);
            
            if (queries.isEmpty()) {
                System.err.println("Error: No queries loaded");
                return;
            }
            
            System.out.println();
            
            // Execute queries
            System.out.println("Executing queries...");
            
            QueryStats2D totalStats = new QueryStats2D();
            long totalPointsReturned = 0;
            
            for (int i = 0; i < queries.size(); i++) {
                QueryStats2D queryStats = new QueryStats2D();
                
                VResult2D result = QueryEngine2D.queryAndVerify(root, queries.get(i), queryStats);
                
                if (result != null) {
                    totalPointsReturned += result.count();
                    totalStats.add(queryStats);
                }
                
                // Print progress every 100 queries
                if ((i + 1) % 100 == 0 || i == queries.size() - 1) {
                    System.out.printf("Processed %d/%d queries%n", i + 1, queries.size());
                }
            }
            
            // Print summary statistics
            System.out.println();
            System.out.println("=== Summary Statistics ===");
            System.out.println("Number of queries: " + queries.size());
            System.out.printf("Average nodes visited: %.2f%n", 
                             (double) totalStats.nodesVisited / queries.size());
            System.out.printf("Average nodes pruned: %.2f%n", 
                             (double) totalStats.nodesPruned / queries.size());
            System.out.printf("Average points examined: %.2f%n", 
                             (double) totalStats.pointsExamined / queries.size());
            System.out.printf("Average points returned: %.2f%n", 
                             (double) totalStats.pointsReturned / queries.size());
            System.out.printf("Average query time: %.2f μs%n", 
                             totalStats.getQueryTimeUs() / queries.size());
            System.out.printf("Average verification time: %.2f μs%n", 
                             totalStats.getVerifyTimeUs() / queries.size());
            System.out.printf("Average total time: %.2f μs%n", 
                             totalStats.getTotalTimeUs() / queries.size());
            
            // Calculate pruning efficiency
            double pruningRatio = totalStats.getPruningRatio();
            System.out.printf("Pruning efficiency: %.2f%%%n", pruningRatio * 100);
            
            System.out.println();
            System.out.println("Test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void printUsage() {
        System.out.println("Usage: java TestQuery2D <data_file> <query_file> <capacity>");
        System.out.println("  data_file: CSV file with format ID,Year,Month,Day,Time,x,y or x,y");
        System.out.println("  query_file: CSV file with format lx,ly,ux,uy,matching,fraction");
        System.out.println("  capacity: Maximum number of points per leaf node");
    }
}
