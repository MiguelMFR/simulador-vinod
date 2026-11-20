package br.edu.mic1.service;

public class WordService {

    public void validateWord(int[] word) {
        if (word == null || word.length != 16) {
            throw new IllegalArgumentException("Word must be a 16-bit array.");
        }
        for (int bit : word) {
            if (bit != 0 && bit != 1) {
                throw new IllegalArgumentException("Word bits must be 0 or 1.");
            }
        }
    }

    public int[] fromBinaryString(String binary) {
        if (binary == null) {
            throw new IllegalArgumentException("Binary string cannot be null.");
        }
        String normalized = binary.trim();
        if (normalized.length() != 16) {
            throw new IllegalArgumentException("Binary string must have exactly 16 bits.");
        }

        int[] word = new int[16];
        for (int i = 0; i < 16; i++) {
            char c = normalized.charAt(i);
            if (c == '0') {
                word[i] = 0;
            } else if (c == '1') {
                word[i] = 1;
            } else {
                throw new IllegalArgumentException("Invalid character in binary string: " + c);
            }
        }
        return word;
    }

    public String toBinaryString(int[] word) {
        validateWord(word);
        StringBuilder sb = new StringBuilder(16);
        for (int bit : word) {
            sb.append(bit);
        }
        return sb.toString();
    }

    public int[] fromUnsignedInt(int value) {
        if (value < 0 || value > 65535) {
            throw new IllegalArgumentException("Value must be between 0 and 65535.");
        }

        int[] word = new int[16];
        for (int i = 15; i >= 0; i--) {
            word[i] = (value >> (15 - i)) & 1;
        }
        return word;
    }
}
