package br.edu.mic1.model.cache;

public class DataCache extends Cache {
    public DataCache(int capacity) {
        super(capacity);
    }

    public void writeWord(int address, int[] word) {
        validateAddress(address);
        validateWord(word);
        if (containsWord(address)) {
            recordHit();
        } else {
            recordMiss();
        }
        loadWord(address, word);
    }
}
