package com.vulcan.smartcart.service.order;

import com.vulcan.smartcart.dto.OrderDto;
import com.vulcan.smartcart.enums.OrderStatus;
import com.vulcan.smartcart.exceptions.ResourceNotFoundException;
import com.vulcan.smartcart.model.Cart;
import com.vulcan.smartcart.model.Order;
import com.vulcan.smartcart.model.OrderItem;
import com.vulcan.smartcart.model.Product;
import com.vulcan.smartcart.repository.OrderRepository;
import com.vulcan.smartcart.repository.ProductRepository;
import com.vulcan.smartcart.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final ModelMapper modelMapper;
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    @Override
    public Order placeOrder(Long userId) {
        logger.info("Placing order for user with ID: {}", userId);

        // Retrieve cart for the user
        Cart cart = cartService.getCartByUserId(userId);
        logger.info("Cart retrieved for user ID: {} with {} items", userId, cart.getItems().size());

        // Create the order from cart
        Order order = createOrder(cart);
        logger.info("Order created for user ID: {}. Order status set to PENDING", userId);

        // Create order items and update inventory
        List<OrderItem> orderItemList = createOrderItems(order, cart);
        logger.info("Created {} order items for the order. Updating inventory", orderItemList.size());

        // Set order items and calculate total
        order.setOrderItems(new HashSet<>(orderItemList));
        BigDecimal totalAmount = calculateTotalAmount(orderItemList);
        order.setTotalAmount(totalAmount);
        logger.info("Total amount calculated for order: {}", totalAmount);

        // Save the order in the repository
        Order savedOrder = orderRepository.save(order);
        logger.info("Order with ID: {} saved successfully", savedOrder.getOrderId());

        // Clear the cart after placing the order
        cartService.clearCart(cart.getId());
        logger.info("Cart with ID: {} cleared after placing the order", cart.getId());

        return savedOrder;
    }

    private Order createOrder(Cart cart) {
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDate.now());
        logger.info("Order created for user with ID: {}. Status set to PENDING", cart.getUser().getId());
        return  order;
    }

    private List<OrderItem> createOrderItems(Order order, Cart cart){
        return  cart.getItems().stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            int newInventory = product.getInventory() - cartItem.getQuantity();
            product.setInventory(newInventory);
            productRepository.save(product);
            logger.info("Inventory updated for product ID: {}. New inventory: {}", product.getId(), newInventory);

            return  new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    cartItem.getUnitPrice());
        }).toList();
    }

    private BigDecimal calculateTotalAmount(List<OrderItem> orderItemList) {
        BigDecimal total = orderItemList.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        logger.info("Total amount for the order items calculated: {}", total);
        return total;
    }

    @Override
    public OrderDto getOrder(Long orderId) {
        logger.info("Fetching order with ID: {}", orderId);
        return orderRepository.findById(orderId)
                .map(this::convertToDto)
                .orElseThrow(() -> {
                    logger.error("Order with ID: {} not found", orderId);
                    return new ResourceNotFoundException("Order not found");
                });
    }

    @Override
    public List<OrderDto> getUserOrders(Long userId) {
        logger.info("Fetching orders for user with ID: {}", userId);
        List<Order> orders = orderRepository.findByUserId(userId);
        logger.info("Fetched {} orders for user ID: {}", orders.size(), userId);
        return orders.stream().map(this::convertToDto).toList();
    }

    @Override
    public OrderDto convertToDto(Order order) {
        logger.info("Converting order with ID: {} to OrderDto", order.getOrderId());
        return modelMapper.map(order, OrderDto.class);
    }
}
