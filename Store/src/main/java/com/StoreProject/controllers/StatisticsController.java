package com.StoreProject.controllers;

import com.StoreProject.services.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService service;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = service.getDashboardData();
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/vendas")
    public ResponseEntity<Map<String, Object>> getSalesStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        Map<String, Object> stats = service.getSalesStatistics(inicio, fim);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/produtos/top")
    public ResponseEntity<Map<String, Object>> getTopProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        Map<String, Object> topProducts = service.getTopProducts(limit, inicio, fim);
        return ResponseEntity.ok(topProducts);
    }

    @GetMapping("/vendedores")
    public ResponseEntity<Map<String, Object>> getSellerStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        Map<String, Object> stats = service.getSellerStatistics(inicio, fim);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/entregas")
    public ResponseEntity<Map<String, Object>> getDeliveryStatistics() {
        Map<String, Object> stats = service.getDeliveryStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/receita")
    public ResponseEntity<Map<String, Object>> getRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {

        Map<String, Object> revenue = service.getRevenueData(inicio, fim);
        return ResponseEntity.ok(revenue);
    }
}