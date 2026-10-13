package com.webshop.service;

import com.webshop.dto.CreateOrderRequest;
import com.webshop.dto.OrderItemRequest;
import com.webshop.dto.OrderItemResponse;
import com.webshop.dto.OrderResponse;
import com.webshop.entity.Order;
import com.webshop.entity.OrderItem;
import com.webshop.entity.Product;
import com.webshop.entity.User;
import com.webshop.exceptions.OutOfStockException;
import com.webshop.exceptions.ProductNotFoundException;
import com.webshop.repository.OrderRepository;
import com.webshop.repository.ProductRepository;
import com.webshop.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public OrderResponse placeOrder(CreateOrderRequest request, User user) {
        // Közvetlenül használjuk az átadott felhasználót, nincs szükség extra lekérdezésre
        // 2. Rendelés inicializálása a felhasználóval összekötve
        Order order = new Order(BigDecimal.ZERO, LocalDateTime.now(), user);
        BigDecimal calculatedTotal = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            // 1. Termék zárolása sorszinten (SELECT ... FOR UPDATE)
            Product product =
                    productRepository.findByIdWithLock(itemRequest.productId()).orElseThrow(() -> new ProductNotFoundException(itemRequest.productId()));

            // 2. Készlet ellenőrzése
            if (product.getStockQuantity() < itemRequest.quantity()) {
                throw new OutOfStockException(product.getName(), product.getStockQuantity(), itemRequest.quantity());
            }

            // 3. Készlet levonása (dirty checking miatt nem kell manuális save)
            product.setStockQuantity(product.getStockQuantity() - itemRequest.quantity());

            // 4. Részösszeg és tétel hozzáadása
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            calculatedTotal = calculatedTotal.add(lineTotal);

            OrderItem orderItem = new OrderItem(order, product, itemRequest.quantity(), product.getPrice());
            order.addItem(orderItem);
        }

        order.setTotalPrice(calculatedTotal);
        Order savedOrder = orderRepository.save(order);

        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::mapToResponse).toList();
    }
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForUser(String userEmail) {
        return orderRepository.findAllByUserEmail(userEmail).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrdersForAdmin() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }
    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses =
                order.getItems().stream().map(item -> new OrderItemResponse(item.getProduct().getId(),
                        item.getProduct().getName(), item.getQuantity(), item.getPrice(),
                        item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))).toList();

        return new OrderResponse(order.getId(), order.getUser().getEmail(), order.getTotalPrice(),
                order.getCreatedAt(), itemResponses);
    }
}