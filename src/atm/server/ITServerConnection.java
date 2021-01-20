package atm.server;

import atm.model.AccountModel;
import atm.model.UserModel;

import java.util.List;

public class ITServerConnection extends BankServerConnection {

    public ITServerConnection(UserModel user, BankServer bankServer){
        super(user, bankServer);
        if (user.getAuthLevel() != UserModel.AuthLevel.ITHelper) throw new SecurityException("Unauthorized connection!");
    }

    public void backupData() {
        bankServer.save();
    }
}
