package com.coffeeshop.dto.notification.response;

import java.util.List;

public class NotificationListResponseDTO {
    
    private List<NotificationResponseDTO> notifications;
    private long unreadCount;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;

    public List<NotificationResponseDTO> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationResponseDTO> notifications) {
        this.notifications = notifications;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}