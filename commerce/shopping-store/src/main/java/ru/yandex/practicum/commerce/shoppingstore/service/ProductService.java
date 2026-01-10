package ru.yandex.practicum.commerce.shoppingstore.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.interaction.dto.store.ProductCategory;
import ru.yandex.practicum.commerce.interaction.dto.store.ProductDto;
import ru.yandex.practicum.commerce.interaction.dto.store.ProductState;
import ru.yandex.practicum.commerce.interaction.dto.store.QuantityState;
import ru.yandex.practicum.commerce.interaction.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.shoppingstore.entity.Product;
import ru.yandex.practicum.commerce.shoppingstore.exception.ProductNotFoundException;
import ru.yandex.practicum.commerce.shoppingstore.mapper.ProductMapper;
import ru.yandex.practicum.commerce.shoppingstore.repository.ProductRepository;

import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);
        product.setProductId(null); // Генерируется автоматически
        product.setProductState(ProductState.ACTIVE);
        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductDto updateProduct(ProductDto productDto) {
        if (productDto.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required for update");
        }

        Product existing = productRepository.findByProductId(productDto.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productDto.getProductId()));

        existing.setProductName(productDto.getProductName());
        existing.setDescription(productDto.getDescription());
        existing.setImageSrc(productDto.getImageSrc());
        existing.setQuantityState(productDto.getQuantityState());
        existing.setProductState(productDto.getProductState());
        existing.setProductCategory(productDto.getProductCategory());
        existing.setPrice(productDto.getPrice());

        Product saved = productRepository.save(existing);
        return productMapper.toDto(saved);
    }

    @Transactional
    public Boolean removeProductFromStore(UUID productId) {
        Product product = productRepository.findByProductId(productId)
                .orElse(null);

        if (product == null) {
            return false;
        }

        product.setProductState(ProductState.DEACTIVATE);
        productRepository.save(product);
        return true;
    }

    public ProductDto getProduct(UUID productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId));
        return productMapper.toDto(product);
    }

    public Page<ProductDto> getProductsByCategory(ProductCategory category, Pageable pageable) {
        Page<Product> products = productRepository.findByProductCategoryAndProductState(
                category,
                ProductState.ACTIVE,
                pageable
        );
        return products.map(productMapper::toDto);
    }

    @Transactional
    public Boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        Product product = productRepository.findByProductId(request.getProductId())
                .orElse(null);

        if (product == null) {
            return false;
        }

        product.setQuantityState(request.getQuantityState());
        productRepository.save(product);
        return true;
    }
}
