package com.example.localens.improvement.service.component;

import com.example.localens.improvement.domain.model.MetricDifference;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MetricComparator {
    private static final int TOP_DIFFERENCES_LIMIT = 2;

    public List<MetricDifference> findSignificantDifferences(
            Map<String, Integer> metrics1,
            Map<String, Integer> metrics2) {

        return metrics1.entrySet().stream()
                .map(entry -> createMetricDifference(entry.getKey(), entry.getValue(), metrics2))
                .sorted(Comparator.comparing(MetricDifference::getDifference).reversed())
                .limit(TOP_DIFFERENCES_LIMIT)
                .toList();
    }

    private MetricDifference createMetricDifference(String metric, Integer value1, Map<String, Integer> metrics2) {
        int diff = metrics2.getOrDefault(metric, 0) - value1;
        return new MetricDifference(metric, diff);
    }
}
