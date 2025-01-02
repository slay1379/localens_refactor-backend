package com.example.localens.improvement.controller;

import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.service.ImprovementService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/improvements")
public class ImprovementController {

    private final ImprovementService improvementService;

    @Autowired
    public ImprovementController(ImprovementService improvementService) {
        this.improvementService = improvementService;
    }

    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> recommendEventsWithMetrics(
            @RequestParam String districtUuid,
            @RequestParam String targetClusterUuid) {
        Map<String, Object> response = improvementService.recommendEventsWithDistrictMetrics(districtUuid,
                targetClusterUuid);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
