package com.example.localens.customfeature.repository;

import com.example.localens.customfeature.domain.CustomFeature;
import com.example.localens.member.domain.Member;
import java.awt.Taskbar.Feature;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomFeatureRepository extends JpaRepository<CustomFeature, String> {
    List<CustomFeature> findByMember(Member member);
}
