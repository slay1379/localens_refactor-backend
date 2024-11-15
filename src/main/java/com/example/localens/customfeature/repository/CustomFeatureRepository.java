package com.example.localens.customfeature.repository;

import com.example.localens.customfeature.domain.CustomFeature;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomFeatureRepository extends JpaRepository<CustomFeature, Long> {
    List<CustomFeature> findByMemberId(Long memberId);
}
