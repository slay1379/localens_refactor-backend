package com.example.localens.analysis.service;

import com.example.localens.analysis.domain.CommercialDistrict;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopulationDetailsService {
    private final CommercialDistrictRepository commercialDistrictRepository;
    private final PopulationDetailsInfluxHelper influxHelper;

    public Map<String, Object> getDetailsByDistrictUuid(Integer districtUuid) {
        // 한 번에 모든 정보를 가져옴
        CommercialDistrict district = commercialDistrictRepository.findByDistrictUuid(districtUuid)
                .orElseThrow(() -> new EntityNotFoundException("District not found"));

        String districtName = district.getDistrictName();
        Map<String, Object> result = new LinkedHashMap<>();

        // district 객체를 재사용
        result.put("hourlyFloatingPopulation", influxHelper.getHourlyFloatingPopulation(districtUuid));
        result.put("hourlyStayVisitRatio", influxHelper.getHourlyStayVisitRatio(districtUuid));
        result.put("hourlyCongestionRateChange", influxHelper.getHourlyCongestionRateChange(districtUuid));
        result.put("stayPerVisitorDuration", influxHelper.getStayPerVisitorDuration(districtUuid));
        result.put("hourlyAvgStayDurationChange", influxHelper.getHourlyAvgStayDurationChange(districtUuid));
        result.put("ageGroupStayPattern", influxHelper.getAgeGroupStayPattern(districtUuid));
        result.put("nationalityStayPattern", influxHelper.getNationalityStayPattern(districtUuid));

        return result;
    }
}
