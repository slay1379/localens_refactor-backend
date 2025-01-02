////package com.example.localens.analysis.controller;
////
////import com.example.localens.analysis.service.*;
////import lombok.RequiredArgsConstructor;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////
////import java.util.LinkedHashMap;
////import java.util.Map;
////
////@RestController
////@RequestMapping("/api/datecompare")
////@RequiredArgsConstructor
////public class DateController {
////
////    private final DateAnalysisService dateAnalysisService;
////
////    @GetMapping("/{districtUuid}")
////    public ResponseEntity<Map<String, Object>> getPopulationResponse(
////            @PathVariable Integer districtUuid,
////            @RequestParam String date1,
////            @RequestParam String date2
////    ) {
////        Map<String, Object> date1Result = dateAnalysisService.calculateDateData(districtUuid, date1);
////        Map<String, Object> date2Result = dateAnalysisService.calculateDateData(districtUuid, date2);
////
////        Map<String, Object> response = new LinkedHashMap<>();
////        response.put("date1", date1Result);
////        response.put("date2", date2Result);
////
////        return ResponseEntity.ok(response);
////    }
////}
//
//package com.example.localens.analysis.controller;
//
//import com.example.localens.analysis.service.*;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//@Slf4j
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
//        log.info("Received request for districtUuid: {}, date1: {}, date2: {}", districtUuid, date1, date2);
//
//        Map<String, Object> response = new LinkedHashMap<>();
//        try {
//            log.info("Calculating data for date1: {}", date1);
//            Map<String, Object> date1Result = dateAnalysisService.calculateDateData(districtUuid, date1);
//            log.info("Data for date1 calculated successfully: {}", date1Result);
//
//            log.info("Calculating data for date2: {}", date2);
//            Map<String, Object> date2Result = dateAnalysisService.calculateDateData(districtUuid, date2);
//            log.info("Data for date2 calculated successfully: {}", date2Result);
//
//            response.put("date1", date1Result);
//            response.put("date2", date2Result);
//
//            log.info("Response prepared successfully: {}", response);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            log.error("Error occurred while processing request for districtUuid: {}, date1: {}, date2: {}", districtUuid, date1, date2, e);
//            throw e; // Re-throw the exception to let Spring handle it.
//        }
//    }
//}
//

package com.example.localens.analysis.controller;

import com.example.localens.analysis.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/datecompare")
@RequiredArgsConstructor
public class DateController {

    private final DateAnalysisService dateAnalysisService;

    // 두 가지 날짜 형식을 모두 지원하는 DateTimeFormatter 목록
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"), // 두 자리 월/일
            DateTimeFormatter.ofPattern("yyyy년 M월 d일")   // 한 자리 월/일
    );

    /**
     * 한글 날짜를 LocalDateTime으로 변환하는 메서드
     */
    public LocalDateTime parseKoreanDate(String date) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                // T00:00:00 제거, 날짜만 파싱
                return LocalDateTime.of(
                        LocalDate.parse(date, formatter),
                        LocalTime.MIDNIGHT // 시간은 00:00:00으로 설정
                );
            } catch (Exception e) {
                // Ignore and try the next formatter
            }
        }
        throw new IllegalArgumentException("Invalid date format: " + date + ". Please use 'yyyy년 MM월 dd일' or 'yyyy년 M월 d일'.");
    }

    /**
     * 날짜 비교 API 엔드포인트
     */
    @GetMapping("/{districtUuid}")
    public ResponseEntity<Map<String, Object>> getPopulationResponse(
            @PathVariable Integer districtUuid,
            @RequestParam String date1,
            @RequestParam String date2
    ) {
        log.info("Received request for districtUuid: {}, date1: {}, date2: {}", districtUuid, date1, date2);

        try {
            // 한글 형식 날짜 파싱
            LocalDateTime parsedDate1 = parseKoreanDate(date1);
            LocalDateTime parsedDate2 = parseKoreanDate(date2);

            log.info("Parsed dates: date1={}, date2={}", parsedDate1, parsedDate2);

            // 서비스 호출
            Map<String, Object> date1Result = dateAnalysisService.calculateDateData(districtUuid, parsedDate1.toString());
            Map<String, Object> date2Result = dateAnalysisService.calculateDateData(districtUuid, parsedDate2.toString());

            // 응답 데이터 준비
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("date1", date1Result);
            response.put("date2", date2Result);

            log.info("Response prepared successfully: {}", response);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid date format: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error occurred while processing request for districtUuid: {}, date1: {}, date2: {}", districtUuid, date1, date2, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "An unexpected error occurred."));
        }
    }
}
