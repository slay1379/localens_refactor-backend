package com.example.localens.customfeature.controller;

import com.example.localens.analysis.service.PopulationDetailsService;
import com.example.localens.customfeature.DTO.CustomFeatureDTO;
import com.example.localens.customfeature.domain.ApiResponse;
import com.example.localens.customfeature.domain.CustomFeatureCalculationRequest;
import com.example.localens.customfeature.domain.CustomFeatureCalculationResult;
import com.example.localens.customfeature.domain.DistrictComparisonResult;
import com.example.localens.customfeature.service.CustomFeatureService;
import com.example.localens.customfeature.util.CustomFeatureMapper;
import com.example.localens.customfeature.util.TokenValidator;

import com.example.localens.customfeature.DTO.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.util.*;

import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customFeatures")
@RequiredArgsConstructor
public class CustomFeatureController {
    private static final Map<String, String> FIELD_MAPPING = Map.of(
            "유동인구 수", "population",
            "체류 방문 비율", "stayVisit",
            "혼잡도 변화율", "congestion",
            "체류시간 대비 방문자 수", "stayPerVisitor",
            "방문 집중도", "visitConcentration",
            "평균 체류시간 변화율", "stayTimeChange"
    );

    private final CustomFeatureService customFeatureService;
    private final TokenValidator tokenValidator;
    private final CustomFeatureMapper customFeatureMapper;
    private final PopulationDetailsService populationDetailsService;

    @PostMapping("/calculateAndCreate/{districtUuid1}/{districtUuid2}")
    public ResponseEntity<ApiResponse<Map<String, Double>>> calculateCustomFeature(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody CustomFeatureCalculationRequest request,
            @PathVariable Integer districtUuid1,
            @PathVariable Integer districtUuid2) {

        UUID userUuid = tokenValidator.validateAndGetUserUuid(authorizationHeader);

        String translatedFormula = translateFormula(request.getFormula());

        try {
            CustomFeatureCalculationResult result = customFeatureService.calculateFeature(
                    translatedFormula,
                    districtUuid1,
                    districtUuid2,
                    userUuid,
                    request.getFeatureName()
            );

            return ResponseEntity.ok(ApiResponse.success(result.toMap()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Error calculating formula: " + e.getMessage()));
        }
    }

    @GetMapping("/compare/{districtUuid1}/{districtUuid2}/{customFeatureUuid}")
    public ResponseEntity<ApiResponse<Map<String, DistrictComparisonDTO>>> compareDistricts(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Integer districtUuid1,
            @PathVariable Integer districtUuid2,
            @PathVariable String customFeatureUuid) {

        tokenValidator.validateAndGetUserUuid(authorizationHeader);

        try {
            DistrictComparisonResult comparison = customFeatureService.compareDistricts(
                    customFeatureUuid,
                    districtUuid1,
                    districtUuid2
            );

            Map<String, DistrictComparisonDTO> comparisonDTO = Map.of(
                    "district1", toDto(comparison.getDistrict1()),
                    "district2", toDto(comparison.getDistrict2())
            );

            return ResponseEntity.ok(ApiResponse.success(comparisonDTO));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Error in comparison: " + e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<CustomFeatureDTO>>> listCustomFeatures(
            @RequestHeader("Authorization") String authorizationHeader) {

        UUID userUuid = tokenValidator.validateAndGetUserUuid(authorizationHeader);

        List<CustomFeatureDTO> features = customFeatureService.getFeaturesByUser(userUuid)
                .stream()
                .map(feature -> customFeatureMapper.toDTO(feature, FIELD_MAPPING))
                .toList();

        return ResponseEntity.ok(ApiResponse.success(features));
    }

    @DeleteMapping("/{customFeatureId}")
    public ResponseEntity<ApiResponse<String>> deleteCustomFeature(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String customFeatureId) {

        UUID userUuid = tokenValidator.validateAndGetUserUuid(authorizationHeader);

        customFeatureService.deleteFeatureIfOwner(customFeatureId, userUuid);
        return ResponseEntity.ok(ApiResponse.success("Feature deleted successfully"));
    }

    private String translateFormula(String formula) {
        String translatedFormula = formula;
        for (Map.Entry<String, String> entry : FIELD_MAPPING.entrySet()) {
            translatedFormula = translatedFormula.replace(entry.getKey(), entry.getValue());
        }
        return translatedFormula;
    }

    private DistrictComparisonDTO toDto(DistrictResponseDTO response) {
        return new DistrictComparisonDTO(
                response.getDistrictName(),
                response.getClusterName(),
                response.getOverallData(),
                new CustomFeatureValueDTO(
                        response.getCustomFeature().getName(),
                        response.getCustomFeature().getValue()
                )
        );
    }
}