package com.example.localens.analysis.service;

import com.example.localens.analysis.dto.StayPerVisitorResponse;
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
public class StayPerVisitorService {

    private final CommercialDistrictRepository districtRepository; // MySQL 연결
    private final InfluxDBClientWrapper influxDBClientWrapper; // InfluxDB 연결

    // 시간대별 체류시간 대비 방문자수 비율을 구하는 메서드
    public StayPerVisitorResponse getStayPopulationRatioByDistrictUuid(Integer districtUuid) {
        // Step 1: MySQL에서 상권 이름 조회
        String districtName = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (districtName == null) {
            throw new IllegalArgumentException("유효하지 않은 상권 UUID: " + districtUuid);
        }

        // Step 2: InfluxDB 쿼리 작성 (stay_per_visitor_bucket 사용)
        String fluxQuery = String.format(
                "from(bucket: \"stay_per_visitor_bucket\") " +
                        "|> range(start: 2024-01-01T00:00:00Z, stop: now()) " +
                        "|> filter(fn: (r) => r[\"place\"] == \"%s\") " +
                        "|> keep(columns: [\"tmzn\", \"_value\"])", districtName
        );

        // Step 3: InfluxDB에서 데이터 조회
        List<FluxTable> queryResult = influxDBClientWrapper.query(fluxQuery);

        // Step 4: 체류시간 대비 방문자수 비율 계산
        Map<String, Double> timeZoneRatios = processAndSortTimeZoneRatios(queryResult);


        // 결과 반환
        return new StayPerVisitorResponse(timeZoneRatios);
    }

    private Map<String, Double> processAndSortTimeZoneRatios(List<FluxTable> queryResult) {
        Map<String, Double> timeZoneRatios = new LinkedHashMap<>();

        // 데이터 변환
        for (FluxTable table : queryResult) {
            for (FluxRecord record : table.getRecords()) {
                String timeZone = record.getValueByKey("tmzn").toString(); // 시간대
                Double value = Double.valueOf(record.getValueByKey("_value").toString()); // 비율 값

                // 시간대에 '시' 추가 및 소수점 넷째자리까지 반올림
                timeZoneRatios.put(timeZone, Math.round(value * 100.0) / 100.0);
            }
        }

        // 시간대 정렬
        return timeZoneRatios.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> Integer.parseInt(entry.getKey().replace("시", "")))) // 숫자 기준 정렬
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new // 정렬된 순서 유지
                ));
    }
}
