package com.example.localens.customfeature.DTO;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DistrictResponseDTO {
    private final String districtName;
    private final String clusterName;
    private Map<String, Object> overallData;
    private final CustomFeatureValueDTO customFeature;
}
