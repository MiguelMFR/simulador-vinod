package br.edu.mic1.service;

import java.util.Locale;

/**
 * Codifica macroinstrucoes de 16 bits da arquitetura didatica baseada em MIC-1.
 *
 * A classe trabalha com palavras sem sinal armazenadas em int (0..65535).
 * Ela NAO gera microinstrucoes para a memoria de controle.
 */
public class Encoder {

    private static final int MASK_12 = 0x0FFF;
    private static final int MASK_10 = 0x03FF;
    private static final int MASK_8  = 0x00FF;

    public int encode(String mnemonic, int operand) {
        String op = normalize(mnemonic);

        switch (op) {
            // Formato: opcode de 4 bits + operando de 12 bits
            case "LODD": return encode4_12(0x0, operand, op);
            case "STOD": return encode4_12(0x1, operand, op);
            case "ADDD": return encode4_12(0x2, operand, op);
            case "SUBD": return encode4_12(0x3, operand, op);
            case "JPOS": return encode4_12(0x4, operand, op);
            case "JZER": return encode4_12(0x5, operand, op);
            case "JUMP": return encode4_12(0x6, operand, op);
            case "LOCO": return encode4_12(0x7, operand, op);
            case "LODL": return encode4_12(0x8, operand, op);
            case "STOL": return encode4_12(0x9, operand, op);
            case "ADDL": return encode4_12(0xA, operand, op);
            case "SUBL": return encode4_12(0xB, operand, op);
            case "JNEG": return encode4_12(0xC, operand, op);
            case "JNZE": return encode4_12(0xD, operand, op);

            // Formato: opcode de 6 bits + operando de 10 bits
            case "CALL":
                checkRange(op, operand, 0, MASK_10);
                return (0b111000 << 10) | (operand & MASK_10);

            // Instrucoes sem operando: palavra fixa de 16 bits
            case "PSHI": return 0xF000;
            case "POPI": return 0xF200;
            case "PUSH": return 0xF400;
            case "POP":  return 0xF600;
            case "RETN": return 0xF800;
            case "SWAP": return 0xFA00;

            // Formato: opcode de 8 bits + operando de 8 bits
            case "INSP":
                checkRange(op, operand, 0, MASK_8);
                return 0xFC00 | (operand & MASK_8);
            case "DESP":
                checkRange(op, operand, 0, MASK_8);
                return 0xFE00 | (operand & MASK_8);

            default:
                throw new IllegalArgumentException("Macroinstrucao desconhecida: " + mnemonic);
        }
    }

    /**
     * Sobrecarga para instrucoes que nao possuem operando.
     */
    public int encode(String mnemonic) {
        String op = normalize(mnemonic);
        switch (op) {
            case "PSHI":
            case "POPI":
            case "PUSH":
            case "POP":
            case "RETN":
            case "SWAP":
                return encode(op, 0);
            default:
                throw new IllegalArgumentException(
                    "A instrucao " + op + " exige um operando."
                );
        }
    }

    private int encode4_12(int opcode, int operand, String mnemonic) {
        // O campo de 12 bits aceita valores de -2048 a 4095.
        // Valores negativos sao armazenados em complemento de dois.
        checkRange(mnemonic, operand, -2048, MASK_12);
        return ((opcode & 0xF) << 12) | (operand & MASK_12);
    }

    private void checkRange(String mnemonic, int value, int min, int max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                "Operando de " + mnemonic + " fora do intervalo permitido (" +
                min + " a " + max + "): " + value
            );
        }
    }

    private String normalize(String mnemonic) {
        if (mnemonic == null || mnemonic.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome de instrucao vazio.");
        }
        return mnemonic.trim().toUpperCase(Locale.ROOT);
    }
}
