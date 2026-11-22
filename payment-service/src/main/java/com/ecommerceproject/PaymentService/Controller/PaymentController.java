package com.ecommerceproject.PaymentService.Controller;

import com.ecommerceproject.PaymentService.Dto.PaymentRequestDto;
import com.ecommerceproject.PaymentService.Dto.PaymentResponseDto;
import com.ecommerceproject.PaymentService.Service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponseDto> pay(@RequestBody PaymentRequestDto request) {
        return new ResponseEntity<>(paymentService.process(request), HttpStatus.CREATED);
    }
}
