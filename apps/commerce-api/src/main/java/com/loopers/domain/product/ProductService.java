package com.loopers.domain.product;

import com.loopers.application.product.ProductSortOption;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Product create(
        String name,
        Money price,
        Quantity stockQuantity,
        Long brandId
    ) {
        Product product = Product.create(name, price, stockQuantity, brandId);
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product get(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND,
                "상품이 존재하지 않습니다."
            ));
    }

    @Transactional(readOnly = true)
    public List<Product> getAllWithSortingAndPaging(
        ProductSortOption sort,
        int page,
        int size
    ) {
        return productRepository.findAllWithSortingAndPaging(sort, page, size);
    }

    @Transactional
    public void decreaseStock(Long productId, Quantity quantity) {
        Product product = productRepository.findByIdWithLock(productId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품이 존재하지 않습니다."));

        product.decreaseStock(quantity);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public long count() {
        return productRepository.countAll();
    }
}
