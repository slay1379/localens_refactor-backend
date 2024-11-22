package com.example.localens.member.controller;

import com.example.localens.member.domain.Member;
import com.example.localens.member.dto.*;
import com.example.localens.member.jwt.TokenProvider;
import com.example.localens.member.service.AuthService;
import com.example.localens.member.service.MemberFinderService;
import com.example.localens.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequestMapping("/api/member")
@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final AuthService authService;
    private final MemberFinderService memberFinderService;
//    private final TokenProvider tokenProvider;


    @PostMapping("/signup")
    public ResponseEntity<MemberResponseDto> signup(@Valid @RequestBody MemberRequestDto memberRequestDto) {
        return ResponseEntity.ok(memberService.signup(memberRequestDto));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@Valid @RequestBody LoginDto loginDto) {
        return ResponseEntity.ok(authService.login(loginDto));
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(@Valid @RequestBody TokenRequestDto tokenRequestDto) {
        return ResponseEntity.ok(authService.reissue(tokenRequestDto));
    }

    @GetMapping("/find-email")
    public ResponseEntity<String> findEmailByName(@RequestParam String name) {
        String email = memberFinderService.findEmailByName(name);
        return ResponseEntity.ok(email);
    }

    // 이름과 이메일로 사용자를 검증
    @PostMapping("/validate-member")
    public ResponseEntity<String> validateMember(@Valid @RequestBody ValidateRequestDto validateRequestDto) {
        Optional<Member> memberOptional = memberService.validateMember(validateRequestDto.getName(), validateRequestDto.getEmail());
        if (memberOptional.isPresent()) {
            String resetToken = memberService.generateResetToken(memberOptional.get());
            return ResponseEntity.ok(resetToken); // 클라이언트에 토큰 전달
        } else {
            return ResponseEntity.badRequest().body("등록되지 않은 사용자입니다.");
        }
    }

    // 비밀번호 재설정 엔드포인트
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody PasswordResetRequestDto passwordResetRequestDto) {
        memberService.resetPassword(passwordResetRequestDto.getResetToken(),
                passwordResetRequestDto.getNewPassword(),
                passwordResetRequestDto.getNewPasswordCheck());
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }


//    @GetMapping("/getUuid")
//    public String getCurrentUuid(@RequestHeader("Authorization") String token) {
//        return tokenProvider.getCurrentUuid(token);
//    }

//    @GetMapping("/getUuid")
//    public String getCurrentUuid(@RequestHeader("Authorization") String token) {
//        String accessToken = token.substring(7); // "Bearer " 제거
//        return tokenProvider.getCurrentUuid(accessToken);
//    }

}



