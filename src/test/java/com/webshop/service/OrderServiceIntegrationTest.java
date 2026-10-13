package com.webshop.service;

import com.webshop.dto.CreateOrderRequest;
import com.webshop.dto.OrderItemRequest;
import com.webshop.dto.Role;
import com.webshop.entity.Product;

import com.webshop.entity.User;

import com.webshop.exceptions.OutOfStockException;
import com.webshop.repository.OrderRepository;
import com.webshop.repository.ProductRepository;
import com.webshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Törlés szigorú sorrendben a Foreign Key-k miatt!
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Lementünk egy tesztfelhasználót az adatbázisba
        testUser = userRepository.save(
                new User("integration@webshop.com", "secretHash", Role.ROLE_USER)
        );
    }

    @Test
    @DisplayName("Rollback teszt: ha készlethiány van, a megelőző tétel készlete sem változik az adatbázisban")
    void placeOrder_RollbackOnOutOfStock() {
        // GIVEN
        Product keyboard = productRepository.save(
                new Product("Keychron K2", new BigDecimal("35000"), 10)
        );
        Product mouse = productRepository.save(
                new Product("Logitech MX Master", new BigDecimal("40000"), 1)
        );

        CreateOrderRequest request = new CreateOrderRequest(List.of(
                new OrderItemRequest(keyboard.getId(), 2),
                new OrderItemRequest(mouse.getId(), 5) // Készlethiány!
        ));

        // WHEN & THEN
        assertThatThrownBy(() -> orderService.placeOrder(request, testUser))
                .isInstanceOf(OutOfStockException.class);

        // Nem mentődött el rendelés
        assertThat(orderRepository.count()).isEqualTo(0);

        // A billentyűzet készlete visszagördült, maradt 10!
        Product keyboardAfterRollback = productRepository.findById(keyboard.getId()).orElseThrow();
        assertThat(keyboardAfterRollback.getStockQuantity())
                .as("A tranzakció visszagörgetése miatt a billentyűzet készletének 10-nek kell maradnia!")
                .isEqualTo(10);
    }

    @Test
    @DisplayName("Sikeres rendelés: lefut az adatbázis mentés és a felhasználóhoz kapcsolódik a rendelés")
    void placeOrder_Success_PersistsInDatabase() {
        // GIVEN
        Product monitor = productRepository.save(
                new Product("LG UltraFine", new BigDecimal("120000"), 5)
        );

        CreateOrderRequest request = new CreateOrderRequest(List.of(
                new OrderItemRequest(monitor.getId(), 2)
        ));

        // WHEN
        var response = orderService.placeOrder(request, testUser);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.userEmail()).isEqualTo(testUser.getEmail());
        assertThat(orderRepository.count()).isEqualTo(1);

        // Készlet lecsökkent az adatbázisban 5-ről 3-ra
        Product updatedMonitor = productRepository.findById(monitor.getId()).orElseThrow();
        assertThat(updatedMonitor.getStockQuantity()).isEqualTo(3);

        // A felhasználó szerinti lekérdezés is visszaadja a rendelést
        var userOrders = orderService.getOrdersForUser(testUser.getEmail());
        assertThat(userOrders).hasSize(1);
    }
}