package atm.model;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Currency;
import java.util.Locale;

public class AccountModel {
    private final long id;
    private final AccountType type;
    private double balance;
    private final Date creationDate;
    private final Currency currency;


    public AccountModel(long id, AccountType type, double balance, Date creationDate) {
        this.id = id;
        this.type = type;
        this.balance = balance;
        this.creationDate = creationDate;
        this.currency = Currency.getInstance(Locale.CANADA);
    }

    public long getId() {
        return id;
    }

    public AccountType getType() {
        return type;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String toCSVRowString() {
        return id + "," + type.code + "," + balance + "," + creationDate.getTime();
    }

    public static AccountModel fromCSVRowString(String row) throws IOException {
        String[] cells = row.split(","); // We expect 5 strings.
        if (cells.length != 4) throw new IOException("Incorrect number of cells in a row!");
        Date creationDate = new Date(Long.parseLong(cells[3]));
        return new AccountModel(Long.parseLong(cells[0]), // user id "0" -> 0
                AccountModel.AccountType.getType(Integer.parseInt(cells[1])), // type "1" -> 1 -> AccountType.Savings
                Double.parseDouble(cells[2]),
                creationDate);
    }

    public void addPointsToAccount(double points) {
        this.type.addPoints(points);
    }

    public enum AccountType {
        Checking(0, true, -100, 15.95, "Checking", null),
        Saving(1, true, 0, 4.95, "Saving", null),
        Credit(2, false, -1000, 10, "Credit", null),
        LineOfCredit(3, true, -10000, 10, "Line of Credit", null),
        Rewards(4, false,0,10, "Rewards",0.0),
        Joint(5,true, 0, 5, "Joint", null);

        private final int code;
        private final boolean canWithdraw;
        private final double minBalance;
        private final String name;
        private final double monthlyFee;
        private double points;


        AccountType(int code, boolean canWithdraw, double minBalance, double monthlyFee, String name, Double points) {
            this.code = code;
            this.canWithdraw = canWithdraw;
            this.minBalance = minBalance;
            this.monthlyFee = monthlyFee;
            this.name = name;
        }

        public int getCode() {
            return code;
        }

        public double getMinBalance() {
            return minBalance;
        }

        public boolean canWithdraw() {
            return canWithdraw;
        }

        public double getMonthlyFee() {
            return monthlyFee;
        }

        public String getName() {
            return name;
        }

        public double getPoints() {return points;}

        public static AccountType getType(int code) {
            switch (code) {
                case 0:
                    return Checking;
                case 1:
                    return Saving;
                case 2:
                    return Credit;
                case 3:
                    return LineOfCredit;
                case 4:
                    return Rewards;
                case 5:
                    return Joint;
                default:
                    throw new IllegalArgumentException("code does not match an AccountType!");
            }
        }

        public void addPoints(double amount) {
            this.points += amount;
        }
    }
}

