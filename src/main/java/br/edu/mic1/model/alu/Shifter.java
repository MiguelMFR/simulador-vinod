package br.edu.mic1.model.alu;

public class Shifter {

    public int[] shift(int[] input, int[] control) {
        if (input == null || input.length != 16) {
            throw new IllegalArgumentException("Input must be a 16-bit array.");
        }

        int[] output = new int[16];

        if (control == null || control.length < 2) {
            System.arraycopy(input, 0, output, 0, 16);
            return output;
        }

        int c0 = control[0];
        int c1 = control[1];

        // 01 -> Shift Right (1 bit para a direita, preenche índice 0 com 0)
        if (c0 == 0 && c1 == 1) {
            output[0] = 0;
            System.arraycopy(input, 0, output, 1, 15);
            return output;
        }

        // 10 -> Shift Left (1 bit para a esquerda, preenche índice 15 com 0)
        if (c0 == 1 && c1 == 0) {
            System.arraycopy(input, 1, output, 0, 15);
            output[15] = 0;
            return output;
        }

        // 00 ou 11 -> sem shift
        System.arraycopy(input, 0, output, 0, 16);
        return output;
    }
}
