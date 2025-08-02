package com.loopers.infrastructure.product;

import com.loopers.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    @Query("""
        SELECT p FROM Product p 
        LEFT JOIN Like l ON p.id = l.productId 
        GROUP BY p.id, p.name, p.price.value, p.stockQuantity.value, p.brandId, p.createdAt
        ORDER BY COUNT(l.id) DESC
        """)
    Page<Product> findAllOrderByLikeCountDesc(Pageable pageable);

    @Query("SELECT p FROM Product p ORDER BY p.price.value ASC")
    Page<Product> findAllByOrderByPriceValueAsc(Pageable pageable);

    Page<Product> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
