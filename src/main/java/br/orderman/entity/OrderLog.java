package br.orderman.entity;

import br.orderman.enums.LogType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_logs")
public class OrderLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "message", length = 500)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "log_type", length = 20)
    private LogType logType = LogType.INFO;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public OrderLog() {}
    
    public OrderLog(String message, LogType logType) {
        this.message = message;
        this.logType = logType;
        this.createdAt = LocalDateTime.now();
    }
    
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LogType getLogType() { return logType; }
    public void setLogType(LogType logType) { this.logType = logType; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}