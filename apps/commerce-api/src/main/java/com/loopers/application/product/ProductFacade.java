package com.loopers.application.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.Like;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;
    private final BrandService brandService;
    private final LikeService likeService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public ProductInfo.Detail getProductDetail(ProductCommand.GetDetail command) {
        Product product = productService.findById(command.productId());

        Brand brand = brandService.findById(product.getBrandId());

        long likeCount = likeService.countLikesByProduct(command.productId());

        return ProductInfo.Detail.from(product, brand, likeCount);
    }

    @Transactional(readOnly = true)
    public ProductInfo.PagedList getProducts(ProductCommand.GetList command) {
        List<Product> products = productService.findProductsWithSortingAndPaging(
            command.sort(),
            command.page(),
            command.size()
        );

        long totalCount = productService.countProducts();

        List<ProductInfo.Detail> productDetails = products.stream()
            .map(this::buildProductDetail)
            .toList();

        return ProductInfo.PagedList.of(productDetails, command.page(), command.size(), totalCount);
    }

    @Transactional(readOnly = true)
    public ProductInfo.PagedList getLikedProducts(ProductCommand.GetLikedProducts command) {
        validateUserExists(command.userId());

        List<Like> likes = likeService.findLikesByUserIdWithPaging(
            command.userId(),
            command.page(),
            command.size()
        );

        long totalCount = likeService.countLikesByUser(command.userId());

        List<ProductInfo.Detail> productDetails = likes.stream()
            .map(this::buildLikedProductDetail)
            .toList();

        return ProductInfo.PagedList.of(productDetails, command.page(), command.size(), totalCount);
    }

    private void validateUserExists(String userId) {
        userService.findByUserId(userId);
    }

    private ProductInfo.Detail buildProductDetail(Product product) {
        Brand brand = brandService.findById(product.getBrandId());

        long likeCount = likeService.countLikesByProduct(product.getId());

        return ProductInfo.Detail.from(product, brand, likeCount);
    }

    private ProductInfo.Detail buildLikedProductDetail(Like like) {
        Product product = productService.findById(like.getProductId());

        Brand brand = brandService.findById(product.getBrandId());

        long likeCount = likeService.countLikesByProduct(product.getId());

        return ProductInfo.Detail.from(product, brand, likeCount);
    }
}
