package com.example.localens.improvement.service.component;

import com.example.localens.improvement.domain.model.MetricDifference;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricComparator {
    private static final int TOP_DIFFERENCES_LIMIT = 2;

    public List<MetricDifference> findSignificantDifferences(
            Map<String, Integer> metrics1,
            Map<String, Integer> metrics2) {

        var differences = metrics1.entrySet().stream()
                .map(entry -> {
                    String metric = entry.getKey();
                    int diff = metrics2.get(metric) - entry.getValue();
                    log.info("Comparing metric: {}, value1: {}, value2: {}, diff: {}",
                            metric, entry.getValue(), metrics2.get(metric), diff);
                    return new MetricDifference(metric, diff);
                })
                .sorted(Comparator.comparing(MetricDifference::getDifference).reversed())
                .limit(TOP_DIFFERENCES_LIMIT)
                .toList();

        log.info("Selected top differences: {}",
                differences.stream()
                        .map(d -> String.format("%s: %d", d.getMetricName(), d.getDifference()))
                        .collect(Collectors.joining(", ")));

        return differences;
    }

    private MetricDifference createMetricDifference(String metric, Integer value1, Map<String, Integer> metrics2) {
        int diff = metrics2.getOrDefault(metric, 0) - value1;
        return new MetricDifference(metric, diff);
    }
}
