package com.example.localens.member.repository;

import com.example.localens.member.domain.Member;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<Member> findByName(String name);
    Optional<Member> findByNameAndEmail(String name, String email);
    Optional<Member> findByResetToken(String resetToken);
}
