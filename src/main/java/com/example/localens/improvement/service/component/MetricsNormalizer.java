package com.example.localens.improvement.service.component;

import com.example.localens.improvement.constant.ImprovementConstants;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricsNormalizer {

    public Map<String, Integer> normalizeMetrics(Map<String, Object> districtData) {
        return ImprovementConstants.NORMALIZED_METRICS.stream()
                .collect(Collectors.toMap(
                        this::getMetricKey,
                        metric -> normalizeValue(districtData, metric),
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

    private String getMetricKey(String rawMetric) {
        return switch (rawMetric) {
            case "hourlyFloatingPopulation" -> "population";
            case "hourlyStayVisitRatio" -> "stayVisit";
            case "hourlyCongestionRateChange" -> "congestion";
            case "stayPerVisitorDuration" -> "stayPerVisitor";
            case "visitConcentration" -> "visitConcentration";
            case "hourlyAvgStayDurationChange" -> "stayTimeChange";
            default -> rawMetric;
        };
    }

    @SuppressWarnings("unchecked")
    private int normalizeValue(Map<String, Object> data, String key) {
        var hourlyData = (Map<String, Double>) data.getOrDefault(key, Collections.emptyMap());

        return (int) (hourlyData.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0) * 100);
    }
}
