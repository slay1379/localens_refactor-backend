package com.example.localens.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class AvgStayTimeChangeRateResponse {
    private Map<String, Double> avgStayTimeChangeRateResponse;
}
