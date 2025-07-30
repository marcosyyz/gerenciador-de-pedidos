package br.orderman.repository;

import br.orderman.entity.Order;
import br.orderman.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByExternalOrderId(String externalOrderId);
    
    boolean existsByExternalOrderId(String externalOrderId);
    
    List<Order> findByStatus(OrderStatus status);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items LEFT JOIN FETCH o.logs WHERE o.id = :id")
    Optional<Order> findByIdWithItemsAndLogs(@Param("id") Long id);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.status = :status")
    List<Order> findByStatusWithItems(@Param("status") OrderStatus status);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);
}