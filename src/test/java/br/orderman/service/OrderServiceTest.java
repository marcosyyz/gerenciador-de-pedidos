package br.orderman.service;

import br.orderman.dto.OrderItemDto;
import br.orderman.dto.ReceivedOrderDto;
import br.orderman.dto.CalculatedOrderDto;
import br.orderman.entity.Order;
import br.orderman.enums.OrderStatus;
import br.orderman.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test // testa process order e resultado
    void testProcessOrder_Success() {
        ReceivedOrderDto request = createOrderRequest();
        
        when(orderRepository.existsByExternalOrderId(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        CalculatedOrderDto response = orderService.processOrder(request);

        assertNotNull(response);
        assertEquals("EXT001", response.getExternalOrderId());
        assertEquals("ORD001", response.getOrderNumber());
        assertEquals(OrderStatus.CALCULATED, response.getStatus());
        assertEquals(new BigDecimal("150.00"), response.getTotalValue());
        assertEquals(2, response.getItems().size());

        verify(orderRepository).existsByExternalOrderId("EXT001");
        verify(orderRepository).save(any(Order.class));
    }

    @Test  //  testa restricao de duplicados
    void testProcessOrder_DuplicateOrder() {
        ReceivedOrderDto request = createOrderRequest();
        
        when(orderRepository.existsByExternalOrderId(anyString())).thenReturn(true);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> orderService.processOrder(request)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(orderRepository).existsByExternalOrderId("EXT001");
        verify(orderRepository, never()).save(any(Order.class));
    }
   
   
    
    @Test // testa  busca de pedidos ja calculos 
    void testGetProcessedOrders() {
        List<Order> orders = Arrays.asList(createOrder(), createOrder());
        
        when(orderRepository.findByStatusWithItems(OrderStatus.CALCULATED)).thenReturn(orders);

        List<CalculatedOrderDto> responses = orderService.getProcessedOrders();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(orderRepository).findByStatusWithItems(OrderStatus.CALCULATED);
    }

    // testa se o pedido foi marcado como  SENT apos enviado
    @Test
    void testMarkOrderAsSent() {
        Order order = createOrder();
        
        when(orderRepository.findByExternalOrderId("EXT001")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.markOrderAsSent("EXT001");

        assertEquals(OrderStatus.SENT, order.getStatus());
        verify(orderRepository).save(order);
    }

    ///  cria request para teste
    private ReceivedOrderDto createOrderRequest() {
        OrderItemDto item1 = new OrderItemDto("Product A", 2, new BigDecimal("50.00"));
        OrderItemDto item2 = new OrderItemDto("Product B", 1, new BigDecimal("50.00"));
        
        return new ReceivedOrderDto("EXT001", "ORD001", Arrays.asList(item1, item2));
    }

    ///  cria order para teste
    private Order createOrder() {
        Order order = new Order("EXT001", "ORD001");
        order.setId(1L);
        order.setStatus(OrderStatus.CALCULATED);
        order.setTotalValue(new BigDecimal("150.00"));
        return order;
    }
}