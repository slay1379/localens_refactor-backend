package com.example.localens.analysis.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class PopulationHourlyDataDTO {
    private double zero;
    private double one;
    private double two;
    private double three;
    private double four;
    private double five;
    private double six;
    private double seven;
    private double eight;
    private double nine;
    private double ten;
    private double eleven;
    private double twelve;
    private double thirteen;
    private double fourteen;
    private double fifteen;
    private double sixteen;
    private double seventeen;
    private double eighteen;
    private double nineteen;
    private double twenty;
    private double twentyOne;
    private double twentyTwo;
    private double twentyThree;

    // Map<String, Double> 형태 ( "0" -> 123.4, "1" -> 45.6, ... ) 를
    // 이 DTO로 변환하는 팩토리 메서드
    public static PopulationHourlyDataDTO from(Map<String, Double> rawMap) {
        if (rawMap == null) rawMap = Map.of(); // 안전 처리

        PopulationHourlyDataDTO dto = new PopulationHourlyDataDTO();
        dto.setZero(rawMap.getOrDefault("0", 0.0));
        dto.setOne(rawMap.getOrDefault("1", 0.0));
        dto.setTwo(rawMap.getOrDefault("2", 0.0));
        dto.setThree(rawMap.getOrDefault("3", 0.0));
        dto.setFour(rawMap.getOrDefault("4", 0.0));
        dto.setFive(rawMap.getOrDefault("5", 0.0));
        dto.setSix(rawMap.getOrDefault("6", 0.0));
        dto.setSeven(rawMap.getOrDefault("7", 0.0));
        dto.setEight(rawMap.getOrDefault("8", 0.0));
        dto.setNine(rawMap.getOrDefault("9", 0.0));
        dto.setTen(rawMap.getOrDefault("10", 0.0));
        dto.setEleven(rawMap.getOrDefault("11", 0.0));
        dto.setTwelve(rawMap.getOrDefault("12", 0.0));
        dto.setThirteen(rawMap.getOrDefault("13", 0.0));
        dto.setFourteen(rawMap.getOrDefault("14", 0.0));
        dto.setFifteen(rawMap.getOrDefault("15", 0.0));
        dto.setSixteen(rawMap.getOrDefault("16", 0.0));
        dto.setSeventeen(rawMap.getOrDefault("17", 0.0));
        dto.setEighteen(rawMap.getOrDefault("18", 0.0));
        dto.setNineteen(rawMap.getOrDefault("19", 0.0));
        dto.setTwenty(rawMap.getOrDefault("20", 0.0));
        dto.setTwentyOne(rawMap.getOrDefault("21", 0.0));
        dto.setTwentyTwo(rawMap.getOrDefault("22", 0.0));
        dto.setTwentyThree(rawMap.getOrDefault("23", 0.0));

        return dto;
    }
}
