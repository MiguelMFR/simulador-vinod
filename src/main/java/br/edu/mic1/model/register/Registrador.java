package br.edu.mic1.model.register;

public class Registrador {
    private final int[] registros;

    public Registrador(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("O tamanho do registrador deve ser positivo.");
        }
        this.registros = new int[size];
    }

    public int[] getValor() {
        return registros.clone();
    }

    public void setValor(Integer[] memoriaLinhas) {
        if (memoriaLinhas == null || memoriaLinhas.length != registros.length) {
            throw new IllegalArgumentException("O valor deve ter o mesmo tamanho do registrador.");
        }
        for (int i = 0; i < registros.length; i++) {
            Integer bit = memoriaLinhas[i];
            if (bit == null || (bit != 0 && bit != 1)) {
                throw new IllegalArgumentException("Registradores aceitam somente bits 0 ou 1.");
            }
            registros[i] = bit;
        }
    }

    public void setValor(int[] valor) {
        if (valor == null || valor.length != registros.length) {
            throw new IllegalArgumentException("O valor deve ter o mesmo tamanho do registrador.");
        }
        for (int i = 0; i < valor.length; i++) {
            if (valor[i] != 0 && valor[i] != 1) {
                throw new IllegalArgumentException("Registradores aceitam somente bits 0 ou 1.");
            }
            registros[i] = valor[i];
        }
    }
}