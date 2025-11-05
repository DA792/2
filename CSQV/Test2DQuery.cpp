/**
 *  @file Test2DQuery.cpp
 *  @author Modified for 2D Range Query System
 *  
 *  Test program for 2D range queries with verification
 */

#include "Point2D.hpp"
#include "Node2D.hpp"
#include "Query2D.hpp"
#include <iostream>
#include <chrono>
#include <iomanip>

using namespace std::chrono;

void print_usage(const char* program_name) {
  std::cout << "Usage: " << program_name << " <data_file> <query_file> <capacity>" << std::endl;
  std::cout << "  data_file: CSV file with format ID,Year,Month,Day,Time,x,y" << std::endl;
  std::cout << "  query_file: CSV file with format lx,ly,ux,uy,matching,fraction" << std::endl;
  std::cout << "  capacity: Maximum number of points per leaf node" << std::endl;
}

int main(int argc, char const *argv[]) {
  if (argc < 4) {
    print_usage(argv[0]);
    return 1;
  }
  
  std::string data_file = argv[1];
  std::string query_file = argv[2];
  size_t capacity = std::stoul(argv[3]);
  
  std::cout << "=== 2D 范围查询系统测试 ===" << std::endl;
  std::cout << "数据文件: " << data_file << std::endl;
  std::cout << "查询文件: " << query_file << std::endl;
  std::cout << "叶节点容量: " << capacity << std::endl << std::endl;
  
  // Load data points
  std::cout << "加载数据点..." << std::endl;
  auto load_start = high_resolution_clock::now();
  std::vector<Point2D> points = load_points_file(data_file);
  auto load_end = high_resolution_clock::now();
  
  if (points.empty()) {
    std::cerr << "Error: No points loaded from data file" << std::endl;
    return 1;
  }
  
  std::cout << "已加载 " << points.size() << " 个2D点，耗时 " 
            << duration_cast<milliseconds>(load_end - load_start).count() 
            << " ms" << std::endl << std::endl;
  
  // Build 2D MR-tree
  std::cout << "构建 2D MR-tree..." << std::endl;
  auto build_start = high_resolution_clock::now();
  Node2D *root = build_2d_tree(points, capacity);
  auto build_end = high_resolution_clock::now();
  
  if (!root) {
    std::cerr << "Error: Failed to build tree" << std::endl;
    return 1;
  }
  
  std::cout << "树构建完成，耗时 " 
            << duration_cast<milliseconds>(build_end - build_start).count() 
            << " ms" << std::endl;
  print_2d_tree_stats(root);
  std::cout << std::endl;
  
  // Load queries
  std::cout << "加载查询..." << std::endl;
  std::vector<Rectangle> queries = load_queries_2d(query_file);
  
  if (queries.empty()) {
    std::cerr << "Error: No queries loaded" << std::endl;
    delete_2d_tree(root);
    return 1;
  }
  
  std::cout << "已加载 " << queries.size() << " 个查询" << std::endl << std::endl;
  
  // Execute queries
  std::cout << "执行查询中..." << std::endl;
  
  QueryStats2D total_stats;
  size_t total_points_returned = 0;
  
  for (size_t i = 0; i < queries.size(); i++) {
    QueryStats2D query_stats;
    
    VResult2D *result = query_and_verify_2d(root, queries[i], &query_stats);
    
    if (result) {
      total_points_returned += result->count();
      total_stats.nodes_visited += query_stats.nodes_visited;
      total_stats.nodes_pruned += query_stats.nodes_pruned;
      total_stats.points_examined += query_stats.points_examined;
      total_stats.points_returned += query_stats.points_returned;
      total_stats.query_time_us += query_stats.query_time_us;
      total_stats.verify_time_us += query_stats.verify_time_us;
      
      delete result;
    }
    
    // Print progress every 100 queries
    if ((i + 1) % 100 == 0 || i == queries.size() - 1) {
      std::cout << "已处理 " << (i + 1) << "/" << queries.size() 
                << " 个查询" << std::endl;
    }
  }
  
  // Print summary statistics
  std::cout << std::endl << "=== 统计摘要 ===" << std::endl;
  std::cout << "查询数量: " << queries.size() << std::endl;
  std::cout << "平均访问节点数: " << std::fixed << std::setprecision(2)
            << (double)total_stats.nodes_visited / queries.size() << std::endl;
  std::cout << "平均剪枝节点数: " << std::fixed << std::setprecision(2)
            << (double)total_stats.nodes_pruned / queries.size() << std::endl;
  std::cout << "平均检查点数: " << std::fixed << std::setprecision(2)
            << (double)total_stats.points_examined / queries.size() << std::endl;
  std::cout << "平均返回点数: " << std::fixed << std::setprecision(2)
            << (double)total_stats.points_returned / queries.size() << std::endl;
  std::cout << "平均查询时间: " << std::fixed << std::setprecision(4)
            << total_stats.query_time_us / (queries.size() * 1000.0) << " ms" << std::endl;
  std::cout << "平均验证时间: " << std::fixed << std::setprecision(4)
            << total_stats.verify_time_us / (queries.size() * 1000.0) << " ms" << std::endl;
  std::cout << "平均总时间: " << std::fixed << std::setprecision(4)
            << (total_stats.query_time_us + total_stats.verify_time_us) / (queries.size() * 1000.0)
            << " ms" << std::endl;
  
  // Calculate pruning efficiency
  double pruning_ratio = (double)total_stats.nodes_pruned / 
                        (total_stats.nodes_visited + total_stats.nodes_pruned);
  std::cout << "剪枝效率: " << std::fixed << std::setprecision(2)
            << pruning_ratio * 100 << "%" << std::endl;
  
  // Clean up
  delete_2d_tree(root);
  
  std::cout << std::endl << "测试成功完成！" << std::endl;
  return 0;
}
