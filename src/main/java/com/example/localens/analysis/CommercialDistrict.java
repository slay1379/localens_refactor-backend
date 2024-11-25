package com.example.localens.analysis;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column(name = "cluster_name")
    private String clusterName;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

}