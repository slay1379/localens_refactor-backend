package com.example.localens.analysis.service;

import com.example.localens.analysis.dto.StayDurationChangeResponse;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class StayDurationChangeService {

    private final CommercialDistrictRepository districtRepository; // MySQL 연결
    private final InfluxDBClientWrapper influxDBClientWrapper; // InfluxDB 연결

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

        // Debug: print the number of results
        System.out.println("InfluxDB query result size: " + queryResult.size());

        Map<String, Double> changeRates = calculateCongestionRate(queryResult);

        System.out.println("Calculated congestion rate: " + changeRates);
        return new StayDurationChangeResponse(changeRates);
    }

    // 혼잡도 변화율 계산
    private Map<String, Double> calculateCongestionRate(List<FluxTable> queryResult) {
        // Step 4.1: 시간대별 데이터 수집
        Map<String, Double> timeZoneData = new LinkedHashMap<>();

        // 먼저 tmzn을 기준으로 데이터를 합산합니다.
        for (FluxTable table : queryResult) {
            for (FluxRecord record : table.getRecords()) {
                String timeZone = record.getValueByKey("tmzn").toString(); // 시간대
                Double currentValue = Double.valueOf(record.getValueByKey("_value").toString()); // 현재 값

                // 각 시간대별로 _value를 합산
                timeZoneData.put(timeZone, currentValue);
            }
        }

        // Debug: print timeZoneData after summing values
        System.out.println("Summed timeZoneData: " + timeZoneData);

        // Step 4.2: 체류시간 변화율 계산
        Map<String, Double> timeZoneRatios = new LinkedHashMap<>();
        String[] timeZones = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"};

        for (int i = 0; i < timeZones.length; i++) {
            String currentTimeZone = timeZones[i];
            String previousTimeZone = timeZones[(i == 0) ? 23 : (i - 1)];

            // 현재 시간대와 이전 시간대의 _value 차이를 계산
            Double currentValue = timeZoneData.getOrDefault(currentTimeZone, 0.0);
            System.out.println("currentValue: " + currentValue);
            Double previousValue = timeZoneData.getOrDefault(previousTimeZone, 0.0);
            System.out.println("previousValue: " + previousValue);

            // 혼잡도 변화율 계산 (변화율 = (current - previous) / previous * 100)
            if (previousValue != 0) {
                double congestionRate = ((currentValue - previousValue) / previousValue);
                timeZoneRatios.put(currentTimeZone + "시", Math.round(congestionRate * 100.0) / 100.0); // 소수점 첫째자리로 반올림
            } else {
                timeZoneRatios.put(currentTimeZone + "시", 0.0); // 이전 값이 없을 경우 변화율 0으로 설정
            }
        }

        // Debug: print the final timeZoneRatios
        System.out.println("Final timeZoneRatios: " + timeZoneRatios);

        return timeZoneRatios;
    }
}