package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BrandRepositoryImpl implements BrandRepository {

    private final BrandJpaRepository brandJpaRepository;

    @Override
    public boolean existsById(Long brandId) {
        return brandJpaRepository.existsById(brandId);
    }

    @Override
    public Brand findById(Long brandId) {
        return brandJpaRepository.findById(brandId).orElse(null);
    }

    @Override
    public Brand save(Brand brand) {
        return brandJpaRepository.save(brand);
    }
}
