package com.example.localens.analysis.controller;

import com.example.localens.analysis.domain.Pair;
import com.example.localens.analysis.dto.*;
import com.example.localens.analysis.service.*;
import com.example.localens.analysis.util.RadarUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RadarController {

    private final RadarAnalysisService radarAnalysisService;
    private final RadarComparisonService radarComparisonService;

    @GetMapping("/main/{districtUuid}")
    public ResponseEntity<Map<String, Object>> getOverallData(@PathVariable Integer districtUuid) {
        Map<String, Object> radarData = radarAnalysisService.getRadarData(districtUuid);

        return ResponseEntity.ok(radarData);
    }


    @GetMapping("/compare/{districtUuid1}/{districtUuid2}")
    public ResponseEntity<Map<String, Object>> compareDistricts(
            @PathVariable Integer districtUuid1,
            @PathVariable Integer districtUuid2
    ) {
        Map<String, Object> comparisonResult = radarComparisonService.compareTwoDistricts(districtUuid1, districtUuid2);

        return ResponseEntity.ok(comparisonResult);
    }


//    @GetMapping("/{districtUuid}")
//    public ResponseEntity<RadarTopTwoResponse> getOverallData(@PathVariable Integer districtUuid) {
//        // 각 서비스 호출 결과를 변수에 저장
//        RadarFloatingPopulationResponse floatingPopulation = radarFloatingPopulationService.getNormalizedFloatingPopulation(districtUuid);
//        RadarStayVisitRatioResponse stayVisitRatio = radarStayVisitRatioService.getStayVisitRatioByDistrictUuid(districtUuid);
//        RadarCongestionRateResponse congestionRate = radarCongestionRateService.getCongestionRateByDistrictUuid(districtUuid);
//        RadarStayPerVisitorResponse stayPerVisitor = radarStayPerVisitorService.getStayPerVisitorByDistrictUuid(districtUuid);
//        RadarStayDurationChangeResponse stayDurationChange = radarStayDurationChangeService.calculateAvgStayTimeChangeRate(districtUuid);
//
//        // 결과 리스트에 추가
//        List<Object> overallData = new ArrayList<>();
//        overallData.add(floatingPopulation);
//        overallData.add(stayVisitRatio);
//        overallData.add(congestionRate);
//        overallData.add(stayPerVisitor);
//        overallData.add(stayDurationChange);
//
//        // 상위 두 항목 찾기
//        String[] topTwo = RadarUtils.findTopTwo(overallData);
//
//        // 결과를 DTO로 반환
//        RadarTopTwoResponse response = new RadarTopTwoResponse(overallData, topTwo);
//
//        return ResponseEntity.ok(response);
//    }


//    @GetMapping("/floating-population/{districtUuid}")
//    public RadarFloatingPopulationResponse getFloatingPopulation(@PathVariable Integer districtUuid) {
//        return radarFloatingPopulationService.getNormalizedFloatingPopulation(districtUuid);
//    }
//
//    @GetMapping("/stay-visit-ratio/{districtUuid}")
//    public RadarStayVisitRatioResponse getStayVisitRatio(@PathVariable Integer districtUuid) {
//        return radarStayVisitRatioService.getStayVisitRatioByDistrictUuid(districtUuid);
//    }
//
//    @GetMapping("/congestion-rate/{districtUuid}")
//    public RadarCongestionRateResponse getCongestionRate(@PathVariable Integer districtUuid) {
//        return radarCongestionRateService.getCongestionRateByDistrictUuid(districtUuid);
//    }
//
//    @GetMapping("/stay-per-visitor/{districtUuid}")
//    public RadarStayPerVisitorResponse getStayPerVisitor(@PathVariable Integer districtUuid) {
//        return radarStayPerVisitorService.getStayPerVisitorByDistrictUuid(districtUuid);
//    }
//
//    @GetMapping("/stay-duration-change/{districtUuid}")
//    public RadarStayDurationChangeResponse getAvgStayDurationChange(@PathVariable Integer districtUuid) {
//        return radarStayDurationChangeService.calculateAvgStayTimeChangeRate(districtUuid);
//    }
}
