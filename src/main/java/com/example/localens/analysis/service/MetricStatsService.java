package com.example.localens.analysis.service;

import com.example.localens.analysis.domain.MetricStatistics;
import com.example.localens.analysis.repository.MetricStatisticsRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricStatsService {

    private final MetricStatisticsRepository metricStatisticsRepository;

    public double[] getMinMax(String place, String field) {
        Optional<MetricStatistics> opt = metricStatisticsRepository.findByPlaceAndField(place, field);
        if (opt.isPresent()) {
            MetricStatistics stats = opt.get();
            return new double[]{stats.getMinValue(), stats.getMaxValue()};
        }
        return new double[]{0.0, 1.0};
    }
}
