package atm.server.db;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class BillsTable {

    private static final int DEFAULT_BILLS = 200;
    private static final int FIVES = 5;
    private static final int TENS = 10;
    private static final int TWENTIES = 20;
    private static final int FIFTIES = 50;

    private static Map<Integer, Integer> dollarBills = new TreeMap<Integer, Integer>(Collections.reverseOrder()) {{
        put(FIVES, DEFAULT_BILLS);
        put(TENS, DEFAULT_BILLS);
        put(TWENTIES, DEFAULT_BILLS);
        put(FIFTIES, DEFAULT_BILLS);
    }};

    public Map<Integer, Integer> getAllAmounts() {
        return dollarBills;
    }

    public int getDenomAmount(int denom) {
        return dollarBills.get(denom);
    }

    public static void resetBills() {
        for (Map.Entry<Integer, Integer> entry : dollarBills.entrySet()) {
            entry.setValue(DEFAULT_BILLS);
        }
    }

    public boolean hasEnough(double amount) {
        return calculateTotal() >= amount;
    }

    public boolean hasEnough() {
        for (Map.Entry<Integer, Integer> e : dollarBills.entrySet()) {
            if (e.getValue() < 20) { return false; }
        } return true;
    }

    private int calculateTotal() {
        int total = 0;
        for (Map.Entry<Integer, Integer> entry : dollarBills.entrySet()) {
            total += (entry.getValue() * entry.getKey());
        }
        return total;
    }
}
