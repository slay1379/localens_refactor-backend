package com.example.localens.customfeature.domain;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomFeatureCalculationResult {
    private final double district1Result;
    private final double district2Result;

    public Map<String, Double> toMap() {
        return Map.of(
                "district1_result", district1Result,
                "district2_result", district2Result
        );
    }
}
