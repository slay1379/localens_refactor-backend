package com.example.localens.analysis.repository;

import com.example.localens.analysis.domain.MetricField;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MetricFieldRepository extends JpaRepository<MetricField, Long> {

    @Query("SELECT mf.fieldName FROM MetricField mf")
    List<String> findAllFieldNames();
}
