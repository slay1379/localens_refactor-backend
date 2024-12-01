package com.example.localens.analysis.controller;

import com.example.localens.analysis.dto.DatePopulationResponse;
import com.example.localens.analysis.service.DatePopulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/datecompare")
@RequiredArgsConstructor
public class DateController {

    private final DatePopulationService datepopulationService;


    @GetMapping("/population/{districtUuid}")
    public ResponseEntity<DatePopulationResponse> getPopulationResponse(
            @PathVariable Integer districtUuid,
            @RequestParam String date
    ) {
        log.info("Request received for districtUuid: {}, date: {}", districtUuid, date);
        // Service 호출
        int normalizedValue = datepopulationService.getNormalizedPopulationValue(districtUuid, date);

        // 결과 반환
        DatePopulationResponse response = new DatePopulationResponse(normalizedValue);
        return ResponseEntity.ok(response);
    }
}
