package com.example.localens.analysis.controller;

import com.example.localens.analysis.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PopulationController {

    private final PopulationDetailsService populationDetailsService;

    @GetMapping("/details/{districtUuid}")
    public ResponseEntity<Map<String, Object>> getAllRatiosByDistrictUuid(@PathVariable Integer districtUuid) {
        Map<String, Object> results = populationDetailsService.getDetailsByDistrictUuid(districtUuid);

        return ResponseEntity.ok(results);
    }
}
