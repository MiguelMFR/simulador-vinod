package br.edu.mic1.model.register;

public class Shifter extends Registrador {
    public Shifter() {
        super(16);
    }

    public void shiftLeft() {
        int[] bits = getValor();
        for (int i = 0; i < bits.length - 1; i++) {
            bits[i] = bits[i + 1];
        }
        bits[bits.length - 1] = 0;
        setValor(bits);
    }

    public void shiftRight() {
        int[] bits = getValor();
        for (int i = bits.length - 1; i > 0; i--) {
            bits[i] = bits[i - 1];
        }
        bits[0] = 0;
        setValor(bits);
    }
}