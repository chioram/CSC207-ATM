package atm.server;

import atm.model.AccountModel;
import atm.model.UserModel;

import java.util.List;
import java.util.TreeMap;

public class BankServerConnection {
    public final UserModel user;
    protected BankServer bankServer;

    public BankServerConnection(UserModel user, BankServer bankServer) {
        this.user = user;
        this.bankServer = bankServer;
    }

    public boolean requestTransfer(long srcAccountId, long destAccountId, double amount) {
        return bankServer.requestTransfer(user.getId(), srcAccountId, destAccountId, amount);
    }

    public boolean requestWithdrawal(long accountId, double amount) {
        return bankServer.requestWithdrawal(accountId, amount);
    }

    public boolean requestAccount(long userId, AccountModel.AccountType type) {
        return bankServer.requestAccount(userId, type);
    }

    public long getUserID() {
        return user.getId();
    }

    public boolean tryDeposit(long accountId, double amount) {
        return bankServer.tryDeposit(accountId, amount);
    }

    public List<AccountModel> getUserAccounts() {
        return bankServer.getUserAccounts(user.getId());
    }

    public TreeMap<String, Double> getExchangeRates() {
        return bankServer.getExchangeRates();
    }

    public double convertCurrency(double depositAmount, String inputCurrency) {
        return bankServer.convertCurrency(depositAmount, inputCurrency);
    }

    public boolean writeDepositsText(long userID, long srcAccount, double amount, String type) {
        return bankServer.writeDepositsTxt(userID, srcAccount, amount, type);
    }

    public void restockByDeposit(double amount) {
        bankServer.restockByDeposit(amount);
    }

}
