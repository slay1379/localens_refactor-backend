package com.example.localens.improvement.controller;

import com.example.localens.analysis.controller.DateController;
import com.example.localens.analysis.service.DateAnalysisService;
import com.example.localens.analysis.service.PopulationDetailsService;
import com.example.localens.analysis.service.RadarComparisonService;
import com.example.localens.analysis.service.RadarInfoService;
import com.example.localens.improvement.domain.CommercialDistrictComparisonDTO;
import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.repository.EventMetricsRepository;
import com.example.localens.improvement.repository.EventRepository;
import com.example.localens.improvement.repository.MetricRepository;
import com.example.localens.improvement.service.ImprovementService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/improvements")
@RequiredArgsConstructor
@Slf4j
public class ImprovementController {
    private final ImprovementService improvementService;

    @GetMapping("/recommendations/{districtUuid1}/{districtUuid2}")
    public ResponseEntity<CommercialDistrictComparisonDTO> compareDistricts(
            @PathVariable("districtUuid1") Integer districtUuid1,
            @PathVariable("districtUuid2") Integer districtUuid2) {
        CommercialDistrictComparisonDTO result = improvementService.compareDistricts(districtUuid1, districtUuid2);

        return ResponseEntity.ok(result);
    }
}
