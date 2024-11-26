package com.example.localens.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class TimeZonePopulationRatioResponse {
    private Map<String, Double> timeZoneRatios; // 시간대별 유동인구 비율
}
