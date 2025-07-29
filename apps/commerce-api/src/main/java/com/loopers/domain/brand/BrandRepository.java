package com.loopers.domain.brand;

public interface BrandRepository {

    boolean existsById(Long id);

    Brand findById(Long id);

    Brand save(Brand brand);
}
