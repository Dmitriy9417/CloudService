package ru.netology.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.service.AuthService;
import ru.netology.service.FileService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final FileService fileService;
    private final AuthService authService;

    @GetMapping("/list")
    public ResponseEntity<?> getFiles(@RequestHeader("auth-token") String token,
                                      @RequestParam(value = "limit", required = false) Integer limit) {
        log.debug("Received request to get files list with limit: {}", limit);
        var user = authService.getUserByToken(token);
        if (user == null) {
            log.warn("Unauthorized attempt to get files list");
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        var files = fileService.getFiles(user);
        if (limit != null && limit < files.size()) {
            files = files.subList(0, limit);
        }
        log.info("Returning {} files for user '{}'", files.size(), user.getLogin());
        return ResponseEntity.ok(files);
    }

    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(@RequestHeader("auth-token") String token,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam("filename") String filename) {
        log.debug("Received request to upload file '{}'", filename);
        try {
            var user = authService.getUserByToken(token);
            if (user == null) {
                log.warn("Unauthorized attempt to upload file '{}'", filename);
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }
            fileService.uploadFile(user, file, filename);
            log.info("File '{}' successfully uploaded by user '{}'", filename, user.getLogin());
            return ResponseEntity.ok(Map.of("message", "File uploaded"));
        } catch (Exception e) {
            log.error("Error uploading file '{}': {}", filename, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/file")
    public ResponseEntity<Resource> downloadFile(@RequestHeader("auth-token") String token,
                                                  @RequestParam("filename") String filename) {
        log.debug("Received request to download file '{}'", filename);
        try {
            var user = authService.getUserByToken(token);
            if (user == null) {
                log.warn("Unauthorized attempt to download file '{}'", filename);
                return ResponseEntity.status(401).build();
            }
            byte[] fileContent = fileService.downloadFile(user, filename);
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            log.info("File '{}' successfully downloaded by user '{}'", filename, user.getLogin());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileContent.length)
                    .body(resource);
        } catch (Exception e) {
            log.error("Error downloading file '{}': {}", filename, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/file")
    public ResponseEntity<?> updateFile(@RequestHeader("auth-token") String token,
                                         @RequestParam("filename") String filename,
                                         @RequestBody Map<String, String> body) {
        log.debug("Received request to update file '{}'", filename);
        try {
            var user = authService.getUserByToken(token);
            if (user == null) {
                log.warn("Unauthorized attempt to update file '{}'", filename);
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }
            String newFilename = body.get("filename");
            if (newFilename == null || newFilename.isBlank()) {
                log.warn("New filename not provided for update request");
                return ResponseEntity.badRequest().body(Map.of("message", "New filename is required"));
            }
            fileService.updateFile(user, filename, newFilename);
            log.info("File '{}' successfully updated to '{}' by user '{}'", filename, newFilename, user.getLogin());
            return ResponseEntity.ok(Map.of("message", "File updated"));
        } catch (Exception e) {
            log.error("Error updating file '{}': {}", filename, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(@RequestHeader("auth-token") String token,
                                        @RequestParam("filename") String filename) {
        log.debug("Received request to delete file '{}'", filename);
        try {
            var user = authService.getUserByToken(token);
            if (user == null) {
                log.warn("Unauthorized attempt to delete file '{}'", filename);
                return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
            }
            fileService.deleteFile(user, filename);
            log.info("File '{}' successfully deleted by user '{}'", filename, user.getLogin());
            return ResponseEntity.ok(Map.of("message", "File deleted"));
        } catch (Exception e) {
            log.error("Error deleting file '{}': {}", filename, e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}