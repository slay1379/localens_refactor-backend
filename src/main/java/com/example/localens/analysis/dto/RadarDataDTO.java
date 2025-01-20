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
    private RadarDistrictInfoDTO districtInfo;  // 여기서 타입이 RadarDistrictInfoDTO임
    private Map<String, Integer> overallData;
    private Map<String, Object> topTwo;

    public void setDistrictInfo(RadarDistrictInfoDTO originalDTO) {  // 파라미터 타입도 RadarDistrictInfoDTO로 변경
        if (originalDTO != null) {
            RadarDistrictInfoDTO filteredDTO = new RadarDistrictInfoDTO();
            filteredDTO.setDistrictName(originalDTO.getDistrictName());
            filteredDTO.setClusterName(originalDTO.getClusterName());
            this.districtInfo = filteredDTO;
        }
    }
}
