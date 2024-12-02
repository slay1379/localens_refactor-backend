package com.example.localens.analysis.service;

import com.example.localens.analysis.dto.StayVisitRatioResponse;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class StayVisitRatioService {

    private final CommercialDistrictRepository districtRepository; // MySQL 연결
    private final InfluxDBClientWrapper influxDBClientWrapper; // InfluxDB 연결

    public StayVisitRatioResponse getStayVisitRatioByDistrictUuid(Integer districtUuid) {
        // Step 1: MySQL에서 상권 이름 조회
        String districtName = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        System.out.println("Step 1: Fetched districtName from MySQL: " + districtName);

        if (districtName == null) {
            throw new IllegalArgumentException("유효하지 않은 상권 UUID: " + districtUuid);
        }

        // Step 2: InfluxDB 쿼리 작성
        String fluxQuery = String.format(
                "from(bucket: \"result_stay_visit_bucket\") " +
                        "|> range(start: 2024-01-01T00:00:00Z, stop: now()) " +
                        "|> filter(fn: (r) => r[\"place\"] == \"%s\") " +
                        "|> keep(columns: [\"tmzn\", \"_value\"])", districtName
        );
        System.out.println("Step 2: Generated Flux query: " + fluxQuery);

        // Step 3: InfluxDB에서 데이터 조회
        List<FluxTable> queryResult = influxDBClientWrapper.query(fluxQuery);
        System.out.println("Step 3: InfluxDB query result size: " + queryResult.size());

        // Step 4: 데이터 처리 및 정렬
        Map<String, Double> timeZoneRatios = processAndSortTimeZoneRatios(queryResult);
        System.out.println("Step 4: Processed timeZoneRatios: " + timeZoneRatios);

        return new StayVisitRatioResponse(timeZoneRatios);
    }

    // 시간대별 데이터 처리 및 정렬
    private Map<String, Double> processAndSortTimeZoneRatios(List<FluxTable> queryResult) {
        Map<String, Double> timeZoneRatios = new LinkedHashMap<>();

        // 숫자 -> 영어 단어 매핑
        Map<String, String> numberToWordMap = Map.ofEntries(
                Map.entry("0", "zero"),
                Map.entry("1", "one"),
                Map.entry("2", "two"),
                Map.entry("3", "three"),
                Map.entry("4", "four"),
                Map.entry("5", "five"),
                Map.entry("6", "six"),
                Map.entry("7", "seven"),
                Map.entry("8", "eight"),
                Map.entry("9", "nine"),
                Map.entry("10", "ten"),
                Map.entry("11", "eleven"),
                Map.entry("12", "twelve"),
                Map.entry("13", "thirteen"),
                Map.entry("14", "fourteen"),
                Map.entry("15", "fifteen"),
                Map.entry("16", "sixteen"),
                Map.entry("17", "seventeen"),
                Map.entry("18", "eighteen"),
                Map.entry("19", "nineteen"),
                Map.entry("20", "twenty"),
                Map.entry("21", "twenty-one"),
                Map.entry("22", "twenty-two"),
                Map.entry("23", "twenty-three")
        );

        // 영어 단어 -> 숫자 매핑 (역매핑 생성)
        Map<String, String> wordToNumberMap = numberToWordMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        // Step 4.1: 데이터 변환
        for (FluxTable table : queryResult) {
            for (FluxRecord record : table.getRecords()) {
                String timeZone = record.getValueByKey("tmzn").toString(); // 시간대
                Double value = Double.valueOf(record.getValueByKey("_value").toString()); // 비율 값

                // 숫자를 영어 단어로 변환
                String timeZoneInWords = numberToWordMap.getOrDefault(timeZone, timeZone);
                timeZoneRatios.put(timeZoneInWords, Math.round(value * 10.0) / 10.0);
            }
        }

        // Step 4.2: 시간대 정렬 (영어 단어를 숫자로 다시 매핑)
        return timeZoneRatios.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> {
                    // 영어 단어를 숫자로 다시 매핑
                    String word = entry.getKey();
                    String number = wordToNumberMap.getOrDefault(word, "-1"); // 매핑 실패 시 -1
                    return Integer.parseInt(number);
                }))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new // 정렬된 순서 유지
                ));
    }


//    public StayVisitRatioResponse getStayVisitRatioByDistrictUuid(Integer districtUuid) {
//        // Step 1: MySQL에서 상권 이름 조회
//        String districtName = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
//        System.out.println("Step 1: Fetched districtName from MySQL: " + districtName); // 로그 추가
//
//        if (districtName == null) {
//            throw new IllegalArgumentException("유효하지 않은 상권 UUID: " + districtUuid);
//        }
//
//        // Step 2: InfluxDB 쿼리 작성
//        String fluxQuery = String.format(
//                "from(bucket: \"result_stay_visit_bucket\") " +
//                        "|> range(start: 2024-01-01T00:00:00Z, stop: now()) " +
//                        "|> filter(fn: (r) => r._measurement == \"stay_visit_ratio\" and r[\"place\"] == \"%s\") " +
//                        "|> keep(columns: [\"tmzn\", \"_value\"])", districtName
//        );
//        System.out.println("Step 2: Generated Flux query: " + fluxQuery); // 로그 추가
//
//        // Step 3: InfluxDB에서 데이터 조회
//        List<FluxTable> queryResult = influxDBClientWrapper.query(fluxQuery);
//        System.out.println("Step 3: InfluxDB query result size: " + queryResult.size()); // 로그 추가
//
//        // Step 4: 데이터 처리 및 정렬
//        Map<String, Double> timeZoneRatios = processAndAggregateTimeZoneRatios(queryResult);
//        System.out.println("Step 4: Processed timeZoneRatios: " + timeZoneRatios); // 로그 추가
//
//        return new StayVisitRatioResponse(timeZoneRatios);
//    }
//
//    // 시간대별 데이터 처리
//    private Map<String, Double> processAndAggregateTimeZoneRatios(List<FluxTable> queryResult) {
//        Map<String, List<Double>> tempRatios = new HashMap<>();
//
//        // Step 3.1: 시간대별로 데이터를 그룹화
//        for (FluxTable table : queryResult) {
//            for (FluxRecord record : table.getRecords()) {
//                String timeZone = record.getValueByKey("tmzn").toString(); // 시간대
//                Double value = Double.valueOf(record.getValueByKey("_value").toString()); // 비율 값
//
//                tempRatios.computeIfAbsent(timeZone, k -> new ArrayList<>()).add(value);
//            }
//        }
//        System.out.println("Step 3.1: Grouped timeZone data: " + tempRatios); // 로그 추가
//
//        // Step 3.2: 시간대별 평균 계산 및 정렬
//        return tempRatios.entrySet().stream()
//                .collect(Collectors.toMap(
//                        entry -> entry.getKey() + "시", // "4" -> "4시"
//                        entry -> Math.round(
//                                entry.getValue().stream()
//                                        .mapToDouble(Double::doubleValue)
//                                        .average()
//                                        .orElse(0.0) * 10.0
//                        ) / 10.0, // 소수점 첫째 자리까지 반올림
//                        (oldValue, newValue) -> oldValue,
//                        LinkedHashMap::new // 순서 유지
//                ))
//                .entrySet()
//                .stream()
//                .sorted(Comparator.comparingInt(entry -> Integer.parseInt(entry.getKey().replace("시", "")))) // 숫자 기준 정렬
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        Map.Entry::getValue,
//                        (oldValue, newValue) -> oldValue,
//                        LinkedHashMap::new // 정렬된 순서 유지
//                ));
//    }
}
