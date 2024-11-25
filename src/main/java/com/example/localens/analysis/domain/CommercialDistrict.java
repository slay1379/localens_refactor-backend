package com.example.localens.analysis.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "commercial_districts")
public class CommercialDistrict {

    @Id
    @Column(name = "district_uuid")
    private Integer districtUuid; // 또는 UUID 타입 사용 가능

    @Column(name = "district_name")
    private String districtName;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @ManyToOne
    @JoinColumn(name = "cluster_uuid", nullable = false)
    private Cluster cluster;

}