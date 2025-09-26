package com.ecommerce.inventory_service.service;

import com.ecommerce.inventory_service.dto.OrderRequestDto;
import com.ecommerce.inventory_service.dto.OrderRequestItemDto;
import com.ecommerce.inventory_service.dto.ProductDto;
import com.ecommerce.inventory_service.entity.Product;
import com.ecommerce.inventory_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public List<ProductDto> getAllInventory() {
        log.info("Fetching all inventory items");
        List<Product> inventories = productRepository.findAll();
        return inventories.stream()
                .map(product -> modelMapper.map(product, ProductDto.class))
                .collect(Collectors.toList());
    }

    public ProductDto getInventoryById(Long id) {
        log.info("Fetching inventory items by id {}", id);
        Optional<Product> inventory = productRepository.findById(id);
        return inventory.map((element) -> modelMapper.map(element, ProductDto.class))
                .orElseThrow(() -> new RuntimeException("inventory not found"));
    }

    public Double reduceStocks(OrderRequestDto orderRequestDto) {
        log.info("reducing the stocks");
        Double totalPrice = 0.0;
        for (OrderRequestItemDto orderRequestItemDto : orderRequestDto.getItems()) {
            Long productId = orderRequestItemDto.getProductId();
            Integer quantity = orderRequestItemDto.getQuantity();

            Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("product not found"));
            if (product.getStock() < quantity) {
                throw new RuntimeException("product not enough stock");
            }
            product.setStock(product.getStock() - quantity);
            productRepository.save(product);
            totalPrice += product.getPrice() * quantity;
        }
        return totalPrice;
    }
}
