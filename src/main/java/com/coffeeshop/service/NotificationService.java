package com.coffeeshop.service;

import com.coffeeshop.entity.Notification;
import com.coffeeshop.entity.User;
import com.coffeeshop.entity.Order;
import com.coffeeshop.entity.Reservation;
import com.coffeeshop.enums.NotificationType;

import java.util.List;
import java.util.Optional;

public interface NotificationService {
    // Lấy tất cả thông báo của user, mới nhất trước
    List<Notification> getAllByUser(User user);

    // Lấy thông báo theo nhóm/type
    List<Notification> getByUserAndType(User user, NotificationType type);

    // Lấy thông báo chưa đọc của user
    List<Notification> getUnreadByUser(User user);

    // Đếm số thông báo chưa đọc của user
    long countUnreadByUser(User user);

    // Đếm số chưa đọc theo nhóm/type
    long countUnreadByUserAndType(User user, NotificationType type);

    // Đánh dấu đã đọc 1 thông báo
    void markAsRead(Integer notificationId);

    // Đánh dấu đã đọc tất cả thông báo của user
    void markAllAsRead(User user);

    // Đánh dấu đã đọc tất cả theo nhóm/type
    void markAllAsReadByType(User user, NotificationType type);

    // Xóa 1 thông báo
    void delete(Integer notificationId);

    // Xóa tất cả thông báo theo nhóm/type
    void deleteAllByUserAndType(User user, NotificationType type);

    // Tạo mới thông báo (kiểu cũ: truyền object)
    Notification createNotification(Notification notification);

    // Tạo mới notification từ các tham số rời (sử dụng entities thay vì IDs)
    Notification createNotification(NotificationType type, User user, String content, Order order, Reservation reservation);

    // Lấy thông báo theo id
    Optional<Notification> getById(Integer notificationId);
}