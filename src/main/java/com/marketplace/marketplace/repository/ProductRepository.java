package com.marketplace.marketplace.repository;

import com.marketplace.marketplace.entity.Product;
import com.marketplace.marketplace.entity.enums.ProductStatus;
import org.apache.el.stream.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByStatus(ProductStatus status);
    List<Product> findBySellerId(Long sellerId);
}
