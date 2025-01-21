package com.example.localens.analysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("Teenagers")
    private GenderDataDTO Teenagers;

    @JsonProperty("Twenties")
    private GenderDataDTO Twenties;

    @JsonProperty("Thirties")
    private GenderDataDTO Thirties;

    @JsonProperty("Forties")
    private GenderDataDTO Forties;

    @JsonProperty("Fifties")
    private GenderDataDTO Fifties;

    @JsonProperty("Sixties")
    private GenderDataDTO Sixties;


    public static AgeGroupStayPatternDTO from(Map<String, Map<String, Double>> ageMap) {
        if (ageMap == null) ageMap = Map.of();

        AgeGroupStayPatternDTO dto = new AgeGroupStayPatternDTO();

        // 10대 미만 + 10대 합산
        dto.setTeenagers(
                mergeGenderData(
                        convertToGenderData(ageMap.getOrDefault("10대 미만", null)),
                        convertToGenderData(ageMap.getOrDefault("10대", null))
                )
        );

        // 20대
        dto.setTwenties(
                convertToGenderData(ageMap.getOrDefault("20대", null))
        );

        // 30대
        dto.setThirties(
                convertToGenderData(ageMap.getOrDefault("30대", null))
        );

        // 40대
        dto.setForties(
                convertToGenderData(ageMap.getOrDefault("40대", null))
        );

        // 50대
        dto.setFifties(
                convertToGenderData(ageMap.getOrDefault("50대", null))
        );

        // 60대 + 70대 이상 합산
        dto.setSixties(
                mergeGenderData(
                        convertToGenderData(ageMap.getOrDefault("60대", null)),
                        convertToGenderData(ageMap.getOrDefault("70대 이상", null))
                )
        );

        return dto;
    }

    private static GenderDataDTO mergeGenderData(GenderDataDTO data1, GenderDataDTO data2) {
        return new GenderDataDTO(
                data1.getFEMALE() + data2.getFEMALE(),
                data1.getMALE() + data2.getMALE()
        );
    }

    private static GenderDataDTO convertToGenderData(Map<String, Double> rawGender) {
        if (rawGender == null) rawGender = Map.of();

        double femaleVal = rawGender.getOrDefault("female", 0.0);
        double maleVal = rawGender.getOrDefault("male", 0.0);

        return new GenderDataDTO(femaleVal, maleVal);
    }
}
