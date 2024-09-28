package com.paymentservice.paymentgateway.services;

import com.paymentservice.paymentgateway.dtos.PaymentLinkRequestDto;
import com.paymentservice.paymentgateway.models.PaymentDetails;
import com.paymentservice.paymentgateway.models.PaymentStatus;
import com.paymentservice.paymentgateway.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentService {

     private final PaymentGateway paymentGateway;
     private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentGateway paymentGateway, PaymentRepository paymentRepository) {
        this.paymentGateway = paymentGateway;
        this.paymentRepository = paymentRepository;
    }

    public String createLink(String orderId){
        /*
        Make a call to order service and get the order details.
        OrderDetail order = restTemplate.getMapping(orderId)
        name = order.getCustomerName()
        amount = order.getAmount()
        phone = order.getCustomerPhone()
       */
        PaymentLinkRequestDto paymentLinkRequestDto = new PaymentLinkRequestDto();
        paymentLinkRequestDto.setCustomerName("Sanjay");
        paymentLinkRequestDto.setOrderId(orderId);
        paymentLinkRequestDto.setPhone("8310206130");
        paymentLinkRequestDto.setAmount(100);

        // Generate payment link using the payment gateway
        String paymentLink = paymentGateway.createPaymentLink(paymentLinkRequestDto);

        // Save payment details in the repository
        PaymentDetails paymentResponse = new PaymentDetails();
        paymentResponse.setPaymentLink(paymentLink);
        paymentResponse.setOrderId(orderId);
        paymentRepository.save(paymentResponse);

        return paymentLink;
    }

    public PaymentStatus getPaymentStatus(String paymentId, String orderId) {
        // Retrieve payment details by order ID
        Optional<PaymentDetails> paymentDetails = paymentRepository.findByOrderId(orderId);

        if(paymentDetails.isEmpty()){
            throw new RuntimeException("Payment not found");
        }

        // Get payment status from the payment gateway
        PaymentStatus status = paymentGateway.getStatus(paymentId, orderId);

        // Update and save payment details with the new status
        PaymentDetails paymentResponse = paymentDetails.get();
        paymentResponse.setStatus(status);
        paymentResponse.setPaymentId(paymentId);
        paymentRepository.save(paymentResponse);

        return status;
    }
}
