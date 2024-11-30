package com.example.localens.analysis.controller;

import com.example.localens.analysis.dto.*;
import com.example.localens.analysis.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/main")
@RequiredArgsConstructor
public class RadarController {

    private final RadarFloatingPopulationService radarFloatingPopulationService;
    private final RadarStayVisitRatioService radarStayVisitRatioService;
    private final RadarCongestionRateService radarCongestionRateService;
    private final RadarStayPerVisitorService radarStayPerVisitorService;
    private final RadarStayDurationChangeService radarStayDurationChangeService;

    @GetMapping("/floating-population/{districtUuid}")
    public RadarFloatingPopulationResponse getFloatingPopulation(@PathVariable Integer districtUuid) {
        return radarFloatingPopulationService.getNormalizedFloatingPopulation(districtUuid);
    }

    @GetMapping("/stay-visit-ratio/{districtUuid}")
    public RadarStayVisitRatioResponse getStayVisitRatio(@PathVariable Integer districtUuid) {
        return radarStayVisitRatioService.getStayVisitRatioByDistrictUuid(districtUuid);
    }

    @GetMapping("/congestion-rate/{districtUuid}")
    public RadarCongestionRateResponse getCongestionRate(@PathVariable Integer districtUuid) {
        return radarCongestionRateService.getCongestionRateByDistrictUuid(districtUuid);
    }

    @GetMapping("/stay-per-visitor/{districtUuid}")
    public RadarStayPerVisitorResponse getStayPerVisitor(@PathVariable Integer districtUuid) {
        return radarStayPerVisitorService.getStayPerVisitorByDistrictUuid(districtUuid);
    }

    @GetMapping("/stay-duration-change/{districtUuid}")
    public RadarStayDurationChangeResponse getAvgStayDurationChange(@PathVariable Integer districtUuid) {
        return radarStayDurationChangeService.calculateAvgStayTimeChangeRate(districtUuid);
    }
}
