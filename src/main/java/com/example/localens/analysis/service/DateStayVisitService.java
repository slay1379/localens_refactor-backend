package com.example.localens.analysis.service;

import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Slf4j
@Service
public class DateStayVisitService {

    private final CommercialDistrictRepository districtRepository;
    private final InfluxDBClientWrapper influxDBClientWrapper;

    public DateStayVisitService(CommercialDistrictRepository districtRepository, InfluxDBClientWrapper influxDBClientWrapper) {
        this.districtRepository = districtRepository;
        this.influxDBClientWrapper = influxDBClientWrapper;
    }

    public int getNormalizedStayVisitRatio(Integer districtUuid, String date) {
        // districtUuid로 place 조회
        String place = districtRepository.findDistrictNameByDistrictUuid(districtUuid);

        if (place == null) {
            throw new IllegalArgumentException("Invalid districtUuid: " + districtUuid);
        }

        // 날짜 파싱 및 가공
        LocalDateTime dateTime = LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
        String yearMonth = dateTime.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String dayOfWeek = dateTime.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN);

        // InfluxDB 값 조회
        double rawValue = queryValue(place, yearMonth, dayOfWeek);
        double maxValue = queryMaxValue(place);
        double minValue = queryMinValue(place);

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
                "from(bucket: \"date_stay_visit\") " +
                        "|> range(start: 2024-01-30T00:00:00Z, stop: now()) " +
                        "|> group(columns: [\"day_of_week\", \"p_yyyymm\", \"place\"]) " +
                        "|> mean(column: \"_value\") " +
                        "|> group() " +
                        "|> filter(fn: (r) => r[\"place\"] == \"%s\" and r[\"day_of_week\"] == \"%s\" and r[\"p_yyyymm\"] == \"%s\")",
                place, dayOfWeek, yearMonth
        );
        return executeFluxQuery(fluxQuery);
    }

    private double queryMaxValue(String place) {
        String fluxQuery = String.format(
                "from(bucket: \"date_stay_visit\") " +
                        "|> range(start: 2024-01-30T00:00:00Z, stop: now()) " +
                        "|> group(columns: [\"day_of_week\", \"p_yyyymm\", \"place\"]) " +
                        "|> mean(column: \"_value\") " +
                        "|> group() " +
                        "|> filter(fn: (r) => r[\"place\"] == \"%s\") " +
                        "|> max(column: \"_value\")",
                place
        );
        return executeFluxQuery(fluxQuery);
    }

    private double queryMinValue(String place) {
        String fluxQuery = String.format(
                "from(bucket: \"date_stay_visit\") " +
                        "|> range(start: 2024-01-30T00:00:00Z, stop: now()) " +
                        "|> group(columns: [\"day_of_week\", \"p_yyyymm\", \"place\"]) " +
                        "|> mean(column: \"_value\") " +
                        "|> group() " +
                        "|> filter(fn: (r) => r[\"place\"] == \"%s\") " +
                        "|> min(column: \"_value\")",
                place
        );
        return executeFluxQuery(fluxQuery);
    }

    private double executeFluxQuery(String fluxQuery) {
        var tables = influxDBClientWrapper.query(fluxQuery);
        if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
            return 0.0; // 결과가 없을 경우 기본값 반환
        }
        return Double.parseDouble(tables.get(0).getRecords().get(0).getValueByKey("_value").toString());
    }
}
