package com.vulcan.smartcart.service.order;

import com.vulcan.smartcart.dto.OrderDto;
import com.vulcan.smartcart.model.Order;

import java.util.List;

public interface IOrderService {
    Order placeOrder(Long userId);
    OrderDto getOrder(Long orderId);
    List<OrderDto> getUserOrders(Long userId);

    OrderDto convertToDto(Order order);
}
