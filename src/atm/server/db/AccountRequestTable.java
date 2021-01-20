package atm.server.db;

import atm.model.AccountModel;
import atm.model.AccountRequestModel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;

public class AccountRequestTable {
    HashMap<Long, AccountRequestModel> accountRequestModelById = new HashMap();
    private long nextAccountId = 0;

    public void save(String fileName) {
        try {
            PrintWriter writer = Util.openFileW(fileName);
            writer.println("id,userId,accType");
            for (AccountRequestModel accountRequestModel : accountRequestModelById.values())
                writer.println(accountRequestModel.toCSVRowString());
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to write CSV: " + fileName + ".");
        }
    }

    public void load(String fileName) {
        this.accountRequestModelById.clear();
        this.nextAccountId = 0;
        Util.loadCSV(fileName, row -> addAccountRequest(AccountRequestModel.fromCSVRowString(row)));
    }

    public void addAccountRequest(AccountRequestModel accountRequestModel) {
        if (accountRequestModel.getId() > nextAccountId) nextAccountId = accountRequestModel.getId() + 1;
        this.accountRequestModelById.put(accountRequestModel.getId(), accountRequestModel);
    }

    public void createAccountRequest(long userId, AccountModel.AccountType accountType) {
        addAccountRequest(new AccountRequestModel(nextAccountId, userId, accountType));
        nextAccountId++;
    }

    public AccountRequestModel getAccountRequestModelForId(long accountRequestId) {
        return accountRequestModelById.get(accountRequestId);
    }

    public AccountRequestModel removeAccountRequestModelForId(long accountRequestId) {
        return accountRequestModelById.remove(accountRequestId);
    }

    public Collection<AccountRequestModel> getAllAccountRequests() {
        return accountRequestModelById.values();
    }
}
