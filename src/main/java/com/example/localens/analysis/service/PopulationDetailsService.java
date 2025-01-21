package com.example.localens.analysis.service;

import com.example.localens.analysis.domain.CommercialDistrict;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PopulationDetailsService {
    private final CommercialDistrictRepository commercialDistrictRepository;
    private final PopulationDetailsInfluxHelper influxHelper;

    public Map<String, Object> getDetailsByDistrictUuid(Integer districtUuid) {

        CommercialDistrict district = commercialDistrictRepository.findByDistrictUuid(districtUuid)
                .orElseThrow(() -> new EntityNotFoundException("District not found"));

        Map<String, Object> result = new LinkedHashMap<>();

        // 1) 시간대별 유동인구
        Map<String, Double> floatingPop = influxHelper.getHourlyFloatingPopulation(districtUuid);
        result.put("hourlyFloatingPopulation", floatingPop);

        // 2) 시간대별 체류/방문 비율
        Map<String, Double> stayVisitRatio = influxHelper.getHourlyStayVisitRatio(districtUuid);
        result.put("hourlyStayVisitRatio", stayVisitRatio);

        // 3) 시간대별 혼잡도 변화율
        Map<String, Double> congestionRate = influxHelper.getHourlyCongestionRateChange(districtUuid);
        result.put("hourlyCongestionRateChange", congestionRate);

        // 4) 체류시간 대비 방문자 수
        Map<String, Double> stayPerVisitor = influxHelper.getStayPerVisitorDuration(districtUuid);
        result.put("stayPerVisitorDuration", stayPerVisitor);

        // 5) 평균 체류시간 변화율
        Map<String, Double> avgStayDurationChange = influxHelper.getHourlyAvgStayDurationChange(districtUuid);
        result.put("hourlyAvgStayDurationChange", avgStayDurationChange);

        // 6) 연령대별 체류 패턴
        Map<String, Map<String, Double>> ageGenderPattern = influxHelper.getAgeGroupStayPattern(districtUuid);
        result.put("ageGroupStayPattern", ageGenderPattern);

        // 7) 국적별 체류 패턴
        Map<String, Double> nationalityPattern = influxHelper.getNationalityStayPattern(districtUuid);
        result.put("nationalityStayPattern", nationalityPattern);

        return result;
    }
}
