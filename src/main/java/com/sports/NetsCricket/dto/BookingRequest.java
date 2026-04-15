package com.sports.NetsCricket.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

public class BookingRequest {

    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int number;
	public LocalDate getBookingDate() {
		return bookingDate;
	}
	public void setBookingDate(LocalDate bookingDate) {
		this.bookingDate = bookingDate;
	}
	public LocalTime getStartTime() {
		return startTime;
	}
	public void setStartTime(LocalTime startTime) {
		this.startTime = startTime;
	}
	public LocalTime getEndTime() {
		return endTime;
	}
	public void setEndTime(LocalTime endTime) {
		this.endTime = endTime;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
    
    
}
