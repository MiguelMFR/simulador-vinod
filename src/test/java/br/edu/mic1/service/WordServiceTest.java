package br.edu.mic1.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WordServiceTest {

    private final WordService wordService = new WordService();

    @Test
    void shouldValidateValidSixteenBitWord() {
        int[] word = new int[16];
        word[15] = 1;

        assertDoesNotThrow(() -> wordService.validateWord(word));
    }

    @Test
    void shouldThrowWhenWordIsNullOrWrongLength() {
        assertThrows(IllegalArgumentException.class, () -> wordService.validateWord(null));
        assertThrows(IllegalArgumentException.class, () -> wordService.validateWord(new int[15]));
        assertThrows(IllegalArgumentException.class, () -> wordService.validateWord(new int[17]));
    }

    @Test
    void shouldThrowWhenWordContainsInvalidBitValues() {
        int[] invalidBit = new int[16];
        invalidBit[3] = 2;

        int[] negativeBit = new int[16];
        negativeBit[0] = -1;

        assertThrows(IllegalArgumentException.class, () -> wordService.validateWord(invalidBit));
        assertThrows(IllegalArgumentException.class, () -> wordService.validateWord(negativeBit));
    }

    @Test
    void shouldConvertValidBinaryStringToSixteenBitArray() {
        String binary = "  0000000011111111  ";
        int[] expected = {
                0, 0, 0, 0, 0, 0, 0, 0,
                1, 1, 1, 1, 1, 1, 1, 1
        };

        assertArrayEquals(expected, wordService.fromBinaryString(binary));
    }

    @Test
    void shouldThrowWhenBinaryStringIsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> wordService.fromBinaryString(null));
        assertThrows(IllegalArgumentException.class, () -> wordService.fromBinaryString("1010"));
        assertThrows(IllegalArgumentException.class,
                () -> wordService.fromBinaryString("0000000000000002"));
    }

    @Test
    void shouldConvertSixteenBitArrayToBinaryString() {
        int[] word = {
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 1
        };

        assertEquals("0000000000000001", wordService.toBinaryString(word));
    }

    @Test
    void shouldConvertUnsignedIntToSixteenBitArray() {
        int[] expected255 = {
                0, 0, 0, 0, 0, 0, 0, 0,
                1, 1, 1, 1, 1, 1, 1, 1
        };
        int[] expectedZero = new int[16];
        int[] expectedMax = {
                1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 1, 1, 1
        };

        assertArrayEquals(expected255, wordService.fromUnsignedInt(255));
        assertArrayEquals(expectedZero, wordService.fromUnsignedInt(0));
        assertArrayEquals(expectedMax, wordService.fromUnsignedInt(65535));
    }

    @Test
    void shouldThrowWhenUnsignedIntIsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> wordService.fromUnsignedInt(-1));
        assertThrows(IllegalArgumentException.class, () -> wordService.fromUnsignedInt(65536));
    }
}
