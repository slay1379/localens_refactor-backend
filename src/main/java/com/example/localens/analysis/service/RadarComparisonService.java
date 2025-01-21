package com.example.localens.analysis.service;

import com.example.localens.analysis.dto.AnalysisRadarDistrictInfoDTO;
import com.example.localens.analysis.dto.CompareTwoDistrictsDTO;
import com.example.localens.analysis.dto.ComparisonRadarDistrictInfoDTO;
import com.example.localens.analysis.dto.DifferenceItemDTO;
import com.example.localens.analysis.dto.RadarDataDTO;
import com.example.localens.analysis.dto.RadarDistrictInfoDTO;
import com.example.localens.analysis.dto.TopDifferencesDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RadarComparisonService {

    private final RadarAnalysisService radarAnalysisService;

    private static final LinkedHashMap<String, String> keyToKoreanMap = new LinkedHashMap<>() {{
        put("population", "유동인구 수");
        put("stayVisit", "체류/방문 비율");
        put("congestion", "혼잡도 변화율");
        put("stayPerVisitor", "체류시간 대비 방문자 수");
        put("visitConcentration", "방문 집중도");
        put("stayTimeChange", "체류시간 변화율");
    }};

    public CompareTwoDistrictsDTO compareTwoDistricts(Integer districtUuid1, Integer districtUuid2) {
        RadarDataDTO<ComparisonRadarDistrictInfoDTO> district1Radar = convertToComparisonDTO(
                radarAnalysisService.getRadarData(districtUuid1));
        RadarDataDTO<ComparisonRadarDistrictInfoDTO> district2Radar = convertToComparisonDTO(
                radarAnalysisService.getRadarData(districtUuid2));

        Map<String, Integer> district1Overall = district1Radar.getOverallData();
        Map<String, Integer> district2Overall = district2Radar.getOverallData();

        TopDifferencesDTO topDifferences = calculateTopDifferences(district1Overall, district2Overall);

        CompareTwoDistrictsDTO result = new CompareTwoDistrictsDTO();
        result.setDistrict1(district1Radar);
        result.setDistrict2(district2Radar);
        result.setTopDifferences(topDifferences);

        return result;
    }

    private TopDifferencesDTO calculateTopDifferences(Map<String, Integer> district1Overall,
                                                      Map<String, Integer> district2Overall) {
        Map<String, Double> differences = new HashMap<>();
        for (String key : district1Overall.keySet()) {
            double diff = Math.abs(district1Overall.get(key) - district2Overall.get(key));
            differences.put(key, diff);
        }

        List<Map.Entry<String, Double>> sortedDiffs = new ArrayList<>(differences.entrySet());
        sortedDiffs.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        TopDifferencesDTO topDifferences = new TopDifferencesDTO();

        if (sortedDiffs.size() >= 1) {
            String key = sortedDiffs.get(0).getKey();
            topDifferences.setKey1(createDifferenceItem(
                    key,
                    district1Overall.get(key),
                    district2Overall.get(key),
                    keyToKoreanMap
            ));
        }

        if (sortedDiffs.size() >= 2) {
            String key = sortedDiffs.get(1).getKey();
            topDifferences.setKey2(createDifferenceItem(
                    key,
                    district1Overall.get(key),
                    district2Overall.get(key),
                    keyToKoreanMap
            ));
        }

        if (sortedDiffs.size() >= 3) {
            String key = sortedDiffs.get(2).getKey();
            topDifferences.setKey3(createDifferenceItem(
                    key,
                    district1Overall.get(key),
                    district2Overall.get(key),
                    keyToKoreanMap
            ));
        }

        return topDifferences;
    }

    private DifferenceItemDTO createDifferenceItem(String key, Integer value1, Integer value2,
                                                   Map<String, String> keyToKoreanMap) {
        DifferenceItemDTO item = new DifferenceItemDTO();
        item.setName(keyToKoreanMap.getOrDefault(key, key));
        item.setValue1(value1);
        item.setValue2(value2);
        return item;
    }

    private RadarDataDTO<ComparisonRadarDistrictInfoDTO> convertToComparisonDTO(
            RadarDataDTO<AnalysisRadarDistrictInfoDTO> analysisDTO) {
        RadarDataDTO<ComparisonRadarDistrictInfoDTO> comparisonDTO = new RadarDataDTO<>();

        ComparisonRadarDistrictInfoDTO districtInfo = new ComparisonRadarDistrictInfoDTO();
        districtInfo.setDistrictName(analysisDTO.getDistrictInfo().getDistrictName());
        districtInfo.setClusterName(analysisDTO.getDistrictInfo().getClusterName());

        comparisonDTO.setDistrictInfo(districtInfo);
        comparisonDTO.setOverallData(analysisDTO.getOverallData());
        comparisonDTO.setTopTwo(analysisDTO.getTopTwo());

        return comparisonDTO;
    }

    public Map<String, Map<String, Integer>> calculateArrangedData(Map<String, Integer> district1Overall,
                                                                   Map<String, Integer> district2Overall) {
        List<String> sortedKeys = district1Overall.keySet().stream()
                .sorted((key1, key2) -> {
                    int diff1 = district1Overall.get(key1) - district2Overall.get(key1);
                    int diff2 = district1Overall.get(key2) - district2Overall.get(key2);
                    return Integer.compare(diff2, diff1);
                })
                .toList();

        Map<String, Integer> district1ArrangedData = new LinkedHashMap<>();
        Map<String, Integer> district2ArrangedData = new LinkedHashMap<>();
        for (String key : sortedKeys) {
            district1ArrangedData.put(key, district1Overall.get(key));
            district2ArrangedData.put(key, district2Overall.get(key));
        }

        Map<String, Map<String, Integer>> arrangedData = new HashMap<>();
        arrangedData.put("district1", district1ArrangedData);
        arrangedData.put("district2", district2ArrangedData);

        return arrangedData;
    }
}
