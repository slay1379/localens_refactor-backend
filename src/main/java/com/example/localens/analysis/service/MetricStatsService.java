package com.example.localens.analysis.service;

import com.example.localens.analysis.domain.MetricStatistics;
import com.example.localens.analysis.repository.MetricStatisticsRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricStatsService {
    private final MetricStatisticsRepository metricStatisticsRepository;

    public double normalizeValue(String field, double value) {
        log.info("Normalizing global value for field: {}, value: {}", field, value);

        MetricStatistics stats = metricStatisticsRepository
                .findByPlaceAndField("GLOBAL", field)
                .orElse(null);

        if (stats == null) {
            log.warn("No global statistics found for {}, using raw value", field);
            return normalizeWithoutStats(value);
        }

        log.info("Found global stats - min: {}, max: {}", stats.getMinValue(), stats.getMaxValue());
        return normalize(value, stats.getMinValue(), stats.getMaxValue());
    }

    private double normalize(double value, double min, double max) {
        // 매우 작은 값들을 처리하기 위한 보정
        if (Math.abs(max - min) < 0.0001) {
            log.warn("Very small range detected (max - min < 0.0001), using alternative normalization");
            return normalizeWithoutStats(value);
        }

        // 기본 정규화 (0 ~ 100 범위로)
        double normalized = ((value - min) / (max - min)) * 100;

        // 범위를 벗어나는 경우 처리
        if (normalized < 0) normalized = 0;
        if (normalized > 100) normalized = 100;

        log.info("Normalized {} to {} (min={}, max={})", value, normalized, min, max);
        return normalized;
    }

    private double normalizeWithoutStats(double value) {
        // 매우 작은 값 처리
        if (value < 0.0001) {
            return 0; // 최소값
        }

        if (value > 1000000) {
            return 100; // 최대값
        }

        // 로그 스케일 정규화
        double normalized = (Math.log10(value * 100 + 1) / Math.log10(1000000)) * 100;

        log.info("Normalized without stats {} to {}", value, normalized);
        return normalized;
    }

}
