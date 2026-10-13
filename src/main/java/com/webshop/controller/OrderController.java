package com.webshop.controller;

import com.webshop.dto.CreateOrderRequest;
import com.webshop.dto.OrderResponse;
import com.webshop.entity.Order;
import com.webshop.entity.User;
import com.webshop.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/orders")
public class OrderController {
    private final OrderService orderService;
    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(@Valid @RequestBody CreateOrderRequest orderRequest, @AuthenticationPrincipal User user ){// <-- Közvetlenül a saját User entitásunk!
        return orderService.placeOrder(orderRequest,user);
    }
    @GetMapping
    public List<OrderResponse> getOrders(@AuthenticationPrincipal UserDetails userDetails){
        // Ha ADMIN, látja az összes rendelést, ha sima USER, csak a sajátjait
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a ->a.getAuthority().equals("ROLE_ADMIN"));
        if(isAdmin){
            return orderService.getAllOrdersForAdmin();
        }else{
            return orderService.getOrdersForUser(userDetails.getUsername());
        }

    }
}
