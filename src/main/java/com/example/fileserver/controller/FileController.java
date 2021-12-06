package com.example.fileserver.controller;

import com.example.fileserver.payload.UploadFileResponse;
import com.example.fileserver.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private Environment env;

    //file upload
    @PostMapping("/files")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName,
                file.getContentType(), file.getSize());
    }

    //download file
    @GetMapping("/files/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    //delete file
    @DeleteMapping("/files/{fileName:.+}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileName, HttpServletRequest request) {

        boolean del = false;
        try {
            del = Files.deleteIfExists(Path.of(FileStorageService.getPropertyPath()+"/" + fileName));
        } catch (IOException ex) {
            logger.info("Нет файла епты.");
        }

        logger.info(String.valueOf(del));
        if (del) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();

    }

    //get files (names)
    @GetMapping("/files")
    public List<UploadFileResponse> getFileNames(HttpServletRequest request) {
        List<UploadFileResponse> res = new ArrayList<>();
        try {
            res = Files.list(Path.of(FileStorageService.getPropertyPath())).map(a -> {
                try {
                    return new UploadFileResponse(a.getFileName().toString(), Files.probeContentType(a), Files.size(a));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    @GetMapping("/health")
    public String[] getHealth(HttpServletRequest request) {
        return env.getActiveProfiles();
    }
}