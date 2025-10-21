package ru.netology.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.entity.FileEntity;
import ru.netology.entity.UserEntity;
import ru.netology.repository.FileRepo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileRepo fileRepo;

    private static final String UPLOAD_DIR = "uploads";

    public void uploadFile(UserEntity user, MultipartFile multipartFile, String filename) throws IOException {
        // создаём папку uploads если её нет
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        // сохраняем файл физически
        File file = new File(dir, filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }

        // сохраняем запись в БД
        FileEntity entity = new FileEntity();
        entity.setFilename(filename);
        entity.setSize(multipartFile.getSize());
        entity.setUser(user);
        fileRepo.save(entity);
    }

    public List<FileEntity> getFiles(UserEntity user) {
        return fileRepo.findAllByUser(user);
    }

    public void deleteFile(UserEntity user, String filename) {
        FileEntity fileEntity = fileRepo.findByUserAndFilename(user, filename)
                .orElseThrow(() -> new RuntimeException("File not found"));

        File file = new File(UPLOAD_DIR, filename);
        if (file.exists()) {
            file.delete();
        }

        fileRepo.delete(fileEntity);
    }
}