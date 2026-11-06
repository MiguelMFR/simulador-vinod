package br.edu.mic1.model.register;

public class RegistradorUm extends Registrador {
    public RegistradorUm() {
        super(16);
        setValor(new Integer[] {
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 1
        });
    }

}