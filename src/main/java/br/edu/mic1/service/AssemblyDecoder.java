package br.edu.mic1.service;

import java.util.Locale;

/**
 * Decodifica palavras de maquina de 16 bits para macroinstrucoes.
 * A entrada deve ser um int entre 0 e 65535.
 */
public class AssemblyDecoder {

    public String decode(int machineWord) {
        if (machineWord < 0 || machineWord > 0xFFFF) {
            throw new IllegalArgumentException(
                "A palavra deve estar entre 0 e 65535: " + machineWord
            );
        }

        int first4 = (machineWord >>> 12) & 0xF;
        int first6 = (machineWord >>> 10) & 0x3F;
        int first8 = (machineWord >>> 8) & 0xFF;

        // Formato CALL: 111000 + 10 bits
        if (first6 == 0b111000) {
            int address = machineWord & 0x03FF;
            return "CALL " + address;
        }

        // Formato INSP/DESP: 8 bits de opcode + 8 bits de operando
        if (first8 == 0xFC) {
            return "INSP " + (machineWord & 0xFF);
        }
        if (first8 == 0xFE) {
            return "DESP " + (machineWord & 0xFF);
        }

        // Instrucoes fixas sem operando
        switch (machineWord) {
            case 0xF000: return "PSHI";
            case 0xF200: return "POPI";
            case 0xF400: return "PUSH";
            case 0xF600: return "POP";
            case 0xF800: return "RETN";
            case 0xFA00: return "SWAP";
            default: break;
        }

        // Formato: opcode de 4 bits + operando de 12 bits
        int operand = machineWord & 0x0FFF;
        String mnemonic;

        switch (first4) {
            case 0x0: mnemonic = "LODD"; break;
            case 0x1: mnemonic = "STOD"; break;
            case 0x2: mnemonic = "ADDD"; break;
            case 0x3: mnemonic = "SUBD"; break;
            case 0x4: mnemonic = "JPOS"; break;
            case 0x5: mnemonic = "JZER"; break;
            case 0x6: mnemonic = "JUMP"; break;
            case 0x7: mnemonic = "LOCO"; break;
            case 0x8: mnemonic = "LODL"; break;
            case 0x9: mnemonic = "STOL"; break;
            case 0xA: mnemonic = "ADDL"; break;
            case 0xB: mnemonic = "SUBL"; break;
            case 0xC: mnemonic = "JNEG"; break;
            case 0xD: mnemonic = "JNZE"; break;
            default:
                throw new IllegalArgumentException(
                    String.format(Locale.ROOT,
                        "Opcode nao reconhecido: %16s",
                        Integer.toBinaryString(machineWord))
                );
        }

        // LOCO representa um imediato com sinal de 12 bits.
        if ("LOCO".equals(mnemonic) && (operand & 0x0800) != 0) {
            operand -= 0x1000;
        }

        return mnemonic + " " + operand;
    }

    /**
     * Devolve a palavra em binario, sempre com 16 caracteres.
     */
    public String toBinaryString(int machineWord) {
        if (machineWord < 0 || machineWord > 0xFFFF) {
            throw new IllegalArgumentException("Palavra fora de 16 bits.");
        }
        return String.format("%16s", Integer.toBinaryString(machineWord))
                     .replace(' ', '0');
    }

    /**
     * Converte uma String binaria de exatamente 16 bits em int.
     */
    public int fromBinaryString(String binary) {
        if (binary == null || !binary.matches("[01]{16}")) {
            throw new IllegalArgumentException(
                "A representacao binaria deve conter exatamente 16 bits."
            );
        }
        return Integer.parseInt(binary, 2);
    }
}
