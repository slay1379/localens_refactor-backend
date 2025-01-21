package com.example.localens.analysis.dto;

import java.util.LinkedHashMap;
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

        double foreigner = rawNat.getOrDefault("foreigner", 0.0);
        double local = rawNat.getOrDefault("local", 0.0);

        return new NationalityPatternDTO(foreigner, local);
    }

    public Map<String, Double> toOrderedMap() {
        Map<String, Double> orderedMap = new LinkedHashMap<>();
        orderedMap.put("Foreigner", this.Foreigner);
        orderedMap.put("Local", this.Local);
        return orderedMap;
    }
}
