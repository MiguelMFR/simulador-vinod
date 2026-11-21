package br.edu.mic1.model;

import java.util.Arrays;

public class Multiplexador {
    protected final int[][] entradas;
    protected int[] controle;

    public Multiplexador(int sizeEnt, int sizeCtrl) {
        if (sizeEnt <= 0 || sizeCtrl <= 0) {
            throw new IllegalArgumentException("Entradas e controle devem ser positivos.");
        }
        entradas = new int[sizeEnt][];
        controle = new int[sizeCtrl];
    }

    public void setEntrada(int indice, int[] valor) {
        validarIndice(indice);
        if (valor == null) {
            throw new IllegalArgumentException("A entrada não pode ser nula.");
        }
        entradas[indice] = valor.clone();
    }

    public void setControle(int[] valor) {
        if (valor == null || valor.length != controle.length) {
            throw new IllegalArgumentException("Controle com tamanho inválido.");
        }
        for (int bit : valor) {
            if (bit != 0 && bit != 1) {
                throw new IllegalArgumentException("O controle aceita somente bits 0 ou 1.");
            }
        }
        controle = valor.clone();
    }

    protected int indiceSelecionado() {
        int indice = 0;
        for (int bit : controle) {
            indice = (indice << 1) | bit;
        }
        validarIndice(indice);
        return indice;
    }

    public int[] selecionar() {
        int[] entrada = entradas[indiceSelecionado()];
        return entrada == null ? null : Arrays.copyOf(entrada, entrada.length);
    }

    private void validarIndice(int indice) {
        if (indice < 0 || indice >= entradas.length) {
            throw new IllegalArgumentException("Entrada selecionada inexistente: " + indice);
        }
    }
}