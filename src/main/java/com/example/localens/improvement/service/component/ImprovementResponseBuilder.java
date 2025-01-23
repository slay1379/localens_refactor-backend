package com.example.localens.improvement.service.component;

import com.example.localens.analysis.dto.AnalysisRadarDistrictInfoDTO;
import com.example.localens.analysis.dto.RadarDataDTO;
import com.example.localens.analysis.dto.RadarTimeSeriesDataDTO;
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
public class ImprovementResponseBuilder {

    public CommercialDistrictComparisonDTO buildResponse(
            RadarTimeSeriesDataDTO<AnalysisRadarDistrictInfoDTO> radar1StartData,
            RadarTimeSeriesDataDTO<AnalysisRadarDistrictInfoDTO> radar1EndData,
            RadarTimeSeriesDataDTO<AnalysisRadarDistrictInfoDTO> radar2StartData,
            RadarTimeSeriesDataDTO<AnalysisRadarDistrictInfoDTO> radar2EndData,
            List<MetricDifference> differences,
            List<Event> events) {

        return CommercialDistrictComparisonDTO.builder()
                .recommendedEvents(events.stream()
                        .map(this::convertToRecommendedEvent)
                        .toList())
                .comparisonData(buildComparisonData(
                        radar1StartData,
                        radar1EndData,
                        radar2StartData,
                        radar2EndData,
                        differences))
                .build();
    }

    private ComparisonData buildComparisonData(
            RadarTimeSeriesDataDTO<AnalysisRadarDistrictInfoDTO> radar1StartData,
            RadarTimeSeriesDataDTO<AnalysisRadarDistrictInfoDTO> radar1EndData,
            RadarTimeSeriesDataDTO<AnalysisRadarDistrictInfoDTO> radar2StartData,
            RadarTimeSeriesDataDTO<AnalysisRadarDistrictInfoDTO> radar2EndData,
            List<MetricDifference> differences) {

        return ComparisonData.builder()
                .before(buildDistrictSnapshot(radar1StartData))
                .after(buildDistrictSnapshot(radar2EndData))
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
