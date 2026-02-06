package com.noisevisionsoftware.vitema.repository.jpa;

import com.noisevisionsoftware.vitema.model.product.ProductType;
import com.noisevisionsoftware.vitema.model.product.jpa.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findByNameContainingIgnoreCase(String name);

    List<ProductEntity> findByNameContainingIgnoreCaseAndType(String name, ProductType type);

    List<ProductEntity> findByNameContainingIgnoreCaseAndCategory(String name, String category);

    @Query("SELECT p FROM ProductEntity p WHERE " +
            "lower(p.name) LIKE lower(concat('%', :name, '%')) AND " +
            "(p.type = 'GLOBAL' OR (p.type = 'CUSTOM' AND p.authorId = :trainerId))")
    List<ProductEntity> searchProductsForTrainer(
            @Param("name") String name,
            @Param("trainerId") String trainerId
    );
}
