package com.example.localens.analysis.dto;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RadarDataDTO<T extends BaseRadarDistrictInfoDTO> {
    private T districtInfo;
    private Map<String, Integer> overallData;
    private Map<String, Object> topTwo;

    public void setDistrictInfo(T originalDTO) {
        if (originalDTO != null) {
            this.districtInfo = originalDTO;
        }
    }
}
