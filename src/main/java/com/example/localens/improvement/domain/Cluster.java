package com.example.localens.improvement.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cluster")
@Getter
@Setter
public class Cluster {

    @Id
    @Column(name = "cluster_uuid")
    private String clusterUuid;

    private String clusterName;
    private Integer clusterNumber;
}
