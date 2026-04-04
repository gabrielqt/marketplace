package com.marketplace.marketplace.service;

import com.marketplace.marketplace.dto.request.CreateOrderRequest;
import com.marketplace.marketplace.dto.request.OrderItemRequest;
import com.marketplace.marketplace.dto.response.OrderItemResponse;
import com.marketplace.marketplace.dto.response.OrderResponse;
import com.marketplace.marketplace.entity.Order;
import com.marketplace.marketplace.entity.OrderItem;
import com.marketplace.marketplace.entity.Product;
import com.marketplace.marketplace.entity.User;
import com.marketplace.marketplace.entity.enums.OrderStatus;
import com.marketplace.marketplace.entity.enums.ProductStatus;
import com.marketplace.marketplace.exception.ObjectNotFoundException;
import com.marketplace.marketplace.repository.OrderRepository;
import com.marketplace.marketplace.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse create(CreateOrderRequest request, User buyer) {

        // monta os itens e calcula o total
        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {

            // busca o produto
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ObjectNotFoundException("Product not found"));

            // comprador não pode comprar o próprio produto
            if (product.getSeller().getId().equals(buyer.getId())) {
                throw new IllegalArgumentException("You cannot buy your own product");
            }

            // verifica se produto está ativo
            if (product.getStatus() == ProductStatus.INACTIVE) {
                throw new IllegalArgumentException("Product " + product.getName() + " is unavailable");
            }

            // verifica estoque
            if (product.getStock() < itemRequest.quantity()) {
                throw new IllegalArgumentException("Insufficient stock for " + product.getName());
            }

            // reserva o estoque
            product.setStock(product.getStock() - itemRequest.quantity());
            productRepository.save(product);

            // trava o preço no momento da compra
            OrderItem item = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .price(product.getPrice()) // preço travado agora
                    .build();

            items.add(item);
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        // cria o pedido
        Order order = Order.builder()
                .buyer(buyer)
                .status(OrderStatus.PENDING)
                .total(total)
                .items(items)
                .build();

        // seta a referência do order em cada item
        items.forEach(item -> item.setOrder(order));

        return toResponse(orderRepository.save(order));
    }

    public OrderResponse findById(Long id, User buyer) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Order not found"));

        // comprador só pode ver os próprios pedidos
        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("Access denied");
        }

        return toResponse(order);
    }

    @Transactional
    public OrderResponse pay(Long id, User buyer) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Order not found"));

        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("Access denied");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only pending orders can be paid");
        }

        order.setStatus(OrderStatus.PAID);
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancel(Long id, User buyer) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Order not found"));

        if (!order.getBuyer().getId().equals(buyer.getId())) {
            throw new IllegalArgumentException("Access denied");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only pending orders can be cancelled");
        }

        // devolve o estoque
        order.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        });

        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotal(),
                order.getBuyer().getName(),
                itemResponses
        );
    }
}