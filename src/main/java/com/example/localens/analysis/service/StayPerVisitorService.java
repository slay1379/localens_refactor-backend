package com.example.localens.analysis.service;

import com.example.localens.analysis.dto.TimeZonePopulationRatioResponse;
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
    public TimeZonePopulationRatioResponse getStayPopulationRatioByDistrictUuid(Integer districtUuid) {
        // Step 1: MySQL에서 상권 이름 조회
        String districtName = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (districtName == null) {
            throw new IllegalArgumentException("유효하지 않은 상권 UUID: " + districtUuid);
        }

        // Step 2: InfluxDB 쿼리 작성 (stay_per_visitor_bucket 사용)
        String fluxQuery = String.format(
                "from(bucket: \"stay_per_visitor_bucket\") " +
                        "|> range(start: 2024-01-01T00:00:00Z, stop: now()) " +
                        "|> filter(fn: (r) => r._measurement == \"stay_analysis\" and r[\"place\"] == \"%s\") " +
                        "|> keep(columns: [\"tmzn\", \"_value\"])", districtName
        );

        // Step 3: InfluxDB에서 데이터 조회
        List<FluxTable> queryResult = influxDBClientWrapper.query(fluxQuery);

        // Step 4: 체류시간 대비 방문자수 비율 계산
        Map<String, Double> timeZoneRatios = calculateStayPopulationRatio(queryResult);

        // Step 5: 시간대별 정렬
        Map<String, Double> sortedTimeZoneRatios = timeZoneRatios.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> Integer.parseInt(entry.getKey().replace("시", "")))) // 숫자 기준 정렬
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, // 충돌 처리: 이전 값 유지
                        LinkedHashMap::new // 정렬된 순서 유지
                ));

        // 결과 반환
        return new TimeZonePopulationRatioResponse(sortedTimeZoneRatios);
    }

    // 체류시간 대비 방문자수 비율을 계산하는 메서드
    private Map<String, Double> calculateStayPopulationRatio(List<FluxTable> queryResult) {
        // 시간대별 데이터 수집
        Map<String, Double> timeZoneData = new LinkedHashMap<>();
        double totalValue = 0.0;

        // 수집된 데이터에서 각 시간대별 _value를 누적
        for (FluxTable table : queryResult) {
            for (FluxRecord record : table.getRecords()) {
                String timeZone = record.getValueByKey("tmzn").toString(); // 시간대
                Double currentValue = Double.valueOf(record.getValueByKey("_value").toString()); // _value 값

                // 시간대별로 _value를 누적
                timeZoneData.put(timeZone, timeZoneData.getOrDefault(timeZone, 0.0) + currentValue);
                totalValue += currentValue; // 전체 _value 값 합산
            }
        }

        // 각 시간대별 체류시간 대비 방문자수 비율 계산
        Map<String, Double> timeZoneRatios = new LinkedHashMap<>();

        // _value가 0이 아니면 비율을 계산
        for (Map.Entry<String, Double> entry : timeZoneData.entrySet()) {
            String timeZone = entry.getKey();
            Double value = entry.getValue();

            // 백분율로 계산 (value / totalValue * 100)
            double ratio = totalValue != 0 ? (value / totalValue) * 100 : 0.0;

            timeZoneRatios.put(timeZone + "시", Math.round(ratio * 10.0) / 10.0); // 소수점 첫째자리로 반올림
        }

        return timeZoneRatios;
    }
}
