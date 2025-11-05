@echo off
REM 性能测试脚本 - 测试所有选择性级别

echo ============================================
echo       MR-tree 性能测试套件
echo ============================================
echo.

REM 设置参数
set DATA_FILE=test\data\crash_data_1000.csv
set CAPACITY=50
set NUM_QUERIES=100

echo [1/5] 生成多选择性查询集...
echo ----------------------------------------
QueryGenMultiple.exe %DATA_FILE% queries %NUM_QUERIES%
echo.

echo [2/5] 测试选择性 0.0001 (0.01%%)
echo ----------------------------------------
TestQuery.exe %DATA_FILE% queries_sel_0.0001.csv %CAPACITY%
echo.

echo [3/5] 测试选择性 0.001 (0.1%%)
echo ----------------------------------------
TestQuery.exe %DATA_FILE% queries_sel_0.0010.csv %CAPACITY%
echo.

echo [4/5] 测试选择性 0.01 (1%%)
echo ----------------------------------------
TestQuery.exe %DATA_FILE% queries_sel_0.0100.csv %CAPACITY%
echo.

echo [5/5] 测试选择性 0.1 (10%%)
echo ----------------------------------------
TestQuery.exe %DATA_FILE% queries_sel_0.1000.csv %CAPACITY%
echo.

echo ============================================
echo       性能测试完成！
echo ============================================
echo.
echo 生成的文件:
echo   - queries_sel_0.0001.csv (超小查询)
echo   - queries_sel_0.0010.csv (小查询)
echo   - queries_sel_0.0100.csv (中等查询)
echo   - queries_sel_0.1000.csv (大查询)
echo.
echo 详细对比分析请查看: performance_comparison.md

pause

