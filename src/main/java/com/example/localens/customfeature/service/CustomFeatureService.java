package com.example.localens.customfeature.service;

import com.example.localens.analysis.dto.AnalysisRadarDistrictInfoDTO;
import com.example.localens.analysis.dto.RadarDataDTO;
import com.example.localens.analysis.service.PopulationDetailsService;
import com.example.localens.analysis.service.RadarAnalysisService;
import com.example.localens.customfeature.DTO.CustomFeatureValueDTO;
import com.example.localens.customfeature.DTO.DistrictResponseDTO;
import com.example.localens.customfeature.domain.CustomFeature;
import com.example.localens.customfeature.domain.CustomFeatureCalculationResult;
import com.example.localens.customfeature.domain.DistrictComparisonResult;
import com.example.localens.customfeature.exception.NotFoundException;
import com.example.localens.customfeature.exception.UnauthorizedException;
import com.example.localens.customfeature.repository.CustomFeatureRepository;
import com.example.localens.customfeature.util.FormulaEvaluator;
import com.example.localens.influx.InfluxDBService;
import com.example.localens.member.domain.Member;
import com.example.localens.member.repository.MemberRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Response;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomFeatureService {
    private final CustomFeatureRepository customFeatureRepository;
    private final MemberRepository memberRepository;
    private final FormulaEvaluator formulaEvaluator;
    private final RadarAnalysisService radarAnalysisService;
    private final PopulationDetailsService populationDetailsService;

    public CustomFeatureCalculationResult calculateFeature(
            String formula,
            Integer districtUuid1,
            Integer districtUuid2,
            UUID userUuid,
            String featureName) {

        Member member = getMemberOrThrow(userUuid);

        Map<String, Object> data1 = getDistrictData(districtUuid1);
        Map<String, Object> data2 = getDistrictData(districtUuid2);

        double result1 = formulaEvaluator.evaluate(formula, data1);
        double result2 = formulaEvaluator.evaluate(formula, data2);

        saveCustomFeature(formula, featureName, member);

        return new CustomFeatureCalculationResult(result1, result2);
    }

    public DistrictComparisonResult compareDistricts(
            String customFeatureUuid,
            Integer districtUuid1,
            Integer districtUuid2) {

        CustomFeature feature = getFeatureOrThrow(customFeatureUuid);

        Map<String, Object> data1 = getDistrictData(districtUuid1);
        Map<String, Object> data2 = getDistrictData(districtUuid2);

        double result1 = formulaEvaluator.evaluate(feature.getFormula(), data1);
        double result2 = formulaEvaluator.evaluate(feature.getFormula(), data2);

        List<Integer> normalizedResults = normalizeResults((int)result1, (int)result2);

        return new DistrictComparisonResult(
                buildDistrictResponse(districtUuid1, data1, feature, normalizedResults.get(0)),
                buildDistrictResponse(districtUuid2, data2, feature, normalizedResults.get(1))
        );
    }

    public List<CustomFeature> getFeaturesByUser(UUID userUuid) {
        Member member = getMemberOrThrow(userUuid);
        return customFeatureRepository.findByMember(member);
    }

    public Response<?> deleteFeatureIfOwner(String featureId, UUID userUuid) {
        CustomFeature feature = getFeatureOrThrow(featureId);
        if (!feature.getMember().getMemberUuid().equals(userUuid)) {
            throw new UnauthorizedException("Not authorized to delete this feature");
        }
        customFeatureRepository.deleteById(featureId);
        return Response.success("Feature deleted successfully");
    }

    private Member getMemberOrThrow(UUID userUuid) {
        return memberRepository.findById(userUuid)
                .orElseThrow(() -> new NotFoundException("Member not found"));
    }

    private CustomFeature getFeatureOrThrow(String featureId) {
        return customFeatureRepository.findById(featureId)
                .orElseThrow(() -> new NotFoundException("Custom feature not found"));
    }

    private Map<String, Object> getDistrictData(Integer districtUuid) {
        RadarDataDTO<AnalysisRadarDistrictInfoDTO> radarData = radarAnalysisService.getRadarData(districtUuid);
        if (radarData == null) {
            throw new IllegalArgumentException("Failed to get radar data for district: " + districtUuid);
        }

        Map<String, Integer> overallData = radarData.getOverallData();
        if (overallData == null) {
            log.error("Overall data is null for district {}", districtUuid);
            overallData = new HashMap<>();
        }

        return buildOverallData(overallData);
    }

    private Map<String, Object> buildOverallData(Map<String, Integer> overallData) {
        Map<String, Object> result = new LinkedHashMap<>();

        try{
            log.info("Building overall data from details: {}", overallData);

            result.put("population", overallData.getOrDefault("population", 0));
            result.put("stayVisit", overallData.getOrDefault("stayVisit", 0));
            result.put("congestion", overallData.getOrDefault("congestion", 0));
            result.put("stayPerVisitor", overallData.getOrDefault("stayPerVisitor", 0));
            result.put("visitConcentration", overallData.getOrDefault("visitConcentration", 0));
            result.put("stayTimeChange", overallData.getOrDefault("stayTimeChange", 0));

        }catch (Exception e){
            log.error("Error building overall data: {}", e.getMessage());
            throw e;
        }

        return result;
    }

    private double calculateMetric(Map<String, Object> details, String key) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Double> hourlyData = (Map<String, Double>) details.get(key);
            if (hourlyData == null || hourlyData.isEmpty()) {
                log.warn("No data found for metric: {}", key);
                return 0.0;
            }
            double average = hourlyData.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            return average * 100;
        } catch (Exception e) {
            log.error("Error calculating metric {}: {}", key, e.getMessage());
            return 0.0;
        }
    }

    private CustomFeature saveCustomFeature(String formula, String featureName, Member member) {
        CustomFeature customFeature = new CustomFeature();
        customFeature.setFormula(formula);
        customFeature.setFeatureName(featureName);
        customFeature.setMember(member);

        return customFeatureRepository.save(customFeature);
    }

    private List<Integer> normalizeResults(int result1, int result2) {
        List<Integer> results = new ArrayList<>();

        // 최대값 계산
        int max = Math.max(result1, result2);

        // 최대값이 100보다 큰 경우 값을 비례적으로 나눠 정규화
        if (max > 100) {
            double scaleFactor = 100.0 / max;
            result1 = (int) (result1 * scaleFactor);
            result2 = (int) (result2 * scaleFactor);
        }

        results.add(result1);
        results.add(result2);
        return results;
    }

    private DistrictResponseDTO buildDistrictResponse(
            Integer districtUuid,
            Map<String, Object> data,
            CustomFeature feature,
            int normalizedResult) {

        return new DistrictResponseDTO(
                // TODO: 실제 상권 이름을 가져오는 로직 구현 필요
                "상권 " + districtUuid,
                "클러스터 " + districtUuid,
                data,
                new CustomFeatureValueDTO(
                        feature.getFeatureName(),
                        normalizedResult
                )
        );
    }
}
