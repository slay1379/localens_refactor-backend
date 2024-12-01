package com.example.localens.member.domain;


import com.example.localens.member.dto.MemberResponseDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "member_uuid")
    private String memberUuid;

    @NotNull
    @Length(min = 2, max = 15)
    @Column(length = 15, nullable = false)
    private String name;

    @NotNull
    @Pattern(regexp = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")
    @Column(length = 100, nullable = false)
    private String email;

    @NotNull
    @Column(nullable = false)
    private String password;

    private String resetToken; // 비밀번호 재설정 토큰

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate updatedAt;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate createdAt;



    @Builder
    public Member(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }


    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDate.now(); // 현재 날짜로 갱신
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now(); // 현재 날짜로 설정
        this.updatedAt = LocalDate.now(); // 엔티티 생성 시에도 updatedAt을 현재 날짜로 설정
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

//    public void setResetToken(String resetToken) {
//        this.resetToken = resetToken;
//    }




}
