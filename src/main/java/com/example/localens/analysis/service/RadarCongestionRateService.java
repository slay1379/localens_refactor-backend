package com.example.localens.analysis.service;

import com.example.localens.analysis.dto.RadarCongestionRateResponse;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RadarCongestionRateService {

    private final CommercialDistrictRepository districtRepository; // MySQL 연결
    private final InfluxDBClientWrapper influxDBClientWrapper; // InfluxDB 연결

    public RadarCongestionRateResponse getCongestionRateByDistrictUuid(Integer districtUuid) {
        // Step 1: MySQL에서 상권 이름 조회
        String districtName = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (districtName == null) {
            throw new IllegalArgumentException("유효하지 않은 상권 UUID: " + districtUuid);
        }

        // Step 2: InfluxDB 쿼리 작성
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

        // Step 5: 혼잡도 변화율 평균 계산
        double averageCongestionRate = calculateAverageValue(timeZoneRatios);

        // Step 6: 최대최소 정규화
        double normalizedValue = normalize(averageCongestionRate, 0.55, 4.93);

        // Step 7: 소수점 둘째자리까지 반올림
        double formattedValue = Math.round(normalizedValue * 100.0) / 100.0;

        return new RadarCongestionRateResponse(formattedValue);
    }

    // 혼잡도 변화율 계산
    private Map<String, Double> calculateCongestionRate(List<FluxTable> queryResult) {
        Map<String, Double> timeZoneData = new LinkedHashMap<>();

        for (FluxTable table : queryResult) {
            for (FluxRecord record : table.getRecords()) {
                String timeZone = record.getValueByKey("tmzn").toString();
                Double currentValue = Double.valueOf(record.getValueByKey("_value").toString());
                timeZoneData.put(timeZone, currentValue);
            }
        }

        Map<String, Double> timeZoneRatios = new LinkedHashMap<>();
        String[] timeZones = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"};

        for (int i = 0; i < timeZones.length; i++) {
            String currentTimeZone = timeZones[i];
            String previousTimeZone = timeZones[(i == 0) ? 23 : (i - 1)];
            Double currentValue = timeZoneData.getOrDefault(currentTimeZone, 0.0);
            Double previousValue = timeZoneData.getOrDefault(previousTimeZone, 0.0);

            if (previousValue != 0) {
                double congestionRate = ((currentValue - previousValue) / previousValue) * 100;
                timeZoneRatios.put(currentTimeZone + "시", Math.round(congestionRate * 10.0) / 10.0);
            } else {
                timeZoneRatios.put(currentTimeZone + "시", 0.0);
            }
        }

        return timeZoneRatios;
    }

    // 혼잡도 변화율 평균 계산
    private double calculateAverageValue(Map<String, Double> timeZoneRatios) {
        double sum = timeZoneRatios.values().stream().mapToDouble(Double::doubleValue).sum();
        int count = timeZoneRatios.size();
        if (count == 0) {
            throw new IllegalArgumentException("No valid data found for congestion rate calculation.");
        }
        return sum / count;
    }

    // 최대최소 정규화
    private double normalize(double value, double min, double max) {
        return (value - min) / (max - min) * (1 - 0.1) + 0.1;
    }
}
