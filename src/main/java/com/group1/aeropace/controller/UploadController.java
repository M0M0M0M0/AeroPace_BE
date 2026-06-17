package com.group1.aeropace.controller;

import com.group1.aeropace.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/uploads")
public class UploadController {

    private final UploadService uploadService;

    // PUBLIC — Upload avatar (cần public vì dùng được lúc đăng ký, trước khi có JWT)
    // POST /api/v1/uploads/avatar
    @PostMapping("/avatar")
    public Map<String, String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = uploadService.uploadImage(file, "aeropace/avatars");
        return Map.of("url", url);
    }

    // USER — Upload ảnh review
    // POST /api/v1/uploads/review-image
    @PostMapping("/review-image")
    public Map<String, String> uploadReviewImage(@RequestParam("file") MultipartFile file) {
        String url = uploadService.uploadImage(file, "aeropace/reviews");
        return Map.of("url", url);
    }
}
