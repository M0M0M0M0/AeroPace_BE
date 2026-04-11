package com.group1.shop_runner.controller;

import com.group1.shop_runner.dto.comment.request.CommentRequest;
import com.group1.shop_runner.dto.comment.response.CommentResponse;
import com.group1.shop_runner.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
//@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // =========================================================
    // API 1: Tạo comment
    // POST /api/v1/comments
    // =========================================================
    @PostMapping
    public CommentResponse createComment(@RequestBody @Valid CommentRequest request) {
        return commentService.createComment(request);
    }

    // =========================================================
    // API 2: Lấy comment theo product
    // GET /api/v1/comments/product/{productId}
    // =========================================================
    @GetMapping("/product/{productId}")
    public List<CommentResponse> getCommentsByProduct(@PathVariable Long productId) {
        return commentService.getCommentsByProduct(productId);
    }

    // =========================================================
    // API 3: Xóa comment
    // DELETE /api/v1/comments/{id}
    // =========================================================
    @DeleteMapping("/{id}")
    public String deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return "Delete comment successfully";
    }
}