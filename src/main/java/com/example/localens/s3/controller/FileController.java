package com.example.localens.s3.controller;

import com.example.localens.s3.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@RestController
public class FileController {

    private final S3Service s3Service;

    public FileController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @GetMapping("/get-image-url")
    public ResponseEntity<String> getImageUrl(@RequestParam("key") String keyName) {
        String presignedUrl = s3Service.generatePresignedUrl("localens-image", keyName);
        return ResponseEntity.ok(presignedUrl);
    }
}
