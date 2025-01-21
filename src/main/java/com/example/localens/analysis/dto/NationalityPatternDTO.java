package com.example.localens.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NationalityPatternDTO {
    @JsonProperty("Foreigner")
    private double Foreigner;

    @JsonProperty("Local")
    private double Local;

    public static NationalityPatternDTO from(Map<String, Double> rawNat) {
        if (rawNat == null) rawNat = Map.of();

        double foreigner = rawNat.getOrDefault("foreigner", 0.0);
        double local = rawNat.getOrDefault("local", 0.0);

        return new NationalityPatternDTO(foreigner, local);
    }

    // 순서를 명시적으로 설정
    public Map<String, Double> toOrderedMap() {
        Map<String, Double> orderedMap = new LinkedHashMap<>();
        orderedMap.put("Foreigner", this.Foreigner); // Foreigner 먼저 추가
        orderedMap.put("Local", this.Local);        // Local 다음에 추가
        return orderedMap;
    }
}
