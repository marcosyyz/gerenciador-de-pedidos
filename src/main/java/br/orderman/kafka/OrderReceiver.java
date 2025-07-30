package br.orderman.kafka;

import br.orderman.dto.ReceivedOrderDto;
import br.orderman.dto.CalculatedOrderDto;
import br.orderman.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderReceiver {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderReceiver.class);
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderSender orderSender;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @KafkaListener(topics = "orders-received", groupId = "order-service-group")
    public void receiveOrder(String orderJson) {
        try {
            logger.info("Received order from Kafka: {}", orderJson);
            
            ReceivedOrderDto orderRequest = objectMapper.readValue(orderJson, ReceivedOrderDto.class);
            CalculatedOrderDto processedOrder = orderService.processOrder(orderRequest);
            
            orderSender.sendProcessedOrder(processedOrder);
            
            logger.info("Order processed and sent: {}", processedOrder.getExternalOrderId());
            
        } catch (Exception e) {
            logger.error("Error processing order from Kafka: {}", e.getMessage(), e);
        }
    }
}