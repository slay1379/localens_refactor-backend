package com.example.localens.analysis.service;

import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
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

    private static final String CURRENT_RANGE = "start: 2024-05-30T00:00:00Z, stop: 2025-01-17T23:59:59Z";
    private static final String DATE_COMPARE_RANGE = "start: 2023-08-30T00:00:00Z, stop: 2025-01-17T23:59:59Z";

    public Map<String, Double> getHourlyFloatingPopulation(Integer districtUuid) {
        String place = getDistrictName(districtUuid);
        String query = String.format("""
            from(bucket: "result_bucket")
                |> range(%s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "total_population")
                |> pivot(rowKey:["_time"], columnKey: ["tmzn"], valueColumn: "_value")
            """, CURRENT_RANGE, place);

        return executeTimeBasedQuery(query);
    }

    public Map<String, Double> getHourlyStayVisitRatio(Integer districtUuid) {
        String place = getDistrictName(districtUuid);
        String query = String.format("""
            from(bucket: "result_stay_visit_bucket")
                |> range(%s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "stay_visit_ratio")
                |> pivot(rowKey:["_time"], columnKey: ["tmzn"], valueColumn: "_value")
            """, CURRENT_RANGE, place);

        return executeTimeBasedQuery(query);
    }

    public Map<String, Double> getHourlyCongestionRateChange(Integer districtUuid) {
        String place = getDistrictName(districtUuid);
        String query = String.format("""
            from(bucket: "date_congestion")
                |> range(%s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "congestion_change_rate")
                |> pivot(rowKey:["_time"], columnKey: ["tmzn"], valueColumn: "_value")
            """, DATE_COMPARE_RANGE, place);

        return executeTimeBasedQuery(query);
    }

    public Map<String, Double> getStayPerVisitorDuration(Integer districtUuid) {
        String place = getDistrictName(districtUuid);
        String query = String.format("""
            from(bucket: "stay_per_visitor_bucket")
                |> range(%s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "stay_to_visitor")
                |> pivot(rowKey:["_time"], columnKey: ["tmzn"], valueColumn: "_value")
            """, CURRENT_RANGE, place);

        return executeTimeBasedQuery(query);
    }

    public Map<String, Double> getHourlyAvgStayDurationChange(Integer districtUuid) {
        String place = getDistrictName(districtUuid);
        String query = String.format("""
            from(bucket: "date_stay_duration")
                |> range(%s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "stay_duration_change_rate")
                |> pivot(rowKey:["_time"], columnKey: ["tmzn"], valueColumn: "_value")
            """, DATE_COMPARE_RANGE, place);

        return executeTimeBasedQuery(query);
    }

    public Map<String, Map<String, Double>> getAgeGroupStayPattern(Integer districtUuid) {
        String place = getDistrictName(districtUuid);
        String query = String.format("""
            from(bucket: "age_gender_bucket")
                |> range(%s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "total_population")
                |> pivot(rowKey:["age_group"], columnKey: ["sex"], valueColumn: "_value")
                |> last()
            """, CURRENT_RANGE, place);

        Map<String, Map<String, Double>> result = new LinkedHashMap<>();
        try {
            List<FluxTable> tables = influxDBClientWrapper.query(query);
            if (!tables.isEmpty()) {  // null 체크 추가
                for (FluxTable table : tables) {
                    for (FluxRecord record : table.getRecords()) {
                        if (record != null && record.getValues() != null) {  // null 체크 추가
                            String ageGroup = record.getValueByKey("age_group").toString();
                            Map<String, Double> genderData = new LinkedHashMap<>();

                            // null 체크 개선
                            try {
                                Object maleValue = record.getValueByKey("M");
                                Object femaleValue = record.getValueByKey("F");

                                if (maleValue != null) {
                                    genderData.put("male", Double.parseDouble(maleValue.toString()));
                                }
                                if (femaleValue != null) {
                                    genderData.put("female", Double.parseDouble(femaleValue.toString()));
                                }

                                if (!genderData.isEmpty()) {  // 데이터가 있는 경우만 추가
                                    result.put(ageGroup, genderData);
                                }
                            } catch (NumberFormatException e) {
                                log.error("Error parsing gender values for age group {}: {}", ageGroup, e.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error querying age group stay pattern: {}", e.getMessage());
            log.debug("Query used: {}", query);  // 디버깅을 위한 쿼리 로깅 추가
        }
        return result;
    }

    public Map<String, Double> getNationalityStayPattern(Integer districtUuid) {
        String place = getDistrictName(districtUuid);
        String query = String.format("""
            from(bucket: "nationality_bucket")
                |> range(%s)
                |> filter(fn: (r) => r["place"] == "%s")
                |> filter(fn: (r) => r["_field"] == "total_population")
                |> pivot(rowKey:["nationality"], columnKey: ["_time"], valueColumn: "_value")  // pivot 수정
                |> last()
            """, CURRENT_RANGE, place);

        Map<String, Double> result = new LinkedHashMap<>();
        try {
            List<FluxTable> tables = influxDBClientWrapper.query(query);
            if (!tables.isEmpty()) {
                for (FluxRecord record : tables.get(0).getRecords()) {
                    if (record != null && record.getValues() != null) {
                        String nationality = record.getValueByKey("nationality").toString();
                        Object value = record.getValueByKey("_value");
                        if (value != null) {
                            try {
                                result.put(nationality, Double.parseDouble(value.toString()));
                            } catch (NumberFormatException e) {
                                log.error("Error parsing value for nationality {}: {}", nationality, e.getMessage());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error querying nationality stay pattern: {}", e.getMessage());
            log.debug("Query used: {}", query);
        }
        return result;
    }

    private Map<String, Double> executeTimeBasedQuery(String query) {
        Map<String, Double> result = new LinkedHashMap<>();
        try {
            List<FluxTable> tables = influxDBClientWrapper.query(query);
            if (!tables.isEmpty() && !tables.get(0).getRecords().isEmpty()) {
                FluxRecord record = tables.get(0).getRecords().get(0);
                for (String key : record.getValues().keySet()) {
                    if (key.matches("\\d+")) {  // 시간대를 나타내는 숫자 키만 처리
                        Object value = record.getValueByKey(key);
                        if (value != null) {
                            result.put(key, Double.parseDouble(value.toString()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error executing time based query: {}", e.getMessage());
        }
        return result;
    }

    private String getDistrictName(Integer districtUuid) {
        return commercialDistrictRepository.findDistrictNameByDistrictUuid(districtUuid);
    }
}
