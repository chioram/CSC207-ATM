package atm.view;

import atm.ATMSim;
import atm.model.AccountModel;
import atm.server.ITServerConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ITHelperMenu extends Menu {

    /**
     * Displays options for Bank Clients
     **/

    private Scanner userInput = new Scanner(System.in);

    ITServerConnection serverConnection;

    public ITHelperMenu(ITServerConnection bankServerConnection) {
        this.serverConnection = bankServerConnection;
    }


    public int showOptions() {


        int selection;

        // Display a list of this User's accounts

        System.out.println("Hello " + serverConnection.user.getFirstName() + "! Please select an option: \n ");

        System.out.println("1 - Back-up ATM Data");
        System.out.println("2 - Reboot ATM");
        System.out.println("3 - Shutdown ATM");
        System.out.println("4 - EXIT");

        selection = userInput.nextInt();

        switch (selection) {

            case 1:
                serverConnection.backupData();
                break;
            case 2:
                return ATMSim.STATUS_REBOOT;
            case 3:
                return ATMSim.STATUS_SHUTDOWN;
            case 4:
                return ATMSim.STATUS_EXIT;
            default:
                System.out.println("ERROR. Please select an option from the list above.");
                break;
        }
        return ATMSim.STATUS_RUNNING;
    }


}
