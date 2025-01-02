package com.example.localens.analysis.service;

import com.example.localens.analysis.dto.AgeGenderRatioResponse;
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
public class AgeGenderRatioService {

    private final CommercialDistrictRepository districtRepository; // MySQL 연결
    private final InfluxDBClientWrapper influxDBClientWrapper; // InfluxDB 연결

    public AgeGenderRatioResponse getAgeGenderPopulationRatio(Integer districtUuid) {
        // Step 1: MySQL에서 상권 이름 조회
        String districtName = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (districtName == null) {
            throw new IllegalArgumentException("유효하지 않은 상권 UUID: " + districtUuid);
        }

        // Step 2: InfluxDB 쿼리 작성
        String fluxQuery = String.format(
                "from(bucket: \"age_gender_bucket\") " +
                        "|> range(start: 2024-01-30T00:00:00Z, stop: now()) " +
                        "|> filter(fn: (r) => r[\"place\"] == \"%s\") " +
                        "|> keep(columns: [\"place\", \"_value\", \"age_group\", \"sex\"])", districtName
        );

        // Step 3: InfluxDB에서 데이터 조회
        List<FluxTable> queryResult = influxDBClientWrapper.query(fluxQuery);

        // Step 4: 데이터 처리 및 정렬
        Map<String, Map<String, Double>> ageGenderRatios = processAgeGenderRatios(queryResult);

        return new AgeGenderRatioResponse(ageGenderRatios);
    }

    private Map<String, Map<String, Double>> processAgeGenderRatios(List<FluxTable> queryResult) {
        Map<String, String> ageGroupMapping = Map.of(
                "10대", "Teenagers",
                "10대 미만", "Under10",
                "20대", "Twenties",
                "30대", "Thirties",
                "40대", "Forties",
                "50대", "Fifties",
                "60대", "Sixties",
                "70대 이상", "OverSeventies"
        );

        // 임시로 성별 데이터를 저장
        Map<String, Double> totalByGender = new HashMap<>(); // 성별별 전체 합
        Map<String, Map<String, Double>> ageGenderMap = new LinkedHashMap<>(); // 연령대별 성별 데이터

        // 데이터를 분류 (연령대별로 성별 데이터를 그룹화)
        for (FluxTable table : queryResult) {
            for (FluxRecord record : table.getRecords()) {
                String ageGroup = record.getValueByKey("age_group").toString(); // 연령대
                String sex = record.getValueByKey("sex").toString(); // 성별
                Double value = Double.valueOf(record.getValueByKey("_value").toString()); // 유동인구 값

                // 연령대 한글 -> 영어 변환
                String ageGroupEnglish = ageGroupMapping.getOrDefault(ageGroup, ageGroup);

                // 연령대별 데이터 추가
                ageGenderMap.computeIfAbsent(ageGroupEnglish, k -> new LinkedHashMap<>())
                        .merge(sex, value, Double::sum);

                // 성별별 총합 계산
                totalByGender.merge(sex, value, Double::sum);
            }
        }

        // 각 성별의 비율을 계산
        for (String ageGroup : ageGenderMap.keySet()) {
            Map<String, Double> genderMap = ageGenderMap.get(ageGroup);
            for (String sex : genderMap.keySet()) {
                double totalGenderPopulation = totalByGender.get(sex); // 해당 성별의 총합
                double ageGenderValue = genderMap.get(sex); // 해당 연령대-성별의 값
                genderMap.put(sex, Math.round((ageGenderValue / totalGenderPopulation) * 1000.0) / 10.0); // 비율 계산
            }
        }

        return ageGenderMap;
    }

}
