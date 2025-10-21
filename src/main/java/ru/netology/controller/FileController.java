package ru.netology.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.service.AuthService;
import ru.netology.service.FileService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final AuthService authService;

    @GetMapping("/list")
    public ResponseEntity<?> getFiles(@RequestHeader("auth-token") String token,
                                      @RequestParam(value = "limit", required = false) Integer limit) {
        var user = authService.getUserByToken(token);
        var files = fileService.getFiles(user);
        if (limit != null && limit < files.size()) {
            files = files.subList(0, limit);
        }
        return ResponseEntity.ok(files);
    }

    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(@RequestHeader("auth-token") String token,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam("filename") String filename) {
        System.out.println("DEBUG: file = " + (file != null ? file.getOriginalFilename() : "null"));
        System.out.println("DEBUG: filename = " + filename);
        try {
            var user = authService.getUserByToken(token);
            fileService.uploadFile(user, file, filename);
            return ResponseEntity.ok(Map.of("message", "File uploaded"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(@RequestHeader("auth-token") String token,
                                        @RequestParam("filename") String filename) {
        var user = authService.getUserByToken(token);
        fileService.deleteFile(user, filename);
        return ResponseEntity.ok(Map.of("message", "File deleted"));
    }
}