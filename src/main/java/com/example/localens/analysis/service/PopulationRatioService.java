package com.example.localens.analysis.service;

import com.example.localens.analysis.dto.PopulationRatioResponse;
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
public class PopulationRatioService {

    private final CommercialDistrictRepository districtRepository;
    private final InfluxDBClientWrapper influxDBClientWrapper;

    public PopulationRatioResponse getPopulationRatioByDistrictUuid(Integer districtUuid) {
        // MySQL에서 상권 이름 조회
        String districtName = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (districtName == null) {
            throw new IllegalArgumentException("Invalid districtUuid: " + districtUuid);
        }

        // InfluxDB 쿼리 작성
        String fluxQuery = String.format(
                "from(bucket: \"result_bucket\") " +
                        "|> range(start: 2024-01-01T00:00:00Z, stop: now()) " +
                        "|> filter(fn: (r) => r[\"place\"] == \"%s\") " +
                        "|> keep(columns: [\"tmzn\", \"_value\"])", districtName
        );

        // InfluxDB에서 데이터 조회
        List<FluxTable> queryResult = influxDBClientWrapper.query(fluxQuery);

        // 시간대별 데이터 매핑
        Map<String, Double> timeZoneRatios = new LinkedHashMap<>();
        double[] totalPopulation = {0.0};

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
                Map.entry("21", "twentyOne"),
                Map.entry("22", "twentyTwo"),
                Map.entry("23", "twentyThree")
        );

        for (FluxTable table : queryResult) {
            for (FluxRecord record : table.getRecords()) {
                Object timeZoneObj = record.getValueByKey("tmzn");
                Object valueObj = record.getValueByKey("_value");

                if (timeZoneObj == null || valueObj == null) {
                    continue; // Null 값은 무시
                }

                String timeZone = timeZoneObj.toString();
                double value = Double.parseDouble(valueObj.toString());

                String timeZoneInWords = numberToWordMap.getOrDefault(timeZone, timeZone);

                timeZoneRatios.put(timeZoneInWords, value);
                totalPopulation[0] += value; // 총 유동인구 계산
            }
        }

        // 비율 계산
        Map<String, Double> timeZoneRatiosWithPercentage = timeZoneRatios.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> totalPopulation[0] != 0
                                ? Math.round((entry.getValue() / totalPopulation[0] * 100) * 10.0) / 10.0
                                : 0.0,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));


        // 시간대 정렬 (영어 단어를 숫자로 다시 매핑하여 정렬)
        Map<String, Double> sortedTimeZoneRatios = timeZoneRatiosWithPercentage.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> {
                    // 영어 단어를 숫자로 다시 매핑
                    String key = entry.getKey();
                    for (Map.Entry<String, String> mapping : numberToWordMap.entrySet()) {
                        if (mapping.getValue().equals(key)) {
                            return Integer.parseInt(mapping.getKey());
                        }
                    }
                    throw new IllegalArgumentException("Invalid timeZone word: " + key);
                }))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, // 충돌 처리: 이전 값 유지
                        LinkedHashMap::new // 정렬된 순서 유지
                ));

        return new PopulationRatioResponse(sortedTimeZoneRatios);
    }
}
