package com.mrtree.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Hash utility for computing SHA-256 digests.
 * Java equivalent of the C++ Hash functionality.
 * 
 * @author Java port of CSQV MR-tree
 */
public class HashUtil {
    public static final int HASH_LENGTH = 32; // SHA-256 produces 32 bytes
    
    private static final ThreadLocal<MessageDigest> SHA256_DIGEST = 
        ThreadLocal.withInitial(() -> {
            try {
                return MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-256 not available", e);
            }
        });
    
    /**
     * Compute SHA-256 hash of byte array.
     * @param data input data
     * @return SHA-256 hash
     */
    public static byte[] sha256(byte[] data) {
        MessageDigest digest = SHA256_DIGEST.get();
        digest.reset();
        return digest.digest(data);
    }
    
    /**
     * Compute SHA-256 hash of string.
     * @param input input string
     * @return SHA-256 hash
     */
    public static byte[] sha256(String input) {
        return sha256(input.getBytes());
    }
    
    /**
     * Convert hash bytes to hexadecimal string.
     * @param hash hash bytes
     * @return hexadecimal string representation
     */
    public static String toHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    
    /**
     * Create a buffer for hashing multiple values.
     * @return new hash buffer
     */
    public static HashBuffer createBuffer() {
        return new HashBuffer();
    }
    
    /**
     * Buffer for accumulating data to hash.
     */
    public static class HashBuffer {
        private ByteBuffer buffer;
        
        public HashBuffer() {
            this.buffer = ByteBuffer.allocate(1024); // Initial capacity
        }
        
        /**
         * Add integer to buffer.
         * @param value integer value
         * @return this buffer for chaining
         */
        public HashBuffer putInt(int value) {
            ensureCapacity(4);
            buffer.putInt(value);
            return this;
        }
        
        /**
         * Add long to buffer.
         * @param value long value
         * @return this buffer for chaining
         */
        public HashBuffer putLong(long value) {
            ensureCapacity(8);
            buffer.putLong(value);
            return this;
        }
        
        /**
         * Add byte array to buffer.
         * @param data byte array
         * @return this buffer for chaining
         */
        public HashBuffer putBytes(byte[] data) {
            ensureCapacity(data.length);
            buffer.put(data);
            return this;
        }
        
        /**
         * Compute SHA-256 hash of buffer contents.
         * @return SHA-256 hash
         */
        public byte[] hash() {
            byte[] data = Arrays.copyOf(buffer.array(), buffer.position());
            return sha256(data);
        }
        
        /**
         * Get current buffer size.
         * @return buffer size in bytes
         */
        public int size() {
            return buffer.position();
        }
        
        /**
         * Clear the buffer.
         */
        public void clear() {
            buffer.clear();
        }
        
        /**
         * Ensure buffer has enough capacity.
         * @param additionalBytes additional bytes needed
         */
        private void ensureCapacity(int additionalBytes) {
            if (buffer.remaining() < additionalBytes) {
                // Grow buffer
                int newCapacity = Math.max(buffer.capacity() * 2, 
                                         buffer.position() + additionalBytes);
                ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
                buffer.flip();
                newBuffer.put(buffer);
                buffer = newBuffer;
            }
        }
    }
}
