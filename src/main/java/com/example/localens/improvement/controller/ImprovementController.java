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

        logger.debug("district1Data: {}", district1Data);
        logger.debug("district2Data: {}", district2Data);

        // 두 상권의 overallData 추출
        Object district1OverallObj = district1Data.get("overallData");
        Object district2OverallObj = district2Data.get("overallData");

        // 안전하게 Map<String, Integer> 형태로 변환하는 헬퍼 메서드
        Map<String, Integer> district1Overall = convertToIntegerMap(district1OverallObj);
        Map<String, Integer> district2Overall = convertToIntegerMap(district2OverallObj);

        logger.debug("district1Overall: {}", district1Overall);
        logger.debug("district2Overall: {}", district2Overall);

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

        List<UUID> metricsUuids = new ArrayList<>();
        for (String metricName : topTwoDifferences) {
            // event_metric_change_type 테이블에서 metrics_uuid 찾기
            UUID metricsUuid = metricRepository.findMetricsUuidByMetricsName(metricName);
            if (metricsUuid != null) {
                metricsUuids.add(metricsUuid);
            }
        }

        // metrics_uuid가 event_metrics 테이블에서 매칭되는 event_uuid 찾기
        List<UUID> eventUuids = eventMetricsRepository.findEventUuidByMetricsUuidIn(metricsUuids);

        // 찾은 event_uuid를 통해 event 테이블에서 이벤트 정보 가져오기
        List<Event> events = eventRepository.findAllById(eventUuids);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_DATE_TIME; // ISO 포맷 변환을 위한 포맷터

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
                improveMethod.put("date",
                        event.getEventStart().format(formatter) + " ~ " + event.getEventEnd().format(formatter));
                improveMethod.put("area", event.getEventPlace());
                improveMethod.put("detail", event.getInfo());
                improveMethod.put("uuid", event.getEventUuid().toString());
                improveMethodList.add(improveMethod);

                String startDateTimeStr = event.getEventStart().format(isoFormatter);
                String endDateTimeStr = event.getEventEnd().format(isoFormatter);

                int normalizedPopulationValue1 = datePopulationService.getNormalizedPopulationValue(event.getEventPlaceInt(), startDateTimeStr);
                int visitConcentrationValue1 = dateVisitConcentrationService.getNormalizedPopulationValue(event.getEventPlaceInt(), startDateTimeStr);
                int stayVisitRatioValue1 = dateStayVisitService.getNormalizedStayVisitRatio(event.getEventPlaceInt(), startDateTimeStr);
                int congestionRateValue1 = dateCongestionRateService.getNormalizedCongestionRate(event.getEventPlaceInt(), startDateTimeStr);
                int stayPerVisitorValue1 = dateStayPerVisitorService.getNormalizedStayPerVisitorValue(event.getEventPlaceInt(), startDateTimeStr);
                int stayDurationRateValue1 = dateStayDurationRateService.getNormalizedStayDurationRate(event.getEventPlaceInt(), startDateTimeStr);

                Map<String, Integer> values1 = new LinkedHashMap<>();
                values1.put("population", normalizedPopulationValue1);
                values1.put("stayVisit", stayVisitRatioValue1);
                values1.put("visitConcentration", visitConcentrationValue1);
                values1.put("congestion", congestionRateValue1);
                values1.put("stayPerVisitor", stayPerVisitorValue1);
                values1.put("stayTimeChange", stayDurationRateValue1);

                int normalizedPopulationValue2 = datePopulationService.getNormalizedPopulationValue(event.getEventPlaceInt(), endDateTimeStr);
                int visitConcentrationValue2 = dateVisitConcentrationService.getNormalizedPopulationValue(event.getEventPlaceInt(), endDateTimeStr);
                int stayVisitRatioValue2 = dateStayVisitService.getNormalizedStayVisitRatio(event.getEventPlaceInt(), endDateTimeStr);
                int congestionRateValue2 = dateCongestionRateService.getNormalizedCongestionRate(event.getEventPlaceInt(), endDateTimeStr);
                int stayPerVisitorValue2 = dateStayPerVisitorService.getNormalizedStayPerVisitorValue(event.getEventPlaceInt(), endDateTimeStr);
                int stayDurationRateValue2 = dateStayDurationRateService.getNormalizedStayDurationRate(event.getEventPlaceInt(), endDateTimeStr);

                Map<String, Integer> values2 = new LinkedHashMap<>();
                values2.put("population", normalizedPopulationValue2);
                values2.put("stayVisit", stayVisitRatioValue2);
                values2.put("visitConcentration", visitConcentrationValue2);
                values2.put("congestion", congestionRateValue2);
                values2.put("stayPerVisitor", stayPerVisitorValue2);
                values2.put("stayTimeChange", stayDurationRateValue2);

                // 변화량 계산
                Map<String, Integer> diffMap = new LinkedHashMap<>();
                for (String key : values1.keySet()) {
                    if (values2.containsKey(key)) {
                        int v1 = values1.get(key);
                        int v2 = values2.get(key);
                        int diff = v2 - v1;
                        diffMap.put(key, diff);
                    }
                }

                Entry<String, Integer> maxEntry = diffMap.entrySet().stream()
                        .max(Entry.comparingByValue())
                        .orElse(null);

                if (maxEntry != null) {
                    changedFeatureNames.add(maxEntry.getKey());
                    changedFeatureValues.add(maxEntry.getValue());
                }

                beforeOverallDataList.add(Map.of("values", values1));
                afterOverallDataList.add(Map.of("values", values2));
                beforeDates.add(event.getEventStart().format(formatter));
                afterDates.add(event.getEventEnd().format(formatter));
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

    /**
     * overallData를 안전하게 Map<String, Integer> 형태로 변환하는 헬퍼 메서드.
     * 해당 메서드는 전체 데이터 구조를 보장할 수 없으므로, 만약 값이 Integer가 아닌 경우 예외를 발생시킨다.
     */
    private Map<String, Integer> convertToIntegerMap(Object obj) {
        if (!(obj instanceof Map)) {
            throw new ClassCastException("The overallData is not of type Map");
        }
        Map<?, ?> rawMap = (Map<?, ?>) obj;
        Map<String, Integer> intMap = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                throw new ClassCastException("Key is not a String: " + entry.getKey());
            }
            if (!(entry.getValue() instanceof Integer)) {
                throw new ClassCastException("Value is not an Integer for key: " + entry.getKey()
                        + ", found: " + entry.getValue().getClass().getName());
            }
            intMap.put((String) entry.getKey(), (Integer) entry.getValue());
        }
        return intMap;
    }

}
