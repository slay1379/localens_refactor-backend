package com.example.localens.improvement.service;

import com.example.localens.analysis.dto.AnalysisRadarDistrictInfoDTO;
import com.example.localens.analysis.dto.RadarDataDTO;
import com.example.localens.analysis.service.PopulationDetailsService;
import com.example.localens.analysis.service.RadarAnalysisService;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO;
import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.domain.model.MetricDifference;
import com.example.localens.improvement.repository.EventMetricsRepository;
import com.example.localens.improvement.repository.EventRepository;
import com.example.localens.improvement.repository.MetricRepository;
import com.example.localens.improvement.service.component.EventFinder;
import com.example.localens.improvement.service.component.MetricComparator;
import com.example.localens.improvement.service.component.MetricsNormalizer;
import com.example.localens.improvement.service.component.ImprovementResponseBuilder;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImprovementService {
    private final RadarAnalysisService radarAnalysisService;
    private final PopulationDetailsService populationDetailsService;
    private final MetricRepository metricRepository;
    private final EventMetricsRepository eventMetricsRepository;
    private final EventRepository eventRepository;
    private final MetricsNormalizer metricsNormalizer;
    private final MetricComparator metricComparator;
    private final EventFinder eventFinder;
    private final ImprovementResponseBuilder improvementResponseBuilder;

    public CommercialDistrictComparisonDTO compareDistricts(Integer districtUuid1, Integer districtUuid2) {
        log.info("Comparing districts {} and {}", districtUuid1, districtUuid2);
        // 1. 각 상권의 데이터 조회
        RadarDataDTO<AnalysisRadarDistrictInfoDTO> radar1Data = radarAnalysisService.getRadarData(districtUuid1);
        RadarDataDTO<AnalysisRadarDistrictInfoDTO> radar2Data = radarAnalysisService.getRadarData(districtUuid2);

        Map<String, Integer> metrics1 = radar1Data.getOverallData();
        Map<String, Integer> metrics2 = radar2Data.getOverallData();

        log.info("Normalized metrics - District 1: {}", metrics1);
        log.info("Normalized metrics - District 2: {}", metrics2);

        List<MetricDifference> differences = metricComparator.findSignificantDifferences(
                metrics1, metrics2);
        log.info("Found {} significant differences", differences.size());

        List<Event> recommendedEvents = eventFinder.findRelevantEvents(differences);
        log.info("Found {} recommended events", recommendedEvents.size());

        return improvementResponseBuilder.buildResponse(
                radar1Data,
                radar2Data,
                metrics1,
                metrics2,
                differences,
                recommendedEvents
        );
    }
}
