package com.example.localens.customfeature.service;

import com.example.localens.customfeature.domain.BasicData;
import com.example.localens.customfeature.domain.CustomFeature;
import com.example.localens.customfeature.repository.BasicDataRepository;
import com.example.localens.customfeature.repository.CustomFeatureRepository;
import jakarta.transaction.Transactional;
import java.beans.Transient;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomFeatureService {

    @Autowired
    private BasicDataRepository basicDataRepository;

    @Autowired
    private CustomFeatureRepository customFeatureRepository;

    public List<BasicData> getAllBasicData() {
        return basicDataRepository.findAll();
    }

    @Transactional
    public CustomFeature createCustomFeature(String metricName, String formula) {
        CustomFeature customFeature = new CustomFeature();
        customFeature.setMetricName(metricName);
        customFeature.setFormula(formula);
        return customFeatureRepository.save(customFeature);
    }

    public Optional<CustomFeature> getCustomFeatureById(Long id) {
        return customFeatureRepository.findById(id);
    }

    public List<CustomFeature> getAllCustomFeatures() {
        return customFeatureRepository.findAll();
    }

    public double calculateCustomFeatureValue(CustomFeature customFeature) {
        return 0.0; //구현 필요
    }
}
