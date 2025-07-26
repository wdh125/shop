package com.coffeeshop.controller.admin;

import com.coffeeshop.dto.admin.response.statistics.OverviewStatisticsResponseDTO;
import com.coffeeshop.dto.admin.response.statistics.RevenueStatisticsResponseDTO;
import com.coffeeshop.dto.admin.response.statistics.OrderStatisticsResponseDTO;
import com.coffeeshop.dto.admin.response.statistics.TopCustomersResponseDTO;
import com.coffeeshop.dto.admin.response.statistics.TopProductsResponseDTO;
import com.coffeeshop.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/statistics")
public class StatisticsController {
    @Autowired
    private StatisticsService statisticsService;

    // Các endpoint thống kê sẽ được thêm sau

    @GetMapping("/overview")
    public OverviewStatisticsResponseDTO getOverviewStatistics() {
        return statisticsService.getOverviewStatistics();
    }

    @GetMapping("/revenue")
    public RevenueStatisticsResponseDTO getRevenueStatistics(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return statisticsService.getRevenueStatistics(from, to);
    }

    @GetMapping("/orders")
    public OrderStatisticsResponseDTO getOrderStatistics(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return statisticsService.getOrderStatistics(from, to);
    }

    @GetMapping("/top-customers")
    public TopCustomersResponseDTO getTopCustomers(@RequestParam(value = "limit", defaultValue = "5") int limit) {
        return statisticsService.getTopCustomers(limit);
    }

    @GetMapping("/top-products")
    public TopProductsResponseDTO getTopProducts(@RequestParam(value = "limit", defaultValue = "5") int limit) {
        return statisticsService.getTopProducts(limit);
    }
} 