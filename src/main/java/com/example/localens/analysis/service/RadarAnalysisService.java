package com.example.localens.analysis.service;

import com.example.localens.analysis.domain.CommercialDistrict;
import com.example.localens.analysis.dto.ClusterDTO;
import com.example.localens.analysis.dto.DistrictDTO;
import com.example.localens.analysis.dto.RadarDataDTO;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import com.example.localens.influx.InfluxDBClientWrapper;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RadarAnalysisService {

    private final CommercialDistrictRepository districtRepository;
    private final MetricStatsService metricStatsService;
    private final InfluxDBClientWrapper influxDBClientWrapper;

    public RadarDataDTO getRadarData(Integer districtUuid) {
        //1) 상권 정보 조회
        CommercialDistrict district = districtRepository.findById(districtUuid)
                .orElseThrow(() -> new IllegalArgumentException("Invalid districtUuid: " + districtUuid));
        String place = district.getDistrictName();

        Map<String, Double> rawData = queryRadarRawData(place);

        Map<String, Integer> normalizedMap = new LinkedHashMap<>();
        for (Entry<String, Double> entry : rawData.entrySet()) {
            String field = entry.getKey();
            Double rawValue = entry.getValue();
            double normalized = metricStatsService.normalizeValue(place, field, rawValue);
            normalizedMap.put(field, (int) Math.round(normalized * 100));
        }

        DistrictDTO districtDTO = new DistrictDTO();
        districtDTO.setDistrictUuid(district.getDistrictUuid());
        districtDTO.setDistrictName(district.getDistrictName());
        if (district.getCluster() != null) {
            ClusterDTO clusterDTO = new ClusterDTO();
            clusterDTO.setClusterName(district.getCluster().getClusterName());
            clusterDTO.setClusterUuid(district.getCluster().getClusterUuid());
            districtDTO.setCluster(clusterDTO);
        }

        RadarDataDTO radarDataDTO = new RadarDataDTO();
        radarDataDTO.setDistrictInfo(districtDTO);
        radarDataDTO.setOverallData(normalizedMap);

        return radarDataDTO;
    }

    private Map<String, Double> queryRadarRawData(String place) {

        // (1) Flux 쿼리 작성
        //     - 어떤 버킷을 쓸지, 어떤 기간(range)을 쓸지,
        //       그리고 상권(place)에 맞춰 필터링을 어떻게 할지 결정
        //     - 여러 지표가 같은 measurement & bucket에 저장돼 있다 가정.
        String fluxQuery = String.format(
                "from(bucket: \"aggregate_24h\") "
                        + "|> range(start: -30d) "
                        + "|> filter(fn: (r) => r[\"place\"] == \"%s\") "
                        + "|> keep(columns: [\"_time\", \"_field\", \"_value\"])",
                place
        );

        List<FluxTable> tables = influxDBClientWrapper.query(fluxQuery);

        Map<String, Double> rawData = new LinkedHashMap<>();

        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Object fieldObj = record.getValueByKey("_field");
                Object valueObj = record.getValueByKey("_value");

                if (fieldObj == null || valueObj == null) {
                    continue;
                }

                String fieldName = fieldObj.toString();
                double numericValue = Double.parseDouble(valueObj.toString());

                rawData.put(fieldName, numericValue);
            }
        }
        return rawData;
    }
}
