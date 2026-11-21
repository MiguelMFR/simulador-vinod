package br.edu.mic1.model.memory;

import br.edu.mic1.model.MemoriaPrincipal;
import java.util.ArrayList;
import java.util.Arrays;

public class Memoria {
    private ArrayList<Integer[]> memoria;
    private MemoriaPrincipal principal;

    public Memoria(int tamanho) {
        memoria = new ArrayList<>(tamanho);
        for (int i = 0; i < tamanho; i++) {
            memoria.add(new Integer[16]);
        }
    }

    public Memoria(MemoriaPrincipal principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Memoria principal nao pode ser nula.");
        }
        this.principal = principal;
    }

    public Integer[] ler(int endereco) {
        if (principal != null) {
            int[] word = principal.read(endereco);
            Integer[] result = new Integer[16];
            for (int i = 0; i < word.length; i++) {
                result[i] = word[i];
            }
            return result;
        }
        if (endereco < 0 || endereco >= memoria.size()) {
            throw new IndexOutOfBoundsException("Endereço da memória: " + endereco);
        }
        return memoria.get(endereco);
    }

    public void escrever(int endereco, int[] word) {
        if (principal != null) {
            principal.write(endereco, word);
            return;
        }
        if (endereco < 0 || endereco >= memoria.size()) {
            throw new IndexOutOfBoundsException("Endereco da memoria: " + endereco);
        }
        if (word == null || word.length != 16) {
            throw new IllegalArgumentException("Palavra invalida.");
        }
        Integer[] copy = new Integer[16];
        for (int i = 0; i < 16; i++) {
            copy[i] = word[i];
        }
        memoria.set(endereco, copy);
    }

    public int tamanho() {
        return principal != null ? principal.size() : memoria.size();
    }

    public void limpar() {
        if (principal != null) {
            principal.clear();
            return;
        }
        for (Integer[] word : memoria) {
            Arrays.fill(word, 0);
        }
    }
}