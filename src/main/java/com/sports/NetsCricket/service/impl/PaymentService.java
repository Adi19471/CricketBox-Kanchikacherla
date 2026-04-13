package com.sports.NetsCricket.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.sports.NetsCricket.dto.Response;
import com.sports.NetsCricket.entity.Booking;
import com.sports.NetsCricket.entity.Payment;
import com.sports.NetsCricket.repo.BookingRepository;
import com.sports.NetsCricket.repo.PaymentRepository;
import com.sports.NetsCricket.service.interfac.IPaymentService;

@Service
public class PaymentService implements IPaymentService {

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private static final String SECRET = "xX5N33FYgBcHV0m99MIiWxmq";

    // ✅ CREATE ORDER
    @Override
    public Response createOrder(Long bookingId) {
    	
    	System.out.println("---------createOrder---------"+bookingId);

        Response response = new Response();

        try {

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", (int) (booking.getAmount() * 100)); // paisa
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "booking_" + bookingId);
            
            System.out.println("---------point1---------");

            Order order = razorpayClient.orders.create(orderRequest);
            
            System.out.println("---------point2---------");

            // ✅ Save payment
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setRazorpayOrderId(order.get("id"));
            payment.setAmount(booking.getAmount());
            payment.setStatus("CREATED");
            
            System.out.println("---------point3---------");

            paymentRepository.save(payment);
            
            
         // ✅ Convert to safe response
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("orderId", order.get("id"));
            orderData.put("amount", order.get("amount"));
            orderData.put("currency", order.get("currency"));

            response.setStatusCode(200);
            response.setData(orderData); // ✅ FIXED

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }

        return response;
    }

    // ✅ VERIFY PAYMENT
    @Override
    public Response verifyPayment(String orderId, String paymentId, String signature) {

        Response response = new Response();

        try {

            // ✅ Verify signature
            String payload = orderId + "|" + paymentId;

            boolean isValid = Utils.verifySignature(payload, signature, SECRET);

            if (!isValid) {
                throw new RuntimeException("Invalid payment signature");
            }

            // ✅ Fetch payment
            Payment payment = paymentRepository.findByRazorpayOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // ✅ Update payment
            payment.setRazorpayPaymentId(paymentId);
            payment.setStatus("PAID");
            payment.setPaymentDate(LocalDateTime.now());

            paymentRepository.save(payment);

            // ✅ Update booking
            Booking booking = payment.getBooking();
            booking.setPaymentStatus("PAID");
            booking.setStatus("BOOKED");

            bookingRepository.save(booking);

            response.setStatusCode(200);
            response.setMessage("Payment successful");

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }

        return response;
    }
}
