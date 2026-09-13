package com.anshubank.service;

import com.anshubank.model.Account;
import com.anshubank.model.Customer;
import com.anshubank.model.FraudAlert;
import com.anshubank.model.Transaction;
import com.anshubank.repository.AccountRepository;
import com.anshubank.repository.CustomerRepository;
import com.anshubank.repository.FraudAlertRepository;
import com.anshubank.repository.TransactionRepository;
import com.anshubank.util.FileManager;

import java.util.List;

public class ReportService {

    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final FraudAlertRepository fraudAlertRepository;

    public ReportService() {
        this.transactionRepository = new TransactionRepository();
        this.customerRepository = new CustomerRepository();
        this.accountRepository = new AccountRepository();
        this.fraudAlertRepository = new FraudAlertRepository();
    }

    /**
     * Generates a complete transaction report.
     */
    public String transactionReport() throws Exception {

        StringBuilder report =
                new StringBuilder("ANSHUBANK TRANSACTION REPORT\n\n");

        List<Transaction> transactions =
                transactionRepository.all();

        if (transactions.isEmpty()) {
            report.append("No transactions found.\n");
            return report.toString();
        }

        for (Transaction transaction : transactions) {
            report.append(transaction).append('\n');
        }

        return report.toString();
    }

    /**
     * Generates a fraud-alert report.
     */
    public String fraudReport() throws Exception {

        StringBuilder report =
                new StringBuilder("ANSHUBANK FRAUD REPORT\n\n");

        List<FraudAlert> alerts =
                fraudAlertRepository.all();

        if (alerts.isEmpty()) {
            report.append("No fraud alerts found.\n");
            return report.toString();
        }

        for (FraudAlert alert : alerts) {
            report.append(alert).append('\n');
        }

        return report.toString();
    }

    /**
     * Generates a customer report.
     */
    public String customerReport() throws Exception {

        List<Customer> customers =
                customerRepository.findAll();

        StringBuilder report =
                new StringBuilder("ANSHUBANK CUSTOMER REPORT\n\n");

        if (customers.isEmpty()) {
            report.append("No customers found.\n");
            return report.toString();
        }

        for (Customer customer : customers) {
            report.append(customer).append('\n');
        }

        return report.toString();
    }

    /**
     * Generates an account report.
     */
    public String accountReport() throws Exception {

        List<Account> accounts =
                accountRepository.findAll();

        StringBuilder report =
                new StringBuilder("ANSHUBANK ACCOUNT REPORT\n\n");

        if (accounts.isEmpty()) {
            report.append("No accounts found.\n");
            return report.toString();
        }

        for (Account account : accounts) {
            report.append(account).append('\n');
        }

        return report.toString();
    }

    /**
     * Generates a system summary containing
     * important banking statistics.
     */
    public String summary() throws Exception {

        int customerCount =
                customerRepository.findAll().size();

        int accountCount =
                accountRepository.findAll().size();

        long transactionCount =
        transactionRepository.count();

        int suspiciousCount =
                transactionRepository.suspicious().size();

        String deposits =
                transactionRepository.sum("DEPOSIT").toString();

        String withdrawals =
                transactionRepository.sum("WITHDRAWAL").toString();

        String transfers =
                transactionRepository.sum("TRANSFER").toString();

        StringBuilder report =
                new StringBuilder("ANSHUBANK SYSTEM SUMMARY\n\n");

        report.append("Customers: ")
                .append(customerCount)
                .append('\n');

        report.append("Accounts: ")
                .append(accountCount)
                .append('\n');

        report.append("Transactions: ")
                .append(transactionCount)
                .append('\n');

        report.append("Suspicious: ")
                .append(suspiciousCount)
                .append('\n');

        report.append("Deposits: Rs.")
                .append(deposits)
                .append('\n');

        report.append("Withdrawals: Rs.")
                .append(withdrawals)
                .append('\n');

        report.append("Transfers: Rs.")
                .append(transfers)
                .append('\n');

        return report.toString();
    }

    /**
     * Saves a generated report to a file.
     */
    public void save(String fileName, String content) throws Exception {
        FileManager.writeReport(fileName, content);
    }
}