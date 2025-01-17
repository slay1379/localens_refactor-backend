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
        log.info("=== StatsBatchService: updateMinMaxStatistics() START ===");

        List<String> placeList = commercialDistrictRepository.findAllPlaces();
        log.info("Found places from DB: {}", placeList);

        // 각 지표별로 min/max 계산
        String[] fields = {
                "total_population",
                "stay_visit_ratio",
                "congestion_change_rate",
                "stay_to_visitor",
                "stay_duration_change_rate"
        };

        for (String place : placeList) {
            for (String field : fields) {
                try {
                    String minQuery = String.format("""
                        from(bucket: "hourly")
                            |> range(start: 2023-08-30T00:00:00Z, stop: 2024-08-31T23:59:59Z)
                            |> filter(fn: (r) => r["place"] == "%s")
                            |> filter(fn: (r) => r["_field"] == "%s")
                            |> min()
                        """, place, field);

                    String maxQuery = String.format("""
                        from(bucket: "hourly")
                            |> range(start: 2023-08-30T00:00:00Z, stop: 2024-08-31T23:59:59Z)
                            |> filter(fn: (r) => r["place"] == "%s")
                            |> filter(fn: (r) => r["_field"] == "%s")
                            |> max()
                        """, place, field);

                    double minVal = executeQuery(minQuery);
                    double maxVal = executeQuery(maxQuery);

                    log.info("Stats for {}.{}: min={}, max={}", place, field, minVal, maxVal);

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

                } catch (Exception e) {
                    log.error("Error processing stats for {}.{}: {}", place, field, e.getMessage());
                }
            }
        }

        log.info("=== StatsBatchService: updateMinMaxStatistics() END ===");
    }

    private double executeQuery(String query) {
        List<FluxTable> tables = influxDBClientWrapper.query(query);
        if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
            return 0.0;
        }
        Object value = tables.get(0).getRecords().get(0).getValueByKey("_value");
        return value != null ? Double.parseDouble(value.toString()) : 0.0;
    }
}