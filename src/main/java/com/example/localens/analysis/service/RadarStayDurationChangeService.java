package com.example.localens.analysis.service;

import com.example.localens.analysis.dto.RadarStayDurationChangeResponse;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RadarStayDurationChangeService {

    private final CommercialDistrictRepository districtRepository; // MySQL 연결
    private final InfluxDBClientWrapper influxDBClientWrapper; // InfluxDB 연결

    public RadarStayDurationChangeResponse calculateAvgStayTimeChangeRate(Integer districtUuid) {
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

        // Step 3: InfluxDB에서 데이터 조회
        List<FluxTable> queryResult = influxDBClientWrapper.query(fluxQuery);

        // Step 4: 시간대별 변화율 계산
        Map<String, Double> timeZoneRatios = calculateStayDurationChange(queryResult);

        // Step 5: 24개 변화율의 평균 계산
        double averageChangeRate = calculateAverageValue(timeZoneRatios);

        // Step 6: 최대최소 정규화
        double normalizedValue = normalize(averageChangeRate, 0.13917, 0.45167);

        // Step 7: 소수점 둘째 자리까지 반올림
        double formattedValue = Math.round(normalizedValue * 100.0) / 100.0;

        return new RadarStayDurationChangeResponse(formattedValue);
    }

    private Map<String, Double> calculateStayDurationChange(List<FluxTable> queryResult) {
        Map<String, Double> timeZoneData = new LinkedHashMap<>();
        Map<String, Double> timeZoneRatios = new LinkedHashMap<>();
        String[] timeZones = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"};

        // 시간대별 데이터를 수집
        for (FluxTable table : queryResult) {
            for (FluxRecord record : table.getRecords()) {
                String timeZone = record.getValueByKey("tmzn").toString(); // 시간대
                Double currentValue = Double.valueOf(record.getValueByKey("_value").toString()); // 값
                timeZoneData.put(timeZone, currentValue);
            }
        }

        // 시간대별 변화율 계산
        for (int i = 0; i < timeZones.length; i++) {
            String currentTimeZone = timeZones[i];
            String previousTimeZone = timeZones[(i == 0) ? 23 : (i - 1)];

            Double currentValue = timeZoneData.getOrDefault(currentTimeZone, 0.0);
            Double previousValue = timeZoneData.getOrDefault(previousTimeZone, 0.0);

            if (previousValue != 0) {
                double changeRate = (currentValue - previousValue) / previousValue;
                timeZoneRatios.put(currentTimeZone + "시", Math.round(changeRate * 100.0) / 100.0);
            } else {
                timeZoneRatios.put(currentTimeZone + "시", 0.0);
            }
        }

        return timeZoneRatios;
    }

    private double calculateAverageValue(Map<String, Double> timeZoneRatios) {
        double sum = 0.0;
        for (Double value : timeZoneRatios.values()) {
            sum += value;
        }
        return sum / timeZoneRatios.size();
    }

    private double normalize(double value, double min, double max) {
        return (value - min) / (max - min) * (1 - 0.1) + 0.1;
    }
}
