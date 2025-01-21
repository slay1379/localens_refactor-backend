package com.example.localens.analysis.service;

import com.example.localens.analysis.dto.AnalysisRadarDistrictInfoDTO;
import com.example.localens.analysis.dto.CompareTwoDistrictsDTO;
import com.example.localens.analysis.dto.ComparisonRadarDistrictInfoDTO;
import com.example.localens.analysis.dto.DifferenceItemDTO;
import com.example.localens.analysis.dto.RadarDataDTO;
import com.example.localens.analysis.dto.RadarDistrictInfoDTO;
import com.example.localens.analysis.dto.TopDifferencesDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RadarComparisonService {

    private final RadarAnalysisService radarAnalysisService;

    private static final Map<String, String> keyToKoreanMap = Map.of(
            "population", "유동인구 수",
            "stayVisit", "체류/방문 비율",
            "congestion", "혼잡도 변화율",
            "stayPerVisitor", "체류시간 대비 방문자 수",
            "visitConcentration", "방문 집중도",
            "stayTimeChange", "체류시간 변화율"
    );

    public CompareTwoDistrictsDTO compareTwoDistricts(Integer districtUuid1, Integer districtUuid2) {
        RadarDataDTO<ComparisonRadarDistrictInfoDTO> district1Radar = convertToComparisonDTO(
                radarAnalysisService.getRadarData(districtUuid1));
        RadarDataDTO<ComparisonRadarDistrictInfoDTO> district2Radar = convertToComparisonDTO(
                radarAnalysisService.getRadarData(districtUuid2));

        if (district1Radar == null || district2Radar == null) {
            throw new IllegalStateException("Failed to get radar data for districts");
        }

        Map<String, Integer> district1Overall = district1Radar.getOverallData();
        Map<String, Integer> district2Overall = district2Radar.getOverallData();

        if (district1Overall == null || district2Overall == null) {
            log.error("Overall data is null for districts {} and/or {}", districtUuid1, districtUuid2);
            district1Overall = new HashMap<>();
            district2Overall = new HashMap<>();
        }

        TopDifferencesDTO topDifferences = calculateTopDifferences(district1Overall, district2Overall);

        return new CompareTwoDistrictsDTO(district1Radar, district2Radar, topDifferences);
    }

    private TopDifferencesDTO calculateTopDifferences(Map<String, Integer> district1Overall,
                                                      Map<String, Integer> district2Overall) {
        if (district1Overall == null) district1Overall = new HashMap<>();
        if (district2Overall == null) district2Overall = new HashMap<>();

        Map<String, Double> differences = new HashMap<>();
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(district1Overall.keySet());
        allKeys.addAll(district2Overall.keySet());

        for (String key : allKeys) {
            int value1 = district1Overall.getOrDefault(key, 0);
            int value2 = district2Overall.getOrDefault(key, 0);
            double diff = Math.abs((double)(value1 - value2));
            differences.put(key, diff);
        }

        List<Map.Entry<String, Double>> sortedDiffs = new ArrayList<>(differences.entrySet());
        sortedDiffs.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        TopDifferencesDTO topDifferences = new TopDifferencesDTO();
        if (!sortedDiffs.isEmpty()) {
            setDifferenceItems(topDifferences, sortedDiffs, district1Overall, district2Overall);
        }

        return topDifferences;
    }

    private void setDifferenceItems(TopDifferencesDTO topDifferences,
                                    List<Map.Entry<String, Double>> sortedDiffs,
                                    Map<String, Integer> district1Overall,
                                    Map<String, Integer> district2Overall) {
        if (sortedDiffs.size() >= 1) {
            String key = sortedDiffs.get(0).getKey();
            topDifferences.setKey1(createDifferenceItem(key, district1Overall, district2Overall));
        }
        if (sortedDiffs.size() >= 2) {
            String key = sortedDiffs.get(1).getKey();
            topDifferences.setKey2(createDifferenceItem(key, district1Overall, district2Overall));
        }
        if (sortedDiffs.size() >= 3) {
            String key = sortedDiffs.get(2).getKey();
            topDifferences.setKey3(createDifferenceItem(key, district1Overall, district2Overall));
        }
    }

    private DifferenceItemDTO createDifferenceItem(String key,
                                                   Map<String, Integer> district1Overall,
                                                   Map<String, Integer> district2Overall) {
        return new DifferenceItemDTO(
                keyToKoreanMap.getOrDefault(key, key),
                district1Overall.getOrDefault(key, 0),
                district2Overall.getOrDefault(key, 0)
        );
    }

    private RadarDataDTO<ComparisonRadarDistrictInfoDTO> convertToComparisonDTO(
            RadarDataDTO<AnalysisRadarDistrictInfoDTO> analysisDTO) {
        if (analysisDTO == null) {
            log.error("Analysis DTO is null");
            return null;
        }

        RadarDataDTO<ComparisonRadarDistrictInfoDTO> comparisonDTO = new RadarDataDTO<>();

        ComparisonRadarDistrictInfoDTO districtInfo = new ComparisonRadarDistrictInfoDTO();
        if (analysisDTO.getDistrictInfo() != null) {
            districtInfo.setDistrictName(analysisDTO.getDistrictInfo().getDistrictName());
            districtInfo.setClusterName(analysisDTO.getDistrictInfo().getClusterName());
        }

        comparisonDTO.setDistrictInfo(districtInfo);
        comparisonDTO.setOverallData(analysisDTO.getOverallData() != null ?
                analysisDTO.getOverallData() : new HashMap<>());
        comparisonDTO.setTopTwo(analysisDTO.getTopTwo());

        return comparisonDTO;
    }
}
