package com.sports.NetsCricket.repo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sports.NetsCricket.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ✅ Get bookings by user
    List<Booking> findByUserId(Long userId);

    // ✅ Overlap check
    @Query("""
        SELECT b FROM Booking b
        WHERE b.bookingDate = :date
        AND (
            :startTime < b.endTime AND
            :endTime > b.startTime
        )
        AND b.status <> 'CANCELLED'
    """)
    List<Booking> findOverlappingBookings(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime
    );

    // ✅ Date-wise bookings (for reports)
    List<Booking> findByBookingDate(LocalDate date);
}
