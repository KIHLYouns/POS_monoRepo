package com.dev.inventoryManagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.inventoryManagement.models.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}