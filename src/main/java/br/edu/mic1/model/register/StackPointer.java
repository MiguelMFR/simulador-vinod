package br.edu.mic1.model.register;

public class StackPointer extends Registrador {
    public StackPointer() {
        super(16);
        setValor(new Integer[] {
                0, 0, 0, 1, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0
        });
    }

}