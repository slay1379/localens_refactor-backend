package com.example.localens.analysis.dto;

import lombok.Data;

@Data
public class PopulationDetailsResponseDTO {
    private PopulationHourlyDataDTO hourlyFloatingPopulation;
    private PopulationHourlyDataDTO hourlyStayVisitRatio;
    private PopulationHourlyDataDTO hourlyCongestionRateChange;
    private PopulationHourlyDataDTO stayPerVisitorDuration;
    private PopulationHourlyDataDTO hourlyAvgStayDurationChange;
    private AgeGroupStayPatternDTO ageGroupStayPattern;
    private NationalityPatternDTO nationalityStayPattern;
}
