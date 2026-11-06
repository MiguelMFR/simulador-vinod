package br.edu.mic1.model.register;

public class Registrador {
    private int[] registros;

    public Registrador(int size) {
        this.registros = new int[size];
    }

    public int[] getValor() {
        return registros;
    }

    public void setValor(Integer[] memoriaLinhas) {
        int i = 0;
        for (Integer mem : memoriaLinhas) {
            if (mem != null) {
                registros[i] = mem;
            }
            i++;
        }
    }
}