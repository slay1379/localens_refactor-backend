package com.example.localens.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PopulationDetailsTransformedDTO {
    private PopulationHourlyDataDTO hourlyFloatingPopulation;
    private PopulationHourlyDataDTO hourlyStayVisitRatio;
    private PopulationHourlyDataDTO hourlyCongestionRateChange;
    private PopulationHourlyDataDTO stayPerVisitorDuration;
    private PopulationHourlyDataDTO hourlyAvgStayDurationChange;

    private AgeGroupStayPatternDTO ageGroupStayPattern;
    private NationalityPatternDTO nationalityStayPattern;
}
