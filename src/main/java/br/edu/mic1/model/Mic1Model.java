package br.edu.mic1.model;

import br.edu.mic1.model.memory.Memoria;
import java.util.List;

public class Mic1Model {
    private final MemoriaPrincipal memoria;
    private final MemoriaControle memoriaControle;
    private final UnidadeControle unidadeControle;
    private final Memoria memoriaLegada;
    private int loadedProgramLength;
    private int executableInstructionLength;
    private int[] initialDataWords = new int[0];

    public Mic1Model(int memoriaSize, int memoriaControleSize) {
        memoria = new MemoriaPrincipal(memoriaSize);
        memoriaLegada = new Memoria(memoria);
        memoriaControle = new MemoriaControle(memoriaControleSize);
        unidadeControle = new UnidadeControle(memoriaControle, memoria);
        carregarMicroprogramaInicial();
    }

    public void step() {
        if (unidadeControle.isHalted()) {
            return;
        }
        int pc = bitsToInt(unidadeControle.getRegistrador(2));
        if (loadedProgramLength > 0
                && pc >= executableInstructionLength
                && unidadeControle.getMpc() == 0) {
            unidadeControle.halt();
            return;
        }
        unidadeControle.ciclo();
    }

    public void reset() {
        unidadeControle.reset();
        for (int i = 0; i < initialDataWords.length; i++) {
            int address = executableInstructionLength + i;
            memoria.write(address, intToBits(initialDataWords[i]));
        }
    }

    public void loadProgram(List<Integer> machineCode) {
        loadProgram(machineCode, machineCode == null ? 0 : machineCode.size());
    }

    public void loadProgram(List<Integer> machineCode, int executableLength) {
        if (machineCode == null) {
            throw new IllegalArgumentException("O programa montado nao pode ser nulo.");
        }
        if (executableLength < 0 || executableLength > machineCode.size()) {
            throw new IllegalArgumentException("Quantidade de instrucoes invalida.");
        }
        if (machineCode.size() > memoria.size()) {
            throw new IllegalArgumentException("O programa excede o tamanho da memoria principal.");
        }
        memoria.clear();
        unidadeControle.reset();
        for (int address = 0; address < machineCode.size(); address++) {
            Integer word = machineCode.get(address);
            if (word == null || word < 0 || word > 0xFFFF) {
                throw new IllegalArgumentException("Palavra invalida no endereco " + address + ".");
            }
            memoria.write(address, intToBits(word));
        }
        loadedProgramLength = machineCode.size();
        executableInstructionLength = executableLength;
        initialDataWords = new int[machineCode.size() - executableLength];
        for (int i = executableLength; i < machineCode.size(); i++) {
            initialDataWords[i - executableLength] = machineCode.get(i);
        }
        unidadeControle.reset();
    }

    public MemoriaPrincipal getMemoria() {
        return memoria;
    }

    public Memoria getMemoriaLegada() {
        return memoriaLegada;
    }

    public MemoriaControle getMemoriaControle() {
        return memoriaControle;
    }

    public UnidadeControle getUnidadeControle() {
        return unidadeControle;
    }

    public boolean isHalted() {
        return unidadeControle.isHalted();
    }

    private void carregarMicroprogramaInicial() {
        memoriaControle.write(0, new Microinstruction(false, Microinstruction.Condition.NEVER,
                Microinstruction.AluOperation.PASS_A, Microinstruction.Shift.NONE,
                false, true, false, false, true, 0, 0, 2, 0));
        memoriaControle.write(1, new Microinstruction(false, Microinstruction.Condition.NEVER,
                Microinstruction.AluOperation.PASS_A, Microinstruction.Shift.NONE,
                false, false, true, false, false, 0, 0, 0, 0));
        memoriaControle.write(2, new Microinstruction(false, Microinstruction.Condition.NEVER,
                Microinstruction.AluOperation.ADD, Microinstruction.Shift.NONE,
                false, false, false, false, true, 2, 8, 2, 0));
        memoriaControle.write(3, new Microinstruction(false, Microinstruction.Condition.NEVER,
                Microinstruction.AluOperation.PASS_A, Microinstruction.Shift.NONE,
                false, false, false, false, true, 3, 0, 1, 0));
        memoriaControle.write(4, Microinstruction.dispatch());

        // Microprogramas disponiveis para macroinstrucoes suportadas pelo datapath atual.
        carregarLODD();
        carregarSTOD();
        carregarADDD();
        carregarLOCO();
        carregarJUMP();
        carregarSUBD();
        carregarJZER();
        carregarOtherMacros();

        unidadeControle.registerMacroInstruction(0x0, 16);
        unidadeControle.registerMacroInstruction(0x1, 32);
        unidadeControle.registerMacroInstruction(0x2, 48);
        unidadeControle.registerMacroInstruction(0x7, 64);
        unidadeControle.registerMacroInstruction(0x6, 80);
        unidadeControle.registerMacroInstruction(0x3, 112);
        unidadeControle.registerMacroInstruction(0x5, 96);
    }

    private void carregarLODD() {
        memoriaControle.write(16, microIRToMar());
        memoriaControle.write(17, memoryRead());
        memoriaControle.write(18, destinationFromMbr(5));
        memoriaControle.write(19, alwaysFetch());
    }

    private void carregarSTOD() {
        memoriaControle.write(32, microIRToMar());
        memoriaControle.write(33, mbrFromAcc());
        memoriaControle.write(34, memoryWrite());
        memoriaControle.write(35, alwaysFetch());
    }

    private void carregarADDD() {
        memoriaControle.write(48, microIRToMar());
        memoriaControle.write(49, memoryRead());
        memoriaControle.write(50, new Microinstruction(false, Microinstruction.Condition.NEVER,
                Microinstruction.AluOperation.ADD, Microinstruction.Shift.NONE,
                false, false, false, false, true, 5, 1, 5, 0));
        memoriaControle.write(51, alwaysFetch());
    }

    private void carregarLOCO() {
        memoriaControle.write(64, new Microinstruction(false, Microinstruction.Condition.NEVER,
                Microinstruction.AluOperation.PASS_A, Microinstruction.Shift.NONE,
                false, false, false, false, true, 5, 0, 3, 0));
        memoriaControle.write(65, alwaysFetch());
    }

    private void carregarJUMP() {
        memoriaControle.write(80, new Microinstruction(false, Microinstruction.Condition.NEVER,
                Microinstruction.AluOperation.PASS_A, Microinstruction.Shift.NONE,
                false, false, false, false, true, 2, 0, 3, 0));
        memoriaControle.write(81, alwaysFetch());
    }

    private void carregarSUBD() {
        memoriaControle.write(112, microIRToMar());
        memoriaControle.write(113, memoryRead());
        memoriaControle.write(114, new Microinstruction(false, Microinstruction.Condition.NEVER,
                Microinstruction.AluOperation.NOT_A, Microinstruction.Shift.NONE,
                false, false, false, false, true, 7, 0, 1, 0));
        memoriaControle.write(115, new Microinstruction(false, Microinstruction.Condition.NEVER,
                Microinstruction.AluOperation.ADD, Microinstruction.Shift.NONE,
                false, false, false, false, true, 7, 8, 7, 0));
        memoriaControle.write(116, new Microinstruction(false, Microinstruction.Condition.NEVER,
                Microinstruction.AluOperation.ADD, Microinstruction.Shift.NONE,
                false, false, false, false, true, 5, 7, 5, 0));
        memoriaControle.write(117, alwaysFetch());
    }

    private void carregarJZER() {
        memoriaControle.write(96, new Microinstruction(false, Microinstruction.Condition.NEVER,
                Microinstruction.AluOperation.PASS_A, Microinstruction.Shift.NONE,
                false, false, false, false, false, 0, 0, 0, 0));
    }

    private void carregarOtherMacros() {
        for (int address = 128; address <= 255; address++) {
            memoriaControle.write(address, Microinstruction.nop());
        }
        unidadeControle.registerMacroInstruction(0x4, 128);
        unidadeControle.registerMacroInstruction(0xC, 144);
        unidadeControle.registerMacroInstruction(0xD, 160);
        unidadeControle.registerMacroInstruction(0xE, 192);
        unidadeControle.registerMacroInstruction(0xF0, 196);
        unidadeControle.registerMacroInstruction(0xF2, 200);
        unidadeControle.registerMacroInstruction(0xF4, 224);
        unidadeControle.registerMacroInstruction(0xF6, 228);
        unidadeControle.registerMacroInstruction(0xF8, 208);
        unidadeControle.registerMacroInstruction(0xFA, 212);
        unidadeControle.registerMacroInstruction(0xFC, 232);
        unidadeControle.registerMacroInstruction(0xFE, 236);
        unidadeControle.registerMacroInstruction(0x8, 240);
        unidadeControle.registerMacroInstruction(0x9, 244);
        unidadeControle.registerMacroInstruction(0xA, 248);
        unidadeControle.registerMacroInstruction(0xB, 252);
    }

    private Microinstruction microIRToMar() {
        return new Microinstruction(false, Microinstruction.Condition.NEVER,
                Microinstruction.AluOperation.PASS_A, Microinstruction.Shift.NONE,
                false, true, false, false, true, 0, 0, 3, 0);
    }

    private Microinstruction memoryRead() {
        return new Microinstruction(false, Microinstruction.Condition.NEVER,
                Microinstruction.AluOperation.PASS_A, Microinstruction.Shift.NONE,
                false, false, true, false, false, 0, 0, 0, 0);
    }

    private Microinstruction memoryWrite() {
        return new Microinstruction(false, Microinstruction.Condition.NEVER,
                Microinstruction.AluOperation.PASS_A, Microinstruction.Shift.NONE,
                false, false, false, true, false, 0, 0, 0, 0);
    }

    private Microinstruction mbrFromAcc() {
        return new Microinstruction(false, Microinstruction.Condition.NEVER,
                Microinstruction.AluOperation.PASS_A, Microinstruction.Shift.NONE,
                true, false, false, false, false, 0, 0, 5, 0);
    }

    private Microinstruction destinationFromMbr(int destination) {
        return new Microinstruction(false, Microinstruction.Condition.NEVER,
                Microinstruction.AluOperation.PASS_A, Microinstruction.Shift.NONE,
                false, false, false, false, true, destination, 0, 1, 0);
    }

    private Microinstruction alwaysFetch() {
        return new Microinstruction(false, Microinstruction.Condition.ALWAYS,
                Microinstruction.AluOperation.PASS_A, Microinstruction.Shift.NONE,
                false, false, false, false, false, 0, 0, 0, 0);
    }

    private static int[] intToBits(int value) {
        int[] bits = new int[16];
        for (int i = 15; i >= 0; i--) {
            bits[i] = value & 1;
            value >>>= 1;
        }
        return bits;
    }

    private static int bitsToInt(int[] bits) {
        int value = 0;
        for (int bit : bits) {
            value = (value << 1) | bit;
        }
        return value;
    }
}
