package com.mrtree;

import com.mrtree.geometry.Point2D;
import com.mrtree.geometry.Rectangle;
import com.mrtree.node.Node2D;
import com.mrtree.node.TreeBuilder2D;
import com.mrtree.util.DataLoader;
import java.util.List;

/**
 * Test program for 2D tree construction performance.
 * Java equivalent of the C++ Test2DIndex program.
 * 
 * @author Java port of CSQV MR-tree
 */
public class TestIndex2D {
    
    public static void main(String[] args) {
        if (args.length < 2) {
            printUsage();
            return;
        }
        
        String dataFile = args[0];
        int capacity = Integer.parseInt(args[1]);
        
        System.out.println("=== 2D 树构建测试 ===");
        System.out.println("数据文件: " + dataFile);
        System.out.println("容量: " + capacity);
        System.out.println();
        
        try {
            // Load data points
            System.out.println("正在加载数据点...");
            long loadStart = System.nanoTime();
            List<Point2D> points = DataLoader.loadPointsFromFile(dataFile);
            long loadEnd = System.nanoTime();
            
            if (points.isEmpty()) {
                System.err.println("错误: 无法从数据文件加载点");
                return;
            }
            
            double loadTimeMs = (loadEnd - loadStart) / 1_000_000.0;
            System.out.printf("已加载 %d 个点，耗时 %.2f 毫秒%n", points.size(), loadTimeMs);
            
            // Compute data statistics
            Rectangle dataMbr = DataLoader.computeMBR(points);
            System.out.printf("数据边界矩形: (%d, %d) 到 (%d, %d)%n", 
                             dataMbr.lx, dataMbr.ly, dataMbr.ux, dataMbr.uy);
            
            long width = (long)dataMbr.ux - dataMbr.lx;
            long dataHeight = (long)dataMbr.uy - dataMbr.ly;
            System.out.printf("数据维度: %d x %d%n", width, dataHeight);
            System.out.println();
            
            // Build 2D MR-tree
            System.out.println("正在构建 2D MR-tree...");
            long buildStart = System.nanoTime();
            Node2D root = TreeBuilder2D.buildTree(points, capacity);
            long buildEnd = System.nanoTime();
            
            if (root == null) {
                System.err.println("错误: 树构建失败");
                return;
            }
            
            double buildTimeMs = (buildEnd - buildStart) / 1_000_000.0;
            double buildTimeUs = (buildEnd - buildStart) / 1_000.0;
            
            // Print construction results
            System.out.println();
            System.out.println("=== 构建结果 ===");
            System.out.printf("构建时间: %.0f 微秒%n", buildTimeUs);
            System.out.printf("构建时间: %.2f 毫秒%n", buildTimeMs);
            
            TreeBuilder2D.printTreeStats(root);
            
            // Calculate additional statistics
            int leaves = TreeBuilder2D.countLeaves(root);
            int height = TreeBuilder2D.computeHeight(root);
            double avgPointsPerLeaf = (double) points.size() / leaves;
            double treeUtilization = avgPointsPerLeaf / capacity;
            
            System.out.println();
            System.out.println("=== Additional Statistics ===");
            System.out.printf("Average points per leaf: %.2f%n", avgPointsPerLeaf);
            System.out.printf("Tree utilization: %.2f%%%n", treeUtilization * 100);
            System.out.printf("Points per microsecond: %.2f%n", points.size() / buildTimeUs);
            
            // Memory usage estimation (rough)
            long estimatedMemory = estimateMemoryUsage(points.size(), leaves, height);
            System.out.printf("Estimated memory usage: %.2f MB%n", estimatedMemory / (1024.0 * 1024.0));
            
            System.out.println();
            System.out.println("Tree construction test completed!");
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void printUsage() {
        System.out.println("Usage: java TestIndex2D <data_file> <capacity>");
        System.out.println("  data_file: CSV file with format ID,Year,Month,Day,Time,x,y or x,y");
        System.out.println("  capacity: Maximum number of points per leaf node");
    }
    
    private static long estimateMemoryUsage(int numPoints, int leaves, int height) {
        // Rough estimation
        long pointMemory = numPoints * 32; // Approximate size per Point2D
        long leafMemory = leaves * 128;    // Approximate size per leaf node
        long internalMemory = (leaves / 4 + 1) * 96 * height; // Rough estimate for internal nodes
        return pointMemory + leafMemory + internalMemory;
    }
}
