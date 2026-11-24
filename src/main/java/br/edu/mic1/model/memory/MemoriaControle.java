package br.edu.mic1.model;

import java.util.Arrays;

public class MemoriaControle {
    private final Microinstruction[] words;

    public MemoriaControle(int size) {
        if (size <= 0 || size > 256) {
            throw new IllegalArgumentException("A memória de controle deve ter de 1 a 256 palavras.");
        }
        words = new Microinstruction[size];
        Arrays.fill(words, Microinstruction.nop());
    }

    public Microinstruction read(int address) {
        if (address < 0 || address >= words.length) {
            throw new IndexOutOfBoundsException("Endereço da memória de controle: " + address);
        }
        return words[address];
    }

    public void write(int address, Microinstruction instruction) {
        if (address < 0 || address >= words.length) {
            throw new IndexOutOfBoundsException("Endereço da memória de controle: " + address);
        }
        if (instruction == null) {
            throw new IllegalArgumentException("A microinstrução não pode ser nula.");
        }
        words[address] = instruction;
    }

    public int size() {
        return words.length;
    }
}
