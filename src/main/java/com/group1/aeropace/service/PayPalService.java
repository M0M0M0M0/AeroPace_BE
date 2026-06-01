package com.group1.aeropace.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
public class PayPalService {

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();


    private String getPayPalAccessToken() {
        String credentials = clientId + ":" + clientSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encoded);

        HttpEntity<String> entity = new HttpEntity<>("grant_type=client_credentials", headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/v1/oauth2/token",
                HttpMethod.POST,
                entity,
                Map.class
        );

        if (response.getBody() == null || !response.getBody().containsKey("access_token")) {
            throw new RuntimeException("Không thể lấy PayPal access token");
        }

        return response.getBody().get("access_token").toString();
    }

    /**
     * Refund một giao dịch PayPal
     * @param captureId  — chính là paymentTransactionId (ch_xxx của PayPal capture)
     */
    public void refundPayPal(String captureId) {
        String payPalAccessToken = getPayPalAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + payPalAccessToken);

        String body = "{}";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/v2/payments/captures/" + captureId + "/refund",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

//            log.info("PayPal refund thành công: {}", response.getBody());
        } catch (Exception e) {
            log.error("PayPal refund thất bại: {}", e.getMessage());
            throw new RuntimeException("Refund PayPal thất bại: " + e.getMessage());
        }
    }
}