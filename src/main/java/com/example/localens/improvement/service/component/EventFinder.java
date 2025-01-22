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
        var metricUuids = findMetricUuids(differences);
        if (metricUuids.isEmpty()) {
            log.warn("No matching metrics found for differences: {}", differences);
            return Collections.emptyList();
        }

        var eventUuids = eventMetricsRepository.findEventUuidByMetricsUuidIn(metricUuids);
        if (eventUuids.isEmpty()) {
            log.warn("No events found for metric UUIDs: {}", metricUuids);
            return Collections.emptyList();
        }

        return eventRepository.findAllById(eventUuids);
    }

    private List<String> findMetricUuids(List<MetricDifference> differences) {
        return differences.stream()
                .map(diff -> {
                    var dbMetricName = ImprovementConstants.METRIC_DB_MAPPING
                            .getOrDefault(diff.getMetricName(), diff.getMetricName());
                    var uuid = metricRepository.findMetricsUuidByMetricsName(dbMetricName);
                    log.debug("Metric mapping: {} -> {} (UUID: {})",
                            diff.getMetricName(), dbMetricName, uuid);
                    return uuid;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
