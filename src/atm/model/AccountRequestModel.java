package atm.model;

import java.io.IOException;
import java.util.Date;

public class AccountRequestModel {
    private final long id;
    private final long requesterUserId;
    private final AccountModel.AccountType requestedAccountType;

    public AccountRequestModel(long id, long requesterUserId, AccountModel.AccountType requestedAccountType) {
        this.id = id;
        this.requesterUserId = requesterUserId;
        this.requestedAccountType = requestedAccountType;
    }

    public long getId() {
        return id;
    }

    public long getRequesterUserId() {
        return requesterUserId;
    }

    public AccountModel.AccountType getRequestedAccountType() {
        return requestedAccountType;
    }

    public String toCSVRowString() {
        return id + "," + requesterUserId + "," + requestedAccountType.getCode();
    }

    public static AccountRequestModel fromCSVRowString(String row) throws IOException {
        String[] cells = row.split(","); // We expect 5 strings.
        if (cells.length != 3) throw new IOException("Incorrect number of cells in a row!");
        return new AccountRequestModel(
                Long.parseLong(cells[0]), // transaction id
                Long.parseLong(cells[1]), // user id
                AccountModel.AccountType.getType(Integer.parseInt(cells[2]))); // type "1" -> 1 -> AccountType.Savings
    }
}
