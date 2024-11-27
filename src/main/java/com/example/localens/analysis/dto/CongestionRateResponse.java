package com.example.localens.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class CongestionRateResponse {
    private Map<String, Double> 시간대별_혼잡도_변화율;
}
