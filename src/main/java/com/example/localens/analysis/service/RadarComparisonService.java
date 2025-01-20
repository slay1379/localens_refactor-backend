package com.example.localens.analysis.service;

import com.example.localens.analysis.dto.CompareTwoDistrictsDTO;
import com.example.localens.analysis.dto.DifferenceItemDTO;
import com.example.localens.analysis.dto.RadarDataDTO;
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
        RadarDataDTO district1Radar = radarAnalysisService.getRadarData(districtUuid1);
        RadarDataDTO district2Radar = radarAnalysisService.getRadarData(districtUuid2);

        Map<String, Integer> district1Overall = district1Radar.getOverallData();
        Map<String, Integer> district2Overall = district2Radar.getOverallData();

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

        CompareTwoDistrictsDTO result = new CompareTwoDistrictsDTO();
        result.setDistrict1(district1Radar);
        result.setDistrict1(district2Radar);
        result.setTopDifferences(topDifferences);
        
        return result;
    }

    private DifferenceItemDTO createDifferenceItem(String key, Integer value1, Integer value2, Map<String, String> keyToKoreanMap) {
        DifferenceItemDTO item = new DifferenceItemDTO();
        item.setName(keyToKoreanMap.getOrDefault(key, key));
        item.setValue1(value1);
        item.setValue2(value2);
        return item;
    }

    public Map<String, Map<String, Object>> findTopDifferences(Map<String, Integer> district1Overall, Map<String, Integer> district2Overall) {
        // 차이를 계산
        Map<String, Double> differences = new HashMap<>();
        for (String key : district1Overall.keySet()) {
            double diff = Math.abs(district1Overall.get(key) - district2Overall.get(key));
            differences.put(key, diff);
        }

        // 차이를 기준으로 내림차순 정렬, 값이 같으면 keyToKoreanMap의 순서를 기준으로 정렬
        List<Map.Entry<String, Double>> sortedDifferences = new ArrayList<>(differences.entrySet());
        sortedDifferences.sort((a, b) -> {
            int compareDiff = Double.compare(b.getValue(), a.getValue());
            if (compareDiff != 0) {
                return compareDiff; // 차이가 다르면 내림차순 정렬
            }
            // 차이가 같으면 keyToKoreanMap의 순서에 따라 정렬
            List<String> keyOrder = new ArrayList<>(keyToKoreanMap.keySet());
            return Integer.compare(keyOrder.indexOf(a.getKey()), keyOrder.indexOf(b.getKey()));
        });

        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (int i = 0; i < 3 && i < sortedDifferences.size(); i++) {
            Map.Entry<String, Double> entry = sortedDifferences.get(i);
            String key = entry.getKey();

            Map<String, Object> differenceMap = new LinkedHashMap<>();
            differenceMap.put("name", keyToKoreanMap.getOrDefault(key, key));
            differenceMap.put("value1", district1Overall.get(key));
            differenceMap.put("value2", district2Overall.get(key));

            result.put("key" + (i + 1), differenceMap);
        }

//        // 상위 3개의 차이를 keyToKoreanMap에서 한글로 변환하여 반환
//        Map<String, String> result = new LinkedHashMap<>();
//        result.put("first", keyToKoreanMap.getOrDefault(sortedDifferences.get(0).getKey(), sortedDifferences.get(0).getKey()));
//        result.put("second", keyToKoreanMap.getOrDefault(sortedDifferences.get(1).getKey(), sortedDifferences.get(1).getKey()));
//        result.put("third", keyToKoreanMap.getOrDefault(sortedDifferences.get(2).getKey(), sortedDifferences.get(2).getKey()));

        return result;
    }

    public Map<String, Object> findTopTwo(Map<String, Integer> overallData) {
        // 데이터를 차이값 기준으로 정렬
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(overallData.entrySet());
        sortedEntries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        // 상위 2개의 데이터를 keyToKoreanMap으로 변환
        Map<String, Object> topTwo = new LinkedHashMap<>();
        topTwo.put("first", Map.of(
                "name", keyToKoreanMap.getOrDefault(sortedEntries.get(0).getKey(), sortedEntries.get(0).getKey()),
                "value", sortedEntries.get(0).getValue()
        ));
        topTwo.put("second", Map.of(
                "name", keyToKoreanMap.getOrDefault(sortedEntries.get(1).getKey(), sortedEntries.get(1).getKey()),
                "value", sortedEntries.get(1).getValue()
        ));

        return topTwo;
    }

    public Map<String, Map<String, Integer>> calculateArrangedData(Map<String, Integer> district1Overall, Map<String, Integer> district2Overall) {
        // 각 키별로 차이를 계산하여 정렬
        List<String> sortedKeys = district1Overall.keySet().stream()
                .sorted((key1, key2) -> {
                    int diff1 = district1Overall.get(key1) - district2Overall.get(key1);
                    int diff2 = district1Overall.get(key2) - district2Overall.get(key2);
                    return Integer.compare(diff2, diff1); // 차이값 기준으로 내림차순 정렬
                })
                .toList();

        // 정렬된 데이터를 사용해 arrangedData 생성
        Map<String, Integer> district1ArrangedData = new LinkedHashMap<>();
        Map<String, Integer> district2ArrangedData = new LinkedHashMap<>();
        for (String key : sortedKeys) {
            district1ArrangedData.put(key, district1Overall.get(key));
            district2ArrangedData.put(key, district2Overall.get(key));
        }

        // 두 상권의 arrangedData를 반환
        Map<String, Map<String, Integer>> arrangedData = new HashMap<>();
        arrangedData.put("district1", district1ArrangedData);
        arrangedData.put("district2", district2ArrangedData);

        return arrangedData;
    }
}
