package com.example.localens.member.controller;

import com.example.localens.member.dto.*;
import com.example.localens.member.jwt.TokenProvider;
import com.example.localens.member.service.AuthService;
import com.example.localens.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/member")
@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final AuthService authService;
    private final TokenProvider tokenProvider;


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



