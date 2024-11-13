package com.example.localens.customfeature.repository;

import com.example.localens.customfeature.domain.BasicData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BasicDataRepository extends JpaRepository<BasicData, Long> {
}
