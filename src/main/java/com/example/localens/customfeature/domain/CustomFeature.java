package com.example.localens.customfeature.domain;

import com.example.localens.member.domain.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "custom_feature")
@Getter
@Setter
public class CustomFeature {

    @Id
    @Column(name = "feature_uuid", length = 36)
    private String featureUuid;

    @Column(name = "feature_name")
    private String featureName;

    @Column(name = "formula", length = 1000)
    private String formula;

    @Column(name = "variables", columnDefinition = "JSON")
    private String variables; // JSON 형태로 저장

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_uuid", referencedColumnName = "member_uuid")
    private Member member;

    public CustomFeature() {
        this.featureUuid = java.util.UUID.randomUUID().toString();
    }
}
