package com.dev.inventoryManagement.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionDTO {
    private Long id;
    private String date;
    private double totalAmount;
    private List<TransactionItemDTO> items;
}