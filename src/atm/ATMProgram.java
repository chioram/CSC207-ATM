package atm;

import atm.model.UserModel;
import atm.server.BankServer;
import atm.server.BankServerConnection;

import java.util.Date;
import java.util.Scanner;

public class ATMProgram {

    public static void main(String[] args) {
        // Loads the CSV files as java objects.
        BankServer centralDatabase = new BankServer();

        // ATM simulation starts here...
        while (true) {
            System.out.println("Starting Up...");
            Date startUpDate = new Date();
            Scanner scanInput = new Scanner(System.in);
            while (hoursDifference(new Date(), startUpDate) < 24) {
                System.out.println("Please enter your user name and press enter:");
                String enteredUsername = scanInput.nextLine();
                System.out.println("Please enter your password and press enter:");
                String enteredPassword = scanInput.nextLine();
                try {
                    BankServerConnection connection = centralDatabase.tryLogin(enteredUsername, enteredPassword);
                    int returnCode = new ATMSim().runATM(connection);
                    if (returnCode == ATMSim.STATUS_REBOOT) {
                        if (connection.user.getAuthLevel() != UserModel.AuthLevel.User) break;
                    } else if (returnCode == ATMSim.STATUS_SHUTDOWN) {
                        if (connection.user.getAuthLevel() != UserModel.AuthLevel.User) {
                            scanInput.close();
                            centralDatabase.save();
                            System.out.println("Shutting Down...");
                            return;
                        }
                    }
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
            scanInput.close();
            // Back-up data every 24 hours.
            centralDatabase.save();
            System.out.println("Rebooting...");
        }
    }

    private static int hoursDifference(Date date1, Date date2) {
        final int MILLI_TO_HOUR = 1000 * 60 * 60;
        return (int) (date1.getTime() - date2.getTime()) / MILLI_TO_HOUR;
    }
}
