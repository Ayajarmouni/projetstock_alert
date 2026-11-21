package com.stockalert.notification.repository;

import com.stockalert.notification.entite.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    boolean existsByEventId(String eventId);

    List<NotificationLog> findByPurchaseOrderIdOrderByIdAsc(Long purchaseOrderId);
}
