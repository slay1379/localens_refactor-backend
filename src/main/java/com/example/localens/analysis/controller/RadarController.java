package com.example.localens.analysis.controller;

import com.example.localens.analysis.dto.RadarFloatingPopulationResponse;
import com.example.localens.analysis.dto.RadarStayVisitRatioResponse;
import com.example.localens.analysis.service.RadarFloatingPopulationService;
import com.example.localens.analysis.service.RadarStayVisitRatioService;
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

    @GetMapping("/floating-population/{districtUuid}")
    public RadarFloatingPopulationResponse getFloatingPopulation(@PathVariable Integer districtUuid) {
        return radarFloatingPopulationService.getNormalizedFloatingPopulation(districtUuid);
    }

    @GetMapping("/stay-visit-ratio/{districtUuid}")
    public RadarStayVisitRatioResponse getStayVisitRatio(@PathVariable Integer districtUuid) {
        return radarStayVisitRatioService.getStayVisitRatioByDistrictUuid(districtUuid);
    }
}
