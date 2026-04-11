package com.group1.shop_runner.service;

import com.group1.shop_runner.dto.post.request.PostRequest;
import com.group1.shop_runner.dto.post.response.PostResponse;
import com.group1.shop_runner.entity.Post;
import com.group1.shop_runner.entity.User;
import com.group1.shop_runner.shared.exception.AppException;
import com.group1.shop_runner.shared.exception.ErrorCode;
import com.group1.shop_runner.repository.PostRepository;
import com.group1.shop_runner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    // =========================================================
    // API 1: Tạo post
    // =========================================================
    public PostResponse createPost(PostRequest request) {

        postRepository.findBySlug(request.getSlug())
                .ifPresent(p -> {
                    throw new AppException(ErrorCode.POST_ALREADY_EXISTS);
                });

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setSlug(request.getSlug());
        post.setContent(request.getContent());
        post.setThumbnail(request.getThumbnail());
        post.setUser(user);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        Post saved = postRepository.save(post);

        return mapToResponse(saved);
    }

    // =========================================================
    // API 2: Lấy tất cả post
    // =========================================================
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // API 3: Lấy post theo id
    // =========================================================
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        return mapToResponse(post);
    }

    // =========================================================
    // API 4: Lấy post theo user
    // =========================================================
    public List<PostResponse> getPostsByUser(Long userId) {
        return postRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // API 5: Update post
    // =========================================================
    public PostResponse updatePost(Long id, PostRequest request) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        post.setTitle(request.getTitle());
        post.setSlug(request.getSlug());
        post.setContent(request.getContent());
        post.setThumbnail(request.getThumbnail());
        post.setUser(user);
        post.setUpdatedAt(LocalDateTime.now());

        Post updated = postRepository.save(post);

        return mapToResponse(updated);
    }

    // =========================================================
    // API 6: Delete post
    // =========================================================
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        postRepository.delete(post);
    }

    private PostResponse mapToResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getContent(),
                post.getThumbnail(),
                post.getUser().getId(),
                post.getUser().getUsername()
        );
    }
}