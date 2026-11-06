package br.edu.mic1.model.register;

public class RegistradorZero extends Registrador {
    public RegistradorZero() {
        super(16);
        setValor(new Integer[] {
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0
        });
    }

}