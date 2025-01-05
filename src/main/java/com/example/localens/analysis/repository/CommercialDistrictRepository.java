package com.example.localens.analysis.repository;

import com.example.localens.analysis.domain.CommercialDistrict;
import java.util.List;
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
}


