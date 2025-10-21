package ru.netology.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.netology.entity.FileEntity;
import ru.netology.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface FileRepo extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findAllByUser(UserEntity user);
    Optional<FileEntity> findByUserAndFilename(UserEntity user, String filename);
}