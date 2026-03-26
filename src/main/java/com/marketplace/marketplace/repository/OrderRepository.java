package com.marketplace.marketplace.repository;

import com.marketplace.marketplace.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByBuyerId(Long buyerId);
}
