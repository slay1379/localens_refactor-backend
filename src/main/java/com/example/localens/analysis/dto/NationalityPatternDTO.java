package com.example.localens.analysis.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NationalityPatternDTO {
    private double Foreigner;
    private double Local;

    public static NationalityPatternDTO from(Map<String, Double> nationalityData) {
        return new NationalityPatternDTO(
                nationalityData.getOrDefault("장기체류외국인", 0.0),
                nationalityData.getOrDefault("내국인", 0.0)
        );
    }
}
