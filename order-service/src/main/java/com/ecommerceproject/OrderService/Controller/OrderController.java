package com.ecommerceproject.OrderService.Controller;

import com.ecommerceproject.OrderService.Dto.CreateOrderRequestDto;
import com.ecommerceproject.OrderService.Dto.OrderResponseDto;
import com.ecommerceproject.OrderService.Service.OrderService;
import com.ecommerceproject.common.security.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody CreateOrderRequestDto request,
                                                        HttpServletRequest httpRequest) {
        Long userId = currentUserId(httpRequest);
        OrderResponseDto response = orderService.createOrder(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getOrdersByUser(@RequestParam Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
    }

    /** The authenticated user id is placed on the request by the shared token-validation filter. */
    private Long currentUserId(HttpServletRequest request) {
        Object attribute = request.getAttribute(AuthContext.ATTRIBUTE);
        if (attribute instanceof AuthContext authContext && authContext.getUserId() != null) {
            return authContext.getUserId();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user");
    }
}
