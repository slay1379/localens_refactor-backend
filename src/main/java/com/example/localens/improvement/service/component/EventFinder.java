package com.example.localens.improvement.service.component;

import com.example.localens.improvement.constant.ImprovementConstants;
import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.domain.model.MetricDifference;
import com.example.localens.improvement.repository.EventMetricsRepository;
import com.example.localens.improvement.repository.EventRepository;
import com.example.localens.improvement.repository.MetricRepository;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventFinder {
    private final MetricRepository metricRepository;
    private final EventMetricsRepository eventMetricsRepository;
    private final EventRepository eventRepository;

    public List<Event> findRelevantEvents(List<MetricDifference> differences) {
        log.info("Finding events for differences: {}", differences);

        List<String> metricUuids = differences.stream()
                .map(diff -> {
                    String dbMetricName = convertMetricNameToDbName(diff.getMetricName());
                    String uuid = metricRepository.findMetricsUuidByMetricsName(dbMetricName);
                    log.info("Converting {} -> {} (UUID: {})",
                            diff.getMetricName(), dbMetricName, uuid);
                    return uuid;
                })
                .filter(Objects::nonNull)
                .toList();

        log.info("Found metric UUIDs: {}", metricUuids);

        if (metricUuids.isEmpty()) {
            log.warn("No matching metrics found");
            return Collections.emptyList();
        }

        List<String> eventUuids = eventMetricsRepository.findEventUuidByMetricsUuidIn(metricUuids);
        log.info("Found event UUIDs: {}", eventUuids);

        return eventRepository.findAllById(eventUuids);
    }

    private String convertMetricNameToDbName(String metricName) {
        return switch (metricName) {
            case "stayVisit" -> "STAY_VISIT_RATIO";
            case "stayPerVisitor" -> "STAY_PER_VISITOR";
            case "population" -> "TOTAL_POPULATION";
            case "congestion" -> "CONGESTION_RATE";
            case "visitConcentration" -> "VISIT_CONCENTRATION";
            case "stayTimeChange" -> "STAY_TIME_CHANGE";
            default -> metricName;
        };
    }
}
