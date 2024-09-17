package com.vulcan.smartcart.service.cart;

import com.vulcan.smartcart.model.Cart;
import com.vulcan.smartcart.model.User;

import java.math.BigDecimal;

public interface ICartService {
    Cart getCart(Long id);
    void clearCart(Long id);
    BigDecimal getTotalPrice(Long id);
    Cart initializeNewCart(User user);
    Cart getCartByUserId(Long userId);
}
