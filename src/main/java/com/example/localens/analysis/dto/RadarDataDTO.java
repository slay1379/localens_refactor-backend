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
public class RadarDataDTO {
    private DistrictDTO districtInfo;
    private Map<String, Integer> overallData;
    private Map<String, Object> topTwo;

    public void setDistrictInfo(DistrictDTO originalDTO) {
        DistrictDTO filteredDTO = new DistrictDTO();
        filteredDTO.setDistrictName(originalDTO.getDistrictName());
        filteredDTO.setClusterName(originalDTO.getClusterName());
        this.districtInfo = filteredDTO;
    }
}
