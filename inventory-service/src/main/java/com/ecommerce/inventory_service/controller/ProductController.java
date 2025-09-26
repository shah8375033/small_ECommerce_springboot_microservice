package com.ecommerce.inventory_service.controller;

import com.ecommerce.inventory_service.dto.OrderRequestDto;
import com.ecommerce.inventory_service.dto.ProductDto;
import com.ecommerce.inventory_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final DiscoveryClient  discoveryClient;
    private final RestClient restClient;

    //calling dummy api from order service
    @GetMapping("/fetchOrders")
    public String fetchFromOrderService() {
        ServiceInstance orderService=discoveryClient.getInstances("order-service").getFirst();

        return restClient.get()
                .uri(orderService.getUri()+"/orders/core/helloOrders")
                .retrieve()
                .body(String.class);

    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllInventory(){
        return ResponseEntity.ok(productService.getAllInventory());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getInventoryById(@PathVariable Long id){
        return ResponseEntity.ok(productService.getInventoryById(id));
    }

    @PutMapping("/reduce-stocks")
    public ResponseEntity<Double> reduceStocks(@RequestBody OrderRequestDto  orderRequestDto){
        Double totalPrice=productService.reduceStocks(orderRequestDto);
        return ResponseEntity.ok(totalPrice);
    }
}
