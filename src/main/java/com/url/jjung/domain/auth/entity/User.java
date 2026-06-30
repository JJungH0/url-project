package com.url.jjung.domain.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 50, nullable = false)
    private String nickname;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;

    @PrePersist
    private void onCreate() {
        this.createAt = LocalDateTime.now();
    }

    @Builder
    public User(String email, String pw, String nickname) {
        this.email = email;
        this.password = pw;
        this.nickname = nickname;
    }
}
