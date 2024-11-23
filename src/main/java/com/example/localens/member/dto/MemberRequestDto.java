package com.example.localens.member.dto;

import com.example.localens.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberRequestDto {

    private String name;
    private String email;
    private String password;
    private String passwordCheck;

    public Member toMember(PasswordEncoder passwordEncoder) {
        if (!password.equals(passwordCheck)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return Member.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();
    }

}
