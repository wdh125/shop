package com.coffeeshop.repository;

import com.coffeeshop.entity.Notification;
import com.coffeeshop.entity.User;
import com.coffeeshop.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    // Lấy tất cả thông báo của user, mới nhất trước
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    // Lấy thông báo theo nhóm/type
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, NotificationType type);

    // Lấy thông báo chưa đọc của user
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    // Đếm số thông báo chưa đọc của user
    long countByUserAndIsReadFalse(User user);

    // Đếm số chưa đọc theo nhóm/type
    long countByUserAndTypeAndIsReadFalse(User user, NotificationType type);

    // Đánh dấu đã đọc tất cả thông báo của user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user")
    void markAllAsReadByUser(@Param("user") User user);

    // Đánh dấu đã đọc tất cả theo nhóm/type
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.type = :type")
    void markAllAsReadByUserAndType(@Param("user") User user, @Param("type") NotificationType type);

    // Xóa tất cả thông báo theo nhóm/type
    void deleteAllByUserAndType(User user, NotificationType type);
}