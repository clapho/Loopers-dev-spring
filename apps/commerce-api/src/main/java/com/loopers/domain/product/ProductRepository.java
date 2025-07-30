package com.loopers.domain.product;

public interface ProductRepository {

    Product save(Product product);

    Product findById(Long id);

    boolean existsById(Long id);
}
