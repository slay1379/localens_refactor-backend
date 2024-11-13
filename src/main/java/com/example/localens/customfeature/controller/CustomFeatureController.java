package com.example.localens.customfeature.controller;

import com.example.localens.customfeature.domain.BasicData;
import com.example.localens.customfeature.domain.CustomFeature;
import com.example.localens.customfeature.service.CustomFeatureService;
import java.util.List;
import java.util.Optional;
import javax.crypto.spec.OAEPParameterSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/custom-feature")
public class CustomFeatureController {

    @Autowired
    private CustomFeatureService customFeatureService;

    @GetMapping("/basic-data")
    public List<BasicData> getAllBasicData() {
        return customFeatureService.getAllBasicData();
    }

    @PostMapping("/create")
    public CustomFeature createCustomFeature(@RequestParam String metricName, @RequestParam String formula) {
        return customFeatureService.createCustomFeature(metricName, formula);
    }

    @GetMapping("/{id}")
    public Optional<CustomFeature> getCustomFeatureById(@PathVariable Long id) {
        return customFeatureService.getCustomFeatureById(id);
    }

    @GetMapping("/all")
    public List<CustomFeature> getAllCustomFeatures() {
        return customFeatureService.getAllCustomFeatures();
    }
}
