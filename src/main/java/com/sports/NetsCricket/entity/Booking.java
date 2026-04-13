package com.sports.NetsCricket.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ User who booked
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // ✅ Booking Date
    private LocalDate bookingDate;

    // ✅ Flexible time (6:30 allowed)
    private LocalTime startTime;
    private LocalTime endTime;

    // ✅ Calculated fields
    private double totalHours;
    private double amount;

    // ✅ Status fields
    private String status;        // PENDING / BOOKED / CANCELLED
    private String paymentStatus; // PENDING / PAID
}