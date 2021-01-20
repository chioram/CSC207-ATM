package atm.model;

import java.io.IOException;

public class TransactionModel {
    // public static final long ATM_ACCOUNT_ID = -1;
    // If src = ATM_ACCOUNT_ID the transaction represents deposit in into destAccountId
    // If dest = ATM_ACCOUNT_ID the transaction represents withrdrawal from srcAccountId to ATM
    private final long id;
    private final long userId; // Transaction Id
    private final long srcAccountId;
    private final long destAccountId;
    private final double amount;

    public TransactionModel(long id, long userId, long srcAccountId, long destAccountId, double amount) {
        this.id = id;
        this.userId = userId;
        this.srcAccountId = srcAccountId;
        this.destAccountId = destAccountId;
        this.amount = amount;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public long getSrcAccountId() {
        return srcAccountId;
    }

    public long getDestAccountId() {
        return destAccountId;
    }

    public double getAmount() {
        return amount;
    }

    public String toCSVRowString() {
        return id + "," + userId + "," + srcAccountId + "," + destAccountId + "," + amount;
    }

    public static TransactionModel fromCSVRowString(String row) throws IOException {
        String[] cells = row.split(","); // We expect 5 strings.
        if (cells.length != 5) throw new IOException("Incorrect number of cells in a row!");
        return new TransactionModel(
                Long.parseLong(cells[0]), // transaction id
                Long.parseLong(cells[1]), // user id
                Long.parseLong(cells[2]), // src acc id
                Long.parseLong(cells[3]), // dest acc id
                Double.parseDouble(cells[4])); // type "1" -> 1 -> AccountType.Savings
    }
}
