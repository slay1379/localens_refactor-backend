package com.example.localens.analysis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DateAnalysisService {

    private final DatePopulationService datePopulationService;
    private final DateVisitConcentrationService dateVisitConcentrationService;
    private final DateStayVisitService dateStayVisitService;
    private final DateCongestionRateService dateCongestionRateService;
    private final DateStayPerVisitorService dateStayPerVisitorService;
    private final DateStayDurationRateService dateStayDurationRateService;

    public Map<String, Object> calculateDateData(Integer districtUuid, String date) {
        int normalizedPopulationValue = datePopulationService.getNormalizedPopulationValue(districtUuid, date);
        int visitConcentrationValue = dateVisitConcentrationService.getNormalizedPopulationValue(districtUuid, date);
        int stayVisitRatioValue = dateStayVisitService.getNormalizedStayVisitRatio(districtUuid, date);
        int congestionRateValue = dateCongestionRateService.getNormalizedCongestionRate(districtUuid, date);
        int stayPerVisitorValue = dateStayPerVisitorService.getNormalizedStayPerVisitorValue(districtUuid, date);
        int stayDurationRateValue = dateStayDurationRateService.getNormalizedStayDurationRate(districtUuid, date);

        Map<String, Integer> values = new LinkedHashMap<>();
        values.put("population", normalizedPopulationValue);
        values.put("stayVisit", stayVisitRatioValue);
        values.put("visitConcentration", visitConcentrationValue);
        values.put("congestion", congestionRateValue);
        values.put("stayPerVisitor", stayPerVisitorValue);
        values.put("stayTimeChange", stayDurationRateValue);

        // 가장 큰 값과 두 번째로 큰 값 찾기
        List<Map.Entry<String, Integer>> sortedEntries = values.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        String firstMaxName = sortedEntries.get(0).getKey();
        String secondMaxName = sortedEntries.get(1).getKey();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("values", values);

        Map<String, String> topTwo = new LinkedHashMap<>();
        topTwo.put("first", firstMaxName);
        topTwo.put("second", secondMaxName);

        result.put("topTwo", topTwo);

        return result;
    }
}
