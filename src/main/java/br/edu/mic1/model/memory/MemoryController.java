package br.edu.mic1.model.memory;

import br.edu.mic1.model.cache.DataCache;
import br.edu.mic1.model.cache.InstructionCache;
import br.edu.mic1.model.MemoriaPrincipal;

public class MemoryController {
    private final MemoriaPrincipal memory;
    private final InstructionCache instructionCache;
    private final DataCache dataCache;

    public MemoryController(MemoriaPrincipal memory) {
        if (memory == null) {
            throw new IllegalArgumentException("A memória principal é obrigatória.");
        }
        this.memory = memory;
        instructionCache = new InstructionCache(memory.size());
        dataCache = new DataCache(memory.size());
    }

    public int[] read(int address, boolean instructionFetch) {
        java.util.Optional<int[]> cached = instructionFetch
                ? instructionCache.readWord(address)
                : dataCache.readWord(address);
        if (cached.isPresent()) {
            return cached.get();
        }
        int[] word = memory.read(address);
        if (instructionFetch) {
            instructionCache.loadWord(address, word);
        } else {
            dataCache.loadWord(address, word);
        }
        return word.clone();
    }

    public void write(int address, int[] word) {
        memory.write(address, word);
        dataCache.writeWord(address, word);
    }

    public void clearCaches() {
        instructionCache.clear();
        dataCache.clear();
    }

    public long getInstructionCacheHits() {
        return instructionCache.getHitCount();
    }

    public long getInstructionCacheMisses() {
        return instructionCache.getMissCount();
    }

    public long getDataCacheHits() {
        return dataCache.getHitCount();
    }

    public long getDataCacheMisses() {
        return dataCache.getMissCount();
    }
}
