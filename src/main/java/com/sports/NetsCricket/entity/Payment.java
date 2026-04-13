package com.sports.NetsCricket.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "payments")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// ✅ Link with Booking
	@OneToOne
	@JoinColumn(name = "booking_id")
	private Booking booking;

	private String razorpayOrderId;
	private String razorpayPaymentId;

	private double amount;

	private String status; // CREATED / PAID / FAILED

	private LocalDateTime paymentDate;
}
