package com.sports.NetsCricket.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.sports.NetsCricket.dto.BookingRequest;
import com.sports.NetsCricket.dto.Response;
import com.sports.NetsCricket.service.interfac.IBookingService;

@RestController
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private IBookingService bookingService;

    // ✅ 1. Create Booking
    @PostMapping("/create")
    public ResponseEntity<Response> createBooking(@RequestBody BookingRequest request) {

        Response response = bookingService.createBooking(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // ✅ 2. Get My Bookings (JWT required)
    @GetMapping("/my")
    public ResponseEntity<Response> getMyBookings() {

        Response response = bookingService.getMyBookings();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // ✅ 3. Cancel Booking (ADMIN only)
    @PutMapping("/cancel/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Response> cancelBooking(@PathVariable Long id) {

        Response response = bookingService.cancelBooking(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}