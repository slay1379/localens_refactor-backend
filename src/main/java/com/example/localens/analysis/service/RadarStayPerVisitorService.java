package com.example.localens.analysis.service;

import com.example.localens.analysis.dto.RadarStayPerVisitorResponse;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RadarStayPerVisitorService {

    private final CommercialDistrictRepository districtRepository; // MySQL 연결
    private final InfluxDBClientWrapper influxDBClientWrapper; // InfluxDB 연결

    public RadarStayPerVisitorResponse getStayPerVisitorByDistrictUuid(Integer districtUuid) {
        // Step 1: MySQL에서 상권 이름 조회
        String districtName = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (districtName == null) {
            throw new IllegalArgumentException("유효하지 않은 상권 UUID: " + districtUuid);
        }

        // Step 2: InfluxDB 쿼리 작성
        String fluxQuery = String.format(
                "from(bucket: \"stay_per_visitor_bucket\") " +
                        "|> range(start: 2024-01-01T00:00:00Z, stop: now()) " +
                        "|> filter(fn: (r) => r[\"place\"] == \"%s\") " +
                        "|> keep(columns: [\"tmzn\", \"_value\"])", districtName
        );

        // Step 3: InfluxDB에서 데이터 조회
        List<FluxTable> queryResult = influxDBClientWrapper.query(fluxQuery);

        // Step 4: 데이터 평균 계산
        double averageValue = calculateAverageValue(queryResult);

        // Step 5: 최대최소 정규화
        double normalizedValue = normalize(averageValue, 0.06375, 0.96792);

        // Step 6: 소수점 둘째 자리까지 반올림
        double formattedValue = Math.round(normalizedValue * 100.0) / 100.0;

        return new RadarStayPerVisitorResponse(formattedValue);
    }

    private double calculateAverageValue(List<FluxTable> queryResult) {
        double sum = 0.0;
        int count = 0;

        for (FluxTable table : queryResult) {
            for (FluxRecord record : table.getRecords()) {
                Object valueObj = record.getValueByKey("_value");
                if (valueObj != null) {
                    sum += Double.parseDouble(valueObj.toString());
                    count++;
                }
            }
        }

        if (count == 0) {
            throw new IllegalArgumentException("No valid data found in query result.");
        }

        return sum / count;
    }

    private double normalize(double value, double min, double max) {
        return (value - min) / (max - min) * (1 - 0.1) + 0.1;
    }
}
