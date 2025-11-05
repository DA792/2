package com.mrtree.query;

/**
 * Query statistics for performance analysis.
 * Java equivalent of the C++ QueryStats2D struct.
 * 
 * @author Java port of CSQV MR-tree
 */
public class QueryStats2D {
    public long nodesVisited;      // Number of nodes visited during query
    public long nodesPruned;       // Number of nodes pruned
    public long pointsExamined;    // Total points examined
    public long pointsReturned;    // Points that match the query
    public double queryTimeNs;     // Query execution time in nanoseconds
    public double verifyTimeNs;    // Verification time in nanoseconds
    
    /**
     * Default constructor initializing all stats to zero.
     */
    public QueryStats2D() {
        this.nodesVisited = 0;
        this.nodesPruned = 0;
        this.pointsExamined = 0;
        this.pointsReturned = 0;
        this.queryTimeNs = 0.0;
        this.verifyTimeNs = 0.0;
    }
    
    /**
     * Add statistics from another QueryStats2D object.
     * @param other the other statistics to add
     */
    public void add(QueryStats2D other) {
        this.nodesVisited += other.nodesVisited;
        this.nodesPruned += other.nodesPruned;
        this.pointsExamined += other.pointsExamined;
        this.pointsReturned += other.pointsReturned;
        this.queryTimeNs += other.queryTimeNs;
        this.verifyTimeNs += other.verifyTimeNs;
    }
    
    /**
     * Get total time in nanoseconds.
     * @return total time
     */
    public double getTotalTimeNs() {
        return queryTimeNs + verifyTimeNs;
    }
    
    /**
     * Get query time in microseconds.
     * @return query time in microseconds
     */
    public double getQueryTimeUs() {
        return queryTimeNs / 1000.0;
    }
    
    /**
     * Get verification time in microseconds.
     * @return verification time in microseconds
     */
    public double getVerifyTimeUs() {
        return verifyTimeNs / 1000.0;
    }
    
    /**
     * Get total time in microseconds.
     * @return total time in microseconds
     */
    public double getTotalTimeUs() {
        return getTotalTimeNs() / 1000.0;
    }
    
    /**
     * Calculate pruning efficiency.
     * @return pruning ratio (0.0 to 1.0)
     */
    public double getPruningRatio() {
        long totalNodes = nodesVisited + nodesPruned;
        return totalNodes > 0 ? (double) nodesPruned / totalNodes : 0.0;
    }
    
    /**
     * Print query statistics.
     */
    public void print() {
        System.out.println("Query Statistics:");
        System.out.println("  Nodes visited: " + nodesVisited);
        System.out.println("  Nodes pruned: " + nodesPruned);
        System.out.println("  Points examined: " + pointsExamined);
        System.out.println("  Points returned: " + pointsReturned);
        System.out.printf("  Query time: %.2f μs%n", getQueryTimeUs());
        System.out.printf("  Verification time: %.2f μs%n", getVerifyTimeUs());
        System.out.printf("  Total time: %.2f μs%n", getTotalTimeUs());
        System.out.printf("  Pruning efficiency: %.2f%%%n", getPruningRatio() * 100);
    }
    
    /**
     * String representation.
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("QueryStats2D[visited=%d, pruned=%d, examined=%d, returned=%d, time=%.2fμs]",
                           nodesVisited, nodesPruned, pointsExamined, pointsReturned, getTotalTimeUs());
    }
}
