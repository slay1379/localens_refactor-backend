package com.example.localens.analysis.controller;

import com.example.localens.analysis.dto.*;
import com.example.localens.analysis.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/population")
@RequiredArgsConstructor
public class PopulationController {

    private final PopulationRatioService populationRatioService;
    private final StayVisitRatioService stayVisitRatioService;
    private final CongestionRateService congestionRateService;
    private final StayPerVisitorService stayPerVisitorService;
    private final StayDurationChangeService stayDurationChangeService;
    private final AgeGenderRatioService ageGenderRatioService;
    private final NationalityRatioService nationalityRatioService;

    @GetMapping("/all-ratios/{districtUuid}")
    public ResponseEntity<List<Object>> getAllRatiosByDistrictUuid(@PathVariable Integer districtUuid) {
        List<Object> results = new ArrayList<>();

        // 각 서비스 호출 결과를 리스트에 추가
        results.add(populationRatioService.getPopulationRatioByDistrictUuid(districtUuid));
        results.add(stayVisitRatioService.getStayVisitRatioByDistrictUuid(districtUuid));
        results.add(congestionRateService.getCongestionRateByDistrictUuid(districtUuid));
        results.add(stayPerVisitorService.getStayPopulationRatioByDistrictUuid(districtUuid));
        results.add(stayDurationChangeService.calculateAvgStayTimeChangeRate(districtUuid));
        results.add(ageGenderRatioService.getAgeGenderPopulationRatio(districtUuid));
        results.add(nationalityRatioService.getNationalityPopulationRatio(districtUuid));

        return ResponseEntity.ok(results);
    }

//    @GetMapping("/population-ratio/{districtUuid}")
//    public ResponseEntity<PopulationRatioResponse> getPopulationRatioByDistrictUuid(
//            @PathVariable Integer districtUuid) {
//        PopulationRatioResponse result = populationRatioService.getPopulationRatioByDistrictUuid(districtUuid);
//        return ResponseEntity.ok(result);
//    }
//
//    @GetMapping("/stay-visit-ratio/{districtUuid}")
//    public ResponseEntity<StayVisitRatioResponse> getStayVisitRatioByDistrictUuid(
//            @PathVariable Integer districtUuid) {
//        StayVisitRatioResponse result = stayVisitRatioService.getStayVisitRatioByDistrictUuid(districtUuid);
//        return ResponseEntity.ok(result);
//    }
//
//    @GetMapping("/congestion-rate/{districtUuid}")
//    public ResponseEntity<CongestionRateResponse> getCongestionRateByDistrictUuid(
//            @PathVariable Integer districtUuid) {
//        CongestionRateResponse result = congestionRateService.getCongestionRateByDistrictUuid(districtUuid);
//        return ResponseEntity.ok(result);
//    }
//
//    @GetMapping("/stay-population-ratio/{districtUuid}")
//    public ResponseEntity<StayPerVisitorResponse> getStayPopulationRatioByDistrictUuid(
//            @PathVariable Integer districtUuid) {
//        StayPerVisitorResponse result = stayPerVisitorService.getStayPopulationRatioByDistrictUuid(districtUuid);
//        return ResponseEntity.ok(result);
//    }
//
//    @GetMapping("/stay-duration-rate/{districtUuid}")
//    public ResponseEntity<StayDurationChangeResponse> getAvgStayTimeChangeRate(
//            @PathVariable Integer districtUuid) {
//        StayDurationChangeResponse result = stayDurationChangeService.calculateAvgStayTimeChangeRate(districtUuid);
//        return ResponseEntity.ok(result);
//    }
//
//    @GetMapping("/age-gender-ratio/{districtUuid}")
//    public ResponseEntity<AgeGenderRatioResponse> getAgeGenderPopulationRatio(
//            @PathVariable Integer districtUuid) {
//        AgeGenderRatioResponse result = ageGenderRatioService.getAgeGenderPopulationRatio(districtUuid);
//        return ResponseEntity.ok(result);
//    }
//
//    @GetMapping("/nationality-ratio/{districtUuid}")
//    public ResponseEntity<NationalityRatioResponse> getNationalityRatioByDistrictUuid(
//            @PathVariable Integer districtUuid) {
//        NationalityRatioResponse result = nationalityRatioService.getNationalityPopulationRatio(districtUuid);
//        return ResponseEntity.ok(result);
//    }
}
