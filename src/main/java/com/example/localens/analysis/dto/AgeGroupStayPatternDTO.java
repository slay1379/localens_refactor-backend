package com.example.localens.analysis.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgeGroupStayPatternDTO {
    private GenderDataDTO TeenagersBelow;
    private GenderDataDTO Teenagers;
    private GenderDataDTO Twenties;
    private GenderDataDTO Thirties;
    private GenderDataDTO Forties;
    private GenderDataDTO Fifties;
    private GenderDataDTO Sixties;
    private GenderDataDTO SeventiesAbove;

    // Factory method to map JSON data to AgeGroupStayPatternDTO
    public static AgeGroupStayPatternDTO from(Map<String, Map<String, Double>> ageGroupData) {
        AgeGroupStayPatternDTO dto = new AgeGroupStayPatternDTO();

        dto.setTeenagersBelow(convertToGenderData(ageGroupData.getOrDefault("10대 미만", null)));
        dto.setTeenagers(convertToGenderData(ageGroupData.getOrDefault("10대", null)));
        dto.setTwenties(convertToGenderData(ageGroupData.getOrDefault("20대", null)));
        dto.setThirties(convertToGenderData(ageGroupData.getOrDefault("30대", null)));
        dto.setForties(convertToGenderData(ageGroupData.getOrDefault("40대", null)));
        dto.setFifties(convertToGenderData(ageGroupData.getOrDefault("50대", null)));
        dto.setSixties(convertToGenderData(ageGroupData.getOrDefault("60대", null)));
        dto.setSeventiesAbove(convertToGenderData(ageGroupData.getOrDefault("70대 이상", null)));

        return dto;
    }

    // Helper method to convert Map<String, Double> to GenderDataDTO
    private static GenderDataDTO convertToGenderData(Map<String, Double> data) {
        double femaleValue = (data != null && data.containsKey("female")) ? data.get("female") : 0.0;
        double maleValue = (data != null && data.containsKey("male")) ? data.get("male") : 0.0;

        return new GenderDataDTO(femaleValue, maleValue);
    }
}