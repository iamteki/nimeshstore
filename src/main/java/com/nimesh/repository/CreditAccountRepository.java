package com.nimesh.repository;

import com.nimesh.model.CreditAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CreditAccountRepository extends JpaRepository<CreditAccount, Long> {
    CreditAccount findByCustomerId(Long customerId);
    
    @Query("SELECT ca FROM CreditAccount ca WHERE ca.balance > 0")
    List<CreditAccount> findAllWithOutstandingBalance();
    
    @Query("SELECT SUM(ca.balance) FROM CreditAccount ca")
    BigDecimal getTotalOutstandingCredit();
}