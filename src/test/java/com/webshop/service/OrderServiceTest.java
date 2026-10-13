package com.webshop.service;

import com.webshop.dto.CreateOrderRequest;
import com.webshop.dto.OrderItemRequest;
import com.webshop.dto.OrderResponse;
import com.webshop.dto.Role;
import com.webshop.entity.Order;
import com.webshop.entity.Product;

import com.webshop.entity.User;
import com.webshop.exceptions.OutOfStockException;
import com.webshop.exceptions.ProductNotFoundException;
import com.webshop.repository.OrderRepository;
import com.webshop.repository.ProductRepository;
import com.webshop.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {
    @InjectMocks
    private OrderService orderService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    private final String TEST_USER_EMAIL = "buyer@webshop.com";
    @Test
    @DisplayName("Sikeres rendelés: a készlet lecsökken, a végösszeg pontos, a rendelés mentésre kerül")
    void placeOrder_Successful() {
        // GIVEN (Előkészítés)
        User user = new User(TEST_USER_EMAIL,"hashedpass", Role.ROLE_USER);
        Product macbook = new Product("MacBook Pro", new BigDecimal("800000"), 5);
        // Beállítjuk a mockolt termék ID-ját!
        setId(macbook, 1L);

        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(macbook));

        // Amikor az orderRepository.save-et hívjuk, adja vissza a kapott Order-t egy generált ID-val
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order orderToSave = invocation.getArgument(0);
            setId(orderToSave, 100L);
            return orderToSave;
        });

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(1L, 2)) // 2 db MacBook
        );

        // WHEN (Végrehajtás)
        OrderResponse response = orderService.placeOrder(request,user);

        // THEN (Ellenőrzések)
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.totalPrice()).isEqualByComparingTo("1600000"); // 2 * 800 000
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).quantity()).isEqualTo(2);

        // Kritikus üzleti szabály: a termék memóriabeli készlete 5-ről 3-ra kellett csökkenjen!
        assertThat(macbook.getStockQuantity()).isEqualTo(3);

        // Ellenőrizzük, hogy lefutott-e a mentés a repository-ban
        verify(productRepository, times(1)).findByIdWithLock(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Készlethiány: OutOfStockException-t dob, a készlet nem változik és a rendelés nem mentődik el")
    void placeOrder_OutOfStock_ThrowsException() {
        // GIVEN: csak 1 darab van raktáron
        User user = new User(TEST_USER_EMAIL,"hashedpass", Role.ROLE_USER);

        Product monitor = new Product("Dell Monitor", new BigDecimal("150000"), 1);
        setId(monitor, 2L);

        when(productRepository.findByIdWithLock(2L)).thenReturn(Optional.of(monitor));

        // 3 darabot szeretne rendelni a vevő
        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(2L, 3))
        );

        // WHEN & THEN: elvárjuk a kivételt
        assertThatThrownBy(() -> orderService.placeOrder(request,user))
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining("Nincs elég készlet");

        // A készletnek érintetlennek kell maradnia!
        assertThat(monitor.getStockQuantity()).isEqualTo(1);

        // A rendelést SOHA nem szabadott elmenteni
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Nem létező termék: ProductNotFoundException-t dob")
    void placeOrder_ProductNotFound_ThrowsException() {
        User user = new User(TEST_USER_EMAIL, "hashedPassword", Role.ROLE_USER);
        // GIVEN: nincs ilyen termék az adatbázisban
        when(productRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest(
                List.of(new OrderItemRequest(99L, 1))
        );

        // WHEN & THEN
        assertThatThrownBy(() -> orderService.placeOrder(request,user))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");

        verify(orderRepository, never()).save(any(Order.class));
    }

    // Segédmetódus a privát ID mező beállításához (mivel nincs setId és az adatbázis generálná)
    private void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


