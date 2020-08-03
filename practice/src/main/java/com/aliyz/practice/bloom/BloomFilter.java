package com.aliyz.practice.bloom;

import com.sun.tools.javac.util.ArrayUtils;

import java.io.Serializable;
import java.util.BitSet;

/**
 * All rights Reserved, Designed By www.tusdao.com
 *
 * <p></p>
 * Created by mawl at 2020-07-30 15:05
 * Copyright: 2020 www.tusdao.com Inc. All rights reserved.
 */
public class BloomFilter implements Serializable {
    private static final int STEPS_8 = 8;
    private static final int LOW_3_BITS = 7;
    private static final int ENSURE_BYTE = 255;
    private int expectedNumberOfFilterElements = 0;
    private int numberOfAddedElements;
    private int k = 3;
    private int bitSetSize = 2048;
    private BitSet bitset;

    public BloomFilter(byte[] bloom) {
        this.bitset = new BitSet(this.bitSetSize);
        ArrayUtils.reverse(bloom);
        this.bitset = BitSet.valueOf(bloom);
    }

    public BloomFilter() {
        this.bitset = new BitSet(this.bitSetSize);
        this.numberOfAddedElements = 0;
    }

    public void setBitset(BitSet bitset) {
        this.bitset = bitset;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            BloomFilter other = (BloomFilter)obj;
            if (this.expectedNumberOfFilterElements != other.expectedNumberOfFilterElements) {
                return false;
            } else if (this.k != other.k) {
                return false;
            } else if (this.bitSetSize != other.bitSetSize) {
                return false;
            } else {
                return this.bitset == other.bitset || this.bitset != null && this.bitset.equals(other.bitset);
            }
        }
    }

    public int hashCode() {
        int hash = 7;
        int hash = 61 * hash + (this.bitset != null ? this.bitset.hashCode() : 0);
        hash = 61 * hash + this.expectedNumberOfFilterElements;
        hash = 61 * hash + this.bitSetSize;
        hash = 61 * hash + this.k;
        return hash;
    }

    public double expectedFalsePositiveProbability() {
        return this.getFalsePositiveProbability((double)this.expectedNumberOfFilterElements);
    }

    public double getFalsePositiveProbability(double numberOfElements) {
        return Math.pow(1.0D - Math.exp((double)(-this.k) * numberOfElements / (double)this.bitSetSize), (double)this.k);
    }

    public double getFalsePositiveProbability() {
        return this.getFalsePositiveProbability((double)this.numberOfAddedElements);
    }

    public int getK() {
        return this.k;
    }

    public synchronized void clear() {
        this.bitset.clear();
        this.numberOfAddedElements = 0;
    }

    public synchronized void add(byte[] bytes) {
        int[] hashes = this.createHashes(bytes, this.k);
        int[] var3 = hashes;
        int var4 = hashes.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            int hash = var3[var5];
            this.bitset.set(Math.abs(hash % this.bitSetSize), true);
        }

        ++this.numberOfAddedElements;
    }

    private int[] createHashes(byte[] bytes, int k) {
        int[] ret = new int[k];
        if (bytes.length / 4 < k) {
            int[] maxHashes = new int[bytes.length / 4];
            ByteUtils.bytesToInts(bytes, maxHashes, false);

            for(int i = 0; i < ret.length; ++i) {
                ret[i] = maxHashes[i % maxHashes.length];
            }
        } else {
            this.generateRet(bytes, ret);
        }

        return ret;
    }

    private void generateRet(byte[] toBloom, int[] ret) {
        ret[0] = ((toBloom[0] & 255 & 7) << 8) + (toBloom[1] & 255);
        ret[1] = ((toBloom[2] & 255 & 7) << 8) + (toBloom[3] & 255);
        ret[2] = ((toBloom[4] & 255 & 7) << 8) + (toBloom[5] & 255);
    }

    public synchronized boolean contains(byte[] bytes) {
        int[] hashes = this.createHashes(bytes, this.k);
        int[] var3 = hashes;
        int var4 = hashes.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            int hash = var3[var5];
            if (!this.bitset.get(Math.abs(hash % this.bitSetSize))) {
                return false;
            }
        }

        return true;
    }

    public synchronized boolean getBit(int bit) {
        return this.bitset.get(bit);
    }

    public synchronized void setBit(int bit, boolean value) {
        this.bitset.set(bit, value);
    }

    public synchronized BitSet getBitSet() {
        return this.bitset;
    }

    public synchronized int size() {
        return this.bitSetSize;
    }

    public synchronized int count() {
        return this.numberOfAddedElements;
    }

    public int getExpectedNumberOfElements() {
        return this.expectedNumberOfFilterElements;
    }
}
