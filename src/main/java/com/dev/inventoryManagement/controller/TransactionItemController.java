package com.dev.inventoryManagement.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev.inventoryManagement.models.TransactionItem;
import com.dev.inventoryManagement.service.TransactionItemService;

@RestController
@RequestMapping("/api/transactionItems")
public class TransactionItemController {

    @Autowired
    private TransactionItemService transactionItemService;

    @GetMapping
    public List<TransactionItem> getAllTransactionItems() {
        return transactionItemService.getAllTransactionItems();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionItem> getTransactionItemById(@PathVariable Long id) {
        Optional<TransactionItem> transactionItem = transactionItemService.getTransactionItemById(id);
        return transactionItem.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TransactionItem> createTransactionItem(@RequestBody TransactionItem transactionItem) {
        // No need for try-catch here anymore
        TransactionItem createdTransactionItem = transactionItemService.saveTransactionItem(transactionItem);
        return ResponseEntity.ok(createdTransactionItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionItem> updateTransactionItem(@PathVariable Long id, @RequestBody TransactionItem transactionItem) {
        Optional<TransactionItem> updatedTransactionItem = transactionItemService.updateTransactionItem(id, transactionItem);
        return updatedTransactionItem.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransactionItem(@PathVariable Long id) {
        transactionItemService.deleteTransactionItem(id);
        return ResponseEntity.ok().build();
    }
}
