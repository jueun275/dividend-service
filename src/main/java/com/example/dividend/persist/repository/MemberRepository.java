package com.example.dividend.persist.repository;

import com.example.dividend.persist.entity.MemberEntity;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
