package com.example.localens.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetRequestDto {
    private String resetToken;   // 비밀번호 재설정을 위한 토큰
    private String newPassword;  // 새로운 비밀번호
    private String newPasswordCheck;
}
