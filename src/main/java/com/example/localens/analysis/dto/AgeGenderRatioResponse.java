package com.example.localens.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class AgeGenderRatioResponse {
    private Map<String, Map<String, Double>> 연령대별_체류패턴;
}
