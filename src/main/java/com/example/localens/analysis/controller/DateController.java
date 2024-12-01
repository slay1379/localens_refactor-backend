package com.example.localens.analysis.controller;

import com.example.localens.analysis.service.DateCongestionRateService;
import com.example.localens.analysis.service.DatePopulationService;
import com.example.localens.analysis.service.DateStayVisitService;
import com.example.localens.analysis.service.DateVisitConcentrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/datecompare")
@RequiredArgsConstructor
public class DateController {

    private final DatePopulationService datePopulationService;
    private final DateVisitConcentrationService dateVisitConcentrationService;
    private final DateStayVisitService dateStayVisitService;
    private final DateCongestionRateService dateCongestionRateService;

    @GetMapping("/population/{districtUuid}")
    public ResponseEntity<Map<String, Integer>> getPopulationResponse(
            @PathVariable Integer districtUuid,
            @RequestParam String date
    ) {
        int normalizedPopulationValue = datePopulationService.getNormalizedPopulationValue(districtUuid, date);
        int visitConcentrationValue = dateVisitConcentrationService.getNormalizedPopulationValue(districtUuid, date);
        int stayVisitRatioValue = dateStayVisitService.getNormalizedStayVisitRatio(districtUuid, date);
        int congestionRateValue = dateCongestionRateService.getNormalizedCongestionRate(districtUuid, date);

        Map<String, Integer> response = new LinkedHashMap<>();
        response.put("유동인구 수", normalizedPopulationValue);
        response.put("체류/방문 비율", stayVisitRatioValue);
        response.put("방문 집중도", visitConcentrationValue);
        response.put("혼잡도_변화율", congestionRateValue);

        return ResponseEntity.ok(response);
    }
}
