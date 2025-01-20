package com.example.localens.analysis.service;

import com.example.localens.analysis.domain.MetricStatistics;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.analysis.repository.MetricFieldRepository;
import com.example.localens.analysis.repository.MetricStatisticsRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxTable;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsBatchService {

    private final InfluxDBClientWrapper influxDBClientWrapper;
    private final MetricStatisticsRepository metricStatisticsRepository;
    private final CommercialDistrictRepository commercialDistrictRepository;


    @PostConstruct
    public void initializeStats() {
        log.info("Initializing statistics...");
        updateMinMaxStatistics();
    }

    public void updateMinMaxStatistics() {
        List<String> placeList = commercialDistrictRepository.findAllPlaces();
        log.info("Found places from DB: {}", placeList);

        // 지표-버킷 매핑 수정
        Map<String, BucketFieldMapping> fieldMappings = Map.of(
                "total_population", new BucketFieldMapping("result_bucket", "total_population"),
                "stay_visit_ratio", new BucketFieldMapping("result_stay_visit_bucket", "stay_visit_ratio"),
                "visit_concentration", new BucketFieldMapping("date_stay_visit", "visit_concentration"),
                "congestion_change_rate", new BucketFieldMapping("date_congestion", "congestion_change_rate"),
                "stay_to_visitor", new BucketFieldMapping("stay_per_visitor_bucket", "stay_to_visitor"),
                "stay_duration_change_rate", new BucketFieldMapping("date_stay_duration", "stay_duration_change_rate")
        );

        for (String place : placeList) {
            for (Map.Entry<String, BucketFieldMapping> entry : fieldMappings.entrySet()) {
                String field = entry.getKey();
                BucketFieldMapping mapping = entry.getValue();
                try {
                    String minQuery = String.format("""
                    from(bucket: "%s")
                        |> range(start: 2023-08-30T00:00:00Z, stop: 2025-01-15T23:59:59Z)
                        |> filter(fn: (r) => r["place"] == "%s")
                        |> filter(fn: (r) => r["_field"] == "%s")
                        |> min()
                        |> yield(name: "min")
                    """, mapping.bucket(), place, mapping.fieldName());

                    String maxQuery = String.format("""
                    from(bucket: "%s")
                        |> range(start: 2023-08-30T00:00:00Z, stop: 2025-01-15T23:59:59Z)
                        |> filter(fn: (r) => r["place"] == "%s")
                        |> filter(fn: (r) => r["_field"] == "%s")
                        |> max()
                        |> yield(name: "max")
                    """, mapping.bucket(), place, mapping.fieldName());

                    double minVal = executeQuery(minQuery);
                    double maxVal = executeQuery(maxQuery);

                    log.info("Stats for {}.{}: min={}, max={}", place, field, minVal, maxVal);

                    saveMetricStatistics(place, field, minVal, maxVal);
                } catch (Exception e) {
                    log.error("Error processing stats for {}.{}: {}", place, field, e.getMessage());
                }
            }
        }
    }

    // 버킷과 필드 이름을 함께 관리하기 위한 레코드
    private record BucketFieldMapping(String bucket, String fieldName) {}

    private void saveMetricStatistics(String place, String field, double minVal, double maxVal) {
        MetricStatistics stats = metricStatisticsRepository
                .findByPlaceAndField(place, field)
                .orElse(MetricStatistics.builder()
                        .place(place)
                        .field(field)
                        .build());

        stats.setMinValue(minVal);
        stats.setMaxValue(maxVal);
        stats.setLastUpdated(LocalDateTime.now());

        metricStatisticsRepository.save(stats);
        log.info("Saved stats for {}.{}", place, field);
    }

    private double executeQuery(String query) {
        List<FluxTable> tables = influxDBClientWrapper.query(query);
        log.debug("Query result tables: {}", tables);

        if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
            log.warn("No data found for query: {}", query);
            return 0.0;
        }

        Object value = tables.get(0).getRecords().get(0).getValueByKey("_value");
        double result = value != null ? Double.parseDouble(value.toString()) : 0.0;
        log.info("Query value: {}", result);
        return result;
    }
}