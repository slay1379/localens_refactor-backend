package com.example.localens.analysis.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RadarComparisonService {

    // 영어 key를 한글로 매핑하는 Map
    private static final Map<String, String> keyToKoreanMap = Map.of(
            "population", "유동인구 수",
            "stayVisit", "체류/방문 비율",
            "congestion", "혼잡도 변화율",
            "stayPerVisitor", "체류시간 대비 방문자 수",
            "visitConcentration", "방문 집중도",
            "stayTimeChange", "체류시간 변화율"
    );

    public Map<String, String> findTopDifferences(Map<String, Integer> district1Overall, Map<String, Integer> district2Overall) {
        // 차이를 계산
        Map<String, Double> differences = new HashMap<>();
        for (String key : district1Overall.keySet()) {
            double diff = Math.abs(district1Overall.get(key) - district2Overall.get(key));
            differences.put(key, diff);
        }

        // 차이를 기준으로 내림차순 정렬
        List<Map.Entry<String, Double>> sortedDifferences = new ArrayList<>(differences.entrySet());
        sortedDifferences.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // 가장 큰 차이와 두 번째로 큰 차이를 한글로 변환하여 반환
        Map<String, String> result = new LinkedHashMap<>();
        result.put("first", keyToKoreanMap.getOrDefault(sortedDifferences.get(0).getKey(), sortedDifferences.get(0).getKey()));
        result.put("second", keyToKoreanMap.getOrDefault(sortedDifferences.get(1).getKey(), sortedDifferences.get(1).getKey()));

        return result;
    }

    public Map<String, Object> constructDistrictData(
            Integer districtUuid,
            RadarFloatingPopulationService floatingPopulationService,
            RadarStayVisitRatioService stayVisitRatioService,
            RadarCongestionRateService congestionRateService,
            RadarStayPerVisitorService stayPerVisitorService,
            RadarVisitConcentrationService visitConcentrationService,
            RadarStayDurationChangeService stayDurationChangeService,
            RadarInfoService infoService
    ) {
        // 상권 정보 조회
        var commercialDistrict = infoService.getCommercialDistrictByUuid(districtUuid);

        // 각 서비스 호출 결과를 변수에 저장
        Map<String, Integer> overallData = new LinkedHashMap<>();
        overallData.put("population", (int)(floatingPopulationService.getNormalizedFloatingPopulation(districtUuid).get유동인구_수() * 100));
        overallData.put("stayVisit", (int)(stayVisitRatioService.getStayVisitRatioByDistrictUuid(districtUuid).get체류_방문_비율() * 100));
        overallData.put("congestion", (int)(congestionRateService.getCongestionRateByDistrictUuid(districtUuid).get혼잡도_변화율() * 100));
        overallData.put("stayPerVisitor", (int)(stayPerVisitorService.getStayPerVisitorByDistrictUuid(districtUuid).get체류시간_대비_방문자_수() * 100));
        overallData.put("visitConcentration", (int)(visitConcentrationService.getVisitConcentrationByDistrictUuid(districtUuid).get방문_집중도() * 100));
        overallData.put("stayTimeChange", (int)(stayDurationChangeService.calculateAvgStayTimeChangeRate(districtUuid).get평균_체류시간_변화율() * 100));

        // 상권 및 클러스터 정보 추가
        Map<String, Object> districtInfo = new LinkedHashMap<>();
        districtInfo.put("districtName", commercialDistrict.getDistrictName());
        districtInfo.put("clusterName", commercialDistrict.getCluster().getClusterName());
        districtInfo.put("latitude", commercialDistrict.getLatitude());
        districtInfo.put("longitude", commercialDistrict.getLongitude());

        // 최종 반환 데이터
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("districtInfo", districtInfo);
        response.put("overallData", overallData);

        return response;
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
