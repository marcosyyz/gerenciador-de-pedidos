package br.orderman.kafka;

import br.orderman.dto.CalculatedOrderDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderSender {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderSender.class);
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public void sendProcessedOrder(CalculatedOrderDto order) {
        try {
            String orderJson = objectMapper.writeValueAsString(order);
            kafkaTemplate.send("orders-processed", order.getExternalOrderId(), orderJson);
            
            logger.info("Sent processed order to Kafka: {}", order.getExternalOrderId());
            
        } catch (Exception e) {
            logger.error("Error sending order to Kafka: {}", e.getMessage(), e);
        }
    }
}