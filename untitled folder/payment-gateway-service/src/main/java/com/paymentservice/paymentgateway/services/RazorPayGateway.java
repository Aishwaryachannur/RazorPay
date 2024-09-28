package com.paymentservice.paymentgateway.services;
import com.paymentservice.paymentgateway.dtos.PaymentLinkRequestDto;
import com.paymentservice.paymentgateway.models.PaymentStatus;
import com.paymentservice.paymentgateway.repositories.PaymentRepository;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.razorpay.Payment;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class RazorPayGateway implements PaymentGateway{

    private final RazorpayClient razorpayClient;

    @Autowired
    public RazorPayGateway(RazorpayClient razorpayClient) {
        this.razorpayClient = razorpayClient;
    }

    @Override
    public String createPaymentLink(PaymentLinkRequestDto paymentLinkRequestDto) {
         /*
           There are generally two ways to integrate with a 3rd party
           1. Make an api call
           2. Client sdk (Code in a jar)
        */

        JSONObject paymentLinkRequest = new JSONObject();
        paymentLinkRequest.put("amount",paymentLinkRequestDto.getAmount());
        paymentLinkRequest.put("currency","INR");
        paymentLinkRequest.put("expire_by", LocalDate.now().plusDays(7).atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        paymentLinkRequest.put("reference_id",paymentLinkRequestDto.getOrderId());
        paymentLinkRequest.put("description","Payment for order no " + paymentLinkRequestDto.getOrderId());

        JSONObject customer = new JSONObject();
        customer.put("name",paymentLinkRequestDto.getCustomerName());
        customer.put("contact",paymentLinkRequestDto.getPhone());
        customer.put("email","geekysanjay@gmail.com");
        paymentLinkRequest.put("customer",customer);

        JSONObject notes = new JSONObject();
        notes.put("policy_name","Jeevan Bima");
        paymentLinkRequest.put("notes",notes);
        paymentLinkRequest.put("callback_url","https://www.geekysanjay.com");
        paymentLinkRequest.put("callback_method","get");

        try {
            PaymentLink  payment = razorpayClient.paymentLink.create(paymentLinkRequest);
            return payment.get("short_url");
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create payment link", e);
        }
    }

    @Override
    public PaymentStatus getStatus(String paymentId, String orderId) {

        try {
            Payment  payment = razorpayClient.payments.fetch(paymentId);
            String statusType = payment.get("status");
            return switch (statusType) {
                case "captured" -> PaymentStatus.SUCCESS;
                case "failed" -> PaymentStatus.FAILURE;
                default -> PaymentStatus.INITIATED;
            };
        } catch (RazorpayException e) {
            throw new RuntimeException("Unable to fetch the payment details", e);
        }
    }
}
