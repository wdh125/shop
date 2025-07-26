package com.coffeeshop.service;

import com.coffeeshop.dto.admin.response.statistics.OverviewStatisticsResponseDTO;
import com.coffeeshop.dto.admin.response.statistics.RevenueStatisticsResponseDTO;
import com.coffeeshop.dto.admin.response.statistics.OrderStatisticsResponseDTO;
import com.coffeeshop.dto.admin.response.statistics.TopCustomersResponseDTO;
import com.coffeeshop.dto.admin.response.statistics.TopProductsResponseDTO;
import java.time.LocalDate;

public interface StatisticsService {
    // Khai báo các method thống kê ở đây
    OverviewStatisticsResponseDTO getOverviewStatistics();
    RevenueStatisticsResponseDTO getRevenueStatistics(LocalDate from, LocalDate to);
    OrderStatisticsResponseDTO getOrderStatistics(java.time.LocalDate from, java.time.LocalDate to);
    TopCustomersResponseDTO getTopCustomers(int limit);
    TopProductsResponseDTO getTopProducts(int limit);
} 