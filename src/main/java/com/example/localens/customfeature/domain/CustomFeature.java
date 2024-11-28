package com.example.localens.customfeature.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "custom_features")
@Getter
@Setter
public class CustomFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long featureId;

    private String userUuId;
    private String featureName;

    @Column(length = 1000)
    private String formula;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CustomFeature() {
    }
}
