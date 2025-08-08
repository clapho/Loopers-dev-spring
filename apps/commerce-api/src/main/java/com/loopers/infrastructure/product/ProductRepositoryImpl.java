package com.loopers.infrastructure.product;

import com.loopers.application.product.ProductSortOption;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return productJpaRepository.findById(id);
    }

    @Override
    public Optional<Product> findByIdWithLock(Long id) {
        return productJpaRepository.findByIdWithLock(id);
    }

    @Override
    public boolean existsById(Long id) {
        return productJpaRepository.existsById(id);
    }

    @Override
    public List<Product> findAllWithSortingAndPaging(ProductSortOption sort, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return switch (sort) {
            case LATEST -> productJpaRepository.findAllByOrderByCreatedAtDesc(pageable).getContent();
            case PRICE_ASC -> productJpaRepository.findAllByOrderByPriceValueAsc(pageable).getContent();
            case LIKES_DESC -> productJpaRepository.findAllOrderByLikeCountDesc(pageable).getContent();
        };
    }

    @Override
    public long countAll() {
        return productJpaRepository.count();
    }
}
