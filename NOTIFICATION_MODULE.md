# Notification Module Documentation

## Overview
The Notification Module provides a comprehensive notification system for the Coffee Shop application. It uses proper JPA entity relationships instead of primitive IDs, ensuring data integrity and leveraging the full power of relational database design.

## Key Features

### 1. Entity Relationships (No Primitive IDs)
- **Notification Entity**: Uses `@ManyToOne` relationships to reference related entities
  - `User user` - The notification recipient
  - `Order relatedOrder` - Related order (optional)
  - `Payment relatedPayment` - Related payment (optional)
  - `Reservation relatedReservation` - Related reservation (optional)

### 2. Notification Types
- `ORDER_CREATED` - When a new order is placed
- `ORDER_STATUS_CHANGED` - When order status changes
- `ORDER_CANCELLED` - When order is cancelled
- `ORDER_COMPLETED` - When order is completed
- `PAYMENT_RECEIVED` - When payment is successful
- `PAYMENT_FAILED` - When payment fails
- `RESERVATION_CONFIRMED` - When reservation is confirmed/created
- `RESERVATION_CANCELLED` - When reservation is cancelled
- `RESERVATION_REMINDER` - For reservation reminders
- `TABLE_AVAILABLE` - When table becomes available
- `SYSTEM_ANNOUNCEMENT` - System-wide announcements

### 3. Automatic Notification Creation
The module automatically creates notifications for key business events:

#### Order Events
- **Order Creation**: When a customer places a new order
- **Status Changes**: When order moves through PREPARING → SERVED → COMPLETED → CANCELLED
- **Payment Events**: Success/failure notifications

#### Reservation Events
- **New Reservations**: When a reservation is created
- **Status Updates**: When reservation is confirmed or cancelled
- **Cancellations**: When customer or staff cancels reservation

### 4. API Endpoints

```
POST /api/notifications
- Create notification (Admin/Staff only)
- Body: NotificationCreateRequestDTO

GET /api/notifications/my?page=0&size=20&onlyUnread=false
- Get current user's notifications with pagination

GET /api/notifications/user/{userId}?page=0&size=20&onlyUnread=false
- Get specific user's notifications (Admin/Staff or self only)

POST /api/notifications/{id}/read
- Mark specific notification as read

POST /api/notifications/mark-all-read
- Mark all user's notifications as read

GET /api/notifications/unread-count
- Get count of unread notifications
```

### 5. Database Schema

```sql
CREATE TABLE notifications (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    related_order_id INT NULL,
    related_payment_id INT NULL,
    related_reservation_id INT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    read_at DATETIME NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (related_order_id) REFERENCES orders(id),
    FOREIGN KEY (related_payment_id) REFERENCES payments(id),
    FOREIGN KEY (related_reservation_id) REFERENCES reservations(id)
);
```

### 6. Service Integration

The notification system is integrated into existing services:

- **OrderService**: Creates notifications for order lifecycle events
- **PaymentService**: Creates notifications for payment events
- **ReservationService**: Creates notifications for reservation events

### 7. Security
- Users can only access their own notifications
- Admin/Staff can create notifications for any user
- Admin/Staff can view any user's notifications
- Proper authentication and authorization checks

### 8. Benefits of Entity Relationships

Instead of storing primitive IDs, the system uses proper JPA relationships:

**Bad (Primitive IDs):**
```java
private Integer userId;
private Integer orderId;
```

**Good (Entity Relationships):**
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "related_order_id")
private Order relatedOrder;
```

**Advantages:**
- Type safety at compile time
- Automatic foreign key constraints
- Lazy loading support
- Better query optimization
- Referential integrity
- IDE support and code completion

### 9. Usage Examples

#### Creating Order Notification
```java
notificationService.createOrderNotification(
    user, 
    order, 
    NotificationType.ORDER_CREATED,
    "New Order Created",
    "Your order #" + order.getOrderNumber() + " has been placed successfully."
);
```

#### Getting User Notifications
```java
NotificationListResponseDTO notifications = 
    notificationService.getUserNotifications(userId, 0, 20, false);
```

### 10. Testing
Comprehensive unit tests cover all major functionality:
- Notification creation with entity relationships
- User notification retrieval with pagination
- Read/unread status management
- Integration with business services

## Future Enhancements
- Real-time notifications via WebSocket
- Email/SMS notification delivery
- Notification preferences management
- Push notifications for mobile apps