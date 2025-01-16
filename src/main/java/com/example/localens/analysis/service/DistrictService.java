    package com.example.localens.analysis.service;

    import com.example.localens.analysis.domain.Cluster;
    import com.example.localens.analysis.domain.CommercialDistrict;
    import com.example.localens.analysis.dto.ClusterDTO;
    import com.example.localens.analysis.dto.DistrictDTO;
    import com.example.localens.analysis.repository.ClusterRepository;
    import com.example.localens.analysis.repository.CommercialDistrictRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;

    @Service
    @RequiredArgsConstructor
    public class DistrictService {

        private final CommercialDistrictRepository districtRepository;
        private final ClusterRepository clusterRepository;

        public DistrictDTO getDistrictDTO(Integer districtUuid) {
            CommercialDistrict district = districtRepository.findById(districtUuid)
                    .orElseThrow(() -> new RuntimeException("District not found"));

            DistrictDTO districtDTO = new DistrictDTO();
            districtDTO.setDistrictName(district.getDistrictName());

            if (district.getCluster() != null) {
                Cluster clusterEntity = district.getCluster();
                ClusterDTO clusterDTO = new ClusterDTO();
                clusterDTO.setClusterUuid(clusterEntity.getClusterUuid());
                clusterDTO.setClusterName(clusterEntity.getClusterName());
            }

            return districtDTO;
        }
    }
