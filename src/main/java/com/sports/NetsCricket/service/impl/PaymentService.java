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
import com.sports.NetsCricket.entity.User;
import com.sports.NetsCricket.repo.BookingRepository;
import com.sports.NetsCricket.repo.PaymentRepository;
import com.sports.NetsCricket.service.interfac.IPaymentService;
import org.springframework.beans.factory.annotation.Value;


@Service
public class PaymentService implements IPaymentService {

    @Autowired
    private RazorpayClient razorpayClient;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private static final String SECRET = "xX5N33FYgBcHV0m99MIiWxmq";
    
    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.key.id}")
    private String keyId;

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
    
    
    @Override
    public Response createPaymentLink(Long bookingId) {

        Response response = new Response();

        try {

            // ✅ Get booking
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // ✅ Get user from booking
            User user = booking.getUser();

            if (user == null) {
                throw new RuntimeException("User not found for booking");
            }

            // ✅ Create Razorpay client
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            // ✅ Request object
            JSONObject request = new JSONObject();
            request.put("amount", (int) (booking.getAmount() * 100));
            request.put("currency", "INR");

            // ✅ Customer details from USER entity
            JSONObject customer = new JSONObject();
            customer.put("name", user.getName());     // 👈 change based on your User fields
            customer.put("email", user.getEmail());
            customer.put("contact", user.getPhoneNumber());

            request.put("customer", customer);

            // ✅ Notification
            JSONObject notify = new JSONObject();
            notify.put("sms", true);
            notify.put("email", true);
            request.put("notify", notify);

			/*
			 * // ✅ Callback request.put("callback_url",
			 * "http://localhost:8080/payment-success"); request.put("callback_method",
			 * "get");
			 */

            // ✅ Create payment link
            com.razorpay.PaymentLink paymentLink = client.paymentLink.create(request);

            // ✅ Save payment
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(booking.getAmount());
            payment.setStatus("CREATED");
            payment.setRazorpayOrderId(paymentLink.get("id"));

            paymentRepository.save(payment);

            // ✅ Response
            Map<String, Object> data = new HashMap<>();
            data.put("paymentLink", paymentLink.get("short_url"));

            response.setStatusCode(200);
            response.setData(data);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }

        return response;
    }
}
