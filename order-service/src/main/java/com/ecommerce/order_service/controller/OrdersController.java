package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.dto.OrderRequestDto;
import com.ecommerce.order_service.service.OrdersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/core")
@RequiredArgsConstructor
@Slf4j
public class OrdersController {
    private final OrdersService ordersService;

    //Dummy calling
    @GetMapping("/helloOrders")
    public String helloOrders(){
        return "Hello Orders from Order Service";
    }

    @PostMapping("/create-order")
    public ResponseEntity<OrderRequestDto> createOrder(@RequestBody OrderRequestDto orderRequestDto){
        OrderRequestDto orders=ordersService.createOrder(orderRequestDto);
        return ResponseEntity.ok(orders);
    }


    @GetMapping
    public ResponseEntity<List<OrderRequestDto>> getAllOrders() {
        log.info("Fetching all orders");
        List<OrderRequestDto> orders = ordersService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderRequestDto> getOrderById(@PathVariable Long id) {
        log.info("Fetching order with id {}", id);
        OrderRequestDto order = ordersService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
}
