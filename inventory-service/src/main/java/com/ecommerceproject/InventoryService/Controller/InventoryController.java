package com.ecommerceproject.InventoryService.Controller;

import com.ecommerceproject.InventoryService.Model.Stock;
import com.ecommerceproject.InventoryService.Service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getStock(@PathVariable UUID productId) {
        return ResponseEntity.ok(Map.of("productId", productId, "quantity", inventoryService.getStock(productId)));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Stock> setStock(@PathVariable UUID productId, @RequestBody Map<String, Integer> body) {
        int quantity = body.getOrDefault("quantity", 0);
        return ResponseEntity.ok(inventoryService.setStock(productId, quantity));
    }
}
