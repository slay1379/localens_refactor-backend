package com.example.localens.analysis.controller;

import com.example.localens.analysis.dto.*;
import com.example.localens.analysis.service.*;
import com.example.localens.analysis.util.RadarUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class RadarController {

    private final RadarFloatingPopulationService radarFloatingPopulationService;
    private final RadarStayVisitRatioService radarStayVisitRatioService;
    private final RadarCongestionRateService radarCongestionRateService;
    private final RadarStayPerVisitorService radarStayPerVisitorService;
    private final RadarStayDurationChangeService radarStayDurationChangeService;

    @GetMapping("/{districtUuid}")
    public ResponseEntity<RadarTopTwoResponse> getOverallData(@PathVariable Integer districtUuid) {
        // 각 서비스 호출 결과를 변수에 저장
        RadarFloatingPopulationResponse floatingPopulation = radarFloatingPopulationService.getNormalizedFloatingPopulation(districtUuid);
        RadarStayVisitRatioResponse stayVisitRatio = radarStayVisitRatioService.getStayVisitRatioByDistrictUuid(districtUuid);
        RadarCongestionRateResponse congestionRate = radarCongestionRateService.getCongestionRateByDistrictUuid(districtUuid);
        RadarStayPerVisitorResponse stayPerVisitor = radarStayPerVisitorService.getStayPerVisitorByDistrictUuid(districtUuid);
        RadarStayDurationChangeResponse stayDurationChange = radarStayDurationChangeService.calculateAvgStayTimeChangeRate(districtUuid);

        // 결과 데이터를 Map에 추가
        Map<String, Object> overallData = new HashMap<>();
        overallData.put("유동인구_수", floatingPopulation.get유동인구_수());
        overallData.put("체류_방문_비율", stayVisitRatio.get체류_방문_비율());
        overallData.put("혼잡도_변화율", congestionRate.get혼잡도_변화율());
        overallData.put("체류시간_대비_방문자_수", stayPerVisitor.get체류시간_대비_방문자_수());
        overallData.put("평균_체류시간_변화율", stayDurationChange.get평균_체류시간_변화율());

        // 상위 두 항목을 Map에 추가
        String[] topTwoArray = RadarUtils.findTopTwo(List.of(floatingPopulation, stayVisitRatio, congestionRate, stayPerVisitor, stayDurationChange));
        Map<String, Object> topTwo = new HashMap<>();
        topTwo.put("first", topTwoArray[0]);
        topTwo.put("second", topTwoArray[1]);

        // 결과를 DTO로 반환
        RadarTopTwoResponse response = new RadarTopTwoResponse(overallData, topTwo);

        return ResponseEntity.ok(response);
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
