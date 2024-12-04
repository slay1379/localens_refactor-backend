package com.example.localens.customfeature.DTO;

import lombok.Getter;

@Getter
public class CustomFeatureDto {
    private String featureName;
    private String formula;

    public CustomFeatureDto(String featureName, String formula) {
        this.featureName = featureName;
        this.formula = formula;
    }
}
