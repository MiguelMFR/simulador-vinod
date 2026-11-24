package br.edu.mic1.model.register;

public class Smask extends Registrador {
    public Smask() {
        super(16);
        setValor(new int[] {
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 1, 1, 1, 1
        });
    }

}