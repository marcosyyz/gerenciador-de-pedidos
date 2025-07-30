package br.orderman.service;

import br.orderman.dto.OrderItemDto;
import br.orderman.dto.ReceivedOrderDto;
import br.orderman.dto.CalculatedOrderDto;
import br.orderman.entity.*;
import br.orderman.enums.*;
import br.orderman.repository.OrderRepository;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    public CalculatedOrderDto processOrder(ReceivedOrderDto orderRequest) {
        logger.info("Processing order with external ID: {}", orderRequest.getExternalOrderId());
        
        if (orderRepository.existsByExternalOrderId(orderRequest.getExternalOrderId())) {
            logger.warn("Duplicate order detected: {}", orderRequest.getExternalOrderId());
            throw new IllegalStateException("Order with external ID " + orderRequest.getExternalOrderId() + " already exists");
        }
        
        Order order = createOrderFromRequest(orderRequest);
        order.addLog(new OrderLog("Order received from external product A", LogType.INFO));
        
        order.setStatus(OrderStatus.PROCESSING);
        order.addLog(new OrderLog("Order processing started", LogType.INFO));
        
        calculateOrderValues(order);
        
        order.setStatus(OrderStatus.CALCULATED);
        order.addLog(new OrderLog("Order calculations completed", LogType.INFO));
        
        Order savedOrder = orderRepository.save(order);
        logger.info("Order processed successfully: {}", savedOrder.getExternalOrderId());
        return convertToResponseDto(savedOrder);
    }
    
    @Transactional(readOnly = true)
    public List<CalculatedOrderDto> getProcessedOrders() {
        logger.info("Retrieving processed orders for external product B");
        
        List<Order> orders = orderRepository.findByStatusWithItems(OrderStatus.CALCULATED);
        return orders.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CalculatedOrderDto getOrderById(Long id) {
        Order order = orderRepository.findByIdWithItemsAndLogs(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
        
        return convertToResponseDto(order);
    }
    
    @Transactional(readOnly = true)
    public CalculatedOrderDto getOrderByExternalId(String externalOrderId) {
        Order order = orderRepository.findByExternalOrderId(externalOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found with external ID: " + externalOrderId));
        
        return convertToResponseDto(order);
    }
    
    public void markOrderAsSent(String externalOrderId) {
        Order order = orderRepository.findByExternalOrderId(externalOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found with external ID: " + externalOrderId));
        
        order.setStatus(OrderStatus.SENT);
        order.addLog(new OrderLog("Order sent to external product B", LogType.INFO));
        
        orderRepository.save(order);
        logger.info("Order marked as sent: {}", externalOrderId);
    }
    
    private Order createOrderFromRequest(ReceivedOrderDto orderRequest) {
        Order order = new Order(orderRequest.getExternalOrderId(), orderRequest.getOrderNumber());
        
        for (OrderItemDto itemDto : orderRequest.getItems()) {
            validateOrderItem(itemDto);
            OrderItem item = new OrderItem(
                itemDto.getProductName(),
                itemDto.getQuantity(),
                itemDto.getUnitPrice()
            );
            BigDecimal itemTotal = itemDto.getUnitPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            item.setTotalPrice(itemTotal);
            order.addItem(item);
        }
        
        return order;
    }
    
    private void validateOrderItem(OrderItemDto itemDto) {
        if (itemDto.getProductName() == null || itemDto.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (itemDto.getQuantity() == null || itemDto.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (itemDto.getUnitPrice() == null || itemDto.getUnitPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero");
        }
    }
    
    private void calculateOrderValues(Order order) {
        BigDecimal totalValue = BigDecimal.ZERO;
        
        for (OrderItem item : order.getItems()) {
            BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setTotalPrice(itemTotal);
            totalValue = totalValue.add(itemTotal);
        }
        
        order.setTotalValue(totalValue);
        logger.info("Order total calculated: {} for order {}", 
                   totalValue, order.getExternalOrderId());
    }
    
    private CalculatedOrderDto convertToResponseDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> new OrderItemDto(
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice()
                ))
                .collect(Collectors.toList());
        
        return new CalculatedOrderDto(
            order.getId(),
            order.getExternalOrderId(),
            order.getOrderNumber(),
            order.getTotalValue(),
            order.getStatus(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            itemDtos
        );
    }
}