@echo off
echo === Java MR-tree Test Script ===
echo.

REM Check if Java is available
where java >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java not found! Please install Java 11 or higher.
    goto :end
)

echo Java version:
java -version
echo.

REM Create data directory if it doesn't exist
if not exist "data" mkdir data

REM Copy test data if it exists
if exist "..\CSQV\test\data\crash_data_1000.csv" (
    echo Copying test data...
    copy "..\CSQV\test\data\crash_data_1000.csv" "data\" >nul
    echo Test data copied successfully.
) else (
    echo Warning: Test data not found. Please copy crash_data_1000.csv to data/ directory.
)

echo.

REM Compile Java sources
echo Compiling Java sources...
if not exist "build\classes" mkdir build\classes

javac -d build\classes -cp src\main\java src\main\java\com\mrtree\*.java src\main\java\com\mrtree\geometry\*.java src\main\java\com\mrtree\node\*.java src\main\java\com\mrtree\query\*.java src\main\java\com\mrtree\util\*.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    goto :end
)

echo Compilation successful!
echo.

REM Check if data file exists
if not exist "data\crash_data_1000.csv" (
    echo Error: Test data file not found!
    echo Please copy crash_data_1000.csv to the data/ directory.
    goto :end
)

REM Run TestIndex2D
echo === Running Tree Construction Test ===
java -cp build\classes com.mrtree.TestIndex2D data\crash_data_1000.csv 128

echo.
echo === Generating Test Queries ===
java -cp build\classes com.mrtree.QueryGenerator2D data\crash_data_1000.csv queries.csv 50

if exist "queries.csv" (
    echo.
    echo === Running Query Performance Test ===
    java -cp build\classes com.mrtree.TestQuery2D data\crash_data_1000.csv queries.csv 128
) else (
    echo Failed to generate queries.
)

:end
echo.
pause
