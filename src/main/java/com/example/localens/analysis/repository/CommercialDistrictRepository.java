package com.example.localens.analysis.repository;

import com.example.localens.analysis.domain.CommercialDistrict;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommercialDistrictRepository extends JpaRepository<CommercialDistrict, Integer> {
    @Query("SELECT c.districtName FROM CommercialDistrict c WHERE c.districtUuid = :districtUuid")
    String findDistrictNameByDistrictUuid(@Param("districtUuid") Integer districtUuid);

    @Query("SELECT DISTINCT c.districtName FROM CommercialDistrict c")
    List<String> findAllPlaces();

    @Query("SELECT c FROM CommercialDistrict c WHERE c.districtUuid = :districtUuid")
    Optional<CommercialDistrict> findCompleteDistrictByUuid(@Param("districtUuid") Integer districtUuid);

    @Query("SELECT c FROM CommercialDistrict c LEFT JOIN FETCH c.cluster WHERE c.districtUuid = :districtUuid")
    Optional<CommercialDistrict> findByDistrictUuid(@Param("districtUuid") Integer districtUuid);
}


