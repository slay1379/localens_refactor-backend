package com.example.localens.analysis.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "cluster")
public class Cluster {

    @Id
    @Column(name = "cluster_uuid")
    private Integer clusterUuid;

    @Column(name = "cluster_name")
    private String clusterName;

    @OneToMany(mappedBy = "cluster", cascade = CascadeType.ALL)
    private List<CommercialDistrict> commercialDistricts = new ArrayList<>();

}
