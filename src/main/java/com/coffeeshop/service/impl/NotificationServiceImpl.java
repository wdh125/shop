package com.coffeeshop.service.impl;

import com.coffeeshop.entity.Notification;
import com.coffeeshop.entity.User;
import com.coffeeshop.entity.Order;
import com.coffeeshop.entity.Reservation;
import com.coffeeshop.enums.NotificationType;
import com.coffeeshop.repository.NotificationRepository;
import com.coffeeshop.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<Notification> getAllByUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<Notification> getByUserAndType(User user, NotificationType type) {
        return notificationRepository.findByUserAndTypeOrderByCreatedAtDesc(user, type);
    }

    @Override
    public List<Notification> getUnreadByUser(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    @Override
    public long countUnreadByUser(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Override
    public long countUnreadByUserAndType(User user, NotificationType type) {
        return notificationRepository.countByUserAndTypeAndIsReadFalse(user, type);
    }

    @Override
    @Transactional
    public void markAsRead(Integer notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadByUser(user);
    }

    @Override
    @Transactional
    public void markAllAsReadByType(User user, NotificationType type) {
        notificationRepository.markAllAsReadByUserAndType(user, type);
    }

    @Override
    @Transactional
    public void delete(Integer notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    @Transactional
    public void deleteAllByUserAndType(User user, NotificationType type) {
        notificationRepository.deleteAllByUserAndType(user, type);
    }

    @Override
    public Notification createNotification(Notification notification) {
        notification.setIsRead(false);
        return notificationRepository.save(notification);
    }

    @Override
    public Optional<Notification> getById(Integer notificationId) {
        return notificationRepository.findById(notificationId);
    }

    @Override
    public Notification createNotification(NotificationType type, User user, String content, Order order, Reservation reservation) {
        Notification notification = new Notification();
        notification.setType(type);
        notification.setUser(user);
        notification.setContent(content);
        notification.setOrder(order);
        notification.setReservation(reservation);
        notification.setIsRead(false);
        return notificationRepository.save(notification);
    }
}