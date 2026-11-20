package br.edu.mic1.model.register;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistradorTest {

    @Test
    void shouldInitializeWithZerosOfGivenSize() {
        Registrador reg = new Registrador(16);

        assertArrayEquals(new int[16], reg.getValor());
    }

    @Test
    void shouldSetValuesAndIgnoreNullEntries() {
        Registrador reg = new Registrador(4);
        reg.setValor(new Integer[]{1, null, 0, 1});

        assertArrayEquals(new int[]{1, 0, 0, 1}, reg.getValor());
    }

    @Test
    void shouldInitializeRegistradorZeroWithSixteenZeros() {
        RegistradorZero zero = new RegistradorZero();

        assertArrayEquals(new int[16], zero.getValor());
    }

    @Test
    void shouldInitializeRegistradorUmWithOneInLastPosition() {
        RegistradorUm um = new RegistradorUm();
        int[] expected = new int[16];
        expected[15] = 1;

        assertArrayEquals(expected, um.getValor());
    }

    @Test
    void shouldAllowOverwritingConstantRegisterViaInheritedSetValor() {
        RegistradorZero zero = new RegistradorZero();
        zero.setValor(new Integer[]{1, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null});

        assertEquals(1, zero.getValor()[0]);
    }

    @Test
    void shouldInitializeAmaskWithEightOnesInLeastSignificantBits() {
        Amask amask = new Amask();
        int[] expected = new int[]{
                0, 0, 0, 0, 0, 0, 0, 0,
                1, 1, 1, 1, 1, 1, 1, 1
        };

        assertArrayEquals(expected, amask.getValor());
    }

    @Test
    void shouldInitializeSmaskWithFourOnesInLeastSignificantBits() {
        Smask smask = new Smask();
        int[] expected = new int[]{
                0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 1, 1, 1, 1
        };

        assertArrayEquals(expected, smask.getValor());
    }

    @Test
    void shouldInitializeStackPointerWithExpectedBitPattern() {
        StackPointer sp = new StackPointer();
        int[] expected = new int[]{
                0, 0, 0, 1, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0
        };

        assertArrayEquals(expected, sp.getValor());
    }
}