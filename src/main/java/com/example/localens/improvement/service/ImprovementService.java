package com.example.localens.improvement.service;

import com.example.localens.analysis.service.PopulationDetailsService;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO.ComparisonData;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO.DistrictSnapshot;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO.MetricChange;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO.MetricsData;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO.RecommendedEvent;
import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.domain.EventMetrics;
import com.example.localens.improvement.repository.EventMetricsRepository;
import com.example.localens.improvement.repository.EventRepository;
import com.example.localens.improvement.repository.MetricRepository;
import com.example.localens.influx.InfluxDBService;
import com.example.localens.s3.service.S3Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImprovementService {
    private final PopulationDetailsService populationDetailsService;
    private final MetricRepository metricRepository;
    private final EventMetricsRepository eventMetricsRepository;
    private final EventRepository eventRepository;

    public CommercialDistrictComparisonDTO compareDistricts(Integer districtUuid1, Integer districtUuid2) {
        // 1. 각 상권의 데이터 조회
        Map<String, Object> district1Data = populationDetailsService.getDetailsByDistrictUuid(districtUuid1);
        Map<String, Object> district2Data = populationDetailsService.getDetailsByDistrictUuid(districtUuid2);

        // 2. 데이터 정규화 및 비교
        Map<String, Integer> metrics1 = normalizeMetrics(district1Data);
        Map<String, Integer> metrics2 = normalizeMetrics(district2Data);

        // 3. 주요 차이점 식별
        List<MetricDifference> significantDifferences = findSignificantDifferences(metrics1, metrics2);

        // 4. 관련 이벤트 조회
        List<Event> recommendedEvents = findRelevantEvents(significantDifferences);

        // 5. 응답 데이터 구성
        return buildComparisonResponse(
                district1Data,
                district2Data,
                metrics1,
                metrics2,
                significantDifferences,
                recommendedEvents
        );
    }

    @Getter
    @AllArgsConstructor
    private static class MetricDifference {
        private String metricName;
        private int difference;
    }

    private Map<String, Integer> normalizeMetrics(Map<String, Object> districtData) {
        Map<String, Integer> normalized = new LinkedHashMap<>();

        normalized.put("population", normalizeValue(districtData, "hourlyFloatingPopulation"));
        normalized.put("stayVisit", normalizeValue(districtData, "hourlyStayVisitRatio"));
        normalized.put("congestion", normalizeValue(districtData, "hourlyCongestionRateChange"));
        normalized.put("stayPerVisitor", normalizeValue(districtData, "stayPerVisitorDuration"));
        normalized.put("visitConcentration", normalizeValue(districtData, "visitConcentration"));
        normalized.put("stayTimeChange", normalizeValue(districtData, "hourlyAvgStayDurationChange"));

        return normalized;
    }

    private int normalizeValue(Map<String, Object> data, String key) {
        Map<String, Double> hourlyData = (Map<String, Double>) data.get(key);
        if (hourlyData == null || hourlyData.isEmpty()) {
            return 0;
        }

        double average = hourlyData.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        return (int) (average * 100);
    }

    private List<MetricDifference> findSignificantDifferences(Map<String, Integer> metrics1,
                                                              Map<String, Integer> metrics2) {
        return metrics1.entrySet().stream()
                .map(entry -> {
                    String metric = entry.getKey();
                    int diff = metrics2.get(metric) - entry.getValue();
                    return new MetricDifference(metric, diff);
                })
                .sorted(Comparator.comparing(MetricDifference::getDifference).reversed())
                .limit(2)
                .collect(Collectors.toList());
    }

    private List<Event> findRelevantEvents(List<MetricDifference> differences) {
        List<String> metricUuids = differences.stream()
                .map(diff -> metricRepository.findMetricsUuidByMetricsName(diff.getMetricName()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (metricUuids.isEmpty()) {
            log.warn("No matching metrics found for differences: {}", differences);
            return Collections.emptyList();
        }

        List<String> eventUuids = eventMetricsRepository.findEventUuidByMetricsUuidIn(metricUuids);
        if (eventUuids.isEmpty()) {
            log.warn("No events found for metric UUIDs: {}", metricUuids);
            return Collections.emptyList();
        }

        return eventRepository.findAllById(eventUuids);
    }

    private CommercialDistrictComparisonDTO buildComparisonResponse(
            Map<String, Object> district1Data,
            Map<String, Object> district2Data,
            Map<String, Integer> metrics1,
            Map<String, Integer> metrics2,
            List<MetricDifference> differences,
            List<Event> events) {

        List<RecommendedEvent> recommendedEvents = events.stream()
                .map(this::convertToRecommendedEvent)
                .collect(Collectors.toList());

        ComparisonData comparisonData = buildComparisonData(
                district1Data,
                district2Data,
                metrics1,
                metrics2,
                differences
        );

        return CommercialDistrictComparisonDTO.builder()
                .recommendedEvents(recommendedEvents)
                .comparisonData(comparisonData)
                .build();
    }

    private RecommendedEvent convertToRecommendedEvent(Event event) {
        return RecommendedEvent.builder()
                .uuid(event.getEventUuid().toString())
                .name(event.getEventName())
                .imageUrl(event.getEventImg())
                .place(event.getEventPlace())
                .period(formatEventPeriod(event.getEventStart(), event.getEventEnd()))
                .detail(event.getInfo())
                .build();
    }

    private String formatEventPeriod(LocalDateTime start, LocalDateTime end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        return String.format("%s ~ %s",
                start.format(formatter),
                end.format(formatter));
    }

    private ComparisonData buildComparisonData(
            Map<String, Object> district1Data,
            Map<String, Object> district2Data,
            Map<String, Integer> metrics1,
            Map<String, Integer> metrics2,
            List<MetricDifference> differences) {

        return ComparisonData.builder()
                .before(buildDistrictSnapshot(district1Data, metrics1))
                .after(buildDistrictSnapshot(district2Data, metrics2))
                .changes(differences.stream()
                        .map(diff -> MetricChange.builder()
                                .name(diff.getMetricName())
                                .value(diff.getDifference())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private DistrictSnapshot buildDistrictSnapshot(
            Map<String, Object> districtData,
            Map<String, Integer> metrics) {

        MetricsData metricsData = MetricsData.builder()
                .population(metrics.get("population"))
                .stayVisit(metrics.get("stayVisit"))
                .congestion(metrics.get("congestion"))
                .stayPerVisitor(metrics.get("stayPerVisitor"))
                .visitConcentration(metrics.get("visitConcentration"))
                .stayTimeChange(metrics.get("stayTimeChange"))
                .build();

        // 날짜 정보는 실제 데이터에 맞게 조정 필요
        String formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월"));

        return DistrictSnapshot.builder()
                .overallData(Collections.singletonList(metricsData))
                .dates(Collections.singletonList(formattedDate))
                .build();
    }
}
