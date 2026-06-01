package com.group1.aeropace.service;

import com.group1.aeropace.dto.StripeVerifyResult;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;

import com.stripe.param.RefundCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    public StripeVerifyResult createPaymentIntent(long amount, String currency) throws StripeException {
        Stripe.apiKey = stripeSecretKey;
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .build();
        PaymentIntent intent = PaymentIntent.create(params);
        return new StripeVerifyResult(intent.getId(), intent.getClientSecret());
    }

    public StripeVerifyResult verifyAndExtract(String paymentIntentId) throws StripeException {
        Stripe.apiKey = stripeSecretKey;
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

        if (!"succeeded".equals(intent.getStatus())) {
            throw new IllegalStateException("PaymentIntent chưa succeeded: " + intent.getStatus());
        }

        return new StripeVerifyResult(paymentIntentId, intent.getLatestCharge());
    }

    public String getChargeId(String paymentIntentId) throws StripeException {
        Stripe.apiKey = stripeSecretKey;
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
        return intent.getLatestCharge(); // ch_xxx
    }


    public Refund refundStripeCharge(String paymentIntentId) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId) // thay vì .setCharge()
                .build();

        return Refund.create(params);
    }
}

