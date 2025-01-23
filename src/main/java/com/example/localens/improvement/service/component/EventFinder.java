package com.example.localens.improvement.service.component;

import com.example.localens.improvement.constant.ImprovementConstants;
import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.domain.model.MetricDifference;
import com.example.localens.improvement.repository.EventMetricsRepository;
import com.example.localens.improvement.repository.EventRepository;
import com.example.localens.improvement.repository.MetricRepository;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
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

        List<String> eventUuidStrings = eventMetricsRepository.findEventUuidByMetricsUuidIn(metricUuids);
        log.info("Found event UUID strings (before conversion): {}", eventUuidStrings);

        List<String> convertedUuids = eventUuidStrings.stream()
                .map(this::convertAsciiToHexUuid)
                .distinct()
                .toList();
        log.info("Converted UUIDs: {}", convertedUuids);

        return eventRepository.findAllById(convertedUuids);
    }

    private String convertAsciiToHexUuid(String asciiUuid) {
        try {
            // ASCII 문자열을 바이트 배열로 변환
            byte[] bytes = asciiUuid.getBytes(StandardCharsets.US_ASCII);

            // 바이트를 16진수 문자열로 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : bytes) {
                hexString.append(String.format("%02x", b));
            }

            // "be39ea0e-d8c1-11ef-b2ac-0ef5022d4f7b" 형식으로 변환
            String result = hexString.substring(0, 32);
            return String.format("%s-%s-%s-%s-%s",
                    result.substring(0, 8),
                    result.substring(8, 12),
                    result.substring(12, 16),
                    result.substring(16, 20),
                    result.substring(20, 32));
        } catch (Exception e) {
            log.error("Error converting UUID: {} - {}", asciiUuid, e.getMessage());
            return asciiUuid;
        }
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
