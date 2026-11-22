package com.ecommerceproject.PaymentService.Service;

import com.ecommerceproject.PaymentService.Dto.PaymentRequestDto;
import com.ecommerceproject.PaymentService.Dto.PaymentResponseDto;
import com.ecommerceproject.PaymentService.Messaging.PaymentEventPublisher;
import com.ecommerceproject.PaymentService.Model.Payment;
import com.ecommerceproject.PaymentService.Model.PaymentStatus;
import com.ecommerceproject.PaymentService.Repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentService(PaymentRepository paymentRepository, PaymentEventPublisher paymentEventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    public PaymentResponseDto process(PaymentRequestDto request) {
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        // Mock gateway: payments always succeed. A real one would call an external PSP.
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCreatedAt(new Date());

        Payment saved = paymentRepository.save(payment);
        paymentEventPublisher.publishPaymentCompleted(saved);

        PaymentResponseDto response = new PaymentResponseDto();
        response.setPaymentId(saved.getId());
        response.setOrderId(saved.getOrderId());
        response.setAmount(saved.getAmount());
        response.setStatus(saved.getStatus().name());
        return response;
    }
}
