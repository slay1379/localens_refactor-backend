package com.example.localens.customfeature.service;

import com.example.localens.customfeature.domain.CustomFeature;
import com.example.localens.customfeature.repository.CustomFeatureRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomFeatureService {

    private final CustomFeatureRepository customFeatureRepository;

    @Autowired
    public CustomFeatureService(CustomFeatureRepository customFeatureRepository) {
        this.customFeatureRepository = customFeatureRepository;
    }

    public CustomFeature saveCustomFeature(CustomFeature customFeature) {
        return customFeatureRepository.save(customFeature);
    }

    public List<CustomFeature> getCustomFeaturesByUserId(Long userId) {
        return customFeatureRepository.findByUserId(userId);
    }
}
