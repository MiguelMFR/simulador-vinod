package br.edu.mic1.model.cache;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CacheTest {

    private static int[] wordOf(int... bits) {
        assertEquals(16, bits.length, "Helper de teste exige exatamente 16 bits.");
        return bits;
    }

    private static int[] zeros() {
        return new int[16];
    }

    private static int[] ones() {
        return new int[]{
                1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1
        };
    }

    @Test
    void shouldRejectNonPositiveCapacity() {
        assertThrows(IllegalArgumentException.class, () -> new Cache(0));
        assertThrows(IllegalArgumentException.class, () -> new Cache(-4));
        assertThrows(IllegalArgumentException.class, () -> new InstructionCache(0));
        assertThrows(IllegalArgumentException.class, () -> new DataCache(-1));
    }

    @Test
    void shouldRejectNegativeAddress() {
        Cache cache = new Cache(4);
        int[] word = zeros();

        assertThrows(IndexOutOfBoundsException.class, () -> cache.readWord(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> cache.loadWord(-1, word));

        DataCache dataCache = new DataCache(4);
        assertThrows(IndexOutOfBoundsException.class, () -> dataCache.writeWord(-2, word));
    }

    @Test
    void shouldRejectInvalidWords() {
        Cache cache = new Cache(4);

        assertThrows(IllegalArgumentException.class, () -> cache.loadWord(0, null));
        assertThrows(IllegalArgumentException.class, () -> cache.loadWord(0, new int[15]));
        assertThrows(IllegalArgumentException.class, () -> cache.loadWord(0, new int[17]));

        int[] invalidBitWord = zeros();
        invalidBitWord[5] = 2;
        assertThrows(IllegalArgumentException.class, () -> cache.loadWord(0, invalidBitWord));

        DataCache dataCache = new DataCache(4);
        assertThrows(IllegalArgumentException.class, () -> dataCache.writeWord(0, invalidBitWord));
    }

    @Test
    void shouldRecordMissThenHitOnReadAfterLoad() {
        InstructionCache cache = new InstructionCache(4);

        Optional<int[]> firstRead = cache.readWord(2);
        assertTrue(firstRead.isEmpty());
        assertEquals(0, cache.getHitCount());
        assertEquals(1, cache.getMissCount());

        int[] expected = wordOf(
                0, 1, 0, 1, 0, 1, 0, 1,
                1, 0, 1, 0, 1, 0, 1, 0
        );
        cache.loadWord(2, expected);

        Optional<int[]> secondRead = cache.readWord(2);
        assertTrue(secondRead.isPresent());
        assertArrayEquals(expected, secondRead.get());
        assertEquals(1, cache.getHitCount());
        assertEquals(1, cache.getMissCount());
    }

    @Test
    void shouldIsolateArraysFromExternalMutation() {
        Cache cache = new Cache(4);
        int[] original = zeros();
        cache.loadWord(1, original);

        // Mutar o array original depois de carregar não deve alterar a cache
        original[0] = 1;

        Optional<int[]> read1 = cache.readWord(1);
        assertTrue(read1.isPresent());
        assertEquals(0, read1.get()[0]);

        // Mutar o array retornado por readWord não deve alterar leituras futuras
        read1.get()[0] = 1;

        Optional<int[]> read2 = cache.readWord(1);
        assertTrue(read2.isPresent());
        assertEquals(0, read2.get()[0]);
    }

    @Test
    void shouldHandleDirectMappingIndexAndTagCollisions() {
        // Capacidade 4: endereços 0 e 4 têm index = 0, mas tags diferentes (0 vs 1)
        Cache cache = new Cache(4);
        int[] wordAt0 = zeros();
        int[] wordAt4 = ones();

        cache.loadWord(0, wordAt0);
        cache.loadWord(4, wordAt4);

        // Endereço 0 foi sobrescrito na linha 0 -> deve ser miss
        assertTrue(cache.readWord(0).isEmpty());

        // Endereço 4 continua na linha 0 com tag 1 -> deve ser hit
        Optional<int[]> at4 = cache.readWord(4);
        assertTrue(at4.isPresent());
        assertArrayEquals(wordAt4, at4.get());
    }

    @Test
    void shouldUpdateMetricsAndValueOnDataCacheWriteWord() {
        DataCache dataCache = new DataCache(4);
        int[] firstWord = zeros();
        int[] secondWord = ones();

        // Escrita em linha vazia -> miss, mas carrega localmente
        dataCache.writeWord(3, firstWord);
        assertEquals(0, dataCache.getHitCount());
        assertEquals(1, dataCache.getMissCount());

        Optional<int[]> afterFirstWrite = dataCache.readWord(3);
        assertTrue(afterFirstWrite.isPresent());
        assertArrayEquals(firstWord, afterFirstWrite.get());
        assertEquals(1, dataCache.getHitCount());
        assertEquals(1, dataCache.getMissCount());

        // Nova escrita no mesmo endereço (mesma tag) -> hit e atualiza valor
        dataCache.writeWord(3, secondWord);
        assertEquals(2, dataCache.getHitCount());
        assertEquals(1, dataCache.getMissCount());

        Optional<int[]> afterSecondWrite = dataCache.readWord(3);
        assertTrue(afterSecondWrite.isPresent());
        assertArrayEquals(secondWord, afterSecondWrite.get());
    }

    @Test
    void shouldKeepInstructionCacheAndDataCacheIndependent() {
        InstructionCache iCache = new InstructionCache(4);
        DataCache dCache = new DataCache(4);

        int[] instructionWord = zeros();
        int[] dataWord = ones();

        iCache.loadWord(0, instructionWord);
        dCache.writeWord(0, dataWord);

        assertArrayEquals(instructionWord, iCache.readWord(0).orElseThrow());
        assertArrayEquals(dataWord, dCache.readWord(0).orElseThrow());
    }
}
