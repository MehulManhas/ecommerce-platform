package com.ecommerceproject.InventoryService.Service;

import com.ecommerceproject.InventoryService.Model.Stock;
import com.ecommerceproject.InventoryService.Repository.StockRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class InventoryService {

    private final StockRepository stockRepository;

    public InventoryService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public int getStock(UUID productId) {
        return stockRepository.findById(productId).map(Stock::getQuantity).orElse(0);
    }

    public Stock setStock(UUID productId, int quantity) {
        Stock stock = stockRepository.findById(productId).orElseGet(() -> {
            Stock s = new Stock();
            s.setProductId(productId);
            return s;
        });
        stock.setQuantity(Math.max(quantity, 0));
        return stockRepository.save(stock);
    }

    public void decrement(UUID productId, int quantity) {
        stockRepository.findById(productId).ifPresent(stock -> {
            stock.setQuantity(Math.max(stock.getQuantity() - quantity, 0));
            stockRepository.save(stock);
        });
    }
}
