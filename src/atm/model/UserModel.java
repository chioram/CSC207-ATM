package atm.model;

import java.io.IOException;

public class UserModel {
    private final long id;
    private String firstName;
    private String lastName;
    private final String username;
    private String password;
    private long primaryAccountId;
    private AuthLevel authLevel;

    public UserModel(long id, String firstName, String lastName, String username, String password, long primaryAccountId,
                     AuthLevel authLevel) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.primaryAccountId = primaryAccountId;
        this.authLevel = authLevel;
    }

    public long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getPrimaryAccountId() {
        return primaryAccountId;
    }

    public void setPrimaryAccountId(long primaryAccountId) {
        this.primaryAccountId = primaryAccountId;
    }

    public AuthLevel getAuthLevel() {
        return authLevel;
    }

    public String toCSVRowString() {
        return id + "," + firstName + "," + lastName + "," + username + "," + password + "," + primaryAccountId + ","
                + authLevel.code;
    }

    public static UserModel fromCSVRowString(String row) throws IOException {
        String[] cells = row.split(","); // We expect 7 strings.
        if (cells.length != 7) throw new IOException("Incorrect number of cells in a row!");
        return new UserModel(Long.parseLong(cells[0]), // user id "0" -> 0
                cells[1], // first name
                cells[2], // last name
                cells[3], // username
                cells[4], // password
                Long.parseLong(cells[5]), // Primary count id
                UserModel.AuthLevel.getType(Integer.parseInt(cells[6]))); // Authentication level.]
    }

    public enum AuthLevel {
        User(0),
        BankManager(1),
        ITHelper(2);


        private int code;

        AuthLevel(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static AuthLevel getType(int code) {
            switch (code) {
                case 0:
                    return User;
                case 1:
                    return BankManager;
                case 2:
                    return ITHelper;
                default:
                    throw new IllegalArgumentException("code does not match an AccountType!");
            }
        }
    }

}
