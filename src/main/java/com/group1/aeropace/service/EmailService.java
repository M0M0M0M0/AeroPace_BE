package com.group1.aeropace.service;

import com.group1.aeropace.config.MailProperties;
import com.group1.aeropace.entity.Order;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateBuilder templateBuilder;
    private final MailProperties mailProperties;

    @Async("emailTaskExecutor")
    public void sendOrderConfirmationEmail(Order order) {
        String toEmail = order.getUser().getEmail();
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("[Email] User {} không có email, bỏ qua gửi xác nhận đơn {}",
                    order.getUser().getId(), order.getOrderCode());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailProperties.getFrom(), mailProperties.getFromName());
            helper.setTo(toEmail);
            helper.setSubject("Xác nhận đơn hàng #" + order.getOrderCode());
            helper.setText(templateBuilder.buildOrderConfirmation(order), true);

            mailSender.send(message);
            log.info("[Email] Gửi xác nhận đơn {} thành công → {}", order.getOrderCode(), toEmail);

        } catch (Exception e) {
            log.error("[Email] Gửi xác nhận đơn {} thất bại: {}", order.getOrderCode(), e.getMessage(), e);
        }
    }

    @Async("emailTaskExecutor")
    public void sendRefundConfirmationEmail(Order order) {
        String toEmail = order.getUser().getEmail();
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("[Email] User {} không có email, bỏ qua gửi xác nhận refund đơn {}",
                    order.getUser().getId(), order.getOrderCode());
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailProperties.getFrom(), mailProperties.getFromName());
            helper.setTo(toEmail);
            helper.setSubject("Xác nhận hoàn tiền đơn hàng #" + order.getOrderCode());
            helper.setText(templateBuilder.buildRefundConfirmation(order), true);

            mailSender.send(message);
            log.info("[Email] Gửi xác nhận refund đơn {} thành công → {}", order.getOrderCode(), toEmail);

        } catch (Exception e) {
            log.error("[Email] Gửi xác nhận refund đơn {} thất bại: {}", order.getOrderCode(), e.getMessage(), e);
        }
    }
}