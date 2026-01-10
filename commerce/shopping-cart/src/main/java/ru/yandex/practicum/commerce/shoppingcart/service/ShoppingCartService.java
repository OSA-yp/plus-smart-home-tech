package ru.yandex.practicum.commerce.shoppingcart.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.client.WarehouseClient;
import ru.yandex.practicum.commerce.interaction.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interaction.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.shoppingcart.entity.CartState;
import ru.yandex.practicum.commerce.shoppingcart.entity.ShoppingCart;
import ru.yandex.practicum.commerce.shoppingcart.exception.InsufficientProductQuantityException;
import ru.yandex.practicum.commerce.shoppingcart.exception.NoProductsInShoppingCartException;
import ru.yandex.practicum.commerce.shoppingcart.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.shoppingcart.mapper.ShoppingCartMapper;
import ru.yandex.practicum.commerce.shoppingcart.repository.ShoppingCartRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ShoppingCartService {

    private final ShoppingCartRepository cartRepository;
    private final ShoppingCartMapper cartMapper;
    private final WarehouseClient warehouseClient;

    public ShoppingCartService(ShoppingCartRepository cartRepository,
                               ShoppingCartMapper cartMapper,
                               WarehouseClient warehouseClient) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
        this.warehouseClient = warehouseClient;
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new NotAuthorizedUserException("Username must not be empty");
        }
    }

    @Transactional
    public ShoppingCartDto getShoppingCart(String username) {
        validateUsername(username);

        ShoppingCart cart = cartRepository.findByUsernameAndCartState(username, CartState.ACTIVE)
                .orElseGet(() -> {
                    ShoppingCart newCart = new ShoppingCart();
                    newCart.setUsername(username);
                    newCart.setCartState(CartState.ACTIVE);
                    return cartRepository.save(newCart);
                });

        return cartMapper.toDto(cart);
    }

    @Transactional
    public ShoppingCartDto addProductToShoppingCart(String username, Map<UUID, Long> products) {
        validateUsername(username);

        ShoppingCart cart = cartRepository.findByUsernameAndCartState(username, CartState.ACTIVE)
                .orElseGet(() -> {
                    ShoppingCart newCart = new ShoppingCart();
                    newCart.setUsername(username);
                    newCart.setCartState(CartState.ACTIVE);
                    return cartRepository.save(newCart);
                });

        if (cart.getCartState() != CartState.ACTIVE) {
            throw new IllegalStateException("Cannot add products to deactivated cart");
        }

        // Создаем временный DTO для проверки на складе (объединяем существующие и новые товары)
        ShoppingCartDto tempCart = new ShoppingCartDto();
        tempCart.setShoppingCartId(cart.getShoppingCartId());
        Map<UUID, Long> combinedProducts = new HashMap<>(cart.getProducts());
        products.forEach((productId, quantity) -> {
            combinedProducts.merge(productId, quantity, Long::sum);
        });
        tempCart.setProducts(combinedProducts);

        try {
            warehouseClient.checkProductQuantityEnoughForShoppingCart(tempCart);
        } catch (Exception e) {
            throw new InsufficientProductQuantityException(
                    "Insufficient product quantity in warehouse: " + e.getMessage());
        }

        // Добавляем/обновляем товары в корзине
        products.forEach((productId, quantity) -> {
            cart.getProducts().merge(productId, quantity, Long::sum);
        });

        ShoppingCart saved = cartRepository.save(cart);
        return cartMapper.toDto(saved);
    }

    @Transactional
    public ShoppingCartDto changeProductQuantity(String username, ChangeProductQuantityRequest request) {
        validateUsername(username);

        ShoppingCart cart = cartRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Shopping cart not found for user: " + username));

        if (!cart.getProducts().containsKey(request.getProductId())) {
            throw new NoProductsInShoppingCartException(
                    "Product not found in shopping cart: " + request.getProductId());
        }

        cart.getProducts().put(request.getProductId(), request.getNewQuantity());
        ShoppingCart saved = cartRepository.save(cart);
        return cartMapper.toDto(saved);
    }

    @Transactional
    public ShoppingCartDto removeFromShoppingCart(String username, List<UUID> productIds) {
        validateUsername(username);

        ShoppingCart cart = cartRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Shopping cart not found for user: " + username));

        boolean removed = false;
        for (UUID productId : productIds) {
            if (cart.getProducts().remove(productId) != null) {
                removed = true;
            }
        }

        if (!removed) {
            throw new NoProductsInShoppingCartException("No specified products found in shopping cart");
        }

        ShoppingCart saved = cartRepository.save(cart);
        return cartMapper.toDto(saved);
    }

    @Transactional
    public void deactivateCurrentShoppingCart(String username) {
        validateUsername(username);

        ShoppingCart cart = cartRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Shopping cart not found for user: " + username));

        cart.setCartState(CartState.DEACTIVATED);
        cartRepository.save(cart);
    }
}
