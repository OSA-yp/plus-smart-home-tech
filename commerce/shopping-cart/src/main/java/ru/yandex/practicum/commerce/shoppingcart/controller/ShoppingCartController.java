package ru.yandex.practicum.commerce.shoppingcart.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction.client.ShoppingCartClient;
import ru.yandex.practicum.commerce.interaction.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.interaction.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.commerce.shoppingcart.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ShoppingCartController implements ShoppingCartClient {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @Override
    @GetMapping("/shopping-cart")
    public ShoppingCartDto getShoppingCart(@RequestParam String username) {
        return shoppingCartService.getShoppingCart(username);
    }

    @Override
    @PutMapping("/shopping-cart")
    public ShoppingCartDto addProductToShoppingCart(@RequestParam String username,
                                                    @RequestBody Map<UUID, Long> products) {
        return shoppingCartService.addProductToShoppingCart(username, products);
    }

    @Override
    @DeleteMapping("/shopping-cart")
    public void deactivateCurrentShoppingCart(@RequestParam String username) {
        shoppingCartService.deactivateCurrentShoppingCart(username);
    }

    @Override
    @PostMapping("/shopping-cart/remove")
    public ShoppingCartDto removeFromShoppingCart(@RequestParam String username,
                                                  @RequestBody List<UUID> productIds) {
        return shoppingCartService.removeFromShoppingCart(username, productIds);
    }

    @Override
    @PostMapping("/shopping-cart/change-quantity")
    public ShoppingCartDto changeProductQuantity(@RequestParam String username,
                                                 @RequestBody @Valid ChangeProductQuantityRequest request) {
        return shoppingCartService.changeProductQuantity(username, request);
    }
}
