package com.example.localens.analysis.service;

import com.example.localens.analysis.dto.NationalityRatioResponse;
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
public class NationalityRatioService {

    private final CommercialDistrictRepository districtRepository; // MySQL 연결
    private final InfluxDBClientWrapper influxDBClientWrapper; // InfluxDB 연결

    public NationalityRatioResponse getNationalityPopulationRatio(Integer districtUuid) {
        // Step 1: MySQL에서 상권 이름 조회
        String districtName = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (districtName == null) {
            throw new IllegalArgumentException("유효하지 않은 상권 UUID: " + districtUuid);
        }

        // Step 2: InfluxDB 쿼리 작성
        String fluxQuery = String.format(
                "from(bucket: \"nationality_bucket\") " +
                        "|> range(start: 2024-01-30T00:00:00Z, stop: now()) " +
                        "|> filter(fn: (r) => r[\"place\"] == \"%s\") " +
                        "|> keep(columns: [\"nationality\", \"_value\"])", districtName
        );

        // Step 3: InfluxDB에서 데이터 조회
        List<FluxTable> queryResult = influxDBClientWrapper.query(fluxQuery);

        // Step 4: 데이터 처리 및 정렬
        Map<String, Double> nationalityRatios = processNationalityRatios(queryResult);

        return new NationalityRatioResponse(nationalityRatios);
    }

    // 내외국인 비율 계산
    private Map<String, Double> processNationalityRatios(List<FluxTable> queryResult) {
        Map<String, String> nationalityMapping = Map.of(
                "내국인", "Local",
                "장기체류외국인", "Foreigner"
        );
        Map<String, Double> nationalityMap = new HashMap<>();

        // 데이터를 내국인, 외국인별로 분류
        for (FluxTable table : queryResult) {
            for (FluxRecord record : table.getRecords()) {
                String nationality = record.getValueByKey("nationality").toString(); // 내외국인 구분
                Double value = Double.valueOf(record.getValueByKey("_value").toString()); // 유동인구 값

                String translatedNationality = nationalityMapping.getOrDefault(nationality, nationality);
                nationalityMap.merge(translatedNationality, value, Double::sum); // 값 누적
            }
        }

        // 총합 계산
        double totalPopulation = nationalityMap.values().stream().mapToDouble(Double::doubleValue).sum();

        // 비율 계산
        nationalityMap.replaceAll((nationality, value) -> Math.round((value / totalPopulation) * 1000.0) / 10.0); // 소수점 첫째 자리 반올림

        return nationalityMap;
    }
}
