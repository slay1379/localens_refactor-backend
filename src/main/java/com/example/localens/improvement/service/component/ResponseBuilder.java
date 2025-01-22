package com.example.localens.improvement.service.component;

import com.example.localens.improvement.constant.ImprovementConstants;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO.ComparisonData;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO.DistrictSnapshot;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO.MetricChange;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO.MetricsData;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO.RecommendedEvent;
import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.domain.model.MetricDifference;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResponseBuilder {

    public CommercialDistrictComparisonDTO buildResponse(
            Map<String, Object> district1Data,
            Map<String, Object> district2Data,
            Map<String, Integer> metrics1,
            Map<String, Integer> metrics2,
            List<MetricDifference> differences,
            List<Event> events) {

        return CommercialDistrictComparisonDTO.builder()
                .recommendedEvents(events.stream()
                        .map(this::convertToRecommendedEvent)
                        .toList())
                .comparisonData(buildComparisonData(
                        district1Data,
                        district2Data,
                        metrics1,
                        metrics2,
                        differences))
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
        return String.format("%s ~ %s",
                start.format(ImprovementConstants.EVENT_DATE_FORMATTER),
                end.format(ImprovementConstants.EVENT_DATE_FORMATTER));
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
                        .toList())
                .build();
    }

    private DistrictSnapshot buildDistrictSnapshot(
            Map<String, Object> districtData,
            Map<String, Integer> metrics) {

        var metricsData = MetricsData.builder()
                .population(metrics.get("population"))
                .stayVisit(metrics.get("stayVisit"))
                .congestion(metrics.get("congestion"))
                .stayPerVisitor(metrics.get("stayPerVisitor"))
                .visitConcentration(metrics.get("visitConcentration"))
                .stayTimeChange(metrics.get("stayTimeChange"))
                .build();

        return DistrictSnapshot.builder()
                .overallData(Collections.singletonList(metricsData))
                .dates(Collections.singletonList(
                        LocalDate.now().format(ImprovementConstants.DISTRICT_DATE_FORMATTER)))
                .build();
    }
}
