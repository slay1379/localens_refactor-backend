package com.example.localens.analysis.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisRadarDistrictInfoDTO extends BaseRadarDistrictInfoDTO {
    private BigDecimal latitude;
    private BigDecimal longitude;
}
