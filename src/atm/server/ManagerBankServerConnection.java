package atm.server;

import atm.model.AccountRequestModel;
import atm.model.TransactionModel;
import atm.model.UserModel;

import java.util.List;
import java.util.Map;

public class ManagerBankServerConnection extends BankServerConnection {

    public ManagerBankServerConnection(UserModel user, BankServer bankServer) {
        super(user, bankServer);
        if (user.getAuthLevel() != UserModel.AuthLevel.BankManager)
            throw new SecurityException("Unauthorized connection!");
    }

    public boolean grantAccount(long accRequestId) {
        return bankServer.grantAccount(accRequestId);
    }

    public boolean undoLastTransaction(long userId, int numTransaction) {
        return bankServer.undoLastTransaction(userId, numTransaction);
    }

    public boolean createUser(String firstName, String lastName, String userName, String initialPassword) {
        return bankServer.createUser(firstName, lastName, userName, initialPassword);
    }

    public TransactionModel getLastUserTransaction(long userId) {
        return bankServer.getLastUserTransaction(userId);
    }

    public List<AccountRequestModel> getPendingAccountRequests() {
        return bankServer.getPendingAccountRequests();
    }

    public void save() {
        bankServer.save();
    }

    public void readAlerts() {
        bankServer.readAlerts();
    }

    public Map<Integer, Integer> getBills() {
        return bankServer.billsTable.getAllAmounts();
    }

    public void restock() {
        bankServer.restock();
    }
}
