package ru.netology.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import ru.netology.entity.UserEntity;



public interface UserRepo extends JpaRepository<UserEntity, Long> {
    UserEntity findByLogin(String login);
    UserEntity findByAuthToken(String authToken);
}