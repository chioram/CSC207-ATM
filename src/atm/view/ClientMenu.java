package atm.view;

import atm.ATMSim;
import atm.model.AccountModel;
import atm.server.BankServerConnection;
import atm.server.ITServerConnection;
import atm.server.db.UserTransactionTable;

import javax.sound.midi.SysexMessage;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.*;

public class ClientMenu extends Menu {

    /**
     * Displays options for Bank Clients
     **/

    private Scanner userInput = new Scanner(System.in);

    private BankServerConnection serverConnection;

    public ClientMenu(BankServerConnection bankServerConnection) {
        this.serverConnection = bankServerConnection;
    }

    private ArrayList<AccountModel> displayAccounts;


    public int showOptions() {


        int selection;

        // Display a list of this User's accounts

        System.out.println("Your Accounts: \n");
        displayAccounts = new ArrayList<>();
        List<AccountModel> userAccounts = serverConnection.getUserAccounts();
        int idx = 1;
        for (AccountModel acc : userAccounts) {
            System.out.println(idx + " - " + acc.getType() + " Account id: " + acc.getId());
            displayAccounts.add(acc);
            System.out.println("Balance: " + acc.getCurrency() + " " + acc.getBalance() + "\n \n");
            idx++;
        }


        System.out.println("Hello " + serverConnection.user.getFirstName() + "! Please select an option: \n ");
        System.out.println("1 - Transfer Between Accounts");
        System.out.println("2 - Transfer to User");
        System.out.println("3 - Pay a Bill");
        System.out.println("4 - Deposit Funds");
        System.out.println("5 - Withdraw Funds");
        System.out.println("6 - Request a New Account");
        System.out.println("7 - Convert Currencies");
        System.out.println("8 - EXIT");

        selection = userInput.nextInt();
        boolean successful = false;

        switch (selection) {

            case 1:

                transferBetweenAccounts(idx);
                break;

            case 2:
                transferToUser(idx);
                break;
            case 3:
                payBill();
                break;
            case 4:
                deposit(idx);
                break;
            case 5:
                withdraw(idx);
                break;
            case 6:
                requestNewAcc();
                break;
            case 7:
                convertCurrencies(idx);
                break;
            case 8:
                return ATMSim.STATUS_EXIT;
            default:
                System.out.println("ERROR. Please select an option from the list above.");
                showOptions();
        }
        return ATMSim.STATUS_RUNNING;
    }


    public AccountModel makeAccountSelection(int index) {
        //System.out.println(index);
        int transferTo = userInput.nextInt();
        //System.out.println(transferTo);
        AccountModel accTransferTo = null;
        //System.out.println(displayAccounts.get(0).getType());
        for (int i = 0; i < index; i++) {
            if (i == transferTo) {
                accTransferTo = displayAccounts.get(i - 1);
            }

        }

        if (accTransferTo == null) {
            System.out.println("Please make a valid selection");
            return makeAccountSelection(index);
        }
        System.out.println(accTransferTo.getType());
        return accTransferTo;
    }


    public void requestNewAcc() {

        System.out.println("Enter a number for the type of account would you like to create.\n0 - Checking " +
                "1 - Saving 2 - Credit 3 - Line of Credit 4 - Rewards");
        int accTypeNum = userInput.nextInt();

        while (accTypeNum > 4) {
            System.out.println("Invalid account type. Re-enter.\n0 - Checking 1 - Saving 2 - Credit " +
                    "3 - Line of Credit 4 - Rewards");
            accTypeNum = userInput.nextInt();
        }

        AccountModel.AccountType accType = AccountModel.AccountType.getType(accTypeNum);
        serverConnection.requestAccount(serverConnection.user.getId(), accType);
        System.out.println("Account successfully requested.");
    }

    public void payBill() {

        ArrayList<AccountModel> displayCrAcc = new ArrayList<>();
        ArrayList<AccountModel> displayDbAcc = new ArrayList<>();

        for (int i = 0; i < displayAccounts.size(); i++) {
            if (displayAccounts.get(i).getType() == AccountModel.AccountType.Credit) {
                displayCrAcc.add(displayAccounts.get(i));
            } else if (displayAccounts.get(i).getType() == AccountModel.AccountType.Checking ||
                    displayAccounts.get(i).getType() == AccountModel.AccountType.Saving) {
                displayDbAcc.add(displayAccounts.get(i));
            }
        }

        if (displayCrAcc.size() == 0) {
            System.out.println("You do not have any credit accounts. Exiting...");
            return;
        }

        if (displayDbAcc.size() == 0) {
            System.out.println("You do not have any debit accounts to pay from. Exiting...");
            return;
        }

        System.out.println("Select a credit account to pay your bill.");
        printArrayOfAccs(displayCrAcc);

        int crAcc = userInput.nextInt();

        System.out.println("Your credit balance is " + displayAccounts.get(crAcc).getBalance() + "\n" +
                "How much would you like to pay off?");
        double amount = userInput.nextDouble();

        System.out.println("Select which debit account you'd like to pay from.");
        printArrayOfAccs(displayDbAcc);
        int dbAcc = userInput.nextInt();
        boolean requested = serverConnection.requestWithdrawal(displayDbAcc.get(dbAcc).getId(), amount);

        System.out.println("To what payee would you like this bill to be paid to?");
        String nameOfPayee = userInput.next();

        if (requested) {
            try {
                PrintWriter outgoing = atm.server.db.Util.openFileW("outgoing.txt");
                outgoing.write("$ " + amount + " to " + nameOfPayee);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println("Payment requested. $" + amount + " to " + nameOfPayee);
        }
    }

    public void printArrayOfAccs(ArrayList<AccountModel> accs) {
        for (int i = 0; i < accs.size(); i++) {
            System.out.println(i + " - Account id: " + accs.get(i).getId() + " Balance: " +
                    accs.get(i).getBalance());
        }
    }

    public void displayExchangeRates() {

        TreeMap<String, Double> rates = serverConnection.getExchangeRates();
        System.out.println("Rates up to date as of " + new Date());
        for (Map.Entry<String, Double> entry : rates.entrySet()) {
            System.out.println(entry.getKey() + ":  " + entry.getValue());
        }
    }


    public void transferBetweenAccounts(int idx) {

        //

        boolean successful = false;
        System.out.println("Transfer Between Accounts:");

        System.out.println("What account would you like to transfer from?");
        AccountModel withdrawFrom = makeAccountSelection(idx);

        //System.out.println(withdrawFrom.getType());

        System.out.println("What account would you like to transfer to?");
        AccountModel transferTo = makeAccountSelection(idx);

        //System.out.println(transferTo.getType());

        System.out.println("How much money would you like to transfer?");
        double amount = userInput.nextDouble();

        try {

            successful = serverConnection.requestTransfer(withdrawFrom.getId(), transferTo.getId(), amount);
        } catch (SecurityException sE) {

            System.out.println("This account does not belong to you, you will be redirected to the main menu.");

            return;

        } catch (IllegalArgumentException e) {

            System.out.println("Error encountered. Please try again. You will be redirected ot the main menu.");
            return;

        }

        if (successful)
            System.out.println("Transfer successful. You will be redirected to main menu");
        else
            System.out.println("Transfer unsuccessful. You will be redirected to main menu. Please try again later");


    }

    public void transferToUser(int idx) {

        //
        System.out.println("Transfer To User:");

        boolean successful = false;

        System.out.println("What account would you like to transfer from?");
        AccountModel transferFrom = makeAccountSelection(idx);

        System.out.println("Please enter the account ID of the destination user's account you are transferring" +
                "funds to:");
        long destAcc = userInput.nextLong();

        System.out.println("Please enter the amount you would like to transfer");

        double amountToTransfer = userInput.nextDouble();
        try {
            successful = serverConnection.requestTransfer(transferFrom.getId(), destAcc, amountToTransfer);
        } catch (SecurityException sE) {

            System.out.println("This account does not belong to you, you will be redirected to the main menu.");
            return;

        } catch (IllegalArgumentException e) {

            System.out.println("Error encountered. Please try again. You will be redirected ot the main menu.");
            return;

        }

        if (successful)
            System.out.println("Transfer to user successful. You will be redirected to main menu");

        else
            System.out.println("Transfer unsuccessful. You will be redirected to main menu. Please try again later");


    }

    public void deposit(int idx) {

        // deposit
        boolean successful = false;
        System.out.println("Please select the account you would like to deposit funds into");
        AccountModel deposit = makeAccountSelection(idx);

        System.out.println("What amount would you like to deposit?");
        double amountDeposit = userInput.nextDouble();

        System.out.println("Would you like to deposit cash or cheque?");
        System.out.println("1 - Cash");
        System.out.println("2 - Cheque");
        String typeDep;
        int choice = userInput.nextInt();
        if (choice == 1)
            typeDep = "cash";
        else
            typeDep = "cheque";


        try {
            successful = serverConnection.tryDeposit(deposit.getId(), amountDeposit);
            serverConnection.writeDepositsText(serverConnection.getUserID(), deposit.getId(), amountDeposit, typeDep);
        } catch (IllegalArgumentException e) {

            if (amountDeposit <= 0) {
                System.out.println("Amount must be greater than $0, You will be redirected to main menu");
                return;
            } else {
                System.out.println("Account does not exist! You will be redirected to main menu");
                return;
            }
        }

        if (successful) {
            System.out.println("Deposit successful! You will now be redirected to main menu");
        } else
            System.out.println("Deposit unsuccessful. You will be redirected to main menu. Please try again later");

    }

    public void withdraw(int idx) {
        boolean successful = false;

        System.out.println("Please select the account you would like to withdraw funds from");

        AccountModel withdraw = makeAccountSelection(idx);

        System.out.println("What amount would you like to withdraw?");

        double amountWithdraw = userInput.nextDouble();

        try {
            successful = serverConnection.requestWithdrawal(withdraw.getId(), amountWithdraw);
        } catch (IllegalArgumentException e) {

            if (!withdraw.getType().canWithdraw()) {
                System.out.println("Credit accounts can't be withdrawn from! You will be redirected to the main menu");
            } else if (withdraw.getBalance() - amountWithdraw < withdraw.getType().getMinBalance()) {
                System.out.println("Cannot withdraw more than the allowed amount! You will be redirected to the main menu");
            } else if (amountWithdraw <= 0) {
                System.out.println("You must withdraw more than $0. You will be redirected to the main menu");

            } else
                System.out.println("Account does not exist. Please try again. You will be redirected to the main menu");

            return;
        }

        if (successful)
            System.out.println("Withdraw successful! Please allow time for changes to be reflected in your accounts." +
                    "You will now be redirected to the main menu");

        else
            System.out.println("Withdrawal unsuccessful. You will be redirected to the main menu. Please try again" +
                    "at a later time");
    }

    public void convertCurrencies(int idx) {
        System.out.println("Hello! Below is a list of all the available currencies and their respective exchange" +
                "rates");
        displayExchangeRates();
        System.out.println("Would you like to make a deposit/withdrawal? Please enter Y/N");
        userInput.nextLine();
        String yesNo = userInput.nextLine();

        if (yesNo.toLowerCase().equals("n")) {
            System.out.println("Okay. Returning to main menu...");
            return;
        }

        System.out.println("Please enter the 3 letter currency code that you would like to convert to/from");
        String currencyCode = userInput.nextLine().toUpperCase();

        System.out.println("Please enter the amount that you would like to transfer/withdraw/deposit");
        double amountInput = userInput.nextDouble();

        double amountConverted = serverConnection.convertCurrency(amountInput, currencyCode);
        System.out.println("Would you like to transfer to a user, withdraw or deposit funds? Enter T for transfer, " +
                "D for deposit or W for withdraw");
        userInput.nextLine();
        String typeOfTransaction = userInput.nextLine().toLowerCase();

        if (typeOfTransaction.equals("t")) {

            boolean successful = false;

            System.out.println("What account would you like to transfer from?");
            AccountModel transferFrom = makeAccountSelection(idx);

            System.out.println("Please enter the account ID of the destination user's account you are transferring" +
                    "funds to:");
            long destAcc = userInput.nextLong();


            try {
                successful = serverConnection.requestTransfer(transferFrom.getId(), destAcc, amountConverted);
            } catch (SecurityException sE) {
                System.out.println("ERROR!");
                System.out.println("This account does not belong to you, you will be redirected to the main menu.");
                return;

            } catch (IllegalArgumentException e) {

                System.out.println("Error encountered. Please try again. You will be redirected ot the main menu.");
                return;

            }

            if (successful)
                System.out.println("Transfer to user successful. You will be redirected to main menu");

            else
                System.out.println("Transfer unsuccessful. You will be redirected to main menu. Please try again later");

        } else if (typeOfTransaction.equals("w")) {
            boolean successful = false;

            System.out.println("Please select the account you would like to withdraw funds from");

            AccountModel withdraw = makeAccountSelection(idx);


            try {
                successful = serverConnection.requestWithdrawal(withdraw.getId(), amountConverted);
            } catch (IllegalArgumentException e) {

                if (!withdraw.getType().canWithdraw()) {
                    System.out.println("ERROR");
                    System.out.println("Credit accounts can't be withdrawn from! You will be redirected to the main menu");
                } else if (withdraw.getBalance() - amountConverted < withdraw.getType().getMinBalance()) {
                    System.out.println("ERROR");
                    System.out.println("Cannot withdraw more than the allowed amount! You will be redirected to the main menu");
                } else if (amountConverted <= 0) {
                    System.out.println("You must withdraw more than $0. You will be redirected to the main menu");

                } else
                    System.out.println("Account does not exist. Please try again. You will be redirected to the main menu");

                return;
            }

            if (successful)
                System.out.println("Withdraw successful! Please allow time for changes to be reflected in your accounts." +
                        "You will now be redirected to the main menu");

            else
                System.out.println("Withdrawal unsuccessful. You will be redirected to the main menu. Please try again" +
                        "at a later time");

        } else if (typeOfTransaction.equals("d")) {
            // deposit
            boolean successful = false;
            System.out.println("Please select the account you would like to deposit funds into");
            AccountModel deposit = makeAccountSelection(idx);

            System.out.println("Are you depositing cash or cheque?");
            System.out.println("1 - cash");
            System.out.println("2 - cheque");

            int cashCheque = userInput.nextInt();
            String type;
            if (cashCheque == 1) {
                type = "cash";
            } else
                type = "cheque";

            try {
                successful = serverConnection.tryDeposit(deposit.getId(), amountConverted);
                serverConnection.writeDepositsText(serverConnection.getUserID(), deposit.getId(), amountConverted, type);
                if (type == "cash") {
                    serverConnection.restockByDeposit(amountConverted);
                }
            } catch (IllegalArgumentException e) {

                if (amountConverted <= 0) {
                    System.out.println("ERROR");
                    System.out.println("Amount must be greater than $0, You will be redirected to main menu");
                    return;
                } else {
                    System.out.println("ERROR");
                    System.out.println("Account does not exist! You will be redirected to main menu");
                    return;
                }
            }

            if (successful)
                System.out.println("Deposit successful! You will now be redirected to main menu");
            else
                System.out.println("Deposit unsuccessful. You will be redirected to main menu. Please try again later");
        }

    }
}
