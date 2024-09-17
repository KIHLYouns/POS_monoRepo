package com.dev.inventoryManagement.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev.inventoryManagement.models.Transaction;
import com.dev.inventoryManagement.models.TransactionItem;
import com.dev.inventoryManagement.dto.TransactionDTO;
import com.dev.inventoryManagement.dto.TransactionItemDTO;
import com.dev.inventoryManagement.models.InventoryItem;
import com.dev.inventoryManagement.repository.TransactionRepository;

import jakarta.persistence.EntityNotFoundException;

import com.dev.inventoryManagement.repository.TransactionItemRepository;
import com.dev.inventoryManagement.repository.InventoryItemRepository;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private TransactionItemRepository transactionItemRepository;

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    @Transactional
    public Transaction createTransaction(TransactionDTO transactionDTO) {
        Transaction transaction = new Transaction();
        transaction.setDate(transactionDTO.getDate()); // Assuming you're using a suitable date type
        transaction.setTotalAmount(transactionDTO.getTotalAmount());

        List<TransactionItem> transactionItems = transactionDTO.getItems().stream()
                .map(itemDTO -> {
                    TransactionItem item = new TransactionItem();
                    item.setQuantitySold(itemDTO.getQuantitySold());
                    item.setFinalUnitPrice(itemDTO.getFinalUnitPrice());

                    InventoryItem inventoryItem = inventoryItemRepository.findById(itemDTO.getInventoryItemId())
                            .orElseThrow(() -> new EntityNotFoundException("Inventory item not found"));
                    item.setInventoryItem(inventoryItem);
                    item.setTransaction(transaction); // Associate the item with the transaction

                    // Update inventory quantity
                    int newQuantity = inventoryItem.getStockQuantity() - itemDTO.getQuantitySold();
                    if (newQuantity < 0) {
                        throw new IllegalArgumentException("Insufficient quantity for item: " + inventoryItem.getProduct().getName());
                    }
                    inventoryItem.setStockQuantity(newQuantity);
                    inventoryItemRepository.save(inventoryItem);

                    return item;
                })
                .collect(Collectors.toList());

        transaction.setItems(transactionItems);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction saveTransaction(Transaction transaction) {
        // Save the transaction first to get its generated ID
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Now associate each TransactionItem with the saved Transaction
        for (TransactionItem item : transaction.getItems()) {
            item.setTransaction(savedTransaction);
            transactionItemRepository.save(item);
        }

        return savedTransaction;
    }

    @Transactional
    public Optional<Transaction> updateTransaction(Long id, TransactionDTO updatedTransactionDTO) {
        return transactionRepository.findById(id)
                .map(existingTransaction -> {
                    // Check if the transaction exists
                    if (existingTransaction == null) {
                        throw new EntityNotFoundException("Transaction not found with id: " + id);
                    }

                    // Update basic transaction details
                    existingTransaction.setDate(updatedTransactionDTO.getDate());
                    existingTransaction.setTotalAmount(updatedTransactionDTO.getTotalAmount());

                    // Update or add transaction items
                    updateTransactionItems(existingTransaction, updatedTransactionDTO.getItems());

                    return transactionRepository.save(existingTransaction);
                });
    }

    @Transactional
    private void updateTransactionItems(Transaction transaction, List<TransactionItemDTO> itemDTOs) {
        // Create a map of existing transaction items for efficient lookup
        Map<Long, TransactionItem> existingItemsMap = transaction.getItems().stream()
                .collect(Collectors.toMap(TransactionItem::getId, item -> item));

        for (TransactionItemDTO itemDTO : itemDTOs) {
            TransactionItem item;
            if (itemDTO.getId() != null && existingItemsMap.containsKey(itemDTO.getId())) {
                // Update existing item
                item = existingItemsMap.get(itemDTO.getId());
                int originalQuantitySold = item.getQuantitySold();
                item.setQuantitySold(itemDTO.getQuantitySold());
                item.setFinalUnitPrice(itemDTO.getFinalUnitPrice());

                // Adjust inventory if quantitySold changed
                int quantityDifference = itemDTO.getQuantitySold() - originalQuantitySold;
                adjustInventoryQuantity(item.getInventoryItem().getId(), quantityDifference);
            } else {
                // Add new item
                item = new TransactionItem();
                item.setQuantitySold(itemDTO.getQuantitySold());
                item.setFinalUnitPrice(itemDTO.getFinalUnitPrice());

                InventoryItem inventoryItem = inventoryItemRepository.findById(itemDTO.getInventoryItemId())
                        .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + itemDTO.getInventoryItemId()));

                // Deduct from inventory
                adjustInventoryQuantity(inventoryItem.getId(), -itemDTO.getQuantitySold());

                item.setInventoryItem(inventoryItem);
                item.setTransaction(transaction);
                transaction.getItems().add(item);
            }

            transactionItemRepository.save(item);
        }

        // Remove deleted items
        Set<Long> itemDtoIds = itemDTOs.stream()
                .filter(dto -> dto.getId() != null)
                .map(TransactionItemDTO::getId)
                .collect(Collectors.toSet());

        List<TransactionItem> itemsToRemove = transaction.getItems().stream()
                .filter(item -> !itemDtoIds.contains(item.getId()))
                .collect(Collectors.toList());

        for (TransactionItem itemToRemove : itemsToRemove) {
            // Adjust inventory quantity back
            adjustInventoryQuantity(itemToRemove.getInventoryItem().getId(), itemToRemove.getQuantitySold());

            transaction.getItems().remove(itemToRemove); // This is enough to trigger the delete due to cascading
        }
    }

    private void adjustInventoryQuantity(Long inventoryItemId, int quantityDifference) {
        InventoryItem inventoryItem = inventoryItemRepository.findById(inventoryItemId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory item not found with id: " + inventoryItemId));
    
        int newQuantity;
        if (quantityDifference > 0) { 
            // Quantity sold increased, so deduct from stock
            newQuantity = inventoryItem.getStockQuantity() - quantityDifference; 
        } else {
            // Quantity sold decreased, so add back to stock
            newQuantity = inventoryItem.getStockQuantity() - quantityDifference; 
        }
    
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Insufficient quantity for item: " + inventoryItem.getProduct().getName());
        }
        inventoryItem.setStockQuantity(newQuantity);
        inventoryItemRepository.save(inventoryItem);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        // Retrieve the transaction with all its items
        Optional<Transaction> transactionOptional = transactionRepository.findById(id);
        if (transactionOptional.isPresent()) {
            Transaction transaction = transactionOptional.get();
            
            // Adjust inventory quantity for each transaction item, ensuring item is not null
            transaction.getItems().forEach(item -> {
                if (item != null && item.getInventoryItem() != null) {
                    InventoryItem inventoryItem = item.getInventoryItem();
                    int adjustedQuantity = inventoryItem.getStockQuantity() + item.getQuantitySold();
                    inventoryItem.setStockQuantity(adjustedQuantity);
                    inventoryItemRepository.save(inventoryItem);
                }
            });
            
            // Delete the transaction
            transactionRepository.delete(transaction);
        } else {
            throw new EntityNotFoundException("Transaction not found with id: " + id);
        }
    }
}
