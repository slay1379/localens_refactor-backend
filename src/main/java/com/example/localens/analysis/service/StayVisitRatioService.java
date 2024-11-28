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
                        "|> filter(fn: (r) => r._measurement == \"stay_visit_ratio\" and r[\"place\"] == \"%s\") " +
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

        // Step 4.1: 데이터 변환
        for (FluxTable table : queryResult) {
            for (FluxRecord record : table.getRecords()) {
                String timeZone = record.getValueByKey("tmzn").toString(); // 시간대
                Double value = Double.valueOf(record.getValueByKey("_value").toString()); // 비율 값

                // 시간대에 '시' 추가
                timeZoneRatios.put(timeZone + "시", Math.round(value * 10.0) / 10.0);
            }
        }

        // Step 4.2: 시간대 정렬
        return timeZoneRatios.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> Integer.parseInt(entry.getKey().replace("시", "")))) // 숫자 기준 정렬
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
