package com.example.localens.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class NationalityRatioResponse {
    private Map<String, Double> 국적별_체류패턴;
}
