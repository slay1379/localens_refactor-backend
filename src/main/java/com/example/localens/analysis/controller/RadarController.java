package com.example.localens.analysis.controller;

import com.example.localens.analysis.dto.FloatingPopulationResponse;
import com.example.localens.analysis.service.RadarFloatingPopulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/radar")
@RequiredArgsConstructor
public class RadarController {

    private final RadarFloatingPopulationService radarFloatingPopulationService;

    @GetMapping("/main/{districtUuid}")
    public FloatingPopulationResponse getFloatingPopulation(@PathVariable Integer districtUuid) {
        return radarFloatingPopulationService.getNormalizedFloatingPopulation(districtUuid);
    }
}
