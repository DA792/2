#
#	File:	makefile2d
#	Author:	Modified for 2D Range Query System
#

CXX=g++
CXX_FLAGS= -std=c++17 -O2 -IC:\msys64\mingw64\include -Ilibmorton
LD_FLAGS= -LC:\msys64\mingw64\lib -lcrypto -lws2_32 -lcrypt32

.PHONY: all clean

# Core objects for 2D system
OBJECTS_2D=Buffer.o Hash.o Point2D.o Node2D.o Query2D.o

# Target executables
TARGETS=TestQuery QueryGen TestIndex QueryGenMultiple

%.o: %.cpp
	$(CXX) $(CXX_FLAGS) -c $^

# 2D Range Query System targets
TestQuery: $(OBJECTS_2D) Test2DQuery.o
	$(CXX) $^ $(LD_FLAGS) -o TestQuery

QueryGen: $(OBJECTS_2D) QueryGen2D.o
	$(CXX) $^ $(LD_FLAGS) -o QueryGen

TestIndex: $(OBJECTS_2D) Test2DIndex.o
	$(CXX) $^ $(LD_FLAGS) -o TestIndex

QueryGenMultiple: $(OBJECTS_2D) QueryGenMultiple.o
	$(CXX) $^ $(LD_FLAGS) -o QueryGenMultiple

# Build targets
all: $(TARGETS)

clean:
	-rm -f *.o $(TARGETS)

# Help target
help:
	@echo "Available targets:"
	@echo "  all       - Build 2D range query system"
	@echo "  clean     - Remove all built files"
	@echo ""
	@echo "2D Range Query System executables:"
	@echo "  TestQuery  - Test 2D range queries with verification"
	@echo "  QueryGen   - Generate random 2D range queries"
	@echo "  TestIndex  - Test 2D tree construction"
