package ru.netology.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String password;

    @Column(name = "auth_token")
    private String authToken;

    @Column(name = "token_expiry")
    private Instant tokenExpiry;

    public void setTokenExpiry(Instant plus) {
   this.tokenExpiry = plus;
    }
}