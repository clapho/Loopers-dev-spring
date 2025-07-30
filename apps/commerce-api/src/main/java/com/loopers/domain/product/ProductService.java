package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public Product create(
        String name,
        Money price,
        Quantity stockQuantity,
        Long brandId
    ) {
        Product product = Product.create(name, price, stockQuantity, brandId);
        return productRepository.save(product);
    }

    public Product findById(Long id) {
        Product product = productRepository.findById(id);

        if (product == null) {
            throw new CoreException(
                ErrorType.NOT_FOUND,
                "상품이 존재하지 않습니다."
            );
        }

        return product;
    }
}
