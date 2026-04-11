package com.group1.shop_runner.controller;

import com.group1.shop_runner.dto.post.request.PostRequest;
import com.group1.shop_runner.dto.post.response.PostResponse;
import com.group1.shop_runner.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@CrossOrigin(origins = "*")
public class PostController {

    @Autowired
    private PostService postService;

    // =========================================================
    // API 1: Tạo post
    // POST /api/v1/posts
    // =========================================================
    @PostMapping
    public PostResponse createPost(@RequestBody @Valid PostRequest request) {
        return postService.createPost(request);
    }

    // =========================================================
    // API 2: Lấy tất cả post
    // GET /api/v1/posts
    // =========================================================
    @GetMapping
    public List<PostResponse> getAllPosts() {
        return postService.getAllPosts();
    }

    // =========================================================
    // API 3: Lấy post theo id
    // GET /api/v1/posts/{id}
    // =========================================================
    @GetMapping("/{id}")
    public PostResponse getPostById(@PathVariable Long id) {
        return postService.getPostById(id);
    }

    // =========================================================
    // API 4: Lấy post theo user
    // GET /api/v1/posts/user/{userId}
    // =========================================================
    @GetMapping("/user/{userId}")
    public List<PostResponse> getPostsByUser(@PathVariable Long userId) {
        return postService.getPostsByUser(userId);
    }

    // =========================================================
    // API 5: Update post
    // PUT /api/v1/posts/{id}
    // =========================================================
    @PutMapping("/{id}")
    public PostResponse updatePost(@PathVariable Long id,
                                   @RequestBody @Valid PostRequest request) {
        return postService.updatePost(id, request);
    }

    // =========================================================
    // API 6: Delete post
    // DELETE /api/v1/posts/{id}
    // =========================================================
    @DeleteMapping("/{id}")
    public String deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return "Delete post successfully";
    }
}