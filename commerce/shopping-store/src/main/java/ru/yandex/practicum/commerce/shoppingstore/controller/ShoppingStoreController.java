package ru.yandex.practicum.commerce.shoppingstore.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.interaction.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.interaction.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.interaction.dto.store.ProductDto;
import ru.yandex.practicum.commerce.interaction.dto.store.QuantityState;
import ru.yandex.practicum.commerce.interaction.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.shoppingstore.service.ProductService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ShoppingStoreController implements ShoppingStoreClient {

    private final ProductService productService;

    public ShoppingStoreController(ProductService productService) {
        this.productService = productService;
    }

    @Override
    @GetMapping("/shopping-store")
    public Page<ProductDto> getProducts(@RequestParam String category, Pageable pageable) {
        ProductCategory productCategory = ProductCategory.valueOf(category);
        return productService.getProductsByCategory(productCategory, pageable);
    }

    @Override
    @PutMapping("/shopping-store")
    public ProductDto createNewProduct(@RequestBody @Valid ProductDto product) {
        return productService.createProduct(product);
    }

    @Override
    @PostMapping("/shopping-store")
    public ProductDto updateProduct(@RequestBody @Valid ProductDto product) {
        return productService.updateProduct(product);
    }

    @Override
    @PostMapping("/shopping-store/removeProductFromStore")
    public Boolean removeProductFromStore(@RequestBody UUID productId) {
        return productService.removeProductFromStore(productId);
    }

    @Override
    @PostMapping("/shopping-store/quantityState")
    public Boolean setProductQuantityState(@RequestBody @Valid SetProductQuantityStateRequest request) {
        return productService.setProductQuantityState(request);
    }

    //     Альтернативный endpoint для установки статуса количества товара через query параметры.
    //     Используется тестами Postman, непонятно какой из них правильный
    @PostMapping(value = "/shopping-store/quantityState", params = {"productId", "quantityState"})
    public Boolean setProductQuantityState(@RequestParam UUID productId,
                                           @RequestParam String quantityState) {
        SetProductQuantityStateRequest request = new SetProductQuantityStateRequest();
        request.setProductId(productId);
        request.setQuantityState(QuantityState.valueOf(quantityState));
        return productService.setProductQuantityState(request);
    }

    @Override
    @GetMapping("/shopping-store/{productId}")
    public ProductDto getProduct(@PathVariable UUID productId) {
        return productService.getProduct(productId);
    }
}
