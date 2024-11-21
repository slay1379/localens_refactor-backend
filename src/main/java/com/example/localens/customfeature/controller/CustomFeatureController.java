package com.example.localens.customfeature.controller;

import com.example.localens.customfeature.domain.CustomFeature;
import com.example.localens.customfeature.service.CustomFeatureService;
import com.example.localens.influx.InfluxDBService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customFeatures")
public class CustomFeatureController {

    private final CustomFeatureService customFeatureService;
    private final InfluxDBService influxDBService;

    @Value("${influxdb.measurement}")
    private String measurement;

    @Autowired
    public CustomFeatureController(CustomFeatureService customFeatureService, InfluxDBService influxDBService) {
        this.customFeatureService = customFeatureService;
        this.influxDBService = influxDBService;
    }

    //피처 생성 폼
    @GetMapping("/new")
    public String showCustomFeatureForm(Model model) {
        model.addAttribute("custom_feature", new CustomFeature());
        model.addAttribute("dataColumns", influxDBService.getFieldKeys(measurement));
        return "custom_feature_form";
    }
}
