package com.example.localens.customfeature.service;

import com.example.localens.customfeature.domain.CustomFeature;
import com.example.localens.customfeature.repository.CustomFeatureRepository;
import com.example.localens.member.domain.Member;
import com.example.localens.member.repository.MemberRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomFeatureService {

    private final CustomFeatureRepository customFeatureRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public CustomFeatureService(CustomFeatureRepository customFeatureRepository,
                                MemberRepository memberRepository) {
        this.customFeatureRepository = customFeatureRepository;
        this.memberRepository = memberRepository;
    }

    public CustomFeature saveCustomFeature(CustomFeature customFeature) {
        return customFeatureRepository.save(customFeature);
    }

    public List<CustomFeature> getCustomFeatureByUserUuid(String userUuid) {
        Member member = memberRepository.findById(userUuid).orElse(null);
        if (member == null) {
            return List.of();
        }
        return customFeatureRepository.findByMember(member);
    }

    public CustomFeature getCustomFeatureById(Long customFeatureId) {
        return customFeatureRepository.findById(customFeatureId).orElse(null);
    }

    public void deleteFeature(Long customFeatureId) {
        customFeatureRepository.deleteById(customFeatureId);
    }
}
