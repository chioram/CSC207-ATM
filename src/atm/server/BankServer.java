package atm.server;

import atm.server.db.*;
import atm.model.AccountModel;
import atm.model.AccountRequestModel;
import atm.model.TransactionModel;
import atm.model.UserModel;

import java.io.*;
import java.util.*;

import java.io.FileWriter;
import java.io.PrintWriter;

public class BankServer {
    UserTable userTable;
    AccountsTable accountTable;
    UserAccountsTable userAccountsTable;
    AccountRequestTable accountRequestTable;
    UserTransactionTable userTransactionTable;
    ExchangeRateTable exchangeRateTable;
    BillsTable billsTable;

    private int request_id_ticker = 0;


    public BankServer() {
        userTable = new UserTable();
        accountTable = new AccountsTable();
        userAccountsTable = new UserAccountsTable();
        accountRequestTable = new AccountRequestTable();
        userTransactionTable = new UserTransactionTable();
        exchangeRateTable = new ExchangeRateTable();
        billsTable = new BillsTable();
        ;
        load();
        if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1) applySavingsInterests();
        if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1) chargeMonthlyFees();
    }

    public void load() {
        userTable.load("users.csv");
        accountTable.load("accounts.csv");
        userAccountsTable.load("useraccounts.csv");
        accountRequestTable.load("accountrequest.csv");
        userTransactionTable.load("transactions.csv");
        exchangeRateTable.load();

    }

    public void save() {
        userTable.save("users.csv");
        accountTable.save("accounts.csv");
        userAccountsTable.save("useraccounts.csv");
        accountRequestTable.save("accountrequest.csv");
        userTransactionTable.save("transactions.csv");
    }

    public BankServerConnection tryLogin(String username, String password) {
        UserModel userModel = userTable.getUserModelForUserName(username);
        if (userModel != null) {
            if (userModel.getPassword().equals(password)) {
                if (userModel.getAuthLevel() == UserModel.AuthLevel.User)
                    return new BankServerConnection(userModel, this);
                else if (userModel.getAuthLevel() == UserModel.AuthLevel.BankManager)
                    return new ManagerBankServerConnection(userModel, this);
                else if (userModel.getAuthLevel() == UserModel.AuthLevel.ITHelper)
                    return new ITServerConnection(userModel, this);
                else throw new SecurityException("User has an invalid AuthLevel.");
            } else {
                throw new SecurityException("Incorrect username/password combination!");
            }
        } else {
            throw new IllegalArgumentException("User does not exist!");
        }
    }

    public boolean requestWithdrawal(long accountId, double amount) {
        // 1. Find AccountModel for accountId using your hashmap.
        // 2. Check that account type IS ALLOWED to withdrawal
        // 3. Check if enough balance.
        // 4. Finally return success!
        if (amount <= 0) throw new IllegalArgumentException("Must withdraw more than $0");
        AccountModel accountModel = accountTable.getAccountModelForId(accountId);
        if (accountModel == null) throw new IllegalArgumentException("Account does not exist!");
        if (!accountModel.getType().canWithdraw())
            throw new IllegalArgumentException("Credit accounts can't be withdrawn from!");
        double newBalance = accountModel.getBalance() - amount;
        if (newBalance < accountModel.getType().getMinBalance())
            throw new IllegalArgumentException("Cannot withdraw more than the allowed amount!");

        if (!billsTable.hasEnough(amount)) {
            throw new IllegalArgumentException("This ATM has insufficient Funds. Please withdraw a lower amount");
        }
        if (!isDivisibleBy5(amount)) {
            throw new IllegalArgumentException(
                    "Must withdraw a valid amount. Please enter a combination of 5, 10, 20 or 50 dollar bills.");
        }
        giveOutBills((int) amount);
        accountModel.setBalance(newBalance);
        return true;
    }

    public boolean tryDeposit(long accountId, double amount) {
        // 0. Check if amount is valid.
        // 1. Find AccountModel for accountId using your hashmap.
        // 2. Check that account type IS ALLOWED to deposit
        // 3. Finally return success!
        if (amount <= 0) throw new IllegalArgumentException("Must deposit more than $0");
        AccountModel accountModel = accountTable.getAccountModelForId(accountId);
        if (accountModel == null) throw new IllegalArgumentException("Account does not exist!");
        if (accountModel.getType() == AccountModel.AccountType.Credit) {
            accountModel.setBalance(accountModel.getBalance() - amount);
        } else {
            accountModel.setBalance(accountModel.getBalance() + amount);
        }
        UserTransactionTable table = new UserTransactionTable();
        return true;
    }

    public boolean requestTransfer(long srcUserId, long srcAccountId, long destAccountId, double amount) {
        if (!userAccountsTable.checkIfUserOwnsAccount(srcUserId, srcAccountId))
            throw new SecurityException("srcAccountId does not belong to srcUserId!");
        AccountModel destAccountModel = accountTable.getAccountModelForId(destAccountId);
        if (destAccountModel == null)
            throw new IllegalArgumentException("destAccountId does not exist in our database!");
        AccountModel srcAccountModel = accountTable.getAccountModelForId(srcAccountId);
        if (!srcAccountModel.getType().canWithdraw())
            throw new IllegalArgumentException("Credit accounts can't be withdrawn from!");
        double newSrcBalance = srcAccountModel.getBalance() - amount;
        if (newSrcBalance < srcAccountModel.getType().getMinBalance())
            throw new IllegalArgumentException("srcAccount does not have enough balance to transfer money!");
        userTransactionTable.createTransactionModel(srcUserId, srcAccountId, destAccountId, amount);
//        check if reward account
        if (accountTable.isRewardAccount(srcAccountId)) {
            double points = userTransactionTable.calculatePoints(amount);
            accountTable.getAccountModelForId(srcAccountId).addPointsToAccount(points);
        }
        srcAccountModel.setBalance(newSrcBalance);
        if (destAccountModel.getType() == AccountModel.AccountType.Credit) {
            destAccountModel.setBalance(destAccountModel.getBalance() + -amount);
        } else {
            destAccountModel.setBalance(destAccountModel.getBalance() + amount);
        }
        return true; // If success
    }

    public void applySavingsInterests() {
        for (AccountModel accountModel : accountTable.getAllAccountModels()) {
            if (accountModel.getType() == AccountModel.AccountType.Saving)
                accountModel.setBalance(accountModel.getBalance() * 1.001);
        }
    }

    public void chargeMonthlyFees() {
        for (AccountModel accountModel : accountTable.getAllAccountModels()) {
            accountModel.setBalance(accountModel.getBalance() - (accountModel.getType().getMonthlyFee()));
        }
    }


    public boolean grantAccount(long accRequestId) {
        AccountRequestModel requestModel = accountRequestTable.removeAccountRequestModelForId(accRequestId);
        if (requestModel == null) throw new IllegalArgumentException("accRequestId does not exist in our database!");
        AccountModel newAccountModel = accountTable.createAccount(requestModel.getRequestedAccountType());
        userAccountsTable.createEntryForUser(requestModel.getRequesterUserId(), newAccountModel.getId());
        return true;
    }



    public boolean undoLastTransaction(long userId, int numTransaction) {
        ArrayList<TransactionModel> array = new ArrayList<>(userTransactionTable.transactionsForUserId.get(userId));
        for (int i = 1; i < numTransaction + 1; i++) {
            TransactionModel transactionModel = array.get(array.size() - 1);
            userTransactionTable.transactionsForUserId.remove(userId, transactionModel);
            if (transactionModel == null)
                throw new IllegalArgumentException("userId does not have a last transaction in our database!");
            AccountModel destAccountModel = accountTable.getAccountModelForId(transactionModel.getDestAccountId());
            AccountModel srcAccountModel = accountTable.getAccountModelForId(transactionModel.getSrcAccountId());
            double newDestBalance = destAccountModel.getBalance() - transactionModel.getAmount();
            if (newDestBalance < destAccountModel.getType().getMinBalance())
                throw new IllegalArgumentException("destAccount does not have enough balance to undo transaction!");
            destAccountModel.setBalance(newDestBalance);
            srcAccountModel.setBalance(srcAccountModel.getBalance() + transactionModel.getAmount());
            System.out.println(i + "transaction(s) reverted");
        }
        return true;
    }


    public boolean createUser(String firstName, String lastName, String userName, String initialPassword) {
        // 1. Create new checking account.
        AccountModel primaryAccountModel = accountTable.createAccount(AccountModel.AccountType.Checking);
        UserModel newUserModel = userTable.createUser(firstName, lastName, userName, initialPassword, primaryAccountModel.getId());
        userAccountsTable.createEntryForUser(newUserModel.getId(), primaryAccountModel.getId());
        return true;
    }

    public List<AccountModel> getUserAccounts(long userId) {
        List<AccountModel> accountModels = new ArrayList<>();
        for (long accId : userAccountsTable.getUserAccountIds(userId))
            accountModels.add(accountTable.getAccountModelForId(accId));
        return accountModels;
    }

    public TransactionModel getLastUserTransaction(long userId) {
        ArrayList<TransactionModel> array = new ArrayList<>(userTransactionTable.transactionsForUserId.get(userId));
        return array.remove(0);
    }

    public List<AccountRequestModel> getPendingAccountRequests() {
        return new ArrayList<>(accountRequestTable.getAllAccountRequests());
    }


    public boolean requestAccount(long userId, AccountModel.AccountType type){

        AccountRequestModel accModel = new AccountRequestModel(request_id_ticker, userId, type);
        request_id_ticker += 1;
        accountRequestTable.addAccountRequest(accModel);
        return true;
    }


    public TreeMap<String, Double> getExchangeRates() {
        return exchangeRateTable.getAllRates();
    }

    public double convertCurrency(double depositAmount, String inputCurrency) {

        Double targetRate = exchangeRateTable.getSingleRate(inputCurrency);
        return depositAmount * targetRate;

    }

    private boolean isDivisibleBy5(double amount) {
        return amount % 5 == 0;
    }

    public void giveOutBills(int amount) {
        int x = amount;
        for (Map.Entry<Integer, Integer> entry : billsTable.getAllAmounts().entrySet()) {
            while (x - entry.getKey() >= 0) {
                //update <x> and reduce denomination amount by one
                x -= entry.getKey();
                entry.setValue(entry.getValue() - 1);
            }
        }
        if (!billsTable.hasEnough(amount)) {
            writeAlerts();
        }
    }

    public boolean writeDepositsTxt(long userID, long srcAccID, double amount, String type) {
        UserTransactionTable userTable = new UserTransactionTable();
        userTable.writeToDeposits(userID, srcAccID, amount, type);
        return true;
    }

    public void readAlerts() {
        String filename = "alerts.txt";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            System.out.println(reader.readLine());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeAlerts() {
        String filename = "alerts.txt";
        try {
            //PrintWriter writer = Util.openFileW(filename);
            //writer.println("WARNING: ATM running low on bills. Recorded on " + new Date());
            FileWriter write = new FileWriter(filename, false);
            PrintWriter print_line = new PrintWriter(write);
            print_line.print("WARNING: ATM running low on bills. Recorded on " + new Date());
            print_line.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to write to file: " + filename + ".");
        }
    }

    public void restock() {
        BillsTable.resetBills();
    }

    public void restockByDeposit(double amount) {

        int x = (int) amount;
        for (Map.Entry<Integer, Integer> entry : billsTable.getAllAmounts().entrySet()) {
            while (x - entry.getKey() >= 0) {
                //update <x> and reduce denomination amount by one
                x -= entry.getKey();
                entry.setValue(entry.getValue() + 1);
            }
        }
    }
}