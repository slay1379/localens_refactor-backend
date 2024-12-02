package com.example.localens.analysis.service;

import com.example.localens.analysis.dto.CongestionRateResponse;
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
public class CongestionRateService {

    private final CommercialDistrictRepository districtRepository; // MySQL 연결
    private final InfluxDBClientWrapper influxDBClientWrapper; // InfluxDB 연결

    // 숫자 → 영어 단어 매핑
    private final Map<String, String> numberToWordMap = Map.ofEntries(
            Map.entry("0", "zero"), Map.entry("1", "one"), Map.entry("2", "two"),
            Map.entry("3", "three"), Map.entry("4", "four"), Map.entry("5", "five"),
            Map.entry("6", "six"), Map.entry("7", "seven"), Map.entry("8", "eight"),
            Map.entry("9", "nine"), Map.entry("10", "ten"), Map.entry("11", "eleven"),
            Map.entry("12", "twelve"), Map.entry("13", "thirteen"), Map.entry("14", "fourteen"),
            Map.entry("15", "fifteen"), Map.entry("16", "sixteen"), Map.entry("17", "seventeen"),
            Map.entry("18", "eighteen"), Map.entry("19", "nineteen"), Map.entry("20", "twenty"),
            Map.entry("21", "twenty-one"), Map.entry("22", "twenty-two"), Map.entry("23", "twenty-three")
    );

    // 영어 단어 → 숫자 매핑
    private final Map<String, String> wordToNumberMap = numberToWordMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    // 혼잡도 변화율 API
    public CongestionRateResponse getCongestionRateByDistrictUuid(Integer districtUuid) {
        // Step 1: MySQL에서 상권 이름 조회
        String districtName = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (districtName == null) {
            throw new IllegalArgumentException("유효하지 않은 상권 UUID: " + districtUuid);
        }

        // Step 2: InfluxDB 쿼리 작성 (congestion_rate_bucket 사용)
        String fluxQuery = String.format(
                "from(bucket: \"result_bucket\") " +
                        "|> range(start: 2024-01-01T00:00:00Z, stop: now()) " +
                        "|> filter(fn: (r) => r[\"place\"] == \"%s\") " +
                        "|> keep(columns: [\"tmzn\", \"_value\"])", districtName
        );

        // Step 3: InfluxDB에서 데이터 조회
        List<FluxTable> queryResult = influxDBClientWrapper.query(fluxQuery);

        // Step 4: 혼잡도 변화율 계산
        Map<String, Double> timeZoneRatios = calculateCongestionRate(queryResult);

        // Step 5: 영어 단어로 변환된 키로 변경
        Map<String, Double> wordKeyRatios = timeZoneRatios.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> numberToWordMap.get(entry.getKey()), // 숫자 → 영어 단어
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        // Step 6: 다시 숫자로 매핑하여 정렬
        Map<String, Double> sortedRatios = wordKeyRatios.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> Integer.parseInt(wordToNumberMap.get(entry.getKey())))) // 영어 단어 → 숫자 변환 후 정렬
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        return new CongestionRateResponse(sortedRatios);
    }

    // 혼잡도 변화율 계산
    private Map<String, Double> calculateCongestionRate(List<FluxTable> queryResult) {
        Map<String, Double> timeZoneData = new LinkedHashMap<>();

        // 데이터 수집
        for (FluxTable table : queryResult) {
            for (FluxRecord record : table.getRecords()) {
                String timeZone = record.getValueByKey("tmzn").toString();
                Double currentValue = Double.valueOf(record.getValueByKey("_value").toString());
                timeZoneData.put(timeZone, currentValue);
            }
        }

        // 혼잡도 변화율 계산
        Map<String, Double> timeZoneRatios = new LinkedHashMap<>();
        String[] timeZones = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"};

        for (int i = 0; i < timeZones.length; i++) {
            String currentTimeZone = timeZones[i];
            String previousTimeZone = timeZones[(i == 0) ? 23 : (i - 1)];

            Double currentValue = timeZoneData.getOrDefault(currentTimeZone, 0.0);
            Double previousValue = timeZoneData.getOrDefault(previousTimeZone, 0.0);

            if (previousValue != 0) {
                double congestionRate = ((currentValue - previousValue) / previousValue) * 100;
                timeZoneRatios.put(currentTimeZone, Math.round(congestionRate * 10.0) / 10.0);
            } else {
                timeZoneRatios.put(currentTimeZone, 0.0);
            }
        }

        return timeZoneRatios;
    }
}



