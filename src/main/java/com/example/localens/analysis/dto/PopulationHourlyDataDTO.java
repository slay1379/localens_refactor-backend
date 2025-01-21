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

    public static PopulationHourlyDataDTO from(Map<String, Double> hourlyData) {
        PopulationHourlyDataDTO dto = new PopulationHourlyDataDTO();
        dto.setZero(hourlyData.getOrDefault("0", 0.0));
        dto.setOne(hourlyData.getOrDefault("1", 0.0));
        dto.setTwo(hourlyData.getOrDefault("2", 0.0));
        dto.setThree(hourlyData.getOrDefault("3", 0.0));
        dto.setFour(hourlyData.getOrDefault("4", 0.0));
        dto.setFive(hourlyData.getOrDefault("5", 0.0));
        dto.setSix(hourlyData.getOrDefault("6", 0.0));
        dto.setSeven(hourlyData.getOrDefault("7", 0.0));
        dto.setEight(hourlyData.getOrDefault("8", 0.0));
        dto.setNine(hourlyData.getOrDefault("9", 0.0));
        dto.setTen(hourlyData.getOrDefault("10", 0.0));
        dto.setEleven(hourlyData.getOrDefault("11", 0.0));
        dto.setTwelve(hourlyData.getOrDefault("12", 0.0));
        dto.setThirteen(hourlyData.getOrDefault("13", 0.0));
        dto.setFourteen(hourlyData.getOrDefault("14", 0.0));
        dto.setFifteen(hourlyData.getOrDefault("15", 0.0));
        dto.setSixteen(hourlyData.getOrDefault("16", 0.0));
        dto.setSeventeen(hourlyData.getOrDefault("17", 0.0));
        dto.setEighteen(hourlyData.getOrDefault("18", 0.0));
        dto.setNineteen(hourlyData.getOrDefault("19", 0.0));
        dto.setTwenty(hourlyData.getOrDefault("20", 0.0));
        dto.setTwentyOne(hourlyData.getOrDefault("21", 0.0));
        dto.setTwentyTwo(hourlyData.getOrDefault("22", 0.0));
        dto.setTwentyThree(hourlyData.getOrDefault("23", 0.0));
        return dto;
    }
}