package com.group1.shop_runner.repository;

import com.group1.shop_runner.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByProductId(Long productId);

    List<Comment> findByUserId(Long userId);
}