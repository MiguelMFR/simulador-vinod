package br.edu.mic1.model;

import br.edu.mic1.model.memory.MemoryController;
import br.edu.mic1.model.alu.Datapath;
import br.edu.mic1.model.register.RegisterFile;

public class SpecialMicroinstructionExecutor {
    public record Result(boolean handled, int nextMpc, boolean halted) {
        public static Result notHandled() {
            return new Result(false, 0, false);
        }
    }

    private final RegisterFile registers;
    private final MemoryController memory;

    public SpecialMicroinstructionExecutor(RegisterFile registers, MemoryController memory) {
        this.registers = registers;
        this.memory = memory;
    }

    public Result execute(int mpc, int instruction) {
        int operand12 = signedOperand12(instruction);
        switch (mpc) {
            case 96:
                if (isAllZero(registers.get(5))) {
                    registers.set(2, Datapath.bits(operand12));
                }
                return handled(0);
            case 128:
                int[] accumulator = registers.get(5);
                if (accumulator[0] == 0 && !isAllZero(accumulator)) {
                    registers.set(2, Datapath.bits(operand12));
                }
                return handled(0);
            case 144:
                if (registers.get(5)[0] == 1) {
                    registers.set(2, Datapath.bits(operand12));
                }
                return handled(0);
            case 160:
                if (!isAllZero(registers.get(5))) {
                    registers.set(2, Datapath.bits(operand12));
                }
                return handled(0);
            case 176:
                return new Result(true, 0, true);
            case 192:
                registers.set(1, registers.get(2));
                registers.set(0, registers.get(4));
                return handled(193);
            case 193:
                memory.write(Datapath.bitsToInt(registers.get(0)), registers.get(1));
                return handled(194);
            case 194:
                registers.set(4, Datapath.bits(Datapath.bitsToInt(registers.get(4)) - 1));
                return handled(195);
            case 195:
                registers.set(2, Datapath.bits(instruction & 0x03FF));
                return handled(0);
            case 208:
                registers.set(4, Datapath.bits(Datapath.bitsToInt(registers.get(4)) + 1));
                return handled(209);
            case 209:
                registers.set(0, registers.get(4));
                return handled(210);
            case 210:
                registers.set(1, memory.read(
                        Datapath.bitsToInt(registers.get(0)), false));
                return handled(211);
            case 211:
                registers.set(2, registers.get(1));
                return handled(0);
            case 212:
                int[] temporary = registers.get(5);
                registers.set(5, registers.get(4));
                registers.set(4, temporary);
                return handled(0);
            case 224:
                registers.set(1, registers.get(5));
                registers.set(0, registers.get(4));
                return handled(225);
            case 225:
                memory.write(Datapath.bitsToInt(registers.get(0)), registers.get(1));
                return handled(226);
            case 226:
                registers.set(4, Datapath.bits(Datapath.bitsToInt(registers.get(4)) - 1));
                return handled(227);
            case 227:
                return handled(0);
            case 228:
                registers.set(4, Datapath.bits(Datapath.bitsToInt(registers.get(4)) + 1));
                return handled(229);
            case 229:
                registers.set(0, registers.get(4));
                return handled(230);
            case 230:
                registers.set(1, memory.read(
                        Datapath.bitsToInt(registers.get(0)), false));
                return handled(231);
            case 231:
                registers.set(5, registers.get(1));
                return handled(0);
            case 196:
                registers.set(0, registers.get(5));
                return handled(197);
            case 197:
                registers.set(1, memory.read(
                        Datapath.bitsToInt(registers.get(0)), false));
                return handled(198);
            case 198:
                registers.set(0, registers.get(4));
                memory.write(Datapath.bitsToInt(registers.get(0)), registers.get(1));
                return handled(199);
            case 199:
                registers.set(4, Datapath.bits(Datapath.bitsToInt(registers.get(4)) - 1));
                return handled(0);
            case 200:
                registers.set(4, Datapath.bits(Datapath.bitsToInt(registers.get(4)) + 1));
                return handled(201);
            case 201:
                registers.set(0, registers.get(4));
                registers.set(1, memory.read(
                        Datapath.bitsToInt(registers.get(0)), false));
                return handled(202);
            case 202:
                registers.set(0, registers.get(5));
                memory.write(Datapath.bitsToInt(registers.get(0)), registers.get(1));
                return handled(203);
            case 203:
                return handled(0);
            case 232:
                registers.set(4, Datapath.bits(
                        Datapath.bitsToInt(registers.get(4)) - (instruction & 0xFF)));
                return handled(0);
            case 236:
                registers.set(4, Datapath.bits(
                        Datapath.bitsToInt(registers.get(4)) + (instruction & 0xFF)));
                return handled(0);
            case 240:
                registers.set(0, Datapath.bits(
                        Datapath.bitsToInt(registers.get(4)) + operand12));
                return handled(241);
            case 241:
                registers.set(1, memory.read(
                        Datapath.bitsToInt(registers.get(0)), false));
                return handled(242);
            case 242:
                registers.set(5, registers.get(1));
                return handled(243);
            case 243:
                return handled(0);
            case 244:
                registers.set(0, Datapath.bits(
                        Datapath.bitsToInt(registers.get(4)) + operand12));
                return handled(245);
            case 245:
                registers.set(1, registers.get(5));
                return handled(246);
            case 246:
                memory.write(Datapath.bitsToInt(registers.get(0)), registers.get(1));
                return handled(247);
            case 247:
                return handled(0);
            case 248:
                registers.set(0, Datapath.bits(
                        Datapath.bitsToInt(registers.get(4)) + operand12));
                return handled(249);
            case 249:
                registers.set(1, memory.read(
                        Datapath.bitsToInt(registers.get(0)), false));
                return handled(250);
            case 250:
                registers.set(5, Datapath.bits(
                        Datapath.bitsToInt(registers.get(5))
                                + Datapath.bitsToInt(registers.get(1))));
                return handled(251);
            case 251:
                return handled(0);
            case 252:
                registers.set(0, Datapath.bits(
                        Datapath.bitsToInt(registers.get(4)) + operand12));
                return handled(253);
            case 253:
                registers.set(1, memory.read(
                        Datapath.bitsToInt(registers.get(0)), false));
                return handled(254);
            case 254:
                registers.set(5, Datapath.bits(
                        Datapath.bitsToInt(registers.get(5))
                                - Datapath.bitsToInt(registers.get(1))));
                return handled(255);
            case 255:
                return handled(0);
            default:
                return Result.notHandled();
        }
    }

    private static Result handled(int nextMpc) {
        return new Result(true, nextMpc, false);
    }

    private static int signedOperand12(int instruction) {
        int operand = instruction & 0x0FFF;
        return (operand & 0x0800) != 0 ? operand - 0x1000 : operand;
    }

    private static boolean isAllZero(int[] value) {
        for (int bit : value) {
            if (bit != 0) {
                return false;
            }
        }
        return true;
    }
}
