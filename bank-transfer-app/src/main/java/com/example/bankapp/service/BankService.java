package com.example.bankapp.service;

import com.example.bankapp.exception.AccountNotFoundException;
import com.example.bankapp.exception.InsufficientBalanceException;
import com.example.bankapp.exception.InvalidTransactionDataException;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.repository.BankTransactionRepository;
import com.example.bankapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;
    private final TransactionAuditService auditService;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAllOrdered();
    }

    public List<Customer> searchCustomers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCustomers();
        }
        return customerRepository.searchByName(keyword);
    }

    public Customer getCustomerByAccountNumber(String accountNumber) {
        return customerRepository.getByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
    }

    public BigDecimal getTotalBalance() {
        BigDecimal total = customerRepository.getTotalBalance();
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<BankTransaction> getAllTransactions() {
        return transactionRepository.findAllLatest();
    }

    public List<BankTransaction> getAccountHistory(String accountNumber) {
        return transactionRepository.findHistory(accountNumber);
    }

    public long getCountByStatus(TransactionStatus status) {
        return transactionRepository.countByStatus(status);
    }

    @Transactional
    public Customer createCustomer(Customer customer) {
        if (customerRepository.countAccounts(customer.getAccountNumber()) > 0) {
            throw new InvalidTransactionDataException("Account number already exists: " + customer.getAccountNumber());
        }
        if (customerRepository.countEmails(customer.getEmail()) > 0) {
            throw new InvalidTransactionDataException("Email already exists: " + customer.getEmail());
        }
        return customerRepository.save(customer);
    }

    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(String fromAccount, String toAccount, BigDecimal amount, boolean simulateFailure) {
        // Step 1: Validation
        if (fromAccount.equals(toAccount)) {
            throw new InvalidTransactionDataException("Sender and receiver accounts must be different.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionDataException("Transfer amount must be greater than zero.");
        }

        Customer sender = getCustomerByAccountNumber(fromAccount);
        Customer receiver = getCustomerByAccountNumber(toAccount);

        // Step 2: Debit sender
        int updatedRows = customerRepository.debit(fromAccount, amount);
        if (updatedRows == 0) {
            throw new InsufficientBalanceException("Insufficient balance in account " + fromAccount);
        }

        // Step 3: Simulate failure experiment
        if (simulateFailure) {
            throw new RuntimeException("Simulated system failure occurred!");
        }

        // Step 4: Credit receiver
        customerRepository.credit(toAccount, amount);

        // Step 5: Save SUCCESS record
        BankTransaction tx = new BankTransaction();
        tx.setFromAccount(fromAccount);
        tx.setToAccount(toAccount);
        tx.setAmount(amount);
        tx.setDescription("Transfer completed");
        tx.setStatus(TransactionStatus.SUCCESS);
        return transactionRepository.save(tx);
    }
}