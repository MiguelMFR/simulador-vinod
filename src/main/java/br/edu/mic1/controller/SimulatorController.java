package br.edu.mic1.controller;

import br.edu.mic1.model.Mic1Model;
import br.edu.mic1.service.AssemblyDecoder;
import br.edu.mic1.service.Montador;

import java.util.ArrayList;
import java.util.List;

public class SimulatorController {
    private final Mic1Model model;
    private final Montador montador = new Montador();
    private final AssemblyDecoder decoder = new AssemblyDecoder();
    private List<Integer> assembledProgram = new ArrayList<>();
    private int assembledExecutableLength;
    private List<String> sourceLines = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();

    public SimulatorController(Mic1Model model) {
        if (model == null) {
            throw new IllegalArgumentException("O modelo é obrigatório.");
        }
        this.model = model;
    }

    public void step() {
        model.step();
        notifyListeners();
    }

    public void reset() {
        model.reset();
        notifyListeners();
    }

    public AssemblyResult montar(String sourceCode) {
        try {
            List<Integer> words = montador.montar(sourceCode);
            assembledExecutableLength = montador.getLastInstructionCount();
            List<AssemblyLine> lines = new ArrayList<>();
            sourceLines = new ArrayList<>();
            for (int address = 0; address < words.size(); address++) {
                String source = decoder.decode(words.get(address));
                sourceLines.add(source);
                lines.add(new AssemblyLine(address, source,
                        decoder.toBinaryString(words.get(address))));
            }
            assembledProgram = words;
            notifyListeners();
            return AssemblyResult.success(lines);
        } catch (IllegalArgumentException ex) {
            return AssemblyResult.error(ex.getMessage());
        }
    }

    public void carregarProgramaMontado() {
        model.loadProgram(assembledProgram, assembledExecutableLength);
        notifyListeners();
    }

    public boolean hasAssembledProgram() {
        return !assembledProgram.isEmpty();
    }

    public AssemblyResult montarECarregar(String sourceCode) {
        AssemblyResult result = montar(sourceCode);
        if (!result.isSuccess()) {
            return result;
        }
        try {
            model.loadProgram(assembledProgram, assembledExecutableLength);
            notifyListeners();
            return result;
        } catch (IllegalArgumentException ex) {
            return AssemblyResult.error(ex.getMessage());
        }
    }

    public Mic1Model getModel() {
        return model;
    }

    public void addStateListener(Runnable listener) {
        if (listener == null) {
            throw new IllegalArgumentException("O listener não pode ser nulo.");
        }
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    public static class AssemblyLine {
        private final int address;
        private final String source;
        private final String binary;

        public AssemblyLine(int address, String source, String binary) {
            this.address = address;
            this.source = source;
            this.binary = binary;
        }

        public int getAddress() { return address; }
        public String getSource() { return source; }
        public String getBinary() { return binary; }
    }

    public static class AssemblyResult {
        private final boolean success;
        private final String message;
        private final List<AssemblyLine> lines;

        private AssemblyResult(boolean success, String message, List<AssemblyLine> lines) {
            this.success = success;
            this.message = message;
            this.lines = lines;
        }

        public static AssemblyResult success(List<AssemblyLine> lines) {
            return new AssemblyResult(true, "Programa montado e carregado com sucesso.", lines);
        }

        public static AssemblyResult error(String message) {
            return new AssemblyResult(false, message, new ArrayList<>());
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<AssemblyLine> getLines() { return lines; }
    }
}
