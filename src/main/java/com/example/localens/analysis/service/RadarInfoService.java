package com.example.localens.analysis.service;

import com.example.localens.analysis.domain.CommercialDistrict;
import com.example.localens.analysis.repository.CommercialDistrictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RadarInfoService {

    private final CommercialDistrictRepository commercialDistrictRepository;

    public CommercialDistrict getCommercialDistrictByUuid(Integer districtUuid) {
        return commercialDistrictRepository.findById(districtUuid)
                .orElseThrow(() -> new IllegalArgumentException("Invalid district UUID: " + districtUuid));
    }
}
