package com.example.localens.analysis.service;

import com.example.localens.analysis.dto.PopulationDetailsDTO;
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

    private static final String CURRENT_RANGE = "start: 2023-08-01T00:00:00Z, stop: 2025-01-17T23:59:59Z";

    public Map<String, Double> getHourlyFloatingPopulation(PopulationDetailsDTO details) {
        String query = String.format("""
           from(bucket: "result_bucket")
               |> range(%s)
               |> filter(fn: (r) => r["place"] == "%s")
               |> filter(fn: (r) => r["_field"] == "total_population")
               |> pivot(rowKey:["_time"], columnKey: ["tmzn"], valueColumn: "_value")
           """, CURRENT_RANGE, details.getDistrictName());

        return executeTimeBasedQuery(query);
    }

    public Map<String, Double> getHourlyStayVisitRatio(PopulationDetailsDTO details) {
        String query = String.format("""
           from(bucket: "result_stay_visit_bucket")
               |> range(%s)
               |> filter(fn: (r) => r["place"] == "%s")
               |> filter(fn: (r) => r["_field"] == "stay_visit_ratio")
               |> pivot(rowKey:["_time"], columnKey: ["tmzn"], valueColumn: "_value")
           """, CURRENT_RANGE, details.getDistrictName());

        return executeTimeBasedQuery(query);
    }

    public Map<String, Double> getHourlyCongestionRateChange(PopulationDetailsDTO details) {
        String query = String.format("""
           from(bucket: "date_congestion")
               |> range(%s)
               |> filter(fn: (r) => r["place"] == "%s")
               |> filter(fn: (r) => r["_field"] == "congestion_change_rate")
               |> pivot(rowKey:["_time"], columnKey: ["tmzn"], valueColumn: "_value")
           """, CURRENT_RANGE, details.getDistrictName());

        return executeTimeBasedQuery(query);
    }

    public Map<String, Double> getStayPerVisitorDuration(PopulationDetailsDTO details) {
        String query = String.format("""
           from(bucket: "stay_per_visitor_bucket")
               |> range(%s)
               |> filter(fn: (r) => r["place"] == "%s")
               |> filter(fn: (r) => r["_field"] == "stay_to_visitor")
               |> pivot(rowKey:["_time"], columnKey: ["tmzn"], valueColumn: "_value")
           """, CURRENT_RANGE, details.getDistrictName());

        return executeTimeBasedQuery(query);
    }

    public Map<String, Double> getHourlyAvgStayDurationChange(PopulationDetailsDTO details) {
        String query = String.format("""
           from(bucket: "date_stay_duration")
               |> range(%s)
               |> filter(fn: (r) => r["place"] == "%s")
               |> filter(fn: (r) => r["_field"] == "stay_duration_change_rate")
               |> pivot(rowKey:["_time"], columnKey: ["tmzn"], valueColumn: "_value")
           """, CURRENT_RANGE, details.getDistrictName());

        return executeTimeBasedQuery(query);
    }

    public Map<String, Map<String, Double>> getAgeGroupStayPattern(PopulationDetailsDTO details) {
        String query = String.format("""
        from(bucket: "age_gender_bucket")
            |> range(%s)
            |> filter(fn: (r) => r["place"] == "%s")
            |> filter(fn: (r) => r["_field"] == "total_population")
            |> pivot(rowKey:["age_group"], columnKey: ["sex"], valueColumn: "_value")
            |> filter(fn: (r) => r["_value"] > 0)
            |> last()
        """, CURRENT_RANGE, details.getDistrictName());

        Map<String, Map<String, Double>> result = executeAgeGroupQuery(query);
        log.info("Age group query result: {}", result);
        return result;
    }

    public Map<String, Double> getNationalityStayPattern(PopulationDetailsDTO details) {
        String query = String.format("""
           from(bucket: "nationality_bucket")
               |> range(%s)
               |> filter(fn: (r) => r["place"] == "%s")
               |> filter(fn: (r) => r["_field"] == "total_population")
               |> group(columns: ["nationality"])
               |> mean()
           """, CURRENT_RANGE, details.getDistrictName());

        return executeNationalityQuery(query);
    }

    private Map<String, Map<String, Double>> executeAgeGroupQuery(String query) {
        Map<String, Map<String, Double>> result = new LinkedHashMap<>();
        try {
            log.info("Executing age group query: {}", query);
            List<FluxTable> tables = influxDBClientWrapper.query(query);
            log.info("Age group query returned {} tables", tables.size());

            if (tables.isEmpty()) {
                log.warn("No tables returned from age group query");
                return result;
            }

            for (FluxTable table : tables) {
                log.info("Processing table with {} records", table.getRecords().size());
                for (FluxRecord record : table.getRecords()) {
                    try {
                        log.debug("Processing record: {}", record.getValues());
                        if (record.getValueByKey("age_group") == null) {
                            log.warn("Record missing age_group key: {}", record.getValues());
                            continue;
                        }

                        String ageGroup = record.getValueByKey("age_group").toString();
                        Map<String, Double> genderData = new LinkedHashMap<>();

                        // M과 F 값을 직접 가져오지 않고 모든 값을 로깅
                        record.getValues().forEach((key, value) -> {
                            log.debug("Record key: {}, value: {}", key, value);
                        });

                        Object maleValue = record.getValueByKey("M");
                        Object femaleValue = record.getValueByKey("F");

                        if (maleValue != null) {
                            double maleCount = Double.parseDouble(maleValue.toString());
                            if (maleCount > 0) {
                                genderData.put("male", maleCount);
                            }
                        }

                        if (femaleValue != null) {
                            double femaleCount = Double.parseDouble(femaleValue.toString());
                            if (femaleCount > 0) {
                                genderData.put("female", femaleCount);
                            }
                        }

                        if (!genderData.isEmpty()) {
                            result.put(ageGroup, genderData);
                            log.info("Added age group {} with gender data: {}", ageGroup, genderData);
                        }
                    } catch (Exception e) {
                        log.error("Error processing record: {} - {}", record.getValues(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error executing age group query: {} - {}", query, e.getMessage());
        }

        return result;
    }

    private Map<String, Double> executeNationalityQuery(String query) {
        Map<String, Double> result = new LinkedHashMap<>();
        try {
            List<FluxTable> tables = influxDBClientWrapper.query(query);
            log.debug("Nationality query result tables: {}", tables);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    try {
                        String nationality = record.getValueByKey("nationality").toString();
                        Object value = record.getValueByKey("_value");

                        if (value != null) {
                            result.put(nationality, Double.parseDouble(value.toString()));
                        }
                    } catch (Exception e) {
                        log.error("Error processing nationality record: {}", e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error executing nationality query: {} - {}", query, e.getMessage());
        }
        return result;
    }

    private Map<String, Double> executeTimeBasedQuery(String query) {
        Map<String, Double> result = new LinkedHashMap<>();
        try {
            List<FluxTable> tables = influxDBClientWrapper.query(query);
            log.debug("Time based query result tables: {}", tables);

            if (!tables.isEmpty() && !tables.get(0).getRecords().isEmpty()) {
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
}
