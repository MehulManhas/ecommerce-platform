package com.ecommerceproject.OrderService.Service;

import com.ecommerceproject.OrderService.Client.ProductClient;
import com.ecommerceproject.OrderService.Dto.CreateOrderRequestDto;
import com.ecommerceproject.OrderService.Event.OrderPlacedEvent;
import com.ecommerceproject.OrderService.Messaging.OrderEventPublisher;
import com.ecommerceproject.OrderService.Dto.OrderItemRequestDto;
import com.ecommerceproject.OrderService.Dto.OrderItemResponseDto;
import com.ecommerceproject.OrderService.Dto.OrderResponseDto;
import com.ecommerceproject.OrderService.Dto.ProductDto;
import com.ecommerceproject.OrderService.Model.Order;
import com.ecommerceproject.OrderService.Model.OrderItem;
import com.ecommerceproject.OrderService.Model.OrderStatus;
import com.ecommerceproject.OrderService.Repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(OrderRepository orderRepository,
                        ProductClient productClient,
                        OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.productClient = productClient;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Transactional
    public OrderResponseDto createOrder(Long userId, CreateOrderRequestDto request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must contain at least one item");
        }

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(new Date());

        double total = 0.0;
        for (OrderItemRequestDto itemRequest : request.getItems()) {
            ProductDto product = productClient.getProduct(itemRequest.getProductId());
            if (product == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unknown product: " + itemRequest.getProductId());
            }
            int quantity = Math.max(itemRequest.getQuantity(), 1);

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductTitle(product.getTitle());
            item.setUnitPrice(product.getPrice());
            item.setQuantity(quantity);
            order.addItem(item);

            total += product.getPrice() * quantity;
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        orderEventPublisher.publishOrderPlaced(new OrderPlacedEvent(
                saved.getId(),
                saved.getUserId(),
                saved.getTotalAmount(),
                saved.getItems().size(),
                System.currentTimeMillis()));

        return toResponse(saved);
    }

    public OrderResponseDto getOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + id));
        return toResponse(order);
    }

    public List<OrderResponseDto> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    private OrderResponseDto toResponse(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
        dto.setTotalAmount(order.getTotalAmount());
        dto.setItems(order.getItems().stream().map(item -> {
            OrderItemResponseDto itemDto = new OrderItemResponseDto();
            itemDto.setProductId(item.getProductId());
            itemDto.setProductTitle(item.getProductTitle());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setUnitPrice(item.getUnitPrice());
            return itemDto;
        }).toList());
        return dto;
    }
}
