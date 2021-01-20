package atm.server.db;

import atm.model.UserModel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserTable {
    List<UserModel> users = new ArrayList<>();
    HashMap<String, UserModel> usersByUsername = new HashMap<>();
    private long nextUserId = 0;

    public void save(String fileName) {
        try {
            PrintWriter writer = Util.openFileW(fileName);
            writer.println("id,firstName,lastName,username,password,primaryAccId,authLevel");
            for (UserModel userModel : users) writer.println(userModel.toCSVRowString());
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to write CSV: " + fileName + ".");
        }
    }

    public void load(String fileName) {
        this.users.clear();
        this.usersByUsername.clear();
        nextUserId = 0;
        Util.loadCSV(fileName, row -> addUser(UserModel.fromCSVRowString(row)));
    }

    public void addUser(UserModel userModel) {
        if (userModel.getId() > nextUserId) nextUserId = userModel.getId() + 1;
        this.users.add(userModel);
        this.usersByUsername.put(userModel.getUsername(), userModel);
    }

    public UserModel createUser(String firstName, String lastName, String userName, String initialPassword, long primaryAccId) {
        UserModel userModel = new UserModel(nextUserId, firstName, lastName, userName, initialPassword, primaryAccId, UserModel.AuthLevel.User);
        addUser(userModel);
        return userModel;
    }

    public UserModel getUserModelForUserName(String username) {
        return usersByUsername.get(username);
    }

}
