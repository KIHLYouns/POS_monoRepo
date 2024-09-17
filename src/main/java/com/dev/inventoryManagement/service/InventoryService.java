// Spring_Backend/src/main/java/com/dev/inventoryManagement/service/InventoryService.java
package com.dev.inventoryManagement.service;

import com.dev.inventoryManagement.models.InventoryItem;
import com.dev.inventoryManagement.repository.InventoryItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    public List<InventoryItem> getAllInventoryItems() {
        return inventoryItemRepository.findAll();
    }

    public Optional<InventoryItem> getInventoryItemById(Long id) {
        return inventoryItemRepository.findById(id);
    }

    public InventoryItem saveInventoryItem(InventoryItem inventoryItem) {
        return inventoryItemRepository.save(inventoryItem);
    }

    @Transactional
    public Optional<InventoryItem> updateInventoryItem(
            Long id,
            InventoryItem inventoryItem
    ) {
        return inventoryItemRepository
                .findById(id)
                .map(existingInventory -> {
                    existingInventory.setProduct(inventoryItem.getProduct());
                    existingInventory.setStockQuantity(inventoryItem.getStockQuantity());
                    existingInventory.setVendorCost(inventoryItem.getVendorCost());
                    existingInventory.setRetailPrice(inventoryItem.getRetailPrice());
                    existingInventory.setBarcode(inventoryItem.getBarcode()); // Update barcode
                    return inventoryItemRepository.save(existingInventory);
                });
    }

    @Transactional
    public void deleteInventoryItem(Long id) {
        inventoryItemRepository.deleteById(id);
    }

    // Add a new method to get inventory by barcode
    public Optional<InventoryItem> getInventoryItemByBarcode(String barcode) {
        return inventoryItemRepository.findByBarcode(barcode);
    }
}
