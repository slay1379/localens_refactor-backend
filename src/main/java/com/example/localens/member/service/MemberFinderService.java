package com.example.localens.member.service;

import com.example.localens.member.domain.Member;
import com.example.localens.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberFinderService {
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public String findEmailByName(String name) {
        Member member = memberRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("해당 이름으로 회원을 찾을 수 없습니다."));
        return member.getEmail();
    }

    @Transactional(readOnly = true)
    public String findPasswordByNameAndEmail(String name, String email) {
        Member member = memberRepository.findByNameAndEmail(name, email)
                .orElseThrow(() -> new RuntimeException("해당 정보로 회원을 찾을 수 없습니다."));
        return member.getPassword();
    }
}
