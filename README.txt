===========
    ATM
===========

by Group 0325

=== SETTING UP THE PROGRAM ===

In order to run the program, simply run it from the main entry-point in ATMProgram.java with no arguments.
Project SDK should be set to 1.8, Languagae Level to 8.
We have provided sample simulation data in the project folder. Please see the .csv files in /res.

The project makes use of the JSoup web-scraping library. Please find the associated .jar in /my_lib.
Make sure to add the .jar by navigating to Project Structure > Modules > Dependencies

=== USING THE ATM ===

This program uses a command-line user interface.
First login with one of the following sample accounts:
    1. Bank Manager - username: areejn password: test
    2. User/Client - username biancap password: test2
    3. IT Helper - none provided. feel free to edit the csv files to create an IT Helper (see note on Auth Level)

After logging in, you will be greeted with a menu and a list of possible actions.
Whenever user-input is required, you will be prompted.

~ BANK MANAGER

Menu Options:

'Add New Client':       as described
'Undo Transaction':     Undo up to x amount of transactions on this ATM
'Add a New Account':    View and manage the list of bank account requests made by different users.
'Manage ATM Funds':     View system alerts and manage the amount of bills stocked in the ATM.
'Save Data':            Saves changes you make to the System. MUST Save Data before exiting or shutting down the ATM.
'Reboot ATM':           Restarts the ATM
'Shutdown ATM':         Shuts down the ATM without restarting.
'EXIT':                 Returns user to menu screen

~ USER

Menu Options:

'Transfer Between Accounts':    Transfer money between any accounts you own
'Transfer to User':     Transfer money to another user
'Pay a Bill':           Writes to outgoing,txt
'Deposit Funds':        as described
'Withdraw Funds':       as described
'Request a New Account':Sends a request to the Bank Manager for a new account
'Convert Currencies':   Users can deposit/withdraw/transfer money in any of 30 chosen world currencies. Displays the
                        current exchange rate for each currency.
'EXIT':

~ IT HELPER

Menu Options:

'Backup Data'
'Reboot ATM'
'Shutdown ATM'
'EXIT'

=== PROJECT STRUCTURE ===

> /res

Stores the simulation data as .csv files. Accounts.csv stores a list of BANK ACCOUNTS. useraccounts.csv stores a list
of USERS and their associated BANK ACCOUNTS. Users.csv stores a list of USER ACCOUNTS.

Hence, you can change the simulation parameters by directly editing the .csv files.
For example, if I want to manually add a new user named 'Chiora Mba-Uzoukwu' and give him a new checking account,
I would make the following additions to the .csvs:
    - Users.csv : add the line '4, Chiora, Mba-Uzoukwu, chioram, test3, 12, 0'
    - Useraccounts.csv : add the line '4, 12'
    - Accounts.csv : add the line '12, 0, 100, <some date>' (sets initial account balance to CAD 100)

Any change made to ATM data in BankServer.java will be written back into the .csvs when the program shuts down. If you
log in as a Bank Manager however, make sure to use th Save Data menu option.

> /src

ATMProgram.java contains the main method body. When run, all the data in /db is set to initial state. If login is
successful, an instance of ATMSim is created. ATMSim.java calls a runATM method which simply acts as a while loop for
the simulation. Additionally, the level of access each user has to the bank server is defined here.
AuthLevel 0 = Regular Client, AuthLevel 1 = Bank manager, AuthLevel 2 = IT Helper. Note that the although each user's
AuthLevel is written into Users.csv, the AuthLevel of a User should NOT be changed once that user has been created once,
EVEN FOR SIMULATION PURPOSES.

> /src/atm/model

Holds Model objects to represent the Server's basic data. The bank sever constructor reads .csv files in /res and
creates/stores instances of these models.

Note that AccountModel is set to store CAD by default.

> /src/atm/server

BankServer.java represents the Bank's remote data. User's interact only with the UI classes in menu.java. The menu
classes instantiate a BankServerConnection which will then send/receive data from Bank Server. Using ServerConnection
classes introduces a level of abstraction, and ensures that the menu classes are not directly loading instances
of the server.

>> .../db

The Table classes hold maps to store bank data and methods to request/perform basic operations on that data.
For example:
        BillsTable.java ---> stores the representation of the ATM's cash
        UserAccountsTable.java ---> stores information about which bank accounts are held by which users.
        AccountsTable.java ----> stores bank accounts

As such,the server holds an instance of each table class.

ExchangeRateTable.java makes use of ExchangeRateScraper.java in ../scraper_tools. ExchangeRateScraper.java updates
the exchange rates of 30 world currencies when BankServer is initialized.

Util.java was created to hold the functions of common utilities in static method bodies, so that they may be created
only once in memory, and can then be easily shared among methods in BankServer.java

> /src/atm/view

Holds all UI classes.

Menu.java is a superclass of the other three menus. The other three menus correspond to a different type of user/
level of access.

=== MISC ===
