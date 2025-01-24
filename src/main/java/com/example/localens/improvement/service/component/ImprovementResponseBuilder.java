package com.example.localens.improvement.service.component;

import com.example.localens.analysis.dto.AnalysisRadarDistrictInfoDTO;
import com.example.localens.analysis.dto.RadarDataDTO;
import com.example.localens.analysis.dto.RadarTimeSeriesDataDTO;
import com.example.localens.analysis.service.RadarAnalysisService;
import com.example.localens.improvement.constant.ImprovementConstants;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO.ComparisonData;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO.DistrictSnapshot;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO.EventComparisonData;
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
public class ImprovementResponseBuilder {
    private final RadarAnalysisService radarAnalysisService;
    private final MetricComparator metricComparator;

    public CommercialDistrictComparisonDTO buildResponse(Integer districtUuid, List<Event> events) {
        List<RecommendedEvent> recommendedEvents = events.stream()
                .map(this::convertToRecommendedEvent)
                .toList();

        List<EventComparisonData> comparisonDataList = events.stream()
                .map(event -> buildEventComparisonData(districtUuid,event))
                .toList();

        return CommercialDistrictComparisonDTO.builder()
                .recommendedEvents(recommendedEvents)
                .comparisonData(comparisonDataList)
                .build();
    }

    private EventComparisonData buildEventComparisonData(Integer districtUuid, Event event) {
        RadarTimeSeriesDataDTO<AnalysisRadarDistrictInfoDTO> startData = radarAnalysisService.getRadarDataByDate(
                districtUuid, event.getEventStart());
        RadarTimeSeriesDataDTO<AnalysisRadarDistrictInfoDTO> endData = radarAnalysisService.getRadarDataByDate(
                districtUuid, event.getEventEnd());

        List<MetricDifference> differences = metricComparator.findSignificantDifferences(startData.getOverallData(),
                endData.getOverallData());

        return EventComparisonData.builder()
                .eventId(event.getEventUuid().toString())
                .startData(buildDistrictSnapshot(startData))
                .endData(buildDistrictSnapshot(endData))
                .changes(differences.stream()
                        .map(diff -> MetricChange.builder()
                                .name(diff.getMetricName())
                                .value(diff.getDifference())
                                .build())
                        .toList())
                .build();
    }

    private DistrictSnapshot buildDistrictSnapshot(
            RadarTimeSeriesDataDTO<AnalysisRadarDistrictInfoDTO> radarData) {

        return DistrictSnapshot.builder()
                .overallData(Collections.singletonList(createMetricsData(radarData.getOverallData())))
                .dates(Collections.singletonList(
                        radarData.getTimeSeriesData().get(0).format(ImprovementConstants.DISTRICT_DATE_FORMATTER)))
                .build();
    }

    private MetricsData createMetricsData(Map<String, Integer> metrics) {
        return MetricsData.builder()
                .population(metrics.get("population"))
                .stayVisit(metrics.get("stayVisit"))
                .congestion(metrics.get("congestion"))
                .stayPerVisitor(metrics.get("stayPerVisitor"))
                .visitConcentration(metrics.get("visitConcentration"))
                .stayTimeChange(metrics.get("stayTimeChange"))
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
}
