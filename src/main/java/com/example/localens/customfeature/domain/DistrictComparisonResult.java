package com.example.localens.customfeature.domain;

import com.example.localens.customfeature.DTO.DistrictResponseDTO;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DistrictComparisonResult {
    private final DistrictResponseDTO district1;
    private final DistrictResponseDTO district2;

    public Map<String, DistrictResponseDTO> toMap() {
        return Map.of(
                "district1", district1,
                "district2", district2
        );
    }
}
