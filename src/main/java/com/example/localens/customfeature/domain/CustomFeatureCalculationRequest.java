package com.example.localens.customfeature.domain;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomFeatureCalculationRequest {
    private String featureName;
    private String formula;
    private Map<String, Double> variables;
}
