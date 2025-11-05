package com.mrtree.util;

/**
 * Morton encoding utility for 2D coordinates.
 * Provides Z-order (Morton order) encoding for spatial locality.
 * 
 * @author Java port of CSQV MR-tree
 */
public class MortonEncoder {
    
    /**
     * Encode 2D coordinates into Morton (Z-order) index.
     * @param x x-coordinate
     * @param y y-coordinate
     * @return Morton encoded value
     */
    public static long encode(int x, int y) {
        // Convert to unsigned for proper bit manipulation
        long ux = Integer.toUnsignedLong(x);
        long uy = Integer.toUnsignedLong(y);
        
        return interleaveBits(ux, uy);
    }
    
    /**
     * Interleave bits of two 32-bit integers to create Morton code.
     * @param x first value
     * @param y second value
     * @return interleaved bits as Morton code
     */
    private static long interleaveBits(long x, long y) {
        x = (x | (x << 16)) & 0x0000FFFF0000FFFFL;
        x = (x | (x << 8))  & 0x00FF00FF00FF00FFL;
        x = (x | (x << 4))  & 0x0F0F0F0F0F0F0F0FL;
        x = (x | (x << 2))  & 0x3333333333333333L;
        x = (x | (x << 1))  & 0x5555555555555555L;
        
        y = (y | (y << 16)) & 0x0000FFFF0000FFFFL;
        y = (y | (y << 8))  & 0x00FF00FF00FF00FFL;
        y = (y | (y << 4))  & 0x0F0F0F0F0F0F0F0FL;
        y = (y | (y << 2))  & 0x3333333333333333L;
        y = (y | (y << 1))  & 0x5555555555555555L;
        
        return x | (y << 1);
    }
    
    /**
     * Decode Morton index back to 2D coordinates.
     * @param morton Morton encoded value
     * @return array with [x, y] coordinates
     */
    public static int[] decode(long morton) {
        long x = deinterleaveBits(morton);
        long y = deinterleaveBits(morton >> 1);
        
        return new int[]{(int) x, (int) y};
    }
    
    /**
     * Deinterleave bits to extract one coordinate.
     * @param interleaved interleaved bits
     * @return deinterleaved value
     */
    private static long deinterleaveBits(long interleaved) {
        interleaved &= 0x5555555555555555L;
        interleaved = (interleaved ^ (interleaved >> 1))  & 0x3333333333333333L;
        interleaved = (interleaved ^ (interleaved >> 2))  & 0x0F0F0F0F0F0F0F0FL;
        interleaved = (interleaved ^ (interleaved >> 4))  & 0x00FF00FF00FF00FFL;
        interleaved = (interleaved ^ (interleaved >> 8))  & 0x0000FFFF0000FFFFL;
        interleaved = (interleaved ^ (interleaved >> 16)) & 0x00000000FFFFFFFFL;
        
        return interleaved;
    }
}
