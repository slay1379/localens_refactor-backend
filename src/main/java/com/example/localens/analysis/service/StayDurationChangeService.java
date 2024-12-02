package com.example.localens.analysis.service;

import com.example.localens.analysis.dto.StayDurationChangeResponse;
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
public class StayDurationChangeService {

    private final CommercialDistrictRepository districtRepository; // MySQL 연결
    private final InfluxDBClientWrapper influxDBClientWrapper; // InfluxDB 연결

    // 숫자와 단어 매핑
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

    private final Map<String, String> wordToNumberMap = numberToWordMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    public StayDurationChangeResponse calculateAvgStayTimeChangeRate(Integer districtUuid) {
        // Step 1: MySQL에서 상권 이름 조회
        String districtName = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (districtName == null) {
            throw new IllegalArgumentException("유효하지 않은 상권 UUID: " + districtUuid);
        }

        // Step 2: InfluxDB 쿼리 작성
        String fluxQuery = String.format(
                "from(bucket: \"visit_duration_bucket\") " +
                        "|> range(start: 2024-01-30T00:00:00Z, stop: now()) " +
                        "|> filter(fn: (r) => r[\"place\"] == \"%s\") " +
                        "|> keep(columns: [\"tmzn\", \"_value\"])", districtName
        );

        List<FluxTable> queryResult = influxDBClientWrapper.query(fluxQuery);

        Map<String, Double> changeRates = calculateCongestionRate(queryResult);

        return new StayDurationChangeResponse(changeRates);
    }

    private Map<String, Double> calculateCongestionRate(List<FluxTable> queryResult) {
        // Step 4.1: 시간대별 데이터 수집
        Map<String, Double> timeZoneData = new LinkedHashMap<>();

        for (FluxTable table : queryResult) {
            for (FluxRecord record : table.getRecords()) {
                String timeZone = record.getValueByKey("tmzn").toString(); // 시간대
                Double currentValue = Double.valueOf(record.getValueByKey("_value").toString()); // 현재 값

                // 숫자를 영어 단어로 변환
                String timeZoneInWords = numberToWordMap.getOrDefault(timeZone, timeZone);
                timeZoneData.put(timeZoneInWords, currentValue);
            }
        }

        Map<String, Double> timeZoneRatios = new LinkedHashMap<>();
        List<String> sortedTimeZones = timeZoneData.keySet().stream()
                .sorted(Comparator.comparingInt(timeZone -> Integer.parseInt(wordToNumberMap.get(timeZone))))
                .toList();

        for (int i = 0; i < sortedTimeZones.size(); i++) {
            String currentTimeZone = sortedTimeZones.get(i);
            String previousTimeZone = sortedTimeZones.get((i == 0) ? sortedTimeZones.size() - 1 : i - 1);

            Double currentValue = timeZoneData.getOrDefault(currentTimeZone, 0.0);
            Double previousValue = timeZoneData.getOrDefault(previousTimeZone, 0.0);

            if (previousValue != 0) {
                double congestionRate = ((currentValue - previousValue) / previousValue);
                timeZoneRatios.put(currentTimeZone, Math.round(congestionRate * 100.0) / 100.0);
            } else {
                timeZoneRatios.put(currentTimeZone, 0.0);
            }
        }

        return timeZoneRatios;
    }
}
