package com.example.localens.analysis.service;

import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PopulationDetailsInfluxHelper {

    private final CommercialDistrictRepository districtRepository;
    private final InfluxDBClientWrapper influxDBClientWrapper;

    public Map<String, Double> getHourlyFloatingPopulation(Integer districtUuid) {
        String place = districtRepository.findDistrictNameByDistrictUuid(districtUuid);
        if (place == null) {
            throw new IllegalArgumentException("Invalid districtUuid: " + districtUuid);
        }

        String fluxQuery = String.format(
                "from(bucket: \"result_bucket\") "
                        + "|> range(start: 2023-07-30T00:00:00Z, stop: now()) "
                        + "|> filter(fn: (r) => r[\"place\"] == \"%s\" and r[\"_field\"] == \"population\") "
                        + "|> keep(columns: [\"tmzn\", \"_value\"]) ",
                place
        );

        List<FluxTable> tables = influxDBClientWrapper.query(fluxQuery);

        Map<String, Double> result = new LinkedHashMap<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object tmznObj = record.getValueByKey("tmzn");
                Object valObj = record.getValueByKey("_value");

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
}
