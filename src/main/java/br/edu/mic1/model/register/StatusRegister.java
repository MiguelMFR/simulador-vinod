package br.edu.mic1.model.register;

public class StatusRegister {
    private final RegistradorZero zero = new RegistradorZero();
    private final RegistradorMenosUm negative = new RegistradorMenosUm();

    public void update(int[] ulaFlags) {
        zero.setValor(ulaFlags[1] == 1 ? ones() : new int[16]);
        negative.setValor(ulaFlags[0] == 1 ? ones() : new int[16]);
    }

    public int[] getZero() {
        return zero.getValor();
    }

    public int[] getNegative() {
        return negative.getValor();
    }

    public boolean isZero() {
        return isAllOnes(zero.getValor());
    }

    public boolean isNegative() {
        return isAllOnes(negative.getValor());
    }

    private static int[] ones() {
        int[] value = new int[16];
        java.util.Arrays.fill(value, 1);
        return value;
    }

    private static boolean isAllOnes(int[] value) {
        for (int bit : value) {
            if (bit != 1) {
                return false;
            }
        }
        return true;
    }
}
