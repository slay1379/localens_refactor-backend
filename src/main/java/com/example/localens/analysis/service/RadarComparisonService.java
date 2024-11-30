package com.example.localens.analysis.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RadarComparisonService {

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

        // 가장 큰 차이와 두 번째로 큰 차이 반환
        Map<String, String> result = new LinkedHashMap<>();
        result.put("first", sortedDifferences.get(0).getKey());
        result.put("second", sortedDifferences.get(1).getKey());

        return result;
    }

    public Map<String, Object> constructDistrictData(
            Integer districtUuid,
            RadarFloatingPopulationService floatingPopulationService,
            RadarStayVisitRatioService stayVisitRatioService,
            RadarCongestionRateService congestionRateService,
            RadarStayPerVisitorService stayPerVisitorService,
            RadarStayDurationChangeService stayDurationChangeService,
            RadarInfoService infoService
    ) {
        // 상권 정보 조회
        var commercialDistrict = infoService.getCommercialDistrictByUuid(districtUuid);

        // 각 서비스 호출 결과를 변수에 저장
        Map<String, Integer> overallData = new LinkedHashMap<>();
        overallData.put("유동인구_수", (int)(floatingPopulationService.getNormalizedFloatingPopulation(districtUuid).get유동인구_수() * 100));
        overallData.put("체류_방문_비율", (int)(stayVisitRatioService.getStayVisitRatioByDistrictUuid(districtUuid).get체류_방문_비율() * 100));
        overallData.put("혼잡도_변화율", (int)(congestionRateService.getCongestionRateByDistrictUuid(districtUuid).get혼잡도_변화율() * 100));
        overallData.put("체류시간_대비_방문자_수", (int)(stayPerVisitorService.getStayPerVisitorByDistrictUuid(districtUuid).get체류시간_대비_방문자_수() * 100));
        overallData.put("평균_체류시간_변화율", (int)(stayDurationChangeService.calculateAvgStayTimeChangeRate(districtUuid).get평균_체류시간_변화율() * 100));

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
}
