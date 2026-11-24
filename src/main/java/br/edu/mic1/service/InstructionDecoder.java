package br.edu.mic1.service;

import java.util.HashMap;
import java.util.Map;

public class InstructionDecoder {
    private final Map<Integer, Integer> macroEntrypoints = new HashMap<>();

    public void registerMacroInstruction(int opcode, int microaddress) {
        if (opcode < 0 || opcode > 255) {
            throw new IllegalArgumentException("Opcode deve ter 8 bits.");
        }
        macroEntrypoints.put(opcode, microaddress);
    }

    public int decode(int instruction) {
        int word = instruction & 0xFFFF;
        int first6 = word >>> 10;
        int first8 = word >>> 8;
        int opcode;
        if (first6 == 0b111000) {
            opcode = 0xE;
        } else if (first8 == 0xF0 || first8 == 0xF2 || first8 == 0xF4
                || first8 == 0xF6 || first8 == 0xF8 || first8 == 0xFA
                || first8 == 0xFC || first8 == 0xFE) {
            opcode = first8;
        } else {
            opcode = (word >>> 12) & 0xF;
        }
        Integer entrypoint = macroEntrypoints.get(opcode);
        if (entrypoint == null) {
            throw new IllegalStateException("Opcode sem microprograma: " + opcode);
        }
        return entrypoint;
    }
}
