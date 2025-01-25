package com.example.localens.improvement.service.component;

import com.example.localens.analysis.domain.MetricStatistics;
import com.example.localens.analysis.dto.AnalysisRadarDistrictInfoDTO;
import com.example.localens.analysis.dto.RadarDataDTO;
import com.example.localens.analysis.repository.MetricStatisticsRepository;
import com.example.localens.analysis.service.RadarAnalysisService;
import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.domain.EventMetrics;
import com.example.localens.improvement.domain.model.MetricDifference;
import com.example.localens.improvement.repository.EventMetricsRepository;
import com.example.localens.improvement.repository.EventRepository;
import com.example.localens.improvement.repository.MetricRepository;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventMetricsInitializer implements CommandLineRunner {
    private final EventRepository eventRepository;
    private final EventMetricsRepository eventMetricsRepository;
    private final MetricRepository metricRepository;
    private final RadarAnalysisService radarAnalysisService;
    private final MetricStatisticsRepository metricStatisticsRepository;

    @Override
    public void run(String... args) {
        log.info("Starting event metrics initialization...");
        initializeEventMetrics();
        log.info("Event metrics intialization completed");
    }


    private void initializeEventMetrics() {
        List<Event> events = eventRepository.findAll();
        log.info("Found {} events to process", events.size());

        for (Event event : events) {
            try {
                RadarDataDTO<AnalysisRadarDistrictInfoDTO> startData = radarAnalysisService.getRadarDataByDate(
                        event.getEventPlaceInt(), event.getEventStart());
                RadarDataDTO<AnalysisRadarDistrictInfoDTO> endData = radarAnalysisService.getRadarDataByDate(
                        event.getEventPlaceInt(), event.getEventEnd());

                List<MetricDifference> differences = calculateDifferences(
                        startData.getOverallData(),
                        endData.getOverallData()
                );

                differences.stream()
                        .limit(2)
                        .forEach(diff -> {
                            String metricUuid = metricRepository.findMetricsUuidByMetricsName(
                                    convertMetricNameToDbName(diff.getMetricName())
                            );

                            if (metricUuid != null) {
                                EventMetrics eventMetrics = new EventMetrics();
                                eventMetrics.setEventUuid(event.getEventUuid());
                                eventMetrics.setMetricsUuid(UUID.fromString(metricUuid));
                                eventMetricsRepository.save(eventMetrics);
                                log.info("Saved event metrics for event: {}, metric: {}",
                                        event.getEventName(), diff.getMetricName());
                            }
                        });
            } catch (Exception e) {
                log.error("Error processing event {}: {}", event.getEventName(), e.getMessage());
            }
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

    private String convertMetricNameToGlobalDbName(String metricName) {
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

    private List<MetricDifference> calculateDifferences(Map<String, Integer> startMetrics, Map<String, Integer> endMetrics) {
        return startMetrics.entrySet().stream()
                .map(entry -> {
                    String metric = entry.getKey();
                    double startValue = entry.getValue();
                    double endValue = endMetrics.getOrDefault(metric, 0);

                    MetricStatistics stats = metricStatisticsRepository.findByPlaceAndField("GLOBAL",
                            convertMetricNameToGlobalDbName(metric)).orElse(null);

                    double normalizedDiff = 0;
                    if (stats != null && stats.getMaxValue() != stats.getMinValue()) {
                        double normalizedStart = ((startValue - stats.getMinValue()) /
                                (stats.getMaxValue() - stats.getMinValue())) * 100;
                        double normalizedEnd = ((endValue - stats.getMinValue()) /
                                (stats.getMaxValue() - stats.getMinValue())) * 100;
                        normalizedDiff = normalizedEnd - normalizedStart;
                    }

                    return new MetricDifference(metric, (int) normalizedDiff);
                })
                .sorted(Comparator.comparing(MetricDifference::getDifference, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }
}
