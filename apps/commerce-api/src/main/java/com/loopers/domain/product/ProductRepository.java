package com.loopers.domain.product;

import com.loopers.application.product.ProductSortOption;
import java.util.List;

public interface ProductRepository {

    Product save(Product product);

    Product findById(Long id);

    boolean existsById(Long id);

    List<Product> findAllWithSortingAndPaging(ProductSortOption sort, int page, int size);

    long countAll();
}
