package com.example.localens.improvement.controller;

import com.example.localens.analysis.controller.DateController;
import com.example.localens.analysis.dto.RadarCongestionRateResponse;
import com.example.localens.analysis.dto.RadarFloatingPopulationResponse;
import com.example.localens.analysis.dto.RadarStayDurationChangeResponse;
import com.example.localens.analysis.dto.RadarStayPerVisitorResponse;
import com.example.localens.analysis.dto.RadarStayVisitRatioResponse;
import com.example.localens.analysis.dto.RadarVisitConcentrationResponse;
import com.example.localens.analysis.service.DateAnalysisService;
import com.example.localens.analysis.service.RadarComparisonService;
import com.example.localens.analysis.service.RadarCongestionRateService;
import com.example.localens.analysis.service.RadarFloatingPopulationService;
import com.example.localens.analysis.service.RadarInfoService;
import com.example.localens.analysis.service.RadarStayDurationChangeService;
import com.example.localens.analysis.service.RadarStayPerVisitorService;
import com.example.localens.analysis.service.RadarStayVisitRatioService;
import com.example.localens.analysis.service.RadarVisitConcentrationService;
import com.example.localens.customfeature.domain.CustomFeature;
import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.repository.EventMetricsRepository;
import com.example.localens.improvement.repository.EventRepository;
import com.example.localens.improvement.repository.MetricRepository;
import com.example.localens.improvement.service.ImprovementService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/improvements")
public class ImprovementController {

    private final ImprovementService improvementService;
    private final MetricRepository metricRepository;
    private final EventRepository eventRepository;
    private final EventMetricsRepository eventMetricsRepository;
    private final DateController dateController;
    private final DateAnalysisService dateAnalysisService;

    private final RadarComparisonService radarComparisonService;
    private final RadarFloatingPopulationService radarFloatingPopulationService;
    private final RadarStayVisitRatioService radarStayVisitRatioService;
    private final RadarCongestionRateService radarCongestionRateService;
    private final RadarStayPerVisitorService radarStayPerVisitorService;
    private final RadarStayDurationChangeService radarStayDurationChangeService;
    private final RadarInfoService radarInfoService;
    private final RadarVisitConcentrationService radarVisitConcentrationService;

    @Autowired
    public ImprovementController(ImprovementService improvementService,
                                 MetricRepository metricRepository,
                                 EventRepository eventRepository,
                                 EventMetricsRepository eventMetricsRepository,
                                 DateController dateController,
                                 DateAnalysisService dateAnalysisService,
                                 RadarComparisonService radarComparisonService,
                                 RadarFloatingPopulationService radarFloatingPopulationService,
                                 RadarStayVisitRatioService radarStayVisitRatioService,
                                 RadarCongestionRateService radarCongestionRateService,
                                 RadarStayPerVisitorService radarStayPerVisitorService,
                                 RadarStayDurationChangeService radarStayDurationChangeService,
                                 RadarInfoService radarInfoService,
                                 RadarVisitConcentrationService radarVisitConcentrationService) {
        this.improvementService = improvementService;
        this.metricRepository = metricRepository;
        this.eventRepository = eventRepository;
        this.eventMetricsRepository = eventMetricsRepository;
        this.dateController = dateController;
        this.dateAnalysisService = dateAnalysisService;
        this.radarComparisonService = radarComparisonService;
        this.radarFloatingPopulationService = radarFloatingPopulationService;
        this.radarStayVisitRatioService = radarStayVisitRatioService;
        this.radarCongestionRateService = radarCongestionRateService;
        this.radarStayPerVisitorService = radarStayPerVisitorService;
        this.radarStayDurationChangeService = radarStayDurationChangeService;
        this.radarInfoService = radarInfoService;
        this.radarVisitConcentrationService = radarVisitConcentrationService;
    }

    @GetMapping("/recommendations/{districtUuid1}/{districtUuid2}")
    public ResponseEntity<Map<String, Object>> recommendEventsWithMetrics(
            @PathVariable Integer districtUuid1,
            @PathVariable Integer districtUuid2) {

        // 두 상권의 데이터를 각각 가져옴
        Map<String, Object> district1Data = radarComparisonService.constructDistrictData(
                districtUuid1,
                radarFloatingPopulationService,
                radarStayVisitRatioService,
                radarCongestionRateService,
                radarStayPerVisitorService,
                radarVisitConcentrationService,
                radarStayDurationChangeService,
                radarInfoService
        );

        Map<String, Object> district2Data = radarComparisonService.constructDistrictData(
                districtUuid2,
                radarFloatingPopulationService,
                radarStayVisitRatioService,
                radarCongestionRateService,
                radarStayPerVisitorService,
                radarVisitConcentrationService,
                radarStayDurationChangeService,
                radarInfoService
        );

        // 두 상권의 overallData 추출
        Map<String, Integer> district1Overall = (Map<String, Integer>) district1Data.get("overallData");
        Map<String, Integer> district2Overall = (Map<String, Integer>) district2Data.get("overallData");

        // 각 지표의 차이를 계산하여 저장할 리스트 생성
        List<Map.Entry<String, Integer>> differences = new ArrayList<>();

        for (String key : district1Overall.keySet()) {
            if (district2Overall.containsKey(key)) {
                int value1 = district1Overall.get(key);
                int value2 = district2Overall.get(key);
                int difference = Math.abs(value1 - value2);
                differences.add(new AbstractMap.SimpleEntry<>(key, difference));
            }
        }

        // 차이를 기준으로 내림차순 정렬
        differences.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // 차이가 가장 많이 나는 지표 두 개 추출
        List<String> topTwoDifferences = new ArrayList<>();
        if (differences.size() >= 2) {
            topTwoDifferences.add(differences.get(0).getKey());
            topTwoDifferences.add(differences.get(1).getKey());
        } else if (differences.size() == 1) {
            topTwoDifferences.add(differences.get(0).getKey());
        }

        List<String> metricsUuids = new ArrayList<>();
        for (String metricName : topTwoDifferences) {
            // event_metric_change_type 테이블에서 metrics_uuid 찾기
            String metricsUuid = metricRepository.findMetricsUuidByMetricsName(metricName);
            if (metricsUuid != null) {
                metricsUuids.add(metricsUuid);
            }
        }

        // metrics_uuid가 event_metrics 테이블에서 매칭되는 event_uuid 찾기
        List<String> eventUuids = eventMetricsRepository.findEventUuidByMetricsUuidIn(metricsUuids);

        // 찾은 event_uuid를 통해 event 테이블에서 이벤트 정보 가져오기
        List<Event> events = eventRepository.findAllById(eventUuids);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

        // 이벤트 정보를 improveMethod 리스트에 추가
        List<Map<String, Object>> eventsInfo = new ArrayList<>();
        for (Event event : events) {
            if (event != null) {
                Map<String, String> ImproveMethod = new HashMap<>();
                ImproveMethod.put("eventUuid", event.getEventUuid().toString());
                ImproveMethod.put("image", event.getEventImg());
                ImproveMethod.put("name", event.getEventName());
                ImproveMethod.put("date", event.getEventStart().format(formatter) + " ~ " + event.getEventEnd().format(formatter));
                ImproveMethod.put("area", event.getEventPlace());
                ImproveMethod.put("detail", event.getInfo());

                // beforeAndAfter 데이터 구성
                LocalDate parsedDate1 = event.getEventStart().toLocalDate();
                LocalDate parsedDate2 = event.getEventEnd().toLocalDate();

                Map<String, Object> before = new LinkedHashMap<>();
                before.put("overallData", district1Overall);
                before.put("date", Collections.singletonList(parsedDate1.toString()));  // 날짜만 추가

                Map<String, Object> after = new LinkedHashMap<>();
                after.put("overallData", district2Overall);
                after.put("date", Collections.singletonList(parsedDate2.toString()));  // 날짜만 추가

                String biggestDifferenceMetric = differences.get(0).getKey();
                int biggestDifferenceValue = district2Overall.get(biggestDifferenceMetric) - district1Overall.get(biggestDifferenceMetric);

                Map<String, Object> changedFeature = new LinkedHashMap<>();
                changedFeature.put("name", biggestDifferenceMetric);
                changedFeature.put("value", biggestDifferenceValue);

                Map<String, Object> beforeAndAfter = new LinkedHashMap<>();
                beforeAndAfter.put("before", before);
                beforeAndAfter.put("after", after);
                beforeAndAfter.put("changedFeature", changedFeature);

                Map<String, Object> eventWithBeforeAfter = new HashMap<>();
                eventWithBeforeAfter.put("ImproveMethod", ImproveMethod);
                eventWithBeforeAfter.put("beforeAndAfter", beforeAndAfter);

                eventsInfo.add(eventWithBeforeAfter);
            }
        }

        // 최종 응답 구성
        Map<String, Object> response = new HashMap<>();
        response.put("eventsInfo", eventsInfo);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
