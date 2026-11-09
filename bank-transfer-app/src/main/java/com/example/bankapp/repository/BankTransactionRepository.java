package com.example.bankapp.repository;

import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

    // 1. All transactions, newest first
    @Query("SELECT t FROM BankTransaction t ORDER BY t.createdAt DESC")
    List<BankTransaction> findAllLatest();

    // 2. History of an account (sender or receiver)
    @Query("SELECT t FROM BankTransaction t WHERE t.fromAccount = :account OR t.toAccount = :account ORDER BY t.createdAt DESC")
    List<BankTransaction> findHistory(@Param("account") String account);

    // 3. Filter by status
    @Query("SELECT t FROM BankTransaction t WHERE t.status = :status ORDER BY t.createdAt DESC")
    List<BankTransaction> findByStatusLatest(@Param("status") TransactionStatus status);

    // 4. Count by status for dashboard
    @Query("SELECT COUNT(t) FROM BankTransaction t WHERE t.status = :status")
    long countByStatus(@Param("status") TransactionStatus status);
}
