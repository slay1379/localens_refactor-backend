package com.example.localens.customfeature.repository;

import com.example.localens.customfeature.domain.CustomFeature;
import java.awt.Taskbar.Feature;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomFeatureRepository extends JpaRepository<CustomFeature, Long> {
    List<CustomFeature> findByUserId(Long userId);
}
