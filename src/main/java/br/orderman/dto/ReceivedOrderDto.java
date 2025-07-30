package br.orderman.dto;

import java.util.List;

public class ReceivedOrderDto {
    
    private String externalOrderId;
    private String orderNumber;
    private List<OrderItemDto> items;
    
    public ReceivedOrderDto() {}
    
    public ReceivedOrderDto(String externalOrderId, String orderNumber, List<OrderItemDto> items) {
        this.externalOrderId = externalOrderId;
        this.orderNumber = orderNumber;
        this.items = items;
    }
    
    // Getters and Setters
    public String getExternalOrderId() { return externalOrderId; }
    public void setExternalOrderId(String externalOrderId) { this.externalOrderId = externalOrderId; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }
}