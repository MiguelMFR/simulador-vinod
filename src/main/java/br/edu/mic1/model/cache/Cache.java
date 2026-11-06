package br.edu.mic1.model.cache;

import java.util.Optional;

public class Cache {
    private final CacheLine[] lines;
    private long hits;
    private long misses;

    public Cache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("A capacidade da cache deve ser positiva.");
        }

        this.lines = new CacheLine[capacity];
    }

    public Optional<int[]> readWord(int address) {
        validateAddress(address);
        int index = address % lines.length;
        int tag = address / lines.length;
        CacheLine line = lines[index];

        if (line != null && line.tag == tag) {
            hits++;
            return Optional.of(line.word.clone());
        }

        misses++;
        return Optional.empty();
    }

    public void loadWord(int address, int[] word) {
        validateAddress(address);
        validateWord(word);
        int index = address % lines.length;
        int tag = address / lines.length;
        lines[index] = new CacheLine(tag, word);
    }

    protected final boolean containsWord(int address) {
        validateAddress(address);
        int index = address % lines.length;
        int tag = address / lines.length;
        CacheLine line = lines[index];
        return line != null && line.tag == tag;
    }

    protected final void recordHit() {
        hits++;
    }

    protected final void recordMiss() {
        misses++;
    }

    public long getHitCount() {
        return hits;
    }

    public long getMissCount() {
        return misses;
    }

    protected final void validateAddress(int address) {
        if (address < 0) {
            throw new IndexOutOfBoundsException("O endereço da palavra não pode ser negativo: " + address);
        }
    }

    protected static void validateWord(int[] word) {
        if (word == null || word.length != 16) {
            throw new IllegalArgumentException("Uma palavra deve conter exatamente 16 bits.");
        }
        for (int bit : word) {
            if (bit != 0 && bit != 1) {
                throw new IllegalArgumentException("Cada posição da palavra deve ser 0 ou 1.");
            }
        }
    }

    private static final class CacheLine {
        private final int tag;
        private final int[] word;

        private CacheLine(int tag, int[] word) {
            this.tag = tag;
            this.word = word.clone();
        }
    }
}
