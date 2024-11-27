package com.example.localens.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class NationalityRatioResponse {
    private Map<String, Double> nationalityRatios; // 내외국인 비율
}
