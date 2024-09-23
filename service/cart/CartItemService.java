package com.vulcan.smartcart.service.cart;

import com.vulcan.smartcart.exceptions.ResourceNotFoundException;
import com.vulcan.smartcart.model.Cart;
import com.vulcan.smartcart.model.CartItem;
import com.vulcan.smartcart.model.Product;
import com.vulcan.smartcart.repository.CartItemRepository;
import com.vulcan.smartcart.repository.CartRepository;
import com.vulcan.smartcart.service.product.IProductService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartItemService implements ICartItemService{
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final IProductService productService;
    private final ICartService cartService;
    private static final Logger logger = LoggerFactory.getLogger(CartItemService.class);
    @Override
    public void addItemToCart(Long cartId, Long productId, int quantity) {
        logger.info("Adding item to cart. Cart ID: {}, Product ID: {}, Quantity: {}", cartId, productId, quantity);

        Cart cart = cartService.getCart(cartId);
        Product product = productService.getProductById(productId);

        CartItem cartItem = cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(new CartItem());

        if (cartItem.getId() == null) {
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setUnitPrice(product.getPrice());
            logger.info("New item added to cart. Cart ID: {}, Product ID: {}, Quantity: {}, Unit Price: {}",
                    cartId, productId, quantity, product.getPrice());
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            logger.info("Existing item quantity updated in cart. Cart ID: {}, Product ID: {}, New Quantity: {}",
                    cartId, productId, cartItem.getQuantity());
        }

        cartItem.setTotalPrice();
        cart.addItem(cartItem);
        cartItemRepository.save(cartItem);
        cartRepository.save(cart);
        logger.info("Cart updated successfully. Cart ID: {}", cartId);
    }

    @Override
    public void removeItemFromCart(Long cartId, Long productId) {
        logger.info("Removing item from cart. Cart ID: {}, Product ID: {}", cartId, productId);

        Cart cart = cartService.getCart(cartId);
        CartItem itemToRemove = getCartItem(cartId, productId);

        cart.removeItem(itemToRemove);
        cartRepository.save(cart);
        logger.info("Item removed from cart. Cart ID: {}, Product ID: {}", cartId, productId);
    }

    @Override
    public void updateItemQuantity(Long cartId, Long productId, int quantity) {
        logger.info("Updating item quantity in cart. Cart ID: {}, Product ID: {}, New Quantity: {}", cartId, productId, quantity);

        Cart cart = cartService.getCart(cartId);

        cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    item.setQuantity(quantity);
                    item.setUnitPrice(item.getProduct().getPrice());
                    item.setTotalPrice();
                    logger.info("Item quantity updated. Cart ID: {}, Product ID: {}, New Quantity: {}, Total Price: {}",
                            cartId, productId, quantity, item.getTotalPrice());
                });

        BigDecimal totalAmount = cart.getItems()
                .stream().map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalAmount(totalAmount);
        cartRepository.save(cart);
        logger.info("Cart total amount updated. Cart ID: {}, New Total Amount: {}", cartId, totalAmount);
    }

    @Override
    public CartItem getCartItem(Long cartId, Long productId) {
        logger.info("Fetching cart item. Cart ID: {}, Product ID: {}", cartId, productId);

        Cart cart = cartService.getCart(cartId);
        return cart.getItems()
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Item not found in cart. Cart ID: {}, Product ID: {}", cartId, productId);
                    return new ResourceNotFoundException("Item not found");
                });
    }

}
