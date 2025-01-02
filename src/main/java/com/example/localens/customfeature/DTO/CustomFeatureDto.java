package com.example.localens.customfeature.DTO;

import lombok.Getter;

@Getter
public class CustomFeatureDto {
    private String featureUuid;
    private String featureName;
    private String formula;

    public CustomFeatureDto(String featureUuid, String featureName, String formula) {
        this.featureUuid = featureUuid;
        this.featureName = featureName;
        this.formula = formula;
    }
}
