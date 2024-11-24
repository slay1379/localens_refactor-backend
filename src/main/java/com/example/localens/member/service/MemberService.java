package com.example.localens.member.service;

import com.example.localens.member.domain.Member;
import com.example.localens.member.dto.MemberResponseDto;
import com.example.localens.member.dto.MemberRequestDto;
import com.example.localens.member.exception.AlreadyRegisteredException;
import com.example.localens.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;



    @Transactional
    public MemberResponseDto signup(MemberRequestDto memberRequestDto) {
        if (memberRepository.existsByEmail(memberRequestDto.getEmail())) {
            throw new AlreadyRegisteredException("이미 가입되어 있는 유저입니다");
        }

        Member member = memberRequestDto.toMember(passwordEncoder);
        return MemberResponseDto.of(memberRepository.save(member));
    }

    // 이름과 이메일로 사용자가 존재하는지 확인하는 메서드
    public Optional<Member> validateMember(String name, String email) {
        return memberRepository.findByNameAndEmail(name, email);
    }

    // 사용자가 검증된 후 비밀번호 재설정을 위한 토큰 생성
    public String generateResetToken(Member member) {
        String resetToken = UUID.randomUUID().toString();
        member.setResetToken(resetToken); // Member 엔티티에 resetToken 필드 추가 필요
        memberRepository.save(member);
        return resetToken;
    }

    // 비밀번호 재설정 로직
    public void resetPassword(String resetToken, String newPassword, String newPasswordCheck) {
        if (!newPassword.equals(newPasswordCheck)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        Optional<Member> memberOptional = memberRepository.findByResetToken(resetToken);
        if (memberOptional.isEmpty()) {
            throw new RuntimeException("유효하지 않은 토큰입니다.");
        }

        Member member = memberOptional.get();
        member.updatePassword(passwordEncoder.encode(newPassword));
        member.setResetToken(null); // 토큰 사용 후 무효화
        memberRepository.save(member);
    }

}


