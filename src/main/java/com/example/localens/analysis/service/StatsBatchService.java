package com.example.localens.analysis.service;

import com.example.localens.analysis.domain.MetricStatistics;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.analysis.repository.MetricFieldRepository;
import com.example.localens.analysis.repository.MetricStatisticsRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxTable;
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
    private final MetricFieldRepository metricFieldRepository;

    @Scheduled(cron = "0 0 3 * * ?")
    public void updateMinMaxStatistics() {
        log.info("=== StatsBatchService: updateMinMaxStatistics() START ===");

        // 1) DB에서 상권명 목록 조회
        List<String> placeList = commercialDistrictRepository.findAllPlaces();
        log.info("Found places from DB: {}", placeList);

        // 2) DB에서 지표(Field) 목록 조회
        List<String> fieldList = metricFieldRepository.findAllFieldNames();
        log.info("Found fields from DB: {}", fieldList);

        // 3) 각 (place,field) 조합으로 InfluxDB에서 min/max 구함
        for (String place : placeList) {
            for (String field : fieldList) {
                double minVal = queryMinValue(place, field);
                double maxVal = queryMaxValue(place, field);
                log.info("Calculated for place={}, field={}, min={}, max={}", place, field, minVal, maxVal);

                upsertMetricStatistics(place, field, minVal, maxVal);
            }
        }

        log.info("=== StatsBatchService: updateMinMaxStatistics() END ===");
    }

    // InfluxDB에서 field의 min값 구하기
    private double queryMinValue(String place, String field) {
        String fluxQuery = String.format(
                "from(bucket: \"hourly\") " +
                        "|> range(start: 2024-08-01T00:00:00Z, stop: 2024-09-30T00:00:00Z) " +
                        "|> filter(fn: (r) => r[\"place\"] == \"%s\" and r[\"_field\"] == \"%s\") " +
                        "|> group() " +
                        "|> min(column: \"_value\")",
                place, field
        );
        return executeFluxQueryForSingleValue(fluxQuery);
    }

    // InfluxDB에서 field의 max값 구하기
    private double queryMaxValue(String place, String field) {
        String fluxQuery = String.format(
                "from(bucket: \"hourly\") " +
                        "|> range(start: 2024-08-01T00:00:00Z, stop: 2024-09-30T00:00:00Z) " +
                        "|> filter(fn: (r) => r[\"place\"] == \"%s\" and r[\"_field\"] == \"%s\") " +
                        "|> group() " +
                        "|> max(column: \"_value\")",
                place, field
        );
        return executeFluxQueryForSingleValue(fluxQuery);
    }

    // Flux 쿼리 결과에서 단일 값 추출
    private double executeFluxQueryForSingleValue(String fluxQuery) {
        List<FluxTable> tables = influxDBClientWrapper.query(fluxQuery);
        if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
            return 0.0;
        }
        Object valueObj = tables.get(0).getRecords().get(0).getValueByKey("_value");
        if (valueObj == null) return 0.0;
        return Double.parseDouble(valueObj.toString());
    }

    // MetricStatistics 테이블에 Upsert(있으면 update, 없으면 insert)
    private void upsertMetricStatistics(String place, String field, double minVal, double maxVal) {
        MetricStatistics stats = metricStatisticsRepository
                .findByPlaceAndField(place, field)
                .orElse(MetricStatistics.builder()
                        .place(place)
                        .field(field)
                        .build()
                );

        stats.setMinValue(minVal);
        stats.setMaxValue(maxVal);
        stats.setLastUpdated(LocalDateTime.now());

        log.info("Upserting MetricStatistics: place={}, field={}, minValue={}, maxValue={}",
                place, field, minVal, maxVal);

        metricStatisticsRepository.save(stats);
    }
}
