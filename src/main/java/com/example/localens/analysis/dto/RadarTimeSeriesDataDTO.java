package com.example.localens.analysis.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RadarTimeSeriesDataDTO<T extends BaseRadarDistrictInfoDTO> extends RadarDataDTO<T> {
    private List<LocalDateTime> timeSeriesData;
}
