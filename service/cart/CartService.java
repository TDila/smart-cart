package com.vulcan.smartcart.service.cart;

import com.vulcan.smartcart.dto.CartDto;
import com.vulcan.smartcart.exceptions.ResourceNotFoundException;
import com.vulcan.smartcart.model.Cart;
import com.vulcan.smartcart.model.User;
import com.vulcan.smartcart.repository.CartItemRepository;
import com.vulcan.smartcart.repository.CartRepository;
import com.vulcan.smartcart.service.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService{

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ModelMapper modelMapper;
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final AtomicLong cartIdGenerator = new AtomicLong(0);
    @Override
    public Cart getCart(Long id) {
        logger.info("Fetching cart with ID: {}", id);
        Cart cart = cartRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Cart with ID: {} not found", id);
                    return new ResourceNotFoundException("Cart not found");
                });
        BigDecimal totalAmount = cart.getTotalAmount();
        cart.setTotalAmount(totalAmount);
        logger.info("Returning cart with ID: {} and updated total amount: {}", id, totalAmount);
        return cartRepository.save(cart);
    }

    @Transactional
    @Override
    public void clearCart(Long id) {
        logger.info("Clearing cart with ID: {}", id);
        Cart cart = getCart(id);
        cartItemRepository.deleteAllByCartId(id);
        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.valueOf(0));
        cartRepository.deleteById(id);
        logger.info("Cart with ID: {} cleared and deleted", id);
    }

    @Override
    public BigDecimal getTotalPrice(Long id) {
        logger.info("Fetching total price for cart with ID: {}", id);
        Cart cart = getCart(id);
        BigDecimal totalPrice = cart.getTotalAmount();
        logger.info("Total price for cart with ID: {} is {}", id, totalPrice);
        return totalPrice;
    }

    @Override
    public Cart initializeNewCart(User user) {
        logger.info("Initializing new cart for user ID: {}", user.getId());
        return Optional.ofNullable(getCartByUserId(user.getId()))
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);
                    Cart savedCart = cartRepository.save(cart);
                    logger.info("New cart created for user ID: {}", user.getId());
                    return savedCart;
                });
    }

    @Override
    public Cart getCartByUserId(Long userId) {
        logger.info("Fetching cart for user ID: {}", userId);
        return cartRepository.findByUserId(userId);
    }

    @Override
    public CartDto convertToDto(Cart cart) {
        logger.info("Converting cart with ID: {} to DTO", cart.getId());
        return modelMapper.map(cart, CartDto.class);
    }

}
