# 2D Range Query System with Verification

这是一个专门针对二维范围查询的优化版本，基于原始的CSQV项目，保留了Merkle R-tree的验证机制，但简化了数据模型专门用于二维坐标查询。

## 主要特性

### 🎯 专门优化的二维查询
- **简化数据模型**: 只保留ID和2D坐标信息
- **高效范围查询**: 优化的矩形相交检测
- **Morton Z-order排序**: 可选的空间局部性优化

### 🔐 完整的验证机制
- **SHA-256哈希验证**: 确保查询结果完整性
- **Merkle树结构**: 支持增量验证
- **防篡改保证**: 检测数据修改和查询结果篡改

### ⚡ 性能优化
- **批量加载算法**: 快速树构建
- **智能剪枝**: 减少不必要的节点访问
- **详细统计信息**: 性能分析和调优

## 编译和构建

### 依赖项
- **C++17** 编译器
- **LibreSSL** (用于SHA-256哈希计算)
- **libmorton** (用于Morton Z-order编码，已包含)

### 编译命令
```bash
# 编译2D查询系统
make -f makefile2d 2d

# 编译所有程序（包括原始系统）
make -f makefile2d all

# 清理编译文件
make -f makefile2d clean

# 查看帮助
make -f makefile2d help
```

## 程序使用

### 1. Test2DIndex - 树构建测试
测试2D MR-tree的构建性能和正确性。

```bash
./Test2DIndex <data_file> <capacity>
```

**参数说明:**
- `data_file`: CSV数据文件，格式为 `ID,Year,Month,Day,Time,x,y`
- `capacity`: 每个叶子节点的最大点数

**示例:**
```bash
./Test2DIndex test/data/crash_data_30000.csv 128
```

**输出信息:**
- 数据加载时间和统计
- 树构建时间
- 树结构统计（高度、叶子数量等）
- 内存使用估算
- 正确性验证测试

### 2. QueryGen2D - 查询生成器
生成随机的2D范围查询用于测试。

```bash
./QueryGen2D <data_file> <query_file> <num_queries> [min_size] [max_size]
```

**参数说明:**
- `data_file`: 输入数据文件
- `query_file`: 输出查询文件
- `num_queries`: 生成的查询数量
- `min_size`: 最小查询大小（占数据MBR的比例，默认0.01）
- `max_size`: 最大查询大小（占数据MBR的比例，默认0.1）

**示例:**
```bash
./QueryGen2D test/data/crash_data_30000.csv queries_2d.csv 1000 0.005 0.05
```

**输出格式:**
生成的查询文件格式为：`lx,ly,ux,uy,matching,fraction`

### 3. Test2DQuery - 查询性能测试
执行2D范围查询并进行验证，测量性能。

```bash
./Test2DQuery <data_file> <query_file> <capacity>
```

**参数说明:**
- `data_file`: 数据文件
- `query_file`: 查询文件（由QueryGen2D生成）
- `capacity`: 树节点容量

**示例:**
```bash
./Test2DQuery test/data/crash_data_30000.csv queries_2d.csv 128
```

**输出统计:**
- 平均访问节点数
- 平均剪枝节点数
- 平均检查点数
- 平均返回点数
- 查询时间和验证时间
- 剪枝效率

## 数据格式

### 输入数据格式
CSV文件，包含以下列：
```
ID,Year,Month,Day,Time,x,y
2019-7606-27/05/2021,2019,August,Thursday,03:30 pm,134327535,170856058
```

**注意:** 系统只使用ID、x、y列，其他列会被忽略。

### 查询文件格式
```
lx,ly,ux,uy,matching,fraction
93867047,152575127,115523391,238375647,322,0.0107333
```

其中：
- `lx,ly`: 查询矩形左下角坐标
- `ux,uy`: 查询矩形右上角坐标
- `matching`: 匹配的点数（由生成器计算）
- `fraction`: 选择率（匹配点数/总点数）

## 配置选项

### Morton Z-order排序
在 `Config.hpp` 中定义 `Z_INDEX` 宏来启用Morton排序：

```cpp
#define Z_INDEX  // 启用Morton Z-order排序
// #undef Z_INDEX   // 使用字典序排序
```

**Morton排序的优势:**
- 更好的空间局部性
- 提高缓存效率
- 减少磁盘I/O（对大数据集）

## 性能调优建议

### 1. 节点容量选择
- **小容量 (16-64)**: 更好的剪枝效果，适合选择性查询
- **大容量 (128-512)**: 更少的树层级，适合大范围查询
- **推荐值**: 128-256 对大多数应用场景效果较好

### 2. 查询优化
- 使用Morton排序提高空间局部性
- 批量执行查询以摊销树遍历开销
- 根据查询模式调整树结构

### 3. 内存优化
- 对于大数据集，考虑使用更大的节点容量
- 监控内存使用情况，避免内存不足

## 验证机制说明

### 查询验证流程
1. **查询执行**: 遍历树结构，收集相关节点信息
2. **验证对象生成**: 创建包含必要信息的验证对象
3. **结果重建**: 从验证对象重建查询结果和树根信息
4. **完整性检查**: 验证哈希值确保数据未被篡改

### 验证对象类型
- **VLeaf2D**: 叶子节点，包含实际数据点
- **VPruned2D**: 被剪枝的内部节点，只包含MBR和哈希
- **VContainer2D**: 被探索的内部节点，包含子验证对象

### 安全保证
- **完整性**: SHA-256哈希确保数据未被修改
- **真实性**: Merkle树结构支持增量验证
- **不可否认**: 验证对象可以独立验证查询结果

## 示例工作流程

```bash
# 1. 构建并测试树结构
./Test2DIndex test/data/crash_data_30000.csv 128

# 2. 生成测试查询
./QueryGen2D test/data/crash_data_30000.csv test_queries.csv 100

# 3. 执行查询性能测试
./Test2DQuery test/data/crash_data_30000.csv test_queries.csv 128
```

## 与原始系统的对比

| 特性 | 原始系统 | 2D优化系统 |
|------|----------|------------|
| 数据模型 | 完整车祸记录 | 简化2D点 |
| 内存使用 | 较高 | 优化 |
| 查询性能 | 通用 | 2D优化 |
| 验证机制 | 完整 | 完整保留 |
| 易用性 | 复杂 | 简化 |

## 故障排除

### 常见问题

1. **编译错误**: 确保安装了LibreSSL和C++17编译器
2. **数据加载失败**: 检查CSV文件格式和路径
3. **内存不足**: 减少节点容量或数据集大小
4. **性能问题**: 尝试不同的节点容量设置

### 调试选项
程序会输出详细的统计信息，包括：
- 节点访问模式
- 内存使用情况
- 查询执行时间
- 验证开销

这些信息可以帮助识别性能瓶颈和优化机会。
