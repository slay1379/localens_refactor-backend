package com.example.localens.customfeature.service;

import com.example.localens.customfeature.domain.CustomFeature;
import com.example.localens.customfeature.repository.CustomFeatureRepository;
import com.example.localens.influx.InfluxDBService;
import com.example.localens.member.domain.Member;
import com.example.localens.member.repository.MemberRepository;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomFeatureService {

    private final CustomFeatureRepository customFeatureRepository;
    private final MemberRepository memberRepository;
    private final InfluxDBService influxDBService;

    @Autowired
    public CustomFeatureService(CustomFeatureRepository customFeatureRepository,
                                MemberRepository memberRepository,
                                InfluxDBService influxDBService) {
        this.customFeatureRepository = customFeatureRepository;
        this.memberRepository = memberRepository;
        this.influxDBService = influxDBService;
    }

    public CustomFeature saveCustomFeature(CustomFeature customFeature, UUID userUuid) {
        Member member = memberRepository.findById(userUuid).orElse(null);
        if (member == null) {
            throw new IllegalArgumentException("Invalid user UUID");
        }
        customFeature.setMember(member);
        return customFeatureRepository.save(customFeature);
    }

    public List<CustomFeature> getCustomFeaturesByUserUuid(UUID userUuid) {
        Member member = memberRepository.findById(userUuid).orElse(null);
        if (member == null) {
            return List.of();
        }
        return customFeatureRepository.findByMember(member);
    }

    public CustomFeature getCustomFeatureById(String customFeatureId) {
        return customFeatureRepository.findById(customFeatureId).orElse(null);
    }

    public void deleteFeature(String customFeatureId) {
        customFeatureRepository.deleteById(customFeatureId);
    }

    public double calculateCustomFeatureValue(CustomFeature customFeature, Integer districtUuid) {
        String formula = customFeature.getFormula();
        Map<String, Double> variables = influxDBService.getLatestMetricsByDistrictUuid(
                String.valueOf(districtUuid));

        try {
            Expression e = new ExpressionBuilder(formula)
                    .variables(variables.keySet())
                    .build();

            for (Map.Entry<String, Double> entry : variables.entrySet()) {
                e.setVariable(entry.getKey(), entry.getValue());
            }

            return e.evaluate();
        } catch (Exception e) {
            System.err.println("커스텀 피처를 계산하는 데 오류 발생" + districtUuid + ": " + e.getMessage());
            return 0.0;
        }
    }
}
