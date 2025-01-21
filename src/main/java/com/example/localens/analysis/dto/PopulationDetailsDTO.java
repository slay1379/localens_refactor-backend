package com.example.localens.analysis.dto;

import com.example.localens.analysis.domain.CommercialDistrict;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PopulationDetailsDTO {
    private String districtName;
    private String clusterName;

    public PopulationDetailsDTO(CommercialDistrict district) {
        this.districtName = district.getDistrictName();
        this.clusterName = district.getCluster() != null ? district.getCluster().getClusterName() : null;
    }
}