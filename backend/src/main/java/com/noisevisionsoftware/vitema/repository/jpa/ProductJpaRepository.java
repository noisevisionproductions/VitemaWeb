package com.noisevisionsoftware.vitema.repository.jpa;

import com.noisevisionsoftware.vitema.model.product.jpa.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findByNameContainingIgnoreCase(String name);

    List<ProductEntity> findByNameContainingIgnoreCaseAndCategory(String name, String category);
}
