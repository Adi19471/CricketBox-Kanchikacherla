package com.sports.NetsCricket.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.sports.NetsCricket.dto.BookingRequest;
import com.sports.NetsCricket.dto.Response;
import com.sports.NetsCricket.entity.Booking;
import com.sports.NetsCricket.entity.User;
import com.sports.NetsCricket.entity.WorkingHours;
import com.sports.NetsCricket.repo.BookingRepository;
import com.sports.NetsCricket.repo.UserRepository;
import com.sports.NetsCricket.repo.WorkingHoursRepository;
import com.sports.NetsCricket.service.interfac.IBookingService;

@Service
public class BookingService implements IBookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WorkingHoursRepository workingHoursRepository;

    // 🔥 PRICE PER HOUR (you can move to DB later)
    private static final double PRICE_PER_HOUR = 500;

    // ✅ CREATE BOOKING
    @Override
    public Response createBooking(BookingRequest request) {

        Response response = new Response();

        try {

            // ✅ 1. Get logged-in user
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // ✅ 2. Validate time
            long minutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();

            if (minutes <= 0) {
                throw new RuntimeException("Invalid time range");
            }

            if (minutes % 60 != 0) {
                throw new RuntimeException("Only full hour bookings allowed");
            }

            // ✅ 3. Check overlap
            List<Booking> overlaps = bookingRepository.findOverlappingBookings(
                    request.getBookingDate(),
                    request.getStartTime(),
                    request.getEndTime()
            );

            if (!overlaps.isEmpty()) {
                throw new RuntimeException("Slot already booked");
            }

            // ✅ 4. Calculate hours & amount
            double totalHours = minutes / 60.0;
            double amount = totalHours * PRICE_PER_HOUR;

            // ✅ 5. Save booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setBookingDate(request.getBookingDate());
            booking.setStartTime(request.getStartTime());
            booking.setEndTime(request.getEndTime());
            booking.setTotalHours(totalHours);
            booking.setAmount(amount);
            booking.setStatus("PENDING");
            booking.setPaymentStatus("PENDING");

            bookingRepository.save(booking);

            response.setStatusCode(200);
            response.setMessage("Booking created successfully");
            response.setData(booking);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }

        return response;
    }

    // ✅ GET MY BOOKINGS
    @Override
    public Response getMyBookings() {

        Response response = new Response();

        try {

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<Booking> bookings = bookingRepository.findByUserId(user.getId());

            response.setStatusCode(200);
            response.setData(bookings);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }

        return response;
    }

    // ✅ CANCEL BOOKING (ADMIN ONLY)
    @Override
    public Response cancelBooking(Long bookingId) {

        Response response = new Response();

        try {

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            booking.setStatus("CANCELLED");

            bookingRepository.save(booking);

            response.setStatusCode(200);
            response.setMessage("Booking cancelled successfully");

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }

        return response;
    }
    
    
    @Override
    public Response getBookedSlots(LocalDate bookingDate) {

        Response response = new Response();

        try {

            // ✅ 1. Get working hours (from master table)
            String day = bookingDate.getDayOfWeek().name();

            WorkingHours wh = workingHoursRepository.findByDayOfWeek(day)
                    .orElseThrow(() -> new RuntimeException("Working hours not set"));

            LocalTime start = wh.getStartTime();
            LocalTime end = wh.getEndTime();
            int duration = wh.getSlotDuration(); // ex: 30 mins

            // ✅ 2. Get booked slots
            List<Booking> bookings = bookingRepository.findByBookingDate(bookingDate);

            // ✅ 3. Generate ALL slots (this is your allSlots)
            List<Map<String, String>> allSlots = new ArrayList<>();

            LocalTime temp = start;

            while (temp.isBefore(end)) {

                LocalTime next = temp.plusMinutes(duration);

                Map<String, String> slot = new HashMap<>();
                slot.put("startTime", temp.toString());
                slot.put("endTime", next.toString());

                allSlots.add(slot);

                temp = next;
            }

            // ✅ 4. Booked slots
            List<Map<String, String>> bookedSlots = bookings.stream().map(b -> {
                Map<String, String> map = new HashMap<>();
                map.put("startTime", b.getStartTime().toString());
                map.put("endTime", b.getEndTime().toString());
                return map;
            }).toList();

            // ✅ 5. Available slots (remove overlaps)
            List<Map<String, String>> availableSlots = allSlots.stream()
                    .filter(slot -> bookings.stream().noneMatch(b ->
                            isOverlap(
                                    slot.get("startTime"),
                                    slot.get("endTime"),
                                    b.getStartTime().toString(),
                                    b.getEndTime().toString()
                            )
                    ))
                    .toList();

            // ✅ 6. Final response
            Map<String, Object> result = new HashMap<>();
            result.put("booked", bookedSlots);
            result.put("available", availableSlots);

            response.setStatusCode(200);
            response.setMessage("Slots fetched successfully");
            response.setData(result);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            e.printStackTrace();
        }

        return response;
    }
    
    
    private boolean isOverlap(String s1, String e1, String s2, String e2) {

        LocalTime start1 = LocalTime.parse(s1);
        LocalTime end1 = LocalTime.parse(e1);
        LocalTime start2 = LocalTime.parse(s2);
        LocalTime end2 = LocalTime.parse(e2);

        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
