package com.example.localens.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class StayDurationChangeResponse {
    private Map<String, Double> 시간대별_평균_체류시간_변화율;
}
