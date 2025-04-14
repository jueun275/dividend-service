package com.example.dividend.model;

import com.example.dividend.persist.entity.MemberEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class Member {
    private Long id;

    private String username;

    private String password;

    private List<String> roles;

    @Builder
    public Member(Long id, String username, String password, List<String> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public static Member fromEntity(MemberEntity memberEntity) {
        return Member.builder()
            .id(memberEntity.getId())
            .username(memberEntity.getUsername())
            .password(memberEntity.getPassword())
            .roles(memberEntity.getRoles())
            .build();
    }
}
