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

    public double[] getMinMax(String place, String field) {
        log.info("Searching for MetricStatistics with place={} and field={}", place, field);
        Optional<MetricStatistics> opt = metricStatisticsRepository.findByPlaceAndField(place, field);
        if (opt.isPresent()) {
            MetricStatistics stats = opt.get();
            return new double[]{stats.getMinValue(), stats.getMaxValue()};
        }
        return new double[]{0.0, 1.0};
    }

    public double normalizeValue(String place, String field, double rawValue) {
        double[] minMax = getMinMax(place, field);
        double minVal = minMax[0];
        double maxVal = minMax[1];
        if (Double.compare(minVal, maxVal) == 0) {
            return 0.1;
        }
        double ratio = (rawValue - minVal) / (maxVal - minVal);
        double scaled = ratio * (1 - 0.1) + 0.f;
        if (scaled < 0.1) scaled = 0.1;
        if (scaled > 1.0) scaled = 1.0;
        return scaled;
    }
}
