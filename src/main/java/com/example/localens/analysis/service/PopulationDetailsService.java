package com.example.localens.analysis.service;

import com.example.localens.analysis.domain.CommercialDistrict;
import com.example.localens.analysis.dto.AgeGroupStayPatternDTO;
import com.example.localens.analysis.dto.NationalityPatternDTO;
import com.example.localens.analysis.dto.PopulationDetailsTransformedDTO;
import com.example.localens.analysis.dto.PopulationHourlyDataDTO;
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

    /**
     * 기존에 PopulationDetailsResponseDTO를 반환하던 메서드를
     * 이제 "Map<String,Object>" 로 직접 반환하도록 수정.
     */
    public Map<String, Object> getDetailsByDistrictUuid(Integer districtUuid) {
        // 1) DB에서 상권 찾기
        CommercialDistrict district = commercialDistrictRepository.findByDistrictUuid(districtUuid)
                .orElseThrow(() -> new EntityNotFoundException("District not found: " + districtUuid));

        String districtName = district.getDistrictName();
        log.info("Getting population details for district: {} (uuid={})", districtName, districtUuid);

        // 2) InfluxHelper를 통해 필요한 데이터 전부 조회, 하나의 Map에 모은다
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            // 시간대별 유동인구
            Map<String, Double> floatingPopMap = influxHelper.getHourlyFloatingPopulation(districtUuid);
            result.put("hourlyFloatingPopulation", floatingPopMap);

            // 시간대별 체류/방문 비율
            Map<String, Double> stayVisitMap = influxHelper.getHourlyStayVisitRatio(districtUuid);
            result.put("hourlyStayVisitRatio", stayVisitMap);

            // 시간대별 혼잡도 변화율
            Map<String, Double> congestionMap = influxHelper.getHourlyCongestionRateChange(districtUuid);
            result.put("hourlyCongestionRateChange", congestionMap);

            // 체류시간 대비 방문자 수
            Map<String, Double> stayPerVisitorMap = influxHelper.getStayPerVisitorDuration(districtUuid);
            result.put("stayPerVisitorDuration", stayPerVisitorMap);

            // 시간대별 평균 체류시간 변화율
            Map<String, Double> avgStayDurationMap = influxHelper.getHourlyAvgStayDurationChange(districtUuid);
            result.put("hourlyAvgStayDurationChange", avgStayDurationMap);

            // 연령대별 체류 패턴
            Map<String, Map<String, Double>> ageGroupMap = influxHelper.getAgeGroupStayPattern(districtUuid);
            result.put("ageGroupStayPattern", ageGroupMap);

            // 국적별 체류 패턴
            Map<String, Double> nationalityMap = influxHelper.getNationalityStayPattern(districtUuid);
            result.put("nationalityStayPattern", nationalityMap);

            return result;

        } catch (Exception e) {
            log.error("Error getting population details for district {}: {}", districtName, e.getMessage());
            throw new RuntimeException("Failed to get population details", e);
        }
    }
    public PopulationDetailsTransformedDTO getTransformedDetailsByDistrictUuid(Integer districtUuid) {
        Map<String, Object> raw = getDetailsByDistrictUuid(districtUuid);

        // 이제 raw에서 필요한 키를 뽑아, 우리가 만든 DTO로 매핑
        PopulationDetailsTransformedDTO dto = new PopulationDetailsTransformedDTO();

        // 각각 캐스팅해서 변환
        dto.setHourlyFloatingPopulation(
                PopulationHourlyDataDTO.from(
                        castMapDouble(raw.get("hourlyFloatingPopulation"))
                )
        );
        dto.setHourlyStayVisitRatio(
                PopulationHourlyDataDTO.from(
                        castMapDouble(raw.get("hourlyStayVisitRatio"))
                )
        );
        dto.setHourlyCongestionRateChange(
                PopulationHourlyDataDTO.from(
                        castMapDouble(raw.get("hourlyCongestionRateChange"))
                )
        );
        dto.setStayPerVisitorDuration(
                PopulationHourlyDataDTO.from(
                        castMapDouble(raw.get("stayPerVisitorDuration"))
                )
        );
        dto.setHourlyAvgStayDurationChange(
                PopulationHourlyDataDTO.from(
                        castMapDouble(raw.get("hourlyAvgStayDurationChange"))
                )
        );

        // age group
        dto.setAgeGroupStayPattern(
                AgeGroupStayPatternDTO.from(
                        castMapMapDouble(raw.get("ageGroupStayPattern"))
                )
        );

        // nationality
        dto.setNationalityStayPattern(
                NationalityPatternDTO.from(
                        castMapDouble(raw.get("nationalityStayPattern"))
                )
        );

        return dto;
    }

    // 안전 캐스팅: Object -> Map<String,Double>
    @SuppressWarnings("unchecked")
    private Map<String, Double> castMapDouble(Object obj) {
        if (obj instanceof Map) {
            return (Map<String, Double>) obj;
        }
        return null;
    }

    // 안전 캐스팅: Object -> Map<String,Map<String,Double>>
    @SuppressWarnings("unchecked")
    private Map<String, Map<String, Double>> castMapMapDouble(Object obj) {
        if (obj instanceof Map) {
            return (Map<String, Map<String, Double>>) obj;
        }
        return null;
    }
}
