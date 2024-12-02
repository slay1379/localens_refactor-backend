//package com.example.localens.analysis.controller;
//
//import com.example.localens.analysis.service.*;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/datecompare")
//@RequiredArgsConstructor
//public class DateController {
//
//    private final DateAnalysisService dateAnalysisService;
//
//    @GetMapping("/{districtUuid}")
//    public ResponseEntity<Map<String, Object>> getPopulationResponse(
//            @PathVariable Integer districtUuid,
//            @RequestParam String date1,
//            @RequestParam String date2
//    ) {
//        Map<String, Object> date1Result = dateAnalysisService.calculateDateData(districtUuid, date1);
//        Map<String, Object> date2Result = dateAnalysisService.calculateDateData(districtUuid, date2);
//
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("date1", date1Result);
//        response.put("date2", date2Result);
//
//        return ResponseEntity.ok(response);
//    }
//}

package com.example.localens.analysis.controller;

import com.example.localens.analysis.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/datecompare")
@RequiredArgsConstructor
public class DateController {

    private final DateAnalysisService dateAnalysisService;

    @GetMapping("/{districtUuid}")
    public ResponseEntity<Map<String, Object>> getPopulationResponse(
            @PathVariable Integer districtUuid,
            @RequestParam String date1,
            @RequestParam String date2
    ) {
        log.info("Received request for districtUuid: {}, date1: {}, date2: {}", districtUuid, date1, date2);

        Map<String, Object> response = new LinkedHashMap<>();
        try {
            log.info("Calculating data for date1: {}", date1);
            Map<String, Object> date1Result = dateAnalysisService.calculateDateData(districtUuid, date1);
            log.info("Data for date1 calculated successfully: {}", date1Result);

            log.info("Calculating data for date2: {}", date2);
            Map<String, Object> date2Result = dateAnalysisService.calculateDateData(districtUuid, date2);
            log.info("Data for date2 calculated successfully: {}", date2Result);

            response.put("date1", date1Result);
            response.put("date2", date2Result);

            log.info("Response prepared successfully: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error occurred while processing request for districtUuid: {}, date1: {}, date2: {}", districtUuid, date1, date2, e);
            throw e; // Re-throw the exception to let Spring handle it.
        }
    }
}

