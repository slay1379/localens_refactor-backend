package com.example.localens.analysis.service;

import com.example.localens.analysis.domain.CommercialDistrict;
import com.example.localens.analysis.dto.PopulationDetailsDTO;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
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

        // Helper 클래스 내부에서 중복 쿼리가 발생하지 않도록 district를 전달
        PopulationDetailsDTO details = new PopulationDetailsDTO(district);
        Map<String, Object> result = new LinkedHashMap<>();

        // 모든 쿼리에 district 객체 전달
        result.put("hourlyFloatingPopulation", influxHelper.getHourlyFloatingPopulation(details));
        result.put("hourlyStayVisitRatio", influxHelper.getHourlyStayVisitRatio(details));
        result.put("hourlyCongestionRateChange", influxHelper.getHourlyCongestionRateChange(details));
        result.put("stayPerVisitorDuration", influxHelper.getStayPerVisitorDuration(details));
        result.put("hourlyAvgStayDurationChange", influxHelper.getHourlyAvgStayDurationChange(details));
        result.put("ageGroupStayPattern", influxHelper.getAgeGroupStayPattern(details));
        result.put("nationalityStayPattern", influxHelper.getNationalityStayPattern(details));

        return result;
    }
}
