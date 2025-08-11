package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    public Brand create(String name, String description) {
        Brand brand = Brand.create(name, description);
        return brandRepository.save(brand);
    }

    public Brand get(Long id) {
        Brand brand = brandRepository.findById(id);

        if (brand == null) {
            throw new CoreException(
                ErrorType.NOT_FOUND,
                "브랜드가 존재하지 않습니다."
            );
        }

        return brand;
    }
}
