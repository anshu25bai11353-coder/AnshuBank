package com.anshubank.cli;

import com.anshubank.database.*;
import com.anshubank.enums.*;
import com.anshubank.model.*;
import com.anshubank.repository.*;
import com.anshubank.service.*;
import com.anshubank.security.SecurityUtil;
import com.anshubank.util.*;
import com.anshubank.concurrency.*;

import java.math.*;
import java.io.*;
import java.util.*;

public class BankApplication {

    private final Scanner in = new Scanner(System.in);

    private final AuthenticationService auth = new AuthenticationService();
    private final AccountService accountService = new AccountService();
    private final TransactionService txn = new TransactionService();
    private final AdminService admin = new AdminService();
    private final ReportService reports = new ReportService();

    public void start() {

        DatabaseManager.getInstance().initialize();

        while (true) {

            header("ANSHUBANK", "Banking & Fraud Monitoring System");

            System.out.println("1. Customer Login");
            System.out.println("2. Create New Account");
            System.out.println("3. Admin Login");
            System.out.println("4. Exit");

            String c = prompt("Enter choice: ");

            try {

                switch (c) {

                    case "1" -> customerLogin();

                    case "2" -> createAccount();

                    case "3" -> adminLogin();

                    case "4" -> {
                        JpaManager.close();
                        System.out.println("Goodbye.");
                        return;
                    }

                    default -> error("Invalid choice.");
                }

            } catch (Exception e) {

                error(e.getMessage());
                LoggerUtil.error(e.toString());
            }
        }
    }

    private void customerLogin() throws Exception {

        String id = prompt("Customer ID: ");

        String pw = password("Password: ");

        var c = auth.customer(id, pw);

        if (c.isEmpty()) {
            error("Invalid customer credentials.");
            return;
        }

        LoggerUtil.info("Customer login success: " + id);

        customerMenu(c.get());
    }

   private void customerMenu(Customer c) throws Exception {

    while (true) {

        header(
                "CUSTOMER DASHBOARD",
                "Welcome, " + c.getName()
        );

        System.out.println("1. View Account Details");
        System.out.println("2. Check Balance");
        System.out.println("3. Deposit Money");
        System.out.println("4. Withdraw Money");
        System.out.println("5. Transfer Money");
        System.out.println("6. Transaction History");
        System.out.println("7. View Security Alerts");
        System.out.println("8. Generate Bank Statement");
        System.out.println("9. Change Password");
        System.out.println("10. Logout");

        String choice = prompt("Enter choice: ");

        try {

            switch (choice) {

                case "1" -> showAccounts(c.getCustomerId());

                case "2" -> balance();

                case "3" -> deposit();

                case "4" -> withdraw();

                case "5" -> transfer();

                case "6" -> history();

                case "7" -> alerts();

                case "8" -> statement();

                case "9" -> changePassword(c);

                case "10" -> {
                    success("Logged out successfully.");
                    return;
                }

                default -> error("Invalid choice.");
            }

        } catch (Exception e) {

            String message = e.getMessage();

            if (message == null || message.isBlank()) {
                message = "Operation could not be completed.";
            }

            error(message);

            System.out.println();
            System.out.println("Transaction could not be completed.");
            System.out.println("Returning to Customer Dashboard...");
            System.out.println();
        }
    }
}

    private void createAccount() throws Exception {

        header("CREATE ACCOUNT", "");

        String n = prompt("Customer name: ");

        String e = prompt("Email: ");

        String ph = prompt("Phone (10 digits): ");

        String pw = password("Password (min 8 chars): ");

        if (n.isBlank()
                || !InputValidator.email(e)
                || !InputValidator.phone(ph)
                || !InputValidator.password(pw)) {

            error("Invalid customer details.");
            return;
        }

        AccountType t = prompt("Account type (SAVINGS/CURRENT): ")
                .equalsIgnoreCase("CURRENT")
                        ? AccountType.CURRENT
                        : AccountType.SAVINGS;

        BigDecimal initial = amount();

        if (initial == null) {
            error("Invalid initial deposit.");
            return;
        }

        String[] ids = accountService
                .createCustomerAndAccount(n, e, ph, pw, t, initial)
                .split("\\|");

        success(
                "Account created. Customer ID: "
                        + ids[0]
                        + " | Account ID: "
                        + ids[1]
        );
    }

    private void adminLogin() throws Exception {

        String id = prompt("Admin ID: ");

        String pw = password("Password: ");

        if (!auth.admin(id, pw)) {

            error("Invalid administrator credentials.");
            return;
        }

        LoggerUtil.info("Admin login success: " + id);

        adminMenu();
    }

    private void adminMenu() throws Exception {

        while (true) {

            header("ADMIN DASHBOARD", "");

            System.out.println("1. View All Customers");
            System.out.println("2. View All Accounts");
            System.out.println("3. Search Customer");
            System.out.println("4. Search Account");
            System.out.println("5. View All Transactions");
            System.out.println("6. View Suspicious Transactions");
            System.out.println("7. Review Fraud Alerts");
            System.out.println("8. Manage Accounts");
            System.out.println("9. Generate Reports");
            System.out.println("10. System Statistics");
            System.out.println("11. Transaction Processing Monitor");
            System.out.println("12. Logout");

            switch (prompt("Enter choice: ")) {

                case "1" -> admin.customers
                        .findAll()
                        .forEach(x -> System.out.println(
                                x.getCustomerId()
                                        + " | "
                                        + x.getName()
                                        + " | "
                                        + x.getEmail()
                                        + " | "
                                        + x.getPhone()
                                        + " | "
                                        + x.getStatus()
                        ));

                case "2" -> admin.accounts
                        .findAll()
                        .forEach(this::printAccount);

                case "3" -> admin.customers
                        .search(prompt("Search: "))
                        .forEach(x -> System.out.println(
                                x.getCustomerId()
                                        + " | "
                                        + x.getName()
                                        + " | "
                                        + x.getEmail()
                        ));

                case "4" -> admin.accounts
                        .search(prompt("Search: "))
                        .forEach(this::printAccount);

                case "5" -> showAllTransactions();

                case "6" -> showSuspiciousTransactions();

                case "7" -> reviewAlerts();

                case "8" -> manageAccount();

                case "9" -> generateReports();

                case "10" -> System.out.println(admin.statistics());

                case "11" -> concurrencyDemo();

                case "12" -> {
                    return;
                }

                default -> error("Invalid choice.");
            }
        }
    }

private void showAllTransactions() throws Exception {

    List<Transaction> transactions = admin.transactions.all();

    header("ALL TRANSACTIONS", "Administrator View");

    if (transactions.isEmpty()) {
        System.out.println("No transactions found.");
        return;
    }

    System.out.printf(
            "%-18s %-12s %-14s %-12s %-10s %-12s%n",
            "TRANSACTION ID",
            "TYPE",
            "AMOUNT",
            "STATUS",
            "RISK",
            "DATE"
    );

    System.out.println(
            "--------------------------------------------------------------------------------"
    );

    for (Transaction t : transactions) {

        String id = t.transactionId();

        if (id != null && id.length() > 18) {
            id = id.substring(0, 18);
        }

        String date = "";

        if (t.timestamp() != null) {
            date = t.timestamp().toString();

            if (date.length() >= 10) {
                date = date.substring(0, 10);
            }
        }

        System.out.printf(
                "%-18s %-12s Rs.%-11.2f %-12s %-10s %-12s%n",
                id,
                t.type(),
                t.amount(),
                t.status(),
                t.riskLevel(),
                date
        );
    }

    System.out.println(
            "--------------------------------------------------------------------------------"
    );

    System.out.println(
            "Total Transactions: " + transactions.size()
    );
}


private void showSuspiciousTransactions() throws Exception {

    List<Transaction> transactions =
            admin.transactions.suspicious();

    header(
            "SUSPICIOUS TRANSACTIONS",
            "Administrator Security Monitor"
    );

    if (transactions.isEmpty()) {
        System.out.println("No suspicious transactions found.");
        return;
    }

    System.out.printf(
            "%-18s %-12s %-14s %-12s %-10s %-12s%n",
            "TRANSACTION ID",
            "TYPE",
            "AMOUNT",
            "STATUS",
            "RISK",
            "DATE"
    );

    System.out.println(
            "--------------------------------------------------------------------------------"
    );

    for (Transaction t : transactions) {

        String id = t.transactionId();

        if (id != null && id.length() > 18) {
            id = id.substring(0, 18);
        }

        String date = "";

        if (t.timestamp() != null) {
            date = t.timestamp().toString();

            if (date.length() >= 10) {
                date = date.substring(0, 10);
            }
        }

        System.out.printf(
                "%-18s %-12s Rs.%-11.2f %-12s %-10s %-12s%n",
                id,
                t.type(),
                t.amount(),
                t.status(),
                t.riskLevel(),
                date
        );
    }

    System.out.println(
            "--------------------------------------------------------------------------------"
    );

    System.out.println(
            "Suspicious Transactions: " + transactions.size()
    );
}

    private void showAccounts(String cid) throws Exception {

        for (Account a : accountService.customerAccounts(cid)) {
            printAccount(a);
        }
    }

    private void balance() throws Exception {

        String id = prompt("Account ID: ");

        Account a = admin.accounts
                .findById(id)
                .orElseThrow();

        System.out.println("Account ID: " + id);
        System.out.println("Account Type: " + a.getAccountType());
        System.out.println("Available Balance: Rs." + a.getBalance());
        System.out.println("Account Status: " + a.getStatus());
    }

    private void deposit() throws Exception {

        String id = prompt("Account ID: ");

        BigDecimal a = amount();

        if (a == null) {
            error("Invalid amount.");
            return;
        }

        Transaction t = txn.deposit(
                id,
                a,
                "Customer deposit"
        );

        success(
                "Previous/new balance available in account. "
                        + "Transaction ID: "
                        + t.transactionId()
                        + " | Status: "
                        + t.status()
                        + " | Risk: "
                        + t.riskLevel()
        );
    }

    private void withdraw() throws Exception {

        String id = prompt("Account ID: ");

        BigDecimal a = amount();

        if (a == null) {
            error("Invalid amount.");
            return;
        }

        Transaction t = txn.withdraw(id, a);

        success(
                "Transaction ID: "
                        + t.transactionId()
                        + " | Status: "
                        + t.status()
        );
    }

    private void transfer() throws Exception {

        String f = prompt("Sender Account ID: ");

        String to = prompt("Receiver Account ID: ");

        BigDecimal a = amount();

        if (a == null) {
            error("Invalid amount.");
            return;
        }

        Transaction t = txn.transfer(f, to, a);

        success(
                "Transfer complete. Transaction ID: "
                        + t.transactionId()
                        + " | Status: "
                        + t.status()
                        + " | Risk: "
                        + t.riskLevel()
        );
    }

    private void history() throws Exception {
    String accountId = prompt("Account ID: ");

    if (accountId.isBlank()) {
        error("Account ID cannot be empty.");
        return;
    }

    List<Transaction> transactions = txn.history(accountId);

    header("TRANSACTION HISTORY", "Account ID: " + accountId);

    if (transactions.isEmpty()) {
        System.out.println("No transactions found.");
        return;
    }

    System.out.println();
    System.out.printf(
            "%-20s %-10s %-15s %-12s %-10s%n",
            "TRANSACTION ID",
            "TYPE",
            "AMOUNT",
            "STATUS",
            "RISK"
    );

    System.out.println("--------------------------------------------------------------------------");

    for (Transaction t : transactions) {
        String id = t.transactionId();

        if (id != null && id.length() > 18) {
            id = id.substring(0, 18);
        }

        System.out.printf(
                "%-20s %-10s Rs.%-12.2f %-12s %-10s%n",
                id,
                t.type(),
                t.amount(),
                t.status(),
                t.riskLevel()
        );
    }

    System.out.println("--------------------------------------------------------------------------");
    System.out.println("Total Transactions: " + transactions.size());
}

    private void alerts() throws Exception {

    String accountId = prompt("Account ID: ");

    if (accountId.isBlank()) {
        error("Account ID cannot be empty.");
        return;
    }

    List<FraudAlert> alerts = txn.alertsFor(accountId);

    header(
            "SECURITY ALERTS",
            "Account ID: " + accountId
    );

    if (alerts.isEmpty()) {

        System.out.println(
                "No fraud alerts found for this account."
        );

        System.out.println(
                "The account currently has no flagged transactions."
        );

        return;
    }

    System.out.println(
            "Total Fraud Alerts: " + alerts.size()
    );

    System.out.println(
            "----------------------------------------"
    );

    for (FraudAlert alert : alerts) {

        System.out.println(
                "Alert ID       : " + alert.alertId()
        );

        System.out.println(
                "Transaction ID : " + alert.transactionId()
        );

        System.out.println(
                "Risk Score     : " + alert.riskScore()
        );

        System.out.println(
                "Risk Level     : " + alert.riskLevel()
        );

        System.out.println(
                "Status         : " + alert.reviewStatus()
        );

        System.out.println(
                "Created At     : " + alert.createdAt()
        );

        System.out.println(
                "Reason         : " + alert.ruleTriggered()
        );

        System.out.println(
                "----------------------------------------"
        );
    }
}

    private void statement() throws Exception {

        String id = prompt("Account ID: ");

        Account a = admin.accounts
                .findById(id)
                .orElseThrow();

        StringBuilder b = new StringBuilder();

        b.append("ANSHUBANK STATEMENT\n");
        b.append("Account ID: ").append(id).append("\n");
        b.append("Account Type: ")
                .append(a.getAccountType())
                .append("\n");
        b.append("Closing Balance: Rs.")
                .append(a.getBalance())
                .append("\n\n");
        b.append("Transactions\n");

        txn.history(id).forEach(t ->
                b.append(t.type())
                        .append(" ")
                        .append(t.amount())
                        .append(" | ")
                        .append(t.timestamp())
                        .append(" | ")
                        .append(t.status())
                        .append("\n")
        );

        reports.save(
                "statement_" + id + ".txt",
                b.toString()
        );

        success(
                "Statement saved to reports/statement_"
                        + id
                        + ".txt"
        );
    }

    private void changePassword(Customer c) throws Exception {

        String old = password("Current password: ");

        if (!SecurityUtil.matches(
                old,
                c.getPasswordHash()
        )) {

            error("Current password is incorrect.");
            return;
        }

        String nw = password("New password: ");

        String confirm = password("Confirm password: ");

        if (!InputValidator.password(nw)
                || !nw.equals(confirm)) {

            error("Password validation failed.");
            return;
        }

        new CustomerRepository().updatePassword(
                c.getCustomerId(),
                SecurityUtil.hash(nw)
        );

        success("Password changed.");
    }

    
    private void reviewAlerts() throws Exception {
    
        List<FraudAlert> alerts = admin.alerts.all();
    
        header("FRAUD ALERTS", "Review & Update");
    
        if (alerts.isEmpty()) {
            System.out.println("No fraud alerts found.");
            return;
        }
    
        System.out.println(
                "---------------------------------------------------------------------------------------------"
        );
        System.out.printf(
                "%-22s %-22s %-30s %-10s %-12s%n",
                "Alert ID",
                "Transaction",
                "Rule",
                "Risk",
                "Status"
        );
        System.out.println(
                "---------------------------------------------------------------------------------------------"
        );
    
        for (FraudAlert alert : alerts) {
    
            String alertId = alert.alertId();
            String transactionId = alert.transactionId();
            String rule = alert.ruleTriggered();
    
            // Keep long values inside their columns
            if (alertId.length() > 22) {
                alertId = alertId.substring(0, 19) + "...";
            }
    
            if (transactionId.length() > 22) {
                transactionId = transactionId.substring(0, 19) + "...";
            }
    
            if (rule.length() > 30) {
                rule = rule.substring(0, 27) + "...";
            }
    
            System.out.printf(
                    "%-22s %-22s %-30s %-10s %-12s%n",
                    alertId,
                    transactionId,
                    rule,
                    alert.riskLevel(),
                    alert.reviewStatus()
            );
        }

    System.out.println(
            "---------------------------------------------------------------------------------------------"
    );

    String id = prompt(
            "Alert ID to update (blank to cancel): "
    ).trim();

    if (id.isEmpty()) {
        System.out.println("Operation cancelled.");
        return;
    }

    FraudAlert selectedAlert = null;

    for (FraudAlert alert : alerts) {

        if (alert.alertId().equalsIgnoreCase(id)) {
            selectedAlert = alert;
            break;
        }
    }

    if (selectedAlert == null) {
        error("Alert ID not found: " + id);
        return;
    }

    System.out.println();
    System.out.println("Selected Alert");
    System.out.println("----------------------------------------");
    System.out.println("Alert ID       : " + selectedAlert.alertId());
    System.out.println("Transaction ID : " + selectedAlert.transactionId());
    System.out.println("Rule Triggered : " + selectedAlert.ruleTriggered());
    System.out.println("Risk Score     : " + selectedAlert.riskScore());
    System.out.println("Risk Level     : " + selectedAlert.riskLevel());
    System.out.println("Current Status : " + selectedAlert.reviewStatus());
    System.out.println("----------------------------------------");

    String statusInput = prompt(
            "New Status (OPEN/UNDER_REVIEW/CLEARED/BLOCKED): "
    ).trim();

    if (statusInput.isEmpty()) {
        System.out.println("Operation cancelled.");
        return;
    }

    AlertStatus status;

    try {
        status = AlertStatus.valueOf(
                statusInput.toUpperCase()
        );
    } catch (IllegalArgumentException e) {
        error(
                "Invalid status. Use OPEN, UNDER_REVIEW, CLEARED or BLOCKED."
        );
        return;
    }

    if (selectedAlert.reviewStatus() == status) {
        System.out.println(
                "Alert is already marked as " + status + "."
        );
        return;
    }

    admin.review(
            selectedAlert.alertId(),
            status
    );

    success(
            "Alert " + selectedAlert.alertId()
                    + " updated to " + status + "."
    );
}



    private void manageAccount() throws Exception {

        String id = prompt("Account ID: ");

        String s = prompt(
                "Status (ACTIVE/SUSPENDED/CLOSED): "
        );

        accountService.status(
                id,
                AccountStatus.valueOf(s.toUpperCase())
        );

        success("Account status updated.");
    }

    private void generateReports() throws Exception {

        reports.save(
                "transactions.txt",
                reports.transactionReport()
        );

        reports.save(
                "fraud.txt",
                reports.fraudReport()
        );

        reports.save(
                "customers.txt",
                reports.customerReport()
        );

        reports.save(
                "accounts.txt",
                reports.accountReport()
        );

        reports.save(
                "system_summary.txt",
                reports.summary()
        );

        success("Reports generated in reports/.");
    }

    private void concurrencyDemo() throws Exception {

        String id = prompt(
                "Account ID for controlled test: "
        );

        try (TransactionProcessor p =
                     new TransactionProcessor(txn)) {

            p.demo(id);
            p.await();
        }

        System.out.println(
                "Safe result: one withdrawal may succeed; "
                        + "the other must fail if balance is 50000. "
                        + "Final balance: "
                        + admin.accounts
                                .findById(id)
                                .map(Account::getBalance)
                                .orElse(null)
        );
    }

    private void printAccount(Account a) {

        System.out.println(
                a.getAccountId()
                        + " | "
                        + a.getCustomerId()
                        + " | "
                        + a.getAccountType()
                        + " | Rs."
                        + a.getBalance()
                        + " | "
                        + a.getStatus()
                        + " | "
                        + a.getCreatedAt()
        );
    }

    private BigDecimal amount() {

        String s = prompt("Amount: ");

        return InputValidator.amount(s);
    }

    private String prompt(String s) {

        System.out.print(s);

        return in.nextLine().trim();
    }

    /*
     * Password input with * masking.
     * Works with macOS Terminal and VS Code terminal.
     */
    private String password(String prompt) {

        System.out.print(prompt);

        StringBuilder password = new StringBuilder();

        try {

            Process disableEcho = new ProcessBuilder(
                    "sh",
                    "-c",
                    "stty -echo -icanon min 1"
            ).inheritIO().start();

            disableEcho.waitFor();

            while (true) {

                int ch = System.in.read();

                if (ch == '\n' || ch == '\r') {
                    break;
                }

                // Backspace
                if (ch == 127 || ch == 8) {

                    if (password.length() > 0) {

                        password.deleteCharAt(
                                password.length() - 1
                        );

                        System.out.print("\b \b");
                    }

                    continue;
                }

                // Ctrl + C
                if (ch == 3) {
                    throw new InterruptedException(
                            "Password input cancelled."
                    );
                }

                password.append((char) ch);

                System.out.print("*");
            }

            System.out.println();

        } catch (Exception e) {

            System.out.println();

            /*
             * If terminal masking is unavailable,
             * use the normal console password input.
             */
            Console console = System.console();

            if (console != null) {

                char[] p = console.readPassword(
                        "Password: "
                );

                return new String(p);
            }

            return "";
            
        } finally {

            try {

                Process restoreEcho = new ProcessBuilder(
                        "sh",
                        "-c",
                        "stty echo icanon"
                ).inheritIO().start();

                restoreEcho.waitFor();

            } catch (Exception ignored) {
                // Keep terminal restoration failure silent.
            }
        }

        return password.toString();
    }

    private void header(String a, String b) {

        System.out.println(
                "\n========================================"
        );

        System.out.println(
                "              " + a
        );

        System.out.println(
                "========================================"
        );

        if (!b.isBlank()) {
            System.out.println(b);
        }

        System.out.println();
    }

    private void success(String s) {

        System.out.println(
                "[ SUCCESS ] " + s
        );
    }

    private void error(String s) {

        System.out.println(
                "[ ERROR ] " + s
        );
    }
}

