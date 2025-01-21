package com.example.localens.analysis.service;

import com.example.localens.analysis.domain.CommercialDistrict;
import com.example.localens.analysis.dto.PopulationDetailsDTO;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * "옛 PopulationController" API가 필요로 했던
 *  - 시간대별 유동인구(hourlyFloatingPopulation)
 *  - 시간대별 체류/방문 비율(hourlyStayVisitRatio)
 *  - 시간대별 혼잡도 변화율(hourlyCongestionRateChange)
 *  - 체류시간 대비 방문자 수(stayPerVisitorDuration)
 *  - 시간대별 평균 체류시간 변화율(hourlyAvgStayDurationChange)
 *  - 연령대별 체류 패턴(ageGroupStayPattern)
 *  - 국적별 체류 패턴(nationalityStayPattern)
 *
 * 등을 InfluxDB에서 가져와 Map 형태로 반환.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PopulationDetailsInfluxHelper {

    private final InfluxDBClientWrapper influxDBClientWrapper;
    private final CommercialDistrictRepository commercialDistrictRepository;

    private static final String CURRENT_RANGE = "start: 2023-08-01T00:00:00Z, stop: 2025-01-18T23:59:59Z";

    /**
     * 시간대별 유동인구
     */
    public Map<String, Double> getHourlyFloatingPopulation(Integer districtUuid) {
        String districtName = getDistrictName(districtUuid);

        String query = String.format("""
           from(bucket: "result_bucket")
               |> range(%s)
               |> filter(fn: (r) => r["place"] == "%s")
               |> filter(fn: (r) => r["_field"] == "total_population")
               |> pivot(rowKey:["_time"], columnKey: ["tmzn"], valueColumn: "_value")
           """, CURRENT_RANGE, districtName);

        return executeTimeBasedQuery(query);
    }

    /**
     * 시간대별 체류/방문 비율
     */
    public Map<String, Double> getHourlyStayVisitRatio(Integer districtUuid) {
        String districtName = getDistrictName(districtUuid);

        String query = String.format("""
           from(bucket: "result_stay_visit_bucket")
               |> range(%s)
               |> filter(fn: (r) => r["place"] == "%s")
               |> filter(fn: (r) => r["_field"] == "stay_visit_ratio")
               |> pivot(rowKey:["_time"], columnKey: ["tmzn"], valueColumn: "_value")
           """, CURRENT_RANGE, districtName);

        return executeTimeBasedQuery(query);
    }

    /**
     * 시간대별 혼잡도 변화율
     */
    public Map<String, Double> getHourlyCongestionRateChange(Integer districtUuid) {
        String districtName = getDistrictName(districtUuid);

        String query = String.format("""
           from(bucket: "date_congestion")
               |> range(%s)
               |> filter(fn: (r) => r["place"] == "%s")
               |> filter(fn: (r) => r["_field"] == "congestion_change_rate")
               |> pivot(rowKey:["_time"], columnKey: ["tmzn"], valueColumn: "_value")
           """, CURRENT_RANGE, districtName);

        return executeTimeBasedQuery(query);
    }

    /**
     * 체류시간 대비 방문자 수
     */
    public Map<String, Double> getStayPerVisitorDuration(Integer districtUuid) {
        String districtName = getDistrictName(districtUuid);

        String query = String.format("""
           from(bucket: "stay_per_visitor_bucket")
               |> range(%s)
               |> filter(fn: (r) => r["place"] == "%s")
               |> filter(fn: (r) => r["_field"] == "stay_to_visitor")
               |> pivot(rowKey:["_time"], columnKey: ["tmzn"], valueColumn: "_value")
           """, CURRENT_RANGE, districtName);

        return executeTimeBasedQuery(query);
    }

    /**
     * 시간대별 평균 체류시간 변화율
     */
    public Map<String, Double> getHourlyAvgStayDurationChange(Integer districtUuid) {
        String districtName = getDistrictName(districtUuid);

        String query = String.format("""
           from(bucket: "date_stay_duration")
               |> range(%s)
               |> filter(fn: (r) => r["place"] == "%s")
               |> filter(fn: (r) => r["_field"] == "stay_duration_change_rate")
               |> pivot(rowKey:["_time"], columnKey: ["tmzn"], valueColumn: "_value")
           """, CURRENT_RANGE, districtName);

        return executeTimeBasedQuery(query);
    }

    /**
     * 연령대별 체류 패턴
     */
    public Map<String, Map<String, Double>> getAgeGroupStayPattern(Integer districtUuid) {
        String districtName = getDistrictName(districtUuid);

        String query = String.format("""
        from(bucket: "age_gender_bucket")
            |> range(%s)
            |> filter(fn: (r) => r["place"] == "%s")
            |> filter(fn: (r) => r["_field"] == "total_population")
            |> group(columns: ["age_group", "sex"])
            |> sum()
        """, CURRENT_RANGE, districtName);

        Map<String, Map<String, Double>> result = executeAgeGroupQuery(query);
        log.info("Age group query result: {}", result);
        return result;
    }

    /**
     * 국적별 체류 패턴
     */
    public Map<String, Double> getNationalityStayPattern(Integer districtUuid) {
        String districtName = getDistrictName(districtUuid);

        String query = String.format("""
           from(bucket: "nationality_bucket")
               |> range(%s)
               |> filter(fn: (r) => r["place"] == "%s")
               |> filter(fn: (r) => r["_field"] == "total_population")
               |> group(columns: ["nationality"])
               |> mean()
           """, CURRENT_RANGE, districtName);

        return executeNationalityQuery(query);
    }

    // -------------------------------------
    // 아래는 내부 메서드들 (query 실행/파싱)
    // -------------------------------------

    /**
     * 상권 UUID로 districtName 찾아오기
     */
    private String getDistrictName(Integer districtUuid) {
        CommercialDistrict district = commercialDistrictRepository
                .findByDistrictUuid(districtUuid)
                .orElseThrow(() -> new EntityNotFoundException("District not found: " + districtUuid));

        return district.getDistrictName();
    }

    /**
     * [연령대별] 쿼리 결과 처리
     */
    private Map<String, Map<String, Double>> executeAgeGroupQuery(String query) {
        Map<String, Map<String, Double>> result = new LinkedHashMap<>();

        // 기본 연령대 구조 초기화
        String[] ageGroups = {"10대 미만", "10대", "20대", "30대", "40대", "50대", "60대", "70대 이상"};
        for (String ageGroup : ageGroups) {
            Map<String, Double> genderData = new LinkedHashMap<>();
            genderData.put("male", 0.0);
            genderData.put("female", 0.0);
            result.put(ageGroup, genderData);
        }

        try {
            log.info("Executing age group query: {}", query);
            List<FluxTable> tables = influxDBClientWrapper.query(query);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    String ageGroup = sanitizeAgeGroupKey(record.getValueByKey("age_group").toString());
                    String sex = record.getValueByKey("sex").toString().trim();
                    Double value = record.getValueByKey("_value") != null ?
                            Double.parseDouble(record.getValueByKey("_value").toString()) : 0.0;

                    log.debug("Processing record: age_group={}, sex={}, value={}", ageGroup, sex, value);

                    if (!result.containsKey(ageGroup)) {
                        log.warn("Unknown age group: {}", ageGroup);
                        continue;
                    }

                    if ("M".equalsIgnoreCase(sex) || "MALE".equalsIgnoreCase(sex)) {
                        result.get(ageGroup).put("male", result.get(ageGroup).get("male") + value);
                    } else if ("F".equalsIgnoreCase(sex) || "FEMALE".equalsIgnoreCase(sex)) {
                        result.get(ageGroup).put("female", result.get(ageGroup).get("female") + value);
                    } else {
                        log.warn("Unknown sex: {}", sex);
                    }
                }
            }

            log.info("Final age group result: {}", result);

        } catch (Exception e) {
            log.error("Error executing age group query: {}", e.getMessage());
        }

        return result;
    }

    /**
     * [국적별] 쿼리 결과 처리
     */
    private Map<String, Double> executeNationalityQuery(String query) {
        Map<String, Double> result = new LinkedHashMap<>();
        result.put("local", 0.0);
        result.put("foreigner", 0.0); // 초기화

        try {
            log.info("Executing nationality query: {}", query);
            List<FluxTable> tables = influxDBClientWrapper.query(query);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    try {
                        String nationality = record.getValueByKey("nationality").toString().trim();
                        Double value = record.getValueByKey("_value") != null ?
                                Double.parseDouble(record.getValueByKey("_value").toString()) : 0.0;

                        log.debug("Processing record: nationality={}, value={}", nationality, value);

                        if ("내국인".equalsIgnoreCase(nationality)) {
                            result.put("local", result.get("local") + value);
                        } else if ("장기체류외국인".equalsIgnoreCase(nationality)) {
                            result.put("foreigner", result.get("foreigner") + value);
                        } else {
                            log.warn("Unknown nationality: {}", nationality);
                        }
                    } catch (Exception e) {
                        log.error("Error processing nationality record: {}", e.getMessage());
                    }
                }
            }

            log.info("Final nationality result: {}", result);

        } catch (Exception e) {
            log.error("Error executing nationality query: {}", e.getMessage());
        }

        return result;
    }


    /**
     * [시간대별] 쿼리 결과 처리 (Pivot 된 테이블에서 "0"~"23" 컬럼 추출)
     */
    private Map<String, Double> executeTimeBasedQuery(String query) {
        Map<String, Double> result = new LinkedHashMap<>();
        try {
            List<FluxTable> tables = influxDBClientWrapper.query(query);
            log.debug("Time based query result tables: {}", tables);

            if (!tables.isEmpty() && !tables.get(0).getRecords().isEmpty()) {
                // 첫 테이블의 첫 레코드만 pivot 결과로 보고, 0~23 컬럼을 뽑아옴
                FluxRecord record = tables.get(0).getRecords().get(0);

                for (String key : record.getValues().keySet()) {
                    if (key.matches("\\d+")) {
                        Object value = record.getValueByKey(key);
                        if (value != null) {
                            result.put(key, Double.parseDouble(value.toString()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error executing time based query: {} - {}", query, e.getMessage());
        }
        return result;
    }

    private String sanitizeAgeGroupKey(String ageGroup) {
        if (ageGroup == null) return "";
        return ageGroup.trim().replaceAll("\\s+", ""); // 공백 제거 및 정리
    }

}
