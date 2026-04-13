package com.sports.NetsCricket.service.interfac;

import com.sports.NetsCricket.dto.BookingRequest;
import com.sports.NetsCricket.dto.Response;

public interface IBookingService {

    Response createBooking(BookingRequest request);

    Response getMyBookings();

    Response cancelBooking(Long bookingId);
}