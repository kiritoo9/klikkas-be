package com.klikkas.controller;

import org.springframework.web.bind.annotation.RestController;

import com.klikkas.dto.dashboards.CurrentActivity;
import com.klikkas.dto.dashboards.LatestWeekGraphResponse;
import com.klikkas.dto.dashboards.SummarizeResponse;
import com.klikkas.service.DashboardService;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboards/summarize")
    public SummarizeResponse getSummarize() {
        return dashboardService.getSummarize();
    }

    @GetMapping("/dashboards/latest_week_graph")
    public List<LatestWeekGraphResponse> getLatestWeekGraph() {
        return dashboardService.getLastestWeekGraph();
    }

    @GetMapping("/dashboards/current_activity")
    public List<CurrentActivity> currentActivity() {
        return dashboardService.currentActivity();
    }

}
