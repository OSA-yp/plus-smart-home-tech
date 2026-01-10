package ru.yandex.practicum.commerce.shoppingcart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.shoppingcart.entity.CartState;
import ru.yandex.practicum.commerce.shoppingcart.entity.ShoppingCart;

import java.util.Optional;
import java.util.UUID;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {
    Optional<ShoppingCart> findByUsernameAndCartState(String username, CartState state);

    Optional<ShoppingCart> findByUsername(String username);
}
