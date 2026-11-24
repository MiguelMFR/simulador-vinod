package br.edu.mic1.model.register;

import java.util.LinkedHashMap;
import java.util.Map;

public class RegisterFile {
    private final Map<Integer, Registrador> registers = new LinkedHashMap<>();

    public RegisterFile() {
        registers.put(0, new MemoryAddressRegister());
        registers.put(1, new MemoryBufferRegisterRead());
        registers.put(2, new ProgramCounter());
        registers.put(3, new InstructionRegister());
        registers.put(4, new StackPointer());
        registers.put(5, new Acumulador());
        registers.put(6, new TemporaryInstructionRegister());
        registers.put(7, new Registrador(16));
        registers.put(8, new RegistradorUm());
        for (int i = 9; i < 16; i++) {
            registers.put(i, new Registrador(16));
        }
        reset();
    }

    public int[] get(int index) {
        return register(index).getValor();
    }

    public void set(int index, int[] value) {
        if (index == 8) {
            throw new IllegalArgumentException("O registrador R8 é constante.");
        }
        register(index).setValor(value);
    }

    public void reset() {
        for (Map.Entry<Integer, Registrador> entry : registers.entrySet()) {
            if (entry.getKey() != 8) {
                entry.getValue().setValor(new int[16]);
            }
        }
        registers.get(6).setValor(new RegistradorUm().getValor());
        registers.get(4).setValor(new StackPointer().getValor());
    }

    private Registrador register(int index) {
        Registrador register = registers.get(index);
        if (register == null) {
            throw new IllegalArgumentException("Registrador inexistente: " + index);
        }
        return register;
    }
}
