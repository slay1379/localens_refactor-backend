package com.example.localens.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class StayVisitRatioResponse {
    private Map<String, Double> 시간대별_체류방문비율;
}
