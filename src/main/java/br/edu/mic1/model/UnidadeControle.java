package br.edu.mic1.model;

import br.edu.mic1.model.register.Amask;
import br.edu.mic1.model.register.MicroinstructionRegister;
import br.edu.mic1.model.register.MicroprogramCounter;
import br.edu.mic1.model.register.RegisterFile;
import br.edu.mic1.model.register.Smask;
import br.edu.mic1.model.register.StatusRegister;
import br.edu.mic1.model.alu.Datapath;
import br.edu.mic1.model.alu.ULA;
import br.edu.mic1.model.memory.MemoryController;
import br.edu.mic1.service.InstructionDecoder;

public class UnidadeControle {
    private final MemoriaControle memoriaControle;
    private final MemoriaPrincipal memoria;
    private final MicroprogramCounter mpc = new MicroprogramCounter();
    private final MicroinstructionRegister mir = new MicroinstructionRegister();
    private final Amask amask = new Amask();
    private final Smask smask = new Smask();
    private final RegisterFile registerFile = new RegisterFile();
    private final StatusRegister statusRegister = new StatusRegister();
    private final MemoryController memoryController;
    private final InstructionDecoder instructionDecoder = new InstructionDecoder();
    private final Datapath datapath = new Datapath(registerFile, statusRegister);
    private final SpecialMicroinstructionExecutor specialExecutor;
    private Microinstruction microinstruction = Microinstruction.nop();
    private int[] barramentoC = new int[16];
    private int microprogramCounterExecutado;
    private boolean halted;

    public UnidadeControle(MemoriaControle memoriaControle, MemoriaPrincipal memoria) {
        if (memoriaControle == null || memoria == null) {
            throw new IllegalArgumentException("As memórias da unidade de controle são obrigatórias.");
        }
        this.memoriaControle = memoriaControle;
        this.memoria = memoria;
        memoryController = new MemoryController(memoria);
        specialExecutor = new SpecialMicroinstructionExecutor(registerFile, memoryController);
    }

    public void ciclo() {
        if (halted) {
            return;
        }
        microprogramCounterExecutado = bitsToInt(mpc.getValor());
        microinstruction = memoriaControle.read(microprogramCounterExecutado);
        mir.setValor(microinstruction.bits());

        SpecialMicroinstructionExecutor.Result special =
                specialExecutor.execute(microprogramCounterExecutado,
                        bitsToInt(registerFile.get(3)));
        if (special.handled()) {
            halted = special.halted();
            if (!halted) {
                mpc.setValor(intToBits(special.nextMpc(), 8));
            }
            return;
        }

        barramentoC = datapath.execute(microinstruction);
        if (microinstruction.isEncode()) {
            datapath.writeDestination(microinstruction.getC(), barramentoC);
        }
        if (microinstruction.writesMar()) {
            registerFile.set(0, barramentoC);
        }
        if (microinstruction.writesMbr()) {
            registerFile.set(1, barramentoC);
        }
        if (microinstruction.readsMemory()) {
            int address = bitsToInt(datapath.selectMemoryAddress());
            registerFile.set(1, memoryController.read(address,
                    microprogramCounterExecutado == 1));
        }
        if (microinstruction.writesMemory()) {
            int address = bitsToInt(datapath.selectMemoryAddress());
            memoryController.write(address, registerFile.get(1));
        }
        if (microinstruction.isHalt()) {
            halted = true;
        } else {
            advanceMpc();
        }
    }

    public void reset() {
        mpc.reset();
        microinstruction = Microinstruction.nop();
        mir.setValor(new int[32]);
        barramentoC = new int[16];
        microprogramCounterExecutado = 0;
        halted = false;
        registerFile.reset();
        memoryController.clearCaches();
    }

    public void halt() {
        halted = true;
    }

    public void clearCaches() {
        memoryController.clearCaches();
    }

    public Microinstruction getMicroinstruction() { return microinstruction; }
    public int[] getBarramentoC() { return barramentoC.clone(); }
    public int getMpc() { return bitsToInt(mpc.getValor()); }
    public int getMicroprogramCounterExecutado() { return microprogramCounterExecutado; }
    public int[] getMir() { return mir.getValor(); }
    public int[] getMpcBits() { return mpc.getValor(); }
    public int[] getAmask() { return amask.getValor(); }
    public int[] getSmask() { return smask.getValor(); }
    public int[] getZero() { return statusRegister.getZero(); }
    public int[] getMenosUm() { return statusRegister.getNegative(); }
    public boolean isHalted() { return halted; }
    public boolean isZero() { return statusRegister.isZero(); }
    public int[] getUlaD() { return ULA.getInstance().getD(); }
    public long getInstructionCacheHits() { return memoryController.getInstructionCacheHits(); }
    public long getInstructionCacheMisses() { return memoryController.getInstructionCacheMisses(); }
    public long getDataCacheHits() { return memoryController.getDataCacheHits(); }
    public long getDataCacheMisses() { return memoryController.getDataCacheMisses(); }

    public void registerMacroInstruction(int opcode, int microaddress) {
        instructionDecoder.registerMacroInstruction(opcode, microaddress);
    }

    public void setOutputPointer(int address) {
        if (address < 0 || address >= memoria.size()) {
            throw new IllegalArgumentException("Endereço de saída inválido: " + address);
        }
        registerFile.set(9, intToBits(address, 16));
    }

    public int[] getRegistrador(int indice) {
        return registerFile.get(indice);
    }

    public void setRegistrador(int indice, int[] valor) {
        registerFile.set(indice, valor);
    }

    private void advanceMpc() {
        if (microinstruction.isDispatch()) {
            int ir = bitsToInt(registerFile.get(3)) & 0xFFFF;
            mpc.setValor(intToBits(instructionDecoder.decode(ir), 8));
            return;
        }
        boolean condition = switch (microinstruction.getCondition()) {
            case NEVER -> false;
            case N -> statusRegister.isNegative();
            case Z -> statusRegister.isZero();
            case ALWAYS -> true;
        };
        int next = condition ? microinstruction.getAddress() : getMpc() + 1;
        if (next >= memoriaControle.size()) {
            next = 0;
        }
        mpc.setValor(intToBits(next, 8));
    }

    private static int bitsToInt(int[] bits) {
        int value = 0;
        for (int bit : bits) {
            value = (value << 1) | bit;
        }
        return value;
    }

    private static int[] intToBits(int value, int size) {
        int[] bits = new int[size];
        for (int i = size - 1; i >= 0; i--) {
            bits[i] = value & 1;
            value >>>= 1;
        }
        return bits;
    }
}
