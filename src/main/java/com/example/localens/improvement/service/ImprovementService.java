package com.example.localens.improvement.service;

import com.example.localens.analysis.dto.AnalysisRadarDistrictInfoDTO;
import com.example.localens.analysis.dto.RadarDataDTO;
import com.example.localens.analysis.dto.RadarTimeSeriesDataDTO;
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
import java.time.LocalDateTime;
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
        LocalDateTime startMonth = LocalDateTime.now().withDayOfMonth(1);
        LocalDateTime endMonth = startMonth.plusMonths(1);

        RadarTimeSeriesDataDTO<AnalysisRadarDistrictInfoDTO> radar1StartData =
                radarAnalysisService.getRadarDataByDate(districtUuid1, startMonth);
        RadarTimeSeriesDataDTO<AnalysisRadarDistrictInfoDTO> radar1EndData =
                radarAnalysisService.getRadarDataByDate(districtUuid1, endMonth);
        RadarTimeSeriesDataDTO<AnalysisRadarDistrictInfoDTO> radar2StartData =
                radarAnalysisService.getRadarDataByDate(districtUuid2, startMonth);
        RadarTimeSeriesDataDTO<AnalysisRadarDistrictInfoDTO> radar2EndData =
                radarAnalysisService.getRadarDataByDate(districtUuid2, endMonth);

        List<MetricDifference> differences = metricComparator.findSignificantDifferences(
                radar1EndData.getOverallData(),
                radar2EndData.getOverallData());

        List<Event> recommendedEvents = eventFinder.findRelevantEvents(differences);

        return improvementResponseBuilder.buildResponse(
                radar1StartData,
                radar1EndData,
                radar2StartData,
                radar2EndData,
                differences,
                recommendedEvents);
    }
}
