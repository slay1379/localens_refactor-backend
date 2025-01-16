package com.example.localens.analysis.service;

import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
@Component
@RequiredArgsConstructor
public class PopulationDetailsInfluxHelper {

    private final CommercialDistrictRepository districtRepository;
    private final InfluxDBClientWrapper influxDBClientWrapper;

    // ----------------------------------------------------
    // 1) 시간대별 유동인구
    // ----------------------------------------------------
    public Map<String, Double> getHourlyFloatingPopulation(Integer districtUuid) {
        // 1) districtUuid -> place
        String place = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (place == null) {
            throw new IllegalArgumentException("Invalid districtUuid: " + districtUuid);
        }

        // 2) Flux 쿼리 작성 (예시)
        //    여기서는 bucket="result_bucket" 라고 가정.
        //    "tmzn" = 시간대(0~23), "_value" = 유동인구 값
        String fluxQuery = String.format(
                "from(bucket: \"hourly\") "
                        + "|> range(start: -30d) "
                        + "|> filter(fn: (r) => r[\"place\"] == \"%s\" and r[\"_field\"] == \"total_population\") "
                        + "|> keep(columns: [\"tmzn\", \"_value\"]) ",
                place
        );

        // 3) 쿼리 실행
        List<FluxTable> tables = influxDBClientWrapper.query(fluxQuery);

        // 4) 결과 파싱: 시간대별로 Map<String, Double> ( "0" -> 1234.0, ...)
        Map<String, Double> result = new LinkedHashMap<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object tmznObj = record.getValueByKey("tmzn");
                Object valObj  = record.getValueByKey("_value");

                if (tmznObj == null || valObj == null) {
                    continue;
                }
                String tmzn = tmznObj.toString();  // "0", "1", "2", ...
                double numericValue = Double.parseDouble(valObj.toString());

                result.put(tmzn, numericValue);
            }
        }

        return result;
    }

    // ----------------------------------------------------
    // 2) 시간대별 체류/방문 비율
    // ----------------------------------------------------
    public Map<String, Double> getHourlyStayVisitRatio(Integer districtUuid) {
        String place = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (place == null) {
            throw new IllegalArgumentException("Invalid districtUuid: " + districtUuid);
        }

        String fluxQuery = String.format(
                "from(bucket: \"hourly\") "
                        + "|> range(start: -30d) "
                        + "|> filter(fn: (r) => r[\"place\"] == \"%s\" and r[\"_field\"] == \"stay_visit_ratio\") "
                        + "|> keep(columns: [\"tmzn\", \"_value\"]) ",
                place
        );

        List<FluxTable> tables = influxDBClientWrapper.query(fluxQuery);

        Map<String, Double> result = new LinkedHashMap<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object tmznObj = record.getValueByKey("tmzn");
                Object valObj  = record.getValueByKey("_value");

                if (tmznObj == null || valObj == null) {
                    continue;
                }
                String tmzn = tmznObj.toString();
                double numericValue = Double.parseDouble(valObj.toString());

                result.put(tmzn, numericValue);
            }
        }

        return result;
    }

    // ----------------------------------------------------
    // 3) 시간대별 혼잡도 변화율
    // ----------------------------------------------------
    public Map<String, Double> getHourlyCongestionRateChange(Integer districtUuid) {
        String place = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (place == null) {
            throw new IllegalArgumentException("Invalid districtUuid: " + districtUuid);
        }

        String fluxQuery = String.format(
                "from(bucket: \"hourly\") "
                        + "|> range(start: -30d) "
                        + "|> filter(fn: (r) => r[\"place\"] == \"%s\" and r[\"_field\"] == \"congestion_rate\") "
                        + "|> keep(columns: [\"tmzn\", \"_value\"]) ",
                place
        );

        List<FluxTable> tables = influxDBClientWrapper.query(fluxQuery);

        Map<String, Double> result = new LinkedHashMap<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object tmznObj = record.getValueByKey("tmzn");
                Object valObj  = record.getValueByKey("_value");

                if (tmznObj == null || valObj == null) {
                    continue;
                }
                String tmzn = tmznObj.toString();
                double numericValue = Double.parseDouble(valObj.toString());

                result.put(tmzn, numericValue);
            }
        }

        return result;
    }

    // ----------------------------------------------------
    // 4) 체류시간 대비 방문자 수
    // ----------------------------------------------------
    public Map<String, Double> getStayPerVisitorDuration(Integer districtUuid) {
        String place = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (place == null) {
            throw new IllegalArgumentException("Invalid districtUuid: " + districtUuid);
        }

        // 예시 bucket: "stay_per_visitor_bucket", field: "stayPerVisitor"
        String fluxQuery = String.format(
                "from(bucket: \"hourly\") "
                        + "|> range(start: -30d) "
                        + "|> filter(fn: (r) => r[\"place\"] == \"%s\" and r[\"_field\"] == \"stay_per_visitor\") "
                        + "|> keep(columns: [\"tmzn\", \"_value\"]) ",
                place
        );

        List<FluxTable> tables = influxDBClientWrapper.query(fluxQuery);

        Map<String, Double> result = new LinkedHashMap<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object tmznObj = record.getValueByKey("tmzn");
                Object valObj  = record.getValueByKey("_value");

                if (tmznObj == null || valObj == null) {
                    continue;
                }
                String tmzn = tmznObj.toString();
                double numericValue = Double.parseDouble(valObj.toString());

                result.put(tmzn, numericValue);
            }
        }

        return result;
    }

    // ----------------------------------------------------
    // 5) 시간대별 평균 체류시간 변화율
    // ----------------------------------------------------
    public Map<String, Double> getHourlyAvgStayDurationChange(Integer districtUuid) {
        String place = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (place == null) {
            throw new IllegalArgumentException("Invalid districtUuid: " + districtUuid);
        }

        // 예: bucket: "date_stay_duration", field: "stayTimeChange"
        String fluxQuery = String.format(
                "from(bucket: \"hourly\") "
                        + "|> range(start: -30d) "
                        + "|> filter(fn: (r) => r[\"place\"] == \"%s\" and r[\"_field\"] == \"stayTimeChange\") "
                        + "|> keep(columns: [\"tmzn\", \"_value\"]) ",
                place
        );

        List<FluxTable> tables = influxDBClientWrapper.query(fluxQuery);

        Map<String, Double> result = new LinkedHashMap<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object tmznObj = record.getValueByKey("tmzn");
                Object valObj  = record.getValueByKey("_value");

                if (tmznObj == null || valObj == null) {
                    continue;
                }
                String tmzn = tmznObj.toString();
                double numericValue = Double.parseDouble(valObj.toString());

                result.put(tmzn, numericValue);
            }
        }

        return result;
    }

    // ----------------------------------------------------
    // 6) 연령대별 체류 패턴
    // ----------------------------------------------------
    public Map<String, Map<String, Double>> getAgeGroupStayPattern(Integer districtUuid) {
        String place = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (place == null) {
            throw new IllegalArgumentException("Invalid districtUuid: " + districtUuid);
        }

        // 예시 bucket: "age_gender_bucket"
        // field: (가능하다면) "visitDuration" or "population"
        // columns: age_group, sex, _value
        String fluxQuery = String.format(
                "from(bucket: \"hourly\") "
                        + "|> range(start: -30d) "
                        + "|> filter(fn: (r) => r[\"place\"] == \"%s\") "
                        + "|> keep(columns: [\"age_group\", \"sex\", \"_value\"]) ",
                place
        );

        List<FluxTable> tables = influxDBClientWrapper.query(fluxQuery);

        // Map<연령대, Map<성별, 값>>
        Map<String, Map<String, Double>> result = new LinkedHashMap<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object ageObj = record.getValueByKey("age_group");
                Object sexObj = record.getValueByKey("sex");
                Object valObj = record.getValueByKey("_value");

                if (ageObj == null || sexObj == null || valObj == null) {
                    continue;
                }
                String ageGroup = ageObj.toString(); // "10대", "20대", ...
                String sex      = sexObj.toString(); // "M", "F"
                double numericValue = Double.parseDouble(valObj.toString());

                // 누적 or 평균일 수도 있으니, 여기선 단순 합산 예시
                // (실제로는 groupBy + mean 등 Influx에서 해도 됨)
                result.computeIfAbsent(ageGroup, k -> new LinkedHashMap<>())
                        .merge(sex, numericValue, Double::sum);
            }
        }

        return result;
    }

    // ----------------------------------------------------
    // 7) 국적별 체류 패턴
    // ----------------------------------------------------
    public Map<String, Double> getNationalityStayPattern(Integer districtUuid) {
        String place = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (place == null) {
            throw new IllegalArgumentException("Invalid districtUuid: " + districtUuid);
        }

        // 예: bucket: "nationality_bucket"
        // columns: nationality, _value
        String fluxQuery = String.format(
                "from(bucket: \"hourly\") "
                        + "|> range(start: -30d) "
                        + "|> filter(fn: (r) => r[\"place\"] == \"%s\") "
                        + "|> keep(columns: [\"nationality\", \"_value\"]) ",
                place
        );

        List<FluxTable> tables = influxDBClientWrapper.query(fluxQuery);

        Map<String, Double> result = new LinkedHashMap<>();
        double total = 0.0;

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object natObj = record.getValueByKey("nationality");
                Object valObj = record.getValueByKey("_value");

                if (natObj == null || valObj == null) {
                    continue;
                }
                String nationality = natObj.toString(); // "내국인", "장기체류외국인" ...
                double numericValue = Double.parseDouble(valObj.toString());

                result.merge(nationality, numericValue, Double::sum);
                total += numericValue;
            }
        }

        if (total > 0) {
            for (Map.Entry<String, Double> e : result.entrySet()) {
                double ratio = (e.getValue() / total) * 100.0;
                e.setValue(Math.round(ratio * 10.0) / 10.0); // 소수점 첫째 자리
            }
        }

        return result;
    }
}
