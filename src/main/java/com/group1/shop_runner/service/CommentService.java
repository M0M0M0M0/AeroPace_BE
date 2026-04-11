package com.group1.shop_runner.service;

import com.group1.shop_runner.dto.comment.request.CommentRequest;
import com.group1.shop_runner.dto.comment.response.CommentResponse;
import com.group1.shop_runner.entity.Comment;
import com.group1.shop_runner.entity.Product;
import com.group1.shop_runner.entity.User;
import com.group1.shop_runner.shared.exception.AppException;
import com.group1.shop_runner.shared.exception.ErrorCode;
import com.group1.shop_runner.repository.CommentRepository;
import com.group1.shop_runner.repository.ProductRepository;
import com.group1.shop_runner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    // =========================================================
    // API 1: Tạo comment
    // =========================================================
    public CommentResponse createComment(CommentRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setProduct(product);
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);

        return mapToResponse(saved);
    }

    // =========================================================
    // API 2: Lấy comment theo product
    // =========================================================
    public List<CommentResponse> getCommentsByProduct(Long productId) {
        return commentRepository.findByProductId(productId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // =========================================================
    // API 3: Xóa comment
    // =========================================================
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));

        commentRepository.delete(comment);
    }

    private CommentResponse mapToResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getProduct().getId(),
                comment.getProduct().getName(),
                comment.getContent()
        );
    }
}