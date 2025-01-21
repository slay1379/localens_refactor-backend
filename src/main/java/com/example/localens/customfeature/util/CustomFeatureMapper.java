package com.example.localens.customfeature.util;

import com.example.localens.customfeature.DTO.CustomFeatureDTO;
import com.example.localens.customfeature.domain.CustomFeature;
import java.util.Map;
import java.util.Map.Entry;
import org.springframework.stereotype.Component;

@Component
public class CustomFeatureMapper {
    public CustomFeatureDTO toDTO(CustomFeature feature, Map<String, String> fieldMapping) {
        String translatedFormula = translateFormula(feature.getFormula(), fieldMapping);
        return new CustomFeatureDTO(
                feature.getFeatureUuid(),
                feature.getFeatureName(),
                translatedFormula
        );
    }

    private String translateFormula(String formula, Map<String, String> fieldMapping) {
        String translatedFormula = formula;
        for (Entry<String, String> entry : fieldMapping.entrySet()) {
            translatedFormula = translatedFormula.replace(entry.getValue(), entry.getKey());
        }
        return translatedFormula;
    }
}
