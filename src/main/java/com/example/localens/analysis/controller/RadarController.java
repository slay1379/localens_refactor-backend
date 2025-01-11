package com.example.localens.analysis.controller;

import com.example.localens.analysis.service.*;
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
}
