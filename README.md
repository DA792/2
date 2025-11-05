# CSQV MR-tree Java Implementation

这是C++版本CSQV MR-tree的Java移植版本，专门针对二维范围查询和验证系统进行了优化。

## 项目特性

### 🎯 **核心功能**
- **2D Merkle R-tree**: 完整的Java实现
- **范围查询**: 高效的二维矩形范围查询
- **查询验证**: 基于SHA-256的完整性验证
- **Morton编码**: Z-order空间排序优化
- **批量加载**: 高效的树构建算法

### 🔐 **验证机制**
- **SHA-256哈希**: 确保数据完整性
- **Merkle树结构**: 支持增量验证
- **验证对象**: 完整的查询结果验证

### ⚡ **性能优化**
- **纯Java实现**: 无外部依赖
- **内存优化**: 高效的数据结构
- **并发友好**: 线程安全的哈希计算

## 项目结构

```
CSQV.JAVA/
├── src/main/java/com/mrtree/
│   ├── geometry/           # 几何类
│   │   ├── Point.java      # 2D点
│   │   ├── Point2D.java    # 带ID的2D点
│   │   └── Rectangle.java  # 矩形
│   ├── node/              # 树节点类
│   │   ├── Node2D.java     # 抽象节点
│   │   ├── LeafNode2D.java # 叶子节点
│   │   ├── InternalNode2D.java # 内部节点
│   │   └── TreeBuilder2D.java  # 树构建器
│   ├── query/             # 查询和验证
│   │   ├── VObject2D.java  # 验证对象基类
│   │   ├── VLeaf2D.java    # 叶子验证对象
│   │   ├── VPruned2D.java  # 剪枝验证对象
│   │   ├── VContainer2D.java # 容器验证对象
│   │   ├── VResult2D.java  # 验证结果
│   │   ├── QueryStats2D.java # 查询统计
│   │   └── QueryEngine2D.java # 查询引擎
│   ├── util/              # 工具类
│   │   ├── MortonEncoder.java # Morton编码
│   │   ├── HashUtil.java   # 哈希工具
│   │   └── DataLoader.java # 数据加载
│   ├── TestIndex2D.java   # 树构建测试
│   ├── TestQuery2D.java   # 查询性能测试
│   └── QueryGenerator2D.java # 查询生成器
├── data/                  # 数据文件目录
├── build.gradle          # Gradle构建脚本
└── README.md             # 项目说明
```

## 编译和运行

### **环境要求**
- Java 11 或更高版本
- Gradle 6.0+ (可选，可使用wrapper)

### **编译项目**

```bash
# 使用Gradle编译
./gradlew build

# 或者使用系统Gradle
gradle build

# 构建所有可执行JAR
./gradlew buildAllJars
```

### **运行程序**

#### **1. 树构建测试**
```bash
# 使用Gradle运行
./gradlew runTestIndex --args="data/crash_data_1000.csv 128"

# 或使用Java直接运行
java -cp build/classes/java/main com.mrtree.TestIndex2D data/crash_data_1000.csv 128

# 使用JAR运行
java -jar build/libs/CSQV.JAVA-1.0.0-testindex.jar data/crash_data_1000.csv 128
```

#### **2. 查询生成器**
```bash
# 生成100个随机查询
./gradlew runQueryGen --args="data/crash_data_1000.csv queries.csv 100"

# 指定查询大小范围
java -cp build/classes/java/main com.mrtree.QueryGenerator2D data/crash_data_1000.csv queries.csv 100 0.005 0.05
```

#### **3. 查询性能测试**
```bash
# 执行查询测试
./gradlew runTestQuery --args="data/crash_data_1000.csv queries.csv 128"

# 使用JAR运行
java -jar build/libs/CSQV.JAVA-1.0.0-testquery.jar data/crash_data_1000.csv queries.csv 128
```

## 数据格式

### **输入数据格式**
支持两种CSV格式：

**简单格式 (x,y):**
```csv
431130,392763
211248,448884
469970,87525
```

**完整格式 (ID,Year,Month,Day,Time,x,y):**
```csv
ID,Year,Month,Day,Time,x,y
2019-7606-27/05/2021,2019,August,Thursday,03:30 pm,134327535,170856058
```

### **查询文件格式**
```csv
lx,ly,ux,uy,matching,fraction
93867047,152575127,115523391,238375647,322,0.0107333
```

## 使用示例

### **完整测试流程**

```bash
# 1. 编译项目
./gradlew build

# 2. 复制数据文件到data目录
cp ../CSQV/test/data/crash_data_1000.csv data/

# 3. 测试树构建
java -cp build/classes/java/main com.mrtree.TestIndex2D data/crash_data_1000.csv 128

# 4. 生成测试查询
java -cp build/classes/java/main com.mrtree.QueryGenerator2D data/crash_data_1000.csv queries.csv 50

# 5. 执行查询测试
java -cp build/classes/java/main com.mrtree.TestQuery2D data/crash_data_1000.csv queries.csv 128
```

### **性能调优建议**

#### **节点容量选择**
- **小容量 (16-64)**: 更好的剪枝效果
- **中等容量 (128-256)**: 平衡性能
- **大容量 (512-1024)**: 减少树高度

#### **JVM优化**
```bash
# 增加堆内存
java -Xmx2g -cp build/classes/java/main com.mrtree.TestQuery2D ...

# 启用G1垃圾收集器
java -XX:+UseG1GC -cp build/classes/java/main com.mrtree.TestQuery2D ...
```

## API使用

### **基本用法**

```java
import com.mrtree.geometry.Point2D;
import com.mrtree.geometry.Rectangle;
import com.mrtree.node.Node2D;
import com.mrtree.node.TreeBuilder2D;
import com.mrtree.query.QueryEngine2D;
import com.mrtree.query.VResult2D;

// 创建数据点
List<Point2D> points = Arrays.asList(
    new Point2D(1, 100, 200),
    new Point2D(2, 150, 250),
    new Point2D(3, 200, 300)
);

// 构建树
Node2D root = TreeBuilder2D.buildTree(points, 128);

// 执行查询
Rectangle query = new Rectangle(90, 190, 160, 260);
VResult2D result = QueryEngine2D.queryAndVerify(root, query, null);

// 获取结果
System.out.println("Found " + result.count() + " points");
```

## 与C++版本的对比

| 特性 | C++版本 | Java版本 |
|------|---------|----------|
| 性能 | 更快 | 较快 |
| 内存使用 | 更少 | 适中 |
| 可移植性 | 平台相关 | 跨平台 |
| 开发效率 | 中等 | 高 |
| 内存安全 | 手动管理 | 自动管理 |
| 并发支持 | 复杂 | 简单 |

## 故障排除

### **常见问题**

1. **OutOfMemoryError**: 增加JVM堆内存 `-Xmx4g`
2. **数据加载失败**: 检查CSV文件格式和编码
3. **性能问题**: 调整节点容量或启用JVM优化

### **调试选项**
```bash
# 启用详细GC日志
java -XX:+PrintGC -XX:+PrintGCDetails ...

# 启用JIT编译日志
java -XX:+PrintCompilation ...
```

## 扩展功能

项目设计支持以下扩展：

- **多线程查询**: 并行处理多个查询
- **持久化存储**: 序列化树结构到磁盘
- **网络服务**: 基于HTTP的查询服务
- **可视化**: 树结构和查询结果可视化

## 许可证

本项目基于原始CSQV项目，遵循相同的开源许可证。
