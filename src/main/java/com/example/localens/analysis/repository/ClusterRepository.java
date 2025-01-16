package com.example.localens.analysis.repository;

import com.example.localens.analysis.domain.Cluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClusterRepository extends JpaRepository<Cluster, Integer> {

}
