package com.tiltedhat.financeflow_backend.controller;

import com.tiltedhat.financeflow_backend.dto.CategoryTrendResponse;
import com.tiltedhat.financeflow_backend.dto.MonthlyTrendResponse;
import com.tiltedhat.financeflow_backend.dto.TopSpendingMonthResponse;
import com.tiltedhat.financeflow_backend.entity.User;
import com.tiltedhat.financeflow_backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * GET /api/analytics/monthly-trends?months=6
     * Get monthly income/expense trends
     */
    @GetMapping("/monthly-trends")
    public ResponseEntity<List<MonthlyTrendResponse>> getMonthlyTrends(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "6") Integer months
    ) {
        List<MonthlyTrendResponse> trends = analyticsService.getMonthlyTrends(user, months);
        return ResponseEntity.ok(trends);
    }

    /**
     * GET /api/analytics/category-trends?months=6
     * Get category spending over time
     */
    @GetMapping("/category-trends")
    public ResponseEntity<List<CategoryTrendResponse>> getCategoryTrends(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "6") Integer months
    ) {
        List<CategoryTrendResponse> trends = analyticsService.getCategorySpendingOverTime(user, months);
        return ResponseEntity.ok(trends);
    }

    /**
     * GET /api/analytics/top-spending-months?limit=5
     * Get top spending months
     */
    @GetMapping("/top-spending-months")
    public ResponseEntity<List<TopSpendingMonthResponse>> getTopSpendingMonths(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "5") Integer limit
    ) {
        List<TopSpendingMonthResponse> topMonths = analyticsService.getTopSpendingMonths(user, limit);
        return ResponseEntity.ok(topMonths);
    }
}
