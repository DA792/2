package com.mrtree;

import com.mrtree.geometry.Point2D;
import com.mrtree.geometry.Rectangle;
import com.mrtree.util.DataLoader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Generates random 2D range queries for testing.
 * Java equivalent of the C++ QueryGen2D program.
 * 
 * @author Java port of CSQV MR-tree
 */
public class QueryGenerator2D {
    
    public static void main(String[] args) {
        if (args.length < 3) {
            printUsage();
            return;
        }
        
        String dataFile = args[0];
        String queryFile = args[1];
        int numQueries = Integer.parseInt(args[2]);
        
        double minSize = args.length > 3 ? Double.parseDouble(args[3]) : 0.01;
        double maxSize = args.length > 4 ? Double.parseDouble(args[4]) : 0.1;
        
        System.out.println("=== 2D 查询生成器 ===");
        System.out.println("数据文件: " + dataFile);
        System.out.println("查询文件: " + queryFile);
        System.out.println("查询数量: " + numQueries);
        System.out.printf("查询大小范围: %.4f - %.4f%n", minSize, maxSize);
        System.out.println();
        
        try {
            // Load data points to compute MBR
            System.out.println("正在加载数据点...");
            List<Point2D> points = DataLoader.loadPointsFromFile(dataFile);
            
            if (points.isEmpty()) {
                System.err.println("错误: 无法从数据文件加载点");
                return;
            }
            
            // Compute MBR of the dataset
            Rectangle dataMbr = DataLoader.computeMBR(points);
            System.out.printf("数据边界矩形: (%d, %d) 到 (%d, %d)%n", 
                             dataMbr.lx, dataMbr.ly, dataMbr.ux, dataMbr.uy);
            
            // Generate random queries
            System.out.printf("正在生成 %d 个随机查询...%n", numQueries);
            List<Rectangle> queries = DataLoader.generateRandomQueries(dataMbr, numQueries, 
                                                                      minSize, maxSize);
            
            // Write queries to file
            try (FileWriter writer = new FileWriter(queryFile)) {
                // Write header
                writer.write("lx,ly,ux,uy,matching,fraction\n");
                
                // Process each query and compute statistics
                System.out.println("正在计算查询统计信息...");
                
                for (int i = 0; i < queries.size(); i++) {
                    Rectangle query = queries.get(i);
                    
                    // Count matching points
                    long matching = DataLoader.countPointsInRange(points, query);
                    double fraction = (double) matching / points.size();
                    
                    // Write to file
                    writer.write(String.format("%d,%d,%d,%d,%d,%.6f%n",
                                              query.lx, query.ly, query.ux, query.uy,
                                              matching, fraction));
                    
                    // Print progress
                    if ((i + 1) % 1000 == 0 || i == queries.size() - 1) {
                        System.out.printf("已处理 %d/%d 个查询%n", i + 1, queries.size());
                    }
                }
            }
            
            // Print summary statistics
            System.out.println();
            System.out.println("=== 生成总结 ===");
            System.out.println("已生成 " + queries.size() + " 个查询");
            System.out.println("输出文件: " + queryFile);
            
            // Compute basic statistics
            long totalMatching = 0;
            long minMatching = Long.MAX_VALUE;
            long maxMatching = 0;
            
            for (Rectangle query : queries) {
                long matching = DataLoader.countPointsInRange(points, query);
                totalMatching += matching;
                minMatching = Math.min(minMatching, matching);
                maxMatching = Math.max(maxMatching, matching);
            }
            
            double avgMatching = (double) totalMatching / queries.size();
            double avgFraction = avgMatching / points.size();
            
            System.out.printf("平均匹配点数: %.2f%n", avgMatching);
            System.out.printf("平均选择率: %.4f%%%n", avgFraction * 100);
            System.out.println("最少匹配点数: " + minMatching);
            System.out.println("最多匹配点数: " + maxMatching);
            
            System.out.println();
            System.out.println("查询生成完成！");
            
        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void printUsage() {
        System.out.println("Usage: java QueryGenerator2D <data_file> <query_file> <num_queries> [min_size] [max_size]");
        System.out.println("  data_file: CSV file with 2D points");
        System.out.println("  query_file: Output CSV file for generated queries");
        System.out.println("  num_queries: Number of queries to generate");
        System.out.println("  min_size: Minimum query size as fraction of data MBR (default: 0.01)");
        System.out.println("  max_size: Maximum query size as fraction of data MBR (default: 0.1)");
    }
}
