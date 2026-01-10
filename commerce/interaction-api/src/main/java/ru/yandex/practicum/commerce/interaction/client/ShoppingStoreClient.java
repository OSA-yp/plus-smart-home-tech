package ru.yandex.practicum.commerce.interaction.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.commerce.interaction.dto.store.ProductDto;
import ru.yandex.practicum.commerce.interaction.dto.store.SetProductQuantityStateRequest;

import java.util.UUID;

@FeignClient(name = "shopping-store")
public interface ShoppingStoreClient {
    @GetMapping("/api/v1/shopping-store")
    Page<ProductDto> getProducts(@RequestParam String category,
                                 @SpringQueryMap Pageable pageable);

    @PutMapping("/api/v1/shopping-store")
    ProductDto createNewProduct(@RequestBody @Valid ProductDto product);

    @PostMapping("/api/v1/shopping-store")
    ProductDto updateProduct(@RequestBody @Valid ProductDto product);

    @PostMapping("/api/v1/shopping-store/removeProductFromStore")
    Boolean removeProductFromStore(@RequestBody UUID productId);

    @PostMapping("/api/v1/shopping-store/quantityState")
    Boolean setProductQuantityState(@RequestBody @Valid SetProductQuantityStateRequest request);

    @GetMapping("/api/v1/shopping-store/{productId}")
    ProductDto getProduct(@PathVariable UUID productId);
}
