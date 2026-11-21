package br.edu.mic1.model.register;

public class MicroprogramCounter extends Registrador {
    public MicroprogramCounter() {
        super(8);
    }

    public void reset() {
        setValor(new int[8]);
    }

    public int[] getValue() {
        return getValor();
    }

    public void set(Integer[] value) {
        setValor(value);
    }
}