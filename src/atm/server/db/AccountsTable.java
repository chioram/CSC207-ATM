package atm.server.db;

import atm.model.AccountModel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

public class AccountsTable {
    public HashMap<Long, AccountModel> accountsById = new HashMap<>();
    private long nextAccountId = 0;

    public void save(String fileName) {

        try {
            PrintWriter writer = Util.openFileW(fileName);
            writer.println("id,type,balance,date");
            for (AccountModel accountModel : accountsById.values()) writer.println(accountModel.toCSVRowString());
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to write CSV: " + fileName + ".");
        }
    }

    public void load(String fileName) {
        this.accountsById.clear();
        this.nextAccountId = 0;
        Util.loadCSV(fileName, row -> addAccount(AccountModel.fromCSVRowString(row)));
    }

    public void addAccount(AccountModel accountModel) {
        if (accountModel.getId() > nextAccountId) nextAccountId = accountModel.getId() + 1;
        this.accountsById.put(accountModel.getId(), accountModel); // balance "-10.40" -> -10.40
    }


    public AccountModel createAccount(AccountModel.AccountType type) {
        nextAccountId += 1;
        AccountModel newAccountModel = new AccountModel(nextAccountId, type, 0, new Date());
        addAccount(newAccountModel);
        return newAccountModel;
    }

    public AccountModel getAccountModelForId(long accountId) {
        return accountsById.get(accountId);
    }

    public Collection<AccountModel> getAllAccountModels() {
        return accountsById.values();
    }

    public boolean isRewardAccount(long accountId) {
        return getAccountModelForId(accountId).getType().getCode() == 4;
    }

}
