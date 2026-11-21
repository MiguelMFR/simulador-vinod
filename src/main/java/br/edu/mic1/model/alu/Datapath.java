package br.edu.mic1.model.alu;

import br.edu.mic1.model.AMux;
import br.edu.mic1.model.MMux;
import br.edu.mic1.model.Microinstruction;
import br.edu.mic1.model.register.RegisterFile;
import br.edu.mic1.model.register.StatusRegister;
import br.edu.mic1.model.register.Shifter;

public class Datapath {
    private final RegisterFile registers;
    private final StatusRegister status;
    private final AMux amux = new AMux();
    private final MMux mmux = new MMux();
    private final ULA ula = ULA.getInstance();
    private final Shifter shifter = new Shifter();

    public Datapath(RegisterFile registers, StatusRegister status) {
        this.registers = registers;
        this.status = status;
    }

    public int[] execute(Microinstruction instruction) {
        int[] aFromRegister = registers.get(instruction.getA());
        if (instruction.getA() == 3) {
            aFromRegister = mask12Bits(aFromRegister);
        }
        amux.setEntrada(0, aFromRegister);
        amux.setEntrada(1, registers.get(1));
        amux.setControle(new int[] { instruction.isAmux() ? 1 : 0 });

        int[] b = registers.get(instruction.getB());
        if (instruction.getB() == 3) {
            b = mask12Bits(b);
        }
        int[] a = amux.selecionar();
        int[] function = switch (instruction.getAlu()) {
            case ADD -> new int[] { 0, 0 };
            case AND -> new int[] { 0, 1 };
            case PASS_A -> new int[] { 1, 0 };
            case NOT_A -> new int[] { 1, 1 };
        };
        ula.setA(a);
        ula.setB(b);
        ula.setF(function);
        ula.perform();
        int[] result = ula.getR();
        if (instruction.getAlu() == Microinstruction.AluOperation.ADD
                || instruction.getAlu() == Microinstruction.AluOperation.AND
                || instruction.getAlu() == Microinstruction.AluOperation.NOT_A) {
            status.update(ula.getD());
        }
        if (instruction.getShift() == Microinstruction.Shift.LEFT) {
            shifter.setValor(result);
            shifter.shiftLeft();
            result = shifter.getValor();
        } else if (instruction.getShift() == Microinstruction.Shift.RIGHT) {
            shifter.setValor(result);
            shifter.shiftRight();
            result = shifter.getValor();
        }
        return result;
    }

    public void writeDestination(int index, int[] value) {
        registers.set(index, value);
    }

    public int[] selectMemoryAddress() {
        mmux.setEntrada(0, registers.get(2));
        mmux.setEntrada(1, registers.get(0));
        mmux.setControle(new int[] { 1 });
        return mmux.selecionar();
    }

    public static int[] mask12Bits(int[] word) {
        if (word == null || word.length != 16) {
            throw new IllegalArgumentException("Palavra de 16 bits invalida.");
        }
        int value = bitsToInt(word) & 0x0FFF;
        if ((value & 0x0800) != 0) {
            value |= 0xF000;
        }
        return bits(value);
    }

    public static int bitsToInt(int[] bits) {
        int value = 0;
        for (int bit : bits) {
            value = (value << 1) | bit;
        }
        return value;
    }

    public static int[] bits(int value) {
        int[] result = new int[16];
        for (int i = result.length - 1; i >= 0; i--) {
            result[i] = value & 1;
            value >>>= 1;
        }
        return result;
    }
}
