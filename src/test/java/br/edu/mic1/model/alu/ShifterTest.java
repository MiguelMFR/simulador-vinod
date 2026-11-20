package br.edu.mic1.model.alu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShifterTest {

    private final Shifter shifter = new Shifter();

    @Test
    void shouldKeepInputUnchangedWhenControlIs00() {
        int[] input = {
                1, 0, 1, 0, 0, 1, 1, 0,
                1, 1, 0, 0, 0, 0, 1, 1
        };

        int[] result = shifter.shift(input, new int[]{0, 0});

        assertArrayEquals(input, result);
        assertNotSame(input, result);
    }

    @Test
    void shouldShiftRightByOneBitWithZeroAtIndexZero() {
        int[] input = {
                1, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 1
        };
        int[] expected = {
                0, 1, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0
        };

        int[] result = shifter.shift(input, new int[]{0, 1});

        assertArrayEquals(expected, result);
    }

    @Test
    void shouldShiftLeftByOneBitWithZeroAtIndexFifteen() {
        int[] input = {
                1, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 1
        };
        int[] expected = {
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 1, 0
        };

        int[] result = shifter.shift(input, new int[]{1, 0});

        assertArrayEquals(expected, result);
    }

    @Test
    void shouldKeepInputUnchangedWhenControlIs11() {
        int[] input = {
                0, 1, 0, 1, 1, 0, 1, 0,
                0, 0, 1, 1, 1, 1, 0, 0
        };

        int[] result = shifter.shift(input, new int[]{1, 1});

        assertArrayEquals(input, result);
    }

    @Test
    void shouldThrowExceptionWhenInputIsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> shifter.shift(new int[8], new int[]{0, 0}));
        assertThrows(IllegalArgumentException.class,
                () -> shifter.shift(null, new int[]{0, 0}));
    }
}
