package com.sports.NetsCricket.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sports.NetsCricket.dto.Response;
import com.sports.NetsCricket.service.interfac.IPaymentService;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private IPaymentService paymentService;

    // ✅ Create Razorpay Order
    @PostMapping("/create-order/{bookingId}")
    public ResponseEntity<Response> createOrder(@PathVariable Long bookingId) {
    	
    	System.out.println("-----------createOrder-----"+bookingId);

        Response response = paymentService.createOrder(bookingId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // ✅ Verify Payment
    @PostMapping("/verify")
    public ResponseEntity<Response> verifyPayment(@RequestBody Map<String, String> data) {

        Response response = paymentService.verifyPayment(
                data.get("razorpay_order_id"),
                data.get("razorpay_payment_id"),
                data.get("razorpay_signature")
        );

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    
    
    
    @PostMapping("/create-payment-link/{bookingId}")
    public ResponseEntity<Response> createPaymentLink(@PathVariable Long bookingId) {

        Response response = paymentService.createPaymentLink(bookingId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
