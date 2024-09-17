package com.dev.inventoryManagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dev.inventoryManagement.models.TransactionItem;
import com.dev.inventoryManagement.repository.TransactionItemRepository;

@Service
public class TransactionItemService {

    @Autowired
    private TransactionItemRepository transactionItemRepository;

    public List<TransactionItem> getAllTransactionItems() {
        return transactionItemRepository.findAll();
    }

    public Optional<TransactionItem> getTransactionItemById(Long id) {
        return transactionItemRepository.findById(id);
    }

    public TransactionItem saveTransactionItem(TransactionItem transactionItem) {
        return transactionItemRepository.save(transactionItem);
    }

    public Optional<TransactionItem> updateTransactionItem(Long id, TransactionItem updatedTransactionItem) {
        return transactionItemRepository.findById(id)
                .map(existingTransactionItem -> {
                    if (updatedTransactionItem.getInventoryItem() != null) {
                        existingTransactionItem.setInventoryItem(updatedTransactionItem.getInventoryItem());
                    }
                    existingTransactionItem.setQuantitySold(updatedTransactionItem.getQuantitySold());
                    existingTransactionItem.setFinalUnitPrice(updatedTransactionItem.getFinalUnitPrice());
                    return transactionItemRepository.save(existingTransactionItem);
                });
    }

    public void deleteTransactionItem(Long id) {
        transactionItemRepository.deleteById(id);
    }
}
