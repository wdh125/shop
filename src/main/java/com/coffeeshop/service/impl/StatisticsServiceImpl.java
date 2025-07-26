package com.coffeeshop.service.impl;

import com.coffeeshop.service.StatisticsService;
import com.coffeeshop.dto.admin.response.statistics.OverviewStatisticsResponseDTO;
import com.coffeeshop.dto.admin.response.statistics.RevenueStatisticsResponseDTO;
import com.coffeeshop.dto.admin.response.statistics.OrderStatisticsResponseDTO;
import com.coffeeshop.dto.admin.response.statistics.TopCustomersResponseDTO;
import com.coffeeshop.dto.admin.response.statistics.TopProductsResponseDTO;
import com.coffeeshop.repository.OrderRepository;
import com.coffeeshop.repository.UserRepository;
import com.coffeeshop.repository.ProductRepository;
import com.coffeeshop.repository.CategoryRepository;
import com.coffeeshop.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    // Logic xử lý thống kê sẽ được thêm sau

    @Override
    public OverviewStatisticsResponseDTO getOverviewStatistics() {
        OverviewStatisticsResponseDTO dto = new OverviewStatisticsResponseDTO();
        // Tổng doanh thu: sum totalAmount các order có paymentStatus = PAID
        Double totalRevenue = orderRepository.findAll().stream()
            .filter(o -> o.getPaymentStatus() == com.coffeeshop.enums.PaymentStatus.PAID)
            .mapToDouble(o -> o.getTotalAmount().doubleValue())
            .sum();
        dto.setTotalRevenue(totalRevenue);
        dto.setTotalOrders((int) orderRepository.count());
        dto.setTotalCustomers((int) userRepository.count());
        dto.setTotalProducts((int) productRepository.count());
        dto.setTotalCategories((int) categoryRepository.count());
        return dto;
    }

    @Override
    public RevenueStatisticsResponseDTO getRevenueStatistics(LocalDate from, LocalDate to) {
        RevenueStatisticsResponseDTO dto = new RevenueStatisticsResponseDTO();
        dto.setFromDate(from);
        dto.setToDate(to);
        Double totalRevenue = orderRepository.findAll().stream()
            .filter(o -> o.getPaymentStatus() == com.coffeeshop.enums.PaymentStatus.PAID)
            .filter(o -> !o.getCreatedAt().toLocalDate().isBefore(from) && !o.getCreatedAt().toLocalDate().isAfter(to))
            .mapToDouble(o -> o.getTotalAmount().doubleValue())
            .sum();
        dto.setTotalRevenue(totalRevenue);
        return dto;
    }

    @Override
    public OrderStatisticsResponseDTO getOrderStatistics(java.time.LocalDate from, java.time.LocalDate to) {
        OrderStatisticsResponseDTO dto = new OrderStatisticsResponseDTO();
        dto.setFromDate(from);
        dto.setToDate(to);
        long count = orderRepository.findAll().stream()
            .filter(o -> !o.getCreatedAt().toLocalDate().isBefore(from) && !o.getCreatedAt().toLocalDate().isAfter(to))
            .count();
        dto.setTotalOrders((int) count);
        return dto;
    }

    @Override
    public TopCustomersResponseDTO getTopCustomers(int limit) {
        var allOrders = orderRepository.findAll().stream()
            .filter(o -> o.getPaymentStatus() == com.coffeeshop.enums.PaymentStatus.PAID)
            .collect(Collectors.toList());
        var customerStats = allOrders.stream()
            .collect(Collectors.groupingBy(
                o -> o.getCustomer(),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    orders -> {
                        double totalSpent = orders.stream().mapToDouble(or -> or.getTotalAmount().doubleValue()).sum();
                        int totalOrders = orders.size();
                        TopCustomersResponseDTO.TopCustomerInfo info = new TopCustomersResponseDTO.TopCustomerInfo();
                        info.setId(orders.get(0).getCustomer().getId());
                        info.setName(orders.get(0).getCustomer().getFullName());
                        info.setEmail(orders.get(0).getCustomer().getEmail());
                        info.setTotalSpent(totalSpent);
                        info.setTotalOrders(totalOrders);
                        return info;
                    }
                )
            ))
            .values().stream()
            .sorted((a, b) -> Double.compare(b.getTotalSpent(), a.getTotalSpent()))
            .limit(limit)
            .collect(Collectors.toList());
        TopCustomersResponseDTO dto = new TopCustomersResponseDTO();
        dto.setTopCustomers(customerStats);
        return dto;
    }

    @Override
    public TopProductsResponseDTO getTopProducts(int limit) {
        var allItems = orderItemRepository.findAll();
        var productStats = allItems.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                i -> i.getProduct(),
                java.util.stream.Collectors.collectingAndThen(
                    java.util.stream.Collectors.toList(),
                    items -> {
                        int totalSold = items.stream().mapToInt(it -> it.getQuantity()).sum();
                        double totalRevenue = items.stream().mapToDouble(it -> it.getTotalPrice().doubleValue()).sum();
                        TopProductsResponseDTO.TopProductInfo info = new TopProductsResponseDTO.TopProductInfo();
                        info.setId(items.get(0).getProduct().getId());
                        info.setName(items.get(0).getProduct().getName());
                        info.setTotalSold(totalSold);
                        info.setTotalRevenue(totalRevenue);
                        return info;
                    }
                )
            ))
            .values().stream()
            .sorted((a, b) -> Integer.compare(b.getTotalSold(), a.getTotalSold()))
            .limit(limit)
            .collect(java.util.stream.Collectors.toList());
        TopProductsResponseDTO dto = new TopProductsResponseDTO();
        dto.setTopProducts(productStats);
        return dto;
    }
} 