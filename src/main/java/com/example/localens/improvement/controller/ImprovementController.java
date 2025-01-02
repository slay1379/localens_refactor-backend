package com.example.localens.improvement.controller;

import com.example.localens.analysis.controller.DateController;
import com.example.localens.analysis.dto.RadarCongestionRateResponse;
import com.example.localens.analysis.dto.RadarFloatingPopulationResponse;
import com.example.localens.analysis.dto.RadarStayDurationChangeResponse;
import com.example.localens.analysis.dto.RadarStayPerVisitorResponse;
import com.example.localens.analysis.dto.RadarStayVisitRatioResponse;
import com.example.localens.analysis.dto.RadarVisitConcentrationResponse;
import com.example.localens.analysis.service.DateAnalysisService;
import com.example.localens.analysis.service.DateCongestionRateService;
import com.example.localens.analysis.service.DatePopulationService;
import com.example.localens.analysis.service.DateStayDurationRateService;
import com.example.localens.analysis.service.DateStayPerVisitorService;
import com.example.localens.analysis.service.DateStayVisitService;
import com.example.localens.analysis.service.DateVisitConcentrationService;
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
import com.influxdb.client.domain.RoutesSystem;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.aspectj.bridge.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


@RestController
@RequestMapping("/api/improvements")
public class ImprovementController {
    private static final Logger logger = LoggerFactory.getLogger(ImprovementController.class);

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
    private final DatePopulationService datePopulationService;
    private final DateVisitConcentrationService dateVisitConcentrationService;
    private final DateStayVisitService dateStayVisitService;
    private final DateCongestionRateService dateCongestionRateService;
    private final DateStayPerVisitorService dateStayPerVisitorService;
    private final DateStayDurationRateService dateStayDurationRateService;

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
                                 RadarVisitConcentrationService radarVisitConcentrationService,
                                 DatePopulationService datePopulationService,
                                 DateVisitConcentrationService dateVisitConcentrationService,
                                 DateStayVisitService dateStayVisitService,
                                 DateCongestionRateService dateCongestionRateService,
                                 DateStayPerVisitorService dateStayPerVisitorService,
                                 DateStayDurationRateService dateStayDurationRateService) {
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
        this.datePopulationService = datePopulationService;
        this.dateVisitConcentrationService = dateVisitConcentrationService;
        this.dateStayPerVisitorService = dateStayPerVisitorService;
        this.dateCongestionRateService = dateCongestionRateService;
        this.dateStayVisitService = dateStayVisitService;
        this.dateStayDurationRateService = dateStayDurationRateService;
    }


    @GetMapping("/recommendations/{districtUuid1}/{districtUuid2}")
    public ResponseEntity<Map<String, Object>> recommendEventsWithMetrics(
            @PathVariable Integer districtUuid1,
            @PathVariable Integer districtUuid2) {

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
        List<Map<String, Object>> improveMethodList = new ArrayList<>();
        List<Map<String, Object>> beforeOverallDataList = new ArrayList<>();
        List<Map<String, Object>> afterOverallDataList = new ArrayList<>();
        List<String> beforeDates = new ArrayList<>();
        List<String> afterDates = new ArrayList<>();
        List<String> changedFeatureNames = new ArrayList<>();
        List<Integer> changedFeatureValues = new ArrayList<>();

        for (Event event : events) {
            if (event != null) {
                Map<String, Object> improveMethod = new HashMap<>();
                improveMethod.put("image", event.getEventImg());
                improveMethod.put("name", event.getEventName());
                improveMethod.put("date", event.getEventStart().format(formatter) + " ~ " + event.getEventEnd().format(formatter));
                improveMethod.put("area", event.getEventPlace());
                improveMethod.put("detail", event.getInfo());
                improveMethod.put("uuid", event.getEventUuid().toString());
                improveMethodList.add(improveMethod);

                // beforeAndAfter 데이터 구성
                LocalDate parsedDate1 = event.getEventStart().toLocalDate();
                LocalDate parsedDate2 = event.getEventEnd().toLocalDate();

                Map<String, Object> beforeOverallData = new LinkedHashMap<>();
                beforeOverallData.put("population", district1Overall.get("population"));
                beforeOverallData.put("stayVisit", district1Overall.get("stayVisit"));
                beforeOverallData.put("congestion", district1Overall.get("congestion"));
                beforeOverallData.put("stayPerVisitor", district1Overall.get("stayPerVisitor"));
                beforeOverallData.put("visitConcentration", district1Overall.get("visitConcentration"));
                beforeOverallData.put("stayTimeChange", district1Overall.get("stayTimeChange"));
                beforeOverallDataList.add(beforeOverallData);
                beforeDates.add(parsedDate1.format(DateTimeFormatter.ofPattern("yyyy년 MM월")));

                Map<String, Object> afterOverallData = new LinkedHashMap<>();
                afterOverallData.put("population", district2Overall.get("population"));
                afterOverallData.put("stayVisit", district2Overall.get("stayVisit"));
                afterOverallData.put("congestion", district2Overall.get("congestion"));
                afterOverallData.put("stayPerVisitor", district2Overall.get("stayPerVisitor"));
                afterOverallData.put("visitConcentration", district2Overall.get("visitConcentration"));
                afterOverallData.put("stayTimeChange", district2Overall.get("stayTimeChange"));
                afterOverallDataList.add(afterOverallData);
                afterDates.add(parsedDate2.format(DateTimeFormatter.ofPattern("yyyy년 MM월")));

                String biggestDifferenceMetric = null;
                int biggestDifferenceValue = Integer.MIN_VALUE;
                for (String key : district2Overall.keySet()) {
                    if (district1Overall.containsKey(key)) {
                        int difference = district2Overall.get(key) - district1Overall.get(key);
                        if (difference > biggestDifferenceValue) {
                            biggestDifferenceMetric = key;
                            biggestDifferenceValue = difference;
                        }
                    }
                }
                changedFeatureNames.add(biggestDifferenceMetric);
                changedFeatureValues.add(biggestDifferenceValue);
            }
        }

        Map<String, Object> before = new LinkedHashMap<>();
        before.put("overallData", beforeOverallDataList);
        before.put("date", beforeDates);

        Map<String, Object> after = new LinkedHashMap<>();
        after.put("overallData", afterOverallDataList);
        after.put("date", afterDates);

        Map<String, Object> beforeAndAfter = new LinkedHashMap<>();
        beforeAndAfter.put("before", before);
        beforeAndAfter.put("after", after);
        beforeAndAfter.put("changedFeature", Map.of(
                "name", changedFeatureNames,
                "value", changedFeatureValues
        ));

        // 최종 응답 구성
        Map<String, Object> response = new HashMap<>();
        response.put("ImproveMethod", improveMethodList);
        response.put("beforeAndAfter", beforeAndAfter);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

/*
    @GetMapping("/reco/{districtUuid1}/{districtUuid2}")
    public ResponseEntity<Map<String, Object>> recommendEvents(
            @PathVariable Integer districtUuid1,
            @PathVariable Integer districtUuid2) {

    }*/

    @GetMapping("/reco")
    public ResponseEntity<Map<String, Object>> getImprovedEvents() {
        List<Event> events = eventRepository.findAll();

        // 원하는 JSON 형식의 "ImproveMethod" 생성
        List<Map<String, Object>> improveMethods = new ArrayList<>();
        for (int i = 0; i < 3 && i < events.size(); i++) {
            Event event = events.get(i);
            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put("date", event.getEventStart() + " ~ " + event.getEventEnd());
            eventMap.put("area", event.getEventPlace());
            eventMap.put("image", event.getEventImg());
            eventMap.put("name", event.getEventName());
            eventMap.put("detail", event.getInfo());
            eventMap.put("uuid", event.getEventUuid());
            improveMethods.add(eventMap);
        }

        // "beforeAndAfter" 데이터 생성 (각 이벤트에 맞는 데이터 생성)
        Map<String, Object> beforeAndAfter = new HashMap<>();
        List<Map<String, Object>> overallDataBefore = new ArrayList<>();
        List<Map<String, Object>> overallDataAfter = new ArrayList<>();
        List<Integer> changedValues = new ArrayList<>();
        List<String> changedNames = new ArrayList<>();

        // 각 이벤트에 대해 서로 다른 "before" 및 "after" 데이터 추가
        for (int i = 0; i < 3 && i < events.size(); i++) {
            Map<String, Object> beforeData = new HashMap<>();
            beforeData.put("population", (i + 1) * 10 + 20);
            beforeData.put("stayVisit", (i + 1) * 5 + 30);
            beforeData.put("congestion", (i + 1) * 10);
            beforeData.put("stayPerVisitor", (i + 1) * 3 + 10);
            beforeData.put("visitConcentration", (i + 1) * 7 + 20);
            beforeData.put("stayTimeChange", (i + 1) * 5 + 25);
            overallDataBefore.add(beforeData);

            Map<String, Object> afterData = new HashMap<>();
            afterData.put("population", (i + 1) * 15 + 30);
            afterData.put("stayVisit", (i + 1) * 10 + 40);
            afterData.put("congestion", (i + 1) * 5 + 20);
            afterData.put("stayPerVisitor", (i + 1) * 2 + 10);
            afterData.put("visitConcentration", (i + 1) * 8 + 30);
            afterData.put("stayTimeChange", (i + 1) * 7 + 35);
            overallDataAfter.add(afterData);

            changedValues.add((i + 1) * 6 + 20);
            changedNames.add("featureChange" + (i + 1));
        }

        beforeAndAfter.put("before", Map.of(
                "overallData", overallDataBefore,
                "date", Arrays.asList("2024년 03월", "2023년 12월", "2023년 10월")
        ));
        beforeAndAfter.put("after", Map.of(
                "overallData", overallDataAfter,
                "date", Arrays.asList("2024년 04월", "2024년 01월", "2023년 10월")
        ));

        // changedFeature를 beforeAndAfter의 맨 아래로 이동
        beforeAndAfter.put("changedFeature", Map.of(
                "value", changedValues,
                "name", changedNames
        ));

        // 최종 JSON 반환
        Map<String, Object> response = new HashMap<>();
        response.put("ImproveMethod", improveMethods);
        response.put("beforeAndAfter", beforeAndAfter);

        return ResponseEntity.ok(response);
    }


}
