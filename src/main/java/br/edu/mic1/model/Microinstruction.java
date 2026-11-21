package br.edu.mic1.model;

import java.util.Arrays;

/**
 * Microinstrução MIC-1 codificada em 32 bits.
 *
 * <p>O formato é AMUX(1), COND(2), ALU(2), SH(2), MBR(1), MAR(1),
 * RD(1), WR(1), ENC(1), C(4), B(4), A(4) e ADDR(8). O campo C é o
 * índice do registrador de destino.</p>
 */
public final class Microinstruction {
    public enum Condition { NEVER, N, Z, ALWAYS }
    public enum AluOperation { ADD, AND, PASS_A, NOT_A }
    public enum Shift { NONE, LEFT, RIGHT }

    private final boolean amux;
    private final Condition condition;
    private final AluOperation alu;
    private final Shift shift;
    private final boolean writeMbr;
    private final boolean writeMar;
    private final boolean read;
    private final boolean write;
    private final boolean encode;
    private final int c, b, a, address;

    public Microinstruction(boolean amux, Condition condition, AluOperation alu, Shift shift,
                            boolean writeMbr, boolean writeMar, boolean read, boolean write,
                            boolean encode, int c, int b, int a, int address) {
        this.amux = amux;
        this.condition = condition;
        this.alu = alu;
        this.shift = shift;
        this.writeMbr = writeMbr;
        this.writeMar = writeMar;
        this.read = read;
        this.write = write;
        this.encode = encode;
        this.c = field(c, 4, "C");
        this.b = field(b, 4, "B");
        this.a = field(a, 4, "A");
        this.address = field(address, 8, "ADDR");
    }

    public static Microinstruction nop() {
        return new Microinstruction(false, Condition.NEVER, AluOperation.PASS_A, Shift.NONE,
                false, false, false, false, false, 0, 0, 0, 0);
    }

    public static Microinstruction halt() {
        return new Microinstruction(false, Condition.NEVER, AluOperation.PASS_A, Shift.NONE,
                false, false, false, false, false, 0, 0, 0, 255);
    }

    public static Microinstruction dispatch() {
        return new Microinstruction(false, Condition.NEVER, AluOperation.PASS_A, Shift.NONE,
                false, false, false, false, false, 0, 0, 0, 254);
    }

    public int encode() {
        int value = (amux ? 1 : 0);
        value = (value << 2) | condition.ordinal();
        value = (value << 2) | alu.ordinal();
        value = (value << 2) | shift.ordinal();
        value = (value << 1) | bit(writeMbr);
        value = (value << 1) | bit(writeMar);
        value = (value << 1) | bit(read);
        value = (value << 1) | bit(write);
        value = (value << 1) | bit(encode);
        value = (value << 4) | c;
        value = (value << 4) | b;
        value = (value << 4) | a;
        return (value << 8) | address;
    }

    public int[] bits() {
        int[] result = new int[32];
        int value = encode();
        for (int i = 31; i >= 0; i--) {
            result[i] = value & 1;
            value >>>= 1;
        }
        return result;
    }

    public boolean isAmux() { return amux; }
    public Condition getCondition() { return condition; }
    public AluOperation getAlu() { return alu; }
    public Shift getShift() { return shift; }
    public boolean writesMbr() { return writeMbr; }
    public boolean writesMar() { return writeMar; }
    public boolean readsMemory() { return read; }
    public boolean writesMemory() { return write; }
    public boolean isEncode() { return encode; }
    public boolean isHalt() { return condition == Condition.NEVER && address == 255; }
    public boolean isDispatch() { return condition == Condition.NEVER && address == 254; }
    public int getC() { return c; }
    public int getB() { return b; }
    public int getA() { return a; }
    public int getAddress() { return address; }

    public static Microinstruction fromBits(int[] bits) {
        if (bits == null || bits.length != 32) {
            throw new IllegalArgumentException("A microinstrução deve conter 32 bits.");
        }
        int value = 0;
        for (int bit : bits) {
            if (bit != 0 && bit != 1) {
                throw new IllegalArgumentException("A microinstrução aceita somente bits 0 ou 1.");
            }
            value = (value << 1) | bit;
        }
        int address = value & 0xff; value >>>= 8;
        int a = value & 0xf; value >>>= 4;
        int b = value & 0xf; value >>>= 4;
        int c = value & 0xf; value >>>= 4;
        boolean encode = (value & 1) != 0; value >>>= 1;
        boolean write = (value & 1) != 0; value >>>= 1;
        boolean read = (value & 1) != 0; value >>>= 1;
        boolean writeMar = (value & 1) != 0; value >>>= 1;
        boolean writeMbr = (value & 1) != 0; value >>>= 1;
        Shift shift = Shift.values()[value & 3]; value >>>= 2;
        AluOperation alu = AluOperation.values()[value & 3]; value >>>= 2;
        Condition condition = Condition.values()[value & 3]; value >>>= 2;
        return new Microinstruction((value & 1) != 0, condition, alu, shift,
                writeMbr, writeMar, read, write, encode, c, b, a, address);
    }

    private static int field(int value, int bits, String name) {
        if (value < 0 || value >= (1 << bits)) {
            throw new IllegalArgumentException(name + " fora do intervalo.");
        }
        return value;
    }

    private static int bit(boolean value) { return value ? 1 : 0; }

    @Override
    public String toString() {
        return Arrays.toString(bits());
    }
}
