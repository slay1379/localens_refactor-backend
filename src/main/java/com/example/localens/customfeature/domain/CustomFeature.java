package com.example.localens.customfeature.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class CustomFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String metricName;
    private String formula;

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }
}

