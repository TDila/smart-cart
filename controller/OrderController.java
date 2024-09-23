package com.vulcan.smartcart.controller;

import com.vulcan.smartcart.dto.OrderDto;
import com.vulcan.smartcart.exceptions.ResourceNotFoundException;
import com.vulcan.smartcart.model.Order;
import com.vulcan.smartcart.response.ApiResponse;
import com.vulcan.smartcart.service.order.IOrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/orders")
public class OrderController {
    private final IOrderService orderService;
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(@RequestParam Long userId) {
        try {
            Order order = orderService.placeOrder(userId);
            OrderDto orderDto = orderService.convertToDto(order);
            log.info("Order placed successfully for user ID: {}", userId);
            return ResponseEntity.ok(new ApiResponse("Order placed successfully!", orderDto));
        } catch (Exception e) {
            log.error("Error placing order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Error occurred while placing order!", e.getMessage()));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable Long orderId) {
        try {
            OrderDto order = orderService.getOrder(orderId);
            log.info("Retrieved order successfully: ID = {}", orderId);
            return ResponseEntity.ok(new ApiResponse("Order retrieved successfully!", order));
        } catch (ResourceNotFoundException e) {
            log.warn("Order not found: ID = {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("Order not found!", e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse> getUserOrders(@PathVariable Long userId) {
        try {
            List<OrderDto> orders = orderService.getUserOrders(userId);
            log.info("Retrieved orders for user ID: {}", userId);
            return ResponseEntity.ok(new ApiResponse("User orders retrieved successfully!", orders));
        } catch (ResourceNotFoundException e) {
            log.warn("No orders found for user: ID = {}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse("No orders found for this user!", e.getMessage()));
        }
    }

}
