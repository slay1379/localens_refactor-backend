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

    public static NationalityPatternDTO from(Map<String, Double> rawNat) {
        if (rawNat == null) rawNat = Map.of();

        double foreigner = rawNat.getOrDefault("장기체류외국인", 0.0);
        double local = rawNat.getOrDefault("내국인", 0.0);

        return new NationalityPatternDTO(foreigner, local);
    }
}
