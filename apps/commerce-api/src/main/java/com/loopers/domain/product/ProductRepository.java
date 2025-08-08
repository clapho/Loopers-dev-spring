package com.loopers.domain.product;

import com.loopers.application.product.ProductSortOption;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(Long id);

    Optional<Product> findByIdWithLock(Long id);

    boolean existsById(Long id);

    List<Product> findAllWithSortingAndPaging(ProductSortOption sort, int page, int size);

    long countAll();
}
