package com.dev.inventoryManagement.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionItemDTO {
    private Long id;
    private Long inventoryItemId;
    private int quantitySold;
    private double finalUnitPrice;
}