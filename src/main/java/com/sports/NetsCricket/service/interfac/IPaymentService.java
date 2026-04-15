package com.sports.NetsCricket.service.interfac;

import com.sports.NetsCricket.dto.Response;

public interface IPaymentService {

    Response createOrder(Long bookingId);

    Response verifyPayment(String orderId, String paymentId, String signature);

	Response createPaymentLink(Long bookingId);
}