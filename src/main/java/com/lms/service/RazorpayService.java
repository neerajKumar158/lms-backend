package com.lms.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class RazorpayService {

    private final RazorpayClient razorpayClient;
    private final boolean razorpayEnabled;
    private final String keyId;

    private final String keySecret;

    public RazorpayService(
            @Value("${razorpay.key-id}") String keyId,
            @Value("${razorpay.key-secret}") String keySecret,
            @Value("${razorpay.enabled:true}") boolean enabled) throws RazorpayException {
        this.razorpayEnabled = enabled;
        this.keyId = keyId;
        this.keySecret = keySecret;
        
        if (enabled && !keyId.equals("rzp_test_1234567890") && !keySecret.equals("your_secret_key")) {
            this.razorpayClient = new RazorpayClient(keyId, keySecret);
        } else {
            this.razorpayClient = null;
        }
    }

    /**
     * Create a Razorpay order
     */
    public Map<String, Object> createOrder(BigDecimal amount, String currency, String orderId, String customerName, String customerEmail, String customerPhone) {
        if (!razorpayEnabled || razorpayClient == null) {
            return Map.of("error", "Razorpay is not configured. Please set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET");
        }

        try {
            JSONObject orderRequest = new JSONObject();
            // Amount in paise (multiply by 100)
            orderRequest.put("amount", amount.multiply(new BigDecimal("100")).intValue());
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", "order_" + orderId);
            
            // Customer details
            JSONObject notes = new JSONObject();
            notes.put("orderId", orderId);
            notes.put("customerName", customerName);
            if (customerEmail != null) notes.put("customerEmail", customerEmail);
            if (customerPhone != null) notes.put("customerPhone", customerPhone);
            orderRequest.put("notes", notes);

            Order order = razorpayClient.orders.create(orderRequest);

            return Map.of(
                    "id", order.get("id"),
                    "amount", order.get("amount"),
                    "currency", order.get("currency"),
                    "keyId", keyId,
                    "orderId", orderId
            );
        } catch (RazorpayException e) {
            e.printStackTrace();
            return Map.of("error", "Failed to create Razorpay order: " + e.getMessage());
        }
    }

    /**
     * Verify Razorpay payment signature
     */
    public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        if (!razorpayEnabled || razorpayClient == null) {
            return false;
        }

        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", razorpaySignature);

            return com.razorpay.Utils.verifyPaymentSignature(attributes, keySecret);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get payment details from Razorpay
     */
    public Map<String, Object> getPaymentDetails(String paymentId) {
        if (!razorpayEnabled || razorpayClient == null) {
            return Map.of("error", "Razorpay is not configured");
        }

        try {
            com.razorpay.Payment payment = razorpayClient.payments.fetch(paymentId);
            return Map.of(
                    "id", payment.get("id"),
                    "status", payment.get("status"),
                    "amount", payment.get("amount"),
                    "currency", payment.get("currency"),
                    "method", payment.get("method"),
                    "order_id", payment.get("order_id")
            );
        } catch (RazorpayException e) {
            e.printStackTrace();
            return Map.of("error", "Failed to fetch payment: " + e.getMessage());
        }
    }

    public boolean isEnabled() {
        return razorpayEnabled && razorpayClient != null;
    }

    public String getKeyId() {
        return keyId;
    }
}

