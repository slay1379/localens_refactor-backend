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

    private final RadarFloatingPopulationService radarFloatingPopulationService;
    private final RadarStayVisitRatioService radarStayVisitRatioService;
    private final RadarCongestionRateService radarCongestionRateService;
    private final RadarStayPerVisitorService radarStayPerVisitorService;
    private final RadarStayDurationChangeService radarStayDurationChangeService;
    private final RadarInfoService radarInfoService;
    private final RadarComparisonService radarComparisonService;
    private final RadarVisitConcentrationService radarVisitConcentrationService;

    @GetMapping("/main/{districtUuid}")
    public ResponseEntity<Map<String, Object>> getOverallData(@PathVariable Integer districtUuid) {
        // 서비스에서 상권 정보 조회
        var commercialDistrict = radarInfoService.getCommercialDistrictByUuid(districtUuid);

        // 각 서비스 호출 결과를 변수에 저장
        RadarFloatingPopulationResponse floatingPopulation = radarFloatingPopulationService.getNormalizedFloatingPopulation(districtUuid);
        RadarStayVisitRatioResponse stayVisitRatio = radarStayVisitRatioService.getStayVisitRatioByDistrictUuid(districtUuid);
        RadarCongestionRateResponse congestionRate = radarCongestionRateService.getCongestionRateByDistrictUuid(districtUuid);
        RadarStayPerVisitorResponse stayPerVisitor = radarStayPerVisitorService.getStayPerVisitorByDistrictUuid(districtUuid);
        RadarVisitConcentrationResponse visitConcentration = radarVisitConcentrationService.getVisitConcentrationByDistrictUuid(districtUuid);
        RadarStayDurationChangeResponse stayDurationChange = radarStayDurationChangeService.calculateAvgStayTimeChangeRate(districtUuid);


        // 데이터 리스트 생성
        List<Object> overallData = List.of(floatingPopulation, stayVisitRatio, congestionRate, stayPerVisitor, visitConcentration, stayDurationChange);

        // 가장 큰 두 값을 찾기
        List<Pair<String, Double>> topTwoPairs = RadarUtils.findTopTwo(overallData);

        // 결과 데이터 준비
        Map<String, Object> overallDataMap = new LinkedHashMap<>();
        overallDataMap.put("population", (int)(floatingPopulation.get유동인구_수() * 100));
        overallDataMap.put("stayVisit", (int)(stayVisitRatio.get체류_방문_비율() * 100));
        overallDataMap.put("congestion", (int)(congestionRate.get혼잡도_변화율() * 100));
        overallDataMap.put("stayPerVisitor", (int)(stayPerVisitor.get체류시간_대비_방문자_수() * 100));
        overallDataMap.put("visitConcentration", (int)(visitConcentration.get방문_집중도() * 100));
        overallDataMap.put("stayTimeChange", (int)(stayDurationChange.get평균_체류시간_변화율() * 100));

        // Top 2 데이터 준비
        Map<String, Object> topTwo = new LinkedHashMap<>();
        topTwo.put("first", Map.of("name", topTwoPairs.get(0).getKey(), "value", (int) (topTwoPairs.get(0).getValue() * 100)));
        topTwo.put("second", Map.of("name", topTwoPairs.get(1).getKey(), "value", (int) (topTwoPairs.get(1).getValue() * 100)));

        // 상권 정보 준비
        Map<String, Object> districtInfo = new LinkedHashMap<>();
        districtInfo.put("districtName", commercialDistrict.getDistrictName());
        districtInfo.put("clusterName", commercialDistrict.getCluster().getClusterName());
        districtInfo.put("latitude", commercialDistrict.getLatitude());
        districtInfo.put("longitude", commercialDistrict.getLongitude());

        // 최종 응답 데이터
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("districtInfo", districtInfo);
        response.put("overallData", overallDataMap);
        response.put("topTwo", topTwo);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/compare/{districtUuid1}/{districtUuid2}")
    public ResponseEntity<Map<String, Object>> compareDistricts(
            @PathVariable Integer districtUuid1,
            @PathVariable Integer districtUuid2
    ) {
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

        Map<String, Object> district1Info = (Map<String, Object>) district1Data.get("districtInfo");
        district1Info.remove("latitude");
        district1Info.remove("longitude");

        Map<String, Object> district2Info = (Map<String, Object>) district2Data.get("districtInfo");
        district2Info.remove("latitude");
        district2Info.remove("longitude");

        // 두 상권의 overallData 추출
        Map<String, Integer> district1Overall = (Map<String, Integer>) district1Data.get("overallData");
        Map<String, Integer> district2Overall = (Map<String, Integer>) district2Data.get("overallData");

        // RadarComparisonService를 사용하여 차이가 큰 두 항목 찾기
        Map<String, String> topDifferences = radarComparisonService.findTopDifferences(district1Overall, district2Overall);

        // 결과 반환
        Map<String, Object> comparisonResult = new LinkedHashMap<>();
        comparisonResult.put("district1", district1Data);
        comparisonResult.put("district2", district2Data);
        comparisonResult.put("largestDifferences", topDifferences);

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
