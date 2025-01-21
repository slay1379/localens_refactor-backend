package com.example.localens.analysis.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgeGroupStayPatternDTO {
    private GenderDataDTO TeenagersBelow;   // "10대 미만"
    private GenderDataDTO Teenagers;        // "10대"
    private GenderDataDTO Twenties;         // "20대"
    private GenderDataDTO Thirties;         // "30대"
    private GenderDataDTO Forties;          // "40대"
    private GenderDataDTO Fifties;          // "50대"
    private GenderDataDTO Sixties;          // "60대"
    private GenderDataDTO SeventiesAbove;   // "70대 이상"

    public static AgeGroupStayPatternDTO from(Map<String, Map<String, Double>> ageMap) {
        if (ageMap == null) ageMap = Map.of(); // 안전 처리

        AgeGroupStayPatternDTO dto = new AgeGroupStayPatternDTO();
        dto.setTeenagersBelow(
                convertToGenderData(ageMap.getOrDefault("10대 미만", null))
        );
        dto.setTeenagers(
                convertToGenderData(ageMap.getOrDefault("10대", null))
        );
        dto.setTwenties(
                convertToGenderData(ageMap.getOrDefault("20대", null))
        );
        dto.setThirties(
                convertToGenderData(ageMap.getOrDefault("30대", null))
        );
        dto.setForties(
                convertToGenderData(ageMap.getOrDefault("40대", null))
        );
        dto.setFifties(
                convertToGenderData(ageMap.getOrDefault("50대", null))
        );
        dto.setSixties(
                convertToGenderData(ageMap.getOrDefault("60대", null))
        );
        dto.setSeventiesAbove(
                convertToGenderData(ageMap.getOrDefault("70대 이상", null))
        );
        return dto;
    }

    // "female" -> FEMALE, "male" -> MALE
    private static GenderDataDTO convertToGenderData(Map<String, Double> rawGender) {
        if (rawGender == null) rawGender = Map.of();

        double femaleVal = rawGender.getOrDefault("female", 0.0);
        double maleVal = rawGender.getOrDefault("male", 0.0);

        return new GenderDataDTO(femaleVal, maleVal);
    }
}
