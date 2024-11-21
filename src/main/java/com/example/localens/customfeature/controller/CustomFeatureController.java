package com.example.localens.customfeature.controller;

import com.example.localens.customfeature.domain.CustomFeature;
import com.example.localens.customfeature.service.CustomFeatureService;
import com.example.localens.influx.InfluxDBService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
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

    // 피처 생성 폼
    @GetMapping("/new")
    public String showCustomFeatureForm(Model model) {
        model.addAttribute("custom_feature", new CustomFeature());
        model.addAttribute("dataColumns", influxDBService.getFieldKeys(measurement));
        return "custom_feature_form";
    }

    // 수식 검증 메서드
    private boolean isValidFormula(String formula) {
        try {
            // 변수 목록을 InfluxDBService에서 가져옴
            Set<String> variables = new HashSet<>(influxDBService.getFieldKeys(measurement));

            // 수식 파싱
            Expression e = new ExpressionBuilder(formula)
                    .variables(variables)
                    .build();

            for (String var : variables) {
                e.setVariable(var, 1);
            }

            e.evaluate();
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}
