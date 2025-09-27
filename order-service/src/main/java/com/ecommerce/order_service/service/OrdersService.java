package com.ecommerce.order_service.service;

import com.ecommerce.order_service.clients.InventoryFeignClient;
import com.ecommerce.order_service.dto.OrderRequestDto;
import com.ecommerce.order_service.entity.OrderItem;
import com.ecommerce.order_service.entity.OrderStatus;
import com.ecommerce.order_service.entity.Orders;
import com.ecommerce.order_service.repository.OrdersRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrdersService {
    private final OrdersRepository ordersRepository;
    private final ModelMapper modelMapper;
    private final InventoryFeignClient  inventoryFeignClient;

    public List<OrderRequestDto> getAllOrders(){
        log.info("Fetching all orders");
        List<Orders> orders = ordersRepository.findAll();
        return orders.stream()
                .map((element) -> modelMapper.map(element, OrderRequestDto.class))
                .toList();

    }

    public OrderRequestDto getOrderById(Long id){
        log.info("Fetching order by id {}", id);
        Orders order = ordersRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Order not found"));
        return modelMapper.map(order, OrderRequestDto.class);
    }

    @Retry(name = "inventoryRetry",fallbackMethod = "createOrderFallback")
    @RateLimiter(name = "inventoryRateLimiter",fallbackMethod = "createOrderFallback")
    @CircuitBreaker(name = "inventoryCircuitBreaker",fallbackMethod ="createOrderFallback")
    public OrderRequestDto createOrder(OrderRequestDto orderRequestDto) {
        Double totalPrice=inventoryFeignClient.reduceStocks(orderRequestDto);
        Orders orders=modelMapper.map(orderRequestDto,Orders.class);
        for (OrderItem orderItem:orders.getOrderItems()){
            orderItem.setOrder(orders);
        }
        orders.setPrice(totalPrice);
        orders.setOrderStatus(OrderStatus.Confirmed);
        Orders savedOrder=ordersRepository.save(orders);
        return modelMapper.map(savedOrder, OrderRequestDto.class);
    }
    public OrderRequestDto createOrderFallback(OrderRequestDto orderRequestDto, Throwable throwable) {
        log.error("Fallback Method occured due to {}"+throwable.getMessage());
        return new OrderRequestDto();

    }
}
