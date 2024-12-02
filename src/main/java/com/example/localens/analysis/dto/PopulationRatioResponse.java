package com.example.localens.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class PopulationRatioResponse {
    private Map<String, Double> 시간대별_유동인구수;
}

