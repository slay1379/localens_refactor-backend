package com.example.localens.analysis.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompareTwoDistrictsDTO {

    private RadarDataDTO district1;
    private RadarDataDTO district2;

    private List<DifferenceItemDTO> topDifferences;
}
