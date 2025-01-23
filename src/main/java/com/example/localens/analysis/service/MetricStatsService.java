package com.example.localens.analysis.service;

import com.example.localens.analysis.domain.MetricStatistics;
import com.example.localens.analysis.repository.MetricStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricStatsService {
    private final MetricStatisticsRepository metricStatisticsRepository;
    private static final double MIN_RANGE = 0.0001;
    private static final double LOG_BASE = 10;

    @Cacheable("metricStats")
    public double normalizeValue(String field, double value) {
        MetricStatistics stats = metricStatisticsRepository
                .findByPlaceAndField("GLOBAL", field)
                .orElse(null);

        if (stats == null || !isValidRange(stats.getMinValue(), stats.getMaxValue())) {
            return logScaleNormalize(value);
        }

        return minMaxNormalize(value, stats.getMinValue(), stats.getMaxValue());
    }

    private boolean isValidRange(double min, double max) {
        return Math.abs(max - min) >= MIN_RANGE;
    }

    private double minMaxNormalize(double value, double min, double max) {
        double normalized = ((value - min) / (max - min)) * 100;
        return Math.min(Math.max(normalized, 0), 100);
    }

    private double logScaleNormalize(double value) {
        if (value < MIN_RANGE) return 0;
        if (value > 1_000_000) return 100;

        return (Math.log10(value + 1) / Math.log10(1_000_000)) * 100;
    }
}
