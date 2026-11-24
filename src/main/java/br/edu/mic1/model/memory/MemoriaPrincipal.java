package br.edu.mic1.model;

import java.util.Arrays;

public class MemoriaPrincipal {
    private final int[][] words;

    public MemoriaPrincipal(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("A memória deve ter tamanho positivo.");
        }
        words = new int[size][16];
    }

    public int[] read(int address) {
        validate(address);
        return words[address].clone();
    }

    public void write(int address, int[] word) {
        validate(address);
        if (word == null || word.length != 16) {
            throw new IllegalArgumentException("A memória trabalha com palavras de 16 bits.");
        }
        for (int bit : word) {
            if (bit != 0 && bit != 1) {
                throw new IllegalArgumentException("A memória aceita somente bits 0 ou 1.");
            }
        }
        words[address] = word.clone();
    }

    public void clear() {
        for (int[] word : words) {
            Arrays.fill(word, 0);
        }
    }

    public int size() {
        return words.length;
    }

    private void validate(int address) {
        if (address < 0 || address >= words.length) {
            throw new IndexOutOfBoundsException("Endereço da memória: " + address);
        }
    }
}
