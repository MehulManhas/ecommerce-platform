package com.ecommerceproject.InventoryService.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "stock")
@Getter
@Setter
public class Stock {
    @Id
    private UUID productId;
    private int quantity;
}
