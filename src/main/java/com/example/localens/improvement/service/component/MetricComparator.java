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

        return metrics1.entrySet().stream()
                .map(entry -> {
                    String metric = entry.getKey();
                    int value1 = entry.getValue();
                    int value2 = metrics2.getOrDefault(metric, 0);
                    int diff = value2 - value1;

                    log.info("Comparing {} - First: {}, Second: {}, Difference: {}",
                            metric, value1, value2, diff);

                    return new MetricDifference(metric, diff);
                })
                .filter(diff -> diff.getDifference() != 0)  // 차이가 0인 것은 제외
                .sorted(Comparator.comparing(MetricDifference::getDifference,
                        Comparator.reverseOrder()))  // 절대값이 큰 순서대로 정렬
                .limit(TOP_DIFFERENCES_LIMIT)
                .toList();
    }

    private MetricDifference createMetricDifference(String metric, Integer value1, Map<String, Integer> metrics2) {
        int diff = metrics2.getOrDefault(metric, 0) - value1;
        return new MetricDifference(metric, diff);
    }
}
