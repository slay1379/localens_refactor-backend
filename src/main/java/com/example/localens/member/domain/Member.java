package com.example.localens.member.domain;


import com.example.localens.member.dto.MemberResponseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_uuid")
    private UUID memberUuid;

    @NotNull
    @Length(min = 2, max = 15)
    @Column(name = "username", length = 15, nullable = false)
    private String name;

    @NotNull
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
    @Column(length = 100, nullable = false, unique = true)
    private String email;

    @NotNull
    @Column(nullable = false)
    private String password;

    @Column(name = "reset_token")
    private String resetToken; // 비밀번호 재설정 토큰


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(nullable = true, updatable = false)
    private LocalDate createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(nullable = true)
    private LocalDate updatedAt;



    @Builder
    public Member(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }


//    @PreUpdate
//    protected void onUpdate() {
//        this.updatedAt = LocalDate.now(); // 현재 날짜로 갱신
//    }
//
//    @PrePersist
//    protected void onCreate() {
//        this.createdAt = LocalDate.now(); // 현재 날짜로 설정
//        this.updatedAt = LocalDate.now(); // 엔티티 생성 시에도 updatedAt을 현재 날짜로 설정
//    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

//    public void setResetToken(String resetToken) {
//        this.resetToken = resetToken;
//    }




}
