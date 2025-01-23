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

        log.info("Comparing metrics - First: {}, Second: {}", metrics1, metrics2);

        List<MetricDifference> differences = metrics1.entrySet().stream()
                .map(entry -> {
                    String metric = entry.getKey();
                    int value1 = entry.getValue();
                    int value2 = metrics2.getOrDefault(metric, 0);
                    int diff = value2 - value1;

                    log.info("Comparing {} - First: {}, Second: {}, Difference: {}",
                            metric, value1, value2, diff);

                    return new MetricDifference(metric, diff);
                })
                .sorted(Comparator.comparing(MetricDifference::getDifference).reversed())
                .limit(TOP_DIFFERENCES_LIMIT)
                .toList();

        log.info("Selected top differences: {}", differences);
        return differences;
    }

    private MetricDifference createMetricDifference(String metric, Integer value1, Map<String, Integer> metrics2) {
        int diff = metrics2.getOrDefault(metric, 0) - value1;
        return new MetricDifference(metric, diff);
    }
}
