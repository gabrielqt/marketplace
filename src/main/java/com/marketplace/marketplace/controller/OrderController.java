package com.marketplace.marketplace.controller;

import com.marketplace.marketplace.dto.request.CreateOrderRequest;
import com.marketplace.marketplace.dto.response.OrderResponse;
import com.marketplace.marketplace.entity.User;
import com.marketplace.marketplace.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /orders — só BUYER
    @PostMapping
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<OrderResponse> create(@RequestBody @Valid CreateOrderRequest request,
                                                Authentication authentication) {
        User buyer = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.create(request, buyer));
    }

    // GET /orders/{id} — buyer vê só os próprios pedidos
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id,
                                                  Authentication authentication) {
        User buyer = (User) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.findById(id, buyer));
    }

    // PUT /orders/{id}/pay
    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<OrderResponse> pay(@PathVariable Long id,
                                             Authentication authentication) {
        User buyer = (User) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.pay(id, buyer));
    }

    // PUT /orders/{id}/cancel
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id,
                                                Authentication authentication) {
        User buyer = (User) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.cancel(id, buyer));
    }
}