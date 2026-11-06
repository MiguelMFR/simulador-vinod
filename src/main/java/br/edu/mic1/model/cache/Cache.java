package br.edu.mic1.model.cache;// esse é o cache pai e tem que fazer os filhos dele

import java.util.HashMap;

public class Cache extends HashMap {
    private MicroInstructionRegister mirAtual = null;

    public Cache(MicroInstructionRegister mir) {
        super();
        this.mirAtual = mir;
    }

}