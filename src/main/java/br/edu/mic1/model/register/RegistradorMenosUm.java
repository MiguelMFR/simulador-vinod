package br.edu.mic1.model.register;

public class RegistradorMenosUm extends Registrador {
    public RegistradorMenosUm() {
        super(16);
        setValor(new Integer[] {
                1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1
        });
    }

}