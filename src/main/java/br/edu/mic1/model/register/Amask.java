package br.edu.mic1.model.register;

public class Amask extends Registrador {
    public Amask() {
        super(16);
        setValor(new int[] {
                0, 0, 0, 0,
                1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1
        });
    }

}