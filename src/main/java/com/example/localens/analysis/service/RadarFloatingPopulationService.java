package com.example.localens.analysis.service;

import com.example.localens.analysis.dto.FloatingPopulationResponse;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RadarFloatingPopulationService {

    private final CommercialDistrictRepository districtRepository;
    private final InfluxDBClientWrapper influxDBClientWrapper;

    public FloatingPopulationResponse getNormalizedFloatingPopulation(Integer districtUuid) {
        // MySQL에서 상권 이름 조회
        String districtName = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (districtName == null) {
            throw new IllegalArgumentException("Invalid districtUuid: " + districtUuid);
        }

        String fluxQuery = String.format(
                "from(bucket: \"result_bucket\") " +
                        "|> range(start: 2024-01-30T00:00:00Z, stop: now()) " +
                        "|> group() " +
                        "|> group(columns: [\"place\"]) " +
                        "|> sum(column: \"_value\") " +
                        "|> filter(fn: (r) => r[\"place\"] == \"%s\")", districtName
        );

        // Execute the query
        List<FluxTable> queryResult = influxDBClientWrapper.query(fluxQuery);

        // Extract the result
        double rawValue = extractValueFromQueryResult(queryResult);

        // Normalize the value
        double normalizedValue = normalize(rawValue, 2648146.7, 43203378);

        double formattedValue = Math.round(normalizedValue * 100.0) / 100.0;

        // Return the response
        return new FloatingPopulationResponse(formattedValue);
    }

    private double extractValueFromQueryResult(List<FluxTable> queryResult) {
        for (FluxTable table : queryResult) {
            for (FluxRecord record : table.getRecords()) {
                Object valueObj = record.getValueByKey("_value");
                if (valueObj != null) {
                    return Double.parseDouble(valueObj.toString());
                }
            }
        }
        throw new IllegalArgumentException("No valid data found in query result.");
    }

    private double normalize(double value, double min, double max) {
        return (value - min) / (max - min) * (1 - 0.1) + 0.1;
    }
}
