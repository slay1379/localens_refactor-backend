package com.example.localens.customfeature.domain;

import com.example.localens.member.domain.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;

@Entity
@Getter
public class CustomFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String metricName;
    private String formula;
    private double calculatedValue;

    @ManyToOne
    private Member member;

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}

