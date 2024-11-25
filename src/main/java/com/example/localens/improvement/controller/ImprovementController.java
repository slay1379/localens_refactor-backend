package com.example.localens.improvement.controller;

import com.example.localens.improvement.domain.Event;
import com.example.localens.improvement.service.ImprovementService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<Event> recommendEvents(@RequestParam String districtUuid,
                                       @RequestParam String targetClusterUuid) {
        return improvementService.recommendEvents(districtUuid, targetClusterUuid);
    }
}
