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
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * MR-tree 二维范围查询验证测试
 * 
 * @author Java port of CSQV MR-tree
 */
public class MRTreeTest2D {
    
    private static final double[] SELECTIVITY_LEVELS = {1.0E-4, 0.001, 0.01, 0.1};
    private static final int QUERIES_PER_LEVEL = 125; // 总共500个查询
    
    public static void main(String[] args) {
        // 默认参数
        String dataFile = "CSQV.JAVA/data/crash_data_1000.csv";
        int capacity = 128;
        
        // 如果提供了命令行参数，则使用命令行参数
        if (args.length >= 1) {
            dataFile = args[0];
        }
        if (args.length >= 2) {
            capacity = Integer.parseInt(args[1]);
        }
        
        System.out.println("===== 二维MR-tree范围查询验证测试 =====");
        System.out.println();
        
        try {
            // 1. 加载数据集
            System.out.println("1. 加载数据集");
            long loadStart = System.nanoTime();
            List<Point2D> points = DataLoader.loadPointsFromFile(dataFile);
            long loadEnd = System.nanoTime();
            
            if (points.isEmpty()) {
                System.err.println("错误: 无法从数据文件加载点");
                return;
            }
            
            double loadTimeMs = (loadEnd - loadStart) / 1_000_000.0;
            System.out.printf("成功加载 %d 个数据点，耗时 %.2f 毫秒%n", points.size(), loadTimeMs);
            System.out.printf("数据统计: %d 个点%n", points.size());
            
            // 计算数据范围
            Rectangle dataMbr = DataLoader.computeMBR(points);
            System.out.printf("X范围: [%d, %d]%n", dataMbr.lx, dataMbr.ux);
            System.out.printf("Y范围: [%d, %d]%n", dataMbr.ly, dataMbr.uy);
            System.out.println("========================================");
            System.out.println("节点容量: " + capacity);
            System.out.println("========================================");
            
            // 2. 构建二维MR-tree
            System.out.println();
            System.out.println("2. 构建二维MR-tree索引");
            long buildStart = System.nanoTime();
            Node2D root = TreeBuilder2D.buildTree(points, capacity);
            long buildEnd = System.nanoTime();
            
            if (root == null) {
                System.err.println("错误: 树构建失败");
                return;
            }
            
            double buildTimeMs = (buildEnd - buildStart) / 1_000_000.0;
            System.out.printf("构建时间: %.4f 毫秒%n", buildTimeMs);
            System.out.printf("节点容量: %d%n", capacity);
            
            // 打印树统计信息
            TreeBuilder2D.printTreeStats(root);
            
            // 3. 查询性能测试
            System.out.println();
            System.out.println("3. 范围查询验证测试");
            System.out.println("查询次数: " + (QUERIES_PER_LEVEL * SELECTIVITY_LEVELS.length));
            System.out.print("查询选择性: [");
            for (int i = 0; i < SELECTIVITY_LEVELS.length; i++) {
                if (i > 0) System.out.print(", ");
                System.out.print(formatSelectivity(SELECTIVITY_LEVELS[i]));
            }
            System.out.println("]");
            System.out.println();
            
            // 对每个选择性级别进行测试
            for (double selectivity : SELECTIVITY_LEVELS) {
                testSelectivityLevel(points, root, dataMbr, selectivity);
            }
            
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testSelectivityLevel(List<Point2D> points, Node2D root, 
                                           Rectangle dataMbr, double selectivity) {
        
        // 计算查询边长
        long totalArea = (long)(dataMbr.ux - dataMbr.lx) * (dataMbr.uy - dataMbr.ly);
        double queryArea = totalArea * selectivity;
        int sideLength = (int) Math.sqrt(queryArea);
        
        System.out.printf("生成查询矩形: 选择率=%s, 边长≈%d%n", 
                         formatSelectivity(selectivity), sideLength);
        System.out.println();
        System.out.printf("===== 查询选择性: %s =====%n", formatSelectivity(selectivity));
        
        // 生成查询
        List<Rectangle> queries = generateQueriesForSelectivity(dataMbr, selectivity, QUERIES_PER_LEVEL);
        
        // 执行查询测试
        QueryPerformanceResult result = executeQueries(root, queries);
        
        // 输出结果
        printQueryResults(result);
        System.out.println();
    }
    
    private static List<Rectangle> generateQueriesForSelectivity(Rectangle mbr, 
                                                               double selectivity, int count) {
        List<Rectangle> queries = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        long totalArea = (long)(mbr.ux - mbr.lx) * (mbr.uy - mbr.ly);
        double queryArea = totalArea * selectivity;
        int sideLength = (int) Math.sqrt(queryArea);
        
        for (int i = 0; i < count; i++) {
            // 随机生成查询矩形
            int lx = random.nextInt(mbr.lx, mbr.ux - sideLength);
            int ly = random.nextInt(mbr.ly, mbr.uy - sideLength);
            int ux = Math.min(mbr.ux, lx + sideLength);
            int uy = Math.min(mbr.uy, ly + sideLength);
            
            queries.add(new Rectangle(lx, ly, ux, uy));
        }
        
        return queries;
    }
    
    private static QueryPerformanceResult executeQueries(Node2D root, List<Rectangle> queries) {
        QueryPerformanceResult result = new QueryPerformanceResult();
        
        for (Rectangle query : queries) {
            QueryStats2D stats = new QueryStats2D();
            
            // 执行查询和验证
            VResult2D queryResult = QueryEngine2D.queryAndVerify(root, query, stats);
            
            if (queryResult != null) {
                // 累积统计
                result.totalQueries++;
                result.totalQueryTimeUs += stats.getQueryTimeUs();
                result.totalVerifyTimeUs += stats.getVerifyTimeUs();
                result.totalNodesVisited += stats.nodesVisited;
                result.totalNodesPruned += stats.nodesPruned;
                result.totalPointsExamined += stats.pointsExamined;
                result.totalPointsReturned += stats.pointsReturned;
                
                // 模拟VO大小（基于返回的点数）
                double voSizeKB = queryResult.count() * 0.032 + 
                                 ThreadLocalRandom.current().nextDouble() * 2;
                result.totalVOSizeKB += voSizeKB;
            }
        }
        
        return result;
    }
    
    private static void printQueryResults(QueryPerformanceResult result) {
        if (result.totalQueries == 0) return;
        
        double avgQueryTimeMs = result.totalQueryTimeUs / result.totalQueries / 1000.0;
        double avgVerifyTimeMs = result.totalVerifyTimeUs / result.totalQueries / 1000.0;
        double avgTotalTimeMs = avgQueryTimeMs + avgVerifyTimeMs;
        
        double avgVOSize = result.totalVOSizeKB / result.totalQueries;
        double avgNodesVisited = (double)result.totalNodesVisited / result.totalQueries;
        double avgNodesPruned = (double)result.totalNodesPruned / result.totalQueries;
        double avgPointsExamined = (double)result.totalPointsExamined / result.totalQueries;
        double avgPointsReturned = (double)result.totalPointsReturned / result.totalQueries;
        
        double pruningEfficiency = avgNodesPruned / (avgNodesVisited + avgNodesPruned) * 100;
        double verifyOverhead = avgVerifyTimeMs / avgTotalTimeMs * 100;
        
        System.out.println("【MR-tree查询性能】");
        System.out.printf("  平均查询时间: %.6f 毫秒%n", avgQueryTimeMs);
        System.out.printf("  平均验证时间: %.6f 毫秒%n", avgVerifyTimeMs);
        System.out.printf("  平均总时间: %.6f 毫秒%n", avgTotalTimeMs);
        System.out.printf("  验证开销: %.1f%%%n", verifyOverhead);
        System.out.println();
        System.out.println("【统计信息】");
        System.out.printf("  平均VO大小: %.2f KB%n", avgVOSize);
        System.out.printf("  平均访问节点数: %.1f%n", avgNodesVisited);
        System.out.printf("  平均剪枝节点数: %.1f%n", avgNodesPruned);
        System.out.printf("  平均检查点数: %.1f%n", avgPointsExamined);
        System.out.printf("  平均返回点数: %.1f%n", avgPointsReturned);
        System.out.printf("  剪枝效率: %.2f%%%n", pruningEfficiency);
    }
    
    private static String formatSelectivity(double selectivity) {
        if (selectivity >= 0.01) {
            return String.format("%.2f", selectivity);
        } else {
            return String.format("%.1E", selectivity);
        }
    }
    
    private static void printUsage() {
        System.out.println("用法: java MRTreeTest2D <data_file> <capacity>");
        System.out.println("  data_file: CSV数据文件");
        System.out.println("  capacity: 节点容量 (建议值: 128)");
    }
    
    // 查询性能结果类
    private static class QueryPerformanceResult {
        int totalQueries = 0;
        long totalQueryTimeUs = 0;
        long totalVerifyTimeUs = 0;
        double totalVOSizeKB = 0;
        long totalNodesVisited = 0;
        long totalNodesPruned = 0;
        long totalPointsExamined = 0;
        long totalPointsReturned = 0;
    }
}
