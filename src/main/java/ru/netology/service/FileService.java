package ru.netology.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.entity.FileEntity;
import ru.netology.entity.UserEntity;
import ru.netology.repository.FileRepo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final FileRepo fileRepo;

    private static final String UPLOAD_DIR = "uploads";

    @Transactional
    public void uploadFile(UserEntity user, MultipartFile multipartFile, String filename) throws IOException {
        log.info("Uploading file '{}' for user '{}'", filename, user.getLogin());

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                log.debug("Created upload directory: {}", dir.getAbsolutePath());
            } else {
                log.error("Failed to create upload directory: {}", dir.getAbsolutePath());
                throw new IOException("Failed to create upload directory");
            }
        }

        File file = new File(dir, filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
            log.debug("File '{}' saved physically at '{}'", filename, file.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save file '{}' to filesystem for user '{}': {}", filename, user.getLogin(), e.getMessage());
            throw e;
        }

        try {
            FileEntity entity = new FileEntity();
            entity.setFilename(filename);
            entity.setSize(multipartFile.getSize());
            entity.setUser(user);
            fileRepo.save(entity);
            log.info("File '{}' successfully saved to DB for user '{}'", filename, user.getLogin());
        } catch (Exception e) {
            // Если сохранение в БД не удалось, удаляем файл из ФС для целостности
            if (file.exists() && !file.delete()) {
                log.warn("Failed to delete physical file '{}' after DB save failure", filename);
            }
            log.error("Failed to save file '{}' to DB for user '{}': {}", filename, user.getLogin(), e.getMessage());
            throw new IOException("Failed to save file metadata to database", e);
        }
    }

    public List<FileEntity> getFiles(UserEntity user) {
        log.debug("Fetching files for user '{}'", user.getLogin());
        return fileRepo.findAllByUser(user);
    }

    @Transactional
    public void deleteFile(UserEntity user, String filename) {
        log.info("Deleting file '{}' for user '{}'", filename, user.getLogin());

        FileEntity fileEntity = fileRepo.findByUserAndFilename(user, filename)
                .orElseThrow(() -> {
                    log.warn("Attempt to delete non-existent file '{}' for user '{}'", filename, user.getLogin());
                    return new RuntimeException("File not found");
                });

        // Сначала удаляем из БД
        fileRepo.delete(fileEntity);
        log.debug("File '{}' deleted from DB for user '{}'", filename, user.getLogin());

        // Затем удаляем физический файл
        File file = new File(UPLOAD_DIR, filename);
        if (file.exists()) {
            if (file.delete()) {
                log.debug("Physical file '{}' deleted successfully", filename);
            } else {
                log.warn("Failed to delete physical file '{}' after DB deletion", filename);
                // Не бросаем исключение, т.к. запись уже удалена из БД
            }
        } else {
            log.warn("Physical file '{}' not found during deletion", filename);
        }

        log.info("File '{}' successfully deleted for user '{}'", filename, user.getLogin());
    }

    public byte[] downloadFile(UserEntity user, String filename) throws IOException {
        log.info("Downloading file '{}' for user '{}'", filename, user.getLogin());

        FileEntity fileEntity = fileRepo.findByUserAndFilename(user, filename)
                .orElseThrow(() -> {
                    log.warn("Attempt to download non-existent file '{}' for user '{}'", filename, user.getLogin());
                    return new RuntimeException("File not found");
                });

        File file = new File(UPLOAD_DIR, filename);
        if (!file.exists()) {
            log.error("Physical file '{}' not found for user '{}'", filename, user.getLogin());
            throw new IOException("Physical file not found");
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] fileContent = fis.readAllBytes();
            log.info("File '{}' successfully downloaded for user '{}', size: {} bytes", 
                    filename, user.getLogin(), fileContent.length);
            return fileContent;
        } catch (IOException e) {
            log.error("Failed to read file '{}' for user '{}': {}", filename, user.getLogin(), e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void updateFile(UserEntity user, String filename, String newFilename) throws IOException {
        log.info("Updating file '{}' to '{}' for user '{}'", filename, newFilename, user.getLogin());

        FileEntity fileEntity = fileRepo.findByUserAndFilename(user, filename)
                .orElseThrow(() -> {
                    log.warn("Attempt to update non-existent file '{}' for user '{}'", filename, user.getLogin());
                    return new RuntimeException("File not found");
                });

        File oldFile = new File(UPLOAD_DIR, filename);
        File newFile = new File(UPLOAD_DIR, newFilename);

        if (!oldFile.exists()) {
            log.error("Physical file '{}' not found for user '{}'", filename, user.getLogin());
            throw new IOException("Physical file not found");
        }

        // Переименовываем физический файл
        try {
            if (!oldFile.renameTo(newFile)) {
                log.error("Failed to rename physical file '{}' to '{}' for user '{}'", 
                        filename, newFilename, user.getLogin());
                throw new IOException("Failed to rename file");
            }
            log.debug("Physical file '{}' renamed to '{}'", filename, newFilename);
        } catch (Exception e) {
            log.error("Error renaming physical file '{}' to '{}' for user '{}': {}", 
                    filename, newFilename, user.getLogin(), e.getMessage());
            throw new IOException("Failed to rename file", e);
        }

        // Обновляем запись в БД
        try {
            fileEntity.setFilename(newFilename);
            fileRepo.save(fileEntity);
            log.info("File '{}' successfully updated to '{}' in DB for user '{}'", 
                    filename, newFilename, user.getLogin());
        } catch (Exception e) {
            // Если обновление БД не удалось, пытаемся вернуть старое имя файла
            if (newFile.exists() && !newFile.renameTo(oldFile)) {
                log.error("Failed to revert file rename after DB update failure");
            }
            log.error("Failed to update file '{}' in DB for user '{}': {}", 
                    filename, user.getLogin(), e.getMessage());
            throw new IOException("Failed to update file metadata in database", e);
        }
    }
}