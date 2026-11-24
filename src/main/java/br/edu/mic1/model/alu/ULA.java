package br.edu.mic1.model.alu;

public class ULA {
    private int[] A, B, F, D, R;
    private static ULA ula = null;

    private ULA() {
        D = new int[2]; // aqui fica o NZ
        R = new int[16];
    }

    public static ULA getInstance() {
        if (ula == null) {
            ula = new ULA();
        }
        return ula;
    }
    
    public void perform(){
        validarEntradas();
        // implementação da ULA

        // a operação depende de F, que é o código da operação a ser realizada
        // a operação é realizada com A e B e armazenada em R
        // o valor D armazena o N (negativo) e Z (zero)

        // soma
        if (F[0] == 0 && F[1] == 0) {
            int carry = 0;
            for (int i = 15; i >= 0; i--) {
                int sum = A[i] + B[i] + carry;
                R[i] = sum % 2;
                carry = sum / 2;
            }
        }

        // AND 
        if (F[0] == 0 && F[1] == 1) {
            for (int i = 0; i < 16; i++) {
                // se A e B forem 1, então R é 1, caso contrário R é 0
                R[i] = A[i] & B[i]; 
            }
        }

        // IDENTIDADE
        if (F[0] == 1 && F[1] == 0) {
            for (int i = 0; i < 16; i++) {
                // se A for 1, então R é 1, caso contrário R é 0
                R[i] = A[i];
            }
        }

        // INV
        if (F[0] == 1 && F[1] == 1) {
            for (int i = 0; i < 16; i++) {
                // se A for 1, então R é 0, caso contrário R é 1
                R[i] = A[i] ^ 1; // isso aqui é o XOR, nem sabia que existia brabo dms
            }
        }

        // calc de D
        D[0] = R[0]; // N é o bit mais significativo de R

        D[1] = 1;

        for (int i = 0; i < 16; i++) {
            if (R[i] != 0) {
                D[1] = 0; // Z flag muda pra 0 se algum bit de R for 1
                break;
            }
        }

    
    }

    public void setA(int[] A) {
        this.A = validarPalavra(A);
    }

    public void setB(int[] B) {
        this.B = validarPalavra(B);
    }

    public void setF(int[] F) {
        if (F == null || F.length != 2 || (F[0] != 0 && F[0] != 1)
                || (F[1] != 0 && F[1] != 1)) {
            throw new IllegalArgumentException("A operação da ULA deve conter dois bits.");
        }
        this.F = F.clone();
    }

    public int[] getA() {
        return A == null ? null : A.clone();
    }

    public int[] getB() {
        return B == null ? null : B.clone();
    }

    public int[] getF() {
        return F == null ? null : F.clone();
    }

    public int[] getD() {
        return D.clone();
    }

    public int[] getR() {
        return R.clone();
    }

    private void validarEntradas() {
        if (A == null || B == null || F == null) {
            throw new IllegalStateException("A, B e F devem ser definidos antes da operação.");
        }
    }

    private static int[] validarPalavra(int[] palavra) {
        if (palavra == null || palavra.length != 16) {
            throw new IllegalArgumentException("A palavra da ULA deve conter 16 bits.");
        }
        int[] copia = palavra.clone();
        for (int bit : copia) {
            if (bit != 0 && bit != 1) {
                throw new IllegalArgumentException("A palavra da ULA aceita somente bits 0 ou 1.");
            }
        }
        return copia;
    }
}