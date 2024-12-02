package com.example.localens.analysis.service;

import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class DatePopulationService {

    private final CommercialDistrictRepository districtRepository;
    private final InfluxDBClientWrapper influxDBClientWrapper;

    public DatePopulationService(CommercialDistrictRepository districtRepository, InfluxDBClientWrapper influxDBClientWrapper) {
        this.districtRepository = districtRepository;
        this.influxDBClientWrapper = influxDBClientWrapper;
    }

    public int getNormalizedPopulationValue(Integer districtUuid, String date) {
        log.debug("Received districtUuid: {}", districtUuid);
        log.debug("Received date: {}", date);
        // districtUuid로 place 조회
        String place = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        log.debug("Found place: {}", place);

        if (place == null) {
            throw new IllegalArgumentException("Invalid districtUuid: " + districtUuid);
        }

        // 날짜 파싱 및 가공
        LocalDateTime dateTime = LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
        String yearMonth = dateTime.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String dayOfWeek = dateTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN);

        log.debug("YearMonth: {}, DayOfWeek: {}", yearMonth, dayOfWeek);

        // InfluxDB 값 조회
        double rawValue = queryValue(place, yearMonth, dayOfWeek);
        log.debug("Raw Value: {}", rawValue);
        double maxValue = queryMaxValue(place);
        log.debug("Max Value: {}", maxValue);
        double minValue = queryMinValue(place);
        log.debug("Min Value: {}", minValue);

        // 정규화 계산 및 정수 반환
        return (int) Math.round(normalize(rawValue, minValue, maxValue) * 100);
    }

    private double normalize(double value, double min, double max) {
        if (max == min) {
            return 0.1; // 동일한 값인 경우 최소값 반환
        }
        return ((value - min) / (max - min)) * (1 - 0.1) + 0.1;
    }

    private double queryValue(String place, String yearMonth, String dayOfWeek) {
        String fluxQuery = String.format(
                "from(bucket: \"date_compare_population\") " +
                        "|> range(start: 2024-01-30T00:00:00Z, stop: now()) " +
                        "|> filter(fn: (r) => r[\"place\"] == \"%s\" and r[\"p_yyyymm\"] == \"%s\" and r[\"day_of_week\"] == \"%s\") " +
                        "|> group() " +
                        "|> group(columns: [\"place\", \"p_yyyymm\", \"day_of_week\"]) " +
                        "|> sum() " +
                        "|> keep(columns: [\"place\", \"_value\", \"day_of_week\"])",
                place, yearMonth, dayOfWeek
        );
        return executeFluxQuery(fluxQuery);
    }

    private double queryMaxValue(String place) {
        String fluxQuery = String.format(
                "from(bucket: \"date_compare_population\") " +
                        "|> range(start: 2024-01-30T00:00:00Z, stop: now()) " +
                        "|> filter(fn: (r) => r[\"place\"] == \"%s\") " +
                        "|> group() " +
                        "|> group(columns: [\"place\", \"p_yyyymm\", \"day_of_week\"]) " +
                        "|> sum() " +
                        "|> keep(columns: [\"place\", \"_value\", \"day_of_week\"]) " +
                        "|> group() " +
                        "|> max(column: \"_value\")",
                place
        );
        return executeFluxQuery(fluxQuery);
    }

    private double queryMinValue(String place) {
        String fluxQuery = String.format(
                "from(bucket: \"date_compare_population\") " +
                        "|> range(start: 2024-01-30T00:00:00Z, stop: now()) " +
                        "|> filter(fn: (r) => r[\"place\"] == \"%s\") " +
                        "|> group() " +
                        "|> group(columns: [\"place\", \"p_yyyymm\", \"day_of_week\"]) " +
                        "|> sum() " +
                        "|> keep(columns: [\"place\", \"_value\", \"day_of_week\"]) " +
                        "|> group() " +
                        "|> min(column: \"_value\")",
                place
        );
        return executeFluxQuery(fluxQuery);
    }

    private double executeFluxQuery(String fluxQuery) {
        List<FluxTable> tables = influxDBClientWrapper.query(fluxQuery);
        if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
            return 0.0; // 결과가 없을 경우 기본값 반환
        }
        return Double.parseDouble(tables.get(0).getRecords().get(0).getValueByKey("_value").toString());
    }
}
