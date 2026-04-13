package com.sports.NetsCricket.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class BookingRequest {

    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
}
